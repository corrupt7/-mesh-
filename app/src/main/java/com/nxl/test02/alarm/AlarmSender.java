package com.nxl.test02.alarm;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import com.nxl.test02.ContextUtil;
import com.nxl.test02.room.ScheduleTask;
import com.nxl.test02.tools.PendingIntentUtil;

import java.util.List;

import no.nordicsemi.android.mesh.Group;

public class AlarmSender {
    /**
     * 启动一个定时任务
     * @param group
     * @param status
     * @param name
     * @param time
     */
    @SuppressLint("NewApi")
    public static void setAlarm(Group group, int status, String name, long time){
        Context context = ContextUtil.getInstance();
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent mIntent = new Intent(context,AlarmReceiver.class);
        mIntent.putExtra("taskName",name);
        PendingIntent pendingIntent;
        if(status==1){
            pendingIntent = PendingIntentUtil.getPendingIntent(group,context,mIntent,AlarmReceiver.OPEN_DEVICE_ACTION);
        }else {
            pendingIntent = PendingIntentUtil.getPendingIntent(group,context,mIntent,AlarmReceiver.CLOSE_DEVICE_ACTION);
        }
        if (time == 0){
            am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime()+10*1000,pendingIntent);
        }
        else {
            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+time,24*60*60*1000,pendingIntent);
        }
    }

    /**
     * 关闭一个定时任务
     * @param group
     * @param scheduleTask
     */
    public static void cancelAlarm(Group group, ScheduleTask scheduleTask){
        Context context = ContextUtil.getInstance();
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent mIntent = new Intent(context,AlarmReceiver.class);
        mIntent.putExtra("taskName",scheduleTask.getTaskName());
        PendingIntent pendingIntent = PendingIntentUtil.getPendingIntent(group,context,mIntent,scheduleTask.getAction());
        am.cancel(pendingIntent);
    }
}
