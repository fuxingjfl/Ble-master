package zsx.hldkmj.fy.com.antilose;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.calypso.bluelib.bean.MessageBean;
import com.calypso.bluelib.bean.SearchResult;
import com.calypso.bluelib.listener.OnConnectListener;
import com.calypso.bluelib.listener.OnReceiveMessageListener;
import com.calypso.bluelib.listener.OnSearchDeviceListener;
import com.calypso.bluelib.listener.OnSendMessageListener;
import com.calypso.bluelib.manage.BlueManager;
import com.calypso.bluelib.utils.TypeConversion;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import le.BluetoothLeService;


/**
 *
 * 里面包括低功耗蓝牙，以及传统蓝牙的用法
 *
 *
 */

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    private BlueManager bluemanage;
    private int progress = 0;
    private TextView statusView;
    private TextView contextView;
    private ProgressBar progressBar;
    private StringBuilder stringBuilder;
//    private List<SearchResult> mDevices;
    private List<BleDevice> mDevices;
    private DeviceListAdapter mAdapter;
    private RecyclerView recycleView;
    private RelativeLayout devieslist;
    private RelativeLayout deviesinfo;
    private boolean mConnected = false;
    private OnConnectListener onConnectListener;
    private OnSendMessageListener onSendMessageListener;
    private OnSearchDeviceListener onSearchDeviceListener;
    private OnReceiveMessageListener onReceiveMessageListener;
    private BluetoothLeService mBluetoothLeService;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    public static String IMMIDIATE_ALERT_SERVICE = "00001802-0000-1000-8000-00805f9b34fb";
    public final static UUID UUID_IMMIDIATE_ALERT_SERVICEL =
            UUID.fromString(IMMIDIATE_ALERT_SERVICE);
    public static String ALERT_LEVEL_CHARACTERISTIC = "00002a06-0000-1000-8000-00805f9b34fb";
    public final static UUID UUID_ALERT_LEVEL_CHARACTERISTIC =
            UUID.fromString(ALERT_LEVEL_CHARACTERISTIC);


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String message = msg.obj.toString();
            switch (msg.what) {
                case 0:
                    statusView.setText(message);
                    break;
                case 1:
                    stringBuilder.append(message + " \n");
                    contextView.setText(stringBuilder.toString());
                    progress += 4;
                    progressBar.setProgress(progress);
                    break;
                case 2:
                    progress = 100;
                    progressBar.setProgress(progress);
                    break;
                case 3:
                    statusView.setText("接收完成！");
                    stringBuilder.delete(0, stringBuilder.length());
                    stringBuilder.append(message);
                    contextView.setText(stringBuilder.toString());
                    break;
                case 4:
                    statusView.setText(message);
                    deviesinfo.setVisibility(View.VISIBLE);
                    devieslist.setVisibility(View.GONE);
                    break;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        mDevices = new ArrayList<>();
        mAdapter = new DeviceListAdapter(R.layout.device_list_item, mDevices);
        stringBuilder = new StringBuilder();
        devieslist = (RelativeLayout) findViewById(R.id.parent_r1);
        deviesinfo = (RelativeLayout) findViewById(R.id.parent_r2);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        recycleView = (RecyclerView) findViewById(R.id.blue_rv);
        recycleView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        contextView = (TextView) findViewById(R.id.context);
        statusView = (TextView) findViewById(R.id.status);
        recycleView.setAdapter(mAdapter);
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS)) {
                    Toast.makeText(MainActivity.this, "shouldShowRequestPermissionRationale", Toast.LENGTH_SHORT).show();
                }
            }
        }
        //传统蓝牙
//        initBlueManager();
        initLisetener();

        Intent gattServiceIntent = new Intent(MainActivity.this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


    }

    /**
     * 初始化蓝牙管理，设置监听
     */
