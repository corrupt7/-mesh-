package com.nxl.test02.meshTools;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.Group;
import no.nordicsemi.android.mesh.MeshManagerApi;
import no.nordicsemi.android.mesh.MeshManagerCallbacks;
import no.nordicsemi.android.mesh.MeshNetwork;
import no.nordicsemi.android.mesh.MeshProvisioningStatusCallbacks;
import no.nordicsemi.android.mesh.MeshStatusCallbacks;
import no.nordicsemi.android.mesh.NetworkKey;
import no.nordicsemi.android.mesh.Provisioner;
import no.nordicsemi.android.mesh.UnprovisionedBeacon;
import no.nordicsemi.android.mesh.models.SigModelParser;
import no.nordicsemi.android.mesh.opcodes.ProxyConfigMessageOpCodes;
import no.nordicsemi.android.mesh.provisionerstates.ProvisioningState;
import no.nordicsemi.android.mesh.provisionerstates.UnprovisionedMeshNode;
import no.nordicsemi.android.mesh.transport.ConfigAppKeyAdd;
import no.nordicsemi.android.mesh.transport.ConfigAppKeyStatus;
import no.nordicsemi.android.mesh.transport.ConfigCompositionDataGet;
import no.nordicsemi.android.mesh.transport.ConfigDefaultTtlGet;
import no.nordicsemi.android.mesh.transport.ConfigDefaultTtlStatus;
import no.nordicsemi.android.mesh.transport.ConfigModelAppStatus;
import no.nordicsemi.android.mesh.transport.ConfigModelPublicationStatus;
import no.nordicsemi.android.mesh.transport.ConfigModelSubscriptionStatus;
import no.nordicsemi.android.mesh.transport.ConfigNetworkTransmitSet;
import no.nordicsemi.android.mesh.transport.ConfigNetworkTransmitStatus;
import no.nordicsemi.android.mesh.transport.ConfigNodeResetStatus;
import no.nordicsemi.android.mesh.transport.ConfigProxyStatus;
import no.nordicsemi.android.mesh.transport.ConfigRelayStatus;
import no.nordicsemi.android.mesh.transport.ControlMessage;
import no.nordicsemi.android.mesh.transport.Element;
import no.nordicsemi.android.mesh.transport.GenericLevelStatus;
import no.nordicsemi.android.mesh.transport.GenericOnOffStatus;
import no.nordicsemi.android.mesh.transport.MeshMessage;
import no.nordicsemi.android.mesh.transport.MeshModel;
import no.nordicsemi.android.mesh.transport.ProvisionedMeshNode;
import no.nordicsemi.android.mesh.transport.ProxyConfigFilterStatus;
import no.nordicsemi.android.mesh.transport.SceneRegisterStatus;
import no.nordicsemi.android.mesh.transport.SceneStatus;
import no.nordicsemi.android.mesh.transport.VendorModelMessageStatus;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import static no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.GENERIC_LEVEL_STATUS;
import static no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.GENERIC_ON_OFF_STATUS;
import static no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.SCENE_REGISTER_STATUS;
import static no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes.SCENE_STATUS;
import static no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_APPKEY_STATUS;
import static no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_COMPOSITION_DATA_STATUS;
import static no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_DEFAULT_TTL_STATUS;
import static no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_GATT_PROXY_STATUS;
import static no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_HEARTBEAT_PUBLICATION_STATUS;
import static no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_HEARTBEAT_SUBSCRIPTION_STATUS;
import static no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_MODEL_APP_STATUS;
import static no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_MODEL_PUBLICATION_STATUS;
import static no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_MODEL_SUBSCRIPTION_STATUS;
import static no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_NETWORK_TRANSMIT_STATUS;
import static no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_NODE_RESET_STATUS;
import static no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes.CONFIG_RELAY_STATUS;

import com.google.android.material.internal.ContextUtils;
import com.nxl.test02.ContextUtil;
import com.nxl.test02.bean.ExtendedBluetoothDevice;
import com.nxl.test02.bean.ProvisionerStates;
import com.nxl.test02.notification.NetworkStateNotificationTool;
import com.nxl.test02.tools.Utils;

@Singleton
public class MeshTools implements MeshProvisioningStatusCallbacks, MeshStatusCallbacks, MeshManagerCallbacks, BleMeshManagerCallbacks {

    private static final String TAG = MeshTools.class.getSimpleName();
    private static final int ATTENTION_TIMER = 5;
    static final String EXPORT_PATH = Environment.getExternalStorageDirectory() + File.separator +
            "Nordic Semiconductor" + File.separator + "nRF Mesh" + File.separator;
    private static final String EXPORTED_PATH = "sdcard" + File.separator + "Nordic Semiconductor" + File.separator + "nRF Mesh" + File.separator;

    private final MutableLiveData<Boolean> mIsConnectedToProxy = new MutableLiveData<>();
    private final NetworkStateNotificationTool networkStateNotificationTool = NetworkStateNotificationTool.getInstance();

    private MutableLiveData<Boolean> mIsConnected;

    private final MutableLiveData<Void> mOnDeviceReady = new MutableLiveData<>();

    private final MutableLiveData<String> mConnectionState = new MutableLiveData<>();

    private final SingleLiveEvent<Boolean> mIsReconnecting = new SingleLiveEvent<>();
    private final MutableLiveData<UnprovisionedMeshNode> mUnprovisionedMeshNodeLiveData = new MutableLiveData<>();
    private final MutableLiveData<ProvisionedMeshNode> mProvisionedMeshNodeLiveData = new MutableLiveData<>();
    private final SingleLiveEvent<Integer> mConnectedProxyAddress = new SingleLiveEvent<>();

