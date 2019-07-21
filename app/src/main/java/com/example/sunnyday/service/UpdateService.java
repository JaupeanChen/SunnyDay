package com.example.sunnyday.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

import interfaces.heweather.com.interfacesmodule.bean.weather.Weather;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class UpdateService extends Service {
    public UpdateService() {
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String weatherId = intent.getStringExtra("weather_id");
        queryWeatherInfo(weatherId);
        queryPic();
        int hours = 6*60*60*1000;
        long triggerTime = SystemClock.elapsedRealtime() + hours;
        //每次唤醒之后的触发时间，这里设为6小时
        Intent alarmIntent = new Intent(this,UpdateService.class);
        //自启
        PendingIntent pi = PendingIntent.getService(UpdateService.this,1,alarmIntent,0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pi);
        //唤醒之后先把上次设定的取消，再重新进行定时设定
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerTime,pi);

        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public void queryWeatherInfo(String weatherId){
        HeWeather.getWeather(UpdateService.this, weatherId, new HeWeather.OnResultWeatherDataListBeansListener() {
            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onSuccess(Weather weather) {
                String tmp = weather.getNow().getTmp();

            }
        });

    }

    public void queryPic(){

    }
}
