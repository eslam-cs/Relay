package com.relay.iot.service.processor;

import com.relay.iot.dto.AggregateStats;
import com.relay.iot.model.DeviceType;
import com.relay.iot.model.Reading;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HeartRateProcessorTest {

    private HeartRateProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new HeartRateProcessor();
    }

    @Test
    void testGetDeviceType() {
        assertEquals(DeviceType.HEART_RATE_MONITOR, processor.getDeviceType());
    }

    @Test
    void testGetMinValue() {
        assertEquals(30.0, processor.getMinValue());
    }

    @Test
    void testGetMaxValue() {
        assertEquals(250.0, processor.getMaxValue());
    }

    @Test
    void testValidateReading_ValidHeartRate() {
        Reading reading = new Reading();
        reading.setValue(75.0);
        
        assertTrue(processor.validateReading(reading));
    }

    @Test
    void testValidateReading_BelowMinimum() {
        Reading reading = new Reading();
        reading.setValue(20.0);
        
        assertFalse(processor.validateReading(reading));
    }

    @Test
    void testValidateReading_AboveMaximum() {
        Reading reading = new Reading();
        reading.setValue(300.0);
        
        assertFalse(processor.validateReading(reading));
    }

    @Test
    void testValidateReading_NullValue() {
        Reading reading = new Reading();
        reading.setValue(null);
        
        assertFalse(processor.validateReading(reading));
    }

    @Test
    void testValidateReading_AtMinBoundary() {
        Reading reading = new Reading();
        reading.setValue(30.0);
        
        assertTrue(processor.validateReading(reading));
    }

    @Test
    void testValidateReading_AtMaxBoundary() {
        Reading reading = new Reading();
        reading.setValue(250.0);
        
        assertTrue(processor.validateReading(reading));
    }

    @Test
    void testCalculateAggregates_RestingHeartRate() {
        List<Reading> readings = Arrays.asList(
            createReading(60.0),
            createReading(62.0),
            createReading(58.0),
            createReading(61.0),
            createReading(59.0)
        );

        AggregateStats stats = processor.calculateAggregates(readings);

        assertEquals(60.0, stats.getAverage());
        assertEquals(60.0, stats.getMedian());
        assertEquals(58.0, stats.getMin());
        assertEquals(62.0, stats.getMax());
        assertEquals(5L, stats.getCount());
    }

    @Test
    void testCalculateAggregates_ExerciseHeartRate() {
        List<Reading> readings = Arrays.asList(
            createReading(140.0),
            createReading(150.0),
            createReading(160.0)
        );

        AggregateStats stats = processor.calculateAggregates(readings);

        assertEquals(150.0, stats.getAverage());
        assertEquals(150.0, stats.getMedian());
        assertEquals(140.0, stats.getMin());
        assertEquals(160.0, stats.getMax());
    }

    private Reading createReading(double value) {
        Reading reading = new Reading();
        reading.setValue(value);
        reading.setSensorId("HEART-001");
        reading.setDeviceType(DeviceType.HEART_RATE_MONITOR);
        reading.setTimestamp(Instant.now());
        return reading;
    }
}