    private boolean mIsProvisioningComplete = false;

    private final MutableLiveData<ProvisionedMeshNode> mExtendedMeshNode = new MutableLiveData<>();

    private final MutableLiveData<Element> mSelectedElement = new MutableLiveData<>();

    private final MutableLiveData<MeshModel> mSelectedModel = new MutableLiveData<>();
    private final MutableLiveData<ApplicationKey> mSelectedAppKey = new MutableLiveData<>();
    private final MutableLiveData<Provisioner> mSelectedProvisioner = new MutableLiveData<>();

    private final MutableLiveData<Group> mSelectedGroupLiveData = new MutableLiveData<>();

    private final MeshNetworkLiveData mMeshNetworkLiveData = new MeshNetworkLiveData();
    private final SingleLiveEvent<String> mNetworkImportState = new SingleLiveEvent<>();
    private final SingleLiveEvent<MeshMessage> mMeshMessageLiveData = new SingleLiveEvent<>();
    private final MutableLiveData<List<ProvisionedMeshNode>> mProvisionedNodes = new MutableLiveData<>();

    private final MutableLiveData<List<Group>> mGroups = new MutableLiveData<>();

    private final MutableLiveData<TransactionStatus> mTransactionStatus = new SingleLiveEvent<>();

    private final MutableLiveData<Boolean> mSentGroupMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsSentGroupMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mSentScheduleMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsSentScheduleMessage = new MutableLiveData<>();


    private final MeshManagerApi mMeshManagerApi;
    private BleMeshManager mBleMeshManager;
    private final Handler mHandler;
    private UnprovisionedMeshNode mUnprovisionedMeshNode;
    private ProvisionedMeshNode mProvisionedMeshNode;
    private boolean mIsReconnectingFlag;
    private boolean mIsScanning;
    private boolean mSetupProvisionedNode;
    private ProvisioningStatusLiveData mProvisioningStateLiveData;
    private MeshNetwork mMeshNetwork;
    private boolean mIsCompositionDataReceived;
    private boolean mIsDefaultTtlReceived;
    private boolean mIsAppKeyAddCompleted;
    private boolean mIsNetworkRetransmitSetCompleted;

    private final Runnable mReconnectRunnable = this::startScan;

    private final Runnable mScannerTimeout = () -> {
        stopScan();
        mIsReconnecting.postValue(false);
    };


   // @Inject
    private MeshTools(final MeshManagerApi meshManagerApi,
                     final BleMeshManager bleMeshManager) {
        mMeshManagerApi = meshManagerApi;
        mMeshManagerApi.setMeshManagerCallbacks(this);
        mMeshManagerApi.setProvisioningStatusCallbacks(this);
        mMeshManagerApi.setMeshStatusCallbacks(this);
        mMeshManagerApi.loadMeshNetwork();
        mBleMeshManager = bleMeshManager;
        mBleMeshManager.setGattCallbacks(this);
        mSentGroupMessage.postValue(false);
        mIsSentGroupMessage.postValue(false);
        mSentScheduleMessage.postValue(false);
        mIsSentScheduleMessage.postValue(false);
        mIsConnectedToProxy.postValue(false);
        networkStateNotificationTool.updateNotification("连接状态","未连接至mesh网络");
        mHandler = new Handler(Looper.getMainLooper());
    }
    private static MeshTools meshTools = null;

    public static MeshTools getInstance(){
        if(meshTools==null){
            Context c = ContextUtil.getInstance();
            MeshManagerApi mMeshManagerApi = new MeshManagerApi(c);
            BleMeshManager mBleMeshManager = new BleMeshManager(c);
            meshTools=new MeshTools(mMeshManagerApi,mBleMeshManager);
        }
        return meshTools;
    }


    void clearInstance() {
        mBleMeshManager = null;
    }

    public MutableLiveData<Boolean> getmSentScheduleMessage() {
        return mSentScheduleMessage;
    }

    public MutableLiveData<Boolean> getmIsSentScheduleMessage() {
        return mIsSentScheduleMessage;
    }

    public MutableLiveData<Boolean> getmSentGroupMessage() {
        return mSentGroupMessage;
    }

    public MutableLiveData<Boolean> getmIsSentGroupMessage() {
        return mIsSentGroupMessage;
    }

    /**
     * Returns {@link SingleLiveEvent} containing the device ready state.
     */
    public LiveData<Void> isDeviceReady() {
        return mOnDeviceReady;
    }

    /**
     * Returns {@link SingleLiveEvent} containing the device ready state.
     */
    public LiveData<String> getConnectionState() {
        return mConnectionState;
    }

    /**
     * Returns {@link SingleLiveEvent} containing the device ready state.
     */
    public LiveData<Boolean> isConnected() {
        return mIsConnected;
    }

    /**
     * Returns {@link SingleLiveEvent} containing the device ready state.
     */
    public LiveData<Boolean> isConnectedToProxy() {
        return mIsConnectedToProxy;
    }

    public LiveData<Boolean> isReconnecting() {
        return mIsReconnecting;
    }

    public boolean isProvisioningComplete() {
        return mIsProvisioningComplete;
    }

    public boolean isCompositionDataStatusReceived() {
        return mIsCompositionDataReceived;
    }

    public boolean isDefaultTtlReceived() {
        return mIsDefaultTtlReceived;
    }

    public boolean isAppKeyAddCompleted() {
        return mIsAppKeyAddCompleted;
    }

