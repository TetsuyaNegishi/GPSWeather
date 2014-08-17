package com.example.androidapp;

import java.net.HttpURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	EditText latText = null;
	EditText lonText = null;
	TextView weatherText = null;
	private LocationManager locationManager = null;
	String latitude, longitude;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button weather = (Button) findViewById(R.id.button1);
		
		weather.setOnClickListener(weatherClickListener);
		
	}
	
	private OnClickListener weatherClickListener = new OnClickListener() {
		public void onClick(View v) {
			gpsLocation();
		}
	};
	
	private void gpsLocation(){
		if (locationManager != null) {
		    // 取得処理を終了
			locationManager.removeUpdates(mLocationListener);
		}
	    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
	        	
	    // GPSから位置情報を取得する設定
	    boolean isGpsOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	    // 3Gまたはwifiから位置情報を取得する設定
	    boolean isWifiOn =  locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	    String provider = null;
		if (isGpsOn) {
			provider = LocationManager.GPS_PROVIDER;
		} else if (isWifiOn) {
	        provider = LocationManager.NETWORK_PROVIDER;
	    } else {
	        Toast.makeText(getApplicationContext(), "Wi-FiかGPSをONにしてください", Toast.LENGTH_LONG).show();
	        return;
	    }
		
				
		// ロケーション取得を開始
	    locationManager.requestLocationUpdates(provider, 1000L, 0, mLocationListener);
	}
	
	@Override
    protected void onPause() {
        if (locationManager != null) {
        	locationManager.removeUpdates(mLocationListener);
        }
        super.onPause();
    }

	
	private LocationListener mLocationListener = new LocationListener() {
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        public void onProviderEnabled(String provider) {
        }
        public void onProviderDisabled(String provider) {
        }
        public void onLocationChanged(Location location) {
        	latitude = Double.toString(location.getLatitude());
        	longitude = Double.toString(location.getLongitude());
            // 位置情報の取得を1回しか行わないので取得をストップ
            locationManager.removeUpdates(mLocationListener);
		 	getRequest(latitude, longitude);
        }
    };
	
	private	void getRequest(String latitude, String longitude) {
		//天気のJSON取得
		String requestURL = "http://api.openweathermap.org/data/2.5/forecast/daily"
				+ "?lat=" + latitude + "&lon=" + longitude
				+ "&xmode=json&cnt=1";
		Task task = new Task();
        task.execute(requestURL);
	}

	protected class Task extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params)
        {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(params[0]);
            byte[] result = null;
            String rtn = "";
            try {
                HttpResponse response = client.execute(get);
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpURLConnection.HTTP_OK){
                    result = EntityUtils.toByteArray(response.getEntity());
                    rtn = new String(result, "UTF-8");
                }
            } catch (Exception e) {
            	Log.e("BACKGROUND ERROR", e.getMessage());
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            return rtn;
        }
        
        @Override
        protected void onPostExecute(String result)
        {
            try {
            	JSONObject json = new JSONObject(result);
//////////////////////////////////////////////////////////////////
// お天気
                JSONObject obj = json.getJSONObject("city");
                // 地点名
                String cityName = obj.getString("name");
             	JSONArray listArray = json.getJSONArray("list");
             	JSONObject obj2 = listArray.getJSONObject(0);
             	// 気温(Kから℃に変換)
             	JSONObject mainObj = obj2.getJSONObject("temp");
             	int currentTemp = (int) (mainObj.getDouble("day") - 273.15f);
                JSONArray weatherArray = obj2.getJSONArray("weather");
                // 天気
				String weather = weatherArray.getJSONObject(0).getString("main");
				
				//メール送信
				Uri uri = Uri.parse("mailto:test@test.com"); 
	      		Intent intent=new Intent(Intent.ACTION_SENDTO,uri); 
	        	intent.putExtra(Intent.EXTRA_SUBJECT,"現在地の天気"); 
	        	intent.putExtra(Intent.EXTRA_TEXT, String.format("場所：%s\n天気：%s\n気温：%d度", cityName, weather, currentTemp)); 
	        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	        	startActivity(intent);
	        
            }
            catch (JSONException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
}
