package com.coolweather.app.activity;

import com.coolweather.app.R;
import com.coolweather.app.service.AutoUpdateService;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements OnClickListener {
	
	private LinearLayout weatherInfoLayout;
	private TextView cityNameText;			//������
	private TextView publishText;			//����ʱ��
	private TextView currentDateText;		//��ǰʱ��
	private TextView weatherDescText;		//��������
	private TextView temp1Text;				//����1
	private TextView temp2Text;				//����2
	private Button switchCity;				//�л����а�ť
	private Button refreshWeather;			//����������ť
	
	@Override
	protected void onCreate(Bundle saveInstanceState){
		super.onCreate(saveInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//��ʼ���ؼ�
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		currentDateText = (TextView) findViewById(R.id.current_date);
		weatherDescText = (TextView) findViewById(R.id.weather_desc);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		// ��ȡ��ť
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		
		String countyCode = getIntent().getStringExtra("county_code");
		
		if(!TextUtils.isEmpty(countyCode)){
			// ���ؼ�����ʱ������б����ȥ��ѯ����
			publishText.setText("ͬ����...");
			cityNameText.setVisibility(View.INVISIBLE);
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		}else{
			// û���ؼ�����ʱ����ChooseAreaActityֱ����ת����ֱ����ʾ��������
			showWeather();
		}
	}

	/**
	 * ��ѯ�ؼ���������Ӧ���������š�
	 */
	private void queryWeatherCode(String countyCode){
		String address =  "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
		queryFromServer(address,"countyCode");
	}
	
	/**
	 * ��ѯ������������Ӧ��������
	 */
	private void queryWeatherInfo(String weatherCode){
		String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
		queryFromServer(address, "weatherCode");
	}
	
	/**
	 * ���ݴ���ĵ�ַ������ȥ���������ѯ�������Ż���������Ϣ
	 */
	private void queryFromServer(final String address,final String type){
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){
			@Override
			public void onFinish(final String response){
				if("countyCode".equals(type)){
					if(!TextUtils.isEmpty(response)){
						// �ӷ��������ص������н�������������
						String[] array = response.split("\\|");
						if(array != null && array.length == 2){
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				}else if("weatherCode".equals(type)){
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable(){
						@Override
						public void run(){
							showWeather();
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e){
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						publishText.setText("ͬ��ʧ��");
					}
				});
			}
		});
	}
	
	/**
	 * ��SharedPreferences�ļ��ж�ȡ�洢��������Ϣ������ʾ��������
	 */
	private void showWeather(){
		SharedPreferences prefs = getSharedPreferences("weatherinfo",MODE_PRIVATE);
		cityNameText.setText(prefs.getString("city_name",""));
		publishText.setText("����" + prefs.getString("publish_time","") + "����");
		currentDateText.setText(prefs.getString("current_date",""));
		weatherDescText.setText(prefs.getString("weather_desc",""));
		temp1Text.setText(prefs.getString("temp1",""));
		temp2Text.setText(prefs.getString("temp2",""));
		cityNameText.setVisibility(View.VISIBLE);
		weatherInfoLayout.setVisibility(View.VISIBLE);
		Intent intent = new Intent(this,AutoUpdateService.class);
		startService(intent);		
	}
	
	@Override
	public void onClick(View view) {	
		switch(view.getId()){
		case R.id.switch_city:
			Intent intent = new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			publishText.setText("ͬ����...");
			SharedPreferences prefs = getSharedPreferences("weatherinfo",MODE_PRIVATE);
			String weatherCode = prefs.getString("weather_code", "");
			if(!TextUtils.isEmpty(weatherCode)){
				queryWeatherInfo(weatherCode);
			}
			break;
		default:
			break;
		}
	}
}
