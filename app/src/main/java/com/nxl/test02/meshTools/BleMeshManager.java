package com.nxl.test02.meshTools;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;

import dagger.hilt.android.qualifiers.ApplicationContext;
import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.callback.DataSentCallback;

@Singleton
public class BleMeshManager extends LoggableBleManager<BleMeshManagerCallbacks> {
    private static final int MTU_SIZE_DEFAULT = 23;
    private static final int MTU_SIZE_MAX = 517;

    /**
     * Mesh provisioning service UUID
     */
    public final static UUID MESH_PROVISIONING_UUID = UUID.fromString("00001827-0000-1000-8000-00805F9B34FB");
    /**
     * Mesh provisioning data in characteristic UUID
     */
    private final static UUID MESH_PROVISIONING_DATA_IN = UUID.fromString("00002ADB-0000-1000-8000-00805F9B34FB");
    /**
     * Mesh provisioning data out characteristic UUID
     */
    private final static UUID MESH_PROVISIONING_DATA_OUT = UUID.fromString("00002ADC-0000-1000-8000-00805F9B34FB");

    /**
     * Mesh provisioning service UUID
     */
    public final static UUID MESH_PROXY_UUID = UUID.fromString("00001828-0000-1000-8000-00805F9B34FB");

    /**
     * Mesh provisioning data in characteristic UUID
     */
    private final static UUID MESH_PROXY_DATA_IN = UUID.fromString("00002ADD-0000-1000-8000-00805F9B34FB");

    /**
     * Mesh provisioning data out characteristic UUID
     */
    private final static UUID MESH_PROXY_DATA_OUT = UUID.fromString("00002ADE-0000-1000-8000-00805F9B34FB");

    private BluetoothGattCharacteristic mMeshProvisioningDataInCharacteristic;
    private BluetoothGattCharacteristic mMeshProvisioningDataOutCharacteristic;
    private BluetoothGattCharacteristic mMeshProxyDataInCharacteristic;
    private BluetoothGattCharacteristic mMeshProxyDataOutCharacteristic;

    private static final String TAG = BleMeshManager.class.getSimpleName();
    private boolean isProvisioningComplete;
    private boolean mIsDeviceReady;
    private boolean mNodeReset;

    private class BleMeshGattCallbacks extends BleManager.BleManagerGattCallback {

        @Override
        public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            final BluetoothGattService meshProxyService = gatt.getService(MESH_PROXY_UUID);
            Log.d(TAG, "进入了isRequiredServiceSupported: ");

            if (meshProxyService != null) {
                isProvisioningComplete = true;
                mMeshProxyDataInCharacteristic = meshProxyService.getCharacteristic(MESH_PROXY_DATA_IN);
                mMeshProxyDataOutCharacteristic = meshProxyService.getCharacteristic(MESH_PROXY_DATA_OUT);

                return mMeshProxyDataInCharacteristic != null &&
                        mMeshProxyDataOutCharacteristic != null &&
                        hasNotifyProperty(mMeshProxyDataOutCharacteristic) &&
                        hasWriteNoResponseProperty(mMeshProxyDataInCharacteristic);
            }
            final BluetoothGattService meshProvisioningService = gatt.getService(MESH_PROVISIONING_UUID);
            if (meshProvisioningService != null) {
                isProvisioningComplete = false;
                mMeshProvisioningDataInCharacteristic = meshProvisioningService.getCharacteristic(MESH_PROVISIONING_DATA_IN);
                mMeshProvisioningDataOutCharacteristic = meshProvisioningService.getCharacteristic(MESH_PROVISIONING_DATA_OUT);

                return mMeshProvisioningDataInCharacteristic != null &&
                        mMeshProvisioningDataOutCharacteristic != null &&
                        hasNotifyProperty(mMeshProvisioningDataOutCharacteristic) &&
                        hasWriteNoResponseProperty(mMeshProvisioningDataInCharacteristic);
            }
            return false;
        }

        @Override
        protected void initialize() {
            Log.d("TAG", "进入了initialize: ");
            requestMtu(MTU_SIZE_MAX).enqueue();

            final DataReceivedCallback onDataReceived = (device, data) ->
                    mCallbacks.onDataReceived(device, getMaximumPacketSize(), data.getValue());

            final BluetoothGattCharacteristic characteristic = isProvisioningComplete ?
                    mMeshProxyDataOutCharacteristic : mMeshProvisioningDataOutCharacteristic;
            setNotificationCallback(characteristic).with(onDataReceived);
            enableNotifications(characteristic).enqueue();
        }

        @Override
        protected void onDeviceDisconnected() {
            overrideMtu(MTU_SIZE_DEFAULT);
            mIsDeviceReady = false;
            isProvisioningComplete = false;
            mMeshProvisioningDataInCharacteristic = null;
            mMeshProvisioningDataOutCharacteristic = null;
            mMeshProxyDataInCharacteristic = null;
            mMeshProxyDataOutCharacteristic = null;
        }

        @Override
        protected void onDeviceReady() {
            mIsDeviceReady = true;
            super.onDeviceReady();
        }

        @Override
        protected void onServicesInvalidated() {
            overrideMtu(MTU_SIZE_DEFAULT);
            mIsDeviceReady = false;
            isProvisioningComplete = false;
            mMeshProvisioningDataInCharacteristic = null;
            mMeshProvisioningDataOutCharacteristic = null;
            mMeshProxyDataInCharacteristic = null;
            mMeshProxyDataOutCharacteristic = null;
        }
    }

    @Inject
    public BleMeshManager(@ApplicationContext final Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected BleManager.BleManagerGattCallback getGattCallback() {
        return new BleMeshGattCallbacks();
    }

    @Override
    protected boolean shouldClearCacheWhenDisconnected() {
        final boolean result = !isProvisioningComplete || mNodeReset;
        mNodeReset = false;
        return result;
    }

    public void setClearCacheRequired() {
        mNodeReset = true;
    }

    public void sendPdu(final byte[] pdu) {
        if (!mIsDeviceReady)
            return;

        final DataSentCallback callback = (device, data) ->
                mCallbacks.onDataSent(device, getMaximumPacketSize(), data.getValue());

        final BluetoothGattCharacteristic characteristic = isProvisioningComplete ?
                mMeshProxyDataInCharacteristic : mMeshProvisioningDataInCharacteristic;

            Log.d("TAG", "isProvisioningComplete:"+isProvisioningComplete);

        writeCharacteristic(characteristic, pdu, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
                .split()
                .with(callback)
                .enqueue();
    }

    public int getMaximumPacketSize() {
        return super.getMtu() - 3;
    }

    public boolean isProvisioningComplete() {
        return isProvisioningComplete;
    }

    public boolean isDeviceReady() {
        return mIsDeviceReady;
    }

    private boolean hasWriteNoResponseProperty(@NonNull final BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0;
    }

    private boolean hasNotifyProperty(@NonNull final BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }
}
