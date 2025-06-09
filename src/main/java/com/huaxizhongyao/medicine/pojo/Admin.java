package com.huaxizhongyao.medicine.pojo;

public class Admin {
    private Integer aid;
    private String admin_name;
    private String admin_password;

    @Override
    public String toString() {
        return "Admin{" +
                "aid=" + aid +
                ", admin_name='" + admin_name + '\'' +
                ", admin_password='" + admin_password + '\'' +
                '}';
    }

    public Integer getAid() {
        return aid;
    }

    public void setAid(Integer aid) {
        this.aid = aid;
    }

    public String getAdmin_name() {
        return admin_name;
    }

    public void setAdmin_name(String admin_name) {
        this.admin_name = admin_name;
    }

    public String getAdmin_password() {
        return admin_password;
    }

    public void setAdmin_password(String admin_password) {
        this.admin_password = admin_password;
    }

    public Admin() {
    }

    public Admin(Integer aid, String admin_name, String admin_password) {
        this.aid = aid;
        this.admin_name = admin_name;
        this.admin_password = admin_password;
    }
}
