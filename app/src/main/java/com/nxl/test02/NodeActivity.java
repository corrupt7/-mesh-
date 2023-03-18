package com.nxl.test02;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.nxl.test02.meshTools.MeshTools;
import com.nxl.test02.adapter.NodeAdapter;
import com.nxl.test02.dialog.GroupUpdateDialog;
import com.nxl.test02.style.RecyclerViewItemDecoration;
import com.nxl.test02.tools.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.Group;
import no.nordicsemi.android.mesh.transport.ConfigModelSubscriptionAdd;
import no.nordicsemi.android.mesh.transport.ConfigNodeReset;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.GenericOnOffGet;
import no.nordicsemi.android.mesh.transport.GenericOnOffSet;
import no.nordicsemi.android.mesh.transport.GenericOnOffStatus;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.utils.MeshAddress;

public class NodeActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,NodeAdapter.OnItemClickListener{

    private String TAG = "NodeActivity";
    private MeshTools meshTools = MeshTools.getInstance();
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private NodeAdapter adapter;
    private List<ProvisionedMeshNode> nodes;
    private boolean LEDStatus = false;
    private List<Group> groups;
    private GroupUpdateDialog dialog;
    private MutableLiveData<List<String>> state = new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node);

        initData();
        setObservers();
        initView();

    }

    /**
     * 加载数据
     */
    private void initData(){
        nodes = meshTools.getMeshNetworkLiveData().getMeshNetwork().getNodes();
        System.out.println(nodes);
        List<String> stateValue = new ArrayList<>();
        for (int i=0;i< nodes.size();i++){
            stateValue.add(i,"未知");
        }
        state.postValue(stateValue);
        adapter = new NodeAdapter(nodes,state);
    }

    /**
     * 加载界面数据
     */
    private void initView(){
        swipeRefreshLayout = findViewById(R.id.srl_node);
        recyclerView = findViewById(R.id.recyclerView_node);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Drawable drawable = getDrawable(R.drawable.divider);
        recyclerView.addItemDecoration(new RecyclerViewItemDecoration(drawable));
        swipeRefreshLayout.setOnRefreshListener(this);
        adapter.setOnItemClickListener(this);
        setTitle("我的设备");
    }

    /**
     * 更新node节点和状态信息
     */
    private void refreshData(){
        nodes = meshTools.getMeshNetworkLiveData().getMeshNetwork().getNodes();
        List<String> stateValue = state.getValue();
        int size = nodes.size();
        if (stateValue.size()==size){
            return;
        }
        for (int i =stateValue.size();i<size;i++){
            stateValue.add(stateValue.size(),"未知");
        }
    }

    /**
     * 添加监听函数
     */
    private void setObservers(){
        nodes = meshTools.getMeshNetworkLiveData().getMeshNetwork().getNodes();
        for (int i=0;i< nodes.size();i++){
            setupMeshMessageObservers(i);
            getNodeStatus(nodes.get(i));
        }
//        meshTools.getMeshNetworkLiveData().observe(this,meshNetworkLiveData -> {
//            refreshData();
//            adapter.setData(nodes,state);
//            adapter.notifyDataSetChanged();
//        });

//        state.observe(this,strings -> {
//            if (state.getValue().size()!= nodes.size()){return;}
//            adapter.notifyDataSetChanged();
//        });

    }


    private void refreshState(int position,String stateData){
        List<String> stateValue = state.getValue();
        stateValue.set(position,stateData);
        state.postValue(stateValue);
        adapter.setData(nodes,state);
        adapter.notifyDataSetChanged();
    }

    private void refreshState(int position,String stateData,String color){
        List<String> stateValue = state.getValue();
        stateValue.set(position,stateData);
        state.postValue(stateValue);
        adapter.setData(nodes,state);
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onItemClick(View v, int position,TextView textView) {
        switch (v.getId()){
            case R.id.node_status:
                setupMeshMessageObservers(textView,position);
                getNodeStatus(nodes.get(position));
                break;
            case R.id.node_open:
                LEDStatus=true;
                operateLEDDevice(nodes.get(position));
         //       textView.setText("开启状态");
                refreshState(position,"开启状态");
                textView.setTextColor(this.getResources().getColor(R.color.teal_200));
                break;
            case R.id.node_close:
                LEDStatus=false;
                operateLEDDevice(nodes.get(position));
                textView.setTextColor(this.getResources().getColor(R.color.red));
           //     textView.setText("关闭状态");
                refreshState(position,"关闭状态");
                break;
            case R.id.set_group:
                showGroupDialog(nodes.get(position));
                break;
            case R.id.cancel_network:
                try{
                    cancelNetwork(nodes.get(position));
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case R.id.node_name:
                showUpgradeDeviceNameDialog(position);
                break;
            default:
                break;
        }
    }


    @SuppressLint("ResourceAsColor")
    private void setupMeshMessageObservers(int position){
        if(meshTools.getMeshMessageLiveData().hasObservers()){
            meshTools.getMeshMessageLiveData().removeObservers(this);
        }
        meshTools.getMeshMessageLiveData().observe(this,meshMessage -> {
            if (meshMessage instanceof GenericOnOffStatus) {
                final GenericOnOffStatus status = (GenericOnOffStatus) meshMessage;
                final boolean presentState = status.getPresentState();
                final Boolean targetOnOff = status.getTargetState();
                if (targetOnOff == null) {
                    if (presentState) {
                        //    textView.setText("开启状态");
                        refreshState(position,"开启状态");
                    } else {
                        //    textView.setText("关闭状态");
                        refreshState(position,"关闭状态");
                    }
                } else {
                    if (!targetOnOff) {
                        //        textView.setText("开启状态");
                        refreshState(position,"开启状态");
                    } else {
                        //       textView.setText("关闭状态");
                        refreshState(position,"关闭状态");
                    }
                }
            }
        });
    }

    @SuppressLint("ResourceAsColor")
    private void setupMeshMessageObservers(TextView textView,int position){
        if(meshTools.getMeshMessageLiveData().hasObservers()){
            meshTools.getMeshMessageLiveData().removeObservers(this);
        }
        meshTools.getMeshMessageLiveData().observe(this,meshMessage -> {
            if (meshMessage instanceof GenericOnOffStatus) {
                final GenericOnOffStatus status = (GenericOnOffStatus) meshMessage;
                final boolean presentState = status.getPresentState();
                final Boolean targetOnOff = status.getTargetState();
                if (targetOnOff == null) {
                    if (presentState) {
                    //    textView.setText("开启状态");
                        refreshState(position,"开启状态");
                        textView.setTextColor(this.getResources().getColor(R.color.teal_200));
                    } else {
                    //    textView.setText("关闭状态");
                        refreshState(position,"关闭状态");
                        textView.setTextColor(this.getResources().getColor(R.color.red));
                    }
                } else {
                    if (!targetOnOff) {
                //        textView.setText("开启状态");
                        refreshState(position,"开启状态");
                        textView.setTextColor(this.getResources().getColor(R.color.teal_200));
                    } else {
                 //       textView.setText("关闭状态");
                        refreshState(position,"关闭状态");
                        textView.setTextColor(this.getResources().getColor(R.color.red));
                    }
                }
            }
        });
    }




    /**
     * 更改设备名字的弹窗
     * @param position
     */
    private void showUpgradeDeviceNameDialog(int position){
        final AlertDialog.Builder builder = new AlertDialog.Builder(NodeActivity.this);
        View view = LayoutInflater.from(NodeActivity.this).inflate(R.layout.upgrade_device_name_dialog, null);
        builder.setView(view);
        final AlertDialog dialog = builder.show();
        EditText editText = view.findViewById(R.id.device_name_upgrade_edit_text);
        view.findViewById(R.id.cancel_upgrade_device_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.confirm_upgrade_device_name).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                String deviceName = editText.getText().toString().trim();
                if(deviceName.isEmpty()){
                    ToastUtils.show(NodeActivity.this,"请输入有效设备名!");}
                else {
                    meshTools.getMeshNetworkLiveData().getMeshNetwork().updateNodeName(nodes.get(position),deviceName);
                    dialog.dismiss();
                }
            }
        });

    }

    /**
     * 设置分组信息的弹窗
     * @param mNode
     */
    private void showGroupDialog(ProvisionedMeshNode mNode){
        groups = meshTools.getMeshNetworkLiveData().getMeshNetwork().getGroups();
        dialog = new GroupUpdateDialog(NodeActivity.this, groups, new GroupUpdateDialog.OnSelectorListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void getSelectorPosition(int position) {
                Log.d(TAG, "getSelectorPosition: "+position);
                if (position==-1){
                    ToastUtils.show(NodeActivity.this,"请选择分组");
                    return;
                }
                Group group = groups.get(position);
                if(mNode!=null){
                    for(Element element:mNode.getElements().values()){
                        ArrayList<MeshModel> meshModels = new ArrayList<>(element.getMeshModels().values());
                        for(int i = 0;i<meshModels.size();i++){
                            final int elementAddress = element.getElementAddress();
                            MeshModel model = meshModels.get(i);
                            if (model.getModelId()==0x1000){
                                List<Integer> subscribedAddresses = model.getSubscribedAddresses();
                                for (int j =0;j<subscribedAddresses.size();j++){
                                    if (subscribedAddresses.get(j)==group.getAddress()){
                                        dialog.dismiss();
                                        return;
                                    }
                                }
                                meshTools.setSelectedModel(model);
                                if (meshTools.getSelectedModel().hasObservers()){
                                    meshTools.getSelectedModel().removeObservers(NodeActivity.this);
                                }
                                meshTools.getSelectedModel().observe(NodeActivity.this,meshModel -> {
                                    refreshData();
                                    adapter.setData(nodes,state);
                                    adapter.notifyDataSetChanged();
                                });
                                final int modelIdentifier = model.getModelId();
                                MeshMessage configModelSubscriptionAdd= new ConfigModelSubscriptionAdd(elementAddress, group.getAddress(), modelIdentifier);
                                meshTools.getMeshManagerApi().createMeshPdu(mNode.getUnicastAddress(),configModelSubscriptionAdd);
                            }
                        }
                    }
                }
                else{
                    ToastUtils.show(NodeActivity.this,"请先连接至设备");
                }
                dialog.dismiss();
            }

            @Override
            public void cancel() {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void operateLEDDevice(ProvisionedMeshNode node){
        try {
            final ApplicationKey appKey = meshTools.getMeshNetworkLiveData().getMeshNetwork().getAppKey(0);
            final GenericOnOffSet genericOnOffSet = new GenericOnOffSet(appKey,LEDStatus,new Random().nextInt());
            for (Element element:node.getElements().values()){
                final int address = element.getElementAddress();
                ArrayList<MeshModel> meshModels = new ArrayList<>(element.getMeshModels().values());
                for(int i = 0;i<meshModels.size();i++){
                    if (meshModels.get(i).getModelId()==0x1000){
                        Log.v(TAG, "Sending message to element's unicast address: " + MeshAddress.formatAddress(address, true));
                        meshTools.getMeshManagerApi().createMeshPdu(address,genericOnOffSet);
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getNodeStatus(ProvisionedMeshNode node){
        final ApplicationKey appKey = meshTools.getMeshNetworkLiveData().getMeshNetwork().getAppKey(0);
        final GenericOnOffGet genericOnOffSet = new GenericOnOffGet(appKey);

        for (Element element:node.getElements().values()){
            final int address = element.getElementAddress();
            ArrayList<MeshModel> meshModels = new ArrayList<>(element.getMeshModels().values());
            for(int i = 0;i<meshModels.size();i++){
                if (meshModels.get(i).getModelId()==0x1000){
                    Log.v(TAG, "Sending message to element's unicast address: " + MeshAddress.formatAddress(address, true));
                    meshTools.getMeshManagerApi().createMeshPdu(address,genericOnOffSet);
                }
            }
        }
    }

    private void cancelNetwork(ProvisionedMeshNode node){
        final ConfigNodeReset configNodeReset = new ConfigNodeReset();
        meshTools.getMeshManagerApi().createMeshPdu(node.getUnicastAddress(),configNodeReset);
    }

    @Override
    public void onRefresh() {
        refreshData();
        adapter.setData(nodes,state);
        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }
}