package com.huaxizhongyao.medicine.mapper;

import java.util.List;
import com.huaxizhongyao.medicine.pojo.News;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface NewsMapper {
    @Select("SELECT * FROM news")
    List<News> findAll();

    @Select("SELECT * FROM news WHERE n_id = #{id}")
    News findById(Integer id);

    @Insert("INSERT INTO news(n_title, n_content, n_pic, n_date) " +
            "VALUES(#{n_title}, #{n_content}, #{n_pic}, #{n_date})")
    @Options(useGeneratedKeys = true, keyProperty = "n_id")
    int insert(News news);

    @Update("UPDATE news SET n_title=#{n_title}, n_content=#{n_content}, " +
            "n_pic=#{n_pic}, n_date=#{n_date} WHERE n_id=#{n_id}")
    int update(News news);

    @Delete("DELETE FROM news WHERE n_id = #{id}")
    int delete(Integer id);

    @Select("SELECT * FROM news")
    List<News> findByPage();
}