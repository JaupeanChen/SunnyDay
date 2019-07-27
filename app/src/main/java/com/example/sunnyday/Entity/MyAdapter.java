package com.example.sunnyday.Entity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sunnyday.R;
import com.example.sunnyday.SearchActivity;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private Context context;
    private List<String> list;
    private List<String> weatherList;

    public MyAdapter(Context context,List<String> list,List<String> weatherList){
        this.list = list;
        this.weatherList = weatherList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_item,viewGroup,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        String name = list.get(i);
        String weatherId = weatherList.get(i);
        viewHolder.textView.setText(name + " " + weatherId);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"选择了(itemView)"+ name,Toast.LENGTH_SHORT).show();
                SearchActivity activity = (SearchActivity) MyAdapter.this.context;
                Intent intent = new Intent();
                intent.putExtra("city_name",name);
                intent.putExtra("weather_id",weatherId);
                activity.setResult(Activity.RESULT_OK,intent);
                activity.finish();

            }
        });
        viewHolder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"选择了"+ name,Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

     static class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView textView;
        public  ViewHolder(View view){
            super(view);
            itemView = view;
            textView = view.findViewById(R.id.item);

        }
    }


}
