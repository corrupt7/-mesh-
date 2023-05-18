package com.nxl.test02.bean;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;

import no.nordicsemi.android.mesh.MeshBeacon;

@SuppressLint("ParcelCreator")
public class DeviceBean implements Parcelable {
    private BluetoothDevice bluetoothDevice;
    private String address;
    private String name;
    private BluetoothGatt gatt;
    private ScanResult scanResult;
    private MeshBeacon beacon;
    private int rssi;

    public DeviceBean(BluetoothDevice bluetoothDevice, String address, String name) {
        this.bluetoothDevice = bluetoothDevice;
        this.address = address;
        this.name = name;
    }

    public DeviceBean(BluetoothDevice bluetoothDevice, String address, String name, BluetoothGatt gatt, ScanResult scanResult, MeshBeacon beacon) {
        this.bluetoothDevice = bluetoothDevice;
        this.address = address;
        this.name = name;
        this.gatt = gatt;
        this.scanResult = scanResult;
        this.beacon = beacon;
    }

    public DeviceBean() {
    }
    public DeviceBean(ScanResult scanResult){
        this.scanResult = scanResult;
        this.bluetoothDevice = scanResult.getDevice();
        final ScanRecord scanRecord = scanResult.getScanRecord();
        if(scanRecord != null) {
            this.name = scanRecord.getDeviceName();
        }
        this.rssi = scanResult.getRssi();
    }


    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public BluetoothGatt getGatt() {
        return gatt;
    }

    public void setGatt(BluetoothGatt gatt) {
        this.gatt = gatt;
    }


    public ScanResult getScanResult() {
        return scanResult;
    }

    public void setScanResult(ScanResult scanResult) {
        this.scanResult = scanResult;
    }

    public MeshBeacon getBeacon() {
        return beacon;
    }

    public void setBeacon(MeshBeacon beacon) {
        this.beacon = beacon;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(scanResult,i);
        parcel.writeParcelable(bluetoothDevice,i);
        parcel.writeString(address);
        parcel.writeString(name);
        parcel.writeParcelable(beacon,i);
    }


}
