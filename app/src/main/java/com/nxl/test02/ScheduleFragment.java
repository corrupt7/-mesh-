package com.nxl.test02;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nxl.test02.alarm.AlarmSender;
import com.nxl.test02.meshTools.MeshTools;
import com.nxl.test02.adapter.ScheduleAdapter;
import com.nxl.test02.alarm.AlarmReceiver;
import com.nxl.test02.room.ScheduleTask;
import com.nxl.test02.room.ScheduleTaskDao;
import com.nxl.test02.room.ScheduleTaskDatabase;
import com.nxl.test02.style.RecyclerViewItemDecoration;
import com.nxl.test02.tools.PendingIntentUtil;

import java.util.List;

import no.nordicsemi.android.mesh.Group;

public class ScheduleFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "ScheduleFragment";


    private View rootView;
    private RecyclerView recyclerView;
    private ScheduleAdapter adapter;
    private ScheduleTaskDatabase scheduleTaskDatabase;
    private ScheduleTaskDao scheduleTaskDao;
    private List<ScheduleTask> scheduleTasks;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PendingIntent pendingIntent;
    private MeshTools meshTools;


    public ScheduleFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scheduleTaskDatabase = Room.databaseBuilder(getActivity(),ScheduleTaskDatabase.class,"schedule")
                .allowMainThreadQueries()
                .build();
        scheduleTaskDao = scheduleTaskDatabase.getScheduleTaskDao();
        meshTools = ((InterfaceActivity)getActivity()).getMeshTools();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView ==null){
            rootView = inflater.inflate(R.layout.fragment_schedule,container,false);
        }
        try {
            recyclerView = rootView.findViewById(R.id.schedule_list);
            scheduleTasks = scheduleTaskDao.queryAllScheduleTask();
            adapter = new ScheduleAdapter(scheduleTasks,getActivity(),meshTools);
            adapter.setOnItemClickListener(new ScheduleAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View v, int position) {
                    switch (v.getId()){
                        case R.id.delete_schedule:
                            deleteSchedule(position);
                            break;
                        default:
                            break;
                    }
                }
            });

            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            Drawable drawable = getContext().getDrawable(R.drawable.divider);
            recyclerView.addItemDecoration(new RecyclerViewItemDecoration(drawable));

            swipeRefreshLayout = rootView.findViewById(R.id.srl_schedule);
            swipeRefreshLayout.setOnRefreshListener(this);
        }catch (Exception e){
            e.printStackTrace();
        }

        return rootView;
    }

    private void deleteSchedule(int position){
        ScheduleTask scheduleTask = scheduleTasks.get(position);
        int groupId = scheduleTask.getGroupId();
        MeshTools meshTools = MeshTools.getInstance();
        Group group = meshTools.getMeshNetworkLiveData().getMeshNetwork().getGroup(groupId);
        if (group==null){
            scheduleTaskDao.deleteScheduleTask(scheduleTask);
            scheduleTasks.remove(position);
            return;
        }
        else{
            AlarmSender.cancelAlarm(group,scheduleTask);
            scheduleTaskDao.deleteScheduleTask(scheduleTask);
            scheduleTasks.remove(position);
        }
        adapter.setData(scheduleTasks);
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onRefresh() {
        scheduleTasks = scheduleTaskDao.queryAllScheduleTask();
        adapter.setData(scheduleTasks);
        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }
}