package com.yamauchi.thomas;

import java.net.InetAddress;
import java.net.UnknownHostException;


import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

import android.app.Service;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.IBinder;
import android.util.Log;

public class NewsRetriever extends Thread {
    private static final String HTTPS_CLIENT_TAG = "thomas_http";
    private final String [] mRssList ={
        	"http://http://sankei.jp.msn.com/rss/news/points.xml",
        	"http://sankei.jp.msn.com/rss/news/flash.xml",
        	"http://sankei.jp.msn.com/rss/news/affairs.xml",
        	"http://sankei.jp.msn.com/rss/news/politics.xml",
        	"http://sankei.jp.msn.com/rss/news/economy.xml",
        	"http://sankei.jp.msn.com/rss/news/world.xml",
        	"http://sankei.jp.msn.com/rss/news/sports.xml",
        	"http://sankei.jp.msn.com/rss/news/life.xml",
        	"http://sankei.jp.msn.com/rss/news/entertainments.xml",
        	"http://sankei.jp.msn.com/rss/news/science.xml",
        	"http://sankei.jp.msn.com/rss/news/region.xml",
        	"http://sankei.jp.msn.com/rss/news/soccer.xml",
    };
    private int mListCount = 0;
    
	@Override 
    public void run() { 
    	InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName("www.yahoo.co.jp");
        } catch (UnknownHostException e) {
            Log.d("THOMAS", "network transport NG");
        }
        if( inetAddress != null ){
        	Log.d("THOMAS", "network transport OK -> get News");
        	//Get News
        	for( String url : mRssList ){
        		String rss = retrieveRss( url );
//            	RssData rssData = RssData.parseXML( ThomasService.this, rss );
        	}
	    } 
    }
    
	private String retrieveRss( String url){
		String result = null;
        HttpPost httppost = null;
        try{
        	httppost = new HttpPost(url);
        }catch( Exception e ){
        	return null;
        }
        
        AndroidHttpClient client = AndroidHttpClient.newInstance(HTTPS_CLIENT_TAG);
        try {
            HttpResponse response = client.execute(httppost);
            result = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
        } finally {
            if(client != null){
                client.close();
            }
        }
        return result;
	}

}