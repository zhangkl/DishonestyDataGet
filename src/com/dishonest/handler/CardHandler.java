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
import com.thread.MyFixedThreadPool;

import java.sql.SQLException;
import java.util.concurrent.Executor;

/**
 * Created with IntelliJ IDEA.
 * User: chq
 * Date: 16-7-11
 * Time: 下午3:44
 * To change this template use File | Settings | File Templates.
 */
public class CardHandler implements Runnable {
    ConnUtil connUtil;
    String cardNum;
    int endPageNum;
    int startPageNum;
    int sucessNum;
    int sameNum;
    String code;
    HttpUtil httpUtil;
    String hostName;
    int pagePoolSize = 0;

    public CardHandler(String code, String cardNum, int startPageNum, int endPageNum, HttpUtil httpUtil, int sucessNum, int sameNum, String hostName, int pagePoolSize) {
        this.code = code;
        this.cardNum = cardNum;
        this.startPageNum = startPageNum;
        this.endPageNum = endPageNum;
        this.httpUtil = httpUtil;
        this.sucessNum = sucessNum;
        this.sameNum = sameNum;
        this.hostName = hostName;
        this.pagePoolSize = pagePoolSize;
    }

    @Override
    public void run() {
        Executor executor = new MyFixedThreadPool(pagePoolSize);
        for (int i = startPageNum; i <= endPageNum; i++) {
            PageHandler pageHandler = new PageHandler(connUtil,httpUtil,code,cardNum,i+"",hostName,0,0);
            executor.execute(pageHandler);
        }
        String logSql = "update cred_dishonesty_log set result = '1',dcurrentdate = sysdate where cardnum = '" + cardNum + "'";
        try {
            connUtil.executeSaveOrUpdate(logSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
