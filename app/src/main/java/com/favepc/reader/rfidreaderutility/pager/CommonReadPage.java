package com.favepc.reader.rfidreaderutility.pager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.favepc.reader.rfidreaderutility.AppContext;
import com.favepc.reader.rfidreaderutility.MainActivity;
import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.rfidreaderutility.object.CustomBaseKeyboard;
import com.favepc.reader.rfidreaderutility.object.CustomKeyboardManager;
import com.favepc.reader.service.ReaderService;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Bruce_Chiang on 2017/3/30.
 */

public class CommonReadPage {

    public static final String PROCESS_COMMAND = "PROCESS_COMMAND";
    public static final String PROCESS_DATA = "PROCESS_DATA";
    public static final String COMMON_ACTION_READ = "COMMON_ACTION_READ";
    public static final String COMMON_ACTION_READ_END = "COMMON_ACTION_READ_END";
    public static final String COMMON_ACTION_EPC_ONE = "COMMON_ACTION_EPC_ONE";
    public static final String PROCESS_ARGUMENT = "PROCESS_ARGUMENT";

    private Context         mContext;
    private Activity        mActivity;
    private AppContext      mAppContext;
    private LayoutInflater  mInflater;
    private View            mViewRead;

    private Drawable        mDrawableOK, mDrawableError;
    private Button          mButton;
    private CustomKeyboardManager mCustomKeyboardManager;
    private CustomBaseKeyboard HexKeyboard;
    private ArrayList<HashMap<String, String>> mProcessList;
    private ReaderService mReaderService;

    private boolean         mIsCheckSelectAdressArgs = false, mIsCheckSelectLengthArgs = false, mIsCheckSelectDataArgs = false, mIsCheckAccessPassword = false;
    private boolean         mIsCheckReadAddressArgs = false, mIsCheckReadLengthArgs = false;

    private boolean 		    mRunningFlag = false;

    public CommonReadPage(Context context, Activity act, LayoutInflater inflater, ReaderService rs, CustomKeyboardManager ckm) {
        this.mContext = context;
        this.mActivity = act;
        this.mInflater = inflater;
        this.mReaderService = rs;
        this.mCustomKeyboardManager = ckm;
        this.mAppContext = (AppContext) context.getApplicationContext();

        this.mProcessList = new ArrayList<>();

        this.mViewRead = this.mInflater.inflate(R.layout.adapter_common_pager3, null);

        this.mDrawableOK 	= ContextCompat.getDrawable(this.mActivity, R.mipmap.ic_check_black_48dp);
        this.mDrawableError = ContextCompat.getDrawable(this.mActivity, R.mipmap.ic_close_black_48dp);
        this.mDrawableOK.setBounds(0, 0, 48, 48);
        this.mDrawableError.setBounds(0, 0, 48, 48);


        ArrayAdapter<CharSequence> lunchList = ArrayAdapter.createFromResource(this.mContext, R.array.common_memory_bank,
                R.layout.spinner_style);

        this.mButton = (Button)this.mViewRead.findViewById(R.id.adapter_common_pager3_read);
        this.mButton.setOnClickListener(new View.OnClickListener() {
            HashMap<String, String> _item;
            byte[] _d;

            @Override
            public void onClick(View view) {
                    if (((MainActivity) mContext).isConnected()) {

                        mProcessList.clear();
                        if (mRunningFlag){
                            mRunningFlag = false;
                            mButton.setText("Read");
                            sendBroadcast(COMMON_ACTION_READ_END, null);
                        } else {
                            mRunningFlag = true;

                            mButton.setText("Stop");
                            _d = mReaderService.Q();
                            _item = new HashMap<String, String>();
                            _item.put(PROCESS_COMMAND, "Q");
                            _item.put(PROCESS_DATA, ReaderService.Format.bytesToString(_d));
                            mProcessList.add(_item);

                            _d = mReaderService.W("3", "0100", "01", "FFFF");
                            _item = new HashMap<String, String>();
                            _item.put(PROCESS_COMMAND, "W");
                            _item.put(PROCESS_DATA, ReaderService.Format.bytesToString(_d));
                            mProcessList.add(_item);

                            _d = mReaderService.R("3", "0100", "1");
                            _item = new HashMap<String, String>();
                            _item.put(PROCESS_COMMAND, "R");
                            _item.put(PROCESS_DATA, ReaderService.Format.bytesToString(_d));
                            mProcessList.add(_item);

                            sendBroadcast(COMMON_ACTION_READ, mProcessList);
                        }
                    }

                    else {
                        Toast.makeText(mContext, "All of the communication interface are unlinked.", Toast.LENGTH_SHORT).show();
                    }
                }
//            }
        });
    }

    public View getView() {
        return this.mViewRead;
    }

    public void setInit() {
        mRunningFlag = false;
        sendBroadcast(COMMON_ACTION_READ_END, null);
    }
    private void sendBroadcast(@NonNull String action, ArrayList<HashMap<String, String>> al) {
        Intent i = new Intent(action);
        i.putExtra(PROCESS_ARGUMENT, al);
        mContext.sendBroadcast(i);
    }
}
