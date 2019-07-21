package com.example.sunnyday;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.sunnyday.Entity.WeatherInfoWrapper;
import com.example.sunnyday.Util.OkHttpUtil;
import com.example.sunnyday.db.MyDatabaseHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

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
    public String cityName;
    public boolean isSaved;

    private Button savedButton;
    private TextView date, tmp,weatherNow,wind,maxTmp;
    private LinearLayout linearLayout;

    private TextView comfit,wear,protect,wash;
    private TextView ql,aqi,pm;

    private Toolbar toolbar;
    private ImageView titleView;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView backgroundView;

    private SwipeRefreshLayout swipeRefreshLayout;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
//    private FloatingActionButton floatingButton;


    public MyDatabaseHelper dbHelper;
    public SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        HeConfig.init("HE1906262053031373","b221f29802ca44d5a1ada8d11a65e8d2");
        HeConfig.switchToFreeServerNode();
        fitsSystemWindows();
        //通过EventBus来实现后台自动更新
        EventBus.getDefault().register(WeatherActivity.this);

        dbHelper = new MyDatabaseHelper(WeatherActivity.this,"CityStore",null,1);
        db = dbHelper.getWritableDatabase();

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

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        setNavigationViewListener();
//        floatingButton = findViewById(R.id.floating_button);


        titleView =findViewById(R.id.image);
        toolbar = findViewById(R.id.toolbar_weather);
        collapsingToolbarLayout = findViewById(R.id.collapseView);
        backgroundView = findViewById(R.id.background_view);
        setSupportActionBar(toolbar);
        cityName = getIntent().getStringExtra("city_name");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null ){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        collapsingToolbarLayout.setTitle(cityName);
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);

        weatherId = getIntent().getStringExtra("weather_id");
        queryWeatherNow(weatherId);
        queryWeatherAir(weatherId);
        queryWeatherLifestyle(weatherId);
        queryWeatherForecast(weatherId);
        setScrollViewBackground();

        savedButton = findViewById(R.id.saved_button);
        //先判断该城市是否收藏过，如果是那Button就显示已收藏的图案
        String savedId = queryFromDatabase(cityName);
        if (weatherId.equals(savedId)) {
            savedButton.setActivated(true);
            isSaved = true;
        }
        setSavedButtonListener();


