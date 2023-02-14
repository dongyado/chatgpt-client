package com.dongyadoit.chatgpt.test;
import com.dongyadoit.chatgpt.ChatBot;
import org.junit.Test;
import java.util.Map;

public class TestChatGPT {

    @Test
    public void testPaidUser() {
        String sessionToken = "";
        String cfClearance = "";
        String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.0.0 Safari/537.36";
        String paidUid = "";

        String ask = "请给出5个儿童粉红贸易的卖点，每个卖点不超过10个字";
        ChatBot chatbot = new ChatBot(sessionToken, cfClearance, userAgent, paidUid);

        Map<String, Object> chatResponse = chatbot.getChatResponse(ask);
        System.out.println(chatResponse.get("message"));
    }

    @Test
    public void testFreeUser() {
        String sessionToken ="";
        String cfClearance = "";
        String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.0.0 Safari/537.36";
        String paidUid = "";

        String ask = "请给出5个儿童粉红贸易的卖点，每个卖点不超过10个字";
        ChatBot chatbot = new ChatBot(sessionToken, cfClearance, userAgent, paidUid);

        Map<String, Object> chatResponse = chatbot.getChatResponse(ask);
        System.out.println(chatResponse.get("message"));
    }
}
