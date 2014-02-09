package com.yamauchi.thomas;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements OnClickListener {
	private final String TAG = "ThomasActivity";
	private static final int EVENT_RECOG_START = 10;
	private static final int EVENT_RECOG_TEXT = 20;
	
	private String LOGTAG = "test";
	private static final String P_KATAKANA_ONLY        = "^[\\u30A0-\\u30FF]+$";
	
	@Override
	protected void onResume() {
//		AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
////		am.setMode(AudioManager.MODE_IN_CALL);
////		am.setBluetoothScoOn(true);
////		am.startBluetoothSco();
////		am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 15, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
//		am.setMode(AudioManager.MODE_NORMAL);
//		am.setBluetoothScoOn(false);
//		am.stopBluetoothSco();
		
		ThomasSharedPreferences pref = ThomasApplication.getVvmSharedPreference(this);
		ToggleButton tb1 = (ToggleButton)findViewById(R.id.start_time_count);
		tb1.setChecked(pref.isTimeCountOn());
		ToggleButton tb2 = (ToggleButton)findViewById(R.id.start_subway_check);
		tb2.setChecked(pref.isSubwayCheckOn());
		
		super.onResume();
	}

    @Override
	protected void onPause() {
//		AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
//		am.stopBluetoothSco();
		super.onPause();
	}

	private CodeExchange mCe = new CodeExchange();
	
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch( msg.what ){
			case EVENT_RECOG_START:
				break;
			case EVENT_RECOG_TEXT:

				break;
			}
			super.handleMessage(msg);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
    protected void onDestroy() {
        super.onDestroy();
//        if (null != tts) {
//            // TextToSpeech縺ｮ繝ｪ繧ｽ繝ｼ繧ｹ繧定ｧ｣謾ｾ縺吶ｋ
//            tts.shutdown();
//        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View arg0) {
		ThomasSharedPreferences pref = ThomasApplication.getVvmSharedPreference(this);
		Resources r = this.getResources();
		switch( arg0.getId() ){
		case R.id.start_time_count:
		{
			ToggleButton tb = (ToggleButton)arg0;
			pref.setTimeCountOn(tb.isChecked());
			startTimeCount();
		}
	        break;
		case R.id.start_subway_check:
		{
			ToggleButton tb = (ToggleButton)arg0;
			pref.setSubwayCheckOn(tb.isChecked());
			if( !pref.isTimeCountOn() ){
				startTimeCount();
			}
		}
			break;
		case R.id.button2:
			sendSMS(r.getString(R.string.back_home));
			break;
		case R.id.button3:
			sendSMS(r.getString(R.string.getout_office));
			break;
		case R.id.button4:
//			GpsThread gpsThread = new GpsThread( this );
//			gpsThread.start();
//			getLocation();
//			GpsTask.transmission( this, "0", null);
			
//	        Intent intent = new Intent(this,ThomasService.class);
//	        intent.putExtra( ThomasService.EXTRA_EVENT, ThomasService.EVENT_GET_NEWS );
//	        startService(intent);
			if( false ){
		        Intent intent = new Intent(this,ThomasService.class);
		        intent.putExtra( ThomasService.EXTRA_EVENT, ThomasService.EVENT_GET_GPS );
	//	        intent.putExtra( ThomasService.EXTRA_DATA, "09037754984");
		        intent.putExtra( ThomasService.EXTRA_DATA, "09086182049");
		        this.startService(intent);
			}else{
//				if( !pref.isGetNewsOn() ){
//					pref.setGetNewsOn(true);
//			        Intent intent = new Intent(this,ThomasService.class);
//			        intent.putExtra( ThomasService.EXTRA_EVENT, ThomasService.EVENT_GET_GPS );
//			        this.startService(intent);
//				}else{
//					pref.setGetNewsOn(false);
//				}
				String name;
	        	name = ContactsUtil.getNameByPhoneNumber(this, "09091446538");
            	String message = name + "さんからSMSです。    " + "テスト";
		        Intent intent = new Intent(this,ThomasService.class);
		        intent.putExtra( ThomasService.EXTRA_EVENT, ThomasService.EVENT_SPEECH );
		        intent.putExtra( ThomasService.EXTRA_DATA, message);
		        this.startService(intent);
			}
			break;
		}
	}
	
	private void startTimeCount(){
        Intent intent = new Intent(this,ThomasService.class);
        intent.putExtra( ThomasService.EXTRA_EVENT, ThomasService.EVENT_TIME_COUNT );
        startService(intent);
	}
	
    private void startSpeech( String text ){
		Log.d("THOMAS", "BT Connected: Read SMS" );
        Intent intent = new Intent(this,ThomasService.class);
        intent.putExtra( ThomasService.EXTRA_EVENT, ThomasService.EVENT_SPEECH );
        intent.putExtra( ThomasService.EXTRA_DATA, text);
        this.startService(intent);
    }
    
	private void sendSMS( String message ){
		SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage( "09091446538", null, message,
                PendingIntent.getBroadcast( this, 0, new Intent(ThomasBroadcastReceiver.ACTION_SMS_SENT), 0),
                null);
	}
	
	public boolean contain2byte(String s){
		for (int i = 0; i < s.length(); i++) {
			String s1 = s.substring(i, i + 1);
			try {
				if (URLEncoder.encode(s1,"MS932").length() >= 4) {
					return true;
				}
			} catch (UnsupportedEncodingException e) {
				return false;
			}
		}
		return false;
	}
	
	public static boolean isKatakanaOnly(String str) {
	    return str.matches(P_KATAKANA_ONLY);
	}
}
