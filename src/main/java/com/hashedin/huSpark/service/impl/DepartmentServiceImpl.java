package com.hashedin.huSpark.service.impl;

import com.hashedin.huSpark.entity.Department;
import com.hashedin.huSpark.repository.DepartmentRepository;
import com.hashedin.huSpark.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public Department upsertDepartment(Department department) {
        department = this.departmentRepository.save(department);
        return department;
    }

    @Override
    public List<Department> getAllDepartments() {
        return this.departmentRepository.findAll();
    }
}
