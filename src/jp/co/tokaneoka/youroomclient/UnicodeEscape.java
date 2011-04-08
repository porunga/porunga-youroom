package jp.co.tokaneoka.youroomclient;

/*
 * JavaでUnicodeエスケープ
 * - http://blog.junion.org/2010/11/03/id_74
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnicodeEscape {
	public static String encode(String s) {
		if (s == null)
			return null;
		StringBuilder sb = new StringBuilder(s.length() * 6);
		char[] chars = s.toCharArray();
		for (char c : chars) {
			if (c != '_' && !('0' <= c && c <= '9') && !('a' <= c && c <= 'z') && !('A' <= c && c <= 'Z')) {
				sb.append(String.format("\\u%04x", (int) c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String decode(String s) {
		if (s == null)
			return null;
		Pattern p = Pattern.compile("\\\\u[0-9a-f]{4}");
		Matcher m = p.matcher(s);
		StringBuffer sb = new StringBuffer(s.length());
		while (m.find()) {
			char[] chars = m.group().substring(2, 6).toCharArray();
			int hex = 0;
			for (char c : chars) {
				hex = hex << 4;
				if ('a' <= c && c <= 'f') {
					hex += c - 'a' + 10;
				} else {
					hex += c - '0';
				}
			}
			m.appendReplacement(sb, "" + (char) hex);
		}
		m.appendTail(sb);
		return sb.toString();
	}

}