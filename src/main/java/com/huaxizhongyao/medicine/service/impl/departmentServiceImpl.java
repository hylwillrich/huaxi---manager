package com.huaxizhongyao.medicine.service.impl;

import java.util.List;
import com.huaxizhongyao.medicine.pojo.Department;
import com.huaxizhongyao.medicine.service.departmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class departmentServiceImpl implements departmentService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Department> getAllDepartments() {
        String sql = "SELECT d_id as id, d_name as name, d_floor as floor, "
                + "d_telephone as telephone, d_isopen as isOpen FROM department";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Department.class));
    }

    @Override
    public int getDepartmentCount() {
        String sql = "SELECT COUNT(*) FROM department";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    @Override
    public boolean addDepartment(Department department) {
        String sql =
                "INSERT INTO department (d_name, d_floor, d_telephone, d_isopen) VALUES (?, ?, ?, ?)";
        int isOpenValue = department.getIsOpen() ? 1 : 0;
        int result = jdbcTemplate.update(sql, department.getName(), department.getFloor(),
                department.getTelephone(), isOpenValue);
        return result > 0;
    }

    @Override
    public boolean updateDepartment(Department department) {
        String sql =
                "UPDATE department SET d_name=?, d_floor=?, d_telephone=?, d_isopen=? WHERE d_id=?";
        int isOpenValue = department.getIsOpen() ? 1 : 0;
        int result = jdbcTemplate.update(sql, department.getName(), department.getFloor(),
                department.getTelephone(), isOpenValue, department.getId());
        return result > 0;
    }

    @Override
    public boolean deleteDepartment(Integer id) {
        String sql = "DELETE FROM department WHERE d_id=?";
        int result = jdbcTemplate.update(sql, id);
        return result > 0;
    }
}
