# Build the image for running Offer Engine MicroServices

# Run with the following to avoid PowerScript authority issues:
#   PowerShell.exe -ExecutionPolicy Bypass -File .\build_image.ps1
# Alternatively, remove restrictions for the particular process
#   Set-ExecutionPolicy Bypass -Scope Process

docker rmi fredgeorge/offer_engine_ruby_v3
docker build --tag="fredgeorge/offer_engine_ruby_v3:latest" .