//        if (weatherId.equals(savedId)){
//            floatingButton.setPressed(true);
//            isSaved = true;
//        }
//        setFloatingButtonListener();

        //每次进入天气界面的时候，都去数据库里读取收藏城市数据，然后动态地显示到NavigationView上，这样就能保证显示的都是最新收藏的城市了
        loadSavedCityForNav();

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :
                //因为前面在用户选择过城市之后，做了缓存处理，再次进入程序时如果判断到缓存存在就直接跳到天气界面，
                //所以当我们在天气界面要返回到选择界面的时候，它还是判定我们已经缓存了，还是会直接跳回来，所以在这里我们要
                //先将缓存的城市名和天气id删除掉
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.remove("city_name");
                editor.remove("weather_id");
                editor.apply();
                Intent intent = new Intent(WeatherActivity.this,ChooseArea.class);
                startActivity(intent);
                break;

            case R.id.right_home:
                drawerLayout.openDrawer(GravityCompat.END);
                break;

            case R.id.search:
                Intent searchIntent = new Intent(WeatherActivity.this,SearchActivity.class);
                startActivity(searchIntent);
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

    public void setNavigationViewListener(){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                //拿到当前的城市名，再到数据库中查询得到天气id，有了这两个信息之后就可以跟选择城市界面跳转到天气界面一样了
                //这也是前面在判定城市是否已经存在数据库的时候封装的方法为什么要让它返回天气id然后再判断的原因，因为这边还能复用

                cityName = (String) menuItem.getTitle();  //为什么这里用的是局部变量，刷新的时候却不会被原来的全局变量替换掉？
                weatherId = queryFromDatabase(cityName);  //后面手动再刷新就会发现数据是上个城市的，所以这里还是记得要用全局的name和id

                drawerLayout.closeDrawer(GravityCompat.END);
                queryAndShowWeather(cityName,weatherId);
                return true;
            }
        });
    }

    public void setSavedButtonListener(){
        savedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSaved){
                    ContentValues values = new ContentValues();
                    values.put("city_name",cityName);
                    values.put("weather_id",weatherId);
                    db.insert("CityList",null,values);
                    loadSavedCityForNav();
                    savedButton.setActivated(true);
                    isSaved = true;
                    Toast.makeText(WeatherActivity.this,"收藏城市成功！",Toast.LENGTH_SHORT).show();
                }else {
                    db.delete("CityList","weather_id=?",new String[]{ weatherId });
                    loadSavedCityForNav();
                    savedButton.setActivated(false);
                    isSaved = false;
                    Toast.makeText(WeatherActivity.this,"取消收藏！",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

//    public void setFloatingButtonListener(){
//        floatingButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                //添加之前先判断数据库里是否已经有添加该城市，如果没有再进行添加,不然的话就会出现一个城市添加了多次的情况
//                //这里我们拿城市名去数据库查找，让它返回天气id，如果这个id不为null，那就说明已经添加过了，否则再进行添加
//                //这里的这个查询方法为什么要返回天气id呢，因为后面再NavigationView进行监听跳转到天气显示界面的时候需要传递城市名和id
////                String id = queryFromDatabase(cityName);
////                if (id != null){
////                    Toast.makeText(WeatherActivity.this,"该城市已经收藏过了哦！",Toast.LENGTH_SHORT).show();
////                }else {
////                    ContentValues values = new ContentValues();
////                    values.put("city_name",cityName);
////                    values.put("weather_id",weatherId);
////                    db.insert("CityList",null,values);
////                    loadSavedCityForNav();
////                    Toast.makeText(WeatherActivity.this,"收藏城市成功！",Toast.LENGTH_SHORT).show();
////                }
//
//                //用另一种方法，根据FAB的显示图案来进行收藏/取消的逻辑处理
//                if (!isSaved){
//                    ContentValues values = new ContentValues();
//                    values.put("city_name",cityName);
//                    values.put("weather_id",weatherId);
//                    db.insert("CityList",null,values);
//                    loadSavedCityForNav();
//                    floatingButton.setPressed(true);
//                    isSaved = true;
//                    Toast.makeText(WeatherActivity.this,"收藏城市成功！",Toast.LENGTH_SHORT).show();
//                }else {
//                    db.delete("CityList","weather_id=?",new String[]{ weatherId });
//                    loadSavedCityForNav();
//                    floatingButton.setPressed(false);
//                    isSaved = false;
//                    Toast.makeText(WeatherActivity.this,"取消收藏！",Toast.LENGTH_SHORT).show();
//                }
//
//
//            }
//        });
//    }

    public String queryFromDatabase(String cityName){
        String id = null ;
        Cursor cursor = db.query("CityList",new String[]{"weather_id"},"city_name=?",new String[]{ cityName },null,null,null);
        if (cursor.getCount() == 0){
            return null;
        }
        if (cursor.moveToFirst()){    //这里一定要先对cursor进行判断是否在第一个位置，直接取id会抛出数组下标越界异常
            id = cursor.getString(cursor.getColumnIndex("weather_id"));
        }
//        id = cursor.getString(-1);
        Log.i("weather_id",id);
        return id;
    }

    public void loadSavedCityForNav(){
        Cursor cursor = db.query("CityList",null,null,null,null,null,null);
        Menu menu = navigationView.getMenu();
        menu.clear();
        if (cursor.moveToFirst()){
            do {
                String name = cursor.getString(cursor.getColumnIndex("city_name"));
//                String id = cursor.getString(cursor.getColumnIndex("weather_id"));
                menu.add(name);
            }while (cursor.moveToNext());
        }
        //差点忘记，cursor要记得关
        cursor.close();
    }



    //对所有天气模块和显示封装一下，后面对于NavigationView中收藏城市的监听调用时复用比较方便
    public void queryAndShowWeather(String cityName,String weatherId){
        collapsingToolbarLayout.setTitle(cityName);
        setWeatherViewInvisible();
        swipeRefreshLayout.setRefreshing(true);
        queryWeatherNow(weatherId);
        queryWeatherAir(weatherId);
        queryWeatherLifestyle(weatherId);
        queryWeatherForecast(weatherId);
        requestPic();
        setWeatherViewVisible();
        swipeRefreshLayout.setRefreshing(false);

        //然后，这里要特别注意，因为这时候我们的城市已经更改了，所以也要更新对城市信息缓存，
        //否则如果退掉，重新进入程序，显示的天气界面信息依然会是上次存储的城市
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
        editor.putString("city_name",cityName);
        editor.putString("weather_id",weatherId);
        editor.apply();

    }

    @Subscribe
    public void onEventMessage(WeatherInfoWrapper weatherInfo){


    }

    public void updateInfo(){

    }


}
