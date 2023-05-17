package com.nxl.test02;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nxl.test02.meshTools.MeshTools;
import com.nxl.test02.adapter.FragmentPagerAdapter;
import com.nxl.test02.notification.NetworkStateNotificationTool;
import com.nxl.test02.notification.ScheduleTaskNotificationTool;

import java.util.ArrayList;
import java.util.List;

public class InterfaceActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "InterfaceActivity";
    private List<Fragment> fragmentList;
    private ViewPager2 viewPager;
    private LinearLayout llEquipment,llGroup,llUser,llSchedule;
    private ImageView ivEquipment,ivGroup,ivUser,ivCurrent,ivSchedule;
    private Bundle bundle;
    private MeshTools meshTools;

    public MeshTools getMeshTools() {
        return meshTools;
    }

    public void setMeshTools(MeshTools meshTools) {
        this.meshTools = meshTools;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().hide();
        setContentView(R.layout.activity_interface);
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        bundle = new Bundle();
        bundle.putString("username",username);
        //初始化页面，加入4个fragment
        initViewPager();
        //获取底部组件
        initTabView();
        //状态栏通知
        initNotification();
        setTitle("设备管理");
    }


    private void initNotification(){
        NetworkStateNotificationTool networkStateNotificationTool = NetworkStateNotificationTool.getInstance();
        networkStateNotificationTool.updateNotification(getString(R.string.connect_state),getString(R.string.not_connect));

    }


    private void initViewPager(){
        viewPager = findViewById(R.id.id_viewpager);
        fragmentList = new ArrayList<>();
        fragmentList.add(new BlueToothFragment());
        GroupFragment groupFragment = new GroupFragment();
        groupFragment.setArguments(bundle);
        fragmentList.add(groupFragment);
        fragmentList.add(new ScheduleFragment());
        UserFragment userFragment = new UserFragment();
        userFragment.setArguments(bundle);
        fragmentList.add(userFragment);
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager(),getLifecycle(),fragmentList);
        viewPager.setAdapter(adapter);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                changeTabByPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
    }

    private void initTabView(){
        llEquipment = findViewById(R.id.tab_equipment);
        llEquipment.setOnClickListener(this);
        llGroup = findViewById(R.id.tab_group);
        llGroup.setOnClickListener(this);
        llUser = findViewById(R.id.tab_user);
        llUser.setOnClickListener(this);
        llSchedule = findViewById(R.id.tab_schedule);
        llSchedule.setOnClickListener(this);
        ivEquipment = findViewById(R.id.pic_equipment);
        ivGroup = findViewById(R.id.pic_group);
        ivUser = findViewById(R.id.pic_user);
        ivSchedule = findViewById(R.id.pic_schedule);
        ivEquipment.setSelected(true);
        ivCurrent = ivEquipment;

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        changeTabByID(id);
    }

    private void changeTabByPosition(int position) {
        ivCurrent.setSelected(false);
        switch(position){
            case 0:
                ivEquipment.setSelected(true);
                ivCurrent=ivEquipment;
                setTitle("设备管理");
                break;
            case 1:
                ivGroup.setSelected(true);
                ivCurrent=ivGroup;
                setTitle("设备分组");
                break;
            case 2:
                ivSchedule.setSelected(true);
                ivCurrent=ivSchedule;
                setTitle("定时任务");
                break;
            case 3:
                ivUser.setSelected(true);
                ivCurrent=ivUser;
                setTitle("我的");
                break;
        }
    }


    private void changeTabByID(int id) {
        ivCurrent.setSelected(false);
        switch(id){
            case R.id.tab_equipment:
                viewPager.setCurrentItem(0);
                ivEquipment.setSelected(true);
                ivCurrent=ivEquipment;
                setTitle("设备管理");
                break;
            case R.id.tab_group:
                viewPager.setCurrentItem(1);
                ivGroup.setSelected(true);
                ivCurrent=ivGroup;
                setTitle("设备分组");
                break;
            case R.id.tab_schedule:
                viewPager.setCurrentItem(2);
                ivSchedule.setSelected(true);
                ivCurrent=ivSchedule;
                setTitle("定时任务");
                break;
            case R.id.tab_user:
                viewPager.setCurrentItem(3);
                ivUser.setSelected(true);
                ivCurrent=ivUser;
                setTitle("我的");
                break;
        }
    }

}