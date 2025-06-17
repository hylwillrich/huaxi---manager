package com.huaxizhongyao.medicine.pojo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;

public class order {
    @JsonProperty("pId")
    private Integer p_id;
    
    @JsonProperty("pName") 
    private String p_name;
    
    @JsonProperty("pAge")
    private Integer p_age;
    
    @JsonProperty("dName")
    private String d_name;
    
    @JsonProperty("dFloor")
    private Integer d_floor;
    
    @JsonProperty("docName")
    private String doc_name;
    
    @JsonProperty("docClass")
    private String doc_class;
    
    @JsonProperty("pDatetime")
    private Date p_datetime;
    
    @JsonProperty("pStatus")
    private String p_status;

    @Override
    public String toString() {
        return "order{" +
                "p_id=" + p_id +
                ", p_name='" + p_name + '\'' +
                ", p_age=" + p_age +
                ", d_name='" + d_name + '\'' +
                ", d_floor=" + d_floor +
                ", doc_name='" + doc_name + '\'' +
                ", doc_class='" + doc_class + '\'' +
                ", p_datetime=" + p_datetime +
                ", p_status='" + p_status + '\'' +
                '}';
    }

    public Integer getP_id() {
        return p_id;
    }

    public void setP_id(Integer p_id) {
        this.p_id = p_id;
    }

    public String getP_name() {
        return p_name;
    }

    public void setP_name(String p_name) {
        this.p_name = p_name;
    }

    public Integer getP_age() {
        return p_age;
    }

    public void setP_age(Integer p_age) {
        this.p_age = p_age;
    }

    public String getD_name() {
        return d_name;
    }

    public void setD_name(String d_name) {
        this.d_name = d_name;
    }

    public Integer getD_floor() {
        return d_floor;
    }

    public void setD_floor(Integer d_floor) {
        this.d_floor = d_floor;
    }

    public String getDoc_name() {
        return doc_name;
    }

    public void setDoc_name(String doc_name) {
        this.doc_name = doc_name;
    }

    public String getDoc_class() {
        return doc_class;
    }

    public void setDoc_class(String doc_class) {
        this.doc_class = doc_class;
    }

    public Date getP_datetime() {
        return p_datetime;
    }

    public void setP_datetime(Date p_datetime) {
        this.p_datetime = p_datetime;
    }

    public String getP_status() {
        return p_status;
    }

    public void setP_status(String p_status) {
        this.p_status = p_status;
    }

    public order() {
    }

    public order(Integer p_id, String p_name, Integer p_age, String d_name, Integer d_floor, String doc_name, String doc_class, Date p_datetime, String p_status) {
        this.p_id = p_id;
        this.p_name = p_name;
        this.p_age = p_age;
        this.d_name = d_name;
        this.d_floor = d_floor;
        this.doc_name = doc_name;
        this.doc_class = doc_class;
        this.p_datetime = p_datetime;
        this.p_status = p_status;
    }
}