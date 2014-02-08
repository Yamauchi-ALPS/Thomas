package com.yamauchi.thomas;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.http.AndroidHttpClient;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Xml;

public class ThomasService extends Service implements TextToSpeech.OnInitListener {
	private final String TAG = "THOMAS";
	
	public static final String EXTRA_EVENT = "event";
	public static final String EXTRA_DATA = "data";
	
	public static final int EVENT_NONE        = 0x00;
	public static final int EVENT_SPEECH      = 0x01;
	public static final int EVENT_TIME_COUNT  = 0x02;
	public static final int EVENT_GET_NEWS    = 0x04;
	public static final int EVENT_GET_GPS     = 0x08;
	
	private static final int HANDL_SPEECH_START = 1;
	
	private final String MAP_URL = "http://maps.google.com/maps?q=";
	
	private TextToSpeech tts = null;
	private String mSpeechText = null;
	private boolean isInit = false;
	private int mWorkingEvent = EVENT_NONE;
	ThomasSharedPreferences mPref;
	private LocationManager mLocationManager = null;
	private BroadcastReceiver mReceiver = null;
	
	private int [][] timeTable = {
		{0,9,18},	//0
		{},			//1
		{},			//2
		{},			//3
		{},			//4
		{},			//5
		{18,28,38,48,55},	//6
		{3,10,16,22,26,30,35,39,43,48,52,56},	//7
		{0,5,9,13,17,21,25,29,33,37,41,46,50,54,58},	//8
		{2,6,10,14,18,22,27,32,38,45,52,59},	//9
		{7,14,20,28,35,42,49,56},				//10
		{3,10,17,24,31,38,45,52,59},			//11
		{6,13,20,27,34,41,48,55},				//12
		{2,9,16,23,30,37,44,51,58},				//13
		{5,12,19,26,33,40,47,54},				//14
		{1,8,15,22,29,36,43,50,57},				//15
		{4,11,18,24,29,34,39,44,49,55},			//16
		{0,5,11,16,21,26,31,36,41,47,52,57},	//17
		{2,7,12,18,23,28,33,38,43,48,53,59},	//18
		{4,9,14,19,25,31,39,47,55},				//19
		{3,11,19,27,35,43,52},					//20
		{0,8,16,24,32,40,48,56},				//21
		{4,12,20,28,36,44,53},					//22
		{1,10,17,25,33,41,50} };				//23

	private final float POS_AROUND           = 30.0f;
	private final float POS_HOME[] 		     = { 43.091104f, 141.353702f };
	private final float POS_PRIMARY_SCHOOL[] = { 43.092487f, 141.355297f };
	private final float POS_NURCERY_SCHOOL[] = { 43.073591f, 141.364519f };
	private final float POS_OFFICE[] 		 = { 43.066772f, 141.346070f };
	private final float POS_GRAND_HOME[] 	 = { 43.071314f, 141.364900f };
	private final float POS_STATION_SAPPORO[] = { 43.068085f, 141.350201f };
	private final float POS_STATION_24[]      = { 43.090203f, 141.344611f };
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
  @Override
  public void onCreate() {
    super.onCreate();
    Log.d(TAG, "onCreate");
	mPref = ThomasApplication.getVvmSharedPreference(this);
  }

