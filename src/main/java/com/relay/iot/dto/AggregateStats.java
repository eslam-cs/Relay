package com.relay.iot.dto;

/**
 * Data Transfer Object for aggregate statistics.
 * Used to return calculated statistics from the /stats endpoints.
 * This is not a database entity - values are computed on-the-fly from Reading entities.
 */
public class AggregateStats {
    private Double average;
    private Double median;
    private Double min;
    private Double max;
    private Long count;
    
    public AggregateStats() {
    }
    
    public AggregateStats(Double average, Double median, Double min, Double max, Long count) {
        this.average = average;
        this.median = median;
        this.min = min;
        this.max = max;
        this.count = count;
    }
    
    public Double getAverage() {
        return average;
    }
    
    public void setAverage(Double average) {
        this.average = average;
    }
    
    public Double getMedian() {
        return median;
    }
    
    public void setMedian(Double median) {
        this.median = median;
    }
    
    public Double getMin() {
        return min;
    }
    
    public void setMin(Double min) {
        this.min = min;
    }
    
    public Double getMax() {
        return max;
    }
    
    public void setMax(Double max) {
        this.max = max;
    }
    
    public Long getCount() {
        return count;
    }
    
    public void setCount(Long count) {
        this.count = count;
    }
}
