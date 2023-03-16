package com.nxl.test02;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nxl.test02.meshTools.MeshTools;
import com.nxl.test02.tools.ToastUtils;

public class UserFragment extends Fragment {
    private static final String TAG = "UserFragment";

    private View rootView;
    private LinearLayout upgradePWD,scheduleMonitor,nodeControl;
    TextView userNameTextView;
    private String userName;


    public UserFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView ==null){
            rootView = inflater.inflate(R.layout.fragment_user,container,false);
        }
        try {
            initView();
            initData();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return rootView;
    }


    private void initData(){
        userNameTextView = rootView.findViewById(R.id.welcome_user);
        Bundle bundle = getArguments();
        try {
            userName = bundle.getString("username");
        }
        catch (Exception e){
            e.printStackTrace();
            userName="默认用户";
        }
        userNameTextView.setText(userName);


    }

    private void initView(){
        upgradePWD = rootView.findViewById(R.id.upgrade_password);
        scheduleMonitor = rootView.findViewById(R.id.schedule_monitor);
        nodeControl = rootView.findViewById(R.id.node_control);
        if(upgradePWD!=null){
            upgradePWD.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ToastUtils.show(getActivity(),"点击了修改密码");
                }
            });
        }
        else {
            Log.d(TAG, "找不到这个LinearLayout");
        }

        scheduleMonitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), ScheduleTaskMonitorActivity.class);
                startActivity(intent);
            }
        });


        nodeControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MeshTools meshTools = MeshTools.getInstance();
                if (meshTools==null){
                    ToastUtils.show(getActivity(),"系统内部错误");
                    return;
                }
                if (!meshTools.isConnectedToProxy().getValue()){
                    ToastUtils.show(getActivity(),"需要先连接至组网");
                    return;
                }
                Intent intent = new Intent();
                intent.setClass(getActivity(),NodeActivity.class);
                requireActivity().startActivity(intent);
            }
        });
    }

}