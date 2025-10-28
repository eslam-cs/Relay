package com.relay.iot.controller;

import com.relay.iot.dto.AggregateStats;
import com.relay.iot.model.Reading;
import com.relay.iot.service.ReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/readings")
@Tag(name = "Readings", description = "IoT device readings ingestion and retrieval")
public class ReadingController {
    
    private final ReadingService readingService;
    
    public ReadingController(ReadingService readingService) {
        this.readingService = readingService;
    }
    
    @PostMapping
    @Operation(summary = "Create a single reading", description = "Requires JWT token")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Reading> createReading(@RequestBody Reading reading) {
        Reading savedReading = readingService.saveReading(reading);
        return new ResponseEntity<>(savedReading, HttpStatus.CREATED);
    }
    
    @PostMapping("/batch")
    @Operation(summary = "Create multiple readings in batch for high performance", description = "Requires JWT token")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<Reading>> createReadingsBatch(@RequestBody List<Reading> readings) {
        List<Reading> savedReadings = readingService.saveReadingsBatch(readings);
        return new ResponseEntity<>(savedReadings, HttpStatus.CREATED);
    }
    
    @GetMapping
    @Operation(summary = "Get all readings")
    public ResponseEntity<List<Reading>> getAllReadings() {
        List<Reading> readings = readingService.getAllReadings();
        return ResponseEntity.ok(readings);
    }
    
    @GetMapping("/sensor/{sensorId}")
    @Operation(summary = "Get readings by sensor ID", 
               description = "Returns all readings for a specific sensor")
    public ResponseEntity<List<Reading>> getReadingsBySensor(
            @Parameter(name = "sensorId", description = "Sensor ID (e.g., THERMO-001)", required = true, example = "THERMO-001")
            @PathVariable("sensorId") String sensorId) {
        List<Reading> readings = readingService.getReadingsBySensorId(sensorId);
        return ResponseEntity.ok(readings);
    }
    
    @GetMapping("/range")
    @Operation(summary = "Get readings by time range",
               description = "Returns all readings within a specific time range")
    public ResponseEntity<List<Reading>> getReadingsByTimeRange(
            @Parameter(name = "start", description = "Start time in ISO-8601 format", required = true, example = "2025-10-27T00:00:00Z")
            @RequestParam("start") String start,
            @Parameter(name = "end", description = "End time in ISO-8601 format", required = true, example = "2025-10-27T23:59:59Z")
            @RequestParam("end") String end) {
        Instant startTime = Instant.parse(start);
        Instant endTime = Instant.parse(end);
        List<Reading> readings = readingService.getReadingsByTimeRange(startTime, endTime);
        return ResponseEntity.ok(readings);
    }
    
    @GetMapping("/stats/{sensorId}")
    @Operation(summary = "Get aggregate statistics (avg, median, min, max) for a sensor",
               description = "Returns average, median, min, max values for a specific sensor within a time range")
    public ResponseEntity<AggregateStats> getAggregateStats(
            @Parameter(name = "sensorId", description = "Sensor ID (e.g., THERMO-001)", required = true, example = "THERMO-001")
            @PathVariable("sensorId") String sensorId,
            @Parameter(name = "start", description = "Start time in ISO-8601 format", required = true, example = "2025-10-27T00:00:00Z")
            @RequestParam("start") String start,
            @Parameter(name = "end", description = "End time in ISO-8601 format", required = true, example = "2025-10-27T23:59:59Z")
            @RequestParam("end") String end) {
        Instant startTime = Instant.parse(start);
        Instant endTime = Instant.parse(end);
        AggregateStats stats = readingService.getAggregateStats(sensorId, startTime, endTime);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/stats/group")
    @Operation(summary = "Get aggregate statistics for a group of sensors",
               description = "Returns combined statistics for multiple sensors within a time range")
    public ResponseEntity<AggregateStats> getAggregateStatsForGroup(
            @Parameter(name = "sensorIds", description = "Comma-separated sensor IDs", required = true, example = "THERMO-001,THERMO-002")
            @RequestParam("sensorIds") List<String> sensorIds,
            @Parameter(name = "start", description = "Start time in ISO-8601 format", required = true, example = "2025-10-27T00:00:00Z")
            @RequestParam("start") String start,
            @Parameter(name = "end", description = "End time in ISO-8601 format", required = true, example = "2025-10-27T23:59:59Z")
            @RequestParam("end") String end) {
        Instant startTime = Instant.parse(start);
        Instant endTime = Instant.parse(end);
        AggregateStats stats = readingService.getAggregateStatsForSensorGroup(sensorIds, startTime, endTime);
        return ResponseEntity.ok(stats);
    }
}
