package com.huaxizhongyao.medicine.pojo;

public class Department {
    private Integer id; // 科室ID(d_id)
    private String name; // 科室名称(d_name)
    private String floor; // 科室楼层(d_floor)
    private String telephone; // 科室电话(d_telephone)
    private Boolean isOpen; // 科室状态(d_isopen)

    // 构造方法
    public Department() {}

    // Getter和Setter方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Boolean getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(Boolean isOpen) {
        this.isOpen = isOpen;
    }
}
