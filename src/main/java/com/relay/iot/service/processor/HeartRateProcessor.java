package com.relay.iot.service.processor;

import com.relay.iot.model.DeviceType;
import com.relay.iot.model.Reading;
import org.springframework.stereotype.Component;

@Component
public class HeartRateProcessor implements SensorDataProcessor {
    
    private static final double MIN_BPM = 30.0;
    private static final double MAX_BPM = 250.0;
    
    @Override
    public DeviceType getDeviceType() {
        return DeviceType.HEART_RATE_MONITOR;
    }
    
    @Override
    public boolean validateReading(Reading reading) {
        if (reading.getValue() == null) {
            return false;
        }
        double value = reading.getValue();
        return value >= MIN_BPM && value <= MAX_BPM;
    }
    
    @Override
    public Reading processReading(Reading reading) {
        // Could apply smoothing or anomaly detection here
        return reading;
    }

    @Override
    public double getMinValue() {
        return MIN_BPM;
    }
    
    @Override
    public double getMaxValue() {
        return MAX_BPM;
    }
}
