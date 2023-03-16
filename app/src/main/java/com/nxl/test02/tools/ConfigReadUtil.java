package com.nxl.test02.tools;

import android.content.Context;

import java.io.IOException;
import java.util.Properties;

public class ConfigReadUtil {
    private Properties properties;

    public String getProperty(Context c, String fileName, String key){
        properties = new Properties();
        try {
            properties.load(c.getAssets().open(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty(key);
    }

}
