package com.stars.services.chat.filter;

import com.stars.services.chat.ChatMessage;
import com.stars.services.chat.ChatServiceActor;
import com.stars.util.DirtyWords;

/**
 * Created by zhaowenshuo on 2016/9/26.
 */
public class DirtyWordChannelFilter extends ChatFilter {

//    private static Pattern p = Pattern.compile(StringUtil.objectPattern);

    public DirtyWordChannelFilter(String flag, ChatServiceActor serviceActor) {
        setFlag(flag);
        setServiceActor(serviceActor);
    }

    @Override
    public Object filter(Object object) {
    	
    	if (object instanceof ChatMessage) {
    		ChatMessage chatMessage = (ChatMessage)object;
            if (chatMessage.getContent().contains("</size>")
                    || chatMessage.getContent().contains("</i>")) {
                return null;
            }
            if (!chatMessage.containsObject()) {
                chatMessage.setContent(DirtyWords.normalizeChatMessage(chatMessage.getContent()));
            } else {
                chatMessage.setContent(DirtyWords.normalizeChatMessageWithObject(chatMessage.getContent()));
            }
            return chatMessage;
		}
        return object;
    }

//    private static String replaceSensitiveWordWithObject(String content) {
//        Matcher m = p.matcher(content);
//
//        String[] a = content.split(StringUtil.objectPattern);
//        // 防止用表情/道具分隔敏感字，拼在一起判断，如果存在，则一律替换
//        String s = StringUtil.concat(a);
//        if (StringUtil.hasSensitiveWord(s)) {
//            for (int i = 0; i < a.length; i++) {
//                a[i] = "*";
//            }
//        }
//        // 分段判断
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < a.length; i++) {
//            sb.append(StringUtil.replaceSensitiveWord(a[i])).append(m.find() ? m.group() : "");
//        }
//        while (m.find()) { sb.append(m.group()); }
//        return sb.toString();
//    }

}
