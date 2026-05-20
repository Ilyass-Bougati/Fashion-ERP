FROM python:3.13-slim AS builder

ARG UV_VERSION=0.6.14

ENV UV_COMPILE_BYTECODE=1 \
    UV_LINK_MODE=copy \
    UV_INSTALL_DIR=/usr/local/bin \
    PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
        curl \
        ca-certificates \
    && curl -Ls https://astral.sh/uv/install.sh \
       | UV_VERSION=${UV_VERSION} UV_INSTALL_DIR=/usr/local/bin sh \
    && rm -rf /var/lib/apt/lists/*

COPY pyproject.toml uv.lock ./
RUN uv sync \
        --frozen \
        --no-dev \
        --no-cache \
        --compile-bytecode

COPY main.py models.py ./


FROM python:3.13-slim AS runtime

ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    PATH="/app/.venv/bin:$PATH" \
    HF_HOME=/app/.cache/huggingface

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd --system appuser && \
    useradd --system --gid appuser --create-home appuser && \
    mkdir -p /app/.cache/huggingface && \
    chown -R appuser:appuser /app

COPY --from=builder --chown=appuser:appuser /app/.venv /app/.venv
COPY --from=builder --chown=appuser:appuser /app/main.py /app/models.py ./

USER appuser

EXPOSE 8000

ENTRYPOINT ["/app/.venv/bin/python", "-m", "uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]