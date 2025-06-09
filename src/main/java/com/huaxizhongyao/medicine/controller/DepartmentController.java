package com.huaxizhongyao.medicine.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import com.huaxizhongyao.medicine.pojo.Department;
import com.huaxizhongyao.medicine.service.departmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/department")
public class DepartmentController {

    @Autowired
    private departmentService departmentService;

    @GetMapping("")
    public String department(HttpSession session, org.springframework.ui.Model model) {
        // 验证session是否存在
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        // 获取科室列表并添加到模型
        model.addAttribute("departmentService", departmentService);
        return "department";
    }

    // 获取所有科室
    @GetMapping("/list")
    public List<Department> getAllDepartments() {
        return departmentService.getAllDepartments();
    }

    // 获取科室总数
    @GetMapping("/count")
    public int getDepartmentCount() {
        return departmentService.getDepartmentCount();
    }

    // 添加科室
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addDepartment(@RequestBody Department department) {
        boolean result = departmentService.addDepartment(department);
        Map<String, Object> response = new HashMap<>();
        response.put("success", result);
        return ResponseEntity.ok(response);
    }

    // 更新科室
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateDepartment(
            @RequestBody Department department) {
        boolean result = departmentService.updateDepartment(department);
        Map<String, Object> response = new HashMap<>();
        response.put("success", result);
        return ResponseEntity.ok(response);
    }

    // 删除科室
    @PostMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteDepartment(@PathVariable Integer id) {
        boolean result = departmentService.deleteDepartment(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", result);
        return ResponseEntity.ok(response);
    }
}
