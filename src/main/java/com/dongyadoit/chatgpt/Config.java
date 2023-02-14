package com.dongyadoit.chatgpt;

public class Config {
    private String email;
    private String password;
    private String userAgent;
    private String cfClearance;
    private String session_token;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getCfClearance() {
        return cfClearance;
    }

    public void setCfClearance(String cfClearance) {
        this.cfClearance = cfClearance;
    }

    public String getSession_token() {
        return session_token;
    }

    public void setSession_token(String session_token) {
        this.session_token = session_token;
    }
}
