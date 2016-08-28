package com;

import com.dishonest.dao.ConnUtil;
import com.dishonest.handler.DishonestyService;
import com.dishonest.util.*;
import org.apache.log4j.Logger;
import org.htmlparser.util.ParserException;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: zhangkl
 * Date: 八月,2016
 */
public class MainForBaiduMore implements Runnable {

    static Logger logger = Logger.getLogger(MainForBaiduMore.class);

    Map infoMap;
    DishonestyService ds;
    HttpUtilPool httpUtilPool;

    public MainForBaiduMore(Map infoMap, DishonestyService ds, HttpUtilPool httpUtilPool) {
        this.infoMap = infoMap;
        this.ds = ds;
        this.httpUtilPool = httpUtilPool;
    }

    public static void main(String[] args) throws SQLException, InterruptedException, ParserException, IOException, GetDateException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        HttpUtilPool httpUtilPool = new HttpUtilPool(5);
        DishonestyService ds = new DishonestyService();
        String sql = "select * from cred_dis_baidu_more t where t.sremark = '1' order by iid "; //过滤出百度多出来的数据
        List list = ds.getExeListForPage(sql, 1, 20000);
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Map infoMap = (Map) it.next();
            MainForBaiduMore mainForBaiduMore = new MainForBaiduMore(infoMap, ds, httpUtilPool);
            executorService.execute(mainForBaiduMore);
        }
        executorService.shutdown();
    }

    @Override
    public void run() {
        String name = (String) infoMap.get("SINAME");
        String cardnum = ((String) infoMap.get("SCARDNUM"));
        BigDecimal baidu_id = (BigDecimal) infoMap.get("IID");
        String areacode = "0";
        cardnum = cardnum.replaceAll("\\*", "_");
        HttpUtil httpUtil = null;
        try {
            httpUtil = httpUtilPool.getHttpUtil();
            String html = ds.getPageHtml(name, cardnum, areacode, "1", httpUtil);
            List resultlist = ds.getIDList(html);
            String logStr = "baidu_id:" + baidu_id + ",cardnum:" + cardnum + ",resultlist:" + resultlist;
            /**
             * 判断结果是否能在法院网站搜到对应人的数据，如果能搜到则  轮询  补全中间三位
             * 如果搜不到，则在表中 remark 字段做标示
             */
            if (resultlist.size() > 0) {
                for (int i = 0; i < resultlist.size(); i++) {
                    String fayuan_id = (String) resultlist.get(i);
                    String queryIdSql = "select iid from CRED_DISHONESTY_PERSON p  where p.iid = '" + fayuan_id + "'" +
                            "union  all " +
                            "select iid from CRED_DISHONESTY_ENT t  where t.iid = '" + fayuan_id + "'";
                    List list = ConnUtil.getInstance().executeQueryForList(queryIdSql);
                    if (list != null && list.size() > 0) {
                        logStr = "此人法院表中已存在" + fayuan_id;
                        continue;
                    } else {
                        /**
                         * 轮询补全身份证脱敏生日三位数
                         */
                        String temp = String.valueOf(cardnum.charAt(10));
                        int mounth_1 = Integer.valueOf(temp); //获取生日月份第一位
                        int mounth_2_max;
                        int mounth_2_min;
                        if (mounth_1 == 0) {
                            mounth_2_min = 1;
                            mounth_2_max = 9;
                        } else {
                            mounth_2_min = 0;
                            mounth_2_max = 2;
                        }
                        //根据生日第一位确定生日第二位的范围
                        for (int mounth_2 = mounth_2_min; mounth_2 <= mounth_2_max; mounth_2++) {
                            String tempCardnum = cardnum.replaceFirst("_", String.valueOf(mounth_2));
                            html = ds.getPageHtml(name, tempCardnum, areacode, "1", httpUtil);
                            resultlist = ds.getIDList(html);
                            if (resultlist.size() > 0) {
                                cardnum = tempCardnum; //获取到正确的第二位数值
                                for (int day_1 = 0; day_1 <= 3; day_1++) {
                                    tempCardnum = cardnum.replaceFirst("_", String.valueOf(day_1)); //轮询日期第一位的
                                    html = ds.getPageHtml(name, tempCardnum, areacode, "1", httpUtil);
                                    resultlist = ds.getIDList(html);
                                    if (resultlist.size() > 0) {
                                        cardnum = tempCardnum;//确定日期第一位
                                        int day_2_min = 0;
                                        int day_2_max = 9;
                                        if (day_1==0){
                                            day_2_min = 1;
                                        } else if (day_1 == 3) {
                                            day_2_max = 1;
                                        }
                                        //根据日期第一位轮询日期第二位的范围
                                        for (int day_2 = day_2_min; day_2 < day_2_max; day_2++) {
                                            tempCardnum = cardnum.replaceFirst("_", String.valueOf(day_2));
                                            html = ds.getPageHtml(name, tempCardnum, areacode, "1", httpUtil);
                                            resultlist = ds.getIDList(html);
                                            if (resultlist != null && resultlist.size() > 0) {
                                                for (int j = 0; j < resultlist.size(); j++) {
                                                    cardnum = tempCardnum;
                                                    fayuan_id = (String) resultlist.get(j);
                                                    ds.saveDishoney(fayuan_id, cardnum, areacode, httpUtil);
                                                }
                                                logStr = "法院数据中存在并且已插入resultlist:" + resultlist + ",cardnum:" + cardnum + ",时间:" + DateUtil.getNowDate();
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                logStr = "不存在name:" + name + ",cardnum:" + cardnum + ",idlist:" + resultlist;
            }
            String modifySql = "update cred_dis_baidu_more set sremark = '" + logStr + "' where iid = " + baidu_id;
            ConnUtil.getInstance().executeSaveOrUpdate(modifySql);
            logger.info(logStr);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        } catch (NetWorkException e) {
            e.printStackTrace();
        } catch (GetDateException e) {
            e.printStackTrace();
        }finally {
            httpUtilPool.returnHttpUtil(httpUtil);
        }
    }
}
