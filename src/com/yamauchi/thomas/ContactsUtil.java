package com.yamauchi.thomas;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class ContactsUtil {

	public static String getNameByPhoneNumber( Context context, String phone ){
		String ret = phone;
		
//		ContentResolver resolver = context.getContentResolver();
//		Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI,
//		                                               null, null, null, null);
//
//		String contactsName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		String[] projection    = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
		                ContactsContract.CommonDataKinds.Phone.NUMBER};
		String condition = ContactsContract.CommonDataKinds.Phone.NUMBER +" = "+ phone;
		Cursor people = context.getContentResolver().query(uri, projection, condition, null, null);
		if( people != null ){
			int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
//			int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
			people.moveToFirst();
		    ret   = people.getString(indexName);
//		    String number = people.getString(indexNumber);
		    people.close();
		}
		return ret;
	}
	
}
