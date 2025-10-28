package com.relay.iot.service.processor;

import com.relay.iot.model.DeviceType;
import com.relay.iot.model.Reading;
import org.springframework.stereotype.Component;

@Component
public class FuelMeterProcessor implements SensorDataProcessor {
    
    private static final double MIN_FUEL = 0.0;    // 0%
    private static final double MAX_FUEL = 100.0;  // 100%
    
    @Override
    public DeviceType getDeviceType() {
        return DeviceType.FUEL_METER;
    }
    
    @Override
    public boolean validateReading(Reading reading) {
        if (reading.getValue() == null) {
            return false;
        }
        double value = reading.getValue();
        return value >= MIN_FUEL && value <= MAX_FUEL;
    }
    
    @Override
    public Reading processReading(Reading reading) {
        // Could apply fuel consumption rate calculation here
        // Or detect rapid fuel drops (potential leak)
        return reading;
    }

    @Override
    public double getMinValue() {
        return MIN_FUEL;
    }
    
    @Override
    public double getMaxValue() {
        return MAX_FUEL;
    }
}
