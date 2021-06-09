package com.favepc.reader.rfidreaderutility;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.favepc.reader.rfidreaderutility.fragment.BLEFragment;
import com.favepc.reader.rfidreaderutility.fragment.BLEHandheldFragment;
import com.favepc.reader.rfidreaderutility.fragment.CommonFragment;
import com.favepc.reader.rfidreaderutility.fragment.DemoIRFragment;
import com.favepc.reader.rfidreaderutility.fragment.DemoUFragment;
import com.favepc.reader.rfidreaderutility.fragment.DemoURFragment;
import com.favepc.reader.rfidreaderutility.fragment.OTGFragment;
import com.favepc.reader.rfidreaderutility.fragment.RegularFragment;
import com.favepc.reader.rfidreaderutility.fragment.WiFiFragment;
import com.favepc.reader.rfidreaderutility.object.CustomKeyboardManager;
import com.favepc.reader.service.BluetoothService;
import com.favepc.reader.service.NetService;
import com.favepc.reader.service.OTGService;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // index to identify current nav menu item
    public static int navItemIndex = 0;
    // tags used to attach the fragments
    private static final String TAG_DEMO    = "Reader Demo";
    private static final String TAG_DEMOUR  = "Reader DemoUR";
    private static final String TAG_COMMON  = "Reader Common";
    private static final String TAG_REGULAR = "Reader Regular";
    public static final String TAG_CI_OTG  = "OTG";
    public static final String TAG_CI_BLE  = "BLE";
    public static final String TAG_CI_WIFI = "WiFi";
    public static final String TAG_CI_UNLINK = "UNLINK";
    private static final String TAG_BLE_HANDHELD = "BLE Handheld";
    private static final String TAG_BLE_HANDHELD_DEMO = "Handheld Demo";
    private static String NAV_CURRENT_TAG = TAG_DEMO;
    private static String COMMUNICATION_INTERFACE = TAG_CI_UNLINK;

    private AppContext mAppContext;
    private Handler mHandler;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private FloatingActionButton fab;
    private String[] activityTitles;
    private Fragment mCurrentFragment, mOldFragment;
    private NotificationReceiver mNotificationReceiver;
    private int mConnectStatus = 0; //otg = 1; ble = 2; wifi = 4;

    private CustomKeyboardManager mKeyboardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        startService(new Intent(MainActivity.this, OTGService.class));
        startService(new Intent(MainActivity.this, NetService.class));
        if(android.os.Build.VERSION.SDK_INT >= 18)
            startService(new Intent(MainActivity.this, BluetoothService.class));

        this.mNotificationReceiver = new NotificationReceiver();
        this.registerReceiver(mNotificationReceiver, new IntentFilter(BluetoothService.BLE_ACTION_RECEIVE_DATA));
        this.registerReceiver(mNotificationReceiver, new IntentFilter(OTGService.OTG_ACTION_RECEIVE_DATA));
        this.registerReceiver(mNotificationReceiver, new IntentFilter(NetService.NET_ACTION_TCP_RECEIVE_DATA));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.mHandler = new Handler();

        this.fab = (FloatingActionButton) findViewById(R.id.fab);
        this.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        toggleFab();///

        this.drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, this.drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        this.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        this.navigationView = (NavigationView) findViewById(R.id.nav_view);
        this.navigationView.setNavigationItemSelectedListener(this);

        // load toolbar titles from string resources
        this.activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

        if (savedInstanceState == null) {
            navItemIndex = 0;
            NAV_CURRENT_TAG = TAG_DEMO;
            navigateToFragment();
        }

        this.mKeyboardManager = new CustomKeyboardManager(this);
        this.mAppContext = (AppContext) this.getApplicationContext();
        this.mAppContext.setKeyboard(this.mKeyboardManager);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(MainActivity.this, BluetoothService.class));
        stopService(new Intent(MainActivity.this, OTGService.class));
        unregisterReceiver(this.mNotificationReceiver);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up gradient_pager, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_demo:
                navItemIndex = 0;
                NAV_CURRENT_TAG = TAG_DEMO;
                break;
            case R.id.nav_demoUR:
                navItemIndex = 1;
                NAV_CURRENT_TAG = TAG_DEMOUR;
                break;
            case R.id.nav_common:
                navItemIndex = 2;
                NAV_CURRENT_TAG = TAG_COMMON;
                break;
            case R.id.nav_regular:
                navItemIndex = 3;
                NAV_CURRENT_TAG = TAG_REGULAR;
                break;
            case R.id.nav_ci_otg:
                navItemIndex = 5;
                NAV_CURRENT_TAG = TAG_CI_OTG;
                break;
            case R.id.nav_ci_ble:
                navItemIndex = 6;
                NAV_CURRENT_TAG = TAG_CI_BLE;
                break;
            case R.id.nav_ci_wifi:
                navItemIndex = 7;
                NAV_CURRENT_TAG = TAG_CI_WIFI;
                break;
            case R.id.nav_handheld:
                navItemIndex = 9;
                NAV_CURRENT_TAG = TAG_BLE_HANDHELD;
                break;
            case R.id.nav_handheld_demo:
                navItemIndex = 10;
                NAV_CURRENT_TAG = TAG_BLE_HANDHELD_DEMO;
                break;

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if (item.isChecked()) {
            item.setChecked(false);
        } else {
            item.setChecked(true);
        }
        item.setChecked(true);

        navigateToFragment();

        return true;
    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void navigateToFragment() {
        // selecting appropriate nav menu item
        this.navigationView.getMenu().getItem(navItemIndex).setChecked(true);
        // set toolbar title
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);


        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        mCurrentFragment = getSupportFragmentManager().findFragmentByTag(NAV_CURRENT_TAG);
        if (mCurrentFragment != null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                    if (mOldFragment == null) {
                        /*List<Fragment> lf = getSupportFragmentManager().getFragments();
                        for(int i = 0; i < lf.size(); i++) {
                            if (lf.get(i) != mCurrentFragment)
                                ft.hide(lf.get(i));
                        }*/
                        ft.show(mCurrentFragment).commit();
                    }
                    else {
                        if (!mOldFragment.equals(mCurrentFragment))
                            ft.hide(mOldFragment).show(mCurrentFragment).commit();
                    }
                    mOldFragment = mCurrentFragment;
                }
            }, 250);

            // show or hide the fab gradient_pager
            toggleFab();

            drawerLayout.closeDrawers();
            return;
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                mCurrentFragment = getNavigateFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

                if (!mCurrentFragment.isAdded()) {
                    if (mOldFragment != null)
                        ft.hide(mOldFragment).add(R.id.content_main, mCurrentFragment, NAV_CURRENT_TAG).commit();
                    else
                        ft.add(R.id.content_main, mCurrentFragment, NAV_CURRENT_TAG).commit();
                }
                else {
                    ft.hide(mOldFragment).show(mCurrentFragment).commit();
                }
                mOldFragment = mCurrentFragment;
            }
        };

        if (mPendingRunnable != null) {
            mHandler.postDelayed(mPendingRunnable, 250);
        }

        // show or hide the fab gradient_pager
        toggleFab();

        //Closing drawer on item click
        drawerLayout.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }


    /**
     * show or hide the fab
     * */
    private void toggleFab() {
        /*if (navItemIndex == 0)
            this.fab.show();
        else
            this.fab.hide();*/
        this.fab.hide();
    }

    /**
     * selecting appropriate nav fragment item
     * */
    private Fragment getNavigateFragment() {

        switch (navItemIndex) {
            case 0:
                DemoUFragment demoUFragment = new DemoUFragment(this, this);
                return demoUFragment;
            case 1:
                DemoURFragment demoURFragment = new DemoURFragment(this, this);
                return demoURFragment;
            case 2:
                CommonFragment commonFragment = new CommonFragment(this, this);
                return commonFragment;
            case 3:
                RegularFragment regularFragment = new RegularFragment(this, this);
                return regularFragment;
            case 5:
                OTGFragment oTGFragment = new OTGFragment(this, this);
                return oTGFragment;
            case 6:
                BLEFragment bLEFragment = new BLEFragment(this, this);
                return bLEFragment;
            case 7:
                WiFiFragment wiFiFragment = new WiFiFragment(this, this);
                return wiFiFragment;
            case 9:
                BLEHandheldFragment bLEHandheldFragment = new BLEHandheldFragment(this, this);
                return bLEHandheldFragment;
            case 10:
                DemoIRFragment demoIRFragment = new DemoIRFragment(this, this);
                return demoIRFragment;
            default:
                return new DemoUFragment(this, this);
        }
    }


    /**
     *
     * */
    class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] _bData;

            switch (intent.getAction()) {
                case BluetoothService.BLE_ACTION_RECEIVE_DATA:
                    _bData = intent.getExtras().getByteArray(BluetoothService.BYTES_DATA);
                    synchronized (packets) {
                        packets.add(_bData);
                    }
                    break;
                case OTGService.OTG_ACTION_RECEIVE_DATA:
                    _bData = intent.getExtras().getByteArray(OTGService.BYTES_DATA);
                    synchronized (packets) {
                        packets.add(_bData);
                    }
                    break;
                case NetService.NET_ACTION_TCP_RECEIVE_DATA:
                    _bData = intent.getExtras().getByteArray(NetService.BYTES_DATA);
                    synchronized (packets) {
                        packets.add(_bData);
                    }
                    break;

            }
        }
    }



    /**
     * otg = 1; ble = 2; wifi = 4;
     * */
    public void interfaceCtrl(@NonNull String cif, boolean onoff) {

        switch (cif) {

            case OTGService.INTERFACE_OTG:
                if (COMMUNICATION_INTERFACE.equals(TAG_CI_BLE)) {
                    sendBroadcast(new Intent(BluetoothService.BLE_ACTION_CHANGE_INTERFACE));
                }
                else if (COMMUNICATION_INTERFACE.equals(TAG_CI_WIFI)) {
                    sendBroadcast(new Intent(NetService.NET_ACTION_CHANGE_INTERFACE));
                }
                if (onoff) {
                    mConnectStatus |= 0x01;
                    COMMUNICATION_INTERFACE = TAG_CI_OTG;
                }
                else mConnectStatus &= 0xFE;

                break;

            case BluetoothService.INTERFACE_BLE:
                if (COMMUNICATION_INTERFACE.equals(TAG_CI_OTG)) {
                    sendBroadcast(new Intent(OTGService.OTG_ACTION_CHANGE_INTERFACE));
                }
                else if (COMMUNICATION_INTERFACE.equals(TAG_CI_WIFI)) {
                    sendBroadcast(new Intent(NetService.NET_ACTION_CHANGE_INTERFACE));
                }
                if (onoff) {
                    mConnectStatus |= 0x02;
                    COMMUNICATION_INTERFACE = TAG_CI_BLE;
                }
                else mConnectStatus &= 0xFD;

                break;

            case NetService.INTERFACE_NET:
                if (COMMUNICATION_INTERFACE.equals(TAG_CI_BLE)) {
                    sendBroadcast(new Intent(BluetoothService.BLE_ACTION_CHANGE_INTERFACE));
                }
                else if (COMMUNICATION_INTERFACE.equals(TAG_CI_OTG)) {
                    sendBroadcast(new Intent(OTGService.OTG_ACTION_CHANGE_INTERFACE));
                }
                if (onoff) {
                    mConnectStatus |= 0x04;
                    COMMUNICATION_INTERFACE = TAG_CI_WIFI;
                }
                else
                    mConnectStatus &= 0xFB;
                break;
        }
    }

    /**
     *
     * */
    public boolean isConnected() {
        return (mConnectStatus > 0) ? true : false;
    }

    public String getInterface() {return COMMUNICATION_INTERFACE; }
    /**
     *
     * */
    private List<byte[]> packets = new LinkedList<byte[]>();

    /**
     * send data to interface
     * @param localByte
     */
    public void sendData(byte[] localByte) {

        Intent _intent = null;

        synchronized (packets) {
            if (packets.size() > 0)
                packets.clear();
        }

        switch(COMMUNICATION_INTERFACE) {
            case TAG_CI_BLE:
                _intent = new Intent(BluetoothService.BLE_ACTION_SEND_DATA);
                _intent.putExtra(BluetoothService.BYTES_DATA, localByte);
                break;
            case TAG_CI_OTG:
                _intent = new Intent(OTGService.OTG_ACTION_SEND_DATA);
                _intent.putExtra(OTGService.BYTES_DATA, localByte);
                break;
            case TAG_CI_WIFI:
                _intent = new Intent(NetService.NET_ACTION_TCP_SEND_DATA);
                _intent.putExtra(NetService.BYTES_DATA, localByte);
                break;
        }

        sendBroadcast(_intent);
    }

    /**
     *
     * */
    public int checkData() {
        synchronized (packets) {
            return packets.size();
        }
    }

    /**
     *
     * */
    public @Nullable byte[] getData() {
        byte[] buffer;
        synchronized (packets) {
            if (packets.size() > 0) {
                buffer = packets.get(0);
            	//java.lang.IndexOutOfBoundsException at java.util.LinkedList.remove(LinkedList.java:660)
                packets.remove(0);
                //packets.clear();

                return buffer;
            }
            else return null;
        }
    }
}
