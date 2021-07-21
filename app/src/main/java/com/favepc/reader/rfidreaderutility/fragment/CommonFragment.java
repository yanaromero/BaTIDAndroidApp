package com.favepc.reader.rfidreaderutility.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.favepc.reader.rfidreaderutility.AppContext;
import com.favepc.reader.rfidreaderutility.MainActivity;
import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.rfidreaderutility.adapter.CommonListAdapter;
import com.favepc.reader.rfidreaderutility.adapter.CommonPageAdapter;
import com.favepc.reader.rfidreaderutility.adapter.WrapContentViewPager;
import com.favepc.reader.rfidreaderutility.object.Common;
import com.favepc.reader.rfidreaderutility.object.CustomKeyboardManager;
import com.favepc.reader.rfidreaderutility.pager.CommonReadPage;
import com.favepc.reader.service.OTGService;
import com.favepc.reader.service.ReaderService;
import com.favepc.reader.rfidreaderutility.ApiHolder;
import com.favepc.reader.rfidreaderutility.TempData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Bruce_Chiang on 2017/3/10.
 */

@SuppressLint("ValidFragment")
public class CommonFragment extends Fragment {

    public static final String PROCESS_COMMAND = "PROCESS_COMMAND";
    public static final String PROCESS_DATA = "PROCESS_DATA";
    public int tempRaw = 0;
    public double tempConvert = 0.0;

    private Context	mContext;
    private Activity mActivity;
    private AppContext mAppContext;
    private View mCommonView = null;
    private WrapContentViewPager mViewPager;
    private CommonListAdapter mCommonListAdapter;
    private ArrayList<Common> mCommons = new ArrayList<Common>();
    private LayoutInflater mLayoutInflater;

    //pager
    private List<View> mListCommonPageViews;
    private CommonPageAdapter mCommonPageAdapter;
    public static final int PAGE_EPC = 0;
    public static final int PAGE_TID = 1;
    public static final int PAGE_READ = 2;

    private int	mCommonPagePosition = PAGE_EPC;
    private CommonReadPage mCommonReadPage;

    private ReaderService mReaderService;
    private CommonMsgReceiver mCommnoMsgReceiver;
    private boolean mRunningFlag = false;
    private String mProcessCommand;
    private byte[] mProcess;
    private ArrayList<HashMap<String, String>> mProcessList;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    private CustomKeyboardManager mCustomKeyboardManager;

    private String epcPrev;
    private String epcCur;
    private String write;
    private String readPrev = "";
    private String readCur;
    private String readerID;
    private String datetime;

    ApiHolder apiHolder;

