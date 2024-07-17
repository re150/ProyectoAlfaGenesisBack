package com.alfagenesi.com.BackAG.model;

public class User {
    private String name;
    private int grade;
    private String Group;
    private String password;

    public User() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGrade() {
        return this.grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public String getGroup() {
        return this.Group;
    }

    public void setGroup(String group) {
        this.Group = group;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
