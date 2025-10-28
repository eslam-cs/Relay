package com.relay.iot.service;

import com.relay.iot.dto.AggregateStats;
import com.relay.iot.model.Reading;
import com.relay.iot.repository.ReadingRepository;
import com.relay.iot.service.processor.SensorDataProcessor;
import com.relay.iot.service.processor.SensorProcessorFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ReadingService {
    
    private final ReadingRepository readingRepository;
    private final SensorProcessorFactory processorFactory;
    
    public ReadingService(ReadingRepository readingRepository, SensorProcessorFactory processorFactory) {
        this.readingRepository = readingRepository;
        this.processorFactory = processorFactory;
    }
    
    public Reading saveReading(Reading reading) {

         SensorDataProcessor processor = processorFactory.getProcessor(reading.getDeviceType());
        
        // Validate the reading
        if (!processor.validateReading(reading)) {
            throw new IllegalArgumentException(
                String.format("Invalid reading value %.2f for device type %s. Valid range: %.2f - %.2f",
                    reading.getValue(), reading.getDeviceType(), 
                    processor.getMinValue(), processor.getMaxValue())
            );
        }
        
        // Process/transform the reading if needed
        Reading processedReading = processor.processReading(reading);
        
        return readingRepository.save(processedReading);
    }
    
    @Transactional
    public List<Reading> saveReadingsBatch(List<Reading> readings) {
        List<Reading> processedReadings = new ArrayList<>();
        
        for (Reading reading : readings) {
            SensorDataProcessor processor = processorFactory.getProcessor(reading.getDeviceType());
            
            // Validate
            if (!processor.validateReading(reading)) {
                throw new IllegalArgumentException(
                    String.format("Invalid reading value %.2f for device type %s (sensor: %s). Valid range: %.2f - %.2f",
                        reading.getValue(), reading.getDeviceType(), reading.getSensorId(),
                        processor.getMinValue(), processor.getMaxValue())
                );
            }
            
            // Process
            Reading processedReading = processor.processReading(reading);
            processedReadings.add(processedReading);
        }
        
        return readingRepository.saveAll(processedReadings);
    }
    
    public List<Reading> getAllReadings() {
        return readingRepository.findAll();
    }
    
    public List<Reading> getReadingsBySensorId(String sensorId) {
        return readingRepository.findBySensorId(sensorId);
    }
    
    public List<Reading> getReadingsByTimeRange(Instant start, Instant end) {
        return readingRepository.findByTimestampBetween(start, end);
    }
    
    public AggregateStats getAggregateStats(String sensorId, Instant start, Instant end) {
        List<Reading> readings = readingRepository.findBySensorIdAndTimestampBetween(sensorId, start, end);
        
        if (readings.isEmpty()) {
            return new AggregateStats(null, null, null, null, 0L);
        }
        
        // Use device-specific processor for aggregation
        SensorDataProcessor processor = processorFactory.getProcessor(readings.get(0).getDeviceType());
        return processor.calculateAggregates(readings);
    }
    
    public AggregateStats getAggregateStatsForSensorGroup(List<String> sensorIds, Instant start, Instant end) {
        List<Reading> readings = readingRepository.findBySensorIdsAndTimestampBetween(sensorIds, start, end);
        
        if (readings.isEmpty()) {
            return new AggregateStats(null, null, null, null, 0L);
        }
        
        // For group stats, use generic calculation
        // You could also group by device type and aggregate separately if needed
        return calculateGenericAggregates(readings);
    }
    
    private AggregateStats calculateGenericAggregates(List<Reading> readings) {
        if (readings.isEmpty()) {
            return new AggregateStats(null, null, null, null, 0L);
        }
        
        double sum = 0;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        List<Double> values = new ArrayList<>();
        
        for (Reading reading : readings) {
            double value = reading.getValue();
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
            values.add(value);
        }
        
        double avg = sum / readings.size();
        Double median = calculateMedian(values);
        
        return new AggregateStats(avg, median, min, max, (long) readings.size());
    }
    
    private Double calculateMedian(List<Double> values) {
        if (values.isEmpty()) {
            return null;
        }
        
        Collections.sort(values);
        int size = values.size();
        
        if (size % 2 == 0) {
            return (values.get(size / 2 - 1) + values.get(size / 2)) / 2.0;
        } else {
            return values.get(size / 2);
        }
    }
}
