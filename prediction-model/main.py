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
        past_values = torch.tensor([request.historical_data], dtype=torch.float32, device=device)
        with torch.no_grad():
            outputs = tfm_model(
                past_values=past_values,
                return_dict=True
            )

        point_forecast = outputs.mean_predictions[0][:request.horizon].cpu().tolist()

        quantiles = outputs.full_predictions[0][:request.horizon].cpu()

        lower_bounds = quantiles[:, 1].tolist() if quantiles.shape[-1] >= 10 else quantiles.min(dim=-1).values.tolist()
        upper_bounds = quantiles[:, -2].tolist() if quantiles.shape[-1] >= 10 else quantiles.max(dim=-1).values.tolist()

        point_forecast = [max(0.0, val) for val in point_forecast]
        lower_bounds = [max(0.0, val) for val in lower_bounds]
        upper_bounds = [max(0.0, val) for val in upper_bounds]

        return ForecastResponse(
            predictions=point_forecast,
            lower_bounds=lower_bounds,
            upper_bounds=upper_bounds,
            model_version="google/timesfm-2.5-200m-transformers"
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Prediction failed: {str(e)}")