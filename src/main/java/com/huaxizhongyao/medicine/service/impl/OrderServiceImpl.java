package com.huaxizhongyao.medicine.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.huaxizhongyao.medicine.pojo.order;
import com.huaxizhongyao.medicine.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public boolean addOrder(order order) {
        System.out.println("添加预约信息：");
        System.out.println("患者ID: " + order.getP_id());
        System.out.println("患者姓名: " + order.getP_name());
        System.out.println("患者年龄: " + order.getP_age());
        System.out.println("患者性别: " + order.getP_gender());
        System.out.println("科室名称: " + order.getD_name());
        System.out.println("科室楼层: " + order.getD_floor());
        System.out.println("医生姓名: " + order.getDoc_name());
        System.out.println("医生职称: " + order.getDoc_class());
        System.out.println("预约时间: " + order.getP_datetime());
        System.out.println("预约状态: " + order.getP_status());
        
        String sql = "INSERT INTO `order` (p_name, p_age, p_gender, d_name, d_floor, doc_name, doc_class, p_datetime, p_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int result = jdbcTemplate.update(sql,
                order.getP_name(),
                order.getP_age(),
                order.getP_gender(),
                order.getD_name(),
                order.getD_floor(),
                order.getDoc_name(),
                order.getDoc_class(),
                order.getP_datetime(),
                order.getP_status());
        return result > 0;
    }

    @Override
    public boolean updateOrder(order order) {
        System.out.println("更新预约信息：");
        System.out.println("患者ID: " + order.getP_id());
        System.out.println("患者姓名: " + order.getP_name());
        System.out.println("患者年龄: " + order.getP_age());
        System.out.println("患者性别: " + order.getP_gender());
        System.out.println("科室名称: " + order.getD_name());
        System.out.println("科室楼层: " + order.getD_floor());
        System.out.println("医生姓名: " + order.getDoc_name());
        System.out.println("医生职称: " + order.getDoc_class());
        System.out.println("预约时间: " + order.getP_datetime());
        System.out.println("预约状态: " + order.getP_status());
        
        String sql = "UPDATE `order` SET p_name=?, p_age=?, p_gender=?, d_name=?, d_floor=?, doc_name=?, doc_class=?, p_datetime=?, p_status=? WHERE p_id=?";
        int result = jdbcTemplate.update(sql,
                order.getP_name(),
                order.getP_age(),
                order.getP_gender(),
                order.getD_name(),
                order.getD_floor(),
                order.getDoc_name(),
                order.getDoc_class(),
                order.getP_datetime(),
                order.getP_status(),
                order.getP_id());
        return result > 0;
    }

    @Override
    public boolean deleteOrder(Integer pId) {
        String sql = "DELETE FROM `order` WHERE p_id=?";
        int result = jdbcTemplate.update(sql, pId);
        return result > 0;
    }

    @Override
    public order getOrderById(Integer pId) {
        String sql = "SELECT p_id, p_name, p_age, p_gender, d_name, d_floor, doc_name, doc_class, p_datetime, p_status FROM `order` WHERE p_id=?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(order.class), pId);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<order> getOrdersByPage(int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT p_id, p_name, p_age, p_gender, d_name, d_floor, doc_name, doc_class, p_datetime, p_status FROM `order` LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(order.class), size, offset);
    }

    @Override
    public int getTotalOrders() {
        String sql = "SELECT COUNT(*) FROM `order`";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    @Override
    public List<order> getAllOrders() {
        String sql = "SELECT p_id, p_name, p_age, p_gender, d_name, d_floor, doc_name, doc_class, p_datetime, p_status FROM `order`";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(order.class));
    }

    @Override
    public boolean updateOrderStatus(Integer pId, String status) {
        String sql = "UPDATE `order` SET p_status=? WHERE p_id=?";
        int result = jdbcTemplate.update(sql, status, pId);
        return result > 0;
    }

    @Override
    @Transactional
    public int batchUpdateStatus(List<Map<String, Object>> orders) {
        int updatedCount = 0;
        for (Map<String, Object> order : orders) {
            try {
                Integer pId = (Integer) order.get("id");
                String status = (String) order.get("status");
                if (pId != null && status != null && updateOrderStatus(pId, status)) {
                    updatedCount++;
                }
            } catch (Exception e) {
                System.err.println("批量更新预约状态失败: " + e.getMessage());
            }
        }
        return updatedCount;
    }

    @Override
    public List<String> getGenderOptions() {
        return Arrays.asList("男", "女", "其他");
    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}