/*******************************************************************************
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 ******************************************************************************/

package com.dishonest;

import com.dishonest.dao.TestConn;
import com.dishonest.util.CheckNumber;
import com.dishonest.util.DateUtil;
import com.dishonest.util.HttpUtil;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: chq
 * Date: 16-7-11
 * Time: 下午3:44
 * To change this template use File | Settings | File Templates.
 */
public class TestNum implements Runnable {
    TestConn testConn = TestConn.getInstance();
    String cardNum;
    int endPageNum;
    int stratPageNum;
    int sucessNum;
    int sameNum;
    String code;
    HttpUtil httpUtil;

    int sendTime = 0;
    int maxTime = 5;
    String hostName;

    public TestNum(String code, String cardNum, int stratPageNum, int endPageNum, HttpUtil httpUtil, int sucessNum, int sameNum,String hostName) {
        this.code = code;
        this.cardNum = cardNum;
        this.stratPageNum = stratPageNum;
        this.endPageNum = endPageNum;
        this.httpUtil = httpUtil;
        this.sucessNum = sucessNum;
        this.sameNum = sameNum;
        this.hostName = hostName;
    }

    public static String getImageCode(HttpUtil httpUtil) throws IOException, InterruptedException {
//        long startTime = System.currentTimeMillis();
        byte[] result = httpUtil.doGetByte("http://shixin.court.gov.cn/image.jsp?date=" + System.currentTimeMillis(), null);
        ByteInputStream bin = new ByteInputStream();
        if (result == null) {
            getImageCode(httpUtil);
        }
        bin.setBuf(result);
        BufferedImage image = ImageIO.read(bin);
        String code = CheckNumber.getCheckNumber(image);
//        Long codeTime = System.currentTimeMillis();
//        System.out.println(DateUtil.getNowDateTime() + ":" + Thread.currentThread().getName() + ":获取验证码时间：" + (codeTime - startTime) / 1000 + "s,code:" + code + ",代理地址：" + httpUtil.getProxyURL());
        return code;
    }

    public static int getPageAccount(String cons) {
        int start = cons.indexOf("<input onclick=\"jumpTo()\" value=\"到\" type=\"button\" /> <input id=\"pagenum\" name=\"pagenum\" maxlength=\"6\" value=\"\" size=\"4\" type=\"text\" /> 页");
        int length = "<input onclick=\"jumpTo()\" value=\"到\" type=\"button\" /> <input id=\"pagenum\" name=\"pagenum\" maxlength=\"6\" value=\"\" size=\"4\" type=\"text\" /> 页".length();
        int end = cons.indexOf("条\n" +
                "\t\t</div>");
        int page = Integer.parseInt(cons.substring(start + length, end).split("/")[1].split(" ")[0]);
        return page;
    }

    public static List<String> getIDList(String cons) throws ParserException {
        ArrayList list = new ArrayList();
        Parser e = new Parser();
        e.setInputHTML(cons);
        e.setEncoding("utf-8");
        NodeFilter filter1 = new HasAttributeFilter("class", "View");
        NodeFilter filter2 = new TagNameFilter("a");
        AndFilter contentFilter = new AndFilter(filter1, filter2);
        NodeList nodes2 = e.extractAllNodesThatMatch(contentFilter);
        for (int i = 0; i < nodes2.size(); ++i) {
            LinkTag linkTag = (LinkTag) nodes2.elementAt(i);
            list.add(linkTag.getAttribute("id"));
        }

        return list;
    }

    public static List<String> getCardNum() {
        List list = new ArrayList();
        for (int i = 1; i <= 12; i++) {
            String mounth = String.valueOf(i);
            while (mounth.length() < 2) {
                mounth = "0" + mounth;
            }
            int maxDay;
            if (i == 2) {
                maxDay = 29;
            } else if (i == 4 || i == 6 || i == 9 || i == 11) {
                maxDay = 30;
            } else {
                maxDay = 31;
            }
            for (int j = 1; j <= maxDay; j++) {
                String day = String.valueOf(j);
                while (day.length() < 2) {
                    day = "0" + day;
                }
                String cardNum = mounth + day;
                list.add(cardNum);
            }
        }
        return list;
    }

    public static String getProxy(int istatus) throws SQLException {
        String proxyUrl;
        Map map = TestConn.getInstance().executeQueryForMap("select * from cred_dishonesty_proxy where isusered = '" + istatus + "'");
        if (map == null) {
            System.out.println("***********************************************************");
            System.out.println("********************代理已用光,重置所有代理状态为可用****************************");
            System.out.println("***********************************************************");
            TestConn.getInstance().executeSaveOrUpdate("update cred_dishonesty_proxy set isusered = 0 where isusered = 1");
            getProxy(istatus);
        }
        proxyUrl = (String) map.get("PROXYURL");
        TestConn.getInstance().executeSaveOrUpdate("update cred_dishonesty_proxy set isusered = 1 where proxyurl = '" + proxyUrl + "'");
        return proxyUrl;
    }

