package com.tencent.example.location;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.map.geolocation.TencentPoi;

import java.lang.ref.WeakReference;

public class DemoLocScenarioActivity extends Activity implements OnClickListener,
		TencentLocationListener {

	private static final String[] NAMES = new String[] { "签到", "运动", "出行"};

	private static final int[] Scenario = new int[] {
			TencentLocationManager.SIGN_IN_SCENE,
			TencentLocationManager.SPORT_SCENE,
			TencentLocationManager.TRANSPORT_SCENE,
	};
	private static final int DEFAULT = 0;

	private int mIndex = DEFAULT;
	private int mScenario = Scenario[DEFAULT];
	private TencentLocationManager mLocationManager;
	private TextView mLocationStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_template);
		mLocationStatus = (TextView) findViewById(R.id.status);

		Button settings = ((Button) findViewById(R.id.settings));
		settings.setText("场景");
		settings.setVisibility(View.VISIBLE);

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
		mScenario = Scenario[which];
		dialog.dismiss();
	}

	// ====== view listener

	// 响应点击"停止"
	public void stopLocation(View view) {
		if (mScenario != 0) {
			mLocationManager.stopLocationWithScene(mScenario,this);
		}
		updateLocationStatus("停止定位");
	}

	// 响应点击"开始"
	public void startLocation(View view) {
		// 根据场景启动定位
		mLocationManager.requestLocationWithScene(mScenario, this);

		String msg = "开始定位: \n" + "scenario: " + NAMES[mScenario - 10];
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
			Log.e("onLocationChanged", msg);
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
