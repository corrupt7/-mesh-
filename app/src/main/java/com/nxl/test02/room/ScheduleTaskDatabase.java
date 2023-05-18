package com.nxl.test02.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ScheduleTask.class},version = 1,exportSchema = false)
public abstract class ScheduleTaskDatabase extends RoomDatabase {
    public abstract ScheduleTaskDao getScheduleTaskDao();

}
