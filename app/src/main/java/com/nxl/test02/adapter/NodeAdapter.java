package com.nxl.test02.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.nxl.test02.R;

import java.util.List;

import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.utils.MeshAddress;

public class NodeAdapter extends RecyclerView.Adapter<NodeAdapter.NodeViewHolder> {

    List<ProvisionedMeshNode> data;
    private OnItemClickListener mOnItemClickListener;
    private LiveData<List<String>> state;

    public NodeAdapter(List<ProvisionedMeshNode> data,LiveData<List<String>> state) {
        this.data = data;
        this.state = state;
    }

    public void setData(List<ProvisionedMeshNode> data,LiveData<List<String>> state) {
        this.data = data;
        this.state =state;
    }

    @NonNull
    @Override
    public NodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NodeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_node,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull NodeViewHolder holder, int position) {
        holder.addressTextView.setText(MeshAddress.formatAddress(data.get(position).getUnicastAddress(), true));
        if (data.get(position).getUnicastAddress()==1){
            holder.nameTextView.setText("本机设备");
            holder.statusTextView.setText("本机设备");
            holder.openNode.setVisibility(View.INVISIBLE);
            holder.closeNode.setVisibility(View.INVISIBLE);
            holder.getStatus.setVisibility(View.INVISIBLE);
            holder.cancelNetwork.setVisibility(View.INVISIBLE);
        }
        else{
            holder.nameTextView.setText(data.get(position).getNodeName());
            holder.statusTextView.setText(state.getValue().get(position));
            holder.statusTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick(view,holder.getAdapterPosition(),holder.statusTextView);
                }
            });
            holder.openNode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick(view,holder.getAdapterPosition(),holder.statusTextView);
                }
            });
            holder.closeNode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick(view,holder.getAdapterPosition(),holder.statusTextView);
                }
            });
            holder.getStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick(view,holder.getAdapterPosition(),holder.statusTextView);
                }
            });
            holder.cancelNetwork.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick(view,holder.getAdapterPosition(),holder.statusTextView);
                }
            });
            holder.nameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick(view,holder.getAdapterPosition(),holder.statusTextView);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return data==null? 0 : data.size();
    }

    public class NodeViewHolder extends RecyclerView.ViewHolder{

        private TextView addressTextView,nameTextView,statusTextView;
        private Button openNode,closeNode,getStatus,cancelNetwork;

        public NodeViewHolder(@NonNull View itemView) {
            super(itemView);
            addressTextView = itemView.findViewById(R.id.node_address);
            nameTextView = itemView.findViewById(R.id.node_name);
            statusTextView = itemView.findViewById(R.id.node_status);
            openNode = itemView.findViewById(R.id.node_open);
            closeNode = itemView.findViewById(R.id.node_close);
            getStatus = itemView.findViewById(R.id.set_group);
            cancelNetwork = itemView.findViewById(R.id.cancel_network);
        }
    }


    //自定义一个回调接口来实现Click事件
    public interface OnItemClickListener  {
        void onItemClick(View v,int position,TextView textView);
    }

    //定义方法并暴露给外面的调用者
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener  = listener;
    }
}
