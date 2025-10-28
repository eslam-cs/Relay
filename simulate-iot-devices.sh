#!/bin/bash

# IoT Device Simulation Script
# Simulates 3 distinct IoT devices sending data every second
# Usage: ./simulate-iot-devices.sh [duration_seconds] [token]

set -e

# Configuration
DURATION=${1:-30}  # Default 30 seconds
TOKEN=${2:-""}
API_URL="http://localhost:8081/api/readings"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to generate random value in range
random_value() {
    local min=$1
    local max=$2
    echo "scale=2; $min + ($max - $min) * $RANDOM / 32767" | bc
}

# Function to get current ISO timestamp
get_timestamp() {
    date -u +"%Y-%m-%dT%H:%M:%S.000Z"
}

# Function to send reading
send_reading() {
    local sensor_id=$1
    local device_type=$2
    local value=$3
    local timestamp=$4
    local color=$5
    
    local payload="{\"sensorId\":\"$sensor_id\",\"deviceType\":\"$device_type\",\"value\":$value,\"timestamp\":\"$timestamp\"}"
    
    if [ -z "$TOKEN" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST "$API_URL" \
            -H "Content-Type: application/json" \
            -d "$payload" 2>&1)
    else
        response=$(curl -s -w "\n%{http_code}" -X POST "$API_URL" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $TOKEN" \
            -d "$payload" 2>&1)
    fi
    
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" = "201" ] || [ "$http_code" = "200" ]; then
        echo -e "${color}📡 $device_type [$sensor_id] → $value ${NC}"
    else
        echo -e "${RED}❌ $device_type [$sensor_id] → Failed (HTTP $http_code)${NC}"
    fi
}

# Function to simulate one device
simulate_device() {
    local sensor_id=$1
    local device_type=$2
    local min_value=$3
    local max_value=$4
    local color=$5
    local unit=$6
    
    for ((i=1; i<=DURATION; i++)); do
        value=$(random_value $min_value $max_value)
        timestamp=$(get_timestamp)
        send_reading "$sensor_id" "$device_type" "$value" "$timestamp" "$color"
        sleep 1
    done
}

# Banner
echo -e "${BLUE}"
echo "╔═══════════════════════════════════════════════════════╗"
echo "║         IoT Device Simulation Started                ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo -e "${NC}"
echo "Duration: $DURATION seconds"
echo "API URL: $API_URL"
echo "Token: ${TOKEN:0:20}..."
echo ""
echo -e "${YELLOW}Starting 3 IoT devices...${NC}"
echo ""

# Check if API is reachable
if ! curl -s -f "$API_URL" > /dev/null 2>&1; then
    if ! curl -s -f "http://localhost:8081/api/device-types" > /dev/null 2>&1; then
        echo -e "${RED}Error: API not reachable at $API_URL${NC}"
        echo "Make sure the application is running:"
        echo "  docker compose up -d"
        exit 1
    fi
fi

# Get JWT token if not provided
if [ -z "$TOKEN" ]; then
    echo -e "${YELLOW}No token provided. Attempting to get one...${NC}"
    TOKEN_RESPONSE=$(curl -s -X POST http://localhost:8081/auth/token \
        -H "Content-Type: application/json" 2>&1)
    
    TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    
    if [ -z "$TOKEN" ]; then
        echo -e "${RED}Warning: Could not obtain token. Requests may fail.${NC}"
        echo "Response: $TOKEN_RESPONSE"
        echo ""
    else
        echo -e "${GREEN}✓ Token obtained${NC}"
        echo ""
    fi
fi

# Start simulation timestamp
START_TIME=$(date +%s)

# Run all 3 devices in parallel
(
    echo -e "${GREEN}🚀 Starting THERMOSTAT (THERMO-001)${NC}"
    simulate_device "THERMO-001" "THERMOSTAT" 18 26 "$GREEN" "°C"
) &

(
    echo -e "${BLUE}🚀 Starting HEART_RATE_MONITOR (HEART-001)${NC}"
    simulate_device "HEART-001" "HEART_RATE_MONITOR" 60 100 "$BLUE" "BPM"
) &

(
    echo -e "${YELLOW}🚀 Starting FUEL_METER (FUEL-001)${NC}"
    simulate_device "FUEL-001" "FUEL_METER" 30 95 "$YELLOW" "%"
) &

# Wait for all background jobs to complete
wait

END_TIME=$(date +%s)
ELAPSED=$((END_TIME - START_TIME))

echo ""
echo -e "${GREEN}╔═══════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║         Simulation Complete!                          ║${NC}"
echo -e "${GREEN}╚═══════════════════════════════════════════════════════╝${NC}"
echo ""
echo "Duration: $ELAPSED seconds"
echo "Total readings sent: $((DURATION * 3))"
echo ""
echo -e "${BLUE}View results:${NC}"
echo "  curl http://localhost:8081/api/readings/sensor/THERMO-001"
echo "  curl http://localhost:8081/api/readings/sensor/HEART-001"
echo "  curl http://localhost:8081/api/readings/sensor/FUEL-001"
echo ""
echo -e "${BLUE}Get statistics:${NC}"
# Calculate timestamps (macOS compatible)
if date -v-1H > /dev/null 2>&1; then
    # macOS
    START_TIME=$(date -u -v-1H +%Y-%m-%dT%H:%M:%SZ)
else
    # Linux
    START_TIME=$(date -u -d '-1 hour' +%Y-%m-%dT%H:%M:%SZ)
fi
END_TIME=$(date -u +%Y-%m-%dT%H:%M:%SZ)

echo "  curl \"http://localhost:8081/api/readings/stats/THERMO-001?start=${START_TIME}&end=${END_TIME}\""
echo ""
