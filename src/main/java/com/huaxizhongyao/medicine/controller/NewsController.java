package com.huaxizhongyao.medicine.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import com.huaxizhongyao.medicine.pojo.News;
import com.huaxizhongyao.medicine.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @GetMapping("")
    public String news(HttpSession session, org.springframework.ui.Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        model.addAttribute("newsService", newsService);
        return "news";
    }

    @GetMapping("/list")
    public ResponseEntity<List<News>> getAllNews() {
        return ResponseEntity.ok(newsService.getAllNews());
    }

    @GetMapping("/{id}")
    public ResponseEntity<News> getNewsById(@PathVariable Integer id) {
        return newsService.getNewsById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getNewsCount() {
        return ResponseEntity.ok(newsService.getNewsCount());
    }

    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getNewsByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();
        response.put("news", newsService.getNewsByPage(page, size));
        response.put("total", newsService.getNewsCount());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addNews(@RequestParam("n_title") String n_title,
            @RequestParam("n_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date n_date,
            @RequestParam("n_content") String n_content,
            @RequestParam(value = "n_pic", required = false) MultipartFile n_pic) {

        Map<String, Object> response = new HashMap<>();
        try {
            News news = new News();
            news.setN_title(n_title);
            news.setN_date(n_date);
            news.setN_content(n_content);
            

            boolean result = newsService.addNews(news, n_pic);
            response.put("success", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateNews(@RequestParam("n_id") Integer n_id,
            @RequestParam("n_title") String n_title, 
            @RequestParam("n_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date n_date,
            @RequestParam("n_content") String n_content,
            @RequestParam(value = "n_pic", required = false) MultipartFile n_pic,
            @RequestParam(value = "currentPhotoName", required = false) String currentPhotoName) {

        Map<String, Object> response = new HashMap<>();
        try {
            News news = new News();
            news.setN_id(n_id);
            news.setN_title(n_title);
            news.setN_date(n_date);
            news.setN_content(n_content);

            // 保留原有照片或处理新上传照片
            if (n_pic == null || n_pic.isEmpty()) {
                if (currentPhotoName != null && !currentPhotoName.isEmpty()) {
                    news.setN_pic("/uploads/news/" + currentPhotoName);
                }
            }

            boolean result = newsService.updateNews(news, n_pic);
            response.put("success", result);
            if (!result) {
                response.put("message", "更新新闻信息失败");
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

    @PostMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteNews(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        boolean result = newsService.deleteNews(id);
        response.put("success", result);
        return ResponseEntity.ok(response);
    }
}