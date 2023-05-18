package com.nxl.test02.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nxl.test02.R;
import com.nxl.test02.room.MonitorLog;
import com.nxl.test02.room.ScheduleTask;

import java.util.List;

public class MonitorLogAdapter extends RecyclerView.Adapter<MonitorLogAdapter.MonitorLogViewHolder> {
    private String TAG = "MonitorLogAdapter";

    private List<MonitorLog> data;
    private OnItemClickListener listener;

    public MonitorLogAdapter(List<MonitorLog> data) {
        this.data = data;
    }

    public List<MonitorLog> getData() {
        return data;
    }

    public void setData(List<MonitorLog> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public MonitorLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MonitorLogViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_monitor_log,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MonitorLogViewHolder holder, int position) {
        holder.monitorName.setText(data.get(position).getTaskName());
        holder.monitorTime.setText(data.get(position).getTime());
        String isSucceed = data.get(position).getIsSucceed();
        if(isSucceed.equals("true")){
            holder.monitorResult.setText("成功");
            holder.monitorResult.setTextColor(Color.GREEN);
        }
        else {
            holder.monitorResult.setText("失败");
            holder.monitorResult.setTextColor(Color.RED);
        }
        holder.monitorMessage.setText(data.get(position).getMonitorLogMessage());
        if (listener!=null){
            holder.monitorMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(view,holder.getAdapterPosition());
                }
            });
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(view, holder.getAdapterPosition());
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return data.isEmpty()?0:data.size();
    }

    public class MonitorLogViewHolder extends RecyclerView.ViewHolder {
        private TextView monitorName,monitorTime,monitorResult,monitorMessage;
        private Button deleteButton;
        public MonitorLogViewHolder(@NonNull View itemView) {
            super(itemView);

            deleteButton = itemView.findViewById(R.id.delete_monitor_log);
            monitorName = itemView.findViewById(R.id.monitor_name);
            monitorTime = itemView.findViewById(R.id.monitor_time);
            monitorResult = itemView.findViewById(R.id.monitor_result);
            monitorMessage = itemView.findViewById(R.id.monitor_message);

        }
    }


    public interface OnItemClickListener  {
        void onItemClick(View v,int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener  = listener;
    }
}
