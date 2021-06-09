package com.favepc.reader.rfidreaderutility.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.favepc.reader.rfidreaderutility.MainActivity;
import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.service.ReaderService;

import java.util.ArrayList;

public class DemoIRFragment extends Fragment {

    private Context mContext;
    private Activity mActivity;
    private View mDemoIRView = null;
    private Button mBtnClear;
    private ReaderService mReaderService;
    private ListView lvIRMessage;
    private ArrayList<String> mDemoIRs = new ArrayList<String>();
    private ArrayAdapter<String> mDemoIRAdapter;
    private boolean m_bDemoIRAutoDetect = false;
    private int mCount = 0;

    public DemoIRFragment() { super();}
    @SuppressLint("ValidFragment")
    public DemoIRFragment(Context context, Activity activity) {
        mContext = context;
        mActivity = activity;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        this.mActivity = getActivity();
        this.mReaderService = new ReaderService();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (this.mDemoIRView == null) {
            this.mDemoIRView = inflater.inflate(R.layout.fragment_demoir, container, false);

            this.mDemoIRAdapter = new ArrayAdapter(this.mContext, R.layout.adapter_demoir, mDemoIRs);
            this.lvIRMessage = (ListView)this.mDemoIRView.findViewById(R.id.fragment_demoir_lv_msg);
            this.lvIRMessage.setAdapter(this.mDemoIRAdapter);

            this.mBtnClear = (Button)this.mDemoIRView.findViewById(R.id.fragment_demoir_btn_clear);
            this.mBtnClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDemoIRs != null)
                        mDemoIRs.clear();
                    if (mDemoIRAdapter != null)
                        mDemoIRAdapter.notifyDataSetChanged();
                }
            });

            if (((MainActivity) mContext).isConnected())
            {
                m_bDemoIRAutoDetect = true;
                mAutoHandler.post(mRunnableAutoBackground);
            }
        }

        return this.mDemoIRView;
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            m_bDemoIRAutoDetect = false;
            mAutoHandler.removeCallbacks(mRunnableAutoBackground);
        }
        else {
            if (((MainActivity) mContext).isConnected())
            {
                m_bDemoIRAutoDetect = true;
                mAutoHandler.post(mRunnableAutoBackground);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        m_bDemoIRAutoDetect = false;
        mAutoHandler.removeCallbacks(mRunnableAutoBackground);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    StringBuilder _SubRaw = new StringBuilder(256);
    Byte _SubRawOld = 0;
    private Runnable mRunnableAutoBackground = new Runnable() {
        @Override
        public void run() {
            new Thread(new Runnable() {
                int index;
                byte[] bsData;
                String strData;
                String strSubData = "";

                @Override
                public void run() {

                    while(m_bDemoIRAutoDetect) {
                        if (!((MainActivity) mContext).isConnected()) {
                            mActivity.runOnUiThread(new Runnable() {
                                public void run() {
                                    m_bDemoIRAutoDetect = false;
                                    mAutoHandler.removeCallbacks(mRunnableAutoBackground);
                                }
                            });
                            return;
                        }

                        //[RX]
                        /*try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {e.printStackTrace();}*/

                        if (((MainActivity) mContext).checkData() > 0) {
                            bsData = ((MainActivity) mContext).getData();
                            if (bsData == null) return;
                            for (int i = 0; i < bsData.length; i++)
                            {
                                if (bsData[i] != 0)
                                {
                                    _SubRaw.append((char)bsData[i]);
                                    if (bsData[i] == 0x0A)
                                    {
                                        if (_SubRawOld == 0x0D)
                                        {
                                            //_ReceiveBuffer = _SubRaw.ToString();
                                            //CombineDataReceive(_ReceiveBuffer);
                                            Message msg = new Message();
                                            msg.what = 2;
                                            msg.obj = _SubRaw.toString();
                                            mAutoHandler.sendMessage(msg);
                                            _SubRaw.setLength(0);
                                        }
                                    }
                                    _SubRawOld = bsData[i];
                                }
                            }
                            /*if (bsData.length > 0) {

                                strData = new String(bsData);
                                if (strSubData.length() > 0) {
                                    strData = strSubData + strData;
                                    strSubData = "";
                                }

                                if ((index = strData.indexOf(ReaderService.COMMAND_END)) != -1) {

                                    Message msg = new Message();
                                    msg.what = 2;
                                    msg.obj = strData.substring(0, index + 2);
                                    mAutoHandler.sendMessage(msg);
                                }
                                else {
                                    strSubData = strData;
                                }
                            }*/
                            mCount = 0;
                        }
                        else
                        {
                            mCount++;
                            if (mCount > 200)
                            {
                                mCount = 0;
                                if (strSubData.length() > 0) {
                                    Message msg = new Message();
                                    msg.what = 2;
                                    msg.obj = strSubData;
                                    mAutoHandler.sendMessage(msg);
                                    strSubData = "";
                                }
                            }
                        }
                    }
                }
            }).start();
        }
    };

    private Handler mAutoHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            boolean _bCompare = false;

            switch (msg.what) {
                case 1:
                    //mTextViewCount.setText(Integer.toString((int)msg.obj));
                    break;
                case 2:
                    //byte[] b = ReaderService.Format.stringToBytes((String)msg.obj);
                    //String s = ReaderService.Format.bytesToString(b);
                    mDemoIRs.add(ReaderService.Format.showCRLF((String)msg.obj));
                    mDemoIRAdapter.notifyDataSetChanged();
                    lvIRMessage.setSelection(mDemoIRAdapter.getCount() - 1);
                    break;
            }
            super.handleMessage(msg);
        }
    };
}