    public boolean isNetworkRetransmitSetCompleted() {
        return mIsNetworkRetransmitSetCompleted;
    }

    public final MeshNetworkLiveData getMeshNetworkLiveData() {
        return mMeshNetworkLiveData;
    }

    public LiveData<List<ProvisionedMeshNode>> getNodes() {
        return mProvisionedNodes;
    }

    public LiveData<List<Group>> getGroups() {
        return mGroups;
    }

    public LiveData<String> getNetworkLoadState() {
        return mNetworkImportState;
    }

    public ProvisioningStatusLiveData getProvisioningState() {
        return mProvisioningStateLiveData;
    }

    public LiveData<TransactionStatus> getTransactionStatus() {
        return mTransactionStatus;
    }

    /**
     * Clears the transaction status
     */
    void clearTransactionStatus() {
        if (mTransactionStatus.getValue() != null) {
            mTransactionStatus.postValue(null);
        }
    }

    /**
     * Returns the mesh manager api
     *
     * @return {@link MeshManagerApi}
     */
    public MeshManagerApi getMeshManagerApi() {
        return mMeshManagerApi;
    }

    /**
     * Returns the ble mesh manager
     *
     * @return {@link BleMeshManager}
     */
    public BleMeshManager getBleMeshManager() {
        return mBleMeshManager;
    }

    public LiveData<MeshMessage> getMeshMessageLiveData() {
        return mMeshMessageLiveData;
    }

    public LiveData<Group> getSelectedGroup() {
        return mSelectedGroupLiveData;
    }

    /**
     * Reset mesh network
     */
    public void resetMeshNetwork() {
        disconnect();
        mMeshManagerApi.resetMeshNetwork();
    }


    @SuppressLint("MissingPermission")
    public void connect(final Context context, final ExtendedBluetoothDevice device, final boolean connectToNetwork) {
        mMeshNetworkLiveData.setNodeName(device.getName());
        mIsProvisioningComplete = false;
        mIsCompositionDataReceived = false;
        mIsDefaultTtlReceived = false;
        mIsAppKeyAddCompleted = false;
        mIsNetworkRetransmitSetCompleted = false;
        final LogSession logSession = Logger.newSession(context, null, device.getAddress(), device.getName());
        mBleMeshManager.setLogger(logSession);
        initIsConnectedLiveData(connectToNetwork);
        mConnectionState.postValue("Connecting....");
        Log.d("AA", "Connect issued");
        mBleMeshManager.connect(device.getDevice()).retry(3, 20).enqueue();
    }

    /**
     * Connect to peripheral
     *
     * @param device bluetooth device
     */
    private void connectToProxy(final ExtendedBluetoothDevice device) {
        initIsConnectedLiveData(true);
        mConnectionState.postValue("Connecting....");
        mBleMeshManager.connect(device.getDevice()).retry(3, 200).enqueue();
    }

    private void initIsConnectedLiveData(final boolean connectToNetwork) {
        if (connectToNetwork) {
            mIsConnected = new SingleLiveEvent<>();
        } else {
            mIsConnected = new MutableLiveData<>();
        }
    }

    /**
     * Disconnects from peripheral
     */
    public void disconnect() {
        clearProvisioningLiveData();
        mIsProvisioningComplete = false;
        mBleMeshManager.disconnect().enqueue();
    }

    void clearProvisioningLiveData() {
        stopScan();
        mHandler.removeCallbacks(mReconnectRunnable);
        mSetupProvisionedNode = false;
        mIsReconnectingFlag = false;
        mUnprovisionedMeshNodeLiveData.setValue(null);
        mProvisionedMeshNodeLiveData.setValue(null);
    }

    private void removeCallbacks() {
        mHandler.removeCallbacksAndMessages(null);
    }

    public void identifyNode(final ExtendedBluetoothDevice device) {
        final UnprovisionedBeacon beacon = (UnprovisionedBeacon) device.getBeacon();
        if (beacon != null) {
            mMeshManagerApi.identifyNode(beacon.getUuid(), ATTENTION_TIMER);
        } else {
            final byte[] serviceData = Utils.getServiceData(device.getScanResult(), BleMeshManager.MESH_PROVISIONING_UUID);
            if (serviceData != null) {
                final UUID uuid = mMeshManagerApi.getDeviceUuid(serviceData);
                mMeshManagerApi.identifyNode(uuid, ATTENTION_TIMER);
            }
        }
    }

    private void clearExtendedMeshNode() {
        mExtendedMeshNode.postValue(null);
    }

    public LiveData<UnprovisionedMeshNode> getUnprovisionedMeshNode() {
        return mUnprovisionedMeshNodeLiveData;
    }

    LiveData<Integer> getConnectedProxyAddress() {
        return mConnectedProxyAddress;
    }

    public LiveData<ProvisionedMeshNode> getSelectedMeshNode() {
        return mExtendedMeshNode;
    }

    void setSelectedMeshNode(final ProvisionedMeshNode node) {
        mProvisionedMeshNode = node;
        mExtendedMeshNode.postValue(node);
    }


    LiveData<Element> getSelectedElement() {
        return mSelectedElement;
    }

    void setSelectedElement(final Element element) {
        mSelectedElement.postValue(element);
    }

    void setSelectedAppKey(@NonNull final ApplicationKey appKey) {
        mSelectedAppKey.postValue(appKey);
    }

    LiveData<ApplicationKey> getSelectedAppKey() {
        return mSelectedAppKey;
    }

