package com.example.sunnyday.Util;

import com.example.sunnyday.Entity.City;
import com.example.sunnyday.Entity.County;
import com.example.sunnyday.Entity.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AreaJsonHandler {


    public static List<Province> handleProvinceJson(String string){
        List<Province> provinceList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(string);
            for (int i = 0; i<jsonArray.length(); i++){
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                Province province = new Province();
                province.setProvinceId(jsonObject.getInt("id"));
                province.setName(jsonObject.getString("name"));
                provinceList.add(province);
            }

        }catch (JSONException e){
            e.printStackTrace();
        }
        return provinceList;
    }

    public static List<City> handleCityJson(String string){
        List<City> cityList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(string);
            for (int i=0; i<jsonArray.length(); i++){
                JSONObject object = (JSONObject) jsonArray.get(i);
                int id = object.getInt("id");
                String name = object.getString("name");
                City city = new City();
                city.setName(name);
                city.setCityId(id);
                cityList.add(city);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return cityList;
    }

    public static List<County> handleCountyJson(String string){
        List<County> countyList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(string);
            for (int i=0; i<jsonArray.length(); i++){
                JSONObject object = (JSONObject) jsonArray.get(i);
                County county = new County();
                int countyId = object.getInt("id");
                String name = object.getString("name");
                String weatherId = object.getString("weather_id");
                county.setCountyId(countyId);
                county.setName(name);
                county.setWeatherId(weatherId);
                countyList.add(county);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return countyList;
    }

}
