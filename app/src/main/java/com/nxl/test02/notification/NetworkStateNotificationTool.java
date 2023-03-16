package com.nxl.test02.notification;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.nxl.test02.ContextUtil;
import com.nxl.test02.R;

public class NetworkStateNotificationTool {
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private static String channelId = "networkStateNotification";
    private NotificationChannel channel;
    private static int notifyID = 1;
    private String TAG = "NetworkStateNotificationTool";
    private static NetworkStateNotificationTool networkStateNotificationTool = null;
    private NetworkStateNotificationTool(){}
    public static NetworkStateNotificationTool getInstance(){
        if (networkStateNotificationTool ==null){
            networkStateNotificationTool = new NetworkStateNotificationTool();
        }
        ContextUtil context = ContextUtil.getInstance();
        if (networkStateNotificationTool.notificationManager==null){
            networkStateNotificationTool.notificationManager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
        }
        if (networkStateNotificationTool.notificationBuilder==null){
            networkStateNotificationTool.createNotificationChannel();
            networkStateNotificationTool.notificationBuilder = new NotificationCompat.Builder(context,channelId)
                    .setAutoCancel(false)
                    .setContentTitle("contentTitle")
                    .setContentText("contentText")
                    .setPriority(Notification.PRIORITY_MAX)
                    .setSmallIcon(R.mipmap.ic_launcher);
        }
        return networkStateNotificationTool;
    }

    public void updateNotification(String contentTitle,String contentText){
        if(notificationManager==null){
            Log.d(TAG, "没有初始化notificationManager");
            return;
        }
        if(notificationBuilder==null){
            Log.d(TAG, "没有初始化notificationBuilder");
            return;
        }
        notificationBuilder.setContentTitle(contentTitle)
                .setContentText(contentText);
        notificationManager.notify(notifyID,notificationBuilder.build());
    }

    @SuppressLint("WrongConstant")
    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "createNotificationChannel: ");
            String channelName = "连接状态";
            int importance = NotificationManager.IMPORTANCE_MAX;
            channel = new NotificationChannel(channelId,channelName,importance);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
