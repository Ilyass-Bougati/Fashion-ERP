#!/usr/bin/bash

echo "Running the CD"
docker compose down
docker compose up --build -d
docker image prune -f