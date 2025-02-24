package com.hashedin.huSpark.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashedin.huSpark.api.DepartmentController;
import com.hashedin.huSpark.entity.Department;
import com.hashedin.huSpark.service.DepartmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DepartmentControllerTest {

    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private DepartmentController departmentController;

    private static final ObjectMapper objectMapper = new ObjectMapper();

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
        when(departmentService.upsertDepartment(any())).thenReturn(expected);
        ResponseEntity<?> responseEntity = this.departmentController.upsertDepartment(new Department());
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatusCode.valueOf(200), responseEntity.getStatusCode());
        Department actual = objectMapper.convertValue(responseEntity.getBody(), Department.class);
        assertNotNull(actual);
        assertEquals(expected.getDepartmentId(), actual.getDepartmentId());
        assertEquals(expected.getDepartmentName(), actual.getDepartmentName());
        assertEquals(expected.getDepartmentAddress(), actual.getDepartmentAddress());
        assertEquals( expected.getDepartmentCode(), actual.getDepartmentCode());
    }

    @Test
    public void testGetAllDepartments() {
        when(departmentService.getAllDepartments()).thenReturn(List.of(expected));
        ResponseEntity<?> responseEntity = this.departmentController.getAllDepartments();
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatusCode.valueOf(200), responseEntity.getStatusCode());
        List<?> departments = objectMapper.convertValue(responseEntity.getBody(), List.class);
        assertNotNull(departments);
        assertEquals(1, departments.size());
        Department actual = objectMapper.convertValue(departments.get(0), Department.class);
        assertNotNull(actual);
        assertEquals(expected.getDepartmentId(), actual.getDepartmentId());
        assertEquals(expected.getDepartmentName(), actual.getDepartmentName());
        assertEquals(expected.getDepartmentAddress(), actual.getDepartmentAddress());
        assertEquals( expected.getDepartmentCode(), actual.getDepartmentCode());
    }
}
