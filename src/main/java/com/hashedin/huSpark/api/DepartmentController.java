package com.hashedin.huSpark.api;

import com.hashedin.huSpark.entity.Department;
import com.hashedin.huSpark.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.cors.CorsConfiguration;

@RestController
@RequestMapping("/department")
@CrossOrigin(origins = CorsConfiguration.ALL)
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<?> upsertDepartment(@RequestBody Department department) {
        return ResponseEntity.ok(this.departmentService.upsertDepartment(department));
    }

    @GetMapping
    public ResponseEntity<?> getAllDepartments() {
        return ResponseEntity.ok(this.departmentService.getAllDepartments());
    }
}
