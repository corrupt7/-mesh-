package com.nxl.test02;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.nxl.test02.meshTools.MeshTools;
import com.nxl.test02.adapter.DeviceInGroupAdapter;
import com.nxl.test02.style.RecyclerViewItemDecoration;
import com.nxl.test02.tools.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.mesh.Group;
import no.nordicsemi.android.mesh.transport.ConfigModelSubscriptionDelete;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;

public class DeviceInGroupActivity extends AppCompatActivity implements View.OnClickListener,DeviceInGroupAdapter.OnItemClickListener,SwipeRefreshLayout.OnRefreshListener {

    private Intent intent;
    private Group group;
    private List<ProvisionedMeshNode> devices = new ArrayList<>();
    private String groupName;
    private int groupAddress;
    private DeviceInGroupAdapter adapter;
    private Button upgradeGroupNameButton,clearDeviceButton;
    private RecyclerView recyclerView;
    private MeshTools meshTools = MeshTools.getInstance();
    private SwipeRefreshLayout swipeRefreshLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_in_group);
        try {
            initData();
            initLayout();
            setTitle(groupName);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 加载数据
     */
    private void initData(){
        intent = getIntent();
        groupName = intent!=null? intent.getStringExtra("group_name") : null;
        groupAddress = intent!=null? intent.getIntExtra("group_address",-1) : -1;
        if (groupAddress==-1){
            group = meshTools.getMeshNetworkLiveData().getMeshNetwork().getGroups().get(0);
        }
        else {
            group = meshTools.getMeshNetworkLiveData().getMeshNetwork().getGroup(groupAddress);
        }
        List<ProvisionedMeshNode> nodes = meshTools.getMeshNetworkLiveData().getMeshNetwork().getNodes();
        devices = refreshDevices(nodes);
        meshTools.getSelectedModel().observe(this,meshModel -> {
            if (!meshModel.getSubscribedAddresses().contains(groupAddress)){
                List<ProvisionedMeshNode> list = refreshDevices(meshTools.getMeshNetworkLiveData().getMeshNetwork().getNodes());
                adapter.setData(list);
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 加载UI
     */
    private void initLayout(){
        upgradeGroupNameButton = findViewById(R.id.upgrade_group_name);
        clearDeviceButton = findViewById(R.id.clear_device_in_group);
        upgradeGroupNameButton.setOnClickListener(this);
        clearDeviceButton.setOnClickListener(this);

        swipeRefreshLayout = findViewById(R.id.srl_nodes_in_group);
        recyclerView = findViewById(R.id.device_in_group);
        adapter = new DeviceInGroupAdapter(devices);
        adapter.setOnItemClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(DeviceInGroupActivity.this));
        recyclerView.setAdapter(adapter);
        Drawable drawable = getDrawable(R.drawable.divider);
        recyclerView.addItemDecoration(new RecyclerViewItemDecoration(drawable));
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    /**
     * 更新列表里面的数据
     * @param nodes
     * @return
     */
    private List<ProvisionedMeshNode> refreshDevices(List<ProvisionedMeshNode> nodes){
        List<ProvisionedMeshNode> mDevices = new ArrayList<>();
        for(ProvisionedMeshNode node:nodes){
            for(Element element:node.getElements().values()){
                for (MeshModel model:element.getMeshModels().values()){
                    if (model!=null&&model.getModelId()==0x1000){
                        for(int address:model.getSubscribedAddresses()){
                            if (address==groupAddress&&!mDevices.contains(node)){
                                mDevices.add(node);
                            }
                        }
                    }
                }
            }
        }
        return mDevices;
    }


    /**
     * 此界面的点击事件，不包括recyclerview的点击事件
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.upgrade_group_name:
                showUpgradeGroupNameDialog();
                break;
            case R.id.clear_device_in_group:
                for(ProvisionedMeshNode node:devices){
                    deleteNodeInGroup(node);
                }
                break;
            default:
                break;
        }
    }


    /**
     * recyclerview的点击事件
     * @param view
     * @param position
     */
    @Override
    public void onItemClick(View view, int position) {
        switch (view.getId()){

            case R.id.delete_device:
                deleteNodeInGroup(devices.get(position));
                adapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    /**
     * 点击更改分组名字时的弹窗
     */
    private void showUpgradeGroupNameDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(DeviceInGroupActivity.this);
        View view = LayoutInflater.from(DeviceInGroupActivity.this).inflate(R.layout.upgrade_group_name_dialog, null);
        builder.setView(view);
        final AlertDialog dialog = builder.show();
        EditText editText = view.findViewById(R.id.group_name_upgrade_edit_text);
        view.findViewById(R.id.cancel_upgrade_group_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.confirm_upgrade_group_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String groupNameText = editText.getText().toString().trim();
                if(groupNameText.isEmpty()){
                    ToastUtils.show(DeviceInGroupActivity.this,"请输入有效名称!");
                }
                else {
                    group.setName(groupNameText);
                    meshTools.getMeshNetworkLiveData().getMeshNetwork().updateGroup(group);
                    setTitle(groupNameText);
                }
                dialog.dismiss();
            }
        });
    }


    private void deleteNodeInGroup(ProvisionedMeshNode node){
        if (!meshTools.isConnectedToProxy().getValue()){
            ToastUtils.show(this,"请先连接至网络");
            return;
        }
        for(Element element:node.getElements().values()){
            ArrayList<MeshModel> meshModels = new ArrayList<>(element.getMeshModels().values());
            for (int i = 0;i<meshModels.size();i++){
                MeshModel model = meshModels.get(i);
                if (model.getModelId()==0x1000){
                    final int modelIdentifier = model.getModelId();
                    final MeshMessage meshMessage = new ConfigModelSubscriptionDelete(element.getElementAddress(),
                            group.getAddress(),modelIdentifier);
                    try {
                        meshTools.getMeshManagerApi().createMeshPdu(node.getUnicastAddress(),meshMessage);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                }

            }
        }

    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :
                Intent intent = new Intent(DeviceInGroupActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onRefresh() {
        List<ProvisionedMeshNode> list = refreshDevices(meshTools.getMeshNetworkLiveData().getMeshNetwork().getNodes());
        adapter.setData(list);
        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }
}