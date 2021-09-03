package com.favepc.reader.rfidreaderutility;

import android.app.Application;

/**
 * Created by Bruce_Chiang on 2017/3/28.
 */

public class AppContext extends Application {

    private static AppContext instance;

    private boolean mActionIsAck = false;

    public static AppContext getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public void setAck(boolean b) {
        this.mActionIsAck = b;
    }
    public boolean getAck() {
        return this.mActionIsAck;
    }

}
