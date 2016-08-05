
/*******************************************************************************
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 ******************************************************************************/

package com.dishonest.util;

import com.dishonest.handler.DishonestyService;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;


public class HttpUtilPool {

    Logger logger = Logger.getLogger(HttpUtilPool.class);
    private Hashtable connections = new Hashtable();
    private int sendTime = 0;
    private int maxSendTime = 5;
    private long waitTime = 5000;

    public HttpUtilPool(int noProxySize, int initial) throws SQLException, InterruptedException {
        initializePool(noProxySize, initial);
    }

    public HttpUtil getHttpUtil() throws GetDateException {
        HttpUtil httpUtil = null;
        Enumeration cons = connections.keys();
        sendTime = 0;
        do {
            synchronized (connections) {
                while (cons.hasMoreElements()) {
                    httpUtil = (HttpUtil) cons.nextElement();

                    Boolean b = (Boolean) connections.get(httpUtil);
                    if (b == Boolean.FALSE) {
                        connections.put(httpUtil, Boolean.TRUE);
                        return httpUtil;
                    }
                }
                try {
                    connections.wait(waitTime);
                    sendTime++;
                } catch (InterruptedException e) {
                    logger.error("获取httpUtil错误", e);
                }
            }
        } while (sendTime < maxSendTime && httpUtil == null);
        httpUtil = new HttpUtil();
        return httpUtil;
        /*throw new GetDateException("HttpUtilPool已满,等待" + waitTime + ",重复发送次数：" + sendTime + ",仍无可用httpUtil。");*/

    }

    public void returnHttpUtil(HttpUtil httpUtil) {
        if (connections.containsKey(httpUtil)) {
            connections.put(httpUtil, Boolean.FALSE);
        }
    }

    private void initializePool(int noProxySize, int initial) throws SQLException, InterruptedException {
        DishonestyService service = new DishonestyService();
        int noProxy = 0;
        for (int i = 0; i < initial; i++) {
            if (noProxy < noProxySize) {
                HttpUtil httpUtil = new HttpUtil();
                connections.put(httpUtil, Boolean.FALSE);
                noProxy++;
            } else {
                HttpUtil httpUtil = new HttpUtil(true, service.getProxy(0));
                connections.put(httpUtil, Boolean.FALSE);
            }
            /*HttpUtil httpUtil = new HttpUtil(true,"tky.jp.v0.ss-fast.com:873");
            connections.put(httpUtil, Boolean.FALSE);*/
        }
    }

    public void getStatus() {
        Enumeration enu = connections.keys();
        int trueNum = 0;
        int falseNum = 0;
        while (enu.hasMoreElements()) {
            HttpUtil httpUtil = (HttpUtil) enu.nextElement();
            if (true == connections.get(httpUtil)) {
                trueNum++;
            } else {
                falseNum++;
            }
        }
        logger.info("当前httpPool中空闲个数：" + trueNum + ",在用个数：" + falseNum);
    }

    public static void main(String[] args) throws SQLException, InterruptedException, GetDateException {
        /*HttpUtilPool hp = new HttpUtilPool(10, 10);
        HttpUtil httputil = hp.getHttpUtil();
        hp.getHttpUtil();
        hp.getStatus();
        hp.returnHttpUtil(httputil);
        hp.getStatus();*/
        Integer a = 1;
        Integer b = 2;
        Integer c = 3;
        Integer d = 3;
        Integer e = 321;
        Integer f = 321;
        Long g = 3L;
        Long h = 2L;

        System.out.println(c == d);  //true
        System.out.println(e == f);  //false
        System.out.println(c == (a + b));//true
        System.out.println(c.equals(a + b)); //true
        System.out.println(g == (a + b));//   true
        System.out.println(g.equals(a + b));// false
        System.out.println(g.equals(a + h)); //ture
    }
}
