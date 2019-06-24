package com.example.sunnyday;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.sunnyday.Entity.City;
import com.example.sunnyday.Entity.County;
import com.example.sunnyday.Entity.Province;
import com.example.sunnyday.Util.AreaJsonHandler;
import com.example.sunnyday.Util.OkHttpUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseArea extends AppCompatActivity {
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> stringList = new ArrayList<>();
    private ProgressBar progressBar;

    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private int level;

    private Province selectedProvince;
    private City selectedCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.area_choose);
        Toolbar toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("China");
        listView = findViewById(R.id.list_view);
        queryProvince("http://guolin.tech/api/china");
    }

    public void queryProvince(String url){
        showProgressBar();
        OkHttpUtil.sendRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                Toast.makeText(ChooseArea.this, "Failed to get data", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonString = response.body().string();
                List<Province> list = AreaJsonHandler.handleProvinceJson(jsonString);
                stringList.clear();
                for (Province province :list){
                    stringList.add(province.getName());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressBar();
                        ActionBar actionBar =  getSupportActionBar();
                        actionBar.setTitle("China");
                        actionBar.setDisplayHomeAsUpEnabled(false);
                        adapter = new ArrayAdapter<>(ChooseArea.this, android.R.layout.simple_list_item_1, stringList);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                selectedProvince = list.get(position);
                                int provinceId = selectedProvince.getProvinceId();
                                queryCity("http://guolin.tech/api/china/" + provinceId , selectedProvince);
                            }
                        });
                    }
                });


            }
        });

    }

    public void queryCity(String url, Province selected){
        OkHttpUtil.sendRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonString = response.body().string();
                List<City> cityList = AreaJsonHandler.handleCityJson(jsonString);
                stringList.clear();
                for (City city : cityList){
                    stringList.add(city.getName());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ActionBar actionBar = getSupportActionBar();
                        actionBar.setTitle(selected.getName() + "省");
                        actionBar.setDisplayHomeAsUpEnabled(true);
                        level = LEVEL_CITY;
                        adapter.notifyDataSetChanged();
                        listView.setSelection(0);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                selectedCity = cityList.get(position);
                                int cityId = selectedCity.getCityId();
                                queryCounty("http://guolin.tech/api/china/" + selected.getProvinceId()+"/" + cityId, selectedCity);
                            }
                        });
                    }
                });


            }
        });
    }

    public void queryCounty(String url, City selectedCity){
        OkHttpUtil.sendRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonString = response.body().string();
                List<County> countyList = AreaJsonHandler.handleCountyJson(jsonString);
                stringList.clear();
                for (County county : countyList){
                    stringList.add(county.getName());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getSupportActionBar().setTitle(selectedCity.getName() + "市");
                        level = LEVEL_COUNTY;
                        adapter.notifyDataSetChanged();
                    }
                });

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if (level == LEVEL_COUNTY){
                    queryCity("http://guolin.tech/api/china/" + selectedProvince.getProvinceId(),selectedProvince);
                }else if (level == LEVEL_CITY){
                    queryProvince("http://guolin.tech/api/china");
                }
                break;
                default:
                    break;

        }
        return true;
    }

    public void showProgressBar(){
        progressBar.setVisibility(View.VISIBLE);

    }

    public void closeProgressBar(){
        if (progressBar.getVisibility() == View.VISIBLE ){
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
