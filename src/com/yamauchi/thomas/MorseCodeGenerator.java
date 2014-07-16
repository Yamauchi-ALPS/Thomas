package com.yamauchi.thomas;

import java.util.ArrayList;

import android.content.Context;
import android.os.Vibrator;

public class MorseCodeGenerator {
	static private final int o = 100;
	static private final int _ = o * 3;
	static private final int SPAN = o;
	static private final int SEPARATE = o * 7;
	
	static private final int [][] ABC ={
		{ o,_ },		//A
		{ _,o,o,o, },    //B
		{ _,o,_,o },     //C
		{ _,o,o },       //D
		{ o },           //E
		{ o,o,_,o },     //F
		{ _,_,o },       //G
		{ o,o,o,o, },    //H
		{ o,o },         //I
		{ o,_,_,_ },     //J
		{ _,o,_ },       //K
		{ o,_,o,o },     //L
		{ _,_ },         //M
		{ _,o },         //N
		{ _,_,_ },       //O
		{ o,_,_,o },     //P
		{ _,_,o,_ },     //Q
		{ o,_,o },       //R
		{ o,o,o },       //S
		{ _ },           //T
		{ o,o,_ },       //U
		{ o,o,o,_ },     //V
		{ o,_,_ },       //W
		{ _,o,o,_ },     //X
		{ _,o,_,_ },     //Y
		{ _,_,o,o }};     //Z

	static private final int [][] NUM ={
		{ _,_,_,_,_ },	//0
		{ o,_,_,_,_ },	//1
		{ o,o,_,_,_ },	//2
		{ o,o,o,_,_ },	//3
		{ o,o,o,o,_ },	//4
		{ o,o,o,o,o },	//5
		{ _,o,o,o,o },	//6
		{ _,_,o,o,o },	//7
		{ _,_,_,o,o },	//8
		{ _,_,_,_,o }};	//9

	
	public MorseCodeGenerator( Context context, String code ){
	}
	
	static public void vibrate( Context context, String code ){
		ArrayList<int[]> codeArray = new ArrayList<int[]>();
		int iVal = 0;
		int charCnt = 0;
		for( int i=0; i<code.length(); i++ ){
			iVal = code.charAt(i);
			if( iVal >= 65 && iVal <= 90 ){
				codeArray.add( ABC[iVal-65] );
				charCnt += ABC[iVal-65].length;
			}else if( iVal >= 97 && iVal <= 122 ){
				codeArray.add( ABC[iVal-97] );
				charCnt += ABC[iVal-97].length;
			}else if( iVal >= 48 && iVal <= 57 ){
				codeArray.add( NUM[iVal-48] );
				charCnt += NUM[iVal-48].length;
			}
		}
	
    	Vibrator vib = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
    	int cnt = 0;
        long [] pattern = new long[charCnt * 2];
		pattern[cnt++] = SEPARATE;
		for( int [] codeList : codeArray ){
			for( int c : codeList ){
				pattern[cnt++] = c;
				if( cnt < pattern.length ){
					pattern[cnt++] = SPAN;
				}
			}
			if( cnt < pattern.length ){
				pattern[cnt-1] = SEPARATE;
			}
		}
        vib.vibrate(pattern, -1);
	}
	
}
