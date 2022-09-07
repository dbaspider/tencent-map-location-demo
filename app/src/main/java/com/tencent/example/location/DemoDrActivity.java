package com.tencent.example.location;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationManager;

import java.lang.ref.WeakReference;

/**
 * @Author create by beastchang on 2022/1/13 12:06
 * @Email beastchang@tencent.com
 */
public class DemoDrActivity extends Activity {

    private TextView mTvStatus;
    private TencentLocationManager mLocationManager;
    private Handler mMainHandler;
    private boolean mSwitcher;
    private int mDrStartRe;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template);
        mTvStatus = findViewById(R.id.status);
        mLocationManager = TencentLocationManager.getInstance(this);
        mMainHandler = new CustomHandler(Looper.getMainLooper(), new WeakReference<DemoDrActivity>(this));
    }

    public void startLocation(View view) {
        if (mLocationManager.isDrSupport()) {
            mDrStartRe = mLocationManager.startDrEngine(TencentLocationManager.DR_TYPE_WALK);
            if (mDrStartRe == 0) {
                mSwitcher = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mMainHandler.sendEmptyMessageDelayed(CustomHandler.MSG_TIMER, 1000);
                    }
                }).start();
            }
            mTvStatus.append("dr start re: " + mDrStartRe);
            mTvStatus.append("\r\n");
            mTvStatus.append("-----------------");
            mTvStatus.append("\r\n");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 退出 activity 前一定要停止定位!
        mSwitcher = false;
        mLocationManager.terminateDrEngine();
    }

    public void stopLocation(View view) {
        mSwitcher = false;
        mLocationManager.terminateDrEngine();
        mTvStatus.append("dr stop");
        mTvStatus.append("\r\n");
    }

    public void clearStatus(View view) {
        mTvStatus.setText("");
    }

    private static class CustomHandler extends Handler {
        public static final int MSG_TIMER = 0;
        private WeakReference<DemoDrActivity> mWk;

        public CustomHandler(@NonNull Looper looper, WeakReference<DemoDrActivity> mWk) {
            super(looper);
            this.mWk = mWk;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_TIMER:
                    if (mWk.get().mSwitcher) {
                        removeCallbacksAndMessages(null);
                        handleTimer(mWk);
                        sendEmptyMessageDelayed(CustomHandler.MSG_TIMER,1000);
                    }
                    break;
            }
        }

        private void handleTimer(WeakReference<DemoDrActivity> mWk) {
            if (mWk != null) {
                try {
                    if (mWk.get().mSwitcher) {
                        TencentLocation drLocation = mWk.get().mLocationManager.getDrPosition();
                        mWk.get().mTvStatus.append(drLocation.getProvider() + ", " + drLocation.getLatitude() + ", " + drLocation.getLongitude() + ", " + drLocation.getAltitude()
                                + ", " + drLocation.getAccuracy() + ", " + drLocation.getBearing() + ", " + drLocation.getSpeed());
                        mWk.get().mTvStatus.append("\r\n");
                        mWk.get().mTvStatus.append("-----------------");
                        mWk.get().mTvStatus.append("\r\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
