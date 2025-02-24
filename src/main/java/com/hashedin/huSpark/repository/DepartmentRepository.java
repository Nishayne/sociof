package com.hashedin.huSpark.repository;

import com.hashedin.huSpark.entity.Department;
import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends CrudRepository<Department, Long> {

    Department save(@Nonnull Department department);

    List<Department> findAll();
}
