package com.here.backend.Schedual;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface SchedualRepository extends MongoRepository<SchedualEntity,String> {

    List<SchedualEntity> findByRoomId(String roomId);
    
}
