/*******************************************************************************
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 ******************************************************************************/

package com.baidu.handler;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URLDecoder;

/**
 * Created with IntelliJ IDEA.
 * User: zhangkl
 * Date: 2016/3/16
 * Time: 17:31
 * https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php?resource_id=6899&query=%E5%A4%B1%E4%BF%A1%E8%A2%AB%E6%89%A7%E8%A1%8C%E4%BA%BA%E5%90%8D%E5%8D%95&cardNum=0001&iname=&areaName=&pn=0&ie=utf-8&oe=utf-8&format=json&t1467788181164&cb=jQuery110204892914363355616_1467787459904&_=1467787459917
 * 百度接口获取失信人数据
 * pn为起始条数，每次查询返回50条数据  当查询结果大于2000条时，百度只返回前2000条数据
 */
public class Dishonesty_Person implements Runnable {
    private static Logger logger = Logger.getLogger("Dishonesty_Person.class");
    public long dataCount = 0;//当前url总个数
    String areaName;
    String cardNum;
    String[] areanamearray = BaiduEnum.AREANAMEARRAY.toArray();

    public Dishonesty_Person(String cardNum, String areaName, int dataCount) {
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
                for (int i = 0; i < areanamearray.length; i++) {
                    json = sfb.getData(cardNum, areanamearray[i]);
                    dataCount = sfb.getAccount(json);
                    System.out.println("info:" + Thread.currentThread().getName() + ":" + "查询条件:" + cardNum + "," + URLDecoder.decode(areanamearray[i], "UTF-8") + ",dataCount:" + dataCount);
                    if (dataCount > 2000) {
                        logger.error(Thread.currentThread().getName() + ":" + "查询条件:" + cardNum + "," + URLDecoder.decode(areanamearray[i], "UTF-8") + ",dataCount:" + dataCount);
                    }
                }
            }
        } catch (IOException e) {
            logger.error(Thread.currentThread().getName() + ":" + "查询条件:" + cardNum + "," + areaName + ",dataCount:" + dataCount, e);
        }
    }

}
