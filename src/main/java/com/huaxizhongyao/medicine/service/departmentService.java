package com.huaxizhongyao.medicine.service;

import java.util.List;
import com.huaxizhongyao.medicine.pojo.Department;

public interface departmentService {
    // 获取所有科室
    List<Department> getAllDepartments();

    // 获取科室总数
    int getDepartmentCount();

    // 添加科室
    boolean addDepartment(Department department);

    // 更新科室
    boolean updateDepartment(Department department);

    // 删除科室
    boolean deleteDepartment(Integer id);
}
