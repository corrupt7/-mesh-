package com.nxl.test02.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ScheduleTaskDao {

    @Insert
    void insertScheduleTask(ScheduleTask ... scheduleTasks);

    @Delete
    void deleteScheduleTask(ScheduleTask ... scheduleTasks);

    @Update
    void updateScheduleTask(ScheduleTask ... scheduleTasks);

    @Query("delete from scheduletask")
    void deleteAllScheduleTask();

    @Query("select * from scheduletask")
    List<ScheduleTask> queryAllScheduleTask();

    @Query("select * from scheduletask where _id = :id")
    List<ScheduleTask> queryScheduleTaskByID(int id);


    @Query("select * from scheduletask where groupId = :groupId and `action` = :action")
    List<ScheduleTask> queryScheduleTaskByGroupAndAction(int groupId,String action);


}
