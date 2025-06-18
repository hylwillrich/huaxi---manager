package com.huaxizhongyao.medicine.service;

import java.util.List;
import java.util.Map;
import com.huaxizhongyao.medicine.pojo.order;
import org.springframework.jdbc.core.JdbcTemplate;

    public interface OrderService {
    // 添加预约
    boolean addOrder(order order);
    
    // 获取患者性别选项
    List<String> getGenderOptions();
    
    // 更新预约
    boolean updateOrder(order order);
    
    // 删除预约
    boolean deleteOrder(Integer pId);
    
    // 获取单个预约详情
    order getOrderById(Integer pId);
    
    // 分页查询预约列表
    List<order> getOrdersByPage(int page, int size);
    
    // 获取预约总数
    int getTotalOrders();
    
    // 获取所有预约
    List<order> getAllOrders();
    
    // 更新预约状态
    boolean updateOrderStatus(Integer pId, String status);
    
    /**
     * 批量更新预约状态
     * @param orders 包含预约ID和状态的Map列表
     * @return 成功更新的记录数
     */
    int batchUpdateStatus(List<Map<String, Object>> orders);
    
    // 获取JdbcTemplate
    JdbcTemplate getJdbcTemplate();
}