  @Override
public void onTaskRemoved(Intent rootIntent) {
	isInit = false;
	tts.shutdown();
	tts = null;
	super.onTaskRemoved(rootIntent);
}

@Override
public boolean onUnbind(Intent intent) {
	isInit = false;
	tts.shutdown();
	tts = null;
	return super.onUnbind(intent);
}

@Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "onStartCommand");
    if( intent == null ) return 0;
    int event = intent.getIntExtra( EXTRA_EVENT, EVENT_NONE );
    setEvent(event, true);
    switch( event ){
    case EVENT_NONE:
        eventFinish(EVENT_NONE);
    	break;
    case EVENT_SPEECH:
        if( tts == null ){
    		tts = new TextToSpeech(this, this);
        }
	    String text = intent.getStringExtra( EXTRA_DATA );
	    mSpeechText = text;
	    if( isInit ){
	    	mHandler.sendMessage( mHandler.obtainMessage(HANDL_SPEECH_START, mSpeechText ));
	    }
	    break;
    case EVENT_TIME_COUNT:
    	eventTimeCount();
    	break;
    case EVENT_GET_NEWS:
    	eventGetNews();
    	break;
    case EVENT_GET_GPS:
    	eventBattery( intent.getStringExtra( EXTRA_DATA ));
//    	eventGps( intent.getStringExtra( EXTRA_DATA ) );
    	break;
    }
    return START_STICKY;
  }
  
	private void setEvent( int event, boolean onoff ){
		int evBackup = mWorkingEvent;
		if( onoff ){
			mWorkingEvent |= event;
		}else{
			mWorkingEvent ^= event;
		}
		Log.d("THOMAS", "setEvent: " + evBackup + (onoff ? " + ":" - ") + event + " = " + mWorkingEvent );
	}
	
	private void eventBattery( String str ){
		final String mdn = str; 
        mReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if( action.equalsIgnoreCase(Intent.ACTION_BATTERY_CHANGED)){
//		            int status = intent.getIntExtra("status", 0);
//		            int health = intent.getIntExtra("health", 0);
//		            boolean present = intent.getBooleanExtra("present", false);
		            int level = intent.getIntExtra("level", 0);
//		            int scale = intent.getIntExtra("scale", 0);
//		            int icon_small = intent.getIntExtra("icon-small", 0);
//		            int plugged = intent.getIntExtra("plugged", 0);
//		            int voltage = intent.getIntExtra("voltage", 0);
//		            int temperature = intent.getIntExtra("temperature", 0);
//		            String technology = intent.getStringExtra("technology");
		            
//		            String statusString = "";
//		            
//		            switch (status) {
//		            case BatteryManager.BATTERY_STATUS_UNKNOWN:
//		                statusString = "unknown";
//		                break;
//		            case BatteryManager.BATTERY_STATUS_CHARGING:
//		                statusString = "charging";
//		                break;
//		            case BatteryManager.BATTERY_STATUS_DISCHARGING:
//		                statusString = "discharging";
//		                break;
//		            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
//		                statusString = "not charging";
//		                break;
//		            case BatteryManager.BATTERY_STATUS_FULL:
//		                statusString = "full";
//		                break;
//		            }
//		            
//		            String healthString = "";
//		            
//		            switch (health) {
//		            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
//		                healthString = "unknown";
//		                break;
//		            case BatteryManager.BATTERY_HEALTH_GOOD:
//		                healthString = "good";
//		                break;
//		            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
//		                healthString = "overheat";
//		                break;
//		            case BatteryManager.BATTERY_HEALTH_DEAD:
//		                healthString = "dead";
//		                break;
//		            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
//		                healthString = "voltage";
//		                break;
//		            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
//		                healthString = "unspecified failure";
//		                break;
//		            }
//		            
//		            String acString = "";
//		            
//		            switch (plugged) {
//		            case BatteryManager.BATTERY_PLUGGED_AC:
//		                acString = "plugged ac";
//		                break;
//		            case BatteryManager.BATTERY_PLUGGED_USB:
//		                acString = "plugged usb";
//		                break;
//		            }
		                            
//		            Log.v("THOMAS", "status: " + statusString);
//		            Log.v("THOMAS", "health: " + healthString);
//		            Log.v("THOMAS", "present: " + String.valueOf(present));
		            Log.v("THOMAS", "level: " + String.valueOf(level));
//		            Log.v("THOMAS", "scale: " + String.valueOf(scale));
//		            Log.v("THOMAS", "icon_small: " + String.valueOf(icon_small));
//		            Log.v("THOMAS", "plugged: " + acString);
//		            Log.v("THOMAS", "voltage: " + String.valueOf(voltage));
//		            Log.v("THOMAS", "temperature: " + String.valueOf(temperature));
//		            Log.v("THOMAS", "technology: " + technology);
		            ThomasService.this.unregisterReceiver(mReceiver);
		            mReceiver = null;
		            eventGps( mdn, String.valueOf(level) );
				}
			}
        };  
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        this.registerReceiver(mReceiver, filter);
	}
	
	private void eventGps( String mdn, String bat ){
		if( mdn == null || mdn.isEmpty() ){
			Log.v("THOMAS", "mdn empty");
			eventFinish(EVENT_GET_NEWS);
			return;
		}
		
		final String phoneNumber = mdn;
		final String battery = bat;
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);   // 要求精度
        criteria.setPowerRequirement(Criteria.POWER_LOW);   // 許容電力消費
        criteria.setSpeedRequired(false);               // 速度不要
        criteria.setAltitudeRequired(false);            // 高度不要
        criteria.setBearingRequired(false);            // 方位不要
        criteria.setCostAllowed(false);               // 費用の発生不可？
        
        LocationListener listener = new LocationListener(){
			@Override
			public void onLocationChanged(Location location) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                Log.d( "THOMAS", "now latitude=" + lat + ", longitude=" + lon + "\n\nBattery=" + battery + "%" );
				mLocationManager.removeUpdates(this);
				mLocationManager = null;
				
				if( phoneNumber != null && !phoneNumber.isEmpty() ){
					sendSMS( phoneNumber, MAP_URL + lat + "," + lon + "\n\nBattery=" + battery + "%" );
				}
				eventFinish(EVENT_GET_GPS);
			}

			@Override
			public void onProviderDisabled(String provider) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}
        };
        
        mLocationManager = (LocationManager)this.getSystemService(Activity.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(mLocationManager.getBestProvider(criteria, true),
        										0, 0,
        										listener,
        										Looper.getMainLooper() );
	}
	
	private void eventTimeCount(){
		if( mPref.isTimeCountOn() ){
	        if( tts == null ){
	    		tts = new TextToSpeech(this, this);
	        }
	        AlarmManager am =
	                (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
	        Intent sendIntent = new Intent( ThomasBroadcastReceiver.ACTION_TIME_COUNT );
	        PendingIntent alarmIntent = PendingIntent.getBroadcast
                    (this, 0, sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
	        		SystemClock.elapsedRealtime() + 60000, alarmIntent);

	        Calendar calendar = Calendar.getInstance();
	        int hour = calendar.get(Calendar.HOUR_OF_DAY);
	        int minute = calendar.get(Calendar.MINUTE);
	        int second = calendar.get(Calendar.SECOND);
	        mSpeechText = String.valueOf(hour) + "時" + String.valueOf(minute) + "分" + second + "秒";
	        if( mPref.isSubwayCheckOn() ){
	        	int left = getNextSubway( hour, minute, second );
        		mSpeechText += "、 つぎの地下鉄まであと" + left + "分です。";
	        }
	        
	        setEvent(EVENT_SPEECH, true);
	        setEvent(EVENT_TIME_COUNT, false);
		    if( isInit ){
		    	mHandler.sendMessage( mHandler.obtainMessage(HANDL_SPEECH_START, mSpeechText ));
		    }
		}else{
            eventFinish(EVENT_TIME_COUNT);
		}
	}
	
	private void eventGetNews(){
    	if( mPref.isGetNewsOn() ){
	        AlarmManager am =
	                (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
	        Intent sendIntent = new Intent( ThomasBroadcastReceiver.ACTION_TIME_COUNT );
	        PendingIntent alarmIntent = PendingIntent.getBroadcast
                    (this, 0, sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
	        		SystemClock.elapsedRealtime() + 1200000, alarmIntent);

//	    	(new Thread(new Runnable() { 
//	    	    @Override 
//	    	    public void run() { 
//	            	InetAddress inetAddress = null;
//	                try {
//	                    inetAddress = InetAddress.getByName("www.yahoo.co.jp");
//	                } catch (UnknownHostException e) {
//	                    Log.d("THOMAS", "network transport NG");
//	                }
//	                if( inetAddress != null ){
//	                	Log.d("THOMAS", "network transport OK -> get News");
//	                	//Get News
//	                	String rss = retrieveRss();
////	                	RssData rssData = RssData.parseXML( ThomasService.this, rss );
//	                	
//	                	eventFinish(EVENT_GET_NEWS);
//	                }
//	    	    } 
//	    	})).start(); 
    	}else{
            eventFinish(EVENT_GET_NEWS);
    	}
	}

	private void sendSMS( String mdn, String message ){
		SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage( mdn, null, message, 
        		PendingIntent.getBroadcast( this, 0, new Intent(ThomasBroadcastReceiver.ACTION_SMS_SENT), 0),
        		null);
	}

  private int getNextSubway( int hour, int minute, int second ){
  	int target = -1;
  	int left = 0;
  	for( int min : timeTable[hour] ){
  		if( min > minute ){
  			target = min;
  			left = target - minute;
  			break;
  		}
  	}
  	if( target < 0 ){
  		int nextHour = hour + 1;
  		if( hour == 23 ) nextHour = 0;
  		if( timeTable[nextHour].length > 0 ){
  			target = timeTable[nextHour][0];
  			left = 60 - minute + target;
  		}
  	}
  	if( second > 30 ){
  		left--;
  		if( left <= 0 ){
  			minute += 2;
  			if( minute >= 60 ){
  				hour++;
  				if( hour > 23 ){
  					hour = 0;
  				}
  			}
  			left = getNextSubway( hour, minute, second );
  		}
  	}
  	
  	return left;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (null != tts) {
        // TextToSpeech縺ｮ繝ｪ繧ｽ繝ｼ繧ｹ繧定ｧ｣謾ｾ縺吶ｋ
        tts.shutdown();
        tts = null;
    }
    Log.d(TAG, "onDestroy");
  }

	private boolean btConnected( Context context ){
		AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		boolean a2dp = am.isBluetoothA2dpOn();
		boolean sco = am.isBluetoothScoOn();
		Log.d("THOMAS", "a2dp=" + a2dp + ", sco=" + sco );
		return a2dp | sco ;
	}

  Handler mHandler = new Handler(){
	@Override
	public void handleMessage(Message msg) {
		switch( msg.what ){
		case HANDL_SPEECH_START:
		    setEvent( EVENT_SPEECH, true);
			speechText( (String)msg.obj );
			break;
		}
		super.handleMessage(msg);
	}
	  
  };
		
  private void speechText( String str ) {
	  if( !btConnected(this) && isMannerMode() ){
		  mSpeechText = null;
          eventFinish(EVENT_SPEECH);
	  }else if (0 < str.length()) {
          if (tts.isSpeaking()) {
              // 隱ｭ縺ｿ荳翫￡荳ｭ縺ｪ繧画ｭ｢繧√ｋ
              tts.stop();
          }
          tts.setPitch(0.8f);
          tts.setSpeechRate(1.3f);
          tts.setLanguage(Locale.JAPANESE);
          HashMap<String, String> params = new HashMap<String, String>();
          params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"stringId");
          params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, String.valueOf(0.8));
          tts.speak(str, TextToSpeech.QUEUE_FLUSH, params);
          mSpeechText = null;
          Log.d("THOMAS", "Speak!!");
      }
  }

	@Override
	public void onInit(int arg0) {
		if (TextToSpeech.SUCCESS == arg0 && tts != null ) {
	        Log.d("THOMAS", "onInit Success" );
	        isInit = true;
//	        Locale locale = Locale.ENGLISH;
//	        if (tts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
//	            tts.setLanguage(locale);
//	            Log.d("THOMAS", locale.toString() );
//	        } else {
//	            Log.d("", "Error SetLocale");
//	        }
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener (){
				@Override
				public void onDone(String arg0) {
                    Log.d("THOMAS", "Speech Completed! :" + arg0);
                    if( mSpeechText == null ){
                        eventFinish(EVENT_SPEECH);
                    }
				}

				@Override
				public void onError(String arg0) {
		            Log.d("THOMAS", "speech error" );
                    mSpeechText = null;
                    eventFinish(EVENT_SPEECH);
                }

				@Override
				public void onStart(String arg0) {
		            Log.d("THOMAS", "speech start" );
				}
            });
            
	        if( mSpeechText != null ){
	        	mHandler.sendMessage( mHandler.obtainMessage(HANDL_SPEECH_START, mSpeechText ));
	        }
	    } else {
	        Log.d("THOMAS", "Error Respeech");
	        if( mSpeechText != null ){
	        	mHandler.sendMessage( mHandler.obtainMessage(HANDL_SPEECH_START, mSpeechText ));
	        }
	    }
	}

	private void eventFinish( int event ){
	    setEvent(event, false);
		if( mWorkingEvent == EVENT_NONE ){
			ThomasService.this.stopSelf();
		}
	}
	
	private boolean isMannerMode(){
		boolean ret = true;
		AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);  
		int ringerMode = audioManager.getRingerMode();   
		  
		switch (ringerMode) {
		case AudioManager.RINGER_MODE_NORMAL:
			ret = false;
			break;  
		case AudioManager.RINGER_MODE_SILENT:  
		case AudioManager.RINGER_MODE_VIBRATE:  
			ret = true;
		default:
			break;  
		} 
		return ret;
	}
}
