package com.dongyadoit.chatgpt;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import net.dreamlu.mica.http.HttpRequest;
import okhttp3.Response;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;


public class Session {
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> cookies = new HashMap<>();
    private Map<String, String> proxies = new HashMap<>();

    public Session() {
    }


    public Response post(String url, Map<String, Object> data) {
        getCookiesString();
        return HttpRequest.post(url)
                .addHeader(headers)
                .bodyJson(data)
                .execute()
                .response();
    }

    public void setRequestProxy(cn.hutool.http.HttpRequest request) {
        SocketAddress address = new InetSocketAddress("127.0.0.1", 1080);
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, address);
        request.setProxy(proxy);
    }

    public HttpResponse post2(String url, Map<String, Object> data) {
        String cookie = getCookiesString();
        cn.hutool.http.HttpRequest request =  HttpUtil.createPost(url);
        return request.addHeaders(headers)
                .cookie(cookie)
                .body(JSON.toJSONString(data), "application/json")
                .execute();

    }

    public Response post(String url, Map<String, Object> data, boolean followRedirects) {
        getCookiesString();
        return HttpRequest.post(url)
                .addHeader(headers)
                .followRedirects(followRedirects)
                .bodyJson(data)
                .execute()
                .response();
    }

    public Response get(String url, Map<String, String> data) {

        getCookiesString();
        Map<String, Object> map = new HashMap<>(data);
        Response response = HttpRequest.get(url)
                .addHeader(headers)
                .queryMap(map)
                .execute()
                .response();

        return response;
    }

    public HttpResponse get2(String url) {
        getCookiesString();
        cn.hutool.http.HttpRequest request = HttpUtil.createGet(url);
        return request.addHeaders(headers)
                .cookie(getCookiesString())
                .execute();
    }


    public String getString(String url, Map<String, String> data) {

        getCookiesString();
        Map<String, Object> map = new HashMap<>(data);
        return HttpRequest.get(url)
                .addHeader(headers)
                .queryMap(map)
                .execute()
                .asString();
    }

    public Response get(String url, Map<String, String> data, boolean followRedirects) {
        getCookiesString();
        Map<String, Object> map = new HashMap<>(data);

        return HttpRequest.get(url)
                .addHeader(headers)
                .followRedirects(followRedirects)
                .queryMap(map)
                .execute()
                .response();
    }

    public Response post(String url, Map<String, String> headers, String payload) {

        getCookiesString();
        return HttpRequest.post(url)
                .addHeader(headers)
                .bodyJson(payload)
                .execute()
                .response();
    }


    public String getCookiesString() {
        String result = "";
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            result = result + key + "=" + value + "; ";
        }
        headers.put("cookie", result);
        return result;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public Map<String, String> getProxies() {
        return proxies;
    }

    public void setProxies(Map<String, String> proxies) {
        this.proxies = proxies;
    }
}
