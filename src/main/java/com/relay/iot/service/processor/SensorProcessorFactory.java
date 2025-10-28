package com.relay.iot.service.processor;

import com.relay.iot.model.DeviceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory to get the appropriate processor for a device type
 */
@Component
public class SensorProcessorFactory {
    
    private final Map<DeviceType, SensorDataProcessor> processors;
    
    @Autowired
    public SensorProcessorFactory(List<SensorDataProcessor> processorList) {
        this.processors = new HashMap<>();
        for (SensorDataProcessor processor : processorList) {
            processors.put(processor.getDeviceType(), processor);
        }
    }
    
    public SensorDataProcessor getProcessor(DeviceType deviceType) {
        SensorDataProcessor processor = processors.get(deviceType);
        if (processor == null) {
            throw new IllegalArgumentException("No processor found for device type: " + deviceType);
        }
        return processor;
    }
    
    public boolean hasProcessor(DeviceType deviceType) {
        return processors.containsKey(deviceType);
    }
}
