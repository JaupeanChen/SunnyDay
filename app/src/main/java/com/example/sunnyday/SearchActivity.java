package com.example.sunnyday;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sunnyday.Entity.County;
import com.example.sunnyday.Entity.MyAdapter;
import com.example.sunnyday.Util.OkHttpUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SearchActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    private static final String TAG = "InputEditText";
    private MyAdapter adapter;
    private List<String> list;
    private List<String> weatherList;

    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        EditText inputEditText = findViewById(R.id.search_input);
        searchButton = findViewById(R.id.search_button);
//        searchButton.setSelected(false);
        recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        Button back = findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchActivity.this.finish();
            }
        });
        list = new ArrayList<>();
//        list.add("候选1");
//        list.add("候选2");
//        list.add("候选3");
        weatherList = new ArrayList<>();
//        weatherList.add("id1");
//        weatherList.add("id2");
//        weatherList.add("id3");

        adapter = new MyAdapter(SearchActivity.this, list, weatherList);
        recyclerView.setAdapter(adapter);

        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChanged: " + s + "start" + start + "after" + "count"+count);

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: " + s + "start" + start + "before" + before);
//                if (s != null){
//                    searchButton.setSelected(true);
//                }else {
//                    searchButton.setSelected(false);
//                }
                if (s != null){
                    searchButton.setTextColor(Color.parseColor("#008577"));
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i(TAG, "afterTextChanged: " + s.toString());
//                if (s == null || s.toString().length() < 2 || s.toString().equals(SearchActivity.this.lastSearchLocation)) {
//                    return;
//                }

                if (s.toString().equals("")){
                    searchButton.setTextColor(Color.parseColor("#C0C0C0"));
                }


                String url = "https://search.heweather.net/find?location=" + s + "&key=28b8e2cacacb49f590bdfe10d44c5231&number=10";
                OkHttpUtil.sendRequest(url, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String backString = response.body().string();
                        list.clear();
                        weatherList.clear();
                        try {
                            JSONObject rootObject = new JSONObject(backString);
                            JSONArray array = rootObject.getJSONArray("HeWeather6").getJSONObject(0).getJSONArray("basic");
                            for (int i= 0 ; i<array.length();i++){
                                JSONObject jsonObject = array.getJSONObject(i);
                                list.add(jsonObject.getString("location"));
                                weatherList.add(jsonObject.getString("cid"));
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });

                        }catch (JSONException e){
                            e.printStackTrace();
                        }


                    }
                });

            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityName = list.get(0);
                String weatherId = weatherList.get(0);
                Intent intent = new Intent(SearchActivity.this, WeatherActivity.class);
                intent.putExtra("city_name",cityName);
                intent.putExtra("weather_id",weatherId);
                startActivity(intent);
                SearchActivity.this.finish();
            }
        });



//        inputEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                Editable line = inputEditText.getEditableText();
//                if (line != null){
//                    String cityName = line.toString();
//                    getCityTitleName(cityName);
//                }
//                return true;
//            }
//        });

    }

//    public void getCityTitleName(String cityName){
//        HeWeather.getWeather(SearchActivity.this, cityName, new HeWeather.OnResultWeatherDataListBeansListener() {
//            @Override
//            public void onError(Throwable throwable) {
//                Toast.makeText(SearchActivity.this,"请检查输入",Toast.LENGTH_SHORT).show();
//
//            }
//
//            @Override
//            public void onSuccess(Weather weather) {
//                Basic basic = weather.getBasic();
//                String cityTitle = basic.getParent_city();
//                String weatherId = basic.getCid();
//
//                //这里需要处理的一个情况还是WeatherActivity的本地缓存，要先remove掉。但是这样一来，下次重新进入的时候就需要重新选择
//                //所以可能对于城市信息的缓存会不会放在WeatherActivity里面来做会更好？
//                SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(SearchActivity.this).edit();
////                preferences.remove("city_name");
////                preferences.remove("weather_id");
//                preferences.putString("city_name",cityTitle);
//                preferences.putString("weather_id",weatherId);
//                preferences.apply();
//
//
//                Intent intent = new Intent();
//                intent.putExtra("city_name",cityTitle);
//                intent.putExtra("weather_id",weatherId);
//                setResult(RESULT_OK,intent);
//                finish();
//            }
//        });
//
//    }
}
