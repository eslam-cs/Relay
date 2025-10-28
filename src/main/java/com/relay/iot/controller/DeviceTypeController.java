package com.relay.iot.controller;

import com.relay.iot.model.DeviceType;
import com.relay.iot.service.processor.SensorDataProcessor;
import com.relay.iot.service.processor.SensorProcessorFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/device-types")
@Tag(name = "Device Types", description = "Information about supported device types")
public class DeviceTypeController {
    
    private final SensorProcessorFactory processorFactory;
    
    public DeviceTypeController(SensorProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }
    
    @GetMapping
    @Operation(summary = "Get all supported device types")
    public ResponseEntity<DeviceType[]> getAllDeviceTypes() {
        return ResponseEntity.ok(DeviceType.values());
    }
    
    @GetMapping("/{deviceType}/validation-rules")
    @Operation(summary = "Get validation rules for a device type", 
               description = "Returns min/max values and description for a specific device type. " +
                             "Choose from: THERMOSTAT, HEART_RATE_MONITOR, or FUEL_METER")
    public ResponseEntity<Map<String, Object>> getValidationRules(
            @Parameter(name = "deviceType", 
                       description = "Device type (THERMOSTAT, HEART_RATE_MONITOR, or FUEL_METER)", 
                       required = true,
                       example = "THERMOSTAT")
            @PathVariable("deviceType") DeviceType deviceType) {
        SensorDataProcessor processor = processorFactory.getProcessor(deviceType);
        
        Map<String, Object> rules = new HashMap<>();
        rules.put("deviceType", deviceType);
        rules.put("minValue", processor.getMinValue());
        rules.put("maxValue", processor.getMaxValue());
        rules.put("description", getDeviceDescription(deviceType));
        
        return ResponseEntity.ok(rules);
    }
    
    private String getDeviceDescription(DeviceType deviceType) {
        switch (deviceType) {
            case THERMOSTAT:
                return "Temperature sensor in Celsius";
            case HEART_RATE_MONITOR:
                return "Heart rate monitor in beats per minute (BPM)";
            case FUEL_METER:
                return "Fuel level meter in percentage (%)";
            default:
                return "Unknown device type";
        }
    }
}
