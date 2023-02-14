package com.dongyadoit.chatgpt;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class ChatBot {
    private String conversationId = null;

    private String sessionToken = "";
    private String authorization = "";
    private String cfClearance = "";
    private String userAgent = "";
    private String parentId = "";

    private Map<String, String> headers = new HashMap<>();
    private String conversationIdPrev;
    private String parentIdPrev;

    /**
     * 是否是付费用户
     * */
    private boolean isPaidUser = false;
    private String paidUId;

    /**
     * 请求的 model
     *
     * @see TextModel
     * */
    private TextModel textModel = TextModel.TEXT_FREE_MODEL;

    public ChatBot(Config config) {
        this(config, null);
    }

    public ChatBot(Config config, String conversationId) {
        this.sessionToken = config.getSession_token();
        this.cfClearance = config.getCfClearance();
        this.userAgent = config.getUserAgent();
        this.conversationId = conversationId;
        this.parentId = UUID.randomUUID().toString();
        if (StrUtil.isNotEmpty(sessionToken)) {
            refreshSession();
        }
    }

    /**
     * 构造
     *
     * @param sessionToken
     * @param cfClearance
     * @param userAgent
     * @param paidUId
     */
    public ChatBot(String sessionToken, String cfClearance, String userAgent, String paidUId) {
        this.userAgent = userAgent;
        this.cfClearance = cfClearance;
        this.sessionToken = sessionToken;
        this.parentId = UUID.randomUUID().toString();
        this.isPaidUser = paidUId != null && paidUId.length() > 10;
        if (this.isPaidUser) {
            this.paidUId = paidUId;
            this.textModel = TextModel.TEXT_PAID_MODEL; // 付费用户使用 TEXT_PAID_MODEL
        }
        refreshSession();
    }

    /**
     * 设置请求的 model
     *
     * 请特别注意 免费用户只能使用 TEXT_FREE_MODEL
     *
     * @param model, TextModel
     * */
    public void setRequestTextDataModel(TextModel model) {
        this.textModel = model;
    }

    // Resets the conversation ID and parent ID
    public void resetChat() {
        this.conversationId = null;
        this.parentId = UUID.randomUUID().toString();
    }


    // Refreshes the headers -- Internal use only
    public void refreshHeaders() {

        if (StrUtil.isEmpty(authorization)) {
            authorization = "";
        }
        this.headers = new HashMap<String, String>() {{
            put("Host", "chat.openai.com");
            put("Accept", "text/event-stream");
            put("Authorization", "Bearer " + authorization);
            put("Content-Type", "application/json");
            put("User-Agent", userAgent);
            put("X-Openai-Assistant-App-Id", "");
            put("Connection", "close");
            put("Accept-Language", "en-US,en;q=0.9");
            put("Referer", "https://chat.openai.com/chat");
        }};

    }


    Map<String, Object> getChatStream(Map<String, Object> data) {
        String url = "https://chat.openai.com/backend-api/conversation";

        String body = HttpUtil.createPost(url)
                .headerMap(headers, true)
                .body(JSON.toJSONString(data), "application/json")
                .execute()
                .body();

        String message = "";
        Map<String, Object> chatData = new HashMap<>();
        for (String s : body.split("\n")) {
            if ((s == null) || "".equals(s)) {
                continue;
            }
            if (s.contains("data: [DONE]")) {
                continue;
            }

            String part = s.substring(5);
            JSONObject lineData = JSON.parseObject(part);

            try {

                JSONArray jsonArray = lineData.getJSONObject("message")
                        .getJSONObject("content")
                        .getJSONArray("parts");

                if (jsonArray.size() == 0) {
                    continue;
                }
                message = jsonArray.getString(0);

                conversationId = lineData.getString("conversation_id");
                parentId = (lineData.getJSONObject("message")).getString("id");

                chatData.put("message", message);
                chatData.put("conversation_id", conversationId);
                chatData.put("parent_id", parentId);
            } catch (Exception e) {
                System.out.println("getChatStream Exception: " + part);
                //  e.printStackTrace();
                continue;
            }

        }
        return chatData;

    }

    // Gets the chat response as text -- Internal use only
    public Map<String, Object> getChatText(Map<String, Object> data) {

        // Create request session
        Session session = new Session();

        // set headers
        session.setHeaders(this.headers);

        // Set multiple cookies
        session.getCookies().put("__Secure-next-auth.session-token", sessionToken);
        session.getCookies().put("__Secure-next-auth.callback-url", "https://chat.openai.com/");
        session.getCookies().put("cf_clearance", cfClearance);
        if (this.isPaidUser) {
            session.getCookies().put("_puid", paidUId);
        }


        // Set proxies
        setupProxy(session);

        HttpResponse response = session.post2("https://chat.openai.com/backend-api/conversation",
                data);
        String body = response.body();

        String errorDesc = "";


        String message = "";
        Map<String, Object> chatData = new HashMap<>();
        for (String s : body.split("\n")) {
            if ((s == null) || "".equals(s)) {
                continue;
            }
            if (s.contains("data: [DONE]")) {
                continue;
            }
            if (s.contains("event: ping")) {
                continue;
            }

            if (!s.contains("data")) {
                System.out.println("invalid message: " + s);
                continue;
            }

            if (s.length() < 6) {
                continue;
            }

            String part = s.substring(5);

            try {
                JSONObject lineData = JSON.parseObject(part);

                JSONArray jsonArray = lineData.getJSONObject("message")
                        .getJSONObject("content")
                        .getJSONArray("parts");

                if (jsonArray.size() == 0) {
                    continue;
                }
                message = jsonArray.getString(0);

                conversationId = lineData.getString("conversation_id");
                parentId = (lineData.getJSONObject("message")).getString("id");

                chatData.put("message", message);
                chatData.put("conversation_id", conversationId);
                chatData.put("parent_id", parentId);
            } catch (Exception e) {
                System.out.println("getChatStream Exception: " + part);
                //  e.printStackTrace();
                continue;
            }

        }
        return chatData;
    }

    private void setupProxy(Session session) {
//        if (config.get("proxy") != null && !config.get("proxy").equals("")) {
//            Map<String, String> proxies = new HashMap<>();
//            proxies.put("http", config.get("proxy"));
//            proxies.put("https", config.get("proxy"));
//            session.setProxies(proxies);
//        }
    }

    public Map<String, Object> getChatResponse(String prompt, String output) {
        Map<String, Object> data = new HashMap<>();
        data.put("action", "next");
        data.put("conversation_id", this.conversationId);
        data.put("parent_message_id", this.parentId);
        data.put("model", this.textModel.getValue());

        Map<String, Object> message = new HashMap<>();
        message.put("id", UUID.randomUUID().toString());
        message.put("role", "user");
        Map<String, Object> content = new HashMap<>();
        content.put("content_type", "text");
        content.put("parts", Collections.singletonList(prompt));
        message.put("content", content);
        data.put("messages", Collections.singletonList(message));

        this.conversationIdPrev = this.conversationId;
        this.parentIdPrev = this.parentId;

        if (output.equals("text")) {
            return this.getChatText(data);
        } else if (output.equals("stream")) {
            return this.getChatStream(data);
        } else {
            throw new RuntimeException("Output must be either 'text' or 'stream'");
        }
    }

    public Map<String, Object> getChatResponse(String prompt) {
        return this.getChatResponse(prompt, "text");
    }

    public void refreshSession() {

        if (sessionToken == null || sessionToken.equals("")) {
            throw new RuntimeException("No tokens provided");
        }
        Session session = new Session();

        // Set proxies
        setupProxy(session);

        // Set cookies
        session.getCookies().put("__Secure-next-auth.session-token", sessionToken);
        session.getCookies().put("cf_clearance", cfClearance);
        if (this.isPaidUser) {
            session.getCookies().put("_puid", paidUId);
        }

        String cookiesString = session.getCookiesString();
        Map<String, String> map = new HashMap<>();
        map.put("user-agent", userAgent);
        map.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        map.put("Host", "chat.openai.com");
        map.put("Accept", "text/event-stream");
        map.put("Content-Type", "application/json");

        map.put("Cache-control", "no-cache");
        map.put("X-Openai-Assistant-App-Id", "");
        map.put("Connection", "close");
        map.put("Accept-Language", "en-US,en;q=0.9");
        map.put("Referer", "https://chat.openai.com/");

        map.put("cookie", cookiesString);
        map.put("Cookie", cookiesString);
        session.setHeaders(map);
        String urlSession = "https://chat.openai.com/api/auth/session";
        HttpResponse response = session.get2(urlSession);

        if (response.getStatus() != 200) {
            System.err.println("err code: " + response.getStatus());
            System.err.println("cf_clearance: " + cfClearance);
            System.err.println("token: " + sessionToken);
            System.err.println("userAgent: " + userAgent);

            System.err.println("请检查以上参数是否正确，是否过期。并且获取以上参数的浏览器要和本程序在同一IP地址" );
            System.err.println("Please check whether the above parameters are correct or expired. And the browser that obtains the above parameters must be at the same IP address as this program" );


            System.out.println(response.body().toString());
            return;
        }

        try {
            String name = "__Secure-next-auth.session-token";
            String cookieValue = response.getCookieValue(name);
            sessionToken = cookieValue;

            String body = response.body();
            System.out.println("session_token: " + cookieValue);
            JSONObject responseObject = JSON.parseObject(body);

            String accessToken = responseObject.getString("accessToken");
            System.out.println("accessToken: " + accessToken);

            authorization = accessToken;

            this.refreshHeaders();
        } catch (Exception e) {
            System.out.println("Error refreshing session");
        }
    }

    public void rollbackConversation() {
        this.conversationId = this.conversationIdPrev;
        this.parentId = this.parentIdPrev;
    }

    public static JSONObject resJson(Response response) throws IOException {
        JSONObject responseObject = null;
        String text = response.body().string();
        try {
            response.body().close();
            responseObject = JSON.parseObject(text);
        } catch (Exception e) {
            System.out.println("json err, body: " + text);
            throw new RuntimeException(e);
        }

        return responseObject;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getCfClearance() {
        return cfClearance;
    }

    public void setCfClearance(String cfClearance) {
        this.cfClearance = cfClearance;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getPaidUId() {
        return paidUId;
    }

    public void setPaidUId(String paidUId) {
        this.paidUId = paidUId;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getConversationIdPrev() {
        return conversationIdPrev;
    }

    public void setConversationIdPrev(String conversationIdPrev) {
        this.conversationIdPrev = conversationIdPrev;
    }

    public String getParentIdPrev() {
        return parentIdPrev;
    }

    public void setParentIdPrev(String parentIdPrev) {
        this.parentIdPrev = parentIdPrev;
    }
}
