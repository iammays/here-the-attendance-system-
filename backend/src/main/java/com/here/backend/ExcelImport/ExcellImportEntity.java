package com.here.backend.ExcelImport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;
import com.here.backend.Student.StudentRepository;

@Component
public class ExcellImportEntity {
    
    @Autowired
    private StudentRepository studentRepository;

    public void readExcelAndSaveToDB(@RequestHeader String filePath) {}
}