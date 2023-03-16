package com.nxl.test02;


import static com.nxl.test02.tools.BleScanSetting.buildRepeatScanFilters;
import static com.nxl.test02.tools.BleScanSetting.buildScanSettings;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.nxl.test02.alarm.AlarmSender;
import com.nxl.test02.meshTools.MeshTools;
import com.nxl.test02.bean.ExtendedBluetoothDevice;
import com.nxl.test02.adapter.GroupAdapter;
import com.nxl.test02.alarm.AlarmReceiver;
import com.nxl.test02.room.ScheduleTask;
import com.nxl.test02.room.ScheduleTaskDao;
import com.nxl.test02.room.ScheduleTaskDatabase;
import com.nxl.test02.style.RecyclerViewItemDecoration;
import com.nxl.test02.tools.PendingIntentUtil;
import com.nxl.test02.tools.ToastUtils;

import java.util.ArrayList;
import java.util.List;
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

public class GroupFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "GroupFragment";


    private View rootView;
    private Button insertButton,clearButton;
    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private boolean LEDstatus =false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ScheduleTaskDatabase scheduleTaskDatabase;
    private ScheduleTaskDao scheduleTaskDao;
    private MeshTools meshRepository;
    private MeshNetwork meshNetwork;
    private List<Group> groups;
    private boolean isConnectToProxy = false;
    private MutableLiveData<ExtendedBluetoothDevice> deviceLiveData = new MutableLiveData<>();

    //这四个变量是在定时任务弹窗中的，其他不要使用
    private int hour,minute,LedStatus;
    EditText nameText;



    public GroupFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scheduleTaskDatabase = Room.databaseBuilder(getActivity(),ScheduleTaskDatabase.class,"schedule")
                .allowMainThreadQueries()
                .build();
        scheduleTaskDao = scheduleTaskDatabase.getScheduleTaskDao();
        meshRepository = MeshTools.getInstance();
        if (meshRepository==null){
            meshRepository = ((InterfaceActivity)getActivity()).getMeshTools();
        }
        meshNetwork = meshRepository.getMeshNetworkLiveData().getMeshNetwork();
        meshRepository.isConnectedToProxy().observe(getActivity(),aBoolean -> {
            isConnectToProxy = aBoolean;
        });
        deviceLiveData.postValue(null);
    }

    @Override
    @SuppressLint("MissingPermission")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
        rootView = inflater.inflate(R.layout.fragment_group, container, false);
        }
        groups = meshRepository.getMeshNetworkLiveData().getMeshNetwork().getGroups();
        insertButton = rootView.findViewById(R.id.insert_group);
        clearButton = rootView.findViewById(R.id.clear_groups);
        insertButton.setOnClickListener(this);
        clearButton.setOnClickListener(this);
        swipeRefreshLayout = rootView.findViewById(R.id.srl);

        recyclerView = rootView.findViewById(R.id.device_groups);
        adapter = new GroupAdapter(groups);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));;
        Drawable drawable = getContext().getDrawable(R.drawable.divider);
        recyclerView.addItemDecoration(new RecyclerViewItemDecoration(drawable));

        swipeRefreshLayout.setOnRefreshListener(this);

        adapter.setOnItemClickListener(new GroupAdapter.OnItemClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onItemClick(View v, int position) {
                Group group = groups.get(position);
                switch (v.getId()){
                    case R.id.group_name:
                        if (!meshRepository.isConnectedToProxy().getValue()){
                            ToastUtils.show(getActivity(),"需要先连接至网络");
                            return;
                        }
                        Intent intent = new Intent();
                        intent.setClass(getActivity(),DeviceInGroupActivity.class);
                        intent.putExtra("group_address",group.getAddress());
                        intent.putExtra("group_name",group.getName());
                        startActivity(intent);
                        break;
                    case R.id.turn_on_by_group:
                        LEDstatus = true;
                        operateLEDDevice(group);
                        break;
                    case R.id.turn_off_by_group:
                        LEDstatus = false;
                        operateLEDDevice(group);
                        break;
                    case R.id.delete_group:
                       List<ScheduleTask> scheduleTasks = scheduleTaskDao.queryScheduleTaskByGroupAndAction(group.getAddress(), AlarmReceiver.OPEN_DEVICE_ACTION);
                       List<ScheduleTask> scheduleTasks1 = scheduleTaskDao.queryScheduleTaskByGroupAndAction(group.getAddress(), AlarmReceiver.CLOSE_DEVICE_ACTION);
                       if(scheduleTasks.isEmpty()&&scheduleTasks1.isEmpty()){
                           meshNetwork.removeGroup(group);
                           adapter.notifyDataSetChanged();
                           adapter.notifyDataSetChanged();
                       }
                       else{
                           showWarnDialog();
                       }
                        break;
                    case R.id.set_scheduled_task:
                        setScheduleTask(group);
                        break;
                    default:
                        break;
                }
            }
        });
        return rootView;
    }


    private void showWarnDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("非法操作");
        dialog.setMessage("由于该分组设置有定时任务，所以无法删除");
        dialog.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.show();
    }

    /**
     * 界面的点击事件，不包括recyclerview
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.insert_group:
                showInsertGroupDialog();
                break;
            case R.id.clear_groups:
                try{
                    List<Group> mGroups = meshNetwork.getGroups();
                    for (Group group :mGroups){
                        meshNetwork.removeGroup(group);
                    }
                    groups = meshNetwork.getGroups();
                    adapter.notifyDataSetChanged();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }


    /**
     * 点击添加后显示的对话框
     */
    public void showInsertGroupDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.insert_device_group_dialog, null);
        builder.setView(view);
        final AlertDialog dialog = builder.show();
        EditText groupNameEditText = view.findViewById(R.id.group_name_edit_text);
        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View view) {
                String groupName = groupNameEditText.getText().toString().trim();
                if(groupName==null||groupName.trim().isEmpty()){
                    ToastUtils.show(getActivity(),"请输入有效名称!");
                }
                else {
                    if(isExistGroup(groupName)){
                        ToastUtils.show(getActivity(),"已有重名小组！");
                    }
                    else {
                        Group group = meshNetwork.createGroup(meshNetwork.getSelectedProvisioner(), groupName);
                        try {
                            boolean b = meshNetwork.addGroup(group);
                            groups = meshNetwork.getGroups();
                            adapter.notifyDataSetChanged();
                            if (!b){
                                ToastUtils.show(getActivity(),"出了点小问题，请重试");
                            }
                            dialog.dismiss();
                        }catch (Exception e){
                            e.printStackTrace();
                            ToastUtils.show(getActivity(),"出错了!!");
                            dialog.dismiss();
                        }
                    }
                }
            }
        });

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private boolean isExistGroup(String groupName){
        List<Group> mGroups = meshNetwork.getGroups();
        boolean isExist = false;
        for (Group group:mGroups){
            if(group.getName().equals(groupName)){
                isExist = true;
                break;
            }
        }
        return isExist;
    }

    /**
     * 下拉刷新
     */
    @Override
    public void onRefresh() {
        groups = meshNetwork.getGroups();
        Log.d(getTag(), "onRefresh: "+groups.size());
        if(groups==null||groups.size()==0){
            groups = new ArrayList<Group>();
        }
        adapter.setGroupBeans(groups);
        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * 操作LED灯
     * @param group
     */
    @SuppressLint({"MissingPermission", "NewApi"})
    private void operateLEDDevice(Group group){
        if (!isConnectToProxy){
            meshRepository.getmSentGroupMessage().removeObservers(getActivity());
            meshRepository.getmIsSentGroupMessage().postValue(true);
            meshRepository.getmSentGroupMessage().postValue(false);
            meshRepository.getmSentGroupMessage().observe(getActivity(),aBoolean -> {
                if (aBoolean){
                    if (meshRepository.getmIsSentGroupMessage().getValue()){
                        ToastUtils.show(getActivity(),"发送成功");
                        meshRepository.getmIsSentGroupMessage().postValue(false);
                    }
                }
            });
            meshRepository.isConnectedToProxy().removeObservers(getActivity());
            ToastUtils.show(getActivity(),"正在为您重新连接回mesh网络...");
            meshRepository.isConnectedToProxy().observe(getActivity(),aBoolean -> {
                isConnectToProxy = aBoolean;
                if (aBoolean){
                    ToastUtils.show(getActivity(),"连接成功");
                    sendMessage(group);
                }
            });
            connectToProxy();
        }
        else {
            sendMessage(group);
        }

    }

    private void connectToProxy(){
        deviceLiveData.observe(getActivity(),extendedBluetoothDevice -> {
            if (extendedBluetoothDevice!=null){
                meshRepository.connect(getActivity(),extendedBluetoothDevice,true);
            }
        });
        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, @NonNull ScanResult result) {
                super.onScanResult(callbackType, result);
                try {
                    ScanRecord scanRecord = result.getScanRecord();
                    byte[] meshBeaconData = meshRepository.getMeshManagerApi().getMeshBeaconData(scanRecord.getBytes());
                    MeshBeacon meshBeacon = meshRepository.getMeshManagerApi().getMeshBeacon(meshBeaconData);
                    ExtendedBluetoothDevice bean = new ExtendedBluetoothDevice(result,meshBeacon);
                    if (deviceLiveData.getValue()==null){
                        Log.d(TAG, "自动连接找到了设备");
                        deviceLiveData.postValue(bean);
                    }

                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
        };
        scanner.startScan(buildRepeatScanFilters(),buildScanSettings(),scanCallback);
        Executor singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(15);
                    scanner.stopScan(scanCallback);
                    if (deviceLiveData.getValue()==null){
                        meshRepository.isConnectedToProxy().removeObservers(getActivity());
                        meshRepository.getmSentGroupMessage().removeObservers(getActivity());
                        meshRepository.getmIsSentGroupMessage().postValue(false);
                        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                        dialog.setTitle("无法重连回网络");
                        dialog.setMessage("无法重连回网络，可能是您距离设备太远，请尝试手动重连");
                        dialog.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        dialog.show();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    }
    private  void sendMessage(Group group){
        final MeshMessage meshMessage;
        final ApplicationKey appKey = meshRepository.getMeshNetworkLiveData().getMeshNetwork().getAppKey(0);
        final int tid = new Random().nextInt();
        meshMessage =new GenericOnOffSetUnacknowledged(appKey,LEDstatus,tid);
        try {
            meshRepository.getMeshManagerApi().createMeshPdu(group.getAddress(),meshMessage);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 定时任务弹窗
     */
    @SuppressLint("NewApi")
    private void setScheduleTask(Group group){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.group_schedule_task_dialog,null);
        builder.setView(view);
        final AlertDialog dialog = builder.show();
        TimePicker timePicker = view.findViewById(R.id.time_picker);
        Button confirmOpenButton,confirmCloseButton;
        confirmOpenButton = view.findViewById(R.id.open_schedule_task);
        confirmCloseButton = view.findViewById(R.id.close_schedule_task);
        TextView cancel = view.findViewById(R.id.cancel_schedule_task);
        nameText = view.findViewById(R.id.schedule_task_name);
        Calendar calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR_OF_DAY); /* 获取当前小时 */
        minute = calendar.get(Calendar.MINUTE); /* 获取当前分钟 */
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minuteOfHour) {
                hour = hourOfDay;
                minute =minuteOfHour;
            }
        });

        confirmOpenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String scheduleTaskName = nameText.getText().toString();
                    if(scheduleTaskName.isEmpty()){
                        ToastUtils.show(getActivity(),"请输入名字");
                        return;
                    }
                    LedStatus = 1;
                    scheduleTask(group,LedStatus,scheduleTaskName);
                    dialog.dismiss();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        confirmCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String scheduleTaskName = nameText.getText().toString();
                    if(scheduleTaskName.isEmpty()){
                        ToastUtils.show(getActivity(),"请输入名字");
                        return;
                    }
                    LedStatus = 0;
                    Log.d(TAG, "LedStatus: "+LedStatus);
                    scheduleTask(group,LedStatus,scheduleTaskName);
                    dialog.dismiss();
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

    }

    /**
     *
     * 设置定时任务
     * @param group
     * @param status
     * @param name
     */
    @SuppressLint("NewApi")
    private void scheduleTask(Group group,int status,String name){
        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent mIntent = new Intent(getActivity(),AlarmReceiver.class);
        mIntent.putExtra("taskName",name);
        PendingIntent pendingIntent;
        List<ScheduleTask> scheduleTasks;
        if(status==1){
            pendingIntent = PendingIntentUtil.getPendingIntent(group,getActivity(),mIntent,AlarmReceiver.OPEN_DEVICE_ACTION);
            scheduleTasks = scheduleTaskDao.queryScheduleTaskByGroupAndAction(group.getAddress(),AlarmReceiver.OPEN_DEVICE_ACTION);
        }else {
            pendingIntent = PendingIntentUtil.getPendingIntent(group,getActivity(),mIntent,AlarmReceiver.CLOSE_DEVICE_ACTION);
            scheduleTasks = scheduleTaskDao.queryScheduleTaskByGroupAndAction(group.getAddress(),AlarmReceiver.CLOSE_DEVICE_ACTION);
        }

        ScheduleTask scheduleTask;
        if(status==1){
            scheduleTask = new ScheduleTask(name,group.getAddress(),AlarmReceiver.OPEN_DEVICE_ACTION,hour,minute);
        }
        else {
            scheduleTask = new ScheduleTask(name,group.getAddress(),AlarmReceiver.CLOSE_DEVICE_ACTION,hour,minute);
        }
        long time = getTime(Calendar.getInstance());
        Log.d(TAG, "得到的time "+time);
        if(scheduleTasks.isEmpty()){
            scheduleTaskDao.insertScheduleTask(scheduleTask);
            AlarmSender.setAlarm(group,status,name,time);
        }
        else{
            ScheduleTask mScheduleTask = scheduleTasks.get(0);
            showConfirmDialog(mScheduleTask,scheduleTask,am,pendingIntent,time);
        }
    }

    @SuppressLint("NewApi")
    private long getTime(Calendar calendar){
        int thisHour = calendar.get(Calendar.HOUR_OF_DAY);
        int thisMinute = calendar.get(Calendar.MINUTE);
        long time1 = calendar.getTimeInMillis();
        //如果此时还不到设置的时间
        if(thisHour<hour||(thisHour==hour&&thisMinute<minute)){
            calendar.set(Calendar.HOUR_OF_DAY,hour);
            calendar.set(Calendar.MINUTE,minute);
            long time2 = calendar.getTimeInMillis();
            long time = time2-time1;
            Log.d(TAG, "getTime: "+time);
            return time;
        }
        //此时已经超过时间
        else if(thisHour>hour||(thisHour==hour && thisMinute>minute)){
            calendar.add(Calendar.DATE,1);
            calendar.set(Calendar.HOUR_OF_DAY,hour);
            calendar.set(Calendar.MINUTE,minute);
            long time2 = calendar.getTimeInMillis();
            long time = time2-time1;
            Log.d(TAG, "getTime: "+time);
            return time;
        }
        else {
            Log.d(TAG, "getTime: "+0);
            return 0;
        }
    }

    /**
     * 当存在相同类型的定时任务时进行更新的操作
     * @param scheduleTask
     */
    private void showConfirmDialog(ScheduleTask scheduleTask,ScheduleTask newScheduleTask,AlarmManager am,PendingIntent pendingIntent,long time){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("是否覆盖");
        if (AlarmReceiver.OPEN_DEVICE_ACTION.equals(scheduleTask.getAction())){
            dialog.setMessage("检测到已有此设备的定时开启任务，是否覆盖？");
        }
        else {
            dialog.setMessage("检测到已有此设备的定时关闭任务，是否覆盖？");
        }
        dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @SuppressLint({"NewApi", "MissingPermission"})
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteSchedule(scheduleTask);
                scheduleTaskDao.insertScheduleTask(newScheduleTask);
                if (time ==0){
                    am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() +5*1000,pendingIntent);
                }
                else {
                    am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime()+time,24*60*60*1000,pendingIntent);
                }
            }
        });
        dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialog.show();
    }

    private void deleteSchedule(ScheduleTask scheduleTask){
        int groupId = scheduleTask.getGroupId();
        Group group = meshRepository.getMeshNetworkLiveData().getMeshNetwork().getGroup(groupId);
        if (group==null){
            scheduleTaskDao.deleteScheduleTask(scheduleTask);
            return;
        }
        else{
//            Intent mIntent = new Intent(getActivity(), AlarmReceiver.class);
//            mIntent.putExtra("taskName",scheduleTask.getTaskName());
//            PendingIntent pendingIntent = PendingIntentUtil.getPendingIntent(group,getActivity(),mIntent,scheduleTask.getAction());
//            AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            try{
                AlarmSender.cancelAlarm(group,scheduleTask);
                Log.d(TAG, "删除定时任务成功");
                ToastUtils.show(getContext(),"删除定时任务成功");
            }
            catch (Exception e){
                e.printStackTrace();
            }
            scheduleTaskDao.deleteScheduleTask(scheduleTask);
        }

    }



}