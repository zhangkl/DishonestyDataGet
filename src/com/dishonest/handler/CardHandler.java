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
    String cardNum;
    int endPageNum;
    int startPageNum;
    int sucessNum;
    int sameNum;
    String code;
    String hostName;
    HttpUtil httpUtil;

    public CardHandler(HttpUtil httpUtil,String code, String cardNum, int startPageNum, int endPageNum, int sucessNum, int sameNum, String hostName) {
        this.code = code;
        this.cardNum = cardNum;
        this.startPageNum = startPageNum;
        this.endPageNum = endPageNum;
        this.sucessNum = sucessNum;
        this.sameNum = sameNum;
        this.hostName = hostName;
        this.httpUtil = httpUtil;
    }

    @Override
    public void run() {


        PageHandler pageHandler = new PageHandler(httpUtil,code,cardNum,hostName,sameNum,sucessNum);
        for (int i = startPageNum; i <= endPageNum; i++) {
            pageHandler.work(i+"");
        }
        String logSql = "update cred_dishonesty_log set result = '1',dcurrentdate = sysdate where cardnum = '" + cardNum + "'";
        try {
            ConnUtil.getInstance().executeSaveOrUpdate(logSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
