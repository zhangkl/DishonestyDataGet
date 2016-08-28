/*******************************************************************************
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 ******************************************************************************/

package com.baidu;

import com.baidu.handler.Dishonesty_ENT;
import com.baidu.handler.Dishonesty_Person;
import com.baidu.httpUtil.HttpRespons;
import com.baidu.httpUtil.TestHttp;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created with IntelliJ IDEA.
 * User: zhangkl
 * Date: 七月,2016
 */
public class GetData implements ThreadFactory {
    private static Logger logger = Logger.getLogger("GetData.class");
    private static String[] decodeAreaName = {"北京", "天津", "河北", "山西", "内蒙古", "吉林", "黑龙江", "上海", "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南", "湖北", "湖南", "广东", "广西", "海南", "重庆", "四川", "贵州", "云南", "西藏", "陕西", "甘肃", "青海", "宁夏", "新疆", "香港", "澳门", "台湾"};
    public static String[] areaNameArray = {"%E5%8C%97%E4%BA%AC", "%E5%A4%A9%E6%B4%A5", "%E6%B2%B3%E5%8C%97", "%E5%B1%B1%E8%A5%BF", "%E5%86%85%E8%92%99%E5%8F%A4", "%E5%90%89%E6%9E%97", "%E9%BB%91%E9%BE%99%E6%B1%9F", "%E4%B8%8A%E6%B5%B7", "%E6%B1%9F%E8%8B%8F", "%E6%B5%99%E6%B1%9F", "%E5%AE%89%E5%BE%BD", "%E7%A6%8F%E5%BB%BA", "%E6%B1%9F%E8%A5%BF", "%E5%B1%B1%E4%B8%9C", "%E6%B2%B3%E5%8D%97", "%E6%B9%96%E5%8C%97", "%E6%B9%96%E5%8D%97", "%E5%B9%BF%E4%B8%9C", "%E5%B9%BF%E8%A5%BF", "%E6%B5%B7%E5%8D%97", "%E9%87%8D%E5%BA%86", "%E5%9B%9B%E5%B7%9D", "%E8%B4%B5%E5%B7%9E", "%E4%BA%91%E5%8D%97", "%E8%A5%BF%E8%97%8F", "%E9%99%95%E8%A5%BF", "%E7%94%98%E8%82%83", "%E9%9D%92%E6%B5%B7", "%E5%AE%81%E5%A4%8F", "%E6%96%B0%E7%96%86", "%E9%A6%99%E6%B8%AF", "%E6%BE%B3%E9%97%A8", "%E5%8F%B0%E6%B9%BE"};
    public static String[] cardNumArray = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "x"};
    public static final long maxTimestamp = 1458864000000L;//上次更新时间
    private static int sendTimes = 0;
    private static int maxTimes = 5;

    public static void main(String[] args) {
        try {
            ExecutorService excutorService = Executors.newFixedThreadPool(80);
            for (int i = 0; i < 10000; i++) {
                String cardnum = String.valueOf(i);
                while (cardnum.length() < 4) {
                    cardnum = "0" + cardnum;
                }
                Dishonesty_Person testPerson = new Dishonesty_Person(cardnum, "", 0);
                excutorService.execute(testPerson);
            }
            ExecutorService excutorService2 = Executors.newFixedThreadPool(20);
            for (int i = 0; i < cardNumArray.length; i++) {
                for (int j = 0; j < areaNameArray.length; j++) {
                    Dishonesty_ENT dishonesty_ent = new Dishonesty_ENT(cardNumArray[i], areaNameArray[j], 0);
                    excutorService2.execute(dishonesty_ent);
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    public static String getData(String cardNum, String areaName) {
        TestHttp testHttp = new TestHttp();
        testHttp.setDefaultContentEncoding("utf-8");
        Map map = new HashMap();
        map.put("resource_id", "6899");
        map.put("query", "%E5%A4%B1%E4%BF%A1%E8%A2%AB%E6%89%A7%E8%A1%8C%E4%BA%BA%E5%90%8D%E5%8D%95");
        map.put("ie", "utf-8");
        map.put("oe", "utf-8");
        map.put("format", "json");
        map.put("cardNum", cardNum);
        map.put("areaName", areaName);
        String url = "https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php";
        try {
            HttpRespons hr = testHttp.sendGet(url, map, null);
            String jsonString = hr.getContent();
            return jsonString;
        } catch (IOException e) {
            System.out.println("访问错误，cardNum：" + cardNum + ",areaName:" + areaName + ",sendTimes：" + sendTimes++);
            if (sendTimes >= maxTimes) {
                logger.error("访问错误，cardNum：" + cardNum + ",areaName:" + areaName + ",sendTimes：", e);
            } else {
                getData(cardNum, areaName);
            }
        }
        return "";
    }

    public static int getAccount(String json) throws IOException {
        JSONObject jsonObject = JSONObject.fromObject(json);
        JSONArray jsonArray = JSONArray.fromObject(jsonObject.get("data"));
        if (jsonArray.size() > 0) {
            List list = JSONArray.fromObject(jsonArray.get(0));
            JSONObject jsonObject1 = JSONObject.fromObject(list.get(0));
            String dispNumValue = jsonObject1.getString("dispNum");
            return Integer.valueOf(dispNumValue);
        }
        return 0;
    }

    @Override
    public Thread newThread(Runnable r) {
        return newThread(r);
    }
}
