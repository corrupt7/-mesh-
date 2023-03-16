package com.nxl.test02.tools;

import android.os.ParcelUuid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class Utils {

    @Nullable
    public static byte[] getServiceData(@NonNull final ScanResult result, @NonNull final UUID serviceUuid) {
        final ScanRecord scanRecord = result.getScanRecord();
        if (scanRecord != null) {
            return scanRecord.getServiceData(new ParcelUuid((serviceUuid)));
        }
        return null;
    }
}