//    public void initBlueManager() {
//        onSearchDeviceListener = new OnSearchDeviceListener() {
//            @Override
//            public void onStartDiscovery() {
//                sendMessage(0, "正在搜索设备..");
//                Log.d(TAG, "onStartDiscovery()");
//
//            }
//
//            @Override
//            public void onNewDeviceFound(BluetoothDevice device) {
//                Log.d(TAG, "new device: " + device.getName() + " " + device.getAddress());
//            }
//
//            @Override
//            public void onSearchCompleted(List<SearchResult> bondedList, List<SearchResult> newList) {
//                Log.d(TAG, "SearchCompleted: bondedList" + bondedList.toString());
//                Log.d(TAG, "SearchCompleted: newList" + newList.toString());
//                sendMessage(0, "搜索完成,点击列表进行连接！");
//                mDevices.clear();
//                mDevices.addAll(newList);
//                mAdapter.notifyDataSetChanged();
//                deviesinfo.setVisibility(View.GONE);
//                devieslist.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onError(Exception e) {
//                sendMessage(0, "搜索失败");
//            }
//        };
//        onConnectListener = new OnConnectListener() {
//            @Override
//            public void onConnectStart() {
//                sendMessage(0, "开始连接");
//                Log.i("blue", "onConnectStart");
//            }
//
//            @Override
//            public void onConnectting() {
//                sendMessage(0, "正在连接..");
//                Log.i("blue", "onConnectting");
//            }
//
//            @Override
//            public void onConnectFailed() {
//                sendMessage(0, "连接失败！");
//                Log.i("blue", "onConnectFailed");
//
//            }
//
//            @Override
//            public void onConectSuccess(String mac) {
//                sendMessage(4, "连接成功 MAC: " + mac);
//                Log.i("blue", "onConectSuccess");
//            }
//
//            @Override
//            public void onError(Exception e) {
//                sendMessage(0, "连接异常！");
//                Log.i("blue", "onError");
//            }
//        };
//        onSendMessageListener = new OnSendMessageListener() {
//            @Override
//            public void onSuccess(int status, String response) {
//                sendMessage(0, "发送成功！");
//                Log.i("blue", "send message is success ! ");
//            }
//
//            @Override
//            public void onConnectionLost(Exception e) {
//                sendMessage(0, "连接断开！");
//                Log.i("blue", "send message is onConnectionLost ! ");
//            }
//
//            @Override
//            public void onError(Exception e) {
//                sendMessage(0, "发送失败！");
//                Log.i("blue", "send message is onError ! ");
//            }
//        };
//        onReceiveMessageListener = new OnReceiveMessageListener() {
//
//
//            @Override
//            public void onProgressUpdate(String what, int progress) {
//                sendMessage(1, what);
//            }
//
//            @Override
//            public void onDetectDataUpdate(String what) {
//                sendMessage(3, what);
//            }
//
//            @Override
//            public void onDetectDataFinish() {
//                sendMessage(2, "接收完成！");
//                Log.i("blue", "receive message is onDetectDataFinish");
//            }
//
//            @Override
//            public void onNewLine(String s) {
//                sendMessage(3, s);
//            }
//
//            @Override
//            public void onConnectionLost(Exception e) {
//                sendMessage(0, "连接断开");
//                Log.i("blue", "receive message is onConnectionLost ! ");
//            }
//
//            @Override
//            public void onError(Exception e) {
//                Log.i("blue", "receive message is onError ! ");
//            }
//        };
//        bluemanage = BlueManager.getInstance(getApplicationContext());
//        bluemanage.setOnSearchDeviceListener(onSearchDeviceListener);
//        bluemanage.setOnConnectListener(onConnectListener);
//        bluemanage.setOnSendMessageListener(onSendMessageListener);
//        bluemanage.setOnReceiveMessageListener(onReceiveMessageListener);
//        bluemanage.requestEnableBt();
//    }

    /**
     * 为控件添加事件监听
     */
    public void initLisetener() {


        final ExecutorService executorService = Executors.newFixedThreadPool(4);

        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                final String mac = mDevices.get(position).getAddress();

                Log.e("TAG","mac==="+mac);
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        bluemanage.connectDevice(mac);
//
//                    }
//                }).start();

                //停止获取列表
                if (mScanning) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                }
                statusView.setText("正在建立连接请骚后");
                boolean isInitSucc = mBluetoothLeService.connect(mac);



            }
        });

        findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View view) {
//                bluemanage.setReadVersion(false);
//                bluemanage.searchDevices();
                statusView.setText("开始搜索中·····");
                mDevices.clear();
                if (mAdapter!=null){
                    mAdapter.notifyDataSetChanged();
                }
                devieslist.setVisibility(View.VISIBLE);
                scanLeDevice(true);
            }
        });

        findViewById(R.id.get_sn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageBean item = new MessageBean(TypeConversion.getDeviceVersion());
                bluemanage.setReadVersion(true);
                bluemanage.sendMessage(item, true);
            }
        });

        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                bluemanage.closeDevice();
