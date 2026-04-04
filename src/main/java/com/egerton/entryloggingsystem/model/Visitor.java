package com.egerton.entryloggingsystem.model;

public class Visitor {
    private int id;
    private String fullName;
    private String officeVisiting;
    private String purpose;
    private String phone;
    private int expectedDuration;
    
    public Visitor() {}
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getOfficeVisiting() { return officeVisiting; }
    public void setOfficeVisiting(String officeVisiting) { this.officeVisiting = officeVisiting; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public int getExpectedDuration() { return expectedDuration; }
    public void setExpectedDuration(int expectedDuration) { this.expectedDuration = expectedDuration; }
}
