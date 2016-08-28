/*******************************************************************************
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 ******************************************************************************/

package com.dishonest.handler;

import com.dishonest.dao.ConnUtil;
import com.dishonest.util.HttpUtil;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: zhangkl
 * Date: 七月,2016
 */
public class ProxyHandler implements Runnable {

    Logger logger = Logger.getLogger(ProxyHandler.class);

    private int sendtimes = 0;
    private int maxtimes = 5;

    String proxyurl;

    public ProxyHandler(HttpUtil httpUtil) {
        this.httpUtil = httpUtil;
    }

    HttpUtil httpUtil;

    public ProxyHandler(String proxyurl) {
        this.proxyurl = proxyurl;
    }

    public ProxyHandler() {

    }

    public static void main(String[] args) throws SQLException {
        /*try {
            ProxyHandler proxyHandler = new ProxyHandler();
            proxyHandler.getProxy("http://www.youdaili.net/Daili/http/4789.html");
        } catch (ParserException e) {
            e.printStackTrace();
        }*/
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        try {
            for (int i = 0; i < 6; i++) {
                ProxyHandler proxyHandler = new ProxyHandler("127.0.0.1:108"+i);
                executorService.execute(proxyHandler);
            }
            List list = ConnUtil.getInstance().executeQueryForList("select * from cred_dishonesty_proxy where isusered != 2");
            Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                Map map = (Map) iterator.next();
                String proxyurl = (String) map.get("PROXYURL");
                ProxyHandler proxyHandler = new ProxyHandler(proxyurl);
                executorService.execute(proxyHandler);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        executorService.shutdown();
    }

    @Override
    public void run() {
        try {
            checkProxy("http://shixin.court.gov.cn/image.jsp");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getProxy(String url) throws ParserException, SQLException {
        Parser parser = new Parser(url);
        parser.setEncoding("utf-8");
        NodeFilter filter1 = new HasAttributeFilter("style", "font-size:14px;");
        NodeFilter filter2 = new TagNameFilter("span");
        AndFilter contentFilter = new AndFilter(filter1, filter2);
        NodeList nodes = parser.extractAllNodesThatMatch(contentFilter);
        System.out.println(nodes.asString());
        String[] strings = nodes.asString().split("\n");
        for (int i = 0; i < strings.length - 5; i++) {
            if (strings[i].contains("@")) {
                System.out.println(i + ":" + strings[i].split("@")[0]);
                try {
                    ConnUtil.getInstance().executeSaveOrUpdate("insert into cred_dishonesty_proxy (proxyurl,dgetdata,isusered) values ('" + strings[i].split("@")[0] + "',sysdate,0)");
                } catch (SQLException e) {
                    if (e.getMessage().contains("ORA-00001: 违反唯一约束条件 (CRED.PK_PROXY)")) {
                        System.out.println(e.getMessage());
                        ConnUtil.getInstance().executeSaveOrUpdate("update cred_dishonesty_proxy set isusered = 0 where proxyurl = '" + strings[i].split("@")[0] + "'");
                        continue;
                    } else {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public void checkProxy(String url) throws SQLException {
        HttpGet httpRequest = new HttpGet(url);

        HttpHost proxy = new HttpHost(proxyurl.split(":")[0].toString(), Integer.valueOf(proxyurl.split(":")[1]), "http");
        RequestConfig config = RequestConfig.custom().setProxy(proxy).setConnectTimeout(2000).setConnectionRequestTimeout(2000)
                .setSocketTimeout(2000).build();
        httpRequest.setConfig(config);
        int status ;
        try {
            long startTime = System.currentTimeMillis();
            HttpClient httpClient = HttpClients.createDefault();
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            long endTime = System.currentTimeMillis();
            status =  httpResponse.getStatusLine().getStatusCode();
            System.out.println(proxyurl + ":" + status+"响应时间:" + (endTime - startTime) + "ms");
        } catch (Exception e) {
            System.out.println(proxyurl+e.getMessage());
            status =  0;
        }
        if (status != 403) {
            ConnUtil.getInstance().executeSaveOrUpdate("update cred_dishonesty_proxy set isusered = 2 where proxyurl = '" + proxyurl + "'");
        }
    }

    public void getNetStatus() throws IOException {
        Process process = Runtime.getRuntime().exec("ping shixin.court.gov.cn -t");
        InputStreamReader isr = new InputStreamReader(process.getInputStream(), "GBK");
        LineNumberReader reader = new LineNumberReader(isr);
        String line = "";
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }

}
