/*******************************************************************************
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 ******************************************************************************/

package com.baidu.handler;

import com.baidu.GetData;
import com.baidu.httpUtil.HttpRespons;
import com.baidu.httpUtil.TestHttp;
import com.dishonest.dao.ConnUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        try {
            String json = GetData.getData(cardNum, areaName);
            int dataCount = GetData.getAccount(json);
            System.out.println("info:" + Thread.currentThread().getName() + ":" + "查询条件:" + cardNum + "," + URLDecoder.decode(areaName, "UTF-8") + ",dataCount:" + dataCount);
            if (dataCount > 2000) {
                logger.error(Thread.currentThread().getName() + ":" + "查询条件:" + cardNum + "," + URLDecoder.decode(areaName, "UTF-8") + ",dataCount:" + dataCount);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void getData(String cardNum, String areaName, int pn) {
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
        map.put("pn", String.valueOf(pn));
        String url = "https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php";
        try {
            HttpRespons hr = testHttp.sendGet(url, map, null);
            String jsonString = hr.getContent();
            json2Model(jsonString, this.pn);
        } catch (IOException e) {
            logger.error("访问错误", e);
        }
        String str = Thread.currentThread().getName() + ":" + "查询条件:" + this.cardNum + "," + this.areaName + ",pn:" + this.pn + ",目前成功插入条数:" + sucessCount + ",已查重复条数:" + sameAccount + ",当前查询条件总条数:" + dataCount;
        logger.info(str);
    }

    public synchronized void json2Model(String json, int pn) throws IOException {
        JSONObject jsonObject = JSONObject.fromObject(json);
        JSONArray jsonArray = JSONArray.fromObject(jsonObject.get("data"));
        if (jsonArray.size() > 0) {
            List list = JSONArray.fromObject(jsonArray.get(0));
            JSONObject jsonObject1 = JSONObject.fromObject(list.get(0));
            String dispNumValue = jsonObject1.getString("dispNum");
            dataCount = Integer.valueOf(dispNumValue);
            if (pn >= 2000 || pn >= dataCount) {
                return;
            }
            Iterator it = jsonObject1.keys();
            JSONObject jsonObject2 = null;
            try {
                while (it.hasNext()) {
                    String key = (String) it.next();
                    if ("result".equals(key)) {
                        JSONArray array = jsonObject1.getJSONArray(key);
                        for (int i = 0; i < array.size(); i++) {
                            StringBuffer sql = new StringBuffer();
                            sql.append("insert into CRED_DISHONESTY_ENT (IID, SSTDSTG, SSTDSTL, DUPDATE_TIME, SLOC, DLASTMOD, SCHANGEFREQ, SPRIORITY, SSITELINK, SINAME, STYPE, SCARDNUM," +
                                    " SCASECODE, IAGE, SSEXY, SFOCUSNUMBER, SAREANAME, SBUSINESSENTITY, SCOURTNAME, SDUTY, SPERFORMANCE, SDISRUPTTYPENAME, DPUBLISHDATE, " +
                                    "SPARTYTYPENAME, SGISTID, DREGDATE, SGISTUNIT, SPERFORMEDPART, SUNPERFORMPART, SPUBLISHDATESTAMP, SSITEID) values (");
                            jsonObject2 = JSONObject.fromObject(array.get(i));
                            int iid = getSeqNextVal("SEQ_CRED_DISHONESTY_ENT");
                            sql.append(iid + ",");  //iid
                            sql.append("'" + jsonObject2.getString("StdStg") + "'" + ",");
                            sql.append("'" + jsonObject2.getString("StdStl") + "'" + ",");
                            String _update_time = jsonObject2.getString("_update_time");
                            while (_update_time.length() < 13) {
                                _update_time += "0";
                            }
                            Timestamp timestamp = new Timestamp(Long.valueOf(_update_time));
                            sql.append("to_timestamp('" + timestamp + "', 'yyyy-mm-dd hh24:mi:ss:ff')" + ",");
                            sql.append("'" + jsonObject2.getString("loc") + "'" + ",");
                            String sloc = jsonObject2.getString("loc");
                            String querySql = "select * from CRED_DISHONESTY_ENT where sloc = '" + sloc + "'";
                            ResultSet resultSet = statement.executeQuery(querySql);
                            if (resultSet != null && resultSet.next()) {
                                /*if (sameAccount == 5) {
                                    isOver = true;
                                    return;
                                }*/
                                sameAccount++;
                                continue;
                            }
                            StringBuffer lastmod = new StringBuffer(jsonObject2.getString("lastmod"));
                            lastmod.replace(10, 11, " ");
                            sql.append("to_date('" + lastmod + "','yyyy-mm-dd hh24:mi:ss'),");
                            sql.append("'" + jsonObject2.getString("changefreq") + "'" + ",");
                            sql.append("'" + jsonObject2.getString("priority") + "'" + ",");
                            sql.append("'" + jsonObject2.getString("sitelink") + "'" + ",");
                            sql.append("'" + jsonObject2.getString("iname") + "'" + ",");
                            sql.append("'" + jsonObject2.getString("type") + "'" + ",");
                            sql.append("'" + jsonObject2.getString("cardNum").replace("****", "---") + "'" + ",");
                            sql.append("'" + jsonObject2.getString("caseCode") + "'" + ",");
                            int age = Integer.valueOf(jsonObject2.getString("age"));
                            sql.append(age + ",");
                            sql.append("'" + jsonObject2.getString("sexy") + "'" + ",");
                            sql.append("'" + jsonObject2.getString("focusNumber") + "'" + ",");
                            sql.append("'" + jsonObject2.getString("areaName") + "'" + ",");
                            sql.append("'" + jsonObject2.getString("businessEntity") + "'" + ",");
                            sql.append("'" + jsonObject2.getString("courtName") + "'" + ",");
                            sql.append("'" + jsonObject2.getString("duty").replaceAll("'", "").replaceAll("&", "") + "'" + ",");
                            sql.append("'" + jsonObject2.getString("performance") + "'" + ",");
                            sql.append("'" + jsonObject2.getString("disruptTypeName") + "'" + ",");
                            String publishDate = jsonObject2.getString("publishDate");
                            publishDate = publishDate.replaceAll("[^0-9]", "-").substring(0, publishDate.length() - 1);
                            sql.append("to_date('" + publishDate + "','yyyy-mm-dd hh24:mi:ss'),");
                            sql.append("'" + jsonObject2.getString("partyTypeName") + "'" + ",");
                            sql.append("'" + jsonObject2.getString("gistId") + "'" + ",");
                            String regDate = jsonObject2.getString("regDate");
                            String year = regDate.substring(0, 4);
                            String month = regDate.substring(4, 6);
                            String day = regDate.substring(6, 8);
                            regDate = year + "-" + month + "-" + day;
                            sql.append("to_date('" + regDate + "','yyyy-mm-dd hh24:mi:ss'),");
                            sql.append("'" + jsonObject2.getString("gistUnit") + "'" + ",");
                            sql.append("'" + jsonObject2.getString("performedPart") + "'" + ",");
                            sql.append("'" + jsonObject2.getString("unperformPart") + "'" + ",");
                            String publishDateStamp = jsonObject2.getString("publishDateStamp");
                            while (publishDateStamp.length() < 13) {
                                publishDateStamp += "0";
                            }
                            Timestamp timestamp2 = new Timestamp(Long.valueOf(publishDateStamp));
                            sql.append("to_timestamp('" + timestamp2 + "', 'yyyy-mm-dd hh24:mi:ss:ff')" + ",");
                            sql.append("'" + jsonObject2.getString("SiteId") + "'");
                            sql.append(")");
                            statement.execute(sql.toString());
                            sucessCount++;
                        }
                        /*try {
                            int[] suc = statement.executeBatch();
                            for (int i = 0; i < suc.length; i++) {
                                sucessCount += suc[i];
                            }
                        } catch (Exception e) {
                            logger.error(Thread.currentThread().getName() + ":" + statement.getWarnings(), e);
                        }*/
                    }
                }

            } catch (Exception e) {
                logger.error(Thread.currentThread().getName() + jsonObject2.toString(), e);
            }
        }
    }

    private synchronized int getSeqNextVal(String seqName) throws SQLException {
        ResultSet newrs;
        int id = 0;
        try {
            newrs = statement2.executeQuery("select " + seqName + ".nextval as id from dual");
            if (newrs.next()) {
                id = newrs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("", e);
        }
        return id;
    }
}
