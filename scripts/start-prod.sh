#!/bin/bash

# CMMS11 Production Server Start Script
# Usage: ./start-prod.sh

set -e

echo "Starting CMMS11 Production Server..."

# Check if running as root or with sudo
if [[ $EUID -eq 0 ]]; then
   echo "This script should not be run as root for security reasons"
   exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

# Check if Gradle wrapper exists
if [ ! -f "./gradlew" ]; then
    echo "Error: gradlew not found. Please run this script from the project root directory."
    exit 1
fi

# Set environment variables for production
export SPRING_PROFILES_ACTIVE=prod
export JAVA_OPTS="-Xms1g -Xmx2g -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom"
export DB_PASSWORD=${DB_PASSWORD:-"changeme"}
export AWS_S3_BUCKET=${AWS_S3_BUCKET:-"prodYULSLAB-bucket"}

# Create necessary directories
sudo mkdir -p /opt/cmms11/storage/uploads
sudo mkdir -p /opt/cmms11/logs
sudo chown -R $USER:$USER /opt/cmms11

# Create systemd service file if it doesn't exist
if [ ! -f "/etc/systemd/system/cmms11.service" ]; then
    echo "Creating systemd service file..."
    sudo tee /etc/systemd/system/cmms11.service > /dev/null <<EOF
[Unit]
Description=CMMS11 Application
After=network.target

[Service]
Type=simple
User=$USER
WorkingDirectory=$(pwd)
ExecStart=$(pwd)/gradlew bootRun --args='--spring.profiles.active=prod'
Restart=always
RestartSec=10
Environment=SPRING_PROFILES_ACTIVE=prod
Environment=JAVA_OPTS=-Xms1g -Xmx2g -Dfile.encoding=UTF-8
Environment=DB_PASSWORD=${DB_PASSWORD}
Environment=AWS_S3_BUCKET=${AWS_S3_BUCKET}

[Install]
WantedBy=multi-user.target
EOF
    sudo systemctl daemon-reload
fi

# Build application
echo "Building application..."
./gradlew clean build -x test

# Start the service
echo "Starting CMMS11 service..."
sudo systemctl start cmms11
sudo systemctl enable cmms11

echo "CMMS11 Production Server started successfully!"
echo "Service status:"
sudo systemctl status cmms11 --no-pager
echo ""
echo "To view logs: sudo journalctl -u cmms11 -f"
echo "To stop service: sudo systemctl stop cmms11"
