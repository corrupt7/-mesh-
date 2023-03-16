package com.nxl.test02;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import com.nxl.test02.adapter.MonitorLogAdapter;
import com.nxl.test02.room.MonitorLog;
import com.nxl.test02.room.MonitorLogDao;
import com.nxl.test02.room.MonitorLogDataBase;
import com.nxl.test02.style.RecyclerViewItemDecoration;

import java.util.Collections;
import java.util.List;

public class ScheduleTaskMonitorActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private String TAG = "ScheduleTaskMonitorActivity";

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MonitorLogAdapter adapter;
    private List<MonitorLog> monitorLogs;
    private MonitorLogDao monitorLogDao;
    private MonitorLogDataBase monitorLogDataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_task_monitor);
        initData();
        initView();
    }

    private void initData(){
        monitorLogDataBase = Room.databaseBuilder(ScheduleTaskMonitorActivity.this,MonitorLogDataBase.class,"monitor")
                .allowMainThreadQueries()
                .build();
        monitorLogDao =monitorLogDataBase.getMonitorLogDao();
        monitorLogs = monitorLogDao.queryAllMonitorLog();
    }


    private void initView(){
        swipeRefreshLayout = findViewById(R.id.srl_monitor);
        recyclerView = findViewById(R.id.monitor_list);
        adapter = new MonitorLogAdapter(monitorLogs);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ScheduleTaskMonitorActivity.this));
        Drawable drawable = getDrawable(R.drawable.divider);
        recyclerView.addItemDecoration(new RecyclerViewItemDecoration(drawable));
        swipeRefreshLayout.setOnRefreshListener(this);
        adapter.setOnItemClickListener(new MonitorLogAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                switch (v.getId()){
                    case R.id.monitor_message:
                        String monitorLogMessage = monitorLogs.get(position).getMonitorLogMessage();
                        showDialog(monitorLogMessage);
                        break;
                    case R.id.delete_monitor_log:
                        monitorLogDao.deleteMonitorLog(monitorLogs.get(position));
                        monitorLogs = monitorLogDao.queryAllMonitorLog();
                        adapter.setData(monitorLogs);
                        adapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }
        });

    }

    @Override
    public void onRefresh() {
        monitorLogs = monitorLogDao.queryAllMonitorLog();
        adapter.setData(monitorLogs);
        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }


    private void showDialog(String monitorLogMessage){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(ScheduleTaskMonitorActivity.this);
        dialog.setTitle("详细信息");
        dialog.setMessage(monitorLogMessage);
        dialog.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialog.show();
    }

}