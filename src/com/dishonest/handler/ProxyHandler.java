/*******************************************************************************
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 ******************************************************************************/

package com.dishonest.handler;

import com.dishonest.dao.ConnUtil;
import com.dishonest.util.CheckNumber;
import com.dishonest.util.HttpUtil;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
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
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.sql.SQLException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zhangkl
 * Date: 七月,2016
 */
public class ProxyHandler implements Runnable {

    private int sendtimes = 0;
    private int maxtimes = 5;

    public static void main(String[] args) {
        try {
            getProxy("http://www.youdaili.net/Daili/Socks/4737.html");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getProxy(String url) throws ParserException {
        Parser parser = new Parser(url);
        parser.setEncoding("utf-8");
        NodeFilter filter1 = new HasAttributeFilter("style", "font-size:14px;");
        NodeFilter filter2 = new TagNameFilter("span");
        AndFilter contentFilter = new AndFilter(filter1, filter2);
        NodeList nodes = parser.extractAllNodesThatMatch(contentFilter);
        System.out.println(nodes.asString());
        String[] strings = nodes.asString().split("\n");
        for (int i = 0; i < strings.length - 5; i++) {
            if (strings[i].contains("@")){
                System.out.println(i + ":" + strings[i].split("@")[0]);
                try {
                    ConnUtil.getInstance().executeSaveOrUpdate("insert into cred_dishonesty_proxy (proxyurl,dgetdata,isusered) values ('" + strings[i].split("@")[0] + "',sysdate,0)");
                } catch (SQLException e) {
                    if (e.getMessage().contains("ORA-00001: 违反唯一约束条件 (CRED.PK_PROXY)")) {
                        System.out.println(e.getMessage());
                        continue;
                    } else {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public static void getNetStatus() throws IOException {
        Process process = Runtime.getRuntime().exec("ping shixin.court.gov.cn -t");
        InputStreamReader isr = new InputStreamReader(process.getInputStream(), "GBK");
        LineNumberReader reader = new LineNumberReader(isr);
        String line = "";
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }

    public static void compare() {
        String[] strings = new String[]{"0:1238 共12372", "660:14 共135", "661:7 共66", "662:16 共151", "663:9 共87", "664:18 共174", "665:16 共151", "666:13 共123", "667:41 共401", "668:16 共160", "669:166 共1659", "670:227 共2269", "671:44 共440", "672:147 共1467", "673:43 共423", "674:140 共1392", "675:82 共813", "676:31 共301", "677:30 共294", "678:56 共556", "679:28 共274", "680:2 共17", "681:25 共241", "682:21 共203", "683:18 共171", "684:12 共113", "685:1 共0", "686:9 共83", "687:5 共45", "688:2 共12", "689:7 共65", "690:9 共86", "691:1 共0", "692:1 共0", "693:1 共0"};
        int sum = 0;
        for (int i = 1; i < strings.length; i++) {
            int temp = Integer.valueOf(strings[i].split("共")[1]);
            sum += temp;
        }
        System.out.println(sum);
    }

    public static void getSame() throws InterruptedException, IOException {
        HttpUtil httpUtil = new HttpUtil();
        Map map = new HashMap();
        for (int i = 1; i <= 10; i++) {
            byte[] result = httpUtil.doGetByte("http://shixin.court.gov.cn/image.jsp?date=" + System.currentTimeMillis(), null);
            ByteInputStream bin = new ByteInputStream();
            bin.setBuf(result);
            BufferedImage image = ImageIO.read(bin);
            String code = CheckNumber.getCheckNumber(image);
            String s = httpUtil.doPostString("http://shixin.court.gov.cn/findd",
                    "pName", "__", "pCardNum", "__________1110____", "pProvince", "672", "currentPage", i + "", "pCode", code);
            if (s.contains("验证码错误")) {
                i--;
                continue;
            }
            map.put(i, getIDList(s));
        }
        for (int j = 1; j < map.size(); j++) {
            System.out.println("页数：" + j + ":" + map.get(j));

        }
    }

    public static List<String> getIDList(String cons) {
        ArrayList list = new ArrayList();
        if (cons == null || "".equals(cons) || cons.contains("验证码错误")) {
            return null;
        }
        try {
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
        } catch (Exception var9) {
            var9.printStackTrace();
        }
        return list;
    }

    public static void getAccount() throws InterruptedException, IOException, SQLException {
        HttpUtil httpUtil = new HttpUtil();
        ConnUtil connUtil = ConnUtil.getInstance();
        List list = connUtil.executeQueryForList("select * from cred_dishonesty_log");
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Map map = (Map) it.next();
            String cardNum = (String) map.get("CARDNUM");
            String s;
            do {
                byte[] result = httpUtil.doGetByte("http://shixin.court.gov.cn/image.jsp?date=" + System.currentTimeMillis(), null);
                ByteInputStream bin = new ByteInputStream();
                bin.setBuf(result);
                BufferedImage image = ImageIO.read(bin);
                String code = CheckNumber.getCheckNumber(image);
                s = httpUtil.doPostString("http://shixin.court.gov.cn/findd",
                        "pName", "__", "pCardNum", "__________" + cardNum + "____", "pProvince", "0", "pCode", code);
            } while (s.contains("验证码错误"));
            System.out.println(cardNum + ":" + getPageAccount(s));
        }
    }

    public static String getPageAccount(String cons) {
        int start = cons.indexOf("<input onclick=\"jumpTo()\" value=\"到\" type=\"button\" /> <input id=\"pagenum\" name=\"pagenum\" maxlength=\"6\" value=\"\" size=\"4\" type=\"text\" /> 页");
        int length = "<input onclick=\"jumpTo()\" value=\"到\" type=\"button\" /> <input id=\"pagenum\" name=\"pagenum\" maxlength=\"6\" value=\"\" size=\"4\" type=\"text\" /> 页".length();
        int end = cons.indexOf("条\n" +
                "\t\t</div>");
        String page = null;
        try {
            page = cons.substring(start + length, end).split("/")[1];
        } catch (Exception e) {
            System.out.println(cons);
            e.printStackTrace();
        }
        return page;
    }

    public void testRec() {
        System.out.println(111);
        while (true) {
            testRec();
            System.out.println(222);
        }
    }

    @Override
    public void run() {
        do {
            sendtimes++;
            System.out.println(Thread.currentThread().getName() + ":" + sendtimes);
        } while (sendtimes <= maxtimes);
        sendtimes = 0;

        /*for (int i = 150; i < 1520; i++) {
            try {
                List idrs = testConn.executeQueryForList("select * from CRED_DISHONESTY_PERSON where iid = '" + i + "'");
                if (idrs != null && idrs.size() > 0) {
                    testConn.executeSave("delete from CRED_DISHONESTY_PERSON where iid = '" + i + "'");
                }

                List  list = new ArrayList();
                list.add(i);
                list.add(i);
                list.add(i);
                testConn.executeSave("update cred_dishonesty_log set dcurrentdate = sysdate where cardnum = 0101");
                testConn.psAdd("insert into CRED_DISHONESTY_PERSON (IID, SINAME, SCARDNUM) values (?, ?, ?)",list);

                idrs = testConn.executeQueryForList("select * from CRED_DISHONESTY_PERSON where iid = '" + i + "'");
                if (idrs != null && idrs.size() > 0) {
                    testConn.executeSave("delete from CRED_DISHONESTY_PERSON where iid = '" + i + "'");
                }

                List rs = testConn.executeQueryForList("select count(*) num from v$process");
                Iterator it = rs.iterator();
                while (it.hasNext()){
                    Map map = (Map) it.next();
                    System.out.println(Thread.currentThread().getName()+":"+map.get("NUM"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }*/
    }
}
