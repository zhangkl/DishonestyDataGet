/*******************************************************************************
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 ******************************************************************************/

package com.baidu;

import com.baidu.handler.BaiduEnum;
import com.baidu.handler.Dishonesty_ENT;
import com.baidu.handler.Dishonesty_Person;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: zhangkl
 * Date: 七月,2016
 */
public class GetData {
    private static Logger logger = Logger.getLogger("GetData.class");

    public static void main(String[] args) {
        try {
            ExecutorService excutorService = Executors.newFixedThreadPool(50);
            for (int i = 0; i < 10000; i++) {
                String cardnum = String.valueOf(i);
                while (cardnum.length() < 4) {
                    cardnum = "0" + cardnum;
                }
                Dishonesty_Person testPerson = new Dishonesty_Person(cardnum, "", 0);
                excutorService.execute(testPerson);
            }
            ExecutorService excutorService2 = Executors.newFixedThreadPool(20);
            for (int i = 0; i < BaiduEnum.CARDNUMARRAY.toArray().length; i++) {
                for (int j = 0; j < BaiduEnum.AREANAMEARRAY.toArray().length; j++) {
                    Dishonesty_ENT dishonesty_ent = new Dishonesty_ENT(BaiduEnum.CARDNUMARRAY.toArray()[i], BaiduEnum.AREANAMEARRAY.toArray()[j], 0);
                    excutorService2.execute(dishonesty_ent);
                }
            }
            excutorService.shutdown();
            excutorService2.shutdown();
        } catch (Exception e) {
            logger.error("", e);
        }
    }


}
