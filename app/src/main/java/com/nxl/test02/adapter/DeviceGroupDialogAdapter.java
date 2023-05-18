package com.nxl.test02.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nxl.test02.R;

import java.util.List;

import no.nordicsemi.android.mesh.Group;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;

public class DeviceGroupDialogAdapter extends RecyclerView.Adapter<DeviceGroupDialogAdapter.DeviceGroupDialogViewHolder> {

    private List<Group> data;
    private OnItemClickListener mOnItemClickListener;
    private DeviceGroupDialogViewHolder mHolder;

    public DeviceGroupDialogAdapter(List<Group> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public DeviceGroupDialogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DeviceGroupDialogViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device_group_list,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceGroupDialogViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.groupName.setText(data.get(position).getName());

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mHolder==null){
                    mHolder = holder;
                }else{
                    if (!mHolder.equals(holder)){
                        mHolder.checkBox.setChecked(false);
                        mHolder = holder;
                    }
                }
                boolean checked = mHolder.checkBox.isChecked();
                mOnItemClickListener.onItemClick(position,checked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data==null ? 0 : data.size();
    }

    public class DeviceGroupDialogViewHolder extends RecyclerView.ViewHolder {

        private TextView groupName;
        private CheckBox checkBox;

        public DeviceGroupDialogViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.device_group_name);
            checkBox = itemView.findViewById(R.id.group_checkbox);
        }
    }

    /**
     * @param onItemClickListener 监听设置
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    /**
     * 监听回调接口
     */
    public interface OnItemClickListener {
        void onItemClick(int position,boolean isChecked);
    }

}
