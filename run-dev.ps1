$ErrorActionPreference = "Stop"
$env:SPRING_PROFILES_ACTIVE = "dev"
docker compose up --build
