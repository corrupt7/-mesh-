package com.nxl.test02.alarm;

import static com.nxl.test02.tools.BleScanSetting.buildRepeatScanFilters;
import static com.nxl.test02.tools.BleScanSetting.buildScanSettings;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.room.Room;

import com.nxl.test02.ContextUtil;
import com.nxl.test02.bean.ExtendedBluetoothDevice;
import com.nxl.test02.meshTools.MeshTools;
import com.nxl.test02.notification.ScheduleTaskNotificationTool;
import com.nxl.test02.room.MonitorLog;
import com.nxl.test02.room.MonitorLogDao;
import com.nxl.test02.room.MonitorLogDataBase;
import com.nxl.test02.room.ScheduleTaskDao;
import com.nxl.test02.room.ScheduleTaskDatabase;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.Group;
import no.nordicsemi.android.mesh.MeshBeacon;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.transport.GenericOnOffSetUnacknowledged;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;


public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";
    public static final String OPEN_DEVICE_ACTION = "com.nxl.test02.receiver.AlarmReceiver.OPEN_DEVICE_ACTION";
    public static final String CLOSE_DEVICE_ACTION = "com.nxl.test02.receiver.AlarmReceiver.CLOSE_DEVICE_ACTION";
    private Group group;
    private boolean flag=true;
    private boolean LEDStatus ;
    private ScheduleTaskDatabase scheduleTaskDatabase;
    private ScheduleTaskDao scheduleTaskDao;
    private MonitorLogDataBase monitorLogDataBase;
    private MonitorLogDao monitorLogDao;
    private MonitorLog monitorLog;
    private String taskName;
    private MeshTools meshTools;
    Observer sentScheduleMessageObserver,deviceReadyObserver;
    private ExtendedBluetoothDevice device = null;
    private Executor singleThreadExecutor = Executors.newSingleThreadExecutor();
    private static ScheduleTaskNotificationTool scheduleTaskNotificationTool = ScheduleTaskNotificationTool.getInstance();



    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {


        if (!flag){return;}
        flag=false;
        Log.d(TAG, "收到了这个信息==================================================");
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (TextUtils.equals(action, OPEN_DEVICE_ACTION)){
            Log.d(TAG, "onReceive: 开灯");
            LEDStatus = true;
        }
        else if(TextUtils.equals(action, CLOSE_DEVICE_ACTION)){
            Log.d(TAG, "onReceive: 关灯");
            LEDStatus =false;
        }
        try{
            initData(context,intent);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    private void initData(Context context, Intent intent){
        monitorLogDataBase = Room.databaseBuilder(context,MonitorLogDataBase.class,"monitor")
                .allowMainThreadQueries()
                .build();
        monitorLogDao =monitorLogDataBase.getMonitorLogDao();
        scheduleTaskDatabase = Room.databaseBuilder(context,ScheduleTaskDatabase.class,"schedule")
                .allowMainThreadQueries()
                .build();
        scheduleTaskDao = scheduleTaskDatabase.getScheduleTaskDao();
        monitorLog = new MonitorLog();
        taskName = intent.getStringExtra("taskName");

        //加载meshtools
        meshTools = MeshTools.getInstance();
        if (meshTools==null){
            setMonitorLog("false","程序内部错误");
            scheduleTaskNotificationTool.setNotification("定时任务执行失败","程序内部错误导致定时任务执行失败");
            return;
        }

        //加载meshnetwork
        MeshNetwork meshNetwork = meshTools.getMeshNetworkLiveData().getMeshNetwork();
        if (meshNetwork==null){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            meshNetwork = meshTools.getMeshNetworkLiveData().getMeshNetwork();
            if (meshNetwork==null){
                setMonitorLog("定时任务执行失败","程序与数据库连接过慢");
            }
        }

        //加载group
        int groupId = intent.getIntExtra("groupId",-1);
        group = meshTools.getMeshNetworkLiveData().getMeshNetwork().getGroup(groupId);
        if (group==null){
            setMonitorLog("false","分组未找到，疑似被删除，已经为您删除此定时任务");
            scheduleTaskNotificationTool.setNotification("定时任务执行失败","分组未找到");
            return;
        }

        try {
            setObservers();
            operateLEDDevice();
        }
        catch (Exception e){
            e.printStackTrace();
            setMonitorLog("false","未知错误");
        }
    }

    @SuppressLint("MissingPermission")
    private void operateLEDDevice(){
        boolean value;
        try{
            value = meshTools.isConnectedToProxy().getValue().booleanValue();
        }catch (NullPointerException e){
            value = false;
        }
        if (value){
            sendMessage(group);
        }
        else{
            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            ScanCallback scanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, @NonNull ScanResult result) {
                    super.onScanResult(callbackType, result);
                    try{
                        Log.d(TAG, "收到消息: "+result.getDevice().getAddress());
                        ScanRecord scanRecord = result.getScanRecord();
                        byte[] meshBeaconData = meshTools.getMeshManagerApi().getMeshBeaconData(scanRecord.getBytes());
                        MeshBeacon meshBeacon = meshTools.getMeshManagerApi().getMeshBeacon(meshBeaconData);
                        ExtendedBluetoothDevice bean = new ExtendedBluetoothDevice(result,meshBeacon);
                        if (device==null){
                            meshTools.connect(ContextUtil.getInstance(),bean,true);
                            device = bean;
                            scanner.stopScan(this);
                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };
            scanner.startScan(buildRepeatScanFilters(),buildScanSettings(),scanCallback);
            singleThreadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(40000);
                        scanner.stopScan(scanCallback);
                        if (device==null){
                            meshTools.isDeviceReady().removeObserver(deviceReadyObserver);
                            setMonitorLog("定时任务执行失败","重连过程中未发现设备，可能原因:您距离网络太远了");
                            Log.d(TAG, "定时任务执行失败");
                            scheduleTaskNotificationTool.setNotification("定时任务执行失败","重连过程中未发现设备，可能您距离网络太远了");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }




    private void sendMessage(Group group){
        meshTools.getmSentScheduleMessage().removeObserver(sentScheduleMessageObserver);
        meshTools.getmIsSentScheduleMessage().postValue(true);
        meshTools.getmSentScheduleMessage().postValue(false);
        meshTools.getmSentScheduleMessage().observeForever(sentScheduleMessageObserver);

        final MeshMessage meshMessage;
        final ApplicationKey appKey = meshTools.getMeshNetworkLiveData().getMeshNetwork().getAppKey(0);
        final int tid = new Random().nextInt();
        meshMessage =new GenericOnOffSetUnacknowledged(appKey,LEDStatus,tid);
        try {
            Log.d(TAG, "开始发送定时任务信息===========================================");
            meshTools.getMeshManagerApi().createMeshPdu(group.getAddress(),meshMessage);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    @SuppressLint("NewApi")
    private String getTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");// HH:mm:ss
        Date date = new Date(System.currentTimeMillis());
        String time = simpleDateFormat.format(date);
        Log.d(TAG, "getTime: "+time);
        return time;
    }

    private void setMonitorLog(String isSucceed,String monitorLogMessage){
        monitorLog.setTime(getTime());
        monitorLog.setIsSucceed(isSucceed);
        monitorLog.setTaskName(taskName);
        monitorLog.setMonitorLogMessage(monitorLogMessage);
        monitorLogDao.insertMonitorLog(monitorLog);
    }

    private void setObservers(){


        sentScheduleMessageObserver = new Observer() {
            @Override
            public void onChanged(Object o) {
                boolean aBoolean = (boolean) o;
                if (aBoolean){
                    setMonitorLog("true","定时任务执行成功");
                    scheduleTaskNotificationTool.setNotification("定时任务执行成功",
                            "您设置的"+group.getName()+"分组的"+(LEDStatus?"开启":"关闭")+"任务已经成功执行");
                    meshTools.getmIsSentScheduleMessage().postValue(false);
                    meshTools.getmSentScheduleMessage().removeObserver(this::onChanged);
                }
            }
        };

        deviceReadyObserver = new Observer() {
            @Override
            public void onChanged(Object o) {
                Log.d(TAG, "连接状态改变:"+meshTools.getBleMeshManager().isDeviceReady());
                if (meshTools.getBleMeshManager().isDeviceReady()){
                    sendMessage(group);
                    meshTools.isDeviceReady().removeObserver(this::onChanged);
                }
            }
        };

        meshTools.isDeviceReady().removeObserver(deviceReadyObserver);
        meshTools.isDeviceReady().observeForever(deviceReadyObserver);

    }


}
