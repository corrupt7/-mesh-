package com.nxl.test02.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nxl.test02.bean.ExtendedBluetoothDevice;
import com.nxl.test02.R;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private List<ExtendedBluetoothDevice> data;
    private OnItemClickListener mOnItemClickListener;//声明自定义的接口

    public DeviceAdapter(List<ExtendedBluetoothDevice> data, Context context) {
        this.data = data;
    }

    public void setData(List<ExtendedBluetoothDevice> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DeviceViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device_list,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        holder.addressTextView.setText(data.get(position).getAddress());
        holder.nameTextView.setText(data.get(position).getName());
        if(mOnItemClickListener!=null){

            holder.connectButton.setOnClickListener(new View.OnClickListener() {
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



    public class DeviceViewHolder extends RecyclerView.ViewHolder {
        private TextView addressTextView;
        private TextView nameTextView;
        private Button connectButton;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            addressTextView = itemView.findViewById(R.id.device_address);
            nameTextView = itemView.findViewById(R.id.device_name);
            connectButton = itemView.findViewById(R.id.connect_device);

        }
    }

    //自定义一个回调接口来实现Click事件
    public interface OnItemClickListener  {
        void onItemClick(View v,int position);
    }

    //定义方法并暴露给外面的调用者
    public void setOnItemClickListener(OnItemClickListener  listener) {
        this.mOnItemClickListener  = listener;
    }
}
