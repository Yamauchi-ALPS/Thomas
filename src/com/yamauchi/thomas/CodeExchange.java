package com.yamauchi.thomas;

import java.util.HashMap;
import java.util.Map;

public class CodeExchange {
	  private Map<String, String> m = new HashMap<String, String>();

	  public CodeExchange() {
	        m.put("�A", "a");
	        m.put("�C", "i");
	        m.put("�E", "u");
	        m.put("�G", "e");
	        m.put("�I", "o");
	        m.put("�J", "ka");
	        m.put("�L", "ki");
	        m.put("�N", "ku");
	        m.put("�P", "ke");
	        m.put("�R", "ko");
	        m.put("�T", "sa");
	        m.put("�V", "shi");
	        m.put("�X", "su");
	        m.put("�Z", "se");
	        m.put("�\", "so");
	        m.put("�^", "ta");
	        m.put("�`", "chi");
	        m.put("�c", "tu");
	        m.put("�e", "te");
	        m.put("�g", "to");
	        m.put("�i", "na");
	        m.put("�j", "ni");
	        m.put("�k", "nu");
	        m.put("�l", "ne");
	        m.put("�m", "no");
	        m.put("�n", "ha");
	        m.put("�q", "hi");
	        m.put("�t", "fu");
	        m.put("�w", "he");
	        m.put("�z", "ho");
	        m.put("�}", "ma");
	        m.put("�~", "mi");
	        m.put("��", "mu");
	        m.put("��", "me");
	        m.put("��", "mo");
	        m.put("��", "ya");
	        m.put("��", "yu");
	        m.put("��", "yo");
	        m.put("��", "ra");
	        m.put("��", "ri");
	        m.put("��", "ru");
	        m.put("��", "re");
	        m.put("��", "ro");
	        m.put("��", "wa");
	        m.put("��", "wo");
	        m.put("��", "n");
	        m.put("�K", "ga");
	        m.put("�M", "gi");
	        m.put("�O", "gu");
	        m.put("�Q", "ge");
	        m.put("�S", "go");
	        m.put("�U", "za");
	        m.put("�W", "zi");
	        m.put("�Y", "zu");
	        m.put("�[", "ze");
	        m.put("�]", "zo");
	        m.put("�_", "da");
	        m.put("�a", "di");
	        m.put("�d", "du");
	        m.put("�f", "de");
	        m.put("�h", "do");
	        m.put("�o", "ba");
	        m.put("�r", "bi");
	        m.put("�u", "bu");
	        m.put("�x", "be");
	        m.put("�{", "bo");
	        m.put("�p", "pa");
	        m.put("�s", "pi");
	        m.put("�v", "pu");
	        m.put("�y", "pe");
	        m.put("�|", "po");
	        m.put("�L��", "kya");
	        m.put("�L��", "kyu");
	        m.put("�L��", "kyo");
	        m.put("�V��", "sya");
	        m.put("�V��", "syu");
	        m.put("�V��", "syo");
	        m.put("�`��", "tya");
	        m.put("�`��", "tyu");
	        m.put("�`��", "tyo");
	        m.put("�j��", "nya");
	        m.put("�j��", "nyu");
	        m.put("�j��", "nyo");
	        m.put("�q��", "hya");
	        m.put("�q��", "hyu");
	        m.put("�q��", "hyo");
	        m.put("����", "rya");
	        m.put("����", "ryu");
	        m.put("����", "ryo");
	        m.put("�M��", "gya");
	        m.put("�M��", "gyu");
	        m.put("�M��", "gyo");
	        m.put("�W��", "zya");
	        m.put("�W��", "zyu");
	        m.put("�W��", "zyo");
	        m.put("�a��", "dya");
	        m.put("�a��", "dyu");
	        m.put("�a��", "dyo");
	        m.put("�r��", "bya");
	        m.put("�r��", "byu");
	        m.put("�r��", "byo");
	        m.put("�s��", "pya");
	        m.put("�s��", "pyu");
	        m.put("�s��", "pyo");
	        m.put("�[", "-");
	        m.put("�", "a");
	        m.put("�B", "-");
	        m.put("�D", "u");
	        m.put("�F", "e");
	        m.put("�H", "o");
	}

	  public String kana2roma(String s) {
	    StringBuilder t = new StringBuilder();
	    for ( int i = 0; i < s.length(); i++ ) {
	      if ( i <= s.length() - 2 )  {
	        if ( m.containsKey(s.substring(i,i+2))) {
	          t.append(m.get(s.substring(i, i+2)));
	          i++;
	        } else if (m.containsKey(s.substring(i, i+1))) {
	          t.append(m.get(s.substring(i, i+1)));
	        } else if ( s.charAt(i) == '�b' ) {
	          t.append(((String) m.get(s.substring(i+1, i+2))).charAt(0));
	        } else {
	          t.append(s.charAt(i));
	        }
	      } else {
	        if (m.containsKey(s.substring(i, i+1))) {
	          t.append(m.get(s.substring(i, i+1)));
	        } else {
	          t.append(s.charAt(i));
	        }
	      }
	    }
	    return t.toString();
	  }
}
