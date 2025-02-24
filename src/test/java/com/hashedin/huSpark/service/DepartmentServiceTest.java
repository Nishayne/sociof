package com.hashedin.huSpark.service;

import com.hashedin.huSpark.entity.Department;
import com.hashedin.huSpark.repository.DepartmentRepository;
import com.hashedin.huSpark.service.impl.DepartmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Department expected;

    @BeforeEach
    public void setup() {
        this.expected = new Department();
        expected.setDepartmentId(1L);
        expected.setDepartmentName("test-dep-1");
        expected.setDepartmentAddress("test-dep-1 address");
        expected.setDepartmentCode("DEP1");
    }

    @Test
    public void testUpsertDepartment() {
        when(departmentRepository.save(any())).thenReturn(expected);
        Department actual = this.departmentService.upsertDepartment(new Department());
        assertEquals(expected.getDepartmentId(), actual.getDepartmentId());
        assertEquals(expected.getDepartmentName(), actual.getDepartmentName());
        assertEquals(expected.getDepartmentAddress(), actual.getDepartmentAddress());
        assertEquals( expected.getDepartmentCode(), actual.getDepartmentCode());
    }

    @Test
    public void testGetAllDepartments() {
        when(departmentRepository.findAll()).thenReturn(List.of(expected));
        List<Department> departments = this.departmentService.getAllDepartments();
        assertEquals(1, departments.size());
        Department actual = departments.get(0);
        assertEquals(expected.getDepartmentId(), actual.getDepartmentId());
        assertEquals(expected.getDepartmentName(), actual.getDepartmentName());
        assertEquals(expected.getDepartmentAddress(), actual.getDepartmentAddress());
        assertEquals( expected.getDepartmentCode(), actual.getDepartmentCode());
    }
}
