package com.here.backend.Room;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends MongoRepository<RoomEntity, String> {
    List<RoomEntity> findByScheduleId(String scheduleId);

}