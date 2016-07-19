/*******************************************************************************
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 ******************************************************************************/

package com.dishonest.util;


import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: chq
 * Date: 16-7-11
 * Time: 下午3:33
 * To change this template use File | Settings | File Templates.
 */
public class HttpUtil {
    boolean isProxy = false;
    String proxyURL;
    int proxyPort;
    private HashMap<String, String> mapCookies = new HashMap<String, String>();
    private String cookies = null;
    private HttpClient httpClient;
    private int sendTimes = 0;
    private int maxTimes = 5;

    public HttpUtil(boolean isProxy, String proxyURL) {
        this.isProxy = isProxy;
        this.proxyURL = proxyURL.split(":")[0];
        this.proxyPort = Integer.parseInt(proxyURL.split(":")[1]);
        httpClient = HttpClients.createDefault();
    }
    public HttpUtil() {
        httpClient = HttpClients.createDefault();
    }

    public String getProxyURL() {
        return proxyURL + ":" + proxyPort;
    }

    public void setProxyURL(String proxyURL) {
        this.proxyURL = proxyURL.split(":")[0];
        this.proxyPort = Integer.parseInt(proxyURL.split(":")[1]);
    }

    public HttpUtil clone() {
        HttpUtil instance = new HttpUtil();
        instance.mapCookies = this.mapCookies;
        instance.cookies = this.cookies;
        return instance;
    }

    public void clone(HttpUtil instance) {
        this.mapCookies = instance.mapCookies;
        this.cookies = instance.cookies;
    }

    public String doGetString(String url, Map params) throws InterruptedException {
        Object object;
        do {
            object = doGet(url, params);
            sendTimes++;
            if (object == null) {
                System.out.println(Thread.currentThread().getName() + ":" + url + ":" + params + ",doGetByte获取错误次数" + sendTimes + ",线程休眠");
                Thread.sleep(10000);
            }
        } while (sendTimes <= maxTimes && object == null);
        sendTimes = 0;
        return object.toString();
    }

    public byte[] doGetByte(String url, Map params) throws InterruptedException, ClassCastException {
        Object object;
        do {
            object = doGet(url, params);
            sendTimes++;
            if (object == null) {
                System.out.println(Thread.currentThread().getName() + ":" + url + ":" + params + ",doGetByte获取错误次数" + sendTimes + ",线程休眠");
                Thread.sleep(10000);
            }
        } while (sendTimes <= maxTimes && (object == null || object instanceof String));
        sendTimes = 0;
        return (byte[]) object;
    }

    public Object doGet(String url, Map params) throws InterruptedException {
        /* 建立HTTPGet对象 */
        String paramStr = "";
        if (params != null) {
            Iterator iter = params.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object key = entry.getKey();
                Object val = entry.getValue();
                paramStr += paramStr = "&" + key + "=" + val;
            }
        }

        if (!paramStr.equals("")) {
            paramStr = paramStr.replaceFirst("&", "?");
            url += paramStr;
        }
        HttpGet httpRequest = new HttpGet(url);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(30000).setConnectionRequestTimeout(30000)
                .setSocketTimeout(30000).build();
        httpRequest.setConfig(requestConfig);

        if (isProxy) {
            // 依次是代理地址，代理端口号，协议类型
            HttpHost proxy = new HttpHost(proxyURL, proxyPort, "http");
            RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
            httpRequest.setConfig(config);
        }

