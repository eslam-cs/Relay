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

class ThermostatProcessorTest {

    private ThermostatProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new ThermostatProcessor();
    }

    @Test
    void testGetDeviceType() {
        assertEquals(DeviceType.THERMOSTAT, processor.getDeviceType());
    }

    @Test
    void testGetMinValue() {
        assertEquals(-50.0, processor.getMinValue());
    }

    @Test
    void testGetMaxValue() {
        assertEquals(100.0, processor.getMaxValue());
    }

    @Test
    void testValidateReading_ValidTemperature() {
        Reading reading = new Reading();
        reading.setValue(25.0);
        
        assertTrue(processor.validateReading(reading));
    }

    @Test
    void testValidateReading_BelowMinimum() {
        Reading reading = new Reading();
        reading.setValue(-100.0);
        
        assertFalse(processor.validateReading(reading));
    }

    @Test
    void testValidateReading_AboveMaximum() {
        Reading reading = new Reading();
        reading.setValue(150.0);
        
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
        reading.setValue(-50.0);
        
        assertTrue(processor.validateReading(reading));
    }

    @Test
    void testValidateReading_AtMaxBoundary() {
        Reading reading = new Reading();
        reading.setValue(100.0);
        
        assertTrue(processor.validateReading(reading));
    }

    @Test
    void testProcessReading() {
        Reading reading = new Reading();
        reading.setValue(25.0);
        reading.setSensorId("THERMO-001");
        
        Reading processed = processor.processReading(reading);
        
        assertSame(reading, processed);
        assertEquals(25.0, processed.getValue());
    }

    @Test
    void testCalculateAggregates_WithMultipleReadings() {
        List<Reading> readings = Arrays.asList(
            createReading(20.0),
            createReading(22.0),
            createReading(24.0),
            createReading(26.0),
            createReading(28.0)
        );

        AggregateStats stats = processor.calculateAggregates(readings);

        assertEquals(24.0, stats.getAverage());
        assertEquals(24.0, stats.getMedian());
        assertEquals(20.0, stats.getMin());
        assertEquals(28.0, stats.getMax());
        assertEquals(5L, stats.getCount());
    }

    @Test
    void testCalculateAggregates_EmptyList() {
        AggregateStats stats = processor.calculateAggregates(Collections.emptyList());

        assertNull(stats.getAverage());
        assertNull(stats.getMedian());
        assertNull(stats.getMin());
        assertNull(stats.getMax());
        assertEquals(0L, stats.getCount());
    }

    @Test
    void testCalculateAggregates_SingleReading() {
        List<Reading> readings = Collections.singletonList(createReading(25.0));

        AggregateStats stats = processor.calculateAggregates(readings);

        assertEquals(25.0, stats.getAverage());
        assertEquals(25.0, stats.getMedian());
        assertEquals(25.0, stats.getMin());
        assertEquals(25.0, stats.getMax());
        assertEquals(1L, stats.getCount());
    }

    @Test
    void testCalculateMedian_OddNumberOfValues() {
        List<Reading> readings = Arrays.asList(
            createReading(10.0),
            createReading(20.0),
            createReading(30.0)
        );

        AggregateStats stats = processor.calculateAggregates(readings);
        
        assertEquals(20.0, stats.getMedian());
    }

    @Test
    void testCalculateMedian_EvenNumberOfValues() {
        List<Reading> readings = Arrays.asList(
            createReading(10.0),
            createReading(20.0),
            createReading(30.0),
            createReading(40.0)
        );

        AggregateStats stats = processor.calculateAggregates(readings);
        
        assertEquals(25.0, stats.getMedian()); // (20 + 30) / 2
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
