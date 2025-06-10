package com.huaxizhongyao.medicine.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import com.huaxizhongyao.medicine.pojo.News;
import com.huaxizhongyao.medicine.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class NewsServiceImpl implements NewsService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<News> getAllNews() {

        String sql = "SELECT n_id as nId,n_title as nTitle,n_content as nContent,n_date as nDate , n_pic as nPic FROM news";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(News.class));
    }

    @Override
    public List<News> getNewsByPage(int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT n_id as nId,n_title as nTitle,n_content as nContent,n_date as nDate , n_pic as nPic FROM news LIMIT ? OFFSET ?";


        List<News> Newsgroup =
                jdbcTemplate.query(sql, new Object[] {size, offset}, (rs, rowNum) -> {
                    News news = new News();
                    news.setN_id(rs.getInt("nId"));
                    news.setN_title(rs.getString("nTitle"));
                    news.setN_date(rs.getDate("nDate"));
                    news.setN_content(rs.getString("nContent"));
                    String photoPath = rs.getString("nPic");
                    // 统一处理图片路径格式（确保有/uploads/news/前缀）
                    if (photoPath != null && !photoPath.isEmpty()) {
                        String processedPath = photoPath.replaceFirst("^/?(uploads/news/)?", "/uploads/news/");
                        news.setN_pic(processedPath);
                        System.out.println("[DEBUG] 分页查询图片路径处理: " + photoPath + " -> " + processedPath);
                    } else {
                        news.setN_pic("");
                        System.out.println("[DEBUG] 分页查询图片路径为空");
                    }
                    
                    return news;
                });



        return Newsgroup;
    }

    @Override
    public int getNewsCount() {
        String sql = "SELECT COUNT(*) FROM  news";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }


    private String uploadBaseDir = "src/main/resources/static/uploads";

    @Override
    @Transactional
    public boolean addNews(News news, MultipartFile photoFile) throws IOException {
        String fileName = null;
        File destFile = null;
        
        try {
            // 详细日志记录输入参数
            System.out.println("[DEBUG] 开始添加新闻，参数检查:");
            System.out.println("[DEBUG] 标题: " + news.getN_title());
            System.out.println("[DEBUG] 日期: " + news.getN_date());
            System.out.println("[DEBUG] 内容长度: " + (news.getN_content() != null ? news.getN_content().length() : "null"));
            System.out.println("[DEBUG] 图片文件: " + (photoFile != null ? photoFile.getOriginalFilename() : "null"));

            // 验证必填字段
            if (news.getN_title() == null || news.getN_title().trim().isEmpty()) {
                throw new IllegalArgumentException("新闻标题不能为空");
            }
            if (news.getN_date() == null) {
                throw new IllegalArgumentException("新闻日期不能为空");
            }
            if (news.getN_content() == null || news.getN_content().trim().isEmpty()) {
                throw new IllegalArgumentException("新闻内容不能为空");
            }

            // 转换Date对象为SQL Date
            java.sql.Date sqlDate = new java.sql.Date(news.getN_date().getTime());
            System.out.println("[DEBUG] 日期格式转换: " + news.getN_date() + " -> SQL Date: " + sqlDate);

            // 构建上传目录路径
            String uploadDir = new File(uploadBaseDir, "news").getAbsolutePath() + File.separator;
            System.out.println("[DEBUG] 文件上传目录: " + uploadDir);
            
            // 确保上传目录存在
            File uploadPath = new File(uploadDir);
            if (!uploadPath.exists() && !uploadPath.mkdirs()) {
                throw new IOException("无法创建上传目录: " + uploadDir);
            }

            // 处理图片文件
            if (photoFile != null && !photoFile.isEmpty()) {
                // 生成唯一文件名
                String originalFilename = photoFile.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                fileName = UUID.randomUUID().toString() + fileExtension;
                
                // 创建目标文件
                destFile = new File(uploadDir + fileName);
                
                // 确保父目录存在
                File parent = destFile.getParentFile();
                if (!parent.exists() && !parent.mkdirs()) {
                    throw new IOException("无法创建父目录: " + parent.getAbsolutePath());
                }
                
                // 保存文件
                photoFile.transferTo(destFile);
                String picPath = "/uploads/news/" + fileName;
                news.setN_pic(picPath);
                System.out.println("[DEBUG] 图片保存成功，路径: " + picPath);
            } else {
                news.setN_pic(""); // 确保图片路径不为null
                System.out.println("[DEBUG] 无图片上传，设置空路径");
            }

            // 执行数据库插入
            String sql = "INSERT INTO news (n_title, n_date, n_content, n_pic) VALUES (?, ?, ?, ?)";
            System.out.println("[DEBUG] 执行SQL: " + sql);
            System.out.println("[DEBUG] 参数: " + news.getN_title() + ", " + sqlDate + ", " 
                + (news.getN_content() != null ? "内容(长度:" + news.getN_content().length() + ")" : "null") + ", " 
                + (news.getN_pic() != null ? news.getN_pic() : "null"));
            
            int result = jdbcTemplate.update(sql, 
                news.getN_title(), 
                sqlDate, 
                news.getN_content(), 
                news.getN_pic() != null ? news.getN_pic() : ""
            );
            
            System.out.println("[DEBUG] 数据库操作结果: " + result + " 行受影响");
            
            if (result <= 0) {
                // 数据库插入失败，删除已上传的文件
                if (destFile != null && destFile.exists()) {
                    System.out.println("[DEBUG] 数据库插入失败，删除已上传的文件: " + destFile.getAbsolutePath());
                    destFile.delete();
                }
                return false;
            }
            
            System.out.println("[DEBUG] 新闻添加成功");
            return true;
        } catch (Exception e) {
            // 发生异常时删除已上传的文件
            if (destFile != null && destFile.exists()) {
                destFile.delete();
            }
            throw new IOException("添加新闻失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateNews(News news, MultipartFile photoFile) throws IOException {
        String originalPicPath = null;
        File oldPhotoFile = null;
        
        try {
            // 先查询原有图片路径
            originalPicPath = jdbcTemplate.queryForObject(
                "SELECT n_pic FROM news WHERE n_id=?", 
                String.class, 
                news.getN_id()
            );

            // 处理图片文件
            if (photoFile != null && !photoFile.isEmpty()) {
                // 构建上传目录路径
                String uploadDir = new File(uploadBaseDir, "news").getAbsolutePath() + File.separator;
                System.out.println("更新新闻文件上传目录: " + uploadDir);
                
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
                news.setN_pic("/uploads/news/" + fileName);
                
                // 准备删除旧图片
                if (originalPicPath != null && !originalPicPath.isEmpty()) {
                    String oldFileName = originalPicPath.substring(originalPicPath.lastIndexOf("/") + 1);
                    oldPhotoFile = new File(uploadDir + oldFileName);
                }
            } else {
                // 如果没有上传新图片，保留原有图片路径
                news.setN_pic(originalPicPath != null ? originalPicPath : "");
            }

            String sql = "UPDATE news SET n_title=?, n_date=?, n_content=?, n_pic=? WHERE n_id=?";
            int result = jdbcTemplate.update(sql, 
                news.getN_title(), 
                news.getN_date(), 
                news.getN_content(), 
                news.getN_pic(), 
                news.getN_id()
            );
            
            // 更新成功后删除旧图片
            if (result > 0 && oldPhotoFile != null && oldPhotoFile.exists()) {
                try {
                    if (!oldPhotoFile.delete()) {
                        System.err.println("无法删除旧图片文件: " + oldPhotoFile.getAbsolutePath());
                    }
                } catch (SecurityException e) {
                    System.err.println("删除旧图片时发生安全异常: " + e.getMessage());
                }
            }
            
            return result > 0;
        } catch (Exception e) {
            throw new IOException("更新新闻失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteNews(Integer id) {
        try {
            // 1. 先获取新闻的照片路径
            String photoPath = jdbcTemplate.queryForObject(
                "SELECT n_pic FROM news WHERE n_id=?", 
                String.class, 
                id
            );
            
            // 2. 删除数据库记录
            String sql = "DELETE FROM news WHERE n_id=?";
            int result = jdbcTemplate.update(sql, id);
            
            // 3. 如果删除成功且存在照片，删除照片文件
            if (result > 0 && photoPath != null && !photoPath.isEmpty()) {
                // 从路径中提取文件名
                String fileName = photoPath.substring(photoPath.lastIndexOf("/") + 1);
                // 构建完整文件路径
                String filePath = uploadBaseDir + "/news/" + fileName;
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
            System.err.println("删除信息失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Override
    public Optional<News> getNewsById(Integer id) {
        // 添加调试日志
        
        
        String sql = "SELECT n_id as nId, n_title as nTitle, n_content as nContent, n_date as nDate, n_pic as nPic FROM news WHERE n_id = ?";
        try {
            
            // 1. 首先检查数据库中是否存在该记录
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM news WHERE n_id = ?", 
                Integer.class, 
                id
            );
            
            
            if (count == 0) {
                
                return Optional.empty();
            }
            
            // 2. 尝试直接查询原始数据，不进行映射
            Map<String, Object> rawData = jdbcTemplate.queryForMap(sql, id);
            System.out.println("原始查询结果: " + rawData);
            
            // 3. 尝试手动映射
            News news = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                News n = new News();
                n.setN_id(rs.getInt("nId"));
                n.setN_title(rs.getString("nTitle"));
                n.setN_content(rs.getString("nContent"));
                n.setN_date(rs.getDate("nDate"));
                n.setN_pic(rs.getString("nPic"));
                return n;
            }, id);
            
            
            // 统一处理图片路径格式
            if (news != null && news.getN_pic() != null && !news.getN_pic().isEmpty()) {
                String processedPic = news.getN_pic().startsWith("/") ? news.getN_pic() : "/" + news.getN_pic();
                news.setN_pic(processedPic);
                System.out.println("处理后的图片路径: " + processedPic);
            }
            
            return Optional.ofNullable(news);
        } catch (Exception e) {
            System.err.println("查询新闻详情出错: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
}