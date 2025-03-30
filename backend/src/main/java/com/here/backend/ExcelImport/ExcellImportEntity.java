
//backend\src\main\java\com\here\backend\ExcelImport\ExcellImportEntity.java

package com.here.backend.ExcelImport;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;

import com.here.backend.Student.StudentEntity;
import com.here.backend.Student.StudentRepository;

@Component
public class ExcellImportEntity {
    
    @Autowired
    private StudentRepository studentRepository;

    public void readExcelAndSaveToDB(@RequestHeader String filePath) {
    
    
    
    }
}