    void setSelectedProvisioner(@NonNull final Provisioner provisioner) {
        mSelectedProvisioner.postValue(provisioner);
    }

    LiveData<Provisioner> getSelectedProvisioner() {
        return mSelectedProvisioner;
    }

    public LiveData<MeshModel> getSelectedModel() {
        return mSelectedModel;
    }

    public void setSelectedModel(final MeshModel model) {
        mSelectedModel.postValue(model);
    }

    @Override
    public void onDataReceived(final BluetoothDevice bluetoothDevice, final int mtu, final byte[] pdu) {
        mMeshManagerApi.handleNotifications(mtu, pdu);
    }

    @Override
    public void onDataSent(final BluetoothDevice device, final int mtu, final byte[] pdu) {
        if (mIsSentGroupMessage.getValue()){
            mSentGroupMessage.postValue(true);
        }
        if(mIsSentScheduleMessage.getValue()){
            mSentScheduleMessage.postValue(true);
        }
        mMeshManagerApi.handleWriteCallbacks(mtu, pdu);
    }

    @Override
    public void onDeviceConnecting(@NonNull final BluetoothDevice device) {
        mConnectionState.postValue("Connecting....");
    }

    @Override
    public void onDeviceConnected(@NonNull final BluetoothDevice device) {
        mIsConnected.postValue(true);
        mConnectionState.postValue("Discovering services....");
        mIsConnectedToProxy.postValue(true);
        networkStateNotificationTool.updateNotification("连接状态","已连接至mesh网络");
    }

    @Override
    public void onDeviceDisconnecting(@NonNull final BluetoothDevice device) {
        Log.v(TAG, "Disconnecting...");
        if (mIsReconnectingFlag) {
            mConnectionState.postValue("Reconnecting...");
        } else {
            mConnectionState.postValue("Disconnecting...");
        }
    }

    @Override
    public void onDeviceDisconnected(@NonNull final BluetoothDevice device) {
        Log.v(TAG, "Disconnected");
        mConnectionState.postValue("");
        if (mIsReconnectingFlag) {
            mIsReconnectingFlag = false;
            mIsReconnecting.postValue(false);
            mIsConnected.postValue(false);
            mIsConnectedToProxy.postValue(false);
            networkStateNotificationTool.updateNotification("连接状态","未连接至mesh网络");
        } else {
            mIsConnected.postValue(false);
            mIsConnectedToProxy.postValue(false);
            networkStateNotificationTool.updateNotification("连接状态","未连接至mesh网络");
            if (mConnectedProxyAddress.getValue() != null) {
                final MeshNetwork network = mMeshManagerApi.getMeshNetwork();
                if(network != null) {
                    network.setProxyFilter(null);
                }
            }
            //clearExtendedMeshNode();
        }
        mSetupProvisionedNode = false;
        mConnectedProxyAddress.postValue(null);
    }

    @Override
    public void onLinkLossOccurred(@NonNull final BluetoothDevice device) {
        Log.v(TAG, "Link loss occurred");
        mIsConnected.postValue(false);
    }

    @Override
    public void onServicesDiscovered(@NonNull final BluetoothDevice device, final boolean optionalServicesFound) {
        mConnectionState.postValue("Initializing...");
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onDeviceReady(@NonNull final BluetoothDevice device) {
        mOnDeviceReady.postValue(null);

        if (mBleMeshManager.isProvisioningComplete()) {
            if (mSetupProvisionedNode) {
                if (mMeshNetwork.getSelectedProvisioner().getProvisionerAddress() != null) {
                    mHandler.postDelayed(() -> {
                        //Adding a slight delay here so we don't send anything before we receive the mesh beacon message
                        final ProvisionedMeshNode node = mProvisionedMeshNodeLiveData.getValue();
                        if (node != null) {
                            final ConfigCompositionDataGet compositionDataGet = new ConfigCompositionDataGet();
                            mMeshManagerApi.createMeshPdu(node.getUnicastAddress(), compositionDataGet);
                        }
                    }, 2000);
                } else {
                    mSetupProvisionedNode = false;
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.PROVISIONER_UNASSIGNED);
                    clearExtendedMeshNode();
                }
            }
            mIsConnectedToProxy.postValue(true);
            networkStateNotificationTool.updateNotification("连接状态","已连接至mesh网络");
        }
    }

    @Override
    public void onBondingRequired(@NonNull final BluetoothDevice device) {
        // Empty.
    }

    @Override
    public void onBonded(@NonNull final BluetoothDevice device) {
        // Empty.
    }

    @Override
    public void onBondingFailed(@NonNull final BluetoothDevice device) {
        // Empty.
    }

    @Override
    public void onError(final BluetoothDevice device, @NonNull final String message, final int errorCode) {
        Log.e(TAG, message + " (code: " + errorCode + "), device: " + device.getAddress());
        mConnectionState.postValue(message);
    }

    @Override
    public void onDeviceNotSupported(@NonNull final BluetoothDevice device) {

    }

    @Override
    public void onNetworkLoaded(final MeshNetwork meshNetwork) {
        Log.d(TAG, "从onNetworkLoaded出进入");
        loadNetwork(meshNetwork);
    }

    @Override
    public void onNetworkUpdated(final MeshNetwork meshNetwork) {
        Log.d(TAG, "从onNetworkUpdated出进入");
        loadNetwork(meshNetwork);
        updateSelectedGroup();
    }

    @Override
    public void onNetworkLoadFailed(final String error) {
        mNetworkImportState.postValue(error);
    }

