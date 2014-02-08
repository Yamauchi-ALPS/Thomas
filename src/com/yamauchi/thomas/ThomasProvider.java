package com.yamauchi.thomas;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ThomasProvider extends SQLiteOpenHelper { 
    final static private int DB_VERSION = 1; 
    public ThomasProvider(Context context) { 
        super(context, null, null, DB_VERSION); 
    } 
    
    @Override 
    public void onCreate(SQLiteDatabase db) { 
        // table create 
        db.execSQL( 
            "create table person_table("+ 
            "   name text not null,"+ 
            "   age text"+ 
            ");" 
        ); 

        // table row insert 
        db.execSQL("insert into person_table(name,age) values ('本田 圭佑', 24);"); 
        db.execSQL("insert into person_table(name,age) values ('遠藤 保仁', 30);"); 
        db.execSQL("insert into person_table(name,age) values ('松井 大輔', 29);"); 
    } 

    @Override 
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { 
        // データベースの変更が生じた場合は、ここに処理を記述する。 
    } 
} 
