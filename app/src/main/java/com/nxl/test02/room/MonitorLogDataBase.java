package com.nxl.test02.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {MonitorLog.class},version = 1,exportSchema = false)
public abstract class MonitorLogDataBase extends RoomDatabase {
    public abstract MonitorLogDao getMonitorLogDao();
}
