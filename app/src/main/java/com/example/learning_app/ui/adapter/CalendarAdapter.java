package com.example.learning_app.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.learning_app.R;
import java.util.List;
import java.util.Map;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private List<String> days;
    private Map<String, Integer> statusMap;

    public CalendarAdapter(List<String> days, Map<String, Integer> statusMap) {
        this.days = days;
        this.statusMap = statusMap;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        String fullDate = days.get(position);

        if (fullDate == null || fullDate.isEmpty()) {
            holder.tvDay.setText("");
            holder.tvDay.setVisibility(View.INVISIBLE);
            holder.imgBackground.setVisibility(View.INVISIBLE);
            return;
        }

        holder.tvDay.setVisibility(View.VISIBLE);
        holder.imgBackground.setVisibility(View.INVISIBLE);
        holder.tvDay.setTextColor(Color.parseColor("#B0BEC5"));

        try {
            String dayNumber = fullDate.substring(fullDate.lastIndexOf("-") + 1);
            holder.tvDay.setText(dayNumber);
        } catch (Exception e) {
            holder.tvDay.setText("?");
        }

        if (statusMap != null && statusMap.containsKey(fullDate)) {
            Integer status = statusMap.get(fullDate);
            if (status != null) {
                if (status == 1) {
                    holder.imgBackground.setVisibility(View.VISIBLE);
                    holder.imgBackground.setImageResource(R.drawable.bg_orange_circle);
                    holder.tvDay.setTextColor(Color.WHITE);
                } else if (status == 2) {
                    holder.imgBackground.setVisibility(View.VISIBLE);
                    holder.imgBackground.setImageResource(R.drawable.ic_freeze_blue);
                    holder.tvDay.setTextColor(Color.WHITE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;
        ImageView imgBackground;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            imgBackground = itemView.findViewById(R.id.imgBackground);
        }
    }
}



