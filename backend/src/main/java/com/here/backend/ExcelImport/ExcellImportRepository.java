
//backend\src\main\java\com\here\backend\ExcelImport\ExcellImportRepository.java

package com.here.backend.ExcelImport;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExcellImportRepository extends MongoRepository<ExcellImportEntity, String> {
    // You can add custom query methods here if needed
}

