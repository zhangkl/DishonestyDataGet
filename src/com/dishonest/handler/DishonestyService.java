package com.dishonest.handler;

import com.dishonest.dao.ConnUtil;
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
 * User: zhangkl
 * Date: 七月,2016
 */
public class DishonestyService {

    int sendTime;
    int maxTime = 3;
    String code;

    /**
     * 获取图片验证码
     *
     * @param httpUtil
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public String getImageCode(HttpUtil httpUtil) throws IOException, InterruptedException, SQLException {
        DishonestyService dishonestyService = new DishonestyService();
        byte[] result = httpUtil.doGetByte("http://shixin.court.gov.cn/image.jsp?date=" + System.currentTimeMillis(), null);
        while (result == null) {
            result = httpUtil.doGetByte("http://shixin.court.gov.cn/image.jsp?date=" + System.currentTimeMillis(), null);
            sendTime++;
            if (sendTime < maxTime && result == null) {
                dishonestyService.changeProxy(httpUtil);
                getImageCode(httpUtil);
            }
        }
        ByteInputStream bin = new ByteInputStream();
        bin.setBuf(result);
        BufferedImage image = ImageIO.read(bin);
        String code = CheckNumber.getCheckNumber(image);
        return code;
    }

    /**
     * 获取当前查询条件获得记录条数和最大页数
     *
     * @param cons
     * @return
     */
    public String getPageAccount(String cons) {
        int start = cons.indexOf("<input onclick=\"jumpTo()\" value=\"到\" type=\"button\" /> <input id=\"pagenum\" name=\"pagenum\" maxlength=\"6\" value=\"\" size=\"4\" type=\"text\" /> 页");
        int length = "<input onclick=\"jumpTo()\" value=\"到\" type=\"button\" /> <input id=\"pagenum\" name=\"pagenum\" maxlength=\"6\" value=\"\" size=\"4\" type=\"text\" /> 页".length();
        int end = cons.indexOf("条\n" +
                "\t\t</div>");
        int page = Integer.parseInt(cons.substring(start + length, end).split("/")[1].split(" ")[0]);
        String allAccount = cons.substring(start + length, end).split("/")[1].split("共")[1].replaceAll("条", "");
        return allAccount;
    }

    /**
     * 获取当前页面查询结果的id
     *
     * @param cons
     * @return
     * @throws ParserException
     */
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

    /**
     * 身份证出生日期四位号码获取
     *
     * @return
     */
    public static List<String> getCardNum() {
        List list = new ArrayList();
        for (int i = 1; i <= 12; i++) {
            String mounthNum = String.valueOf(i);
            while (mounthNum.length() < 2) {
                mounthNum = "0" + mounthNum;
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
                String cardNum = mounthNum + day;
                list.add(cardNum);
            }
        }
        return list;
    }

    /**
     * 更换代理方法，status 0表示未使用，1表示正在使用 2表示不可用
     *
     * @param istatus
     * @return
     * @throws SQLException
     */
    public synchronized static String getProxy(int istatus) throws SQLException {
        String proxyUrl;
        Map map = ConnUtil.getInstance().executeQueryForMap("select * from cred_dishonesty_proxy where isusered = '" + istatus + "'");
        if (map == null) {
            System.out.println("*************************************************************************************");
            System.out.println("****************************代理已用光,重置所有代理状态为可用****************************");
            System.out.println("*************************************************************************************");
            ConnUtil.getInstance().executeSaveOrUpdate("update cred_dishonesty_proxy set isusered = 0 where isusered = 1");
            //getProxy(istatus);
            map = ConnUtil.getInstance().executeQueryForMap("select * from cred_dishonesty_proxy where isusered = '" + istatus + "'");
        }
        proxyUrl = (String) map.get("PROXYURL");
        ConnUtil.getInstance().executeSaveOrUpdate("update cred_dishonesty_proxy set isusered = 1 where proxyurl = '" + proxyUrl + "'");
        return proxyUrl;
    }

    /**
     * 更换代理
     *
     * @throws SQLException
     * @throws InterruptedException
     */
    public void changeProxy(HttpUtil httpUtil) throws SQLException, InterruptedException {
        if ("null:0".equals(httpUtil.getProxyURL())) {
            System.out.println(DateUtil.getNowDateTime() + ":" + Thread.currentThread().getName() + ":重复访问出错,无代理不更换，休眠30分钟。");
            Thread.sleep(1000 * 60 * 30);
        } else {
            System.out.println(DateUtil.getNowDateTime() + ":" + Thread.currentThread().getName() + ":重复访问出错,更换前代理:" + httpUtil.getProxyURL());
            Thread.sleep(1000 * 60 * 1);
            String currentProxy = httpUtil.getProxyURL();
            ConnUtil.getInstance().executeSaveOrUpdate("update cred_dishonesty_proxy set isusered = 2 where proxyurl = '" + currentProxy + "'");
            httpUtil.setProxyURL(getProxy(0));
            System.out.println(DateUtil.getNowDateTime() + ":" + Thread.currentThread().getName() + ":重复访问出错,更换后代理:" + httpUtil.getProxyURL());
        }
    }