    @Override
    public void run() {
        System.out.println(DateUtil.getNowDateTime() + ":" + Thread.currentThread().getName() + "开始执行," + cardNum + ",时间：" + DateUtil.getNowDateTime() + ",sameNum:" + sameNum + ",sucess:" + sucessNum + ",代理地址：" + httpUtil.getProxyURL());
        try {
            for (int i = stratPageNum; i <= endPageNum; i++) {
                worker(i + "");
            }
            String logSql = "update cred_dishonesty_log set result = '1',dcurrentdate = sysdate where cardnum = '" + cardNum + "'";
            testConn.executeSaveOrUpdate(logSql);
        } catch (ClassCastException e) {
            try {
                if ("null:0".equals(httpUtil.getProxyURL())) {
                    System.out.println(DateUtil.getNowDateTime() + ":" + Thread.currentThread().getName() + ":重复访问出错,更换前代理:" + httpUtil.getProxyURL());
                    Thread.sleep(1000 * 60 * 30);
                    System.out.println(DateUtil.getNowDateTime() + ":" + Thread.currentThread().getName() + ":重复访问出错,更换后代理:" + httpUtil.getProxyURL());
                } else {
                    System.out.println(DateUtil.getNowDateTime() + ":" + Thread.currentThread().getName() + ":重复访问出错,更换前代理:" + httpUtil.getProxyURL());
                    Thread.sleep(1000 * 60 * 1);
                    changeProxy();
                    System.out.println(DateUtil.getNowDateTime() + ":" + Thread.currentThread().getName() + ":重复访问出错,更换后代理:" + httpUtil.getProxyURL());
                }
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } catch (JSONException jsonException) {
            try {
                if ("null:0".equals(httpUtil.getProxyURL())) {
                    System.out.println(DateUtil.getNowDateTime() + ":" + Thread.currentThread().getName() + ":重复访问出错,更换前代理:" + httpUtil.getProxyURL());
                    Thread.sleep(1000 * 60 * 30);
                    System.out.println(DateUtil.getNowDateTime() + ":" + Thread.currentThread().getName() + ":重复访问出错,更换后代理:" + httpUtil.getProxyURL());
                } else {
                    System.out.println(DateUtil.getNowDateTime() + ":" + Thread.currentThread().getName() + ":重复访问出错,更换前代理:" + httpUtil.getProxyURL());
                    Thread.sleep(1000 * 60 * 1);
                    changeProxy();
                    System.out.println(DateUtil.getNowDateTime() + ":" + Thread.currentThread().getName() + ":重复访问出错,更换后代理:" + httpUtil.getProxyURL());
                }
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeProxy() throws SQLException {
        String currentProxy = httpUtil.getProxyURL();
        testConn.executeSaveOrUpdate("update cred_dishonesty_proxy set isusered = 2 where proxyurl = '" + currentProxy + "'");
        httpUtil.setProxyURL(getProxy(0));
    }

    public void worker(String pageNum) throws InterruptedException, IOException, ParserException, SQLException {
        if ("".equals(code)) {
            code = getImageCode(httpUtil);
        }
        List arrayList = getPageList(pageNum);
        for (int i = 0; i < arrayList.size(); i++) {
            saveDishoney(arrayList.get(i).toString());
        }
        String logStr = DateUtil.getNowDateTime() + ":" + Thread.currentThread().getName() + ":查询条件：" + cardNum + ",当前页数：" + pageNum + "，总重复个数" + sameNum + ",总成功个数：" + sucessNum + ",查询入库完成。" + ",代理地址：" + httpUtil.getProxyURL();
        System.out.println(logStr);
        String logSql = "update cred_dishonesty_log set samenum = '" + sameNum + "',sucessnum='" + sucessNum + "', remark='" + logStr + "',startpage='" + pageNum + "',hostname='" + hostName + "',dcurrentdate = sysdate where cardnum = '" + cardNum + "'";
        testConn.executeSaveOrUpdate(logSql);
    }

    /**
     * 访问错误，重复发起，增加最大访问次数控制
     */
    public List getPageList(String pageNum) throws ParserException, IOException, InterruptedException {
        sendTime = 0;
        String s;
        List arrayList = null;
        do {
            s = httpUtil.doPostString("http://shixin.court.gov.cn/findd",
                    "pName", "__", "pCardNum", "__________" + cardNum + "____", "pProvince", "0", "currentPage", pageNum, "pCode", code);
            sendTime++;
            if (s == null || "".equals(s) || s.contains("验证码错误")) {
                Thread.currentThread().sleep(1000 * 60 * 1);
                System.out.println(DateUtil.getNowDateTime() + ":" + Thread.currentThread().getName() + ":线程休眠1分钟，发送次数：" + sendTime + ",cardNum:" + cardNum + ",pageNum:" + pageNum + ",code:" + code + ",arrayList:" + arrayList + ",s:" + (s == null || "".equals(s) || s.contains("验证码错误")) + ",代理地址：" + httpUtil.getProxyURL());
                code = getImageCode(httpUtil);
                s = httpUtil.doPostString("http://shixin.court.gov.cn/findd",
                        "pName", "__", "pCardNum", "__________" + cardNum + "____", "pProvince", "0", "currentPage", pageNum, "pCode", code);
                sendTime++;
            }
            arrayList = getIDList(s);
        } while (sendTime <= maxTime && (arrayList == null || arrayList.size() == 0 || s.contains("验证码错误")));
        return arrayList;
    }

    /**
     * 获取并存储具体失信人信息
     */
    public void saveDishoney(String saveid) throws InterruptedException, IOException {
        String queryIdSql = "select * from CRED_DISHONESTY_PERSON where iid = '" + saveid + "'";
        List resultlist = null;
        try {
            resultlist = testConn.executeQueryForList(queryIdSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (resultlist != null && resultlist.size() > 0) {
            sameNum++;
            return;
        }
        sendTime = 0;
        String idInfo = "";
        Map map = new HashMap();
        do {
            map.put("id", saveid);
            map.put("pCode", code);
            idInfo = httpUtil.doGetString("http://shixin.court.gov.cn/findDetai", map);
            sendTime++;
            if (idInfo == null || idInfo.contains("验证码错误") || !idInfo.startsWith("{")) {
                Thread.currentThread().sleep(1000 * 60 * 1);
                System.out.println(DateUtil.getNowDateTime() + ":" + Thread.currentThread().getName() + ":发送次数：" + sendTime + ",map:" + map + ",idInfo:" + idInfo + ",代理地址：" + httpUtil.getProxyURL());
                code = getImageCode(httpUtil);
                map.put("pCode", code);
                idInfo = httpUtil.doGetString("http://shixin.court.gov.cn/findDetai", map);
                sendTime++;
            }
        } while (sendTime <= maxTime && (idInfo == null || idInfo.contains("验证码错误") || !idInfo.startsWith("{")));
        JSONObject json = null;
        try{
            json = JSONObject.fromObject(idInfo);
        } catch (JSONException jsonE){
            System.out.println(idInfo);
            throw jsonE;
        }
        Integer iid = json.optInt("id");
        String siname = json.optString("iname");
        String scardnum = json.optString("cardNum");
        String scasecode = json.optString("caseCode");
        String sage = json.optString("age", "0");
        Integer iage = Integer.valueOf(sage);
        String ssexy = json.optString("sexy");
        String sareaname = json.optString("areaName");
        String scourtname = json.optString("courtName");
        String sduty = json.optString("duty");
        String sperformance = json.optString("performance");
        String sperformedpart = json.optString("performedpart");
        String sunperformpart = json.optString("unperformpart");
        String sdisrupttypename = json.optString("disruptTypeName");
        String spublishdate = json.optString("publishDate");
        java.sql.Date dpublishdate = DateUtil.StringToDate2(spublishdate);
        String spartytypename = json.optString("partyTypeName");
        String sgistid = json.optString("gistId");
        String sregdate = json.optString("regDate");
        java.sql.Date dregdate = DateUtil.StringToDate2(sregdate);
        String sgistunit = json.optString("gistUnit");
        List list = new ArrayList();
        list.add(iid);
        list.add(siname);
        list.add(scardnum.replace("****", cardNum));
        list.add(scasecode);
        list.add(iage);
        list.add(ssexy);
        list.add(sareaname);
        list.add(scourtname);
        list.add(dregdate);
        StringReader reader = new StringReader(sduty);
        list.add(reader);
        list.add(sperformance);
        list.add(sperformedpart);
        list.add(sunperformpart);
        list.add(sdisrupttypename);
        list.add(dpublishdate);
        list.add(spartytypename);
        list.add(sgistid);
        list.add(sgistunit);
        String sql = "insert into CRED_DISHONESTY_PERSON (IID, SINAME, SCARDNUM, SCASECODE, IAGE, SSEXY, SAREANAME, SCOURTNAME, DREGDATE," +
                " SDUTY, SPERFORMANCE, SPERFORMEDPART, SUNPERFORMPART, SDISRUPTTYPENAME, DPUBLISHDATE, SPARTYTYPENAME, SGISTID, SGISTUNIT) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            testConn.psAdd(sql, list);
        } catch (SQLException e) {
            System.out.println(DateUtil.getNowDateTime() + ":" + Thread.currentThread().getName() + saveid + ":" + idInfo);
            e.printStackTrace();
        }
        sucessNum++;
    }

}
