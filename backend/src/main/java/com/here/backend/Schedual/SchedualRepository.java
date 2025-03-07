package com.here.backend.Schedual;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface SchedualRepository extends MongoRepository<SchedualEntity,String> {
    
}
