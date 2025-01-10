package com.example.map;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyViewHolder extends RecyclerView.ViewHolder {
    TextView title, subtitle,price,date;
    Button Mapbutton;
    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.textViewAddress);
        subtitle = itemView.findViewById(R.id.textViewSubject);
        price = itemView.findViewById(R.id.textViewPrice);
        date = itemView.findViewById(R.id.textViewDate);
        Mapbutton = itemView.findViewById((R.id.MapButton));
    }
}

class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private List<MyDataModel> dataList;

    public MyAdapter(List<MyDataModel> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MyDataModel data = dataList.get(position);
        holder.title.setText("地址:"+"\n"+data.getAddress());
        holder.subtitle.setText("交易標的:"+data.getSubject());
        holder.price.setText("坪數單價:"+data.getPrice());
        holder.date.setText("交易日期:"+data.getDate());

        holder.Mapbutton.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(),DetailMapActivity.class);
            intent.putExtra("address",data.getAddress());
            intent.putExtra("subject",data.getSubject());
            intent.putExtra("price",data.getPrice());
            intent.putExtra("date",data.getDate());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}