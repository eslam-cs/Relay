package com.relay.iot.service.processor;

import com.relay.iot.dto.AggregateStats;
import com.relay.iot.model.DeviceType;
import com.relay.iot.model.Reading;

import java.util.Collections;
import java.util.List;

/**
 * Interface for processing sensor-specific data
 * Each device type can have its own implementation with custom logic
 */
public interface SensorDataProcessor {
    
    /**
     * Get the device type this processor handles
     */
    DeviceType getDeviceType();
    
    /**
     * Validate a reading before saving
     * @return true if valid, false otherwise
     */
    boolean validateReading(Reading reading);
    
    /**
     * Process/transform a reading before storage (optional)
     * Can apply calibration, normalization, etc.
     */
    Reading processReading(Reading reading);
    
    /**
     * Get the valid range for this sensor type
     */
    double getMinValue();
    double getMaxValue();
    
    /**
     * Default implementation for calculating aggregate statistics.
     * 
     * Computes average, median, min, max, and count from a list of readings.
     * The calculation logic is identical across all sensor types.
     * 
     * Implementations can override this if custom aggregate logic is needed
     * (e.g., adding device-specific metrics like heart rate zones, fuel consumption rate, etc.)
     * 
     * @param readings List of sensor readings
     * @return AggregateStats containing computed statistics
     */
    default AggregateStats calculateAggregates(List<Reading> readings) {
        if (readings == null || readings.isEmpty()) {
            return new AggregateStats(null, null, null, null, 0L);
        }
        
        double sum = 0;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        List<Double> values = new java.util.ArrayList<>();
        
        for (Reading reading : readings) {
            double value = reading.getValue();
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
            values.add(value);
        }
        
        double average = sum / readings.size();
        Double median = calculateMedian(values);
        
        return new AggregateStats(average, median, min, max, (long) readings.size());
    }
    
    /**
     * Default implementation for calculating median value.
     * 
     * Shared across all sensor types since the calculation logic is identical.
     * The median is the middle value when the list is sorted:
     * - For odd-sized lists: returns the middle element
     * - For even-sized lists: returns the average of the two middle elements
     * 
     * Implementations can override this if custom median logic is needed.
     * 
     * @param values List of double values
     * @return Median value, or null if list is empty
     */
    default Double calculateMedian(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        
        // Sort the values to find the middle
        Collections.sort(values);
        int size = values.size();
        
        // For even-sized lists, average the two middle values
        if (size % 2 == 0) {
            return (values.get(size / 2 - 1) + values.get(size / 2)) / 2.0;
        } else {
            // For odd-sized lists, return the middle value
            return values.get(size / 2);
        }
    }
}
