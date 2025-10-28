package com.relay.iot.service.processor;

import com.relay.iot.model.DeviceType;
import com.relay.iot.model.Reading;
import org.springframework.stereotype.Component;

@Component
public class ThermostatProcessor implements SensorDataProcessor {
    
    private static final double MIN_TEMP = -50.0;  // Celsius
    private static final double MAX_TEMP = 100.0;
    
    @Override
    public DeviceType getDeviceType() {
        return DeviceType.THERMOSTAT;
    }
    
    @Override
    public boolean validateReading(Reading reading) {
        if (reading.getValue() == null) {
            return false;
        }
        double value = reading.getValue();
        return value >= MIN_TEMP && value <= MAX_TEMP;
    }
    
    @Override
    public Reading processReading(Reading reading) {
        // Could apply temperature calibration or unit conversion here
        return reading;
    }

    @Override
    public double getMinValue() {
        return MIN_TEMP;
    }
    
    @Override
    public double getMaxValue() {
        return MAX_TEMP;
    }
}
