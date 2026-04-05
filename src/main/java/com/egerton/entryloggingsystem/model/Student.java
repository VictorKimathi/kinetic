package com.egerton.entryloggingsystem.model;

public class Student {
    private int id;
    private String regNumber;
    private String fullName;
    private String courseName;
    private String residence;
    private String phone;
    private String email;
    
    public Student() {}
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getRegNumber() { return regNumber; }
    public void setRegNumber(String regNumber) { this.regNumber = regNumber; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    
    public String getResidence() { return residence; }
    public void setResidence(String residence) { this.residence = residence; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
