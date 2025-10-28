package com.relay.iot.repository;

import com.relay.iot.model.Reading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ReadingRepository extends JpaRepository<Reading, Long> {
    
    List<Reading> findBySensorId(String sensorId);
    
    List<Reading> findByTimestampBetween(Instant start, Instant end);
    
    List<Reading> findBySensorIdAndTimestampBetween(String sensorId, Instant start, Instant end);
    
    @Query("SELECT r FROM Reading r WHERE r.sensorId IN :sensorIds AND r.timestamp BETWEEN :start AND :end")
    List<Reading> findBySensorIdsAndTimestampBetween(@Param("sensorIds") List<String> sensorIds,
                                                       @Param("start") Instant start,
                                                       @Param("end") Instant end);
    
    @Query("SELECT AVG(r.value) FROM Reading r WHERE r.sensorId = :sensorId AND r.timestamp BETWEEN :start AND :end")
    Double findAverageValue(@Param("sensorId") String sensorId, 
                            @Param("start") Instant start, 
                            @Param("end") Instant end);
    
    @Query("SELECT MIN(r.value) FROM Reading r WHERE r.sensorId = :sensorId AND r.timestamp BETWEEN :start AND :end")
    Double findMinValue(@Param("sensorId") String sensorId,
                        @Param("start") Instant start,
                        @Param("end") Instant end);
    
    @Query("SELECT MAX(r.value) FROM Reading r WHERE r.sensorId = :sensorId AND r.timestamp BETWEEN :start AND :end")
    Double findMaxValue(@Param("sensorId") String sensorId,
                        @Param("start") Instant start,
                        @Param("end") Instant end);
    
    @Query("SELECT COUNT(r) FROM Reading r WHERE r.sensorId = :sensorId AND r.timestamp BETWEEN :start AND :end")
    Long countReadings(@Param("sensorId") String sensorId,
                       @Param("start") Instant start,
                       @Param("end") Instant end);
}
