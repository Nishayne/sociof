package com.hashedin.huSpark.service;

import com.hashedin.huSpark.entity.Department;

import java.util.List;

public interface DepartmentService {

    Department upsertDepartment(Department department);

    List<Department> getAllDepartments();
}
