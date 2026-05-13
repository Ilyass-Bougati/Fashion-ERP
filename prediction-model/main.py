from fastapi import FastAPI, HTTPException
from contextlib import asynccontextmanager
import torch
from transformers import TimesFm2_5ModelForPrediction
from models import ForecastRequest, ForecastResponse

tfm_model = None
device = "cpu"


@asynccontextmanager
async def lifespan(app: FastAPI):
    global tfm_model
    global device

    if torch.cuda.is_available():
        device = "cuda"
    elif torch.backends.mps.is_available():
        device = "mps"

    tfm_model = TimesFm2_5ModelForPrediction.from_pretrained(
        "google/timesfm-2.5-200m-transformers",
        torch_dtype=torch.float32
    )

    tfm_model = tfm_model.to(device).eval()
    yield


app = FastAPI(
    title="ERP Prediction Engine",
    description="TimesFM 2.5 wrapper for sales and stock forecasting",
    lifespan=lifespan
)


@app.post("/api/forecast/series", response_model=ForecastResponse)
async def forecast_series(request: ForecastRequest):
    if tfm_model is None:
        raise HTTPException(status_code=503, detail="Model is still loading...")

    try:
        past_values = torch.tensor(request.historical_data, dtype=torch.float32, device=device)

        with torch.no_grad():
            outputs = tfm_model(
                past_values=past_values,
                return_dict=True
            )

        batch_point_forecast = outputs.mean_predictions[:, :request.horizon].cpu().tolist()
        quantiles = outputs.full_predictions[:, :request.horizon, :].cpu()

        if quantiles.shape[-1] >= 10:
            batch_lower_bounds = quantiles[:, :, 1].tolist()
            batch_upper_bounds = quantiles[:, :, -2].tolist()
        else:
            batch_lower_bounds = quantiles.min(dim=-1).values.tolist()
            batch_upper_bounds = quantiles.max(dim=-1).values.tolist()

        batch_point_forecast = [[max(0.0, val) for val in series] for series in batch_point_forecast]
        batch_lower_bounds = [[max(0.0, val) for val in series] for series in batch_lower_bounds]
        batch_upper_bounds = [[max(0.0, val) for val in series] for series in batch_upper_bounds]

        return ForecastResponse(
            predictions=batch_point_forecast,
            lower_bounds=batch_lower_bounds,
            upper_bounds=batch_upper_bounds,
            model_version="google/timesfm-2.5-200m-transformers"
        )

    except Exception as e:
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=f"Prediction failed: {str(e)}")