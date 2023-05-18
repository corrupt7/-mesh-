package com.nxl.test02.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nxl.test02.R;
import com.nxl.test02.adapter.DeviceGroupDialogAdapter;
import com.nxl.test02.style.RecyclerViewItemDecoration;

import java.util.List;

import no.nordicsemi.android.mesh.Group;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;

public class GroupUpdateDialog extends Dialog implements DeviceGroupDialogAdapter.OnItemClickListener {

    private List<Group> data;
    private Context context;
    private TextView confirmButton,cancelButton;
    private OnSelectorListener onSelectorListener;
    private RecyclerView recyclerView;
    private DeviceGroupDialogAdapter adapter;
    private int selectPosition=-1;

    public GroupUpdateDialog(Context context,List<Group> groups, OnSelectorListener onSelectorListener1){
        super(context);
        this.context = context;
        this.onSelectorListener = onSelectorListener1;
        this.data = groups;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.update_device_group_dialog);
        this.setCanceledOnTouchOutside(false);
        initViews();
    }


    private void initViews(){
        cancelButton = findViewById(R.id.cancelUpdate);
        confirmButton = findViewById(R.id.confirmUpdate);
        recyclerView = findViewById(R.id.select_device_group);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        Drawable drawable = context.getDrawable(R.drawable.divider);
        recyclerView.addItemDecoration(new RecyclerViewItemDecoration(drawable));
        adapter = new DeviceGroupDialogAdapter(data);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelectorListener.cancel();
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelectorListener.getSelectorPosition(selectPosition);
            }
        });

    }

    @Override
    public void onItemClick(int position,boolean isChecked) {
        if (isChecked){
            selectPosition = position;
        }
        else{
            selectPosition = -1;
        }
    }



    /**
     * 确定 和 取消控件的回调接口
     */
    public interface OnSelectorListener {

        void getSelectorPosition(int position);

        void cancel();
    }
}
