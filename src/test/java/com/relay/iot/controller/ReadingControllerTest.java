package com.relay.iot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relay.iot.dto.AggregateStats;
import com.relay.iot.model.DeviceType;
import com.relay.iot.model.Reading;
import com.relay.iot.security.JwtAuthenticationFilter;
import com.relay.iot.service.ReadingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReadingController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security for tests
class ReadingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReadingService readingService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter; // Mock security filter

    private Reading sampleReading;

    @BeforeEach
    void setUp() {
        sampleReading = new Reading();
        sampleReading.setId(1L);
        sampleReading.setSensorId("THERMO-001");
        sampleReading.setDeviceType(DeviceType.THERMOSTAT);
        sampleReading.setValue(25.0);
        sampleReading.setTimestamp(Instant.parse("2025-10-27T12:00:00Z"));
    }

    @Test
    void testCreateReading_Success() throws Exception {
        // Arrange
        when(readingService.saveReading(any(Reading.class))).thenReturn(sampleReading);

        // Act & Assert
        mockMvc.perform(post("/api/readings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleReading)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sensorId").value("THERMO-001"))
                .andExpect(jsonPath("$.deviceType").value("THERMOSTAT"))
                .andExpect(jsonPath("$.value").value(25.0));

        verify(readingService, times(1)).saveReading(any(Reading.class));
    }

    @Test
    void testCreateReadingsBatch_Success() throws Exception {
        // Arrange
        List<Reading> readings = Arrays.asList(sampleReading, sampleReading);
        when(readingService.saveReadingsBatch(any(List.class))).thenReturn(readings);

        // Act & Assert
        mockMvc.perform(post("/api/readings/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(readings)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].sensorId").value("THERMO-001"));

        verify(readingService, times(1)).saveReadingsBatch(any(List.class));
    }

    @Test
    void testGetAllReadings() throws Exception {
        // Arrange
        List<Reading> readings = Collections.singletonList(sampleReading);
        when(readingService.getAllReadings()).thenReturn(readings);

        // Act & Assert
        mockMvc.perform(get("/api/readings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sensorId").value("THERMO-001"));

        verify(readingService, times(1)).getAllReadings();
    }

    @Test
    void testGetReadingsBySensor() throws Exception {
        // Arrange
        String sensorId = "THERMO-001";
        List<Reading> readings = Collections.singletonList(sampleReading);
        when(readingService.getReadingsBySensorId(sensorId)).thenReturn(readings);

        // Act & Assert
        mockMvc.perform(get("/api/readings/sensor/{sensorId}", sensorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sensorId").value(sensorId));

        verify(readingService, times(1)).getReadingsBySensorId(sensorId);
    }

    @Test
    void testGetReadingsByTimeRange() throws Exception {
        // Arrange
        String start = "2025-10-27T00:00:00Z";
        String end = "2025-10-27T23:59:59Z";
        List<Reading> readings = Arrays.asList(sampleReading, sampleReading);
        when(readingService.getReadingsByTimeRange(any(Instant.class), any(Instant.class)))
            .thenReturn(readings);

        // Act & Assert
        mockMvc.perform(get("/api/readings/range")
                .param("start", start)
                .param("end", end))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(readingService, times(1)).getReadingsByTimeRange(any(Instant.class), any(Instant.class));
    }

    @Test
    void testGetAggregateStats() throws Exception {
        // Arrange
        String sensorId = "THERMO-001";
        String start = "2025-10-27T00:00:00Z";
        String end = "2025-10-27T23:59:59Z";
        AggregateStats stats = new AggregateStats(25.0, 24.5, 20.0, 30.0, 10L);
        
        when(readingService.getAggregateStats(eq(sensorId), any(Instant.class), any(Instant.class)))
            .thenReturn(stats);

        // Act & Assert
        mockMvc.perform(get("/api/readings/stats/{sensorId}", sensorId)
                .param("start", start)
                .param("end", end))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.average").value(25.0))
                .andExpect(jsonPath("$.median").value(24.5))
                .andExpect(jsonPath("$.min").value(20.0))
                .andExpect(jsonPath("$.max").value(30.0))
                .andExpect(jsonPath("$.count").value(10));

        verify(readingService, times(1)).getAggregateStats(eq(sensorId), any(Instant.class), any(Instant.class));
    }

    @Test
    void testGetAggregateStatsForGroup() throws Exception {
        // Arrange
        String start = "2025-10-27T00:00:00Z";
        String end = "2025-10-27T23:59:59Z";
        AggregateStats stats = new AggregateStats(25.0, 24.5, 20.0, 30.0, 20L);
        
        when(readingService.getAggregateStatsForSensorGroup(any(List.class), any(Instant.class), any(Instant.class)))
            .thenReturn(stats);

        // Act & Assert
        mockMvc.perform(get("/api/readings/stats/group")
                .param("sensorIds", "THERMO-001,THERMO-002")
                .param("start", start)
                .param("end", end))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.average").value(25.0))
                .andExpect(jsonPath("$.count").value(20));

        verify(readingService, times(1))
            .getAggregateStatsForSensorGroup(any(List.class), any(Instant.class), any(Instant.class));
    }
}
