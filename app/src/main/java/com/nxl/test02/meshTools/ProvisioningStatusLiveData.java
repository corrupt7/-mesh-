package com.nxl.test02.meshTools;


import java.util.ArrayList;

import androidx.lifecycle.LiveData;


import com.nxl.test02.bean.ProvisionerStates;
import com.nxl.test02.R;

public class ProvisioningStatusLiveData extends LiveData<ProvisioningStatusLiveData> {

    private final ArrayList<ProvisionerProgress> mProvisioningProgress = new ArrayList<>();

    public void clear() {
        mProvisioningProgress.clear();
        postValue(this);
    }

    public ArrayList<ProvisionerProgress> getStateList() {
        return mProvisioningProgress;
    }


    public ProvisionerProgress getProvisionerProgress() {
        if (mProvisioningProgress.size() == 0)
            return null;
        return mProvisioningProgress.get(mProvisioningProgress.size() - 1);
    }

    void onMeshNodeStateUpdated(final ProvisionerStates state) {
        final ProvisionerProgress provisioningProgress;
        switch (state) {
            case PROVISIONING_INVITE:
                provisioningProgress = new ProvisionerProgress(state, "Sending provisioning invite...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_CAPABILITIES:
                provisioningProgress = new ProvisionerProgress(state, "Provisioning capabilities received...", R.drawable.ic_arrow_back);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_START:
                provisioningProgress = new ProvisionerProgress(state, "Sending provisioning start...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_PUBLIC_KEY_SENT:
                provisioningProgress = new ProvisionerProgress(state, "Sending provisioning public key...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_PUBLIC_KEY_RECEIVED:
                provisioningProgress = new ProvisionerProgress(state, "Provisioning public key received...", R.drawable.ic_arrow_back);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_AUTHENTICATION_STATIC_OOB_WAITING:
            case PROVISIONING_AUTHENTICATION_OUTPUT_OOB_WAITING:
            case PROVISIONING_AUTHENTICATION_INPUT_OOB_WAITING:
                provisioningProgress = new ProvisionerProgress(state, "Waiting for user authentication input...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_AUTHENTICATION_INPUT_ENTERED:
                provisioningProgress = new ProvisionerProgress(state, "OOB authentication entered...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_INPUT_COMPLETE:
                provisioningProgress = new ProvisionerProgress(state, "Provisioning input complete received...", R.drawable.ic_arrow_back);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_CONFIRMATION_SENT:
                provisioningProgress = new ProvisionerProgress(state, "Sending provisioning confirmation...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_CONFIRMATION_RECEIVED:
                provisioningProgress = new ProvisionerProgress(state, "Provisioning confirmation received...", R.drawable.ic_arrow_back);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_RANDOM_SENT:
                provisioningProgress = new ProvisionerProgress(state, "Sending provisioning random...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_RANDOM_RECEIVED:
                provisioningProgress = new ProvisionerProgress(state, "Provisioning random received...", R.drawable.ic_arrow_back);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_DATA_SENT:
                provisioningProgress = new ProvisionerProgress(state, "Sending provisioning data...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_COMPLETE:
                provisioningProgress = new ProvisionerProgress(state, "Provisioning complete received...", R.drawable.ic_arrow_back);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONING_FAILED:
                provisioningProgress = new ProvisionerProgress(state, "Provisioning failed received...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
            default:
                break;
            case COMPOSITION_DATA_GET_SENT:
                provisioningProgress = new ProvisionerProgress(state, "Sending composition data get...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case COMPOSITION_DATA_STATUS_RECEIVED:
                provisioningProgress = new ProvisionerProgress(state, "Composition data status received...", R.drawable.ic_arrow_back);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case SENDING_DEFAULT_TTL_GET:
                provisioningProgress = new ProvisionerProgress(state, "Sending default TLL get...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case DEFAULT_TTL_STATUS_RECEIVED:
                provisioningProgress = new ProvisionerProgress(state, "Default TTL status received...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case SENDING_APP_KEY_ADD:
                provisioningProgress = new ProvisionerProgress(state, "Sending app key add...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case APP_KEY_STATUS_RECEIVED:
                provisioningProgress = new ProvisionerProgress(state, "App key status received...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case SENDING_NETWORK_TRANSMIT_SET:
                provisioningProgress = new ProvisionerProgress(state, "Sending network transmit set...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case NETWORK_TRANSMIT_STATUS_RECEIVED:
                provisioningProgress = new ProvisionerProgress(state, "Network transmit status received...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case SENDING_BLOCK_ACKNOWLEDGEMENT:
                provisioningProgress = new ProvisionerProgress(state, "Sending block acknowledgements", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case BLOCK_ACKNOWLEDGEMENT_RECEIVED:
                provisioningProgress = new ProvisionerProgress(state, "Receiving block acknowledgements", R.drawable.ic_arrow_back);
                mProvisioningProgress.add(provisioningProgress);
                break;
            case PROVISIONER_UNASSIGNED:
                provisioningProgress = new ProvisionerProgress(state, "Provisioner unassigned...", R.drawable.ic_arrow_forward);
                mProvisioningProgress.add(provisioningProgress);
                break;
        }
        postValue(this);
    }
}

