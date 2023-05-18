package com.nxl.test02.room;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MonitorLogDao {

    @Insert
    void insertMonitorLog(MonitorLog ... monitorLog);

    @Delete
    void deleteMonitorLog(MonitorLog ... monitorLog);

    @Query("DELETE from monitorlog")
    void deleteAllMonitorLog();


    @Query("select * from monitorlog order by _id desc")
    List<MonitorLog> queryAllMonitorLog();

    @Query("select * from monitorlog where _id = :id")
    List<MonitorLog> queryMonitorLogByID(int id);



}