    /**
     * 访问错误，重复发起，增加最大访问次数控制
     */
    public List getPageList(HttpUtil httpUtil, String cardNum, String pageNum) throws ParserException, IOException, InterruptedException, SQLException {
        String s = getPageHtml(httpUtil, cardNum, pageNum);
        List arrayList = getIDList(s);
        return arrayList;
    }

    public String getPageHtml(HttpUtil httpUtil, String cardNum, String pageNum) throws InterruptedException, SQLException, IOException {
        sendTime = 0;
        String s;
        if (code == null || "".equals(code)) {
            code = getImageCode(httpUtil);
        }
        s = httpUtil.doPostString("http://shixin.court.gov.cn/findd", "pName", "__", "pCardNum", "__________" + cardNum + "____", "pProvince", "0", "currentPage", pageNum, "pCode", code);
        while (sendTime < maxTime && (s == null || s.contains("验证码错误"))) {
            System.out.println(DateUtil.getNowDateTime() + ":" + Thread.currentThread().getName() + ":getPageList验证码错误,发送次数：" + sendTime + ",cardNum:" + cardNum + ",pageNum:" + pageNum + ",code:" + code);
            Thread.currentThread().sleep(1000 * 5);
            code = getImageCode(httpUtil);
            s = httpUtil.doPostString("http://shixin.court.gov.cn/findd", "pName", "__", "pCardNum", "__________" + cardNum + "____", "pProvince", "0", "currentPage", pageNum, "pCode", code);
            sendTime++;
        }
        return s;
    }

    /**
     * 获取并存储具体失信人信息
     */
    public void saveDishoney(String saveid, HttpUtil httpUtil, String cardNum) throws InterruptedException, IOException, SQLException {
        sendTime = 0;
        String idInfo = "";
        Map map = new HashMap();
        map.put("id", saveid);
        if (code == null || "".equals(code)) {
            code = getImageCode(httpUtil);
        }
        map.put("pCode", code);
        idInfo = httpUtil.doGetString("http://shixin.court.gov.cn/findDetai", map);
        while (sendTime < maxTime && (idInfo == null || !idInfo.startsWith("{"))) {
            System.out.println(DateUtil.getNowDateTime() + ":" + Thread.currentThread().getName() + ":saveDishoney,发送次数：" + sendTime + ",map:" + map + ",idInfo:" + idInfo);
            Thread.currentThread().sleep(1000 * 5);
            code = getImageCode(httpUtil);
            map.put("pCode", code);
            idInfo = httpUtil.doGetString("http://shixin.court.gov.cn/findDetai", map);
            sendTime++;
        }
        if (idInfo == null || !idInfo.startsWith("{")) {
            changeProxy(httpUtil);
            saveDishoney(saveid, httpUtil, cardNum);
        }
        JSONObject json = null;
        try {
            json = JSONObject.fromObject(idInfo);
        } catch (JSONException jsonE) {
            System.out.println(idInfo);
            changeProxy(httpUtil);
            saveDishoney(saveid, httpUtil, cardNum);
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
            ConnUtil.getInstance().psAdd(sql, list);
        } catch (SQLException e) {
            System.out.println(Thread.currentThread().getName() + ",saveid:" + saveid + ":idInfo" + idInfo + ",getProxyURL:" + httpUtil.getProxyURL());
            e.printStackTrace();
        }
    }

    public void saveAllCount(String cardNum, String account) throws SQLException {
        String sql = "update cred_dishonesty_log set allcount = '" + account + "' where cardnum = '" + cardNum + "'";
        ConnUtil.getInstance().executeSaveOrUpdate(sql);
    }

    /**
     * 获取待执行任务列表
     * @param hostNameLimit
     * @param hostName
     * @return
     * @throws SQLException
     */
    public List getExeList(boolean hostNameLimit,String hostName) throws SQLException {
        String querySql = "select * from cred_dishonesty_log where result is null order by to_number(startpage) desc";
        if (hostNameLimit) {
            querySql = "select * from cred_dishonesty_log where result is null and hostname = '" + hostName + "' order by to_number(startpage) desc";
        }
        List list = ConnUtil.getInstance().executeQueryForList(querySql);
        return list;
    }

    /**
     * 重置代理表状态，修改正在使用状态的代理为未使用。
     * @throws SQLException
     */
    public void resetProxy() throws SQLException {
        ConnUtil.getInstance().executeSaveOrUpdate("update cred_dishonesty_proxy set isusered = 0 where isusered = 1");
    }
}
