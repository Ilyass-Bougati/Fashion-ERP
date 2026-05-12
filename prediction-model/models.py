from pydantic import BaseModel, Field
from typing import List

class ForecastRequest(BaseModel):
    historical_data: List[List[float]] = Field(..., description="Batch of historical data sequences")
    horizon: int = Field(default=7, ge=1, le=30, description="Number of future steps to predict")

class ForecastResponse(BaseModel):
    predictions: List[List[float]]
    lower_bounds: List[List[float]]
    upper_bounds: List[List[float]]
    model_version: str

