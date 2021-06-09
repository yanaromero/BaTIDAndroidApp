package com.favepc.reader.rfidreaderutility.fragment;
/**
 * *************************************************************************************************
 * FILE:			BLEFragment.java
 * ------------------------------------------------------------------------------------------------
 * COMPANY:			FAVEPC
 * VERSION:			V1.0
 * CREATED:			2017/3/9
 * DATE:
 * AUTHOR:			Bruce_Chiang
 * ------------------------------------------------------------------------------------------------
 * \PURPOSE:
 * - None
 * \NOTES:
 * - None
 * \Global Variables:
 * - None
 * ------------------------------------------------------------------------------------------------
 * REVISION		Date			User	Description
 * V1.0			2017/12/30     	Bruce	1.First create version
 * <p/>
 * ------------------------------------------------------------------------------------------------
 * *************************************************************************************************
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.favepc.reader.rfidreaderutility.MainActivity;
import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.rfidreaderutility.adapter.BLEListAdapter;
import com.favepc.reader.rfidreaderutility.object.BLEDevice;
import com.favepc.reader.service.BluetoothService;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ValidFragment")
public class BLEFragment extends Fragment {

    private final int REQUEST_COARSE_LOCATION_PERMISSIONS = 689;
    private Context	mContext;
    private Activity mActivity;
    private View mBLEView = null;
    private Button mBtnSearch;
    private TextView mTextViewMsgStatus, mTextViewMsg;
    private ProgressBar mProgressBar;
    private BLEListAdapter mBLEListAdapter;
    private ArrayList<BLEDevice> mBLEDevices = new ArrayList<BLEDevice>();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private Handler mHandler;
    private boolean m_bScanning = false;
    private boolean m_bSupportBLE = false;
    private BLEMsgReceiver mBLEMsgReceiver;

    public BLEFragment() { super();}
    public BLEFragment(Context context, Activity activity) {
        mContext = context;
        mActivity = activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = getActivity();
        mBLEMsgReceiver = new BLEMsgReceiver();
        mContext.registerReceiver(mBLEMsgReceiver, new IntentFilter(BluetoothService.BLE_ACTION_CHANGE_INTERFACE));
        mContext.registerReceiver(mBLEMsgReceiver, new IntentFilter(BluetoothService.BLE_ACTION_GATT_CONNECTED));
        mContext.registerReceiver(mBLEMsgReceiver, new IntentFilter(BluetoothService.BLE_ACTION_GATT_DISCONNECTED));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (this.mBLEView == null) {
            this.mBLEView = inflater.inflate(R.layout.fragment_ble, container, false);

            if (Build.VERSION.SDK_INT >= 18) {
                this.mBluetoothAdapter = ((BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
            }

            this.mBLEListAdapter = new BLEListAdapter(this.mContext, R.layout.adapter_bledevice, this.mBLEDevices);
            ListView lv = (ListView)mBLEView.findViewById(R.id.ble_lvDevice);
            lv.setAdapter(this.mBLEListAdapter);
            lv.setOnItemClickListener(deviceClickListener);

            this.mHandler = new Handler();

            this.mBtnSearch = (Button)mBLEView.findViewById(R.id.ble_btnSearch);
            this.mBtnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!m_bSupportBLE) {
                        String str = Build.DEVICE + " ";
                        mTextViewMsg.setText(str.toUpperCase() + R.string.ble_msg_no_support_ble);
                    }
                    else {
                        mTextViewMsg.setText(R.string.ble_msg_start_scan);
                        mBLEListAdapter.clear();
                        scanLeClassicDevice();
                    }
                }
            });

            this.mTextViewMsgStatus = (TextView) mBLEView.findViewById(R.id.ble_tvMsgStatus);
            this.mTextViewMsg = (TextView) mBLEView.findViewById(R.id.ble_tvMsg);

            this.mProgressBar = (ProgressBar)mBLEView.findViewById(R.id.ble_progressBar);
            this.mProgressBar.setVisibility(View.GONE);
        }

        return this.mBLEView;
    }

    @Override
    public void onStart() {
        super.onStart();
        String str = Build.DEVICE + " ";
        if (android.os.Build.VERSION.SDK_INT < 18) {
            this.mTextViewMsgStatus.setText(str.toUpperCase() + getString(R.string.ble_msg_version_low));
            this.m_bSupportBLE = false;
        }
        else if(!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            this.mTextViewMsgStatus.setText(str.toUpperCase() + getString(R.string.ble_msg_no_support_ble));
            this.m_bSupportBLE = false;
        }
        else {
            this.mTextViewMsgStatus.setText(str.toUpperCase() + getString(R.string.ble_msg_support_ble));
            this.m_bSupportBLE = true;
        }

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        //mContext.unregisterReceiver(mBLEMsgReceiver);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext.unregisterReceiver(mBLEMsgReceiver);
        mContext = null;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BluetoothService.REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    scanLeClassicDevice();
                } else {
                    Toast.makeText(this.mContext, R.string.ble_msg_disenable, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private AdapterView.OnItemClickListener deviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            TextView _tvAddress = (TextView)((RelativeLayout)view).findViewById(R.id.adapter_ble_address);
            Intent _intent = new Intent(BluetoothService.BLE_ACTION_CONNECT);
            _intent.putExtra(BluetoothService.DEVICE_ADDRESS, _tvAddress.getText().toString());
            mActivity.sendBroadcast(_intent);
        }
    };


    private void scanLeClassicDevice() {

        if (ContextCompat.checkSelfPermission(this.mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this.mActivity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                showMessageOKCancel(this.mActivity, "You need to allow access to Contacts",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                ActivityCompat.requestPermissions(mActivity,
                                        new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                                        REQUEST_COARSE_LOCATION_PERMISSIONS);
                            }
                        });
                return;
            }
            ActivityCompat.requestPermissions(this.mActivity,
                    new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_COARSE_LOCATION_PERMISSIONS);
            return;
        }

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, BluetoothService.REQUEST_ENABLE_BLUETOOTH);
        }
        else {
            if (!m_bScanning) {
                m_bScanning = true;

                if (Build.VERSION.SDK_INT >= 21) {
                    mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                    settings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build();
                    filters = new ArrayList<ScanFilter>();
                }
                this.mProgressBar.setVisibility(View.VISIBLE);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT < 21 ) {
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        } else {
                            mLEScanner.stopScan(mScanCallback);

                        }
                        mProgressBar.setVisibility(View.GONE);
                        mTextViewMsg.setText(R.string.ble_msg_stop_scan);
                        m_bScanning = false;
                    }
                }, 5000);

                if (Build.VERSION.SDK_INT < 21) {
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                } else {
                    mLEScanner.startScan(filters, settings, mScanCallback);
                }
            }
        }
    }

    private void showMessageOKCancel(Activity cxt, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(cxt).setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
    {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord)
        {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    BLEDevice info = new BLEDevice(device.getName(), String.valueOf(rssi), device.getAddress());
                    boolean _b = false;
                    for (int i = 0; i < mBLEListAdapter.getCount(); i++) {
                        if (mBLEListAdapter.getItem(i).getAddress().equals(info.getAddress())) {
                            _b = true;
                            break;
                        }
                    }
                    if (!_b)
                    {
                        mBLEListAdapter.add(info);
                    }
                }
            });
        }
    };

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            //super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            BLEDevice info = new BLEDevice(device.getName(), String.valueOf(result.getRssi()), device.getAddress());
            boolean _b = false;
            for (int i = 0; i < mBLEListAdapter.getCount(); i++) {
                if (mBLEListAdapter.getItem(i).getAddress().equals(info.getAddress())) {
                    _b = true;
                    break;
                }
            }
            if (!_b)
            {
                mBLEListAdapter.add(info);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };


    public class BLEMsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothService.BLE_ACTION_CHANGE_INTERFACE:
                    mTextViewMsg.setText(getString(R.string.ble_msg_stop_service));
                    mActivity.sendBroadcast(new Intent(BluetoothService.BLE_ACTION_SERVICE_STOP));
                    break;
                case BluetoothService.BLE_ACTION_GATT_CONNECTED:
                    mTextViewMsg.setText(intent.getExtras().getString(BluetoothService.STRING_DATA));
                    ((MainActivity) mContext).interfaceCtrl(BluetoothService.INTERFACE_BLE, true);
                    break;
                case BluetoothService.BLE_ACTION_GATT_DISCONNECTED:
                    mTextViewMsg.setText(intent.getExtras().getString(BluetoothService.STRING_DATA));
                    ((MainActivity) mContext).interfaceCtrl(BluetoothService.INTERFACE_BLE, false);
                    break;
            }
        }
    }
}