        setHeader(httpRequest);
        if (cookies != null) {
            httpRequest.setHeader("Cookie", cookies);
        }
        String errorInfo = "验证码错误:";
        try {
            /* 发送请求并等待响应 */
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            /* 若状态码为200 ok */
            getCookies(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                /* 读返回数据 */
                getCookies(httpResponse);
                HttpEntity entity = httpResponse.getEntity();
                Object result = null;
                if (entity.getContentType().getValue().startsWith("image")) {
                    result = EntityUtils.toByteArray(entity);
                } else {
                    result = EntityUtils.toString(entity, getcharset(httpResponse));
                }
                httpRequest.abort();
                sendTimes = 0;
                return result;
            } else {
                errorInfo += Thread.currentThread().getName() + ":" + url + ",doGet请求响应码：" + httpResponse.getStatusLine().getStatusCode();
            }
        } catch (ClientProtocolException e) {
            errorInfo += Thread.currentThread().getName() + ":" + url + ",调用doGet异常：" + e.getMessage();
        } catch (IOException e) {
            errorInfo += Thread.currentThread().getName() + ":" + url + ",调用doGet异常：" + e.getMessage();
        }
        httpRequest.abort();
        return errorInfo;
    }

    public String doPostString(String url, final Object... paramlist) throws InterruptedException {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < paramlist.length / 2; i++) {
            map.put(paramlist[i * 2].toString(), paramlist[i * 2 + 1]);
        }
        Object object;
        do {
            object = doPost(url, map);
            sendTimes++;
            if (object == null) {
                System.out.println(Thread.currentThread().getName() + ":" + url + ":" + map + ":doPost获取错误次数" + sendTimes + ",线程休眠");
                Thread.sleep(10000);
            }
        } while (sendTimes <= maxTimes && object == null);
        sendTimes = 0;
        return object.toString();
    }

    private Object doPost(String url, Map map) throws InterruptedException {
        /* 建立HTTPPost对象 */
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        Iterator iter = map.keySet().iterator();
        String key = "";
        String value = null;
        while (iter.hasNext()) {
            key = (String) iter.next();
            value = (String) map.get(key);
            params.add(new BasicNameValuePair(key, value));
        }

        HttpPost httpRequest = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(30000).setConnectionRequestTimeout(30000)
                .setSocketTimeout(30000).build();
        httpRequest.setConfig(requestConfig);
        if (isProxy) {
            // 依次是代理地址，代理端口号，协议类型
            HttpHost proxy = new HttpHost(proxyURL, proxyPort, "http");
            RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
            httpRequest.setConfig(config);
        }

        setHeader(httpRequest);
        if (cookies != null) {
            httpRequest.setHeader("Cookie", cookies);
        }
        String errorInfo = "验证码错误";
        try {
            /* 添加请求参数到请求对象 */
            httpRequest.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            /* 发送请求并等待响应 */
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            /* 若状态码为200 ok */
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                /* 读返回数据 */
                getCookies(httpResponse);
                HttpEntity entity = httpResponse.getEntity();
                Object result = null;
                if (entity.getContentType().getValue().startsWith("image")) {
                    result = EntityUtils.toByteArray(entity);
                } else {
                    result = EntityUtils.toString(entity, getcharset(httpResponse));
                }
                httpRequest.abort();
                sendTimes = 0;
                return result;
            } else {
                errorInfo += Thread.currentThread().getName() + ":" + url + ":" + params + ",post请求响应码：" + httpResponse.getStatusLine().getStatusCode();
            }

        } catch (Exception exception) {
            errorInfo += Thread.currentThread().getName() + ":" + url + ":" + params + ",调用doPost异常：" + exception.getMessage();
        }
        httpRequest.abort();
        return errorInfo;
    }

    private void setHeader(HttpRequestBase http) {
        http.setHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.1.6) Gecko/20091201 Firefox/3.5.6");
    }

    private void getCookies(HttpResponse response) {
        Header[] list = response.getHeaders("Set-Cookie");
        for (Header header : list) {
            String value = header.getValue();
            String[] arrParams = value.split(";");
            for (int j = 0; j < arrParams.length; j++) {
                String param = arrParams[j];
                int index = param.indexOf("=");
                if (index != -1) {
                    String name = param.substring(0, index);
                    mapCookies.put(name, value);
                }
            }
        }
        cookies = getCookie(mapCookies);
    }

    private String getCookie(HashMap<String, String> map) {
        StringBuffer stringBuffer = new StringBuffer();
        Iterator iter = map.keySet().iterator();
        String key = "";
        while (iter.hasNext()) {
            key = (String) iter.next();
            stringBuffer.append(map.get(key));
            stringBuffer.append(";");
        }
        return stringBuffer.toString();
    }

    private String getcharset(HttpResponse response) {
        Header[] list = response.getHeaders("Content-Type");
        for (Header header : list) {
            String value = header.getValue();
            String[] arrParams = value.split(";");
            for (int j = 0; j < arrParams.length; j++) {
                String param = arrParams[j];
                if (param.startsWith("charset=")) {
                    return param.substring(8);
                }
            }
        }
        return "UTF-8";
    }

}
