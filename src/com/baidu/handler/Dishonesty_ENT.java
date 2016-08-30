/*******************************************************************************
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 ******************************************************************************/

package com.baidu.handler;


import com.fayuan.dao.ConnUtil;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.Statement;

/**
 * Created with IntelliJ IDEA.
 * User: zhangkl
 * Date: 2016/3/16
 * Time: 17:31
 * To change this template use File | Settings | File Templates.
 */
public class Dishonesty_ENT implements Runnable {
    private static Logger logger = Logger.getLogger("Test_ENT.class");
    private static int sucessCount;
    private static long sameAccount;
    ConnUtil connUtil;
    String cardNum;
    String areaName;
    int pn;
    Statement statement;
    Statement statement2;
    private int dataCount = 0;

    public Dishonesty_ENT(ConnUtil connUtil, Statement statement, Statement statement2, String cardNum, String areaName, int pn, int dataCount, int sucessCount) {
        this.connUtil = connUtil;
        this.cardNum = cardNum;
        this.areaName = areaName;
        this.pn = pn;
        this.dataCount = dataCount;
        this.statement = statement;
        this.statement2 = statement2;
        Dishonesty_ENT.sucessCount = sucessCount;
    }

    public Dishonesty_ENT(String cardNum, String areaName, int dataCount) {
        this.cardNum = cardNum;
        this.areaName = areaName;
        this.dataCount = dataCount;
    }

    @Override
    public void run() {
        ServiceForBaidu sfb = new ServiceForBaidu();
        try {
            String json = sfb.getData(cardNum, areaName);
            int dataCount = sfb.getAccount(json);
            System.out.println("info:" + Thread.currentThread().getName() + ":" + "查询条件:" + cardNum + "," + URLDecoder.decode(areaName, "UTF-8") + ",dataCount:" + dataCount);
            if (dataCount > 2000) {
                logger.error(Thread.currentThread().getName() + ":" + "查询条件:" + cardNum + "," + URLDecoder.decode(areaName, "UTF-8") + ",dataCount:" + dataCount);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
