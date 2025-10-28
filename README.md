# IoT Data Processing System

Scalable backend for processing and querying IoT device data with interface-based architecture.

## 🚀 Quick Start

```bash
# Run everything with Docker
docker compose up --build


```

Then open: **http://localhost:8081/swagger-ui**

---

## 🎯 Simulate IoT Devices

**Simulate 3 devices sending data every second (Thermostat, Heart Rate Monitor, Fuel Meter):**

### Step 1: Start the Application
```bash
docker compose up -d
```

### Step 2: Run the Simulation
```bash
# Run for 30 seconds (default)
./simulate-iot-devices.sh

# Run for custom duration (e.g., 60 seconds)
./simulate-iot-devices.sh 60
```

### Step 3: View Live Output
```
╔═══════════════════════════════════════════════════════╗
║         IoT Device Simulation Started                ║
╚═══════════════════════════════════════════════════════╝

✓ Token obtained

📡 THERMOSTAT [THERMO-001] → 22.45 °C
📡 HEART_RATE_MONITOR [HEART-001] → 75.20 BPM
📡 FUEL_METER [FUEL-001] → 67.80 %
...
```

### Step 4: Verify Data
```bash
# View readings for each device
curl "http://localhost:8081/api/readings/sensor/THERMO-001"
curl "http://localhost:8081/api/readings/sensor/HEART-001"
curl "http://localhost:8081/api/readings/sensor/FUEL-001"

# Get statistics
curl "http://localhost:8081/api/readings/stats/THERMO-001?start=2025-10-27T00:00:00Z&end=2025-10-29T00:00:00Z"
```

**What it simulates:**
- 🌡️ **Thermostat**: Temperature readings (18-26°C)
- ❤️ **Heart Rate Monitor**: Heart rate data (60-100 BPM)
- ⛽ **Fuel Meter**: Fuel level (30-95%)

Each device sends **one reading per second** continuously for the specified duration.

---

## Features

- **JWT Authentication**: Secure API endpoints with JSON Web Tokens
- **Data Ingestion**: Single and batch endpoints for high-volume data
- **Query API**: Retrieve readings by sensor, time range
- **Aggregate Statistics**: Average, median, min, max for sensors or sensor groups
- **Performance**: Batch processing, connection pooling, database indexing

## Architecture

**Interface-Based Design with Strategy Pattern**

```
src/main/java/com/relay/iot/
├── model/                  # Data models
│   ├── Reading.java
│   ├── DeviceType.java
│   └── AggregateStats.java
├── dto/                    # Data transfer objects
│   ├── TokenRequest.java
│   └── TokenResponse.java
├── repository/             # Data access layer
│   └── ReadingRepository.java
├── service/                # Business logic
│   ├── ReadingService.java
│   └── processor/          # Device-specific processors
│       ├── SensorDataProcessor.java      (Interface)
│       ├── ThermostatProcessor.java      (THERMOSTAT)
│       ├── HeartRateProcessor.java       (HEART_RATE_MONITOR)
│       ├── FuelMeterProcessor.java       (FUEL_METER)
│       └── SensorProcessorFactory.java
├── security/               # Security & JWT
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── SecurityConfig.java
└── controller/             # REST API
    ├── AuthController.java
    ├── ReadingController.java
    └── DeviceTypeController.java
```

### Design Pattern: Strategy Pattern

Each device type has its own processor implementing `SensorDataProcessor`:
- **Validation**: Device-specific value ranges
- **Processing**: Custom data transformation/calibration
- **Aggregation**: Device-specific calculation logic

**Benefits:**
- Easy to add new device types
- Isolated device-specific logic
- Testable individual processors
- Flexible aggregation per device

## Quick Start with Docker

### Prerequisites
- Docker Desktop installed and running

### Run the Application

```bash
# Build and start (first time or after code changes)
docker compose up --build

# Or run in background (detached mode)
docker compose up --build -d
```

### Access the Application

- **API Base URL**: `http://localhost:8081`
- **Swagger UI**: `http://localhost:8081/swagger-ui`
- **API Docs**: `http://localhost:8081/v3/api-docs`

