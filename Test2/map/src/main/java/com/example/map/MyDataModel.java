package com.example.map;

public class MyDataModel {
    private String address;
    private String subject;
    private String price;
    private String year;
    private String month;
    private String date;

    // 無參數構造函數
    public MyDataModel() {}

    public String getAddress() {
        return address;
    }

    public String getSubject() {
        return subject;
    }

    public String getPrice() {
        return price;
    }

    public String getDate(){ return date; }
    public void setDate(String date){
        this.date = date;
    }

    public String getYear(){return year;}

    public String getMonth(){return month;}
}

