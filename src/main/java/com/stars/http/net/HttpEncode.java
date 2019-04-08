package com.stars.http.net;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-7-30
 * Time: 上午10:36
 * To change this template use File | Settings | File Templates.
 */
public class HttpEncode {

    public static byte[] doMyEncode(byte[] input){
            for(int i=1;i<=input.length;i++){
//                if(i%5==0){
//                    input[i-1]=(byte)(input[i-1]+7);
//                }else if(i%4==0){
//                    input[i-1]=(byte)(input[i-1]+3);
//                }else{
//                    input[i-1]=(byte)(input[i-1]+(i*7)%10);
//                }
                input[i-1]=(byte)(input[i-1]+(i*i + i *4)%10);
            }
        return input;
    }
}
