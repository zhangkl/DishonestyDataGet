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
 * https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php?resource_id=6899&query=%E5%A4%B1%E4%BF%A1%E8%A2%AB%E6%89%A7%E8%A1%8C%E4%BA%BA%E5%90%8D%E5%8D%95&cardNum=0001&iname=&areaName=&pn=0&ie=utf-8&oe=utf-8&format=json&t1467788181164&cb=jQuery110204892914363355616_1467787459904&_=1467787459917
 * 百度接口获取失信人数据
 * pn为起始条数，每次查询返回50条数据  当查询结果大于2000条时，百度只返回前2000条数据
 */
public class Dishonesty_Person implements Runnable {
    static ConnUtil connUtil;
    static Statement statement; //数据库链接
    static Statement statement2;//获取序列用
    private static Logger logger = Logger.getLogger("Test_Person.class");
    public int sucessCount = 0;//各线程成功个数统计
    public long dataCount = 0;//当前url个数总计
    public long sameAccount = 0;//当前url个数总计
    public int pn;
    int start;
    int end;
    boolean isOver;//当前查询条件是否已经更新完成
    String areaName;
    String cardNum;

    public Dishonesty_Person(int sucessCount, long dataCount, ConnUtil connUtil, Statement statement, Statement statement2, int start, int end, int pn, boolean isOver, int sameAccount) {
        this.sucessCount = sucessCount;
        this.dataCount = dataCount;
        Dishonesty_Person.connUtil = connUtil;
        Dishonesty_Person.statement = statement;
        Dishonesty_Person.statement2 = statement2;
        this.start = start;
        this.end = end;
        this.pn = pn;
        this.isOver = isOver;
        this.sameAccount = sameAccount;
    }

    public Dishonesty_Person(String cardNum, String areaName, int dataCount) {
        this.cardNum = cardNum;
        this.areaName = areaName;
        this.dataCount = dataCount;
    }

