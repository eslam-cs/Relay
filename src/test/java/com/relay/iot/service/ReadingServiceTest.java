package com.relay.iot.service;

import com.relay.iot.dto.AggregateStats;
import com.relay.iot.model.DeviceType;
import com.relay.iot.model.Reading;
import com.relay.iot.repository.ReadingRepository;
import com.relay.iot.service.processor.SensorDataProcessor;
import com.relay.iot.service.processor.SensorProcessorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadingServiceTest {

    @Mock
    private ReadingRepository readingRepository;

    @Mock
    private SensorProcessorFactory processorFactory;

    @Mock
    private SensorDataProcessor sensorDataProcessor;

    @InjectMocks
    private ReadingService readingService;

    private Reading validReading;

    @BeforeEach
    void setUp() {
        validReading = new Reading();
        validReading.setSensorId("THERMO-001");
        validReading.setDeviceType(DeviceType.THERMOSTAT);
        validReading.setValue(25.0);
        validReading.setTimestamp(Instant.now());
    }

    @Test
    void testSaveReading_ValidReading() {
        // Arrange
        when(processorFactory.getProcessor(DeviceType.THERMOSTAT)).thenReturn(sensorDataProcessor);
        when(sensorDataProcessor.validateReading(validReading)).thenReturn(true);
        when(sensorDataProcessor.processReading(validReading)).thenReturn(validReading);
        when(readingRepository.save(validReading)).thenReturn(validReading);

        // Act
        Reading result = readingService.saveReading(validReading);

        // Assert
        assertNotNull(result);
        assertEquals("THERMO-001", result.getSensorId());
        verify(processorFactory, times(1)).getProcessor(DeviceType.THERMOSTAT);
        verify(sensorDataProcessor, times(1)).validateReading(validReading);
        verify(sensorDataProcessor, times(1)).processReading(validReading);
        verify(readingRepository, times(1)).save(validReading);
    }

    @Test
    void testSaveReading_InvalidReading() {
        // Arrange
        Reading invalidReading = new Reading();
        invalidReading.setValue(200.0); // Out of range for thermostat
        invalidReading.setDeviceType(DeviceType.THERMOSTAT);

        when(processorFactory.getProcessor(DeviceType.THERMOSTAT)).thenReturn(sensorDataProcessor);
        when(sensorDataProcessor.validateReading(invalidReading)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> readingService.saveReading(invalidReading)
        );

        assertTrue(exception.getMessage().contains("Invalid reading"));
        verify(readingRepository, never()).save(any());
    }

    @Test
    void testSaveReadingsBatch_ValidReadings() {
        // Arrange
        List<Reading> readings = Arrays.asList(validReading, validReading);
        when(processorFactory.getProcessor(DeviceType.THERMOSTAT)).thenReturn(sensorDataProcessor);
        when(sensorDataProcessor.validateReading(any(Reading.class))).thenReturn(true);
        when(sensorDataProcessor.processReading(any(Reading.class))).thenReturn(validReading);
        when(readingRepository.saveAll(readings)).thenReturn(readings);

        // Act
        List<Reading> result = readingService.saveReadingsBatch(readings);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(readingRepository, times(1)).saveAll(readings);
    }

    @Test
    void testSaveReadingsBatch_EmptyList() {
        // Arrange
        when(readingRepository.saveAll(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        List<Reading> result = readingService.saveReadingsBatch(Collections.emptyList());

        // Assert
        assertTrue(result.isEmpty());
        verify(readingRepository, times(1)).saveAll(Collections.emptyList());
    }

    @Test
    void testGetAllReadings() {
        // Arrange
        List<Reading> expectedReadings = Arrays.asList(validReading, validReading);
        when(readingRepository.findAll()).thenReturn(expectedReadings);

        // Act
        List<Reading> result = readingService.getAllReadings();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(readingRepository, times(1)).findAll();
    }

    @Test
    void testGetReadingsBySensorId() {
        // Arrange
        String sensorId = "THERMO-001";
        List<Reading> expectedReadings = Collections.singletonList(validReading);
        when(readingRepository.findBySensorId(sensorId)).thenReturn(expectedReadings);

        // Act
        List<Reading> result = readingService.getReadingsBySensorId(sensorId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sensorId, result.get(0).getSensorId());
        verify(readingRepository, times(1)).findBySensorId(sensorId);
    }

    @Test
    void testGetReadingsByTimeRange() {
        // Arrange
        Instant start = Instant.parse("2025-10-27T00:00:00Z");
        Instant end = Instant.parse("2025-10-27T23:59:59Z");
        List<Reading> expectedReadings = Arrays.asList(validReading, validReading);
        when(readingRepository.findByTimestampBetween(start, end)).thenReturn(expectedReadings);

        // Act
        List<Reading> result = readingService.getReadingsByTimeRange(start, end);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(readingRepository, times(1)).findByTimestampBetween(start, end);
    }

    @Test
    void testGetAggregateStats() {
        // Arrange
        String sensorId = "THERMO-001";
        Instant start = Instant.parse("2025-10-27T00:00:00Z");
        Instant end = Instant.parse("2025-10-27T23:59:59Z");
        
        List<Reading> readings = Arrays.asList(
            createReading(20.0),
            createReading(25.0),
            createReading(30.0)
        );
        
        AggregateStats expectedStats = new AggregateStats(25.0, 25.0, 20.0, 30.0, 3L);

        when(readingRepository.findBySensorIdAndTimestampBetween(sensorId, start, end))
            .thenReturn(readings);
        when(processorFactory.getProcessor(DeviceType.THERMOSTAT)).thenReturn(sensorDataProcessor);
        when(sensorDataProcessor.calculateAggregates(readings)).thenReturn(expectedStats);

        // Act
        AggregateStats result = readingService.getAggregateStats(sensorId, start, end);

        // Assert
        assertNotNull(result);
        assertEquals(25.0, result.getAverage());
        assertEquals(25.0, result.getMedian());
        assertEquals(20.0, result.getMin());
        assertEquals(30.0, result.getMax());
        assertEquals(3L, result.getCount());
        
        verify(readingRepository, times(1)).findBySensorIdAndTimestampBetween(sensorId, start, end);
        verify(sensorDataProcessor, times(1)).calculateAggregates(readings);
    }

    @Test
    void testGetAggregateStats_NoReadings() {
        // Arrange
        String sensorId = "THERMO-001";
        Instant start = Instant.parse("2025-10-27T00:00:00Z");
        Instant end = Instant.parse("2025-10-27T23:59:59Z");

        when(readingRepository.findBySensorIdAndTimestampBetween(sensorId, start, end))
            .thenReturn(Collections.emptyList());

        // Act
        AggregateStats result = readingService.getAggregateStats(sensorId, start, end);

        // Assert
        assertNotNull(result);
        assertNull(result.getAverage());
        assertNull(result.getMedian());
        assertNull(result.getMin());
        assertNull(result.getMax());
        assertEquals(0L, result.getCount());
    }

    @Test
    void testGetAggregateStatsForSensorGroup() {
        // Arrange
        List<String> sensorIds = Arrays.asList("THERMO-001", "THERMO-002");
        Instant start = Instant.parse("2025-10-27T00:00:00Z");
        Instant end = Instant.parse("2025-10-27T23:59:59Z");

        List<Reading> readings = Arrays.asList(
            createReading(20.0),
            createReading(22.0),
            createReading(24.0),
            createReading(26.0)
        );

        when(readingRepository.findBySensorIdsAndTimestampBetween(sensorIds, start, end))
            .thenReturn(readings);

        // Act
        AggregateStats result = readingService.getAggregateStatsForSensorGroup(sensorIds, start, end);

        // Assert - Service uses calculateGenericAggregates() which is a real implementation
        assertNotNull(result);
        assertEquals(23.0, result.getAverage()); // (20+22+24+26)/4
        assertEquals(23.0, result.getMedian());  // (22+24)/2
        assertEquals(20.0, result.getMin());
        assertEquals(26.0, result.getMax());
        assertEquals(4L, result.getCount());
        
        verify(readingRepository, times(1)).findBySensorIdsAndTimestampBetween(sensorIds, start, end);
    }

    private Reading createReading(double value) {
        Reading reading = new Reading();
        reading.setValue(value);
        reading.setSensorId("THERMO-001");
        reading.setDeviceType(DeviceType.THERMOSTAT);
        reading.setTimestamp(Instant.now());
        return reading;
    }
}
