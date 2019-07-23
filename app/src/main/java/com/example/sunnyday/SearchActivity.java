package com.example.sunnyday;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import interfaces.heweather.com.interfacesmodule.bean.basic.Basic;
import interfaces.heweather.com.interfacesmodule.bean.search.Search;
import interfaces.heweather.com.interfacesmodule.bean.weather.Weather;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        TextInputEditText inputEditText = findViewById(R.id.search_input);
//        inputEditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (s != null){
//                    String line = s.toString();
//                    getCityTitleName(line);
//                }
//
//            }
//        });
        inputEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
//                    Editable line = inputEditText.getText();
//                    if (line != null){
//                        String cityName = line.toString();
//                        getCityTitleName(cityName);
//                    }
//
//                }
                Editable line = inputEditText.getEditableText();
                if (line != null){
                    String cityName = line.toString();
                    getCityTitleName(cityName);
                }
                return true;
            }
        });

    }

    public void getCityTitleName(String cityName){
        HeWeather.getWeather(SearchActivity.this, cityName, new HeWeather.OnResultWeatherDataListBeansListener() {
            @Override
            public void onError(Throwable throwable) {
                Toast.makeText(SearchActivity.this,"请检查输入",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onSuccess(Weather weather) {
                Basic basic = weather.getBasic();
                String cityTitle = basic.getParent_city();
                String weatherId = basic.getCid();

                //这里需要处理的一个情况还是WeatherActivity的本地缓存，要先remove掉。但是这样一来，下次重新进入的时候就需要重新选择
                //所以可能对于城市信息的缓存会不会放在WeatherActivity里面来做会更好？
                SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(SearchActivity.this).edit();
//                preferences.remove("city_name");
//                preferences.remove("weather_id");
                preferences.putString("city_name",cityTitle);
                preferences.putString("weather_id",weatherId);
                preferences.apply();


                Intent intent = new Intent();
                intent.putExtra("city_name",cityTitle);
                intent.putExtra("weather_id",weatherId);
                startActivity(intent);
                setResult(RESULT_OK,intent);
                finish();
            }
        });

    }
}
