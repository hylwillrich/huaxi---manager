package com.huaxizhongyao.medicine.pojo;


import java.util.Date;

public class News {
    private Integer n_id;
    private String n_title;
    private Date n_date;
    private String n_content;
    private String n_pic;

    @Override
    public String toString() {
        return "News{" +
                "n_id=" + n_id +
                ", n_title='" + n_title + '\'' +
                ", n_date=" + n_date +
                ", n_content='" + n_content + '\'' +
                ", n_pic='" + n_pic + '\'' +
                '}';
    }

    public Integer getN_id() {
        return n_id;
    }

    public void setN_id(Integer n_id) {
        this.n_id = n_id;
    }

    public String getN_title() {
        return n_title;
    }

    public void setN_title(String n_title) {
        this.n_title = n_title;
    }

    public Date getN_date() {
        return n_date;
    }

    public void setN_date(Date n_date) {
        this.n_date = n_date;
    }

    public String getN_content() {
        return n_content;
    }

    public void setN_content(String n_content) {
        this.n_content = n_content;
    }

    public String getN_pic() {
        return n_pic;
    }

    public void setN_pic(String n_pic) {
        this.n_pic = n_pic;
    }

    public News() {
    }

    public News(Integer n_id, String n_title, Date n_date, String n_content, String n_pic) {
        this.n_id = n_id;
        this.n_title = n_title;
        this.n_date = n_date;
        this.n_content = n_content;
        this.n_pic = n_pic;
    }
}