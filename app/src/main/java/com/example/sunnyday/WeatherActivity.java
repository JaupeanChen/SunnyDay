package com.example.sunnyday;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sunnyday.Util.OkHttpUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.Code;
import interfaces.heweather.com.interfacesmodule.bean.air.now.AirNow;
import interfaces.heweather.com.interfacesmodule.bean.air.now.AirNowCity;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.Forecast;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.ForecastBase;
import interfaces.heweather.com.interfacesmodule.bean.weather.hourly.Hourly;
import interfaces.heweather.com.interfacesmodule.bean.weather.hourly.HourlyBase;
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
    private TextView date, tmp,weatherNow,wind,maxTmp;
    private LinearLayout linearLayout;

    private TextView comfit,wear,protect,wash;
    private TextView ql,aqi,pm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        HeConfig.init("HE1906262053031373","b221f29802ca44d5a1ada8d11a65e8d2");
        HeConfig.switchToFreeServerNode();
        date = findViewById(R.id.date);
        tmp = findViewById(R.id.temp);
        weatherNow =findViewById(R.id.weather_now);
        wind = findViewById(R.id.wind);
        maxTmp =findViewById(R.id.max_tmp);
        linearLayout =findViewById(R.id.forecast);

        comfit = findViewById(R.id.comfit);
        wear = findViewById(R.id.wear);
        protect = findViewById(R.id.protect);
        wash = findViewById(R.id.washing_car);

        ql = findViewById(R.id.ql);
        aqi = findViewById(R.id.aqi);
        pm = findViewById(R.id.pm25);

        String weatherId = getIntent().getStringExtra("weather_id");
        queryWeatherNow(weatherId);
        queryWeatherAir(weatherId);
        queryWeatherLifestyle(weatherId);
        queryWeatherForecast(weatherId);


    }


    public void queryWeatherNow(String weatherId){
        HeWeather.getWeatherNow(WeatherActivity.this, weatherId, new HeWeather.OnResultWeatherNowBeanListener() {
            @Override
            public void onError(Throwable throwable) {

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
                    weatherNow.setText(nowBase.getCond_txt());
                    wind.setText(nowBase.getWind_dir());
                    maxTmp.setText("max:" + nowBase.getFl()+"°C");
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

}
