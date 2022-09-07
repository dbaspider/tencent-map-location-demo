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
import com.tencent.map.geolocation.TencentLocationManagerOptions;
import com.tencent.map.geolocation.TencentLocationRequest;

public class DemoGnssSourceActivity extends Activity implements OnClickListener,
		TencentLocationListener {

	private static final String[] NAMES = new String[] { "GPS优先","北斗优先"};

	private static final int[] gnssSrc = new int[] {
			TencentLocationRequest.GNSS_SOURCE_GPS_FIRST,
			TencentLocationRequest.GNSS_SOURCE_BEIDOU_FIRST};
	private static final int DEFAULT = 0;

	private int mIndex = DEFAULT;
	private int mGnssSource = gnssSrc[DEFAULT];
	private TencentLocationManager mLocationManager;
	private TextView mLocationStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_template);
		mLocationStatus = (TextView) findViewById(R.id.status);

		Button settings = ((Button) findViewById(R.id.settings));
		settings.setText("来源");
		settings.setVisibility(View.VISIBLE);
		// TODO 密钥需在腾讯位置服务官网联系进行客服申请，否则无法使用北斗定位功能。官网链接：https://lbs.qq.com/
		String key = "";
		TencentLocationManagerOptions.setExtraKey(key);
		mLocationManager = TencentLocationManager.getInstance(this);
		// 设置坐标系为 gcj-02, 缺省坐标为 gcj-02, 所以通常不必进行如下调用
		mLocationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_GCJ02);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 退出 activity 前一定要停止定位!
		stopLocation(null);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		mIndex = which;
		mGnssSource = gnssSrc[which];
		dialog.dismiss();
	}

	// ====== view listener

	// 响应点击"停止"
	public void stopLocation(View view) {
		mLocationManager.removeUpdates(this);
		updateLocationStatus("停止定位");
	}

	// 响应点击"开始"
	public void startLocation(View view) {
		// 创建定位请求
		TencentLocationRequest request = TencentLocationRequest.create()
				.setInterval(3*1000) // 设置定位周期
				.setAllowGPS(true)  //当为false时，设置不启动GPS。默认启动
				.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_ADMIN_AREA) // 设置定位level
				.setGnssSource(mGnssSource);

		// 开始定位
		mLocationManager.requestLocationUpdates(request, this,getMainLooper());

		String msg = "开始定位: \n" + "定位来源: " + NAMES[mIndex] +
				", allowGps: " + request.isAllowGPS() + ", interval = " + request.getInterval();
		updateLocationStatus(msg);
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
		sb.append("provider=").append(location.getProvider()).append(",");

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
