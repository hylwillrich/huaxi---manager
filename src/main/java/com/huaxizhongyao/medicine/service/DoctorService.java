package com.huaxizhongyao.medicine.service;

import java.io.IOException;
import java.util.List;
import com.huaxizhongyao.medicine.pojo.Doctor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.multipart.MultipartFile;

public interface DoctorService {
    // 获取所有医生
    List<Doctor> getAllDoctors();

    // 分页获取医生
    List<Doctor> getDoctorsByPage(int page, int size);

    // 获取医生总数
    int getDoctorCount();

    // 添加医生
    boolean addDoctor(Doctor doctor, MultipartFile photoFile) throws IOException;

    // 更新医生
    boolean updateDoctor(Doctor doctor, MultipartFile photoFile) throws IOException;

    // 删除医生
    boolean deleteDoctor(Integer id);

    // 获取JdbcTemplate
    JdbcTemplate getJdbcTemplate();
    
    // 根据科室查询医生
    List<Doctor> getDoctorsByDepartment(String department);
    
    // 根据科室名查询医生姓名列表
    List<String> getDoctorNamesByDepartment(String departmentName);
}