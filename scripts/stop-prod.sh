#!/bin/bash

# CMMS11 Production Server Stop Script
# Usage: ./stop-prod.sh

set -e

echo "Stopping CMMS11 Production Server..."

# Stop the systemd service
if systemctl is-active --quiet cmms11; then
    echo "Stopping CMMS11 service..."
    sudo systemctl stop cmms11
    echo "CMMS11 service stopped successfully!"
else
    echo "CMMS11 service is not running"
fi

# Disable the service (optional - comment out if you want it to start on boot)
# sudo systemctl disable cmms11

# Kill any remaining Java processes (fallback)
echo "Checking for remaining Java processes..."
pkill -f "cmms11" || echo "No remaining CMMS11 processes found"

# Stop Gradle daemon
echo "Stopping Gradle daemon..."
./gradlew --stop

echo "CMMS11 Production Server stopped successfully!"
