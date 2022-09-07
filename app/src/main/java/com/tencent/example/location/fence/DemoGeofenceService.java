package com.tencent.example.location.fence;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.tencent.example.location.R;
import com.tencent.map.geolocation.TencentGeofence;
import com.tencent.map.geolocation.TencentGeofenceManager;

/**
 * TencentGeofenceManager 的容器.
 * <p>
 * TencentGeofenceManager 地理围栏服务需要长驻后台运行, 通常应使用 Service 对 TencentGeofenceManager
 * 进行封装以保证其正常运行.
 */
public class DemoGeofenceService extends Service {
    public static final String ACTION_TRIGGER_GEOFENCE = "com.tencent.map.geolocation.xxx";
    public static final String ACTION_ADD_GEOFENCE = "com.tencent.map.geolocation.add";
    public static final String ACTION_DEL_GEOFENCE = "com.tencent.map.geolocation.del";

    private static final String EXTRA_TAG = "com.tencent.map.geolocation.tag";

    private static final String NOTIFICATION_CHANNEL_NAME = "location_demo_geo_fence";

    private TencentGeofenceManager mTencentGeofenceManager;
    private NotificationManager notificationManager;
    private boolean isCreateChannel;

    public static void startMe(Context context, String action, String tag) {
        Intent service = new Intent(context, DemoGeofenceService.class);
        service.setAction(action);
        service.putExtra(EXTRA_TAG, tag);
        context.startService(service);
    }

    public static void stopMe(Context context) {
        context.stopService(new Intent(context, DemoGeofenceService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTencentGeofenceManager = new TencentGeofenceManager(this);

        // 任务栏提示
        startForeground(999, buildNotification());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTencentGeofenceManager.removeAllFences();
        // 完成后必须销毁 TencentGeofenceManager
        mTencentGeofenceManager.destroy();
        // 取消任务栏提示
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }

        String tag = intent.getStringExtra(EXTRA_TAG);
        if (ACTION_ADD_GEOFENCE.equals(intent.getAction())) {
            TencentGeofence geofence = TencentApplication.getLastFence();
            if (geofence == null || !geofence.getTag().equals(tag)) {
                return super.onStartCommand(intent, flags, startId);
            }

            doAddFence(geofence);
        } else if (ACTION_DEL_GEOFENCE.equals(intent.getAction())) {
            doDelFence(tag);
        } else {
            Log.w("DemoGeofenceService",
                    "unknown action: " + intent.getAction());
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void doDelFence(String tag) {
        mTencentGeofenceManager.removeFence(tag);
    }

    private void doAddFence(TencentGeofence geofence) {
        // action: 可自定义
        Intent receiver = new Intent(ACTION_TRIGGER_GEOFENCE);
        receiver.setPackage("com.tencent.example.location");
        // extra: Tag 字段
        receiver.putExtra("KEY_GEOFENCE_ID", geofence.getTag());
        // 还可添加其他自定义的 extra 字段, 比如
        receiver.putExtra("KEY_GEOFENCE_LAT", geofence.getLatitude());
        receiver.putExtra("KEY_GEOFENCE_LNG", geofence.getLongitude());

        // 随机产生的 requestCode, 避免冲突
        int requestCode = (int) (Math.random() * 1E7);
        PendingIntent pi;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pi = PendingIntent.getBroadcast(this, requestCode,
                    receiver, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        } else {
            pi = PendingIntent.getBroadcast(this, requestCode,
                    receiver, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        // 添加围栏
        mTencentGeofenceManager.addFence(geofence, pi);
    }

    private PendingIntent createPendingIntent(Context context) {
        Intent intent = new Intent(context, DemoGeofenceEditorActivty.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        } else {
            return PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    private Notification buildNotification() {
        Notification.Builder builder = null;
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            //Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
            if (notificationManager == null) {
                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            }
            String channelId = getPackageName();
            if (!isCreateChannel) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId,
                        NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(notificationChannel);
                isCreateChannel = true;
            }
            builder = new Notification.Builder(getApplicationContext(), channelId);
        } else {
            builder = new Notification.Builder(getApplicationContext());
        }
        builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("围栏测试中")
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
				.setWhen(System.currentTimeMillis());

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            notification = builder.build();
        } else {
            notification = builder.getNotification();
        }
        return notification;
    }
}