    public CommonFragment() {
        super();
    }
    public CommonFragment(Context context, Activity activity) {
        this.mContext = context;
        this.mActivity = activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        this.mAppContext = (AppContext) context.getApplicationContext();
        this.mCustomKeyboardManager = this.mAppContext.getKeyboard();
        this.mActivity = getActivity();
        this.mAppContext = (AppContext) context.getApplicationContext();
        this.mReaderService = new ReaderService();
        this.mCommnoMsgReceiver = new CommonMsgReceiver();
        this.mContext.registerReceiver(this.mCommnoMsgReceiver, new IntentFilter(OTGService.OTG_ACTION_DISCONNECTED_COMMON));

        this.mContext.registerReceiver(this.mCommnoMsgReceiver, new IntentFilter(CommonReadPage.COMMON_ACTION_READ));
        this.mContext.registerReceiver(this.mCommnoMsgReceiver, new IntentFilter(CommonReadPage.COMMON_ACTION_READ_END));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

//        Gson gson = new GsonBuilder()
//                .setLenient()
//                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.4:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiHolder = retrofit.create(ApiHolder.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (this.mCommonView == null) {

            this.mLayoutInflater = inflater;
            this.mCommonView = inflater.inflate(R.layout.fragment_common, container, false);

            this.mCommonListAdapter = new CommonListAdapter(this.mContext, R.layout.adapter_common, this.mCommons);
            ListView lv = (ListView)this.mCommonView.findViewById(R.id.common_lvMsg);
            lv.setAdapter(this.mCommonListAdapter);


            Runnable mPendingRunnable = new Runnable() {
                @Override
                public void run() {
                    //create pager: epc,tid, read, write
                    {
                        mCommonReadPage = new CommonReadPage(mContext, mActivity, mLayoutInflater, mReaderService, mCustomKeyboardManager);
                    }
                    //add page view
                    {
                        mListCommonPageViews = new ArrayList<View>();
                        mListCommonPageViews.add(mCommonReadPage.getView());
                    }
                    mCommonPageAdapter = new CommonPageAdapter(mContext, mListCommonPageViews, 0);

                    mViewPager = (WrapContentViewPager) mCommonView.findViewById(R.id.common_viewPager);
                    mViewPager.setAdapter(mCommonPageAdapter);
                    mViewPager.setCurrentItem(0);
                    mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                        }

                        @Override
                        public void onPageSelected(int position) {
                            initCommon();
                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {

                        }
                    });
                }
            };
            if (mPendingRunnable != null) {
                mCommonHandler.post(mPendingRunnable);
            }


        }
        return this.mCommonView;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            initCommon();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        initCommon();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext.unregisterReceiver(mCommnoMsgReceiver);
        mContext = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mActivity.getMenuInflater().inflate(R.menu.fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                if (mCommons != null && mCommonListAdapter != null) {
                    mCommons.clear();
                    mCommonListAdapter.notifyDataSetChanged();
                }
                return true;
        }
        return false;
    }

    public class CommonMsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case OTGService.OTG_ACTION_DISCONNECTED_COMMON:
                    initCommon();
                    break;
                case CommonReadPage.COMMON_ACTION_READ_END:
                    mProcessCommand = CommonReadPage.COMMON_ACTION_READ_END;
                    mCommonHandler.removeCallbacks(mRunnableBackground);
                    break;
                case CommonReadPage.COMMON_ACTION_READ:
                    mProcessCommand = CommonReadPage.COMMON_ACTION_READ;
                    mProcessList = (ArrayList<HashMap<String, String>>) intent.getExtras().get(CommonReadPage.PROCESS_ARGUMENT);
                    mCommonHandler.post(mRunnableBackground);
                    break;
            }
        }
    }


    /**
     * init and stop all action
     * */
    private void initCommon() {
        switch(mCommonPagePosition) {
            case PAGE_READ:
                mCommonReadPage.setInit();
                break;
        }
        mCommonHandler.removeCallbacks(mRunnableBackground);
    }


    /**
     * @param common Common class
     */
    private void updateView(Common common) {
        if (common.Title())
            mAppContext.setAck(true);
        else
            mAppContext.setAck(false);
        this.mCommons.add(mCommonListAdapter.getCount(), common);
        this.mCommonListAdapter.notifyDataSetChanged();

        if (mCommonListAdapter.getCount() > 300) {
            mCommons.clear();
            mCommonListAdapter.notifyDataSetChanged();
        }
    }

    private void createPost(String epcPost, String tempPost, String locationPost, String datetimePost) {

        TempData tempData = new TempData(tempPost,epcPost, locationPost, datetimePost);

        Call<TempData> call = apiHolder.createPost(tempData);

        call.enqueue(new Callback<TempData>() {
            @Override
            public void onResponse(Call<TempData> call, Response<TempData> response) {
                if(!response.isSuccessful()){
                    Log.d("TRACK", "onResponse: " + response.code());
                }
                Log.d("TRACK","body response:" + response.body());
            }

            @Override
            public void onFailure(Call<TempData> call, Throwable throwable) {
                Log.d("TRACK","Failure message:" + throwable.getMessage());
            }
        });

    }

    private Handler mCommonHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 1://[TX]
                    ((MainActivity) mActivity).sendData((byte[])msg.obj);
                    break;
                case 2://[RX]
                    String s = ReaderService.Format.removeCRLF((String)msg.obj);

                    if(s.contains("R")){
                        readCur = s;
                        if(
                                !readCur.equals("R")&& // checks if user memory reading was successful
                                !epcCur.equals("Q")&&  // checks if EPC reading was successful
                                !write.equals("W") // checks if writing to memory was successful
                        ) {
                            String remove = epcCur.substring(1,25);
                            epcCur = epcCur.replace(remove, "").replace("Q","");
                            remove = epcCur.substring(8);
                            epcCur = epcCur.replace(remove,"");

                            if (readCur.equals("R8100") && !readPrev.equals(readCur)){
                                    updateView(new Common(true,
                                            epcCur,
                                            mDateFormat.format(new Date())));

                                    readPrev = readCur;

                                    updateView(new Common(true,
                                            "Your tag has low battery.",
                                            mDateFormat.format(new Date())));
                                    epcPrev = "";

                            } else if (readCur.equals("R0100")){
                                updateView(new Common(true,
                                        "Temperature reading was invalid. Please try again.",
                                        mDateFormat.format(new Date())));
                                epcPrev = "";
                                readPrev = "";
                            }
                            else if(!readCur.equals("R8100")){
                                if(!epcCur.equals(epcPrev))
                                {
                                    updateView(new Common(true,
                                            epcCur,
                                            mDateFormat.format(new Date())));

                                    readCur = readCur.replace("R", "");
                                    tempRaw = Integer.parseInt(readCur, 16);
                                    tempConvert = tempRaw;
                                    tempConvert = tempConvert / 4;
                                    readCur = "Temperature: " + String.valueOf(tempConvert) + " C";

                                    readerID = readerID.replace("S","");

                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    datetime = sdf.format(new Date());

                                    createPost(epcCur,String.valueOf(tempConvert),readerID,datetime);
                                    updateView(new Common(true,
                                            readCur,
                                            mDateFormat.format(new Date())));
                                    epcPrev = epcCur;
                                    readPrev = "";
                                }

                            }

                        }
                    }

                    else if(s.contains("W")) {
                        write = s;
                    }
                    else if (s.contains("Q")){
                        epcCur = s;
                    }
                    else if (s.contains("S")){
                        readerID = s;
                    }

                    break;
            }

            super.handleMessage(msg);
        }
    };

    private Runnable mRunnableBackground = new Runnable() {
        @Override
        public void run() {

            new Thread(new Runnable() {
                int _timeOutx10 = 100, _processIndex = 0, _index;
                int _error = 0;
                byte[] _bsData;
                String strData;
                String strSubData = "";
                HashMap<String, String> _item;

                @Override
                public void run() {
                    //initialization ack status
                    mAppContext.setAck(true);

                    while(true) {
                        _timeOutx10 = 100;
                        while(!mAppContext.getAck() && _timeOutx10 > 0) {
                            try {
                                Thread.sleep(10);
                                _timeOutx10--;
                            } catch (InterruptedException e) {e.printStackTrace();}
                        }

                        if (!((MainActivity) mActivity).isConnected()) {
                            mActivity.runOnUiThread(new Runnable() {
                                public void run() {
                                    initCommon();
                                }
                            });
                        }



                        //[TX]
                        switch (mProcessCommand) {
                            case CommonReadPage.COMMON_ACTION_READ:

                               /* if (_processIndex == mProcessList.size())
                                    return;*/
                                _item = mProcessList.get(_processIndex);
                                mCommonHandler.sendMessage(mCommonHandler.obtainMessage(1, ReaderService.Format.stringToBytes(_item.get(PROCESS_DATA))));
                                break;
                        }


                        _timeOutx10 = 100;
                        while(_timeOutx10 > 0) {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {e.printStackTrace();}

                            if (((MainActivity) mActivity).checkData() > 0) {
                                _bsData = ((MainActivity) mActivity).getData();
                                if (_bsData == null) {
                                    _error = -1;
                                    return;
                                }

                                if (_bsData.length > 0) {
                                    strData = new String(_bsData);
                                    if (strSubData.length() > 0) {
                                        strData = strSubData + strData;
                                        strSubData = "";
                                    }

                                    if ((_index = strData.indexOf(ReaderService.COMMAND_END)) != -1) {
                                        mCommonHandler.sendMessage(mCommonHandler.obtainMessage(2, strData.substring(0, _index + 2)));
                                        _timeOutx10 = 0;
                                    }
                                    else {
                                        strSubData = strData;
                                        _timeOutx10++;
                                    }
                                }
                            }
                            _timeOutx10--;
                        } //while(_timeOutx10 > 1)

                        //error process
                        switch(_error) {
                            case -1:
                                mActivity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(mContext, "No data callback. Stop the process", Toast.LENGTH_SHORT).show();
                                        initCommon();
                                    }
                                });

                                return;
                        }

                        _processIndex = (_processIndex+1)%mProcessList.size();



                        //read, write, lock
                        if (
//                                mProcessCommand.equals(CommonReadPage.COMMON_ACTION_READ) ||
                                mProcessCommand.equals(CommonReadPage.COMMON_ACTION_READ_END)) {
                            if (_processIndex == mProcessList.size()){

                                return;
                            }
                        }
                        mAppContext.setAck(true);
                    }
                }
            }).start();
        }
    };
}
