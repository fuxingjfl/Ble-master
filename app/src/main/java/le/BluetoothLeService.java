/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package le;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import zsx.hldkmj.fy.com.antilose.sp.SettingSp;


/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
@SuppressLint("NewApi")
public class BluetoothLeService extends Service {
    private final static String TAG = "TAG";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED           = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED        = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE           = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA                      = "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT       = UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    private Map<String, BluetoothGatt> mMapBleGatt;
    public static String FINDME_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public final static UUID UUID_FINDME_SERVICE =
            UUID.fromString(FINDME_SERVICE);
    public static String FIND_ME_CHARACTERISTIC = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public final static UUID UUID_FIND_ME_CHARACTERISTIC =
            UUID.fromString(FIND_ME_CHARACTERISTIC);
    public static String ALERT_LEVEL_CHARACTERISTIC = "00002a06-0000-1000-8000-00805f9b34fb";
    public final static UUID UUID_ALERT_LEVEL_CHARACTERISTIC =
            UUID.fromString(ALERT_LEVEL_CHARACTERISTIC);
    private Handler mCommandHanler = new Handler();
    public static final String ACTION_DEV_AUDIO_RECORD = "com.fb.ble.action.audio_record";// 录音的广播
    public static final String ACTION_DEV_AUDIO_RECORD_CHANGE = "com.fb.ble.action.audio_record_change";// 录音设置改变的广播
    private boolean isPreviousCommandHandling = false;// 是否在处理前一条指令中

    /**
     * 固定名称
     */
    private String filterName="WIM";;
//	private String filterName="TCM BT313287";;
    /**
     * 通知重启蓝牙
     */
    public static final String ACTION_RESTART_BLE = "com.calm.ble.action_restart_ble";
    public static final String ACTION_RESTART_BLE1 = "com.calm.ble.action_restart_ble1";
    public final static String ACTION_DEV_LOST = "com.fb.ble.action.device_lost";//丢失
    public static final String ACTION_DEV_FIND = "com.fb.ble.action.find_waring";// 寻找报警的广播

    public static final String ACTION_DEV_ANTILOSS = "com.fb.ble.action.antiloss_waring";// 防丢报警的广播

    public static final String ACTION_DEV_WARING_STOP = "com.fb.ble.action.waring_stop";// 停止报警的广播

    /**
     * 定时扫描发现设备
     */
    public static final String ACTION_TIMING_DEV_FOUND = "com.fb.ble.action.device_timing_found";
    /**
     * 广播移除丢失列表
     */
    public static final String ACTION_LOST_REMOVE = "com.fb.ble.action.action_lost_remove";
    /**
     * 广播设备丢失
     */
    public static final String ACTION_DEV_ADD_LOST = "com.calm.ble.action_dev_add_lost";
    /**
     * 广播报警距离变化
     */
    public static final String ACTION_ALARM_DIS_CHANGE = "con.calm.ble.action_alarm_dis_change";

    public static final String ACTION_DEV_SINGLE_CLICK = "com.fb.ble.action.device_single_click";// 单机的指令
    public static String IMMIDIATE_ALERT_SERVICE = "00001802-0000-1000-8000-00805f9b34fb";
    public final static UUID UUID_IMMIDIATE_ALERT_SERVICEL =
            UUID.fromString(IMMIDIATE_ALERT_SERVICE);

    public static int mRssiThresholdMeter ;// 超过多少范围开始报警,
    public static int mRssiThreshold ;
    public float r;

    private BroadcastReceiver mAudioRecordBrostReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(ACTION_ALARM_DIS_CHANGE))
            {
                mRssiThresholdMeter = SettingSp.getInstance(BluetoothLeService.this).getAlarmDis();
                mRssiThreshold = (int) (-4.00 * (float) (mRssiThresholdMeter) - 48.82 - 8);
            }
        }
    };


    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            Log.e("TAG","Service==="+newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);

                //这行可以干掉
                gatt.discoverServices();
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

                // 连接成功，重置这个设备的Rssi值缓存
                Rssi rssi = mRssiMaps.get(gatt.getDevice().getAddress());
                if (rssi != null) {
                    rssi.reset();
                } else {
                    mRssiMaps.put(gatt.getDevice().getAddress(), new Rssi());
                }

            }
