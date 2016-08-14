
/*******************************************************************************
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 ******************************************************************************/

package com.dishonest.util;

import com.dishonest.handler.DishonestyService;
import org.apache.http.HttpException;
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

    public HttpUtilPool(int myProxyNum, int initial) throws SQLException, InterruptedException {
        DishonestyService ds = new DishonestyService();
        for (int i = 0; i < initial; i++) {
             HttpUtil httpUtil = new HttpUtil(true,ds.getProxy(0));
            connections.put(httpUtil,false);
        }
    }

    public HttpUtilPool(int proxyNum) throws SQLException, InterruptedException {
        for (int i = 0; i < proxyNum; i++) {
            HttpUtil httpUtil = new HttpUtil();
            connections.put(httpUtil,false);
        }
    }

    public HttpUtilPool(int proxyNum,String proxyUrl) throws SQLException, InterruptedException {
        DishonestyService ds = new DishonestyService();
        for (int i = 0; i < proxyNum; i++) {
            HttpUtil httpUtil = new HttpUtil(true,proxyUrl);
            connections.put(httpUtil,false);
        }
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

        noProxySize = initial/7;
        for (int i = 0; i < noProxySize; i++) {
            HttpUtil httpUtil = new HttpUtil();
            connections.put(httpUtil, Boolean.FALSE);
        }
        for (int i = 0; i < noProxySize; i++) {
            for (int j = 0; j < 6; j++) {
                HttpUtil httpUtil = new HttpUtil(true, "127.0.0.1:108" + j);
                connections.put(httpUtil, Boolean.FALSE);
            }
        }
    }

    public void getStatus() {
        Enumeration enu = connections.keys();
        int trueNum = 0;
        int falseNum = 0;
        while (enu.hasMoreElements()) {
            HttpUtil httpUtil = (HttpUtil) enu.nextElement();
            boolean flag = (Boolean) connections.get(httpUtil);
            if (flag) {
                trueNum++;
            } else {
                falseNum++;
            }
        }
        logger.info("当前httpPool中空闲个数：" + falseNum + ",在用个数：" + trueNum);
    }

    public static void main(String[] args) throws HttpException, SQLException, GetDateException, InterruptedException {
        Object i = 1;
        System.out.println(String.valueOf(i));
    }
}