    @Override
    public void onNetworkImported(final MeshNetwork meshNetwork) {
        Log.d(TAG, "进入了onNetworkImported方法");
        loadNetwork(meshNetwork);
        mNetworkImportState.postValue(meshNetwork.getMeshName() + " has been successfully imported.\n" +
                "In order to start sending messages to this network, please change the provisioner address. " +
                "Using the same provisioner address will cause messages to be discarded due to the usage of incorrect sequence numbers " +
                "for this address. However if the network does not contain any nodes you do not need to change the address");
    }

    @Override
    public void onNetworkImportFailed(final String error) {
        mNetworkImportState.postValue(error);
    }

    @Override
    public void sendProvisioningPdu(final UnprovisionedMeshNode meshNode, final byte[] pdu) {
        mBleMeshManager.sendPdu(pdu);
    }

    @Override
    public void onMeshPduCreated(final byte[] pdu) {
        mBleMeshManager.sendPdu(pdu);
    }

    @Override
    public int getMtu() {
        return mBleMeshManager.getMaximumPacketSize();
    }

    @Override
    public void onProvisioningStateChanged(final UnprovisionedMeshNode meshNode, final ProvisioningState.States state, final byte[] data) {
        mUnprovisionedMeshNode = meshNode;
        mUnprovisionedMeshNodeLiveData.postValue(meshNode);
        switch (state) {
            case PROVISIONING_INVITE:
                mProvisioningStateLiveData = new ProvisioningStatusLiveData();
                break;
            case PROVISIONING_FAILED:
                mIsProvisioningComplete = false;
                break;
        }
        mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.fromStatusCode(state.getState()));
    }

    @Override
    public void onProvisioningFailed(final UnprovisionedMeshNode meshNode, final ProvisioningState.States state, final byte[] data) {
        mUnprovisionedMeshNode = meshNode;
        mUnprovisionedMeshNodeLiveData.postValue(meshNode);
        if (state == ProvisioningState.States.PROVISIONING_FAILED) {
            mIsProvisioningComplete = false;
        }
        mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.fromStatusCode(state.getState()));
    }

    @Override
    public void onProvisioningCompleted(final ProvisionedMeshNode meshNode, final ProvisioningState.States state, final byte[] data) {
        mProvisionedMeshNode = meshNode;
        Log.d(TAG, "有ProvisionedMeshNode了: ");
        mUnprovisionedMeshNodeLiveData.postValue(null);
        mProvisionedMeshNodeLiveData.postValue(meshNode);
        if (state == ProvisioningState.States.PROVISIONING_COMPLETE) {
            onProvisioningCompleted(meshNode);
        }
        mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.fromStatusCode(state.getState()));
    }

    private void onProvisioningCompleted(final ProvisionedMeshNode node) {
        mIsProvisioningComplete = true;
        mProvisionedMeshNode = node;
        mIsReconnecting.postValue(true);
        mBleMeshManager.disconnect().enqueue();
        loadNodes();
        mHandler.post(() -> mConnectionState.postValue("Scanning for provisioned node"));
        mHandler.postDelayed(mReconnectRunnable, 1000); //Added a slight delay to disconnect and refresh the cache
    }

    @SuppressLint("RestrictedApi")
    private void loadNodes() {
        final List<ProvisionedMeshNode> nodes = new ArrayList<>();
        for (final ProvisionedMeshNode node : mMeshNetwork.getNodes()) {
            if (!node.getUuid().equalsIgnoreCase(mMeshNetwork.getSelectedProvisioner().getProvisionerUuid())) {
                nodes.add(node);
            }
        }
        mProvisionedNodes.postValue(nodes);
    }

    @Override
    public void onTransactionFailed(final int dst, final boolean hasIncompleteTimerExpired) {
        mProvisionedMeshNode = mMeshNetwork.getNode(dst);
        mTransactionStatus.postValue(new TransactionStatus(dst, hasIncompleteTimerExpired));
    }

    @Override
    public void onUnknownPduReceived(final int src, final byte[] accessPayload) {
        final ProvisionedMeshNode node = mMeshNetwork.getNode(src);
        if (node != null) {
            updateNode(node);
        }
    }

    @Override
    public void onBlockAcknowledgementProcessed(final int dst, @NonNull final ControlMessage message) {
        final ProvisionedMeshNode node = mMeshNetwork.getNode(dst);
        if (node != null) {
            mProvisionedMeshNode = node;
            if (mSetupProvisionedNode) {
                mProvisionedMeshNodeLiveData.postValue(mProvisionedMeshNode);
                mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.SENDING_BLOCK_ACKNOWLEDGEMENT);
            }
        }
    }

    @Override
    public void onBlockAcknowledgementReceived(final int src, @NonNull final ControlMessage message) {
        final ProvisionedMeshNode node = mMeshNetwork.getNode(src);
        if (node != null) {
            mProvisionedMeshNode = node;
            if (mSetupProvisionedNode) {
                mProvisionedMeshNodeLiveData.postValue(node);
                mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.BLOCK_ACKNOWLEDGEMENT_RECEIVED);
            }
        }
    }

    @Override
    public void onMeshMessageProcessed(final int dst, @NonNull final MeshMessage meshMessage) {
        final ProvisionedMeshNode node = mMeshNetwork.getNode(dst);
        if (node != null) {
            mProvisionedMeshNode = node;
            if (meshMessage instanceof ConfigCompositionDataGet) {
                if (mSetupProvisionedNode) {
                    mProvisionedMeshNodeLiveData.postValue(node);
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.COMPOSITION_DATA_GET_SENT);
                }
            } else if (meshMessage instanceof ConfigDefaultTtlGet) {
                if (mSetupProvisionedNode) {
                    mProvisionedMeshNodeLiveData.postValue(node);
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.SENDING_DEFAULT_TTL_GET);
                }
            } else if (meshMessage instanceof ConfigAppKeyAdd) {
                if (mSetupProvisionedNode) {
                    mProvisionedMeshNodeLiveData.postValue(node);
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.SENDING_APP_KEY_ADD);
                }
            } else if (meshMessage instanceof ConfigNetworkTransmitSet) {
                if (mSetupProvisionedNode) {
                    mProvisionedMeshNodeLiveData.postValue(node);
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.SENDING_NETWORK_TRANSMIT_SET);
                }
            }
        }
    }

    @Override
    public void onMeshMessageReceived(final int src, @NonNull final MeshMessage meshMessage) {
        final ProvisionedMeshNode node = mMeshNetwork.getNode(src);
        Log.d(TAG, "进入了nMeshMessageReceived但是没有找到节点 ");
        if (node != null)
            Log.d(TAG, "进入了nMeshMessageReceived: ");
        Log.d(TAG, "meshMessage的类型: "+meshMessage.getOpCode());
            if (meshMessage.getOpCode() == ProxyConfigMessageOpCodes.FILTER_STATUS) {
                mProvisionedMeshNode = node;
                setSelectedMeshNode(node);
                final ProxyConfigFilterStatus status = (ProxyConfigFilterStatus) meshMessage;
                final int unicastAddress = status.getSrc();
                Log.v(TAG, "Proxy configuration source: " + MeshAddress.formatAddress(status.getSrc(), false));
                mConnectedProxyAddress.postValue(unicastAddress);
                mMeshMessageLiveData.postValue(status);
            } else if (meshMessage.getOpCode() == CONFIG_COMPOSITION_DATA_STATUS) {
                if (mSetupProvisionedNode) {
                    mIsCompositionDataReceived = true;
                    mProvisionedMeshNodeLiveData.postValue(node);
                    mConnectedProxyAddress.postValue(node.getUnicastAddress());
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.COMPOSITION_DATA_STATUS_RECEIVED);
                    mHandler.postDelayed(() -> {
                        final ConfigDefaultTtlGet configDefaultTtlGet = new ConfigDefaultTtlGet();
                        mMeshManagerApi.createMeshPdu(node.getUnicastAddress(), configDefaultTtlGet);
                    }, 500);
                } else {
                    updateNode(node);
                }
            } else if (meshMessage.getOpCode() == CONFIG_DEFAULT_TTL_STATUS) {
                final ConfigDefaultTtlStatus status = (ConfigDefaultTtlStatus) meshMessage;
                if (mSetupProvisionedNode) {
                    mIsDefaultTtlReceived = true;
                    mProvisionedMeshNodeLiveData.postValue(node);
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.DEFAULT_TTL_STATUS_RECEIVED);
                    mHandler.postDelayed(() -> {
                        final ConfigNetworkTransmitSet networkTransmitSet = new ConfigNetworkTransmitSet(2, 1);
                        mMeshManagerApi.createMeshPdu(node.getUnicastAddress(), networkTransmitSet);
                    }, 1500);
                } else {
                    updateNode(node);
                    mMeshMessageLiveData.postValue(status);
                }
            } else if (meshMessage.getOpCode() == CONFIG_NETWORK_TRANSMIT_STATUS) {
                if (mSetupProvisionedNode) {
                    mIsNetworkRetransmitSetCompleted = true;
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.NETWORK_TRANSMIT_STATUS_RECEIVED);
                    final ApplicationKey appKey = mMeshNetworkLiveData.getSelectedAppKey();
                    if (appKey != null) {
                        mHandler.postDelayed(() -> {
                            // We should use the app key's boundNetKeyIndex as the network key index when adding the default app key
                            final NetworkKey networkKey = mMeshNetwork.getNetKeys().get(appKey.getBoundNetKeyIndex());
                            final ConfigAppKeyAdd configAppKeyAdd = new ConfigAppKeyAdd(networkKey, appKey);
                            mMeshManagerApi.createMeshPdu(node.getUnicastAddress(), configAppKeyAdd);
                        }, 1500);
                    } else {
                        mSetupProvisionedNode = false;
                        mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.APP_KEY_STATUS_RECEIVED);
                    }
                } else {
                    updateNode(node);
                    final ConfigNetworkTransmitStatus status = (ConfigNetworkTransmitStatus) meshMessage;
                    mMeshMessageLiveData.postValue(status);
                }
            } else if (meshMessage.getOpCode() == CONFIG_APPKEY_STATUS) {
                final ConfigAppKeyStatus status = (ConfigAppKeyStatus) meshMessage;
                if (mSetupProvisionedNode) {
                    mSetupProvisionedNode = false;
                    if (status.isSuccessful()) {
                        mIsAppKeyAddCompleted = true;
                        mProvisionedMeshNodeLiveData.postValue(node);
                    }
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.APP_KEY_STATUS_RECEIVED);
                } else {
                    updateNode(node);
                    mMeshMessageLiveData.postValue(status);
                }
            } else if (meshMessage.getOpCode() == CONFIG_MODEL_APP_STATUS) {
                if (updateNode(node)) {
                    final ConfigModelAppStatus status = (ConfigModelAppStatus) meshMessage;
                    final Element element = node.getElements().get(status.getElementAddress());
                    if (node.getElements().containsKey(status.getElementAddress())) {
                        mSelectedElement.postValue(element);
                        final MeshModel model = element.getMeshModels().get(status.getModelIdentifier());
                        mSelectedModel.postValue(model);
                    }
                }
            } else if (meshMessage.getOpCode() == CONFIG_MODEL_PUBLICATION_STATUS) {
                if (updateNode(node)) {
                    final ConfigModelPublicationStatus status = (ConfigModelPublicationStatus) meshMessage;
                    if (node.getElements().containsKey(status.getElementAddress())) {
                        final Element element = node.getElements().get(status.getElementAddress());
                        mSelectedElement.postValue(element);
                        final MeshModel model = element.getMeshModels().get(status.getModelIdentifier());
                        mSelectedModel.postValue(model);
                    }
                }

            } else if (meshMessage.getOpCode() == CONFIG_MODEL_SUBSCRIPTION_STATUS) {
                if (updateNode(node)) {
                    final ConfigModelSubscriptionStatus status = (ConfigModelSubscriptionStatus) meshMessage;
                    if (node.getElements().containsKey(status.getElementAddress())) {
                        final Element element = node.getElements().get(status.getElementAddress());
                        mSelectedElement.postValue(element);
                        final MeshModel model = element.getMeshModels().get(status.getModelIdentifier());
                        mSelectedModel.postValue(model);
                    }
                }

            } else if (meshMessage.getOpCode() == CONFIG_NODE_RESET_STATUS) {
                mBleMeshManager.setClearCacheRequired();
                final ConfigNodeResetStatus status = (ConfigNodeResetStatus) meshMessage;
                mExtendedMeshNode.postValue(null);
                loadNodes();
                mMeshMessageLiveData.postValue(status);

            } else if (meshMessage.getOpCode() == CONFIG_RELAY_STATUS) {
                if (updateNode(node)) {
                    final ConfigRelayStatus status = (ConfigRelayStatus) meshMessage;
                    mMeshMessageLiveData.postValue(status);
                }
            } else if (meshMessage.getOpCode() == CONFIG_HEARTBEAT_PUBLICATION_STATUS) {
                if (updateNode(node)) {
                    final Element element = node.getElements().get(meshMessage.getSrc());
                    final MeshModel model = element.getMeshModels().get((int) SigModelParser.CONFIGURATION_SERVER);
                    mSelectedModel.postValue(model);
                    mMeshMessageLiveData.postValue(meshMessage);
                }
            } else if (meshMessage.getOpCode() == CONFIG_HEARTBEAT_SUBSCRIPTION_STATUS) {
                if (updateNode(node)) {
                    final Element element = node.getElements().get(meshMessage.getSrc());
                    final MeshModel model = element.getMeshModels().get((int) SigModelParser.CONFIGURATION_SERVER);
                    mSelectedModel.postValue(model);
                    mMeshMessageLiveData.postValue(meshMessage);
                }
            } else if (meshMessage.getOpCode() == CONFIG_GATT_PROXY_STATUS) {
                if (updateNode(node)) {
                    final ConfigProxyStatus status = (ConfigProxyStatus) meshMessage;
                    mMeshMessageLiveData.postValue(status);
                }
            } else if (meshMessage.getOpCode() == GENERIC_ON_OFF_STATUS) {
                if (updateNode(node)) {
                    final GenericOnOffStatus status = (GenericOnOffStatus) meshMessage;
                    if (node.getElements().containsKey(status.getSrcAddress())) {
                        final Element element = node.getElements().get(status.getSrcAddress());
                        mSelectedElement.postValue(element);
                        final MeshModel model = element.getMeshModels().get((int) SigModelParser.GENERIC_ON_OFF_SERVER);
                        mSelectedModel.postValue(model);
                    }
                }
            } else if (meshMessage.getOpCode() == GENERIC_LEVEL_STATUS) {
                if (updateNode(node)) {
                    final GenericLevelStatus status = (GenericLevelStatus) meshMessage;
                    if (node.getElements().containsKey(status.getSrcAddress())) {
                        final Element element = node.getElements().get(status.getSrcAddress());
                        mSelectedElement.postValue(element);
                        final MeshModel model = element.getMeshModels().get((int) SigModelParser.GENERIC_LEVEL_SERVER);
                        mSelectedModel.postValue(model);
                    }
                }
            } else if (meshMessage.getOpCode() == SCENE_STATUS) {
                if (updateNode(node)) {
                    final SceneStatus status = (SceneStatus) meshMessage;
                    if (node.getElements().containsKey(status.getSrcAddress())) {
                        final Element element = node.getElements().get(status.getSrcAddress());
                        mSelectedElement.postValue(element);
                    }
                }
            } else if (meshMessage.getOpCode() == SCENE_REGISTER_STATUS) {
                if (updateNode(node)) {
                    final SceneRegisterStatus status = (SceneRegisterStatus) meshMessage;
                    if (node.getElements().containsKey(status.getSrcAddress())) {
                        final Element element = node.getElements().get(status.getSrcAddress());
                        mSelectedElement.postValue(element);
                    }
                }
            } else if (meshMessage instanceof VendorModelMessageStatus) {
                if (updateNode(node)) {
                    final VendorModelMessageStatus status = (VendorModelMessageStatus) meshMessage;
                    if (node.getElements().containsKey(status.getSrcAddress())) {
                        final Element element = node.getElements().get(status.getSrcAddress());
                        mSelectedElement.postValue(element);
                        final MeshModel model = element.getMeshModels().get(status.getModelIdentifier());
                        mSelectedModel.postValue(model);
                    }
                }
            }

        if (mMeshMessageLiveData.hasActiveObservers()) {
            mMeshMessageLiveData.postValue(meshMessage);
        }

        //Refresh mesh network live data
        if(mMeshManagerApi.getMeshNetwork() != null) {
            mMeshNetworkLiveData.refresh(mMeshManagerApi.getMeshNetwork());
        }
    }

    @Override
    public void onMessageDecryptionFailed(final String meshLayer, final String errorMessage) {
        Log.e(TAG, "Decryption failed in " + meshLayer + " : " + errorMessage);
    }

    /**
     * Loads the network that was loaded from the db or imported from the mesh cdb
     *
     * @param meshNetwork mesh network that was loaded
     */
    @SuppressLint("RestrictedApi")
    private void loadNetwork(final MeshNetwork meshNetwork) {
        long startTime = System.currentTimeMillis(); //获取开始时间
        Log.d(TAG, "进入了loadNetwork: ");
        mMeshNetwork = meshNetwork;
        if (mMeshNetwork != null) {

            if (!mMeshNetwork.isProvisionerSelected()) {
                final Provisioner provisioner = meshNetwork.getProvisioners().get(0);
                provisioner.setLastSelected(true);
                mMeshNetwork.selectProvisioner(provisioner);
            }
            //Load live data with mesh network
            mMeshNetworkLiveData.loadNetworkInformation(meshNetwork);
            //Load live data with provisioned nodes
            loadNodes();

            final ProvisionedMeshNode node = getSelectedMeshNode().getValue();
            if (node != null) {
                mExtendedMeshNode.postValue(mMeshNetwork.getNode(node.getUuid()));
            }
        }
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("运行时间：" + (endTime - startTime) + "ms"); //单位毫秒
    }

    /**
     * We should only update the selected node, since sending messages to group address will notify with nodes that is not on the UI
     */
    private boolean updateNode(@NonNull final ProvisionedMeshNode node) {
        if (mProvisionedMeshNode != null && mProvisionedMeshNode.getUnicastAddress() == node.getUnicastAddress()) {
            mProvisionedMeshNode = node;
            mExtendedMeshNode.postValue(node);
            return true;
        }
        return false;
    }

    /**
     * Starts reconnecting to the device
     */
    private void startScan() {
        if (mIsScanning)
            return;

        mIsScanning = true;
        // Scanning settings
        final ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                // Refresh the devices list every second
                .setReportDelay(0)
                // Hardware filtering has some issues on selected devices
                .setUseHardwareFilteringIfSupported(false)
                // Samsung S6 and S6 Edge report equal value of RSSI for all devices. In this app we ignore the RSSI.
                /*.setUseHardwareBatchingIfSupported(false)*/
                .build();

        // Let's use the filter to scan only for Mesh devices
        final List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid((BleMeshManager.MESH_PROXY_UUID))).build());

        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.startScan(filters, settings, scanCallback);
        Log.v(TAG, "Scan started");
        mHandler.postDelayed(mScannerTimeout, 20000);
    }

    /**
     * stop scanning for bluetooth devices.
     */
    private void stopScan() {
        mHandler.removeCallbacks(mScannerTimeout);
        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.stopScan(scanCallback);
        mIsScanning = false;
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(final int callbackType, final ScanResult result) {
            //In order to connectToProxy to the correct device, the hash advertised in the advertisement data should be matched.
            //This is to make sure we connectToProxy to the same device as device addresses could change after provisioning.
            final ScanRecord scanRecord = result.getScanRecord();
            if (scanRecord != null) {
                final byte[] serviceData = Utils.getServiceData(result, BleMeshManager.MESH_PROXY_UUID);
                if (serviceData != null) {
                    if (mMeshManagerApi.isAdvertisedWithNodeIdentity(serviceData)) {
                        final ProvisionedMeshNode node = mProvisionedMeshNode;
                        if (mMeshManagerApi.nodeIdentityMatches(node, serviceData)) {
                            stopScan();
                            mConnectionState.postValue("Provisioned node found");
                            onProvisionedDeviceFound(node, new ExtendedBluetoothDevice(result));
                        }
                    }
                }
            }
        }

        @Override
        public void onBatchScanResults(@NonNull final List<ScanResult> results) {
            // Batch scan is disabled (report delay = 0)
        }

        @Override
        public void onScanFailed(final int errorCode) {

        }
    };

    private void onProvisionedDeviceFound(final ProvisionedMeshNode node, final ExtendedBluetoothDevice device) {
        mSetupProvisionedNode = true;
        mProvisionedMeshNode = node;
        mIsReconnectingFlag = true;
        //Added an extra delay to ensure reconnection
        mHandler.postDelayed(() -> connectToProxy(device), 2000);
    }

    /**
     * Generates the groups based on the addresses each models have subscribed to
     */
    private void loadGroups1() {
        mGroups.postValue(mMeshNetwork.getGroups());
    }

    private void updateSelectedGroup() {
        final Group selectedGroup = mSelectedGroupLiveData.getValue();
        if (selectedGroup != null) {
            mSelectedGroupLiveData.postValue(mMeshNetwork.getGroup(selectedGroup.getAddress()));
        }
    }

    /**
     * Sets the group that was selected from the GroupAdapter.
     */
    void setSelectedGroup(final int address) {
        final Group group = mMeshNetwork.getGroup(address);
        if (group != null) {
            mSelectedGroupLiveData.postValue(group);
        }
    }
}

