package com.nxl.test02.room;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class MonitorLog {


    @PrimaryKey(autoGenerate = true)
    private int _id;
    private String time;
    private String taskName;
    private String isSucceed;
    private String monitorLogMessage;

    @Ignore
    public MonitorLog(){

    }

    public MonitorLog(String time, String taskName, String isSucceed, String monitorLogMessage) {
        this.time = time;
        this.taskName = taskName;
        this.isSucceed = isSucceed;
        this.monitorLogMessage = monitorLogMessage;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getIsSucceed() {
        return isSucceed;
    }

    public void setIsSucceed(String isSucceed) {
        this.isSucceed = isSucceed;
    }

    public String getMonitorLogMessage() {
        return monitorLogMessage;
    }

    public void setMonitorLogMessage(String monitorLogMessage) {
        this.monitorLogMessage = monitorLogMessage;
    }
}
