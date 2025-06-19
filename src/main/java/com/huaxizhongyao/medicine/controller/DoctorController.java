package com.huaxizhongyao.medicine.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import com.huaxizhongyao.medicine.pojo.Doctor;
import com.huaxizhongyao.medicine.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("")
    public String doctor(HttpSession session, org.springframework.ui.Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";   
        }
        model.addAttribute("doctorService", doctorService);
        return "doctor";
    }

    @GetMapping("/list")
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getDoctorCount() {
        return ResponseEntity.ok(doctorService.getDoctorCount());
    }

    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getDoctorsByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();
        response.put("doctors", doctorService.getDoctorsByPage(page, size));
        response.put("total", doctorService.getDoctorCount());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addDoctor(@RequestParam("docName") String docName,
            @RequestParam("dId") Integer dId, @RequestParam("dName") String dName,
            @RequestParam("docGender") String docGender, @RequestParam("docAge") Integer docAge,
            @RequestParam(value = "docPhoto", required = false) MultipartFile docPhoto,
            @RequestParam("docClass") String docClass) {

        Map<String, Object> response = new HashMap<>();
        try {
            Doctor doctor = new Doctor();
            doctor.setDocName(docName);
            doctor.setDId(dId);
            doctor.setDName(dName);
            doctor.setDocGender(docGender);
            doctor.setDocAge(docAge);
            doctor.setDocClass(docClass);

            boolean result = doctorService.addDoctor(doctor, docPhoto);
            response.put("success", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateDoctor(@RequestParam("docId") Integer docId,
            @RequestParam("docName") String docName, @RequestParam("dId") Integer dId,
            @RequestParam("dName") String dName, @RequestParam("docGender") String docGender,
            @RequestParam("docAge") Integer docAge,
            @RequestParam(value = "docPhoto", required = false) MultipartFile docPhoto,
            @RequestParam("docClass") String docClass,
            @RequestParam(value = "currentPhoto", required = false) String currentPhoto) {

        Map<String, Object> response = new HashMap<>();
        try {
            Doctor doctor = new Doctor();
            doctor.setDocId(docId);
            doctor.setDocName(docName);
            doctor.setDId(dId);
            doctor.setDName(dName);
            doctor.setDocGender(docGender);
            doctor.setDocAge(docAge);
            doctor.setDocClass(docClass);

            // 保留原有照片或处理新上传照片
            if (docPhoto == null || docPhoto.isEmpty()) {
                doctor.setDocPhoto(currentPhoto);
            }

            boolean result = doctorService.updateDoctor(doctor, docPhoto);
            response.put("success", result);
            if (!result) {
                response.put("message", "更新医生信息失败");
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "文件上传错误: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "文件保存失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "系统错误: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/department/list")
    public ResponseEntity<List<Map<String, Object>>> getAllDepartments() {
        String sql = "SELECT d_id as id, d_name as name FROM department";
        List<Map<String, Object>> departments = doctorService.getJdbcTemplate().queryForList(sql);
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/listByDepartment")
    public ResponseEntity<List<Doctor>> getDoctorsByDepartment(@RequestParam String department) {
        try {
            List<Doctor> doctors = doctorService.getDoctorsByDepartment(department);
            if (doctors == null || doctors.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteDoctor(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        boolean result = doctorService.deleteDoctor(id);
        response.put("success", result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/department/{dId}")
    public ResponseEntity<Map<String, Object>> getDoctorsByDepartmentId(@PathVariable Integer dId) {
        try {
            // 查询医生信息
            String doctorSql = "SELECT d.doc_id as docId, d.doc_name as docName, d.d_id as dId, " +
                    "COALESCE(d.d_name, '') as dName, d.doc_gender as docGender, d.doc_age as docAge, " +
                    "d.doc_photo as docPhoto, d.doc_class as docClass, dept.d_floor as dFloor " +
                    "FROM doctor d LEFT JOIN department dept ON d.d_id = dept.d_id " +
                    "WHERE d.d_id = ?";
            
            List<Doctor> doctors = doctorService.getJdbcTemplate().query(doctorSql, new Object[]{dId}, (rs, rowNum) -> {
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
                doctor.setDFloor(rs.getString("dFloor")); // 新增科室楼层字段
                return doctor;
            });

            if (doctors == null || doctors.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            // 查询科室信息
            String deptSql = "SELECT d_name, d_floor FROM department WHERE d_id = ?";
            Map<String, Object> departmentInfo = doctorService.getJdbcTemplate().queryForMap(deptSql, dId);

            // 组合返回结果
            Map<String, Object> response = new HashMap<>();
            response.put("doctors", doctors);
            response.put("department", departmentInfo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}