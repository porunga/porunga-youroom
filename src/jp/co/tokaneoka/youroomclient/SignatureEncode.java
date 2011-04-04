package jp.co.tokaneoka.youroomclient;

import java.io.UnsupportedEncodingException;

public class SignatureEncode {
	private static final String UNRESERVEDCHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.~";
	private static final String ESCAPECHARS = ":/=%&";
	public static final String encode(String s) throws UnsupportedEncodingException {
		
        byte[] bytes = s.getBytes("UTF-8");
        
        StringBuffer builder = new StringBuffer();
        for (byte b: bytes){
        	char c = (char) b;
        	if (c < 0x80 && UNRESERVEDCHARS.indexOf(String.valueOf(c)) >= 0) {
        		builder.append(String.valueOf(c));
        	}else if("\n".equals(String.valueOf(c))){
        		builder.append("%250A");
        	}else if(ESCAPECHARS.indexOf(String.valueOf(c))>=0){
        		builder.append("%" + String.valueOf(Integer.toHexString(b > 0 ? b : b + 256)).toUpperCase());
        	}else{
        		builder.append("%25" + String.valueOf(Integer.toHexString(b > 0 ? b : b + 256)).toUpperCase());    		
            		
        	}
        }
        String result = builder.toString();
        return result;
	}
}