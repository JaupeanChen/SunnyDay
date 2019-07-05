package com.example.sunnyday.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//把收藏城市的信息存储到本地，LitePal实在是封装得太好了，所以还是用原生的SQLite来实现
public class MyDatabaseHelper extends SQLiteOpenHelper {
    private static final String CREATE_CITY_TABLE = "create table CityList ("
            + "id integer primary key AUTOINCREMENT,"
            + "city_name text,"
            + "weather_id text )";

    public MyDatabaseHelper(Context context,String name, SQLiteDatabase.CursorFactory cursorFactory,int version){
        super(context, name,cursorFactory,version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CITY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
