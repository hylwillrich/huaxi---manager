package com.huaxizhongyao.medicine.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import com.huaxizhongyao.medicine.pojo.order;
import com.huaxizhongyao.medicine.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("")
    public String order(HttpSession session, org.springframework.ui.Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        model.addAttribute("orderService", orderService);
        return "order";
    }

    // 添加预约
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addOrder(@RequestBody order order) {
        try {
            System.out.println("接收到预约请求数据:");
            System.out.println("患者姓名: " + order.getP_name());
            System.out.println("科室楼层: " + order.getD_floor());
            System.out.println("医生职称: " + order.getDoc_class());
            System.out.println("完整对象: " + order);
            
            boolean success = orderService.addOrder(order);
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "预约添加成功");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "预约添加失败");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 更新预约
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateOrder(@RequestBody order order) {
        try {
            boolean success = orderService.updateOrder(order);
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "预约更新成功");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "预约更新失败");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 删除预约
    @DeleteMapping("/delete/{pId}")
    public ResponseEntity<Map<String, Object>> deleteOrder(@PathVariable Integer pId) {
        try {
            boolean success = orderService.deleteOrder(pId);
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "预约删除成功");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "预约删除失败");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 获取单个预约详情
    @GetMapping("/{pId}")
    public ResponseEntity<Map<String, Object>> getOrderById(@PathVariable Integer pId) {
        try {
            order order = orderService.getOrderById(pId);
            if (order != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("order", order);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "未找到预约记录");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 分页查询预约列表
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getOrdersByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<order> orders = orderService.getOrdersByPage(page, size);
            int total = orderService.getTotalOrders();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orders", orders);
            response.put("total", total);
            response.put("page", page);
            response.put("size", size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 获取所有预约
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllOrders() {
        try {
            List<order> orders = orderService.getAllOrders();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orders", orders);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 更新预约状态
    @PostMapping("/updateStatus")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @RequestParam Integer pId,
            @RequestParam String status) {
        try {
            boolean success = orderService.updateOrderStatus(pId, status);
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "状态更新成功");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "状态更新失败");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 批量更新预约状态
    @PostMapping("/batchUpdateStatus")
    public ResponseEntity<Map<String, Object>> batchUpdateStatus(@RequestBody List<Map<String, Object>> orders) {
        try {
            int updatedCount = orderService.batchUpdateStatus(orders);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("updatedCount", updatedCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 获取JdbcTemplate
    // 获取性别选项
    @GetMapping("/genderOptions")
    public ResponseEntity<List<String>> getGenderOptions() {
        return ResponseEntity.ok(orderService.getGenderOptions());
    }

    @GetMapping("/jdbc")
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}