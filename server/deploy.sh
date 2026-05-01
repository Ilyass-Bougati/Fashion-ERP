#!/usr/bin/bash

if [ ! -d "src/main/resources/certs" ]; then
    echo "Certs not found, generating..."
    chmod +x keygen.sh
    ./keygen.sh
fi

echo "Packaging the application"
mvn package -DskipTests=true

echo "Running the CD"
docker compose up --build -d server
docker image prune -f