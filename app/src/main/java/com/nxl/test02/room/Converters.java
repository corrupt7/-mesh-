package com.nxl.test02.room;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nxl.test02.bean.DeviceBean;

import java.lang.reflect.Type;
import java.util.List;

public class Converters {


    @TypeConverter
    public String objectToString(List<DeviceBean> list) {
        return new Gson().toJson(list);
    }

    @TypeConverter
    public List<DeviceBean> stringToObject(String json) {
        Type listType = new TypeToken<List<DeviceBean>>(){}.getType();
        return new Gson().fromJson(json, listType);
    }

}
