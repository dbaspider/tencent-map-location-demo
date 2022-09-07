package com.tencent.example.location;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.map.geolocation.TencentPoi;

import java.util.List;

public class DemoUserPrivacyActivity extends Activity implements OnClickListener,
		TencentLocationListener {

	private static final String[] NAMES = new String[] { "同意", "不同意"};

	private static final int DEFAULT = 1;
	private int mIndex = DEFAULT;

	private TencentLocationManager mLocationManager;
	private TextView mLocationStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_template);
		mLocationStatus = (TextView) findViewById(R.id.status);

		Button settings = ((Button) findViewById(R.id.settings));
		settings.setText("隐私");
		settings.setVisibility(View.VISIBLE);

		mLocationManager = TencentLocationManager.getInstance(this);
		// java.lang.IllegalStateException: removeUpdates MUST called before set coordinate type!
		mLocationManager.removeUpdates(this);
		// 设置坐标系为 gcj-02, 缺省坐标为 gcj-02, 所以通常不必进行如下调用
		mLocationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_GCJ02);

		// 初始化默认不同意隐私
		TencentLocationManager.setUserAgreePrivacy(false);
		updateLocationStatus("用户隐私状态：" + (TencentLocationManager.getUserAgreePrivacy() ? "同意":"不同意"));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 退出 activity 前一定要停止定位!
		stopLocation(null);
		// 退出 activity 将用户隐私设置为同意
		TencentLocationManager.setUserAgreePrivacy(true);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		mIndex = which;
		if (which == 0) {
			TencentLocationManager.setUserAgreePrivacy(true);
		} else {
			TencentLocationManager.setUserAgreePrivacy(false);
		}
		updateLocationStatus("用户隐私状态：" + (TencentLocationManager.getUserAgreePrivacy() ? "同意":"不同意"));
		dialog.dismiss();
	}

	// ====== view listener

	// 响应点击"停止"
	public void stopLocation(View view) {
		mLocationManager.removeUpdates(this);
		String stopLocStatus = "停止定位";
		if (!TencentLocationManager.getUserAgreePrivacy()) {
			stopLocStatus = stopLocStatus + "，由于未获取隐私权限，停止定位未生效";
		}
		updateLocationStatus(stopLocStatus);
	}

	// 响应点击"开始"
	public void startLocation(View view) {
		// 创建定位请求
		TencentLocationRequest request = TencentLocationRequest.create()
				.setInterval(5*1000) // 设置定位周期
				.setAllowGPS(true)  //当为false时，设置不启动GPS。默认启动
				.setQQ("10001")
				.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_ADMIN_AREA);

		// 开始定位
		int retCode = mLocationManager.requestLocationUpdates(request, this, getMainLooper());

		String locStatus = "开始定位，" +  "定位返回值：" + retCode + "， 坐标系=" + DemoUtils.toString(mLocationManager.getCoordinateType());
		if (retCode == 0) {
			locStatus = locStatus + "\n" + request;
		}
		updateLocationStatus(locStatus);
	}

	public void clearStatus(View view) {
		mLocationStatus.setText(null);
	}

	public void settings(View view) {
		Builder builder = new Builder(this).setSingleChoiceItems(
				NAMES, mIndex, this);
		builder.show();
	}

	// ====== view listener

	// ====== location callback

	@Override
	public void onLocationChanged(TencentLocation location, int error,
			String reason) {
		String msg = null;
		if (error == TencentLocation.ERROR_OK) {
			// 定位成功
			msg = toString(location);
		} else {
			// 定位失败
			msg = "定位失败: " + reason;
		}
		updateLocationStatus(msg);
	}

	@Override
	public void onStatusUpdate(String name, int status, String desc) {
		// ignore
	}

	// ====== location callback

	private void updateLocationStatus(String message) {
		mLocationStatus.append(message);
		mLocationStatus.append("\n---\n");
	}

	// ===== util method
	private static String toString(TencentLocation location) {
		StringBuilder sb = new StringBuilder();

		sb.append("latitude=").append(location.getLatitude()).append(",");
		sb.append("longitude=").append(location.getLongitude()).append(",");
		sb.append("altitude=").append(location.getAltitude()).append(",");
		sb.append("accuracy=").append(location.getAccuracy()).append(",");

		sb.append("nation=").append(location.getNation()).append(",");
		sb.append("province=").append(location.getProvince()).append(",");
		sb.append("city=").append(location.getCity()).append(",");
		sb.append("district=").append(location.getDistrict()).append(",");
		sb.append("town=").append(location.getTown()).append(",");
		sb.append("village=").append(location.getVillage()).append(",");
		sb.append("street=").append(location.getStreet()).append(",");
		sb.append("streetNo=").append(location.getStreetNo()).append(",");
		sb.append("citycode=").append(location.getCityCode()).append(",");

		return sb.toString();
	}
}
