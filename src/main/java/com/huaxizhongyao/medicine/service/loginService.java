package com.huaxizhongyao.medicine.service;

import java.sql.*;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.stereotype.Service;

@Service
public class loginService {
    private Connection connection;

    @PostConstruct
    public void init() throws SQLException {
        String url = "jdbc:mysql://192.168.92.177:3306/huaxi_chinesemedicine";
        String user = "root";
        String password = "root";
        connection = DriverManager.getConnection(url, user, password);
    }

    public boolean loginValidate(String username, String password) {
        String sql = "SELECT * FROM admin WHERE admin_name = ? AND admin_password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @PreDestroy
    public void cleanup() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