### Common Commands

```bash
# View logs
docker compose logs -f app

# View both app and database logs
docker compose logs -f

# Stop everything
docker compose down

# Stop and remove all data (including database)
docker compose down -v

# Restart just the app
docker compose restart app
```

## API Endpoints

### JWT Authentication

**Protected Endpoints (require JWT token):**
- 🔒 `POST /api/readings` - Requires JWT token
- 🔒 `POST /api/readings/batch` - Requires JWT token

**Public Endpoints (no token needed):**
- ✅ All GET endpoints - No authentication required
- ✅ `POST /auth/token` - Generate token

**In Swagger UI:**
1. Go to `/auth/token` endpoint → Click "Execute" → Copy the token value from the response
2. Click the **"Authorize"** button at the top right of Swagger UI (🔓 lock icon)
3. In the popup, paste your token (just the token, **without** "Bearer" prefix)
4. Click "Authorize" then "Close"
5. Now go to any protected endpoint (e.g., `POST /api/readings`)
6. Fill in the request body and click "Execute" - token is automatically included!

**Note:** Protected endpoints will show a 🔒 lock icon. Once you authorize, all requests include your token automatically.

**Step 1: Generate JWT Token (Public - No Auth Required)**

```bash
curl -X POST http://localhost:8081/auth/token
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJpb3QtZGV2aWNlIiwiaWF0IjoxNzI5NTEyMDAwLCJleHAiOjE3Mjk1OTg0MDB9.abc123...",
  "expiresIn": 86400000
}
```

**Step 2: Use Token in API Requests**

Include the token in the `Authorization` header with `Bearer` prefix:

```bash
curl -X GET http://localhost:8081/api/readings \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Testing the Full Authentication Flow:**

```bash
# 1. Generate a token (no body needed!)
TOKEN=$(curl -s -X POST http://localhost:8081/auth/token | jq -r '.token')

# 2. Use the token to create a reading
curl -X POST http://localhost:8081/api/readings \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sensorId": "THERMO-001",
    "deviceType": "THERMOSTAT",
    "value": 22.5,
    "timestamp": "2025-10-27T15:00:00Z"
  }'

# 3. Retrieve readings with the same token
curl http://localhost:8081/api/readings/sensor/THERMO-001 \
  -H "Authorization: Bearer $TOKEN"
