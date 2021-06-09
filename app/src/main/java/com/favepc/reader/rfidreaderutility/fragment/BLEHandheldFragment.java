package com.favepc.reader.rfidreaderutility.fragment;
/**
 * *************************************************************************************************
 * FILE:			BLEHandheldFragment.java
 * ------------------------------------------------------------------------------------------------
 * COMPANY:			FAVEPC
 * VERSION:			V1.0
 * CREATED:			2018/7/18
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
 * V1.0			2018/7/18     	Bruce	1.First create version
 * <p/>
 * ------------------------------------------------------------------------------------------------
 * *************************************************************************************************
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.favepc.reader.rfidreaderutility.AppContext;
import com.favepc.reader.rfidreaderutility.MainActivity;
import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.rfidreaderutility.adapter.EditTextAdapter;
import com.favepc.reader.service.ReaderService;

import static com.favepc.reader.rfidreaderutility.MainActivity.TAG_CI_BLE;

public class BLEHandheldFragment extends Fragment {

    private Context mContext;
    private Activity mActivity;
    private AppContext mAppContext;
    private ReaderService mReaderService;
    private View mBLEHandheldView = null;
    private LayoutInflater mLayoutInflater;
    private Spinner mSpinnerMode;
    private EditTextAdapter mEtTime;
    private Drawable mDrawableOK, mDrawableError;
    private boolean mIsCheckSelectTimeArgs = false;
    private Button mBtnSet;
    private TextView mTextViewTX, mTextViewRX;
    private StringBuilder mBuilder;

    public BLEHandheldFragment() {
        super();
    }

    @SuppressLint("ValidFragment")
    public BLEHandheldFragment(Context context, Activity activity) {
        mContext = context;
        mActivity = activity;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        this.mActivity = getActivity();
        this.mAppContext = (AppContext) context.getApplicationContext();
        this.mReaderService = new ReaderService();
        if (this.mBuilder == null)
            this.mBuilder = new StringBuilder();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (this.mBLEHandheldView == null) {
            this.mLayoutInflater = inflater;
            this.mBLEHandheldView = inflater.inflate(R.layout.fragment_blehandheld, container, false);

            this.mDrawableOK 	= ContextCompat.getDrawable(this.mActivity, R.mipmap.ic_check_black_48dp);
            this.mDrawableError = ContextCompat.getDrawable(this.mActivity, R.mipmap.ic_close_black_48dp);
            this.mDrawableOK.setBounds(0, 0, 48, 48);
            this.mDrawableError.setBounds(0, 0, 48, 48);

            this.mTextViewTX = (TextView) mBLEHandheldView.findViewById(R.id.fragment_ble_handheld_tv_tx);
            this.mTextViewRX = (TextView) mBLEHandheldView.findViewById(R.id.fragment_ble_handheld_tv_rx);
            this.mBtnSet = (Button) mBLEHandheldView.findViewById(R.id.fragment_ble_handheld_set);
            this.mBtnSet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((MainActivity) mContext).isConnected() && ((MainActivity) mContext).getInterface() == TAG_CI_BLE) {
                        if (mIsCheckSelectTimeArgs)
                        {

                            mBuilder.append("@RFIDMODE").
                                    append(mAppContext.getBleHandheldSelectMode()).
                                    append(",").
                                    append(mEtTime.getText().toString());

                            mTextViewTX.setText(
                                ReaderService.Format.showCRLF(
                                    ReaderService.Format.bytesToString(
                                        mReaderService.raw(mBuilder.toString())
                                    )
                                )
                            );
                            mTextViewRX.setText("");
                            mHandler.post(mRunnableBackground);
                        }
                        else
                            Toast.makeText(mContext, "Time parameter error: 10s ~300s.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(mContext, "the communication interface is not Bluetooth or no-Link.", Toast.LENGTH_SHORT).show();
                    }
                }
            });


            this.mSpinnerMode = (Spinner) this.mBLEHandheldView.findViewById(R.id.fragment_ble_handheld_select_mode);
            this.mEtTime = (EditTextAdapter) this.mBLEHandheldView.findViewById(R.id.fragment_ble_handheld_select_time);

            ArrayAdapter<CharSequence> lunchList = ArrayAdapter.createFromResource(this.mContext, R.array.ble_handheld_rfid_mode,
                    R.layout.spinner_style);
            this.mSpinnerMode.setAdapter(lunchList);
            this.mSpinnerMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mAppContext.setBleHandheldSelectMode(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            this.mEtTime.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable.length() > 0) {
                        long val = Long.parseLong(editable.toString(), 10);
                        if (val >= 10 && val <= 300) {
                            mEtTime.setError(null, mDrawableOK);
                            mIsCheckSelectTimeArgs = true;

                        } else {
                            mEtTime.setError(null, mDrawableError);
                            mIsCheckSelectTimeArgs = false;
                        }
                    }
                    else {
                        mEtTime.setError(null, mDrawableError);
                        mIsCheckSelectTimeArgs = false;
                    }
                    mAppContext.setSelectAddress(editable.toString());
                }
            });
        }
        return this.mBLEHandheldView;
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            init();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void init() {
        //this.mProgressBar.setVisibility(View.GONE);
        //mProcessEvent.removeCallbacks(mProcessRunnable);
        mHandler.removeCallbacks(mRunnableBackground);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1:

                    break;
                case 2:
                    mTextViewRX.setText(ReaderService.Format.showCRLF((String)msg.obj));
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private Runnable mRunnableBackground = new Runnable() {
        @Override
        public void run() {
            new Thread(new Runnable() {
                int timeOutx100 = 10;
                int index;
                byte[] bsData;
                String strData;
                String strSubData = "";

                @Override
                public void run() {
                    //[TX]
                    ((MainActivity) mContext).sendData(mReaderService.raw(mBuilder.toString()));
                    mBuilder.setLength(0);

                    //[RX]
                    timeOutx100 = 10;
                    while(timeOutx100 > 1) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {e.printStackTrace();}

                        if (((MainActivity) mContext).checkData() > 0) {
                            bsData = ((MainActivity) mContext).getData();
                            if (bsData == null) return;
                            if (bsData.length > 0) {
                                strData = new String(bsData);
                                if (strSubData.length() > 0) {
                                    strData = strSubData + strData;
                                    strSubData = "";
                                }
                                if ((index = strData.indexOf(ReaderService.COMMAND_END)) != -1) {
                                    Message msg = new Message();
                                    msg.what = 2;
                                    msg.obj = strData.substring(0, index + 2);
                                    mHandler.sendMessage(msg);
                                    timeOutx100 = 1;
                                }
                                else {
                                    strSubData = strData;
                                    timeOutx100++;
                                }
                            }
                        }
                        timeOutx100--;
                    }

                }
            }).start();
        }
    };
}
