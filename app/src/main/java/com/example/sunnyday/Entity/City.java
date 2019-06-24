package com.example.sunnyday.Entity;

public class City {
    int provinceId;
    String name;
    int cityId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
