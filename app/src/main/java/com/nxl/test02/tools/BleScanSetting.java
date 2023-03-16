package com.nxl.test02.tools;

import android.annotation.SuppressLint;
import android.os.ParcelUuid;

import com.nxl.test02.meshTools.BleMeshManager;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class BleScanSetting {



    public static List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilterList = new ArrayList<>();
        ScanFilter.Builder scanFilterBuilder = new ScanFilter.Builder();
        ParcelUuid parcelUuid = new ParcelUuid((BleMeshManager.MESH_PROVISIONING_UUID));
        scanFilterBuilder.setServiceUuid(parcelUuid, null);
        scanFilterList.add(scanFilterBuilder.build());
        return scanFilterList;
    }

    public static List<ScanFilter> buildRepeatScanFilters(){
        List<ScanFilter> scanFilterList = new ArrayList<>();
        ScanFilter.Builder scanFilterBuilder = new ScanFilter.Builder();
        ParcelUuid parcelUuid = new ParcelUuid((BleMeshManager.MESH_PROXY_UUID));
        scanFilterBuilder.setServiceUuid(parcelUuid, null);
        scanFilterList.add(scanFilterBuilder.build());
        return scanFilterList;
    }

    @SuppressLint("NewApi")
    public static ScanSettings buildScanSettings() {
        ScanSettings.Builder scanSettingBuilder = new ScanSettings.Builder();
        scanSettingBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        scanSettingBuilder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
        scanSettingBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
        return scanSettingBuilder.build();
    }


}
