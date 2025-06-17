package com.huaxizhongyao.medicine.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import com.huaxizhongyao.medicine.pojo.Doctor;
import com.huaxizhongyao.medicine.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Doctor> getAllDoctors() {
        String sql = "SELECT doc_id as docId, doc_name as docName, d_id as dId, "
                + "COALESCE(d_name, '') as dName, doc_gender as docGender, doc_age as docAge, "
                + "doc_photo as docPhoto, doc_class as docClass FROM doctor";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Doctor.class));
    }

    @Override
    public List<Doctor> getDoctorsByPage(int page, int size) {
        int offset = (page - 1) * size;
        String sql =
                "SELECT d.doc_id as docId, d.doc_name as docName, d.d_id as dId, dept.d_name as dName, "
                + "d.doc_gender as docGender, d.doc_age as docAge, d.doc_photo as docPhoto, d.doc_class as docClass "
                + "FROM doctor d LEFT JOIN department dept ON d.d_id = dept.d_id LIMIT ? OFFSET ?";


        List<Doctor> doctors =
                jdbcTemplate.query(sql, new Object[] {size, offset}, (rs, rowNum) -> {
                    Doctor doctor = new Doctor();
                    doctor.setDocId(rs.getInt("docId"));
                    doctor.setDocName(rs.getString("docName"));
                    doctor.setDId(rs.getInt("dId"));
                    doctor.setDName(rs.getString("dName"));
                    doctor.setDocGender(rs.getString("docGender"));
                    doctor.setDocAge(rs.getInt("docAge"));
                    String photoPath = rs.getString("docPhoto");
                    // 统一处理图片路径格式
                    if (photoPath != null && !photoPath.isEmpty()) {
                        doctor.setDocPhoto(photoPath.startsWith("/") ? photoPath : "/" + photoPath);
                    } else {
                        doctor.setDocPhoto("");
                    }
                    doctor.setDocClass(rs.getString("docClass"));
                    return doctor;
                });



        return doctors;
    }

    @Override
    public int getDoctorCount() {
        String sql = "SELECT COUNT(*) FROM doctor";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }


    private String uploadBaseDir = "src/main/resources/static/uploads";

    @Override
    public boolean addDoctor(Doctor doctor, MultipartFile photoFile) {
        try {
            // 构建上传目录路径
            String uploadDir = new File(uploadBaseDir, "doctors").getAbsolutePath() + File.separator;
            System.out.println("文件上传目录: " + uploadDir);
            
            // 确保上传目录存在
            File uploadPath = new File(uploadDir);
            if (!uploadPath.exists()) {
                boolean created = uploadPath.mkdirs();
                if(!created) {
                    throw new IOException("无法创建上传目录: " + uploadDir);
                }
            }

            // 处理图片文件
            if (photoFile != null && !photoFile.isEmpty()) {
                // 生成唯一文件名
                String originalFilename = photoFile.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String fileName = UUID.randomUUID().toString() + fileExtension;
                
                // 创建目标文件
                File destFile = new File(uploadDir + fileName);
                
                // 确保父目录存在
                File parent = destFile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                
                // 保存文件
                photoFile.transferTo(destFile);
            
                // 存储绝对路径到数据库
                doctor.setDocPhoto("/uploads/doctors/" + fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        String sql = "INSERT INTO doctor (doc_name, d_id, d_name, doc_gender, "
                + "doc_age, doc_photo, doc_class) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int result = jdbcTemplate.update(sql, doctor.getDocName(), doctor.getDId(),
                doctor.getDName(), doctor.getDocGender(), doctor.getDocAge(), doctor.getDocPhoto(),
                doctor.getDocClass());
        return result > 0;
    }

    @Override
    public boolean updateDoctor(Doctor doctor, MultipartFile photoFile) throws IOException {
        try {
            // 处理图片文件
            if (photoFile != null && !photoFile.isEmpty()) {
                // 构建上传目录路径
                String uploadDir = new File(uploadBaseDir, "doctors").getAbsolutePath() + File.separator;
                System.out.println("更新医生文件上传目录: " + uploadDir);
                
                // 确保上传目录存在
                File uploadPath = new File(uploadDir);
                if (!uploadPath.exists()) {
                    boolean created = uploadPath.mkdirs();
                    if(!created) {
                        throw new IOException("无法创建上传目录: " + uploadDir);
                    }
                }

                // 生成唯一文件名
                String originalFilename = photoFile.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String fileName = UUID.randomUUID().toString() + fileExtension;
                
                // 创建目标文件
                File destFile = new File(uploadDir + fileName);
                
                // 确保父目录存在
                File parent = destFile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                
                // 保存文件
                photoFile.transferTo(destFile);
                
                // 存储绝对路径到数据库
                doctor.setDocPhoto("/uploads/doctors/" + fileName);
            }

            // 先根据dId获取科室名称
            String deptSql = "SELECT d_name FROM department WHERE d_id = ?";
            String dName = jdbcTemplate.queryForObject(deptSql, String.class, doctor.getDId());

            String sql = "UPDATE doctor SET doc_name=?, d_id=?, d_name=?, doc_gender=?, "
                    + "doc_age=?, doc_photo=?, doc_class=? WHERE doc_id=?";
            int result = jdbcTemplate.update(sql, doctor.getDocName(), doctor.getDId(), dName,
                    doctor.getDocGender(), doctor.getDocAge(), doctor.getDocPhoto(),
                    doctor.getDocClass(), doctor.getDocId());
            return result > 0;
        } catch (Exception e) {
            throw new IOException("更新医生失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteDoctor(Integer id) {
        try {
            // 1. 先获取医生的照片路径
            String photoPath = jdbcTemplate.queryForObject(
                "SELECT doc_photo FROM doctor WHERE doc_id=?", 
                String.class, 
                id
            );
            
            // 2. 删除数据库记录
            String sql = "DELETE FROM doctor WHERE doc_id=?";
            int result = jdbcTemplate.update(sql, id);
            
            // 3. 如果删除成功且存在照片，删除照片文件
            if (result > 0 && photoPath != null && !photoPath.isEmpty()) {
                // 从路径中提取文件名
                String fileName = photoPath.substring(photoPath.lastIndexOf("/") + 1);
                // 构建完整文件路径
                String filePath = uploadBaseDir + "/doctors/" + fileName;
                File photoFile = new File(filePath);
                
                // 删除文件
                if (photoFile.exists()) {
                    if (!photoFile.delete()) {
                        System.err.println("无法删除照片文件: " + filePath);
                    }
                }
            }
            
            return result > 0;
        } catch (Exception e) {
            System.err.println("删除医生失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Doctor> getDoctorsByDepartment(String department) {
        String sql = "SELECT doc_id as docId, doc_name as docName, d_id as dId, " +
                "COALESCE(d_name, '') as dName, doc_gender as docGender, doc_age as docAge, " +
                "doc_photo as docPhoto, doc_class as docClass FROM doctor " +
                "WHERE d_name = ?";
        
        return jdbcTemplate.query(sql, new Object[]{department}, (rs, rowNum) -> {
            Doctor doctor = new Doctor();
            doctor.setDocId(rs.getInt("docId"));
            doctor.setDocName(rs.getString("docName"));
            doctor.setDId(rs.getInt("dId"));
            doctor.setDName(rs.getString("dName"));
            doctor.setDocGender(rs.getString("docGender"));
            doctor.setDocAge(rs.getInt("docAge"));
            String photoPath = rs.getString("docPhoto");
            if (photoPath != null && !photoPath.isEmpty()) {
                doctor.setDocPhoto(photoPath.startsWith("/") ? photoPath : "/" + photoPath);
            } else {
                doctor.setDocPhoto("");
            }
            doctor.setDocClass(rs.getString("docClass"));
            return doctor;
        });
    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}