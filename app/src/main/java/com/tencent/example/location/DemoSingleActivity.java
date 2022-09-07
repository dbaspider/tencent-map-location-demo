package com.tencent.example.location;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

import java.lang.ref.WeakReference;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class DemoSingleActivity extends Activity implements DialogInterface.OnClickListener {

	private static final String TAG = DemoSingleActivity.class.getSimpleName();

	private TencentLocationManager mLocationManager;
	private InnerLocationListener mLocationListener;

	private static final String[] SETTINGS = new String[]{"Default","GpsFirst"};
	private int mIndex = 0;
	private String mSettings = SETTINGS[mIndex];

	private TextView tv_content;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single);
		tv_content = findViewById(R.id.tv_content);

		mLocationManager = TencentLocationManager.getInstance(getApplicationContext());
		mLocationListener = new InnerLocationListener(new WeakReference<DemoSingleActivity>(this));

		if (Build.VERSION.SDK_INT >= 23) {
			requirePermission();
		}

		if (!judgeLocationServerState()) {
			//没有打开位置服务开关，这里设计交互逻辑引导用户打开位置服务开关
		}
	}

	@AfterPermissionGranted(1)
	private void requirePermission() {
		String[] permissions = {
				Manifest.permission.ACCESS_COARSE_LOCATION,
				Manifest.permission.ACCESS_FINE_LOCATION,
		};
		String[] permissionsForQ = {
				Manifest.permission.ACCESS_COARSE_LOCATION,
				Manifest.permission.ACCESS_FINE_LOCATION,
				Manifest.permission.ACCESS_BACKGROUND_LOCATION, //target为Q时，动态请求后台定位权限
		};
		if (Build.VERSION.SDK_INT >= 29 ? EasyPermissions.hasPermissions(this, permissionsForQ) :
				EasyPermissions.hasPermissions(this, permissions)) {
			Toast.makeText(this, "权限OK", Toast.LENGTH_LONG).show();
		} else {
			EasyPermissions.requestPermissions(this, "需要权限",
					1, Build.VERSION.SDK_INT >= 29 ? permissionsForQ : permissions);
		}
	}

	private boolean judgeLocationServerState() {
		try {
			return Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.LOCATION_MODE) > 1;
		} catch (Settings.SettingNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void startSingleLocation(View view) {
		if (mLocationManager != null) {
			TencentLocationRequest request = TencentLocationRequest.create();
			if (mSettings.equals(SETTINGS[1])) {
				request.setGpsFirst(true)	// 设置是否优先获取gps点，准确性更高
						.setGpsFirstTimeOut(5*1000);
			}
			request.setQQ("10001").setRequestLevel(3);

			//也可以使用子线程，但是必须包含Looper
			int re = mLocationManager.requestSingleFreshLocation(request, mLocationListener, Looper.getMainLooper());
			Log.i(TAG, "re: " + re);

			tv_content.append("start location: \r\n" );
			tv_content.append("level = " + request.getRequestLevel() + ", LocMode = " + request.getLocMode() +
					", allowGps = " + request.isAllowGPS() + ", isGpsFirst = " + request.isGpsFirst() +
					", GpsFirstTimeOut = " + request.getGpsFirstTimeOut() +  "\n");
			tv_content.append("--------------  \r\n");
		}
	}

	public void clearAll(View view) {
		tv_content.setText("");
	}

	public void settings(View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setSingleChoiceItems(
				SETTINGS, mIndex, this);
		builder.show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		mIndex = which;
		mSettings = SETTINGS[which];
		dialog.dismiss();
	}

	private static class InnerLocationListener implements TencentLocationListener {
		private WeakReference<DemoSingleActivity> mMainActivityWRF;

		public InnerLocationListener(WeakReference<DemoSingleActivity> mainActivityWRF) {
			mMainActivityWRF = mainActivityWRF;
		}

		@Override
		public void onLocationChanged(TencentLocation location, int error,
									  String reason) {
			if (mMainActivityWRF != null) {
				DemoSingleActivity mainActivity = mMainActivityWRF.get();
				if (mainActivity != null) {
					mainActivity.tv_content.append(location.toString());
					mainActivity.tv_content.append("\r\n");
					mainActivity.tv_content.append("--------------  \r\n");
				}
			}
		}

		@Override
		public void onStatusUpdate(String name, int status, String desc) {
			Log.i(TAG, "name: " + name + "status: " + status + "desc: " + desc);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}
}