//            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                intentAction = ACTION_GATT_DISCONNECTED;
//                mConnectionState = STATE_DISCONNECTED;
//
//                Log.i(TAG, "Disconnected from GATT server.");
//                broadcastUpdate(intentAction, gatt.getDevice().getAddress());
//                enablePeerDeviceNotifyMe(gatt, false);
//
//            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e("TAG","准备发广播了" +
                        "");
                broadcastUpdate(BluetoothLeService.ACTION_GATT_DISCONNECTED, gatt.getDevice().getAddress());

                enablePeerDeviceNotifyMe(gatt, false);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, gatt.getDevice().getAddress());
                enablePeerDeviceNotifyMe(gatt, true);

                UUID serviceUUID = UUID_IMMIDIATE_ALERT_SERVICEL;
                UUID characteristicUUID = UUID_ALERT_LEVEL_CHARACTERISTIC;
                BluetoothGattCharacteristic characteristic = getCharacteristic(gatt, serviceUUID, characteristicUUID);
                gatt.readCharacteristic(characteristic);

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(final BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            //蓝牙数据接收  数据包characteristic

            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);


            byte[] remoteKeyValue;
            remoteKeyValue = characteristic.getValue();
            isyc=false;
            if (remoteKeyValue[0] == 1) {
                if (isPreviousCommandHandling) {// 代表双击的第二次

                    Log.i("TAG", "ACTION_DEV_DOUBLE_CLICK---------");
                    mCommandHanler.removeCallbacksAndMessages(null);// 清除上一条指令
                    isPreviousCommandHandling = false;
                    // 立即处理双击指令
                    int doubleClick = SettingSp.getInstance(BluetoothLeService.this).getDoubleClick();
                    if (doubleClick == 0) {// 报警
                        // 通知界面在寻找设备
                        broadcastUpdate(ACTION_DEV_FIND, gatt.getDevice().getAddress());
                    } else {// 录音
//                        broadcastUpdate(ACTION_DEV_AUDIO_RECORD);
                    }
                } else {
                    isPreviousCommandHandling = true;
                    mCommandHanler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    isPreviousCommandHandling = false;
                            broadcastUpdate(ACTION_DEV_SINGLE_CLICK, gatt.getDevice().getAddress(), gatt.getDevice().getName());
                            Log.i("TAG", "ACTION_DEV_SINGLE_CLICK-------------------");
                        }
                    }, 500);
                }
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Rssi rssiObj = mRssiMaps.get(gatt.getDevice().getAddress());
            if (rssiObj != null) {
                if (rssiObj.needVerification()) {
                    boolean waring = rssiObj.verification();
                    if (waring) {
                        if (!isyc){
                            // 通知界面在防丢设备
                            broadcastUpdate(ACTION_DEV_ANTILOSS, gatt.getDevice().getAddress());
                            isyc=true;
                        }
                    }
                } else {
                    rssiObj.add(rssi);
                }
            } else {
                mRssiMaps.put(gatt.getDevice().getAddress(), new Rssi());
            }
        }
    };

    public boolean isyc;

    // 开启接受设备发送的点击指令
    public void enablePeerDeviceNotifyMe(BluetoothGatt gatt, boolean enable) {
        UUID serviceUUID = UUID_FINDME_SERVICE;
        UUID characteristicUUID = UUID_FIND_ME_CHARACTERISTIC;
        //获取某个特定设备的某个服务下的某个特征值
        BluetoothGattCharacteristic characteristic = getCharacteristic(gatt, serviceUUID, characteristicUUID);
        if (characteristic != null) {
            final int charaProp = characteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                //将给定描述符的值写入关联的远程设备
                setCharacteristicNotification(gatt, characteristic, enable);
            }
        }
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic
     *            Characteristic to act on.
     * @param enabled
     *            If true, enable notification. False otherwise.
     */
    private void setCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean enabled) {
        gatt.setCharacteristicNotification(characteristic, enabled);

        // TODO 下面这个是干叼的
        if (UUID_FIND_ME_CHARACTERISTIC.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }

    }

    // 获取某个特定设备的某个服务下的某个特征值
    private BluetoothGattCharacteristic getCharacteristic(BluetoothGatt gatt, UUID serviceUUID, UUID characteristicUUID) {
        if (gatt == null) {
            return null;
        }
        BluetoothGattService gattService = gatt.getService(serviceUUID);
        if (gattService == null) {
            return null;
        }
        BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(characteristicUUID);
        if (gattCharacteristic == null) {
            return null;
        }
        return gattCharacteristic;
    }


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    private void broadcastUpdate(final String action, String deviceAddress) {
        Log.e("TAG","===action===="
        +action);
        final Intent intent = new Intent(action);
        intent.putExtra("device_address", deviceAddress);
        sendBroadcast(intent);
    }
    private void broadcastUpdate(final String action, String deviceAddress, String name) {
        final Intent intent = new Intent(action);
        intent.putExtra("device_address", deviceAddress);
        intent.putExtra("device_name", name);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        mRssiThresholdMeter = SettingSp.getInstance(BluetoothLeService.this).getAlarmDis();
        mRssiThreshold=(int) (-4.00 * (float) (mRssiThresholdMeter) - 48.82 - 8);
        mMapBleGatt = new HashMap<String, BluetoothGatt>();
        mRssiHandler.sendEmptyMessage(0x1);
        IntentFilter filter = new IntentFilter(ACTION_DEV_AUDIO_RECORD);
        filter.addAction(ACTION_DEV_AUDIO_RECORD_CHANGE);
        filter.addAction(ACTION_ALARM_DIS_CHANGE);
        registerReceiver(mAudioRecordBrostReceiver, filter);

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) { //连接

        mBluetoothDeviceAddress=null;

        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        mBluetoothGatt = mMapBleGatt.get(address);
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mMapBleGatt.put(address, mBluetoothGatt);
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    //获取当前连接设备的特征对象
    public Map<String, BluetoothGatt> getmMapBleGatt(){
        return mMapBleGatt;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void disconnect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            return;
        }

        BluetoothGatt gatt = mMapBleGatt.get(address);
        if (gatt != null) {
            gatt.disconnect();
        }
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        unregisterReceiver(mAudioRecordBrostReceiver);
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }


    // 关闭某个device
    public void closeDevice(String address) {
        BluetoothGatt gatt = mMapBleGatt.get(address);
        if (gatt != null) {
            gatt.close();
        }
        mMapBleGatt.remove(address);
    }

    // 关于蓝牙Rssi距离报警的代码
    private Map<String, Rssi> mRssiMaps = new HashMap<String, Rssi>();
    private Handler mRssiHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0x1) {
                mDisconnectGattAddress.clear();
                for (String key : mRssiMaps.keySet()) {
                    updateRssi(key);
                }
                for (String address:mDisconnectGattAddress) {
                    mRssiMaps.remove(address);
                }
                mRssiHandler.sendEmptyMessageDelayed(0x1, 100);
            }
        }
    };


    private class Rssi {
        //private static final int mRssiThresholdMeter = 10;// 超过多少范围开始报警
        //private static final int mRssiThreshold = (int) (-4.00 * (float) (mRssiThresholdMeter) - 48.82 - 8);

        public Rssi()
        {
//			mRssiThresholdMeter = SettingSp.getInstance(BleService.this).getAlarmDis();
            mRssiThreshold = (int) (-4.00 * (float) (mRssiThresholdMeter) - 48.82 - 8);
        }
        private static final int MAX_GRAND_TOTAL = 20;// 最大累计数
        private int current_index = 0;
        private int[] rssi = new int[MAX_GRAND_TOTAL];

        public boolean needVerification() {// 是否需要开始验证
//            Log.e("TAG","current_index==="+current_index);
            if (current_index >= MAX_GRAND_TOTAL) {
                return true;
            }
            return false;
        }

//		public boolean verification() {// 是否超出范围
//			Arrays.sort(rssi);
//			int rssiSum = 0;
//			for (int i = 2; i < MAX_GRAND_TOTAL - 2; i++) {
//				rssiSum = rssiSum + rssi[i];
//			}
//			reset();
//			int rssiReal = rssiSum / (MAX_GRAND_TOTAL - 4);
//			if (rssiReal < mRssiThreshold) {// 当信号比阀值要低，表示距离远了
//				return true;
//			} else {
//				return false;
//			}
//
//		}


        public boolean verification() {// 是否超出范围

            Arrays.sort(rssi);
            int rssiSum = 0;
            for (int i = 2; i < MAX_GRAND_TOTAL - 5; i++) {
                rssiSum = rssiSum + rssi[i];
            }
            reset();

            Log.i(TAG, "rssiSum ----------------:"+rssiSum);

            int rssiReal = rssiSum / (MAX_GRAND_TOTAL - 4);


            Log.i(TAG, "r ----------------:"+r);
            Log.i(TAG, "rssiReal----------->:"+rssiReal);
            Log.i(TAG, "mRssiThreshold------------------>:"+mRssiThreshold);

            if (rssiReal < mRssiThreshold) {// 当信号比阀值要低，表示距离远了

                return true;
            } else {
                return false;
            }

        }





        public void add(int value) {
            if (current_index >= MAX_GRAND_TOTAL) {
                return;
            }
            rssi[current_index] = value;
            current_index++;
        }

        public void reset() {
            current_index = 0;
            for (int i = 0; i < rssi.length; i++) {
                rssi[i] = 0;
            }
        }
    }
    private List<String> mDisconnectGattAddress = new ArrayList<String>();
    private void updateRssi(final String deviceAddress) {
        final BluetoothGatt gatt = mMapBleGatt.get(deviceAddress);
        if (gatt != null) {
            BluetoothDevice device = gatt.getDevice();
            int connectState = mBluetoothManager.getConnectionState(device, BluetoothProfile.GATT_SERVER);
            if (connectState == BluetoothProfile.STATE_CONNECTED) {
                mRssiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        gatt.readRemoteRssi();
                    }
                }, 100);
            } else {
                mDisconnectGattAddress.add(deviceAddress);
            }
        }
    }



}
