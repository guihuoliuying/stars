package test;

import com.stars.util.Md5Util;



public class testMd5 {
	public static String publicKey = "xjw1314";
	public static void main(String[] arg0s){
		String a = "{\"argList\": {}, \"opType\": 1001, \"args\": {\"value\": 0}}";
		System.err.println(publicKey+a);
		System.err.println(Md5Util.getMD5Str(publicKey+a));
		System.err.println(Md5Util.md5(a));
		System.err.println(Md5Util.md5("xjw1314"));
	}
}
