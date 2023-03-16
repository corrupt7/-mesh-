package com.nxl.test02.room;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ScheduleTask {

    @PrimaryKey(autoGenerate = true)
    private int _id;
    private String taskName;
    private int groupId;
    private String action;
    private int hour;
    private int minute;

    public ScheduleTask(String taskName, int groupId,String action,int hour,int minute) {
        this.taskName = taskName;
        this.groupId = groupId;
        this.action = action;
        this.hour = hour;
        this.minute = minute;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }
}