```

## Data Model

**Reading**
- `id` - Auto-generated
- `sensorId` - Sensor identifier (e.g., "THERMO-001")
- `deviceType` - THERMOSTAT | HEART_RATE_MONITOR | FUEL_METER
- `value` - Reading value
- `timestamp` - ISO 8601 timestamp

## Device Type Processors

Each device type has its own processor with specific validation rules:

### THERMOSTAT
- **Range**: -50°C to 100°C
- **Unit**: Celsius
- **Validation**: Temperature within physical limits

### HEART_RATE_MONITOR
- **Range**: 30 BPM to 250 BPM
- **Unit**: Beats per minute
- **Validation**: Physiologically valid heart rate

### FUEL_METER
- **Range**: 0% to 100%
- **Unit**: Percentage
- **Validation**: Fuel level percentage

### Adding New Device Types

1. Add enum value to `DeviceType`
2. Create new processor implementing `SensorDataProcessor`
3. Annotate with `@Component`
4. Factory automatically picks it up!

## Security

### JWT Authentication

The application uses **JWT (JSON Web Tokens)** for stateless authentication.

**Key Features:**
- ✅ Stateless authentication (no server-side sessions)
- ✅ 24-hour token expiration
- ✅ Open token generation (no credentials required)
- ✅ Secure with HMAC-SHA256 signing
- ✅ Custom claims support (deviceId, deviceType)

**Public Endpoints (No Authentication):**
- `POST /auth/token` - Generate JWT token
- `GET /actuator/health` - Health check
- `/swagger-ui/**` - API documentation
- `/v3/api-docs/**` - OpenAPI specification

**Protected Endpoints (JWT Required):**
- All `/api/readings/**` endpoints
- All `/api/device-types/**` endpoints

**Configuration:**
- JWT secret key: Set via `JWT_SECRET` environment variable or uses default
- Token expiration: Configurable in `application.yml` (default: 24 hours)

**Security Best Practices:**
1. Always use HTTPS in production
2. Set a strong `JWT_SECRET` environment variable (64+ characters)
3. Rotate JWT secrets periodically
4. Monitor token usage patterns
5. Consider rate limiting on `/auth/token` endpoint

---

## Technology Stack

- **Spring Boot 3.3.3** - Framework
- **Spring Security** - Authentication & authorization
- **JWT (jjwt 0.12.3)** - Token-based authentication
- **Spring Data JPA** - Data access
- **PostgreSQL 15** - Database
- **Swagger/OpenAPI** - API documentation
- **Maven** - Build tool


## Configuration

### Application Settings

Edit `src/main/resources/application.yml` to customize:
- Database connection
- JWT token expiration
- Logging levels
- Server port

### Environment Variables

**JWT_SECRET** (Recommended for Production)
```bash
# Generate a strong secret key (256-bit minimum)
export JWT_SECRET="your-super-secret-key-at-least-64-characters-long-for-production"

# Run with custom secret
docker compose up --build
```

**Database Configuration**
- `DB_HOST` - PostgreSQL host (default: localhost)
- `DB_PORT` - PostgreSQL port (default: 5432)
- `DB_NAME` - Database name (default: iot)
- `DB_USER` - Database username (default: iot)
- `DB_PASSWORD` - Database password (default: iot)

### Docker Compose with Custom JWT Secret

Add to `docker-compose.yml`:
```yaml
services:
  app:
    environment:
      - JWT_SECRET=your-production-secret-key-here
```

## Database Schema

```sql
CREATE TABLE readings (
    id BIGSERIAL PRIMARY KEY,
    sensor_id VARCHAR(255) NOT NULL,
    device_type VARCHAR(50) NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    timestamp TIMESTAMP NOT NULL
);

CREATE INDEX idx_sensor_timestamp ON readings(sensor_id, timestamp);
CREATE INDEX idx_timestamp ON readings(timestamp);
```

## Troubleshooting

### Command Not Found: docker-compose
Use `docker compose` (with space) instead of `docker-compose`:
```bash
docker compose up --build
```

### Port Already in Use
If port 8081 is busy, change it in:
- `application.yml` - `server.port`
- `docker-compose.yml` - `ports` section
- `Dockerfile` - `EXPOSE`

### View Container Logs
```bash
# All logs
docker compose logs -f

# Just the app
docker compose logs -f app

# Just database
docker compose logs -f postgres
```

### Clean Restart
```bash
# Remove everything and start fresh
docker compose down -v
docker compose up --build
```

## Local Development (Optional)

If you want to run the app locally without Docker:

1. **Start only PostgreSQL:**
   ```bash
   docker compose up postgres -d
   ```

2. **Run Spring Boot:**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=ThermostatProcessorTest
mvn test -Dtest=ReadingServiceTest
mvn test -Dtest=ReadingControllerTest
```

### Run Tests with Coverage
```bash
mvn clean test jacoco:report
# Open report in browser:
open target/site/jacoco/index.html
```

### Test Structure
```
src/test/java/com/relay/iot/
├── service/
│   ├── processor/
│   │   ├── ThermostatProcessorTest.java
│   │   └── HeartRateProcessorTest.java
│   └── ReadingServiceTest.java
├── controller/
│   └── ReadingControllerTest.java
└── security/
    └── JwtTokenProviderTest.java
```

### Test Coverage
- **Unit Tests**: Processors, Services, JWT Provider (pure unit tests, no database)
- **Integration Tests**: Controllers with MockMvc
- **Mocking**: Mockito for mocking repositories and dependencies
- **No database required**: All tests use mocks

**IoT Device Simulation**: See [Simulate IoT Devices](#-simulate-iot-devices) section at the top.

---

## Database Access

### Connect to PostgreSQL Container
```bash
docker exec -it iot-postgres psql -U iot -d iot
```
- \dt - List all tables
- SELECT * FROM reading; - View all readings
- \q - Exit the PostgreSQL prompt

