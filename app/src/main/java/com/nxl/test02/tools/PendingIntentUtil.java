package com.nxl.test02.tools;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import no.nordicsemi.android.mesh.Group;


public class PendingIntentUtil {
    /**
     * PendingIntent帮助类，储存group的id和广播的action
     * @param group
     * @param context
     * @return
     */
    public static PendingIntent getPendingIntent(Group group, Context context, Intent intent, String action){
        PendingIntent pendingIntent;
        intent.setAction(action);
        intent.putExtra("groupId",group.getAddress());
        pendingIntent = PendingIntent.getBroadcast(context,group.hashCode(),intent,PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }
}
