package com.nxl.test02.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.nxl.test02.ContextUtil;
import com.nxl.test02.R;
import com.nxl.test02.ScheduleTaskMonitorActivity;

public class ScheduleTaskNotificationTool {
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private static String channelId = "scheduleTaskNotification";
    private NotificationChannel channel;
    private int notifyID = 2;
    private String TAG = "ScheduleTaskNotificationTool";
    private Context context;
    private static ScheduleTaskNotificationTool scheduleTaskNotificationTool = null;
    private ScheduleTaskNotificationTool(){}

    public static ScheduleTaskNotificationTool getInstance(){
        if (scheduleTaskNotificationTool ==null){
            scheduleTaskNotificationTool = new ScheduleTaskNotificationTool();
        }
        ContextUtil context = ContextUtil.getInstance();
        if (scheduleTaskNotificationTool.notificationManager==null){
            scheduleTaskNotificationTool.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (scheduleTaskNotificationTool.context==null){
            scheduleTaskNotificationTool.context=context;
        }
        return scheduleTaskNotificationTool;
    }

//
//    private void setNotificationManager(NotificationManager notificationManager){
//        this.notificationManager = notificationManager;
//    }
//
//    private void setContext(Context context) {
//        this.context = context;
//    }
//
//
    public void setNotification(String contentTitle, String contentText){
        if(notificationManager==null){
            Log.d(TAG, "没有初始化notificationManager");
            return;
        }
        if (context==null){
            Log.d(TAG, "没有初始化context");
            return;
        }
        createNotificationChannel();

        Intent intent = new Intent(context, ScheduleTaskMonitorActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        notificationBuilder = new NotificationCompat.Builder(context,channelId)
                .setAutoCancel(true)
                .setAutoCancel(false)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher);
        notificationManager.notify(notifyID,notificationBuilder.build());
        notifyID++;
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
