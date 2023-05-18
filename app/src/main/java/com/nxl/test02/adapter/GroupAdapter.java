package com.nxl.test02.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nxl.test02.R;

import java.util.List;

import no.nordicsemi.android.mesh.Group;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<Group> groups;
    private OnItemClickListener listener;

    public List<Group> getGroupBeans() {
        return groups;
    }

    public void setGroupBeans(List<Group> groupBeans) {
        this.groups = groupBeans;
    }

    public GroupAdapter(List<Group> groupBeans) {
        this.groups = groupBeans;
    }

    public GroupAdapter() {
    }

    @NonNull
    @Override
    public GroupAdapter.GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupAdapter.GroupViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_list,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        holder.groupName.setText(groups.get(position).getName());
        if(listener!=null){
            holder.turnOnButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(view,holder.getAdapterPosition());
                }
            });
            holder.groupName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(view,holder.getAdapterPosition());
                }
            });
            holder.turnOffButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(view,holder.getAdapterPosition());
                }
            });
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(view,holder.getAdapterPosition());
                }
            });
            holder.scheduleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(view,holder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {return groups == null ? 0 :groups.size(); }

    public class GroupViewHolder extends RecyclerView.ViewHolder {
        private TextView groupName;
        private Button turnOnButton,turnOffButton,scheduleButton,deleteButton;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);

            groupName = itemView.findViewById(R.id.group_name);
            turnOnButton = itemView.findViewById(R.id.turn_on_by_group);
            turnOffButton = itemView.findViewById(R.id.turn_off_by_group);
            scheduleButton = itemView.findViewById(R.id.set_scheduled_task);
            deleteButton = itemView.findViewById(R.id.delete_group);

        }
    }

    //自定义一个回调接口来实现Click事件
    public interface OnItemClickListener  {
        void onItemClick(View v,int position);
    }

    //定义方法并暴露给外面的调用者
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener  = listener;
    }
}
