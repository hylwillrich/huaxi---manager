package com.huaxizhongyao.medicine.pojo;

public class Doctor {
    private Integer docId; // 医生ID
    private String docName; // 医生姓名
    private Integer dId; // 所属科室ID
    private String dName; // 所属科室名称
    private String docGender; // 性别
    private Integer docAge; // 年龄
    private String docPhoto; // 照片路径
    private String docClass; // 医生级别
    private String dFloor;





    // 构造方法
    public Doctor() {}

    // Getter和Setter方法



    public String getDFloor() {
        return dFloor;
    }
    
    public void setDFloor(String dFloor) {
        this.dFloor = dFloor;
    }

    public Integer getDocId() {
        return docId;
    }

    public void setDocId(Integer docId) {
        this.docId = docId;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public Integer getDId() {
        return dId;
    }

    public void setDId(Integer dId) {
        this.dId = dId;
    }

    public String getDName() {
        return dName;
    }

    public void setDName(String dName) {
        this.dName = dName;
    }

    public String getDocGender() {
        return docGender;
    }

    public void setDocGender(String docGender) {
        this.docGender = docGender;
    }

    public Integer getDocAge() {
        return docAge;
    }

    public void setDocAge(Integer docAge) {
        this.docAge = docAge;
    }

    public String getDocPhoto() {
        return docPhoto;
    }

    public void setDocPhoto(String docPhoto) {
        this.docPhoto = docPhoto;
    }

    public String getDocClass() {
        return docClass;
    }

    public void setDocClass(String docClass) {
        this.docClass = docClass;
    }
}
