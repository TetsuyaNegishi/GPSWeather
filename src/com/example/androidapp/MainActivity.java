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
		    // �擾�������I��
			locationManager.removeUpdates(mLocationListener);
		}
	    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
	        	
	    // GPS����ʒu�����擾����ݒ�
	    boolean isGpsOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	    // 3G�܂���wifi����ʒu�����擾����ݒ�
	    boolean isWifiOn =  locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	    String provider = null;
		if (isGpsOn) {
			provider = LocationManager.GPS_PROVIDER;
		} else if (isWifiOn) {
	        provider = LocationManager.NETWORK_PROVIDER;
	    } else {
	        Toast.makeText(getApplicationContext(), "Wi-Fi��GPS��ON�ɂ��Ă�������", Toast.LENGTH_LONG).show();
	        return;
	    }
		
				
		// ���P�[�V�����擾���J�n
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
            // �ʒu���̎擾��1�񂵂��s��Ȃ��̂Ŏ擾���X�g�b�v
            locationManager.removeUpdates(mLocationListener);
		 	getRequest(latitude, longitude);
        }
    };
	
	private	void getRequest(String latitude, String longitude) {
		//�V�C��JSON�擾
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
// ���V�C
                JSONObject obj = json.getJSONObject("city");
                // �n�_��
                String cityName = obj.getString("name");
             	JSONArray listArray = json.getJSONArray("list");
             	JSONObject obj2 = listArray.getJSONObject(0);
             	// �C��(K���灎�ɕϊ�)
             	JSONObject mainObj = obj2.getJSONObject("temp");
             	int currentTemp = (int) (mainObj.getDouble("day") - 273.15f);
                JSONArray weatherArray = obj2.getJSONArray("weather");
                // �V�C
				String weather = weatherArray.getJSONObject(0).getString("main");
				
				//���[�����M
				Uri uri = Uri.parse("mailto:test@test.com"); 
	      		Intent intent=new Intent(Intent.ACTION_SENDTO,uri); 
	        	intent.putExtra(Intent.EXTRA_SUBJECT,"���ݒn�̓V�C"); 
	        	intent.putExtra(Intent.EXTRA_TEXT, String.format("�ꏊ�F%s\n�V�C�F%s\n�C���F%d�x", cityName, weather, currentTemp)); 
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
