from pydantic import BaseModel, Field
from typing import List

class ForecastRequest(BaseModel):
    historical_data: List[float] = Field(..., min_length=10)
    horizon: int = Field(default=7, ge=1, le=30)

class ForecastResponse(BaseModel):
    predictions: List[float]
    lower_bounds: List[float]
    upper_bounds: List[float]
    model_version: str

