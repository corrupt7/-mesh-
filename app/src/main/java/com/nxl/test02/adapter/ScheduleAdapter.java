package com.nxl.test02.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nxl.test02.meshTools.MeshTools;
import com.nxl.test02.R;
import com.nxl.test02.alarm.AlarmReceiver;
import com.nxl.test02.room.ScheduleTask;

import java.util.List;

import no.nordicsemi.android.mesh.Group;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private static final String TAG = "ScheduleAdapter";
    private List<ScheduleTask> data;
    private Context context;
    private OnItemClickListener mOnItemClickListener;//声明自定义的接口
    private MeshTools meshTools;

    public ScheduleAdapter(List<ScheduleTask> data, Context context, MeshTools meshTools) {
        this.data = data;
        this.context = context;
        this.meshTools = meshTools;
    }

    public List<ScheduleTask> getData() {
        return data;
    }

    public void setData(List<ScheduleTask> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ScheduleViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule_list,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        holder.scheduleNameTextView.setText(data.get(position).getTaskName());

        if(AlarmReceiver.OPEN_DEVICE_ACTION.equals(data.get(position).getAction())){
            holder.scheduleActionTextView.setText("定时开启");
        }
        else {
            Log.d(TAG, "onBindViewHolder: "+data.get(position).getAction());
            holder.scheduleActionTextView.setText("定时关闭");
        }

        int hour = data.get(position).getHour();
        int minute = data.get(position).getMinute();
        String s = (hour<10?"0"+hour:hour) + ":" + (minute<10?"0"+minute:minute);
        holder.scheduleTimeTextView.setText(s);

        int groupId = data.get(position).getGroupId();
        Group group = meshTools.getMeshNetworkLiveData().getMeshNetwork().getGroup(groupId);
        if(group==null){
            holder.scheduleGroupTextView.setText(data.get(position).getGroupId());
        }else {
            holder.scheduleGroupTextView.setText(group.getName());
        }

        if (mOnItemClickListener!=null){
            holder.deleteScheduleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick(view,holder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 :data.size();
    }

    public class ScheduleViewHolder extends RecyclerView.ViewHolder {
        private TextView scheduleNameTextView,scheduleGroupTextView,scheduleActionTextView,scheduleTimeTextView;
        private Button deleteScheduleButton;
        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            scheduleNameTextView = itemView.findViewById(R.id.schedule_name);
            scheduleGroupTextView = itemView.findViewById(R.id.schedule_group);
            scheduleActionTextView = itemView.findViewById(R.id.schedule_action);
            scheduleTimeTextView = itemView.findViewById(R.id.schedule_time);
            deleteScheduleButton = itemView.findViewById(R.id.delete_schedule);
        }
    }

    public interface OnItemClickListener  {
        void onItemClick(View v,int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener  = listener;
    }

}
