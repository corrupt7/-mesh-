package com.nxl.test02.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nxl.test02.R;
import com.nxl.test02.bean.DeviceBean;

import java.util.List;

import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;

public class DeviceInGroupAdapter extends RecyclerView.Adapter<DeviceInGroupAdapter.DeviceInGroupViewHolder> {

    private List<ProvisionedMeshNode> data;
    private OnItemClickListener mOnItemClickListener;

    public List<ProvisionedMeshNode> getData() {
        return data;
    }

    public void setData(List<ProvisionedMeshNode> data) {
        this.data = data;
    }

    public DeviceInGroupAdapter(List<ProvisionedMeshNode> data) {
        this.data = data;
    }


    @NonNull
    @Override
    public DeviceInGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DeviceInGroupViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.itrm_device_in_group,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceInGroupViewHolder holder, int position) {
        holder.deviceName.setText(data.get(position).getNodeName());
        if(mOnItemClickListener!=null){
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick(view,holder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data==null?0:data.size();
    }

    public class DeviceInGroupViewHolder extends RecyclerView.ViewHolder {
        private TextView deviceName;
        private Button deleteButton;
        public DeviceInGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name_in_group);
            deleteButton = itemView.findViewById(R.id.delete_device);
        }
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    /**
     * 监听回调接口
     */
    public interface OnItemClickListener {
        void onItemClick(View view,int position);
    }


}