//                contextView.setText(null);
//                devieslist.setVisibility(View.VISIBLE);
//                deviesinfo.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                bluemanage.setReadVersion(false);
//                progress = 0;
//                progressBar.setProgress(progress);
//                stringBuilder.delete(0, stringBuilder.length());
//                contextView.setText("");
//                MessageBean item = new MessageBean(TypeConversion.startDetect());
//                bluemanage.sendMessage(item, true);

            }
        });
    }

    /**
     * @param type    0 修改状态  1 更新进度  2 体检完成  3 体检数据进度
     * @param context
     */
    public void sendMessage(int type, String context) {
        if (handler != null) {
            Message message = handler.obtainMessage();
            message.what = type;
            message.obj = context;
            handler.sendMessage(message);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 2) {
            if (permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION) && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.
                        permission.ACCESS_COARSE_LOCATION)) {
                    return;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluemanage != null) {
            bluemanage.close();
            bluemanage = null;
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }

        unbindService(mServiceConnection);
        unregisterReceiver(mGattUpdateReceiver);


    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e("", "Unable to initialize Bluetooth");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void run() {
                    Log.e("TAG","停止刷新=======");
                    statusView.setText("搜索完成");
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mAdapter.notifyDataSetChanged();

                }
            }, SCAN_PERIOD);
            Log.e("TAG","检所完成");
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private boolean mScanning;
    private BluetoothAdapter mBluetoothAdapter;
    private static final long SCAN_PERIOD = 10000;


    private Handler mHandler  =  new Handler(){
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void handleMessage(Message msg) {



//            if (mBluetoothLeService!=null){
//                findPeerDevice((byte)msg.arg1, (String)msg.obj);
//            }
            Log.e("TAG","延时任务=======");
//            if(isMapWaring.get((String)msg.obj)){
//                Message message=obtainMessage(msg.what);
//                message.arg1=msg.arg1;
//                message.obj=msg.obj;
//                sendMessageDelayed(message, 15*1000);
//            }
        }
    };


    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {



                    if(!mDevices.contains(device)) {

                        Log.e("TAG","device.getName()=="+device.getName());
                    if (device.getName()!=null){//名字不为空添加

                        for (int i=0;i<mDevices.size();i++){
                            if (device.getAddress().equals(mDevices.get(i).getAddress())){
                                return;
                            }
                        }
                        //添加数据
                        BleDevice bleDevice = new BleDevice();
                        bleDevice.setAddress(device.getAddress());
                        bleDevice.setName(device.getName());
                        bleDevice.setRssi(rssi);
                        mDevices.add(bleDevice);

//                        Log.e("TAG","数据===="+mDevices.get(0).getName());
                    }

//                        mAdapter.notifyDataSetChanged();
                        Log.e("TAG","集合数据：：："+mDevices.size());
                    }
                }
            })      ;
        }
    };


    /*
     * 发送报警指令 //0 停止报警 //1 报警不闪灯 //2 报警闪灯
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void findPeerDevice(byte waringCommand, final String address) {
        if (waringCommand != 0 && waringCommand != 1 && waringCommand != 2) {// 错误的指令
            return;
        }
        UUID serviceUUID = UUID_IMMIDIATE_ALERT_SERVICEL;
        UUID characteristicUUID = UUID_ALERT_LEVEL_CHARACTERISTIC;
        BluetoothGattCharacteristic characteristic = getCharacteristic(address, serviceUUID, characteristicUUID);
        if (characteristic == null) {
            return;
        }
        final int charaProp = characteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            byte value[] = { waringCommand };
            characteristic.setValue(value);
            if (mBluetoothLeService!=null){
                mBluetoothLeService.getmMapBleGatt().get(address).writeCharacteristic(characteristic);
            }else{
                Toast.makeText(MainActivity.this,"当前蓝牙服务挂掉",Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 获取某个特定设备的某个服务下的某个特征值
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private BluetoothGattCharacteristic getCharacteristic(String address, UUID serviceUUID, UUID characteristicUUID) {
        if (mBluetoothLeService!=null){
            BluetoothGatt gatt =mBluetoothLeService.getmMapBleGatt().get(address);
            return getCharacteristic(gatt, serviceUUID, characteristicUUID);
        }else{
            Toast.makeText(MainActivity.this,"当前蓝牙服务挂掉",Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    // 获取某个特定设备的某个服务下的某个特征值
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
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

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_DEV_FIND);
        intentFilter.addAction(BluetoothLeService.ACTION_DEV_SINGLE_CLICK);
        intentFilter.addAction(BluetoothLeService.ACTION_DEV_ANTILOSS);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_DEV_WARING_STOP);
        return intentFilter;
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            final String action = intent.getAction();
            Log.e("TAG","mGattUpdateReceiver====**************"+action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                statusView.setText("连接成功");
                mConnected = true;
//                if (mLeDeviceListAdapter!=null){
//                    mLeDeviceListAdapter.notifyDataSetChanged();
//                }
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.e("TAG","====$%$%$%$%$%$%$===========");
                //连接断开记录
                mConnected = false;
                statusView.setText("断开连接");
                Log.e("TAG","执行断开广播了====");
//                Bundle bundle = intent.getExtras();
//                String devAddress = bundle.getString("device_address", null);
//                mCurrentLocationDeviceAddress=devAddress;
//                changeDeviceStatus(devAddress, BleDevice.DEVICE_DISCONNECTED);

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
//                displayGattServices(mBluetoothLeService.getSupportedGattServices());
//
//                Bundle bundle = intent.getExtras();
//                String devAddress = bundle.getString("device_address", "");
//                changeDeviceStatus(devAddress, BleDevice.DEVICE_CONNECTED);
                //设备连上之后将地址从丢失列表移除
                //请求接口


            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {


            }else if (action.equals(BluetoothLeService.ACTION_DEV_FIND)) {
                Log.i("CALM","Mainactivity ---ACTION_DEV_FIND  -<-- -----------" );
//                Bundle bundle = intent.getExtras();
//                String devAddress = bundle.getString("device_address", null);
//                findWaring(devAddress);
            }else if (action.equals(mBluetoothLeService.ACTION_DEV_WARING_STOP)) {
//                Bundle bundle = intent.getExtras();
//                String devAddress = bundle.getString("device_address", null);
//                waringStop(devAddress);
            } else if (BluetoothLeService.ACTION_DEV_ANTILOSS.equals(action)){// 防丢报警

//                Bundle bundle = intent.getExtras();
//                String devAddress = bundle.getString("device_address", null);
//                antilossWaring(devAddress);

            }else if (action.equals(BluetoothLeService.ACTION_DEV_SINGLE_CLICK)) {
                Bundle bundle = intent.getExtras();
                String devAddress = bundle.getString("device_address", null);
                Log.i("TAG", "Mainactivity ---ACTION_DEV_SINGLE_CLICK  -<-----------");

//                Boolean isWaring = EquipmentFragment.isMapWaring.get(devAddress);
//                Log.i("TAG", "Mainactivity ---isWaring -------:" + isWaring);
//                if (isWaring != null && isWaring) {// 在Waring，停止Waring
//                    //停止报警
//                    broadcastUpdate(BluetoothLeService.ACTION_DEV_WARING_STOP, devAddress);
//                }
            }
        }
    };

}