package com.yamauchi.thomas;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Set;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class ThomasBroadcastReceiver extends BroadcastReceiver {
	private final String TAG = "ThomasBR";
	
	public static final String ACTION_TIME_COUNT = "com.yamauchi.thomas.time_count";
	public static final String ACTION_GET_NEWS = "com.yamauchi.thomas.get_news";
	public static final String ACTION_TEST = "com.yamauchi.thomas.test";
	public static final String ACTION_SPEECH = "com.yamauchi.thomas.speech";
    public static final String ACTION_SMS_SENT = "com.yamauchi.thomas.sms_send";

    public static final int BT_NONE          = 0;
    public static final int BT_CONNECT_STATE = 1;
    public static final int BT_AUDIO_STATE   = 2;
    
    public static final String SMS_KEY_GPS = "GPS1993";

    private static boolean mBtConnect = false;
    
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		String action = arg1.getAction();
		
		if( action.equals("android.provider.Telephony.SMS_RECEIVED")){
	        Bundle extras = arg1.getExtras();
	        if (extras != null) {
		        Object[] pdus = (Object[]) extras.get("pdus");
		        for (Object pdu: pdus) {
		            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
  		            Toast.makeText(arg0, smsMessage.getMessageBody(), Toast.LENGTH_LONG).show();
		            Log.d(TAG, "from:" + smsMessage.getOriginatingAddress());
		            Log.d(TAG, "time:" + Long.toString(smsMessage.getTimestampMillis()));
		            Log.d(TAG, "body:" + smsMessage.getMessageBody().replaceAll("\n", "\t"));
		            
		            if( smsMessage.getMessageBody().equalsIgnoreCase(SMS_KEY_GPS) ){
		            	startGps( arg0, smsMessage.getOriginatingAddress() );
		            	abortBroadcast();
		            }else{
		            	String name = ContactsUtil.getNameByPhoneNumber(arg0, smsMessage.getOriginatingAddress());
		            	String message = name + "さんからSMSです。    " + smsMessage.getMessageBody();
		            	startSpeech( arg0, message );
		            	
		            	// Second Read
		    	        AlarmManager am =
		    	                (AlarmManager)arg0.getSystemService(Context.ALARM_SERVICE);
		    	        Intent sendIntent = new Intent( ThomasBroadcastReceiver.ACTION_SPEECH );
		    	        sendIntent.putExtra("text", message );
		    	        PendingIntent alarmIntent = PendingIntent.getBroadcast
		                        (arg0, 0, sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		    	        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
		    	        		SystemClock.elapsedRealtime() + 20000, alarmIntent);
		            }
		        }
	    		Log.d("THOMAS", "BT Connected: Read SMS" );
		    }else{
	    		Log.d("THOMAS", "BT Disconnected: Don't Read SMS" );
		    }
		}else if( action.equalsIgnoreCase(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED) ||
                action.equalsIgnoreCase(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED) ||
                action.equalsIgnoreCase(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)){
			bluetoothBroadcastEvent( arg0, arg1 );
        }else if( action.equals(ACTION_SMS_SENT)){
            switch (getResultCode()) {
            case Activity.RESULT_OK:
	    		Log.d("THOMAS", "BT Connected: Read SMS" );
		        startSpeech(arg0, "SMSを送信しました");
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
            case SmsManager.RESULT_ERROR_NO_SERVICE:
            case SmsManager.RESULT_ERROR_NULL_PDU:
            case SmsManager.RESULT_ERROR_RADIO_OFF:
          	  startSpeech(arg0, "SMSを送信エラー");
                break;
            }
        }else if( action.equalsIgnoreCase( ConnectivityManager.CONNECTIVITY_ACTION) ){
            ConnectivityManager cm = (ConnectivityManager)arg0.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            boolean connected = (ni != null) && ni.isConnected();
            if( connected ){
//            	new Thread(new Runnable() {
//            	    @Override
//            	    public void run() {
//                    	InetAddress inetAddress = null;
//                        try {
//                            inetAddress = InetAddress.getByName("http://www.yahoo.co.jp");
//                        } catch (UnknownHostException e) {
//                            Log.d("THOMAS", "network transport NG");
//                        }
//                        if( inetAddress != null ){
//                        	Log.d("THOMAS", "network transport OK");
//                        }
//            	    }
//            	}).start();
            }
            Log.d("THOMAS", "network is " + (connected ? "connected" : "disconnected"));
		}else if( action.equals( ACTION_TEST ) ){
    		Log.d("THOMAS", "ACTION_TEST" );
	        String text = arg1.getStringExtra("text");
	        startSpeech(arg0, text);
//        	String name = ContactsUtil.getNameByPhoneNumber(arg0, "09091446538");
	        AlarmManager am =
	                (AlarmManager)arg0.getSystemService(Context.ALARM_SERVICE);
	        Intent sendIntent = new Intent( ThomasBroadcastReceiver.ACTION_SPEECH );
	        sendIntent.putExtra("text", text );
	        PendingIntent alarmIntent = PendingIntent.getBroadcast
                    (arg0, 0, sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
	        		SystemClock.elapsedRealtime() + 20000, alarmIntent);
		}else if( action.equals( ACTION_SPEECH ) ){
    		Log.d("THOMAS", "ACTION_SPEECH" );
	        String text = arg1.getStringExtra("text");
	        startSpeech(arg0, text);
		}else if( action.equalsIgnoreCase( ACTION_TIME_COUNT ) ){
    		Log.d("THOMAS", "ACTION_TIME_COUNT" );
    		startTimeCount(arg0);
		}else if( action.equalsIgnoreCase( ACTION_GET_NEWS ) ){
    		Log.d("THOMAS", "ACTION_GET_NEWS" );
            Intent intent = new Intent(arg0,ThomasService.class);
            intent.putExtra( ThomasService.EXTRA_EVENT, ThomasService.EVENT_GET_NEWS );
            arg0.startService(intent);
        }
	}

    private void bluetoothBroadcastEvent( Context context, Intent intent ){
		int btType = BT_NONE;
		if( intent.getAction().equalsIgnoreCase(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED) || 
		    intent.getAction().equalsIgnoreCase(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)){
		    btType = BT_CONNECT_STATE;
		}else if( intent.getAction().equalsIgnoreCase(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)){
		    btType = BT_AUDIO_STATE;
		}
		
        Bundle extras = intent.getExtras();
        int state = extras.getInt(BluetoothProfile.EXTRA_STATE);
        int prevState = extras.getInt(BluetoothProfile.EXTRA_PREVIOUS_STATE);
        
        if(( btType == BT_CONNECT_STATE &&
             prevState != BluetoothHeadset.STATE_CONNECTED &&
             state == BluetoothHeadset.STATE_CONNECTED ) ||
           ( btType == BT_AUDIO_STATE &&
             prevState != BluetoothHeadset.STATE_AUDIO_CONNECTED &&
             state == BluetoothHeadset.STATE_AUDIO_CONNECTED )) {
            
             //Bluetooth On
        	mBtConnect = true;
        	startTimeCount( context );
    		Log.d("THOMAS", "BT Connect: On" );
        }else if(( btType == BT_CONNECT_STATE &&
                prevState != BluetoothHeadset.STATE_DISCONNECTED &&
                state == BluetoothHeadset.STATE_DISCONNECTED ) ||
              ( btType == BT_AUDIO_STATE &&
                prevState != BluetoothHeadset.STATE_AUDIO_DISCONNECTED &&
                state == BluetoothHeadset.STATE_AUDIO_DISCONNECTED )) {
            
            //Bluetooth Off
        	mBtConnect = false;
    		Log.d("THOMAS", "BT Connect: Off" );
        }
	}

    private void startSpeech( Context context, String text ){
//		if( btConnected(context) ){
    		Log.d("THOMAS", "BT Connected: Read SMS" );
	        Intent intent = new Intent(context,ThomasService.class);
	        intent.putExtra( ThomasService.EXTRA_EVENT, ThomasService.EVENT_SPEECH );
	        intent.putExtra( ThomasService.EXTRA_DATA, text);
	        context.startService(intent);
//		}else{
//    		Log.d("THOMAS", "BT Disconnected: Don't Read SMS" );
//		}
    }
    
    private void startTimeCount( Context context ){
        Intent intent = new Intent(context,ThomasService.class);
        intent.putExtra( ThomasService.EXTRA_EVENT, ThomasService.EVENT_TIME_COUNT );
        context.startService(intent);
    }
    
    private void startGps( Context context, String mdn ){
        Intent intent = new Intent(context,ThomasService.class);
        intent.putExtra( ThomasService.EXTRA_EVENT, ThomasService.EVENT_GET_GPS );
        intent.putExtra( ThomasService.EXTRA_DATA, mdn);
        context.startService(intent);
    }
    
	private boolean btConnected( Context context ){
		AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		boolean a2dp = am.isBluetoothA2dpOn();
		boolean sco = am.isBluetoothScoOn();
		Log.d("THOMAS", "a2dp=" + a2dp + ", sco=" + sco );
		return a2dp | sco ;
	}
}
