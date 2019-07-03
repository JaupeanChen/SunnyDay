package com.example.sunnyday;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.sunnyday.Util.OkHttpUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.Code;
import interfaces.heweather.com.interfacesmodule.bean.air.now.AirNow;
import interfaces.heweather.com.interfacesmodule.bean.air.now.AirNowCity;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.Forecast;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.ForecastBase;
import interfaces.heweather.com.interfacesmodule.bean.weather.lifestyle.Lifestyle;
import interfaces.heweather.com.interfacesmodule.bean.weather.lifestyle.LifestyleBase;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.NowBase;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class WeatherActivity extends AppCompatActivity {
    public String weatherId;

    private TextView date, tmp,weatherNow,wind,maxTmp;
    private LinearLayout linearLayout;

    private TextView comfit,wear,protect,wash;
    private TextView ql,aqi,pm;

    private Toolbar toolbar;
    private ImageView titleView;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView backgroundView;

    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        HeConfig.init("HE1906262053031373","b221f29802ca44d5a1ada8d11a65e8d2");
        HeConfig.switchToFreeServerNode();
        fitsSystemWindows();
        date = findViewById(R.id.date);
        tmp = findViewById(R.id.temp);
        weatherNow =findViewById(R.id.weather_now);
        wind = findViewById(R.id.wind);
        maxTmp =findViewById(R.id.max_tmp);

        comfit = findViewById(R.id.comfit);
        wear = findViewById(R.id.wear);
        protect = findViewById(R.id.protect);
        wash = findViewById(R.id.washing_car);

        ql = findViewById(R.id.ql);
        aqi = findViewById(R.id.aqi);
        pm = findViewById(R.id.pm25);

        linearLayout =findViewById(R.id.forecast);

        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        setListenerForSwipe();

        titleView =findViewById(R.id.image);
        toolbar = findViewById(R.id.toolbar_weather);
        collapsingToolbarLayout = findViewById(R.id.collapseView);
        backgroundView = findViewById(R.id.background_view);
        setSupportActionBar(toolbar);
        String name = getIntent().getStringExtra("city_name");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null ){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        collapsingToolbarLayout.setCollapsedTitleTextColor(292421);
        collapsingToolbarLayout.setTitle(name);

        weatherId = getIntent().getStringExtra("weather_id");
        queryWeatherNow(weatherId);
        queryWeatherAir(weatherId);
        queryWeatherLifestyle(weatherId);
        queryWeatherForecast(weatherId);
        setScrollViewBackground();
    }


    public void queryWeatherNow(String weatherId){
        HeWeather.getWeatherNow(WeatherActivity.this, weatherId, new HeWeather.OnResultWeatherNowBeanListener() {
            @Override
            public void onError(Throwable throwable) {
                Toast.makeText(WeatherActivity.this,"请检查网络",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onSuccess(Now now) {
                if (Code.OK.getCode().equalsIgnoreCase(now.getStatus())){
                    NowBase nowBase = now.getNow();
                    String temp = nowBase.getTmp();
                    tmp.setText(temp + "°C");
                    Date dateNow = new Date();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
                    date.setText(format.format(dateNow));
                    String condType = nowBase.getCond_txt();
                    weatherNow.setText(condType);
                    wind.setText(nowBase.getWind_dir());
                    maxTmp.setText("max:" + nowBase.getFl()+"°C");
                    setTitleView(condType);
                }else {
                    Toast.makeText(WeatherActivity.this,"Fail to get weather_now message.",Toast.LENGTH_SHORT).show();
                    String status = now.getStatus();
                    Code code = Code.toEnum(status);
                    Log.i("Fail","failed code:"+code);

                }

            }
        });
    }

    public void queryWeatherAir(String weatherId){
        HeWeather.getAirNow(WeatherActivity.this, weatherId, new HeWeather.OnResultAirNowBeansListener() {
            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onSuccess(AirNow airNow) {
                if (Code.OK.getCode().equalsIgnoreCase(airNow.getStatus())){
                    AirNowCity city = airNow.getAir_now_city();
                    ql.setText(city.getQlty());
                    aqi.setText(city.getAqi());
                    pm.setText(city.getPm25());
                }else {
                    Toast.makeText(WeatherActivity.this,"Fail to get air_now message.",Toast.LENGTH_SHORT).show();
                    String string = "--";
                    ql.setText(string);
                    aqi.setText(string);
                    pm.setText(string);
                    String status = airNow.getStatus();
                    Code code = Code.toEnum(status);
                    Log.i("Fail","failed code:"+code);
                }

            }
        });
    }

    public void queryWeatherLifestyle(String weatherId){
        HeWeather.getWeatherLifeStyle(WeatherActivity.this, weatherId, new HeWeather.OnResultWeatherLifeStyleBeanListener() {
            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onSuccess(Lifestyle lifestyle) {
                if (Code.OK.getCode().equalsIgnoreCase(lifestyle.getStatus())){
                    List<LifestyleBase> list = lifestyle.getLifestyle();
                    for (LifestyleBase base : list){
                        if (base.getType().equals("comf")){
                            comfit.setText("舒适度:" + base.getTxt());
                        }
                        if (base.getType().equals("drsg")){
                            wear.setText("着装:" + base.getTxt());
                        }
                        if (base.getType().equals("uv")){
                            protect.setText("紫外线：" + base.getTxt());
                        }
                        if (base.getType().equals("cw")){
                            wash.setText("洗车:" + base.getTxt());
                        }
                    }
                }else {
                    Toast.makeText(WeatherActivity.this,"Fail to get lifestyle message.",Toast.LENGTH_SHORT).show();
                    String status = lifestyle.getStatus();
                    Code code = Code.toEnum(status);
                    Log.i("Fail","failed code:"+code);
                }

            }
        });
    }

    public void queryWeatherForecast(String weatherId){
        HeWeather.getWeatherForecast(WeatherActivity.this, weatherId, new HeWeather.OnResultWeatherForecastBeanListener() {
            @Override
            public void onError(Throwable throwable) {
                swipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onSuccess(Forecast forecast) {
                if (Code.OK.getCode().equalsIgnoreCase(forecast.getStatus())){
                    linearLayout.removeAllViews();
                    List<ForecastBase> list = forecast.getDaily_forecast();
                    for (ForecastBase forecastBase : list){
                        View view = getLayoutInflater().inflate(R.layout.forecast_item, null);
                        TextView date = view.findViewById(R.id.date_forecast);
                        date.setText(forecastBase.getDate());
                        TextView cond = view.findViewById(R.id.cond_txt);
                        cond.setText(forecastBase.getCond_txt_d());
                        TextView tmp_min = view.findViewById(R.id.tmp_min);
                        tmp_min.setText(forecastBase.getTmp_min());
                        TextView tmp_max = view.findViewById(R.id.tmp_max);
                        tmp_max.setText(forecastBase.getTmp_max());
                        TextView wind = view.findViewById(R.id.wind);
                        wind.setText(forecastBase.getWind_dir());
                        linearLayout.addView(view);
                    }

                }else {
                    Toast.makeText(WeatherActivity.this,"Fail to get forecast message.",Toast.LENGTH_SHORT).show();
                    String status = forecast.getStatus();
                    Code code = Code.toEnum(status);
                    Log.i("Fail","failed code:"+code);
                }

            }
        });
    }

    public void setTitleView(String condType){
        if (titleView == null){
            return;
        }
        if (condType.equals("晴")){
            Glide.with(WeatherActivity.this).load(R.drawable.sun_live).into(titleView);
        }else if (condType.equals("小雨")){
            Glide.with(WeatherActivity.this).load(R.drawable.smallrain_live).into(titleView);
        }else if (condType.equals("大雨")){
            Glide.with(WeatherActivity.this).load(R.drawable.rain).into(titleView);
        }else {
            Glide.with(WeatherActivity.this).load(R.drawable.cloud).into(titleView);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :
                //因为前面在用户选择过城市之后，做了缓存处理，再次进入程序时如果判断到缓存存在就直接跳到天气界面
                //所以当我们在天气界面要返回到选择界面的时候，它还是判定我们已经缓存了，还是会直接跳回来，所以在这里我们要
                //先将缓存的城市名和天气id删除掉
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.remove("city_name");
                editor.remove("weather_id");
                editor.apply();
                Intent intent = new Intent(WeatherActivity.this,ChooseArea.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    //融合状态栏的直接代码动态判断
    private void fitsSystemWindows(){
        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public void setScrollViewBackground(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
        String content = preferences.getString("pic_content",null);
        if (content != null){
            Glide.with(WeatherActivity.this).load(content).into(backgroundView);
        }else {
            requestPic();
        }
    }

    public void requestPic(){
        String picUrl = "http://guolin.tech/api/bing_pic";
        OkHttpUtil.sendRequest(picUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String content = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("pic_content",content);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(content).into(backgroundView);
                    }
                });

            }
        });
    }

    public void setListenerForSwipe(){
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setWeatherViewInvisible();
                updateWeatherInfo();
            }
        });
    }

    public void updateWeatherInfo(){
        queryWeatherNow(weatherId);
        queryWeatherAir(weatherId);
        queryWeatherLifestyle(weatherId);
        queryWeatherForecast(weatherId);
        requestPic();
        swipeRefreshLayout.setRefreshing(false);
        setWeatherViewVisible();
        Toast.makeText(WeatherActivity.this,"刷新成功",Toast.LENGTH_SHORT).show();

    }

    public void setWeatherViewInvisible(){
        tmp.setVisibility(View.INVISIBLE);
        weatherNow.setVisibility(View.INVISIBLE);
        wind.setVisibility(View.INVISIBLE);
        maxTmp.setVisibility(View.INVISIBLE);

        ql.setVisibility(View.INVISIBLE);
        aqi.setVisibility(View.INVISIBLE);
        pm.setVisibility(View.INVISIBLE);

        comfit.setVisibility(View.INVISIBLE);
        wear.setVisibility(View.INVISIBLE);

    }


    public void setWeatherViewVisible(){
        tmp.setVisibility(View.VISIBLE);
        weatherNow.setVisibility(View.VISIBLE);
        wind.setVisibility(View.VISIBLE);
        maxTmp.setVisibility(View.VISIBLE);

        ql.setVisibility(View.VISIBLE);
        aqi.setVisibility(View.VISIBLE);
        pm.setVisibility(View.VISIBLE);

        comfit.setVisibility(View.VISIBLE);
        wear.setVisibility(View.VISIBLE);
    }
}
