package com.nxl.test02.tools;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlueToothUtil {


    private BluetoothAdapter bluetoothAdapter;

    public BlueToothUtil() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * 判断设备是否支持
     * @return
     */
    public boolean isSupportBlueTooth() {
        if (bluetoothAdapter != null) {
            return true;
        }
        return false;
    }

    /**
     * 返回蓝牙状态
     * @return
     */
    public boolean getBlueToothStatus() {
        assert bluetoothAdapter != null;
        return bluetoothAdapter.isEnabled();
    }


    /**
     * 打开蓝牙
     * @param activity
     * @param requestCode
     * @param context
     */
    @SuppressLint("MissingPermission")
    public void turnOnBlueTooth(Activity activity, int requestCode, Context context) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查找设备
     * @param
     */
    public void findDevice(Context context, Activity activity) {
        assert bluetoothAdapter != null;
        Log.d("1", "9999");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.BLUETOOTH_SCAN,}, 1);
            }
        }
        try {
            if (bluetoothAdapter.isDiscovering()) {
                return;
            } else {
                bluetoothAdapter.startDiscovery();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 取消搜索设备
     */
    public void cancelFindDevice(Context context, Activity activity) {
        assert bluetoothAdapter != null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.BLUETOOTH_SCAN,}, 1);
            }
        }
        try {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            } else {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取绑定设备
     */
    @SuppressLint("MissingPermission")
    public List<BluetoothDevice> getBondedDeviceList(Context context) {

        try {
            return new ArrayList<>(bluetoothAdapter.getBondedDevices());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(bluetoothAdapter.getBondedDevices());
    }

    /**
     * 设备本设备为可见状态
     */
    @SuppressLint("MissingPermission")
    public void setBluetoothAvailable(Activity activity) {
        assert bluetoothAdapter != null;
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            activity.startActivity(discoverableIntent);
        }
    }

}
