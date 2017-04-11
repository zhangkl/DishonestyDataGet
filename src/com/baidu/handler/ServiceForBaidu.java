package com.baidu.handler;

import com.fayuan.dao.ConnUtil;
import com.fayuan.util.HttpUtil;
import com.fayuan.util.NetWorkException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: zhangkl
 * Date: 八月,2016
 */
public class ServiceForBaidu {
    static Logger logger = Logger.getLogger(ServiceForBaidu.class);
    int dataCount;
    int sameAccount;
    int sucessCount;


    /**
     * 根据seq名字获取序列
     * @param seqName
     * @return
     * @throws SQLException
     */
    public static synchronized int getSeqNextVal(String seqName) throws SQLException {
        Map newrs;
        int id = 0;
        try {
            newrs = ConnUtil.getInstance().executeQueryForMap("select " + seqName + ".nextval as id from dual");
            if (newrs != null) {
                id = (Integer) newrs.get("ID");
            }
        } catch (SQLException e) {
            logger.error("", e);
        }
        return id;
    }

    /**
     * 获取总条数
     * @param json
     * @return
     * @throws IOException
     */
    public int getAccount(String json) throws IOException {
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

    /**
     * 按指定参数直接请求百度链接，获取数据
     * @param cardNum
     * @param areaName
     * @param pn
     * @return
     */
    public String getData(String cardNum, String areaName,String pn) {
        Map map = new HashMap();
        map.put("resource_id", "6899");
        map.put("query", "%E5%A4%B1%E4%BF%A1%E8%A2%AB%E6%89%A7%E8%A1%8C%E4%BA%BA%E5%90%8D%E5%8D%95");
        map.put("ie", "utf-8");
        map.put("oe", "utf-8");
        map.put("format", "json");
        map.put("cardNum", cardNum);
        map.put("areaName", areaName);
        map.put("pn", pn);
        String url = BaiduEnum.URL.toString();
        String jsonString = null;
        try {
            jsonString = new HttpUtil().doGetString(url,map);
        } catch (IOException e) {
            System.out.println("访问错误，cardNum：" + cardNum + ",areaName:" + areaName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NetWorkException e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    /**
     * 轮询在start和end范围内的证件号码，轮询所有页数查询
     * @param start
     * @param end
     */
    public void getData(int start, int end) {
        try {
            Map map = new HashMap();
            String url = BaiduEnum.URL.toString();
            map.put("resource_id", "6899");
            map.put("query", "%E5%A4%B1%E4%BF%A1%E8%A2%AB%E6%89%A7%E8%A1%8C%E4%BA%BA%E5%90%8D%E5%8D%95");
            map.put("ie", "utf-8");
            map.put("oe", "utf-8");
            map.put("format", "json");
            for (int i = start; i < end; i++) {
                int pn = 0;
                boolean isOver = false;
                String cardNum = String.valueOf(i);
                while (cardNum.length() < 4) {
                    cardNum = "0" + cardNum;
                }
                map.put("cardNum", cardNum);
                do {
                    map.put("pn", String.valueOf(pn));
                    String jsonString =  new HttpUtil().doGetString(url,map);
                    json2Model(jsonString);
                    dataCount = getAccount(jsonString);
                    /**
                     * 数据大于两千，按所在省份过滤
                     */
                    if (dataCount >= 2000) {
                        for (int j = 0; j < BaiduEnum.AREANAMEARRAY.toArray().length; j++) {
                            int areaPn = 0;
                            isOver = false;
                            map.put("areaName", BaiduEnum.AREANAMEARRAY.toArray()[j]);
                            do {
                                map.put("pn", String.valueOf(areaPn));
                                url = "https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php";
                                jsonString =  new HttpUtil().doGetString(url,map);
                                json2Model(jsonString);
                                String str = Thread.currentThread().getName() + ":" + "查询条件:" + cardNum + "," + BaiduEnum.AREANAMEARRAY.toArray()[j] + ",pn:" + areaPn + ",目前成功插入条数:" + sucessCount + ",当前查询条件总条数:" + dataCount;
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

    /**
     * 处理返回的json数据并入库
     * @param json
     * @throws IOException
     */
    public void json2Model(String json) throws IOException {
        JSONObject jsonObject = JSONObject.fromObject(json);
        String sqlStr = "";
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
                            List resultSet = ConnUtil.getInstance().executeQueryForList(querySql);
                            if (resultSet != null && resultSet.size() > 0) {
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
                            Timestamp timestamp2 = new Timestamp(Long.valueOf(publishDateStamp));
                            sql.append("to_timestamp('" + timestamp2 + "', 'yyyy-mm-dd hh24:mi:ss:ff')" + ",");
                            sql.append("'" + jsonObject2.getString("SiteId") + "'");
                            sql.append(")");
                            sqlStr = sql.toString();
                            ConnUtil.getInstance().executeSaveOrUpdate(sqlStr);
                            sucessCount++;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(Thread.currentThread().getName() + ":" + sqlStr, e);
            }
        }
    }
}
