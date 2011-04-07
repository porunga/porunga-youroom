package jp.co.tokaneoka.youroomclient;

import java.io.UnsupportedEncodingException;

public class SignatureEncode {
	private static final String UNRESERVEDCHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.~";

	public static final String encode(String s)
			throws UnsupportedEncodingException {

		byte[] bytes = s.getBytes("UTF-8");

		StringBuffer builder = new StringBuffer();
		for (byte b : bytes) {
			char c = (char) b;
			if (UNRESERVEDCHARS.indexOf(String.valueOf(c)) >= 0) {
				builder.append(String.valueOf(c));
			} else if ("\n".equals(String.valueOf(c))) {
				builder.append("%0A");
			} else {
				builder.append("%"
						+ String.valueOf(
								Integer.toHexString(b > 0 ? b : b + 256))
								.toUpperCase());
			}
		}
		return builder.toString();
	}
}