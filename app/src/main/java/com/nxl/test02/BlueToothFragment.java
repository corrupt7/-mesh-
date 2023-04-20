package com.nxl.test02;



import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.TimeUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nxl.test02.meshTools.BleMeshManager;
import com.nxl.test02.meshTools.MeshTools;
import com.nxl.test02.meshTools.ProvisionerProgress;
import com.nxl.test02.bean.ExtendedBluetoothDevice;
import com.nxl.test02.bean.ProvisionerStates;
import com.nxl.test02.adapter.DeviceAdapter;
import com.nxl.test02.dialog.GroupUpdateDialog;
import com.nxl.test02.style.RecyclerViewItemDecoration;
import com.nxl.test02.tools.BlueToothUtil;
import com.nxl.test02.tools.ToastUtils;

import static com.nxl.test02.tools.BleScanSetting.buildRepeatScanFilters;
import static com.nxl.test02.tools.BleScanSetting.buildScanFilters;
import static com.nxl.test02.tools.BleScanSetting.buildScanSettings;

import java.security.Timestamp;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.mesh.Group;
import no.nordicsemi.android.mesh.MeshBeacon;
import no.nordicsemi.android.mesh.MeshManagerApi;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.Provisioner;
import no.nordicsemi.android.mesh.provisionerstates.ProvisioningCapabilities;
import no.nordicsemi.android.mesh.provisionerstates.UnprovisionedMeshNode;
import no.nordicsemi.android.mesh.transport.ConfigModelAppBind;
import no.nordicsemi.android.mesh.transport.ConfigModelSubscriptionAdd;
import no.nordicsemi.android.mesh.transport.ConfigModelSubscriptionVirtualAddressAdd;
import no.nordicsemi.android.mesh.transport.ConfigNodeReset;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class BlueToothFragment extends Fragment implements View.OnClickListener {
    private String TAG = "BlueToothFragment";

    private Button searchButton;
    private Button notSearchButton;
    private ProgressBar bar;
    private Button connectDeviceButton;
    private View rootView;
    private BlueToothUtil blueTooth;
    private Context context;
    private List<ProvisionedMeshNode> nodes;
    private RecyclerView recyclerView;
    private DeviceAdapter deviceAdapter;
    private List<ExtendedBluetoothDevice> deviceBeanList;
    private ScanCallback scanCallback;
    private MeshTools meshRepository;
    final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
    private ExtendedBluetoothDevice device;
    private boolean isRepeat = false;
    private boolean myisRepeat = false;
    public int count=1;


    public BlueToothFragment() {
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        //生成scanback
        initBleScan();
        //获取meshtool
        init_mesh();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_blue_tooth, container, false);
        }

        //数据的准备
        blueTooth = new BlueToothUtil();
        deviceBeanList = new ArrayList<>();
        //进度条
        bar = rootView.findViewById(R.id.state_scanning);

        //列表的数据注册
        recyclerView = rootView.findViewById(R.id.id_device_list);
        deviceAdapter = new DeviceAdapter(deviceBeanList, context);
        recyclerView.setAdapter(deviceAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        Drawable drawable = getContext().getDrawable(R.drawable.divider);
        recyclerView.addItemDecoration(new RecyclerViewItemDecoration(drawable));

        //列表监听事件的注册
        deviceAdapter.setOnItemClickListener(new DeviceAdapter.OnItemClickListener() {
            @SuppressLint({"MissingPermission", "RestrictedApi"})
            @Override
            public void onItemClick(View v, int position) {

                if (!meshRepository.getUnprovisionedMeshNode().hasObservers()){
                    System.out.println("!meshRepository.getUnprovisionedMeshNode().hasObservers()");
                    meshRepository.getUnprovisionedMeshNode().observe(getActivity(),meshNode ->{
                        if (meshNode != null){
                            final ProvisioningCapabilities capabilities = meshNode.getProvisioningCapabilities();
                            if (capabilities!=null){
                                final MeshNetwork network = meshRepository.getMeshNetworkLiveData().getMeshNetwork();
                                if (network!=null){
                                    try {
                                        final int elementCount = capabilities.getNumberOfElements();
                                        final Provisioner provisioner = network.getSelectedProvisioner();
                                        final int unicast = network.nextAvailableUnicastAddress(elementCount, provisioner);
                                        network.assignUnicastAddress(unicast);
                                        if(connectDeviceButton.getText().toString().equals("识别中...")){
                                            long startTime = System.currentTimeMillis(); //获取开始时间
                                            connectDeviceButton.setText("组网中...");
                                            setupProvisionerStateObservers();
                                            provisionDevice(meshNode);
                                            long endTime = System.currentTimeMillis(); //获取结束时间
                                            System.out.println("组网时间：" + (endTime - startTime) + "ms"); //单位毫秒
                                        }
                                    }
                                    catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    });
                }

                if(!meshRepository.isDeviceReady().hasObservers()){
                    meshRepository.isDeviceReady().observe(getActivity(),deviceReady ->{
                        if(meshRepository.getBleMeshManager().isDeviceReady()&&!isRepeat){
                            final boolean isComplete = meshRepository.isProvisioningComplete();
                            if (isComplete){
                                setupProvisionerStateObservers();
                            }
                        }
                        else if(meshRepository.getBleMeshManager().isDeviceReady()&&isRepeat){
                            connectDeviceButton.setText("断开连接");
                            connectDeviceButton.setEnabled(true);
                        }
                        try {
                            final UnprovisionedMeshNode node = meshRepository.getUnprovisionedMeshNode().getValue();
                            if(node==null&&!isRepeat){
                                if(connectDeviceButton.getText().toString().equals("连接中...")){
                                    long startTime = System.currentTimeMillis(); //获取开始时间
                                    meshRepository.identifyNode(device);
                                    connectDeviceButton.setText("识别中...");
                                    long endTime = System.currentTimeMillis(); //获取结束时间
                                    System.out.println("识别时间：" + (endTime - startTime) + "ms"); //单位毫秒
                                }
                                return;
                            }
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    });
                }

                ExtendedBluetoothDevice deviceBean = deviceBeanList.get(position);
                switch (v.getId()) {
                    case R.id.connect_device:
                        scanner.stopScan(scanCallback);
                        bar.setVisibility(View.INVISIBLE);
                        try {
                            connectDeviceButton = (Button) v;
                            if(myisRepeat&&connectDeviceButton.getText().toString().equals("连接至此设备")){
                                connectDeviceButton.setText("连接中...");
                                connectDeviceButton.setEnabled(false);
                                long startTime = System.currentTimeMillis(); //获取开始时间
                                device = deviceBean;
                                meshRepository.connect(getActivity(),deviceBean,myisRepeat);
                                long endTime = System.currentTimeMillis(); //获取结束时间
                                System.out.println("连接时间：" + (endTime - startTime) + "ms"); //单位毫秒
                                return;
                            }
                            Log.d(TAG, "文字: "+connectDeviceButton.getText());
                            if (connectDeviceButton.getText().toString().equals("连接至此设备")){
                                connectDeviceButton.setText("连接中...");
                                connectDeviceButton.setEnabled(false);
                                device = deviceBean;
                                meshRepository.connect(getActivity(),deviceBean,false);
                                myisRepeat=true;

                            }else if (connectDeviceButton.getText().toString().equals("断开连接")){
                                try {
                                    nodes = meshRepository.getMeshNetworkLiveData().getMeshNetwork().getNodes();
                                    final ConfigNodeReset configNodeReset = new ConfigNodeReset();
                                    meshRepository.getMeshManagerApi().createMeshPdu(nodes.get(position).getUnicastAddress(),configNodeReset);
                                    meshRepository.disconnect();
                                    connectDeviceButton.setText("连接至此设备");
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                            else {
                                Log.d(TAG, "没进入任何一个");
                            }
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        //搜索和取消搜索按钮的注册
        notSearchButton = rootView.findViewById(R.id.id_not_search_device);
        searchButton = rootView.findViewById(R.id.id_search_device);
        notSearchButton.setOnClickListener(this);
        searchButton.setOnClickListener(this);
        searchButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                try {
                    if (!blueTooth.isSupportBlueTooth()){
                        Toast.makeText(getActivity(),"不支持蓝牙!",Toast.LENGTH_SHORT).show();
                    }
                    if (!blueTooth.getBlueToothStatus()){
                        Toast.makeText(getActivity(),"未打开蓝牙!",Toast.LENGTH_SHORT).show();
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.BLUETOOTH_SCAN,}, 1);
                        }
                    }
                    isRepeat = true;
                    deviceBeanList.clear();
                    meshRepository.disconnect();
                    deviceAdapter.setData(deviceBeanList);
                    deviceAdapter.notifyDataSetChanged();
                    bar.setVisibility(View.VISIBLE);
                    scanner.startScan(buildRepeatScanFilters(),buildScanSettings(),scanCallback);
                }catch (Exception e){
                    e.printStackTrace();
                }
                return true;
            }
        });

        return rootView;
    }


    private void provisionDevice(UnprovisionedMeshNode node){
        if (node.getProvisioningCapabilities() != null) {
            meshRepository.getMeshManagerApi().startProvisioning(node);
        }
        else{
            Log.d(TAG, "ProvisioningCapabilities()为空！！: ");
        }
    }

    private void disconnet(){
        if(meshRepository.isProvisioningComplete()){
            meshRepository.disconnect();
        }
    }

    private void setupProvisionerStateObservers(){
        meshRepository.getProvisioningState().observe(getActivity(), provisioningStatusLiveData -> {
            if (provisioningStatusLiveData != null) {
                final ProvisionerProgress provisionerProgress = provisioningStatusLiveData.getProvisionerProgress();
                if (provisionerProgress != null) {
                    final ProvisionerStates state = provisionerProgress.getState();
                    Log.d(TAG, "配网状态: " + state);
                    if(state== ProvisionerStates.APP_KEY_STATUS_RECEIVED){
                        List<ProvisionedMeshNode> list = meshRepository.getNodes().getValue();
                        ProvisionedMeshNode provisionedMeshNode = list.get(list.size() - 1);
                        for (Element element:provisionedMeshNode.getElements().values()){
                            final int elementAddress = element.getElementAddress();
                            ArrayList<MeshModel> meshModels = new ArrayList<>(element.getMeshModels().values());
                            System.out.println(meshModels.size());
                            for(int i = 0;i<meshModels.size();i++){
                                if (meshModels.get(i).getModelId()==0x1000){
                                    Log.d(TAG, "为此设备配置APPKey");
                                    ConfigModelAppBind configModelAppBind = new ConfigModelAppBind(elementAddress,0x1000,0);
                                    int unicastAddress = provisionedMeshNode.getUnicastAddress();
                                    meshRepository.getMeshManagerApi().createMeshPdu(unicastAddress,configModelAppBind);
                                }
                            }

                        }
                        connectDeviceButton.setText("断开连接");
                        connectDeviceButton.setEnabled(true);
                    }
                } else {
                    Log.d(TAG, "provisionerProgress为空: ");
                }
            } else {
                Log.d(TAG, "provisioningStatusLiveData为空: ");
            }
        });
    }

    /**
     * 点击事件
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.id_search_device:
                try {
                    if (!blueTooth.isSupportBlueTooth()){
                        Toast.makeText(getActivity(),"不支持蓝牙!",Toast.LENGTH_SHORT).show();
                    }
                    if (!blueTooth.getBlueToothStatus()){
                        Toast.makeText(getActivity(),"未打开蓝牙!",Toast.LENGTH_SHORT).show();
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT}, 1);
                        }
                    }
                    isRepeat = false;
                    //debug
//                    meshRepository.disconnect();
//                    deviceBeanList.clear();
                    deviceAdapter.setData(deviceBeanList);
                    deviceAdapter.notifyDataSetChanged();
                    bar.setVisibility(View.VISIBLE);
                    scanner.startScan(buildScanFilters(),buildScanSettings(),scanCallback);
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case R.id.id_not_search_device:
                try {
//                    scanner.stopScan(scanCallback);
//                    bar.setVisibility(View.INVISIBLE);
                    meshRepository.disconnect();
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    @SuppressLint("RestrictedApi")
    public void init_mesh(){
        meshRepository = MeshTools.getInstance();
        ((InterfaceActivity)getActivity()).setMeshTools(meshRepository);
    }

    @SuppressLint("MissingPermission")
    public void initBleScan(){
        //扫描回调函数
        scanCallback = new ScanCallback() {
            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(TAG, "onScanFailed: "+errorCode);
            }

            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                boolean isRepetition = false;
                try {
                    //扫描设备
                    ScanRecord scanRecord = result.getScanRecord();
                    byte[] bytes = scanRecord.getBytes();
                    byte[] meshBeaconData = meshRepository.getMeshManagerApi().getMeshBeaconData(scanRecord.getBytes());
                    MeshBeacon meshBeacon = meshRepository.getMeshManagerApi().getMeshBeacon(meshBeaconData);
                    BluetoothDevice device = result.getDevice();
                    if (!deviceBeanList.isEmpty()){
                        for (ExtendedBluetoothDevice deviceBean:deviceBeanList){
                            if (deviceBean.getAddress().equals(device.getAddress())){
                                isRepetition=true;
                            }
                        }
                    }
                    //扫描可用设备时将不在列表的ble设备加入列表

                    if(!isRepetition){
                        ExtendedBluetoothDevice bean = new ExtendedBluetoothDevice(result,meshBeacon);
                        deviceBeanList.add(bean);
                        deviceAdapter.notifyDataSetChanged();
                        Log.d(TAG, "onScanResult: "+device.getAddress()+"meshBeacon:"+meshBeacon+" bytes:"+bytes.length);
                        for (int i=0;i<bytes.length;i++){
                            Log.d(TAG, "bytes: "+i+" = "+bytes[i]);
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        };

    }


}