    private static synchronized int getSeqNextVal(String seqName) throws SQLException {
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

    @Override
    public void run() {
        try {
            String json = GetData.getData(cardNum, areaName);
            int dataCount = GetData.getAccount(json);
            System.out.println("info:" + Thread.currentThread().getName() + ":" + "查询条件:" + cardNum + "," + URLDecoder.decode(areaName, "UTF-8") + ",dataCount:" + dataCount);
            if (dataCount > 2000) {
                for (int i = 0; i < GetData.areaNameArray.length; i++) {
                    json = GetData.getData(cardNum, GetData.areaNameArray[i]);
                    dataCount = GetData.getAccount(json);
                    System.out.println("info:" + Thread.currentThread().getName() + ":" + "查询条件:" + cardNum + "," + URLDecoder.decode(GetData.areaNameArray[i], "UTF-8") + ",dataCount:" + dataCount);
                    if (dataCount > 2000) {
                        logger.error(Thread.currentThread().getName() + ":" + "查询条件:" + cardNum + "," + URLDecoder.decode(areaName, "UTF-8") + ",dataCount:" + dataCount);
                    }
                }
            }
        } catch (IOException e) {
            logger.error(Thread.currentThread().getName() + ":" + "查询条件:" + cardNum + "," + areaName + ",dataCount:" + dataCount, e);
        }
    }

    public void getData(int start, int end) {
        try {
            TestHttp testHttp = new TestHttp();
            testHttp.setDefaultContentEncoding("utf-8");
            Map map = new HashMap();
            String url;
            map.put("resource_id", "6899");
            map.put("query", "%E5%A4%B1%E4%BF%A1%E8%A2%AB%E6%89%A7%E8%A1%8C%E4%BA%BA%E5%90%8D%E5%8D%95");
            map.put("ie", "utf-8");
            map.put("oe", "utf-8");
            map.put("format", "json");
            for (int i = start; i < end; i++) {
                pn = 0;
                isOver = false;
                String cardNum = String.valueOf(i);
                while (cardNum.length() < 4) {
                    cardNum = "0" + cardNum;
                }
                map.put("cardNum", cardNum);
                do {
                    map.put("pn", String.valueOf(pn));
                    url = "https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php";
                    HttpRespons hr = testHttp.send(url, "GET", map, null);
                    String jsonString = hr.getContent();
                    json2Model(jsonString);
                    /**
                     * 数据大于两千，按所在省份过滤
                     */
                    if (dataCount >= 2000) {
                        for (int j = 0; j < GetData.areaNameArray.length; j++) {
                            int areaPn = 0;
                            isOver = false;
                            map.put("areaName", GetData.areaNameArray[j]);
                            do {
                                map.put("pn", String.valueOf(areaPn));
                                url = "https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php";
                                hr = testHttp.send(url, "GET", map, null);
                                jsonString = hr.getContent();
                                json2Model(jsonString);
                                String str = Thread.currentThread().getName() + ":" + "查询条件:" + cardNum + "," + GetData.areaNameArray[j] + ",pn:" + areaPn + ",目前成功插入条数:" + sucessCount + ",当前查询条件总条数:" + dataCount;
                                logger.info(str);
                                areaPn += 50;
                            } while ((!isOver) && areaPn <= dataCount);
                        }
                        isOver = true;
                    }
                    String str = Thread.currentThread().getName() + ":" + "查询条件:" + cardNum + "," + ",pn:" + pn + ",目前成功插入条数:" + sucessCount + ",当前查询条件总条数:" + dataCount;
                    logger.info(str);
                    pn += 50;
                } while ((!isOver) && pn <= dataCount);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    public void json2Model(String json) throws IOException {
        JSONObject jsonObject = JSONObject.fromObject(json);
        String errorSql = "";
        JSONArray jsonArray = JSONArray.fromObject(jsonObject.get("data"));
        if (jsonArray.size() > 0) {
            List list = JSONArray.fromObject(jsonArray.get(0));
            JSONObject jsonObject1 = JSONObject.fromObject(list.get(0));
            String dispNumValue = jsonObject1.getString("dispNum");
            dataCount = Integer.valueOf(dispNumValue);
            if (dataCount >= 2000) {
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
                            sql.append("insert into CRED_DISHONESTY (IID, SSTDSTG, SSTDSTL, DUPDATE_TIME, SLOC, DLASTMOD, SCHANGEFREQ, SPRIORITY, SSITELINK, SINAME, STYPE, SCARDNUM," +
                                    " SCASECODE, IAGE, SSEXY, SFOCUSNUMBER, SAREANAME, SBUSINESSENTITY, SCOURTNAME, SDUTY, SPERFORMANCE, SDISRUPTTYPENAME, DPUBLISHDATE, " +
                                    "SPARTYTYPENAME, SGISTID, DREGDATE, SGISTUNIT, SPERFORMEDPART, SUNPERFORMPART, SPUBLISHDATESTAMP, SSITEID) values (");
                            jsonObject2 = JSONObject.fromObject(array.get(i));
                            int iid = getSeqNextVal("SEQ_CRED_DISHONESTY");
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
                            String querySql = "select * from CRED_DISHONESTY where sloc = '" + sloc + "'";
                            ResultSet resultSet = statement.executeQuery(querySql);
                            if (resultSet != null && resultSet.next()) {
                                System.out.println("数据已存在，不存储！");
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
                            /*if (Long.valueOf(publishDateStamp)<=maxTimestamp){
                                isOver = true;
                                break;
                            }else{*/
                            Timestamp timestamp2 = new Timestamp(Long.valueOf(publishDateStamp));
                            sql.append("to_timestamp('" + timestamp2 + "', 'yyyy-mm-dd hh24:mi:ss:ff')" + ",");
                            sql.append("'" + jsonObject2.getString("SiteId") + "'");
                            sql.append(")");
                            errorSql = sql.toString();
                            statement.execute(errorSql);
                            sucessCount++;
//                            }
                        }
                        /*int[] suc = statement.executeBatch();
                        for (int i = 0; i < suc.length; i++) {
                            sucessCount += suc[i];
                        }*/
                    }
                }
            } catch (Exception e) {
                logger.error(Thread.currentThread().getName() + ":" + errorSql, e);
            }
        }

    }
}
