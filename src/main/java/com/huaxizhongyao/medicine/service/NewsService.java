package com.huaxizhongyao.medicine.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import com.huaxizhongyao.medicine.pojo.News;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.multipart.MultipartFile;

public interface NewsService {
    // 获取所有新闻
    List<News> getAllNews();

    // 分页获取新闻
    List<News> getNewsByPage(int page, int size);

    // 获取新闻总数
    int getNewsCount();

    // 根据id获取新闻
    Optional<News> getNewsById(Integer id);

    // 添加新闻
    boolean addNews(News news, MultipartFile photoFile) throws IOException;

    // 更新新闻
    boolean updateNews(News news, MultipartFile photoFile) throws IOException;

    // 删除新闻
    boolean deleteNews(Integer id);

    // 获取JdbcTemplate
    JdbcTemplate getJdbcTemplate();
}