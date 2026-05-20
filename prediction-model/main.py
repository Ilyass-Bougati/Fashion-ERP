from fastapi import FastAPI, HTTPException
from contextlib import asynccontextmanager
import torch
import logging
import time
from transformers import TimesFm2_5ModelForPrediction
from models import ForecastRequest, ForecastResponse

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)-8s | %(message)s",
    datefmt="%H:%M:%S",
)
logger = logging.getLogger("forecast")

tfm_model = None
device = "cpu"


@asynccontextmanager
async def lifespan(app: FastAPI):
    global tfm_model, device

    if torch.cuda.is_available():
        device = "cuda"
        logger.info(f"GPU detected: {torch.cuda.get_device_name(0)}")
    elif torch.backends.mps.is_available():
        device = "mps"
        logger.info("Apple MPS detected")
    else:
        logger.info("No GPU found, running on CPU")

    logger.info("Loading TimesFM 2.5 model from HuggingFace...")
    t0 = time.time()
    tfm_model = TimesFm2_5ModelForPrediction.from_pretrained(
        "google/timesfm-2.5-200m-transformers",
        torch_dtype=torch.float32
    )
    tfm_model = tfm_model.to(device).eval()
    logger.info(f"Model loaded and ready on [{device}] in {time.time() - t0:.2f}s")

    yield

    logger.info("Shutting down — releasing model")


app = FastAPI(
    title="ERP Prediction Engine",
    description="TimesFM 2.5 wrapper for sales and stock forecasting",
    lifespan=lifespan
)


@app.get("/health")
async def health():
    return {"status": "ok", "model_loaded": tfm_model is not None}


@app.post("/api/forecast/series", response_model=ForecastResponse)
async def forecast_series(request: ForecastRequest):
    if tfm_model is None:
        raise HTTPException(status_code=503, detail="Model is still loading...")

    n_series = len(request.historical_data)
    series_lengths = [len(s) for s in request.historical_data]
    logger.info(
        f"Request received — {n_series} series | "
        f"lengths: {series_lengths} | horizon: {request.horizon}"
    )

    try:
        logger.info(f"Building input tensor on [{device}]...")
        past_values = torch.tensor(request.historical_data, dtype=torch.float32, device=device)
        logger.info(f"Tensor shape: {list(past_values.shape)}")

        logger.info("Running model inference...")
        t_infer = time.time()
        with torch.no_grad():
            outputs = tfm_model(past_values=past_values, return_dict=True)
        elapsed = time.time() - t_infer
        logger.info(f"Inference done in {elapsed:.3f}s")

        logger.info(
            f"mean_predictions shape: {list(outputs.mean_predictions.shape)} | "
            f"full_predictions shape: {list(outputs.full_predictions.shape)}"
        )

        batch_point_forecast = outputs.mean_predictions[:, :request.horizon].cpu().tolist()
        quantiles = outputs.full_predictions[:, :request.horizon, :].cpu()
        logger.info(f"Quantiles tensor shape after slicing: {list(quantiles.shape)}")

        if quantiles.shape[-1] >= 10:
            logger.info("Using quantile indices [1] and [-2] for bounds")
            batch_lower_bounds = quantiles[:, :, 1].tolist()
            batch_upper_bounds = quantiles[:, :, -2].tolist()
        else:
            logger.info("Fewer than 10 quantiles — using min/max for bounds")
            batch_lower_bounds = quantiles.min(dim=-1).values.tolist()
            batch_upper_bounds = quantiles.max(dim=-1).values.tolist()

        batch_point_forecast = [[max(0.0, v) for v in s] for s in batch_point_forecast]
        batch_lower_bounds   = [[max(0.0, v) for v in s] for s in batch_lower_bounds]
        batch_upper_bounds   = [[max(0.0, v) for v in s] for s in batch_upper_bounds]

        for i, series in enumerate(batch_point_forecast):
            logger.info(
                f"Series [{i}] forecast preview (first 5): "
                f"{[round(v, 2) for v in series[:5]]}"
            )

        logger.info("Response ready — returning forecast")
        return ForecastResponse(
            predictions=batch_point_forecast,
            lower_bounds=batch_lower_bounds,
            upper_bounds=batch_upper_bounds,
            model_version="google/timesfm-2.5-200m-transformers"
        )

    except Exception as e:
        import traceback
        logger.error(f"Prediction failed: {e}")
        logger.debug(traceback.format_exc())
        raise HTTPException(status_code=500, detail=f"Prediction failed: {str(e)}")