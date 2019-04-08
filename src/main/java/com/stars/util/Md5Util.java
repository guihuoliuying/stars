package com.stars.util;

import java.security.MessageDigest;

/**
 *
 */
public class Md5Util {

    private static char[] chars = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f',
    };

    public static String getMD5Str(String str) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(str.getBytes("UTF-8"));
            byte[] bytes = digest.digest();
            char[] md5 = new char[32];
            int len = bytes.length;
            for (int i = 0; i < len; i++) {
                // 通过查表进行转换，比Integer.toHexString()要快
                md5[i<<1] = chars[(bytes[i] & 0xF0) >> 4];
                md5[(i<<1)+1] = chars[bytes[i] & 0x0F];
            }
            return new String(md5);
        } catch (Exception e) {
            // fixme: 怎么记录日志
        }
        return null;
    }
    
    public static String md5(String str){
    	try {
          MessageDigest messageDigest = MessageDigest.getInstance("MD5");
          messageDigest.reset();
          messageDigest.update(str.getBytes("UTF-8"));
          byte[] byteArray = messageDigest.digest();
          StringBuilder stringBuilder = new StringBuilder();
          for (int i = 0; i < byteArray.length; i++) {
              String t = Integer.toHexString(0xFF & byteArray[i]);
              if (t.length() == 1)
                  stringBuilder.append("0");
              stringBuilder.append(t);
          }
          return stringBuilder.toString();
      } catch (Exception e) {
          LogUtil.error(e.getMessage());
      }
      return null;
    }
    

}
