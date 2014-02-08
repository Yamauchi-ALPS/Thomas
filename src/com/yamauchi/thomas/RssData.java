package com.yamauchi.thomas;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

public class RssData implements Serializable{
	private String saveData = null; 
	static private final String SAVE_FOLDER = "/data/data/com.yamauchi.thomas/files/";
	static private final String SAVE_FILE = "/rss.dat";
	private ArrayList<String> arrayList = new ArrayList();
	private int mCount = 0;
	
	public RssData( String data){
		saveData = data;
	}
	
    public void parseXML(Context context) {
    	RssData data = null;
        if( saveData.isEmpty() ){
            return;
        }
        
        XmlPullParser parser = Xml.newPullParser();
        
        try {
            // auto-detect the encoding from the stream
            InputStream is_xml = new ByteArrayInputStream(saveData.getBytes("UTF-8"));
            parser.setInput(is_xml, "UTF-8");
            
            int eventType = parser.getEventType();
            boolean done = false;
            
            while (eventType != XmlPullParser.END_DOCUMENT && !done) {
                String name = null;
                
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        Log.d("THOMAS", "Start: " + name );
                        break;
                    case XmlPullParser.END_TAG:
                        Log.d("THOMAS", "End: " + name );
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
        }
    }

    public boolean serialize( Context context, boolean saveVoice ){
        boolean ret = false;
        File folder = new File(SAVE_FOLDER);
        if( !folder.exists() ){
            if( !folder.mkdir() ){
                Log.d("THOMAS", "Serialize Error: mkdir failed.");
                return ret;
            }
        }
        
        try {
            //Serialize
            FileOutputStream fos = new FileOutputStream(new File(SAVE_FOLDER + SAVE_FILE));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
            ret = true;
        } catch (Exception e) {
            Log.d("THOMAS", "Serialize Error: " + e.toString() );
        }
        
        return ret;
    }

    static public RssData deSerialize( Context context ){
    	RssData ret = null;
        try {
            FileInputStream fis = new FileInputStream(new File(SAVE_FOLDER + SAVE_FILE));
            ObjectInputStream ois = new ObjectInputStream(fis);
            ret = (RssData) ois.readObject();
            ois.close();
        } catch (Exception e) {
            Log.d("THOMAS", "DeSerialize Error");
        }
        
        if( ret != null ){
        	ret.parseXML( context );
        }
        
        return ret;
    }
    
}
