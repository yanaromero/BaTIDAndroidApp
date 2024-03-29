package com.favepc.reader.rfidreaderutility.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.favepc.reader.rfidreaderutility.ApiHolderAmbient;
import com.favepc.reader.rfidreaderutility.AppContext;
import com.favepc.reader.rfidreaderutility.LocalDbHelper;
import com.favepc.reader.rfidreaderutility.LocalTempModel;
import com.favepc.reader.rfidreaderutility.MOResult;
import com.favepc.reader.rfidreaderutility.MOValue;
import com.favepc.reader.rfidreaderutility.MainActivity;
import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.rfidreaderutility.adapter.CommonListAdapter;
import com.favepc.reader.rfidreaderutility.adapter.CommonPageAdapter;
import com.favepc.reader.rfidreaderutility.adapter.WrapContentViewPager;
import com.favepc.reader.rfidreaderutility.object.Common;
import com.favepc.reader.rfidreaderutility.pager.CommonReadPage;
import com.favepc.reader.service.OTGService;
import com.favepc.reader.service.ReaderService;
import com.favepc.reader.rfidreaderutility.ApiHolder;
import com.favepc.reader.rfidreaderutility.ApiHolderAmbient;
import com.favepc.reader.rfidreaderutility.TempData;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;

/**
 * Created by Bruce_Chiang on 2017/3/10.
 */

@SuppressLint("ValidFragment")
public class CommonFragment extends Fragment {

    public static final String PROCESS_COMMAND = "PROCESS_COMMAND";
    public static final String PROCESS_DATA = "PROCESS_DATA";
    public int tempRaw = 0;
    public  double tempRawDouble = 0.0;
    public double tempConvert = 0.0;
    public double ambientTemp;
    public boolean lowTemp = false;
    public boolean noAmbientTemp = false;

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

    private String epcPrev;
    private String epcCur;
    private String write;
    private String readPrev = "";
    private String readCur;
    private String readerID;

    ApiHolder apiHolder;
    ApiHolderAmbient apiHolderAmbient;
    String token = "Bearer cb5c185b0348c227ec2e32e00b41d7a99129a635c474f4278b4cd7c590eb8a1e71500585db23453d9b5a0974e449bcf6596589f05bc31add00b7089859388a06";

    MediaPlayer successSound;
    MediaPlayer failSound;
    MediaPlayer localDbSound;
    ProgressBar sendingProgress;
    TextView textProgress;
    TextView textTempCheck;

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

        //OkHttpClient to add header to all request
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request newRequest  = chain.request().newBuilder()
                        .addHeader("Authorization", token)
                        .build();
                return chain.proceed(newRequest);
            }
        }).build();

        //initialize retrofit builder for api requests

//        Retrofit sparksoft = new Retrofit.Builder()
//                .client(client)
//                .baseUrl("https://dbopayment.sparksoft.com.ph:4000/api/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//        apiHolder = sparksoft.create(ApiHolder.class);
        Retrofit sparksoft = new Retrofit.Builder()
                .client(client)
                .baseUrl("http://13.250.127.145:4000/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiHolder = sparksoft.create(ApiHolder.class);

        Retrofit manilaObservatory = new Retrofit.Builder()
                .baseUrl("https://panahon.observatory.ph/resources/station/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiHolderAmbient = manilaObservatory.create(ApiHolderAmbient.class);

//        Retrofit retrofit = new Retrofit.Builder()
//                .client(client)
//                .baseUrl("http://192.168.1.4:8000/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//        apiHolder = retrofit.create(ApiHolder.class);

        //initialize application sounds
        successSound = MediaPlayer.create(getContext(), R.raw.success);
        failSound = MediaPlayer.create(getContext(), R.raw.fail);

        //initialize get ambient temp
        getAmbientTemp();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (this.mCommonView == null) {

            this.mLayoutInflater = inflater;
            this.mCommonView = inflater.inflate(R.layout.fragment_common, container, false);

            this.mCommonListAdapter = new CommonListAdapter(this.mContext, R.layout.adapter_common, this.mCommons);
            ListView lv = (ListView)this.mCommonView.findViewById(R.id.common_lvMsg);
            sendingProgress = (ProgressBar) mCommonView.findViewById(R.id.sending_progress_bar);
            textProgress = (TextView)mCommonView.findViewById(R.id.progress_textView);
            textTempCheck = (TextView)mCommonView.findViewById(R.id.temperature_check_textView);
            lv.setAdapter(this.mCommonListAdapter);

                    Runnable mPendingRunnable = new Runnable() {
                @Override
                public void run() {
                    //create pager: epc,tid, read, write
                    {
                        mCommonReadPage = new CommonReadPage(mContext, mActivity, mLayoutInflater, mReaderService);
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

    private void addToLocalDb(String bandId, String rawTemperature, String temperature, String rfidNumber, String datetime){
        LocalTempModel localTempModel = null;

        //add bandId, temperature, rfidNumberID, and datetime to local database
        try {
            localTempModel = new LocalTempModel(-1,Integer.parseInt(bandId),Double.parseDouble(rawTemperature),Double.parseDouble(temperature),rfidNumber,datetime);
            Log.d("addTolocalDb", localTempModel.toString());
            LocalDbHelper localDbHelper = new LocalDbHelper(getContext());
            boolean success = localDbHelper.addOne(localTempModel);
        } catch (NumberFormatException ex){
            Log.d("addToLocalDb", "Error creating temp data.");
            sendingProgress.setVisibility(View.GONE);
            textProgress.setVisibility(View.VISIBLE);
            textProgress.setBackgroundColor(Color.parseColor("#ec1e13")); //make bg of textview red
            textProgress.setText("ID Number: " + bandId + " is not a valid ID Number format. IDs must only contain numeric characters. Data cannot be sent to database.");
            textTempCheck.setVisibility(View.GONE);
            failSound.start();
        }


    }

    private LocalTempModel getFromLocalDb(){
        //get first entry in the local database
        LocalDbHelper localDbHelper = new LocalDbHelper(getContext());
        LocalTempModel localTempModel = localDbHelper.getFirstEntry();
        return localTempModel;
    }

    private boolean deleteFromLocalDb(){
        //delete the first entry in the local database
        LocalDbHelper localDbHelper = new LocalDbHelper(getContext());
        boolean success = localDbHelper.deleteFirstEntry();
        return success;
    }

    private boolean containsQueue(){
        LocalDbHelper localDbHelper = new LocalDbHelper(getContext());
        int entriesCount = localDbHelper.countEntries();
        if(entriesCount > 0) return true;
        else return false;
    }

    private void getAmbientTemp(){
        Call <MOResult> weather = apiHolderAmbient.getAmbientTemperature();
        weather.enqueue(new Callback<MOResult>() {
            @Override
            public void onResponse(Call<MOResult> call, Response<MOResult> response) {
                Log.d("AMBIENT TEMP", response.body().getWeatherData().getTemp());
                ambientTemp = Double.parseDouble(response.body().getWeatherData().getTemp());
            }

            @Override
            public void onFailure(Call<MOResult> call, Throwable throwable) {
                Log.d("AMBIENT TEMP", "Fail");
                //don't change ambientTemp if fail
            }
        });
    }

    private double getOffset(){
        getAmbientTemp();

        double offset = (0.0142*ambientTemp*ambientTemp)-(1.1935*ambientTemp)+25.314;
        if(Double.compare(ambientTemp, 37.0) > 0){
            noAmbientTemp = false;
            return 0.0;
        }
        else if (Double.compare(ambientTemp, 0.0) == 0){
            noAmbientTemp = true;
            return 0.0;
        }
        else {
            noAmbientTemp = false;
            return offset;
        }

    }
    private void sendToRemote(){


//        sendingProgressBar.setVisibility(View.VISIBLE);
        LocalTempModel localTempModel = getFromLocalDb();
        deleteFromLocalDb();

        //if local database is not empty
        if (localTempModel != null) {

            //parse the LocalTempModel to TempData
            final TempData tempData = new TempData(
                    String.valueOf(localTempModel.getRawTemperature()),
                    String.valueOf(localTempModel.getTemperature()),
                    String.valueOf(localTempModel.getBandId()),
                    localTempModel.getRfidNumber(),
                    localTempModel.getDatetime()
            );

            Log.d("TEMPDATA CONTENT", "sendToRemote: " + tempData.getRawTemperature() +"," + tempData.getTemperature() + "," + tempData.getBandId() + "," + tempData.getRfidNumber() + "," + tempData.getDatetime());
            //enqueue post request
            Call<TempData> call = apiHolder.createPost(tempData);
            call.enqueue(new Callback<TempData>() {
                @Override
                public void onResponse(Call<TempData> call, Response<TempData> response) {
                    Log.d("check loc", "onResponse: getLOCATION:" );
                    if(response.code() == 200) {
                        //call sendToRemote() function again for queued entries in local database
                        sendingProgress.setVisibility(View.GONE);
                        textProgress.setVisibility(View.VISIBLE);
                        if(containsQueue()){
                            textProgress.setBackgroundColor(Color.parseColor("#efec5c")); //make bg of textview yellow
                            textProgress.setText("Successfully sent data for ID Number: " + tempData.getBandId() +". Please wait, sending queue of data from phone database. This might take some time.");

                        } else{
                            textProgress.setBackgroundColor(Color.parseColor("#57e31c")); //make bg of textview green
                            textProgress.setText("Successfully sent data for ID Number: " + tempData.getBandId() +". Next person in line please scan your tag.");
                            //check if temperature is within normal range
                            if(Double.compare(Double.parseDouble(tempData.getTemperature()),37.5) < 0 ){
                                textTempCheck.setBackgroundColor(Color.parseColor("#57e31c"));//make bg of textview green
                                textTempCheck.setText("Within normal body temperature.");
                                successSound.start();
                            }
                            else{
                                textTempCheck.setBackgroundColor(Color.parseColor("#ec1e13")); //make bg of textview red
                                textTempCheck.setTextColor(Color.parseColor("#ffffff"));
                                textTempCheck.setText("Above normal body temperature.");
                                failSound.start();
                            }
                        }
                        sendToRemote();
                    }
                    else{
                        //if response code is not successful play fail sound and add back to the local database
                        failSound.start();
                        sendingProgress.setVisibility(View.GONE);
                        textProgress.setVisibility(View.VISIBLE);
                        textProgress.setBackgroundColor(Color.parseColor("#ec1e13")); //make bg of textview red
                        textProgress.setText("Unsuccessfully sent data to online database for ID Number: " + tempData.getBandId() + ". Data temporarily stored on phone. Will attempt to send data again later.");
                        addToLocalDb(String.valueOf(tempData.getBandId()),
                                    String.valueOf(tempData.getRawTemperature()),
                                    String.valueOf(tempData.getTemperature()),
                                    tempData.getRfidNumber(),
                                    tempData.getDatetime());
                        Log.d("TRACK", "onResponse: " + response.code() + response);
                        //check if temperature is within normal range
                        if(Double.compare(Double.parseDouble(tempData.getTemperature()),37.5) < 0 ){
                            textTempCheck.setBackgroundColor(Color.parseColor("#57e31c"));//make bg of textview green
                            textTempCheck.setText("Within normal body temperature.");
                            successSound.start();
                        }
                        else{
                            textTempCheck.setBackgroundColor(Color.parseColor("#ec1e13")); //make bg of textview red
                            textTempCheck.setTextColor(Color.parseColor("#ffffff"));
                            textTempCheck.setText("Above normal body temperature.");
                            failSound.start();
                        }
                    }

                }

                @Override
                public void onFailure(Call<TempData> call, Throwable throwable) {
                    //if onFailure play fail sound and add back to the local database
                    failSound.start();
                    Log.d("TRACK", "Failure message:" + throwable.getMessage());
                    sendingProgress.setVisibility(View.GONE);
                    textProgress.setVisibility(View.VISIBLE);
                    textProgress.setBackgroundColor(Color.parseColor("#ec1e13")); //make bg of textview red
                    textProgress.setText("Unsuccessfully sent data to online database for ID Number: " + tempData.getBandId() + ". Data temporarily stored on phone. Will attempt to send data again later.");
                    addToLocalDb(String.valueOf(tempData.getBandId()),
                            String.valueOf(tempData.getRawTemperature()),
                            String.valueOf(tempData.getTemperature()),
                            tempData.getRfidNumber(),
                            tempData.getDatetime());
                    //check if temperature is within normal range
                    if(Double.compare(Double.parseDouble(tempData.getTemperature()),37.5) < 0 ){
                        textTempCheck.setBackgroundColor(Color.parseColor("#57e31c"));//make bg of textview green
                        textTempCheck.setText("Within normal body temperature.");
                        successSound.start();
                    }
                    else{
                        textTempCheck.setBackgroundColor(Color.parseColor("#ec1e13")); //make bg of textview red
                        textTempCheck.setTextColor(Color.parseColor("#ffffff"));
                        textTempCheck.setText("Above normal body temperature.");
                        failSound.start();
                    }
                }
            });
        }
    }

    private void requestPost(String epcPost, String rawTempPost,String tempPost, String rfidNumberPost, String datetime) {
        sendingProgress.setVisibility(View.VISIBLE);
        textProgress.setVisibility(View.VISIBLE);
        textProgress.setBackgroundColor(Color.parseColor("#B1D4E0"));
        textProgress.setText("Attempting to send to online database");
        addToLocalDb(epcPost,rawTempPost, tempPost,rfidNumberPost,datetime);
        sendToRemote();
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
                            && epcCur.length() > 8
                        ) {
//                             remove excess 0's from epc reading
                            String remove = epcCur.substring(0,9);
                            epcCur = epcCur.replace(remove, "");
//                            remove = epcCur.substring(8);
//                            epcCur = epcCur.replace(remove,"");
////                            epcCur = epcCur.replace("Q","").replace("3A00","");
                            String regex = "^0+(?!$)";
                            epcCur = epcCur.replaceFirst(regex, "");
                            remove = epcCur.substring(epcCur.length()-4);
                            epcCur = epcCur.replace(remove, "");

                            //checks if low battery and invalid reading && not repeating tag
                            if (readCur.equals("R8100")){
                                //if it's not low bat and invalid reading again
                                if(!readCur.equals(readPrev)) {
                                    // add the tag number and low battery message and date to screen
                                    updateView(new Common(true,
                                            epcCur,
                                            mDateFormat.format(new Date())));

                                    updateView(new Common(true,
                                            "Your tag has low battery.",
                                            mDateFormat.format(new Date())));

                                    readPrev = readCur;
                                    epcPrev = "";
                                    failSound.start();
                                }

                            }
                            //checks if invalid reading
                            else if (readCur.equals("R0100")){
                                //if it's not the same invalid reading again
                                if(!readCur.equals(readPrev)){
                                    //add invalid reading message and date to screen
                                    updateView(new Common(true,
                                            "Temperature reading was invalid. Please try again.",
                                            mDateFormat.format(new Date())));

                                    readPrev = readCur;
                                    epcPrev = "";
                                    failSound.start();
                                }
                            }
                            else{
                                //extract and convert temperature
                                readCur = readCur.replace("R", "");
                                tempRaw = Integer.parseInt(readCur, 16);
                                tempConvert = tempRaw;
                                tempRawDouble = tempRaw;
                                tempRawDouble = tempRawDouble / 4;
                                tempConvert = tempConvert / 4; //tempConvert is true skin temp at this point

                                // checks if skin temp is above 31 & if it's not the same tag as last reading
                                if ((Double.compare(tempConvert, 31) >= 0 && !epcCur.equals(epcPrev)) ||
                                        (Double.compare(tempConvert, 31) >= 0 && lowTemp == true)) {
                                    lowTemp = false;
                                    //display tag number
                                    updateView(new Common(true,
                                            epcCur,
                                            mDateFormat.format(new Date())));

                                    tempConvert = tempConvert + getOffset();
                                    BigDecimal tempConvert2Places=new BigDecimal(tempConvert).setScale(2, RoundingMode.UP);
                                    readCur = tempConvert2Places + " C";

                                    //get readerID for rfidNumber ID
                                    readerID = readerID.replace("S", "");

                                    //generate current datetime
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    String datetime = sdf.format(new Date());

                                    //call requestPost function to send data to database
                                    requestPost(epcCur, String.valueOf(tempRawDouble), String.valueOf(tempConvert2Places), readerID, datetime);

                                    //display temperature to screen
                                    if(noAmbientTemp){
                                        updateView(new Common(true,
                                                "Skin temperature: " + readCur,
                                                mDateFormat.format(new Date())));
                                    }
                                    else {
                                        updateView(new Common(true,
                                                "Temperature: " + readCur,
                                                mDateFormat.format(new Date())));
                                    }
                                    epcPrev = epcCur;
                                    readPrev = "";
                                }
                                //if it's not the same tag as last reading but below 31
                                else if(!epcCur.equals(epcPrev) )
                                {
                                    //display tag number
                                    updateView(new Common(true,
                                            epcCur,
                                            mDateFormat.format(new Date())));

                                    //display low temperature warning to screen
                                    lowTemp = true;
                                    textProgress.setVisibility(View.GONE);
                                    updateView(new Common(true,
                                            "Temperature read too low.",
                                            mDateFormat.format(new Date())));
                                    textTempCheck.setBackgroundColor(Color.parseColor("#ec1e13")); //make bg of textview red
                                    textTempCheck.setTextColor(Color.parseColor("#ffffff"));
                                    textTempCheck.setText("Detected temperature too low. Please check if band is worn properly and wait for a few minutes for accurate temperature.");
                                    failSound.start();
                                    epcPrev = epcCur;
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
