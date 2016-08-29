package com.fayuan.handler;

import com.fayuan.dao.ConnUtil;
import com.fayuan.util.GetDateException;
import com.fayuan.util.HttpUtil;
import com.fayuan.util.HttpUtilPool;
import com.fayuan.util.NetWorkException;
import org.apache.log4j.Logger;
import org.htmlparser.util.ParserException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zhangkl
 * Date: 七月,2016
 */
public class PageHandler implements Runnable {

    Logger logger = Logger.getLogger(PageHandler.class);

    String name;
    String cardNum;
    String areaCode;
    String pageNum;
    String hostName;
    HttpUtil httpUtil;
    HttpUtilPool httpUtilPool;
    DishonestyService dishonestyService;
    int dataType;//数据类型  1为个人 2为企业

    public PageHandler(String name, String cardNum, String areaCode, String pageNum, String hostName, HttpUtilPool httpUtilPool, DishonestyService dishonestyService, int dataType) {
        this.name = name;
        this.cardNum = cardNum;
        this.areaCode = areaCode;
        this.pageNum = pageNum;
        this.hostName = hostName;
        this.httpUtilPool = httpUtilPool;
        this.dishonestyService = dishonestyService;
        this.dataType = dataType;
    }

    @Override
    public void run() {
        try {
            int sameNum = 0;
            int sucessNum = 0;
            this.httpUtil = httpUtilPool.getHttpUtil();
            String html = dishonestyService.getPageHtml(name, cardNum, areaCode, pageNum, httpUtil);
            List arrayList = dishonestyService.getPageList(html);
            for (int j = 0; j < arrayList.size(); j++) {
                String saveid = arrayList.get(j).toString();
                String queryIdSql = "select * from CRED_DISHONESTY_PERSON where iid = '" + saveid + "'";
                if (dataType == 2) {
                    queryIdSql = "select * from CRED_DISHONESTY_ENT where iid = '" + saveid + "'";
                }
                List resultlist = ConnUtil.getInstance().executeQueryForList(queryIdSql);
                if (resultlist != null && resultlist.size() > 0) {
                    sameNum++;
                    continue;
                } else {
                    int num = dishonestyService.saveDishoney(arrayList.get(j).toString(), cardNum, httpUtil);
                    if (num == 0) {
                        sameNum++;
                    } else {
                        sucessNum++;
                    }
                }
            }
            String logStr = "查询条件：" + cardNum + ",areacode:" + areaCode + ",当前页数：" + pageNum + "，总重复个数" + sameNum + ",总成功个数：" + sucessNum + ",查询入库完成,idList:" + arrayList + ",代理地址：" + httpUtil.getProxyURL();
            logger.info(logStr);
            String sql = "select * from cred_dishonesty_pagelog where cardnum ='" + cardNum + "' and pagenum = '" + pageNum + "' and areacode = '" + areaCode + "'";
            List pagelist = dishonestyService.getExeList(sql);
            String logSql;
            if (pagelist != null && pagelist.size() > 0) {
                logSql = "update cred_dishonesty_pagelog set samenum = '" + sameNum + "',sucessnum = '" + sucessNum + "' where pagenum = '" + pageNum + "' and cardnum = '" + cardNum + "'";
            } else {
                logSql = "insert into cred_dishonesty_pagelog (CARDNUM, PAGENUM, HOSTNAME, RESULT, DCURRENTDATE, REMARK, SAMENUM, SUCESSNUM,AREACODE)" +
                        " values ( '" + cardNum + "', '" + pageNum + "', '" + hostName + "', '" + arrayList.toString() + "', sysdate, '" + logStr + "', '" + sameNum + "', '" + sucessNum + "','" + areaCode + "')";
            }
            ConnUtil.getInstance().executeSaveOrUpdate(logSql);
        } catch (GetDateException e) {
            logger.error("线程错误：", e);
            throw new RuntimeException(new InterruptedException());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        } catch (NetWorkException e) {
            e.printStackTrace();
        } finally {
            httpUtilPool.returnHttpUtil(httpUtil);
        }
    }
}
