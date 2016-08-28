/*******************************************************************************
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 ******************************************************************************/

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.baidu.httpUtil;

import com.dishonest.dao.ConnUtil;
import org.apache.log4j.Logger;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.*;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class HtmlParser {
    private static final String SiteName = "";
    private static Logger logger;
    private Connection conn = null;

    public HtmlParser() {
    }

    public static void main(String[] args) throws IOException, ParserException, InterruptedException {
        getProxy("http://www.youdaili.net/Daili/http/4722.html");
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

    public static Map<String, String> getData(String url, String content, NodeFilter nodeFilter) {
        HashMap map = new HashMap();

        try {
            Parser e;
            if (url != null && !"".equals(url)) {
                e = new Parser(url);
            } else {
                e = new Parser();
            }

            if (content != null) {
                e.setInputHTML(content);
            }

            e.setEncoding("utf-8");
            NodeList nodes = e.extractAllNodesThatMatch(nodeFilter);

            for (int divFilter = 1; divFilter < nodes.size(); ++divFilter) {
                String divnodes = nodes.elementAt(divFilter).toPlainTextString().trim();
                String filters = nodes.elementAt(divFilter).getChildren().elementAt(0).toPlainTextString().trim();
                map.put(filters, divnodes);
            }

            AndFilter var14 = new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "bd aboutBd"));
            NodeList var15 = e.extractAllNodesThatMatch(var14);
            map.put("公司简介", var15.asString());
            NodeFilter[] var16 = new NodeFilter[]{new AndFilter(new TagNameFilter("span"), new HasAttributeFilter("class", "name")), null, null};
            var16[1] = new HasChildFilter(var16[0]);
            var16[2] = new TagNameFilter("p");
            NodeList componynodes = e.extractAllNodesThatMatch(nodeFilter);

            for (int i = 1; i < componynodes.size(); ++i) {
                String value = componynodes.elementAt(i).toPlainTextString().trim();
                String describe = componynodes.elementAt(i).getChildren().elementAt(0).toPlainTextString().trim();
                map.put(describe, value);
                System.out.println(map);
            }
        } catch (Exception var13) {
            var13.printStackTrace();
        }

        System.out.println(map);
        return map;
    }

    public static int getPage(String url, String cons) {
        int start = cons.indexOf("<input onclick=\"jumpTo()\" value=\"到\" type=\"button\" /> <input id=\"pagenum\" name=\"pagenum\" maxlength=\"6\" value=\"\" size=\"4\" type=\"text\" /> 页");
        int length = "<input onclick=\"jumpTo()\" value=\"到\" type=\"button\" /> <input id=\"pagenum\" name=\"pagenum\" maxlength=\"6\" value=\"\" size=\"4\" type=\"text\" /> 页".length();
        int end = cons.indexOf("条\n" +
                "\t\t</div>");
        int page = Integer.parseInt(cons.substring(start + length, end).split("/")[1].split(" ")[0]);
        System.out.println(page);
        return page;
    }


    public static List<Map<String, String>> getLink(String url, String cons) {
        ArrayList list = new ArrayList();

        try {
            Parser e;
            if (url != null && !"".equals(url)) {
                e = new Parser(url);
                System.out.println(e.toString());
            } else {
                e = new Parser();
                e.setInputHTML(cons);
            }

            e.setEncoding("utf-8");
            NodeFilter filter1 = new HasAttributeFilter("class", "View");
            NodeFilter filter2 = new TagNameFilter("a");
            AndFilter contentFilter = new AndFilter(filter1, filter2);
            NodeList nodes2 = e.extractAllNodesThatMatch(contentFilter);

            for (int i = 0; i < nodes2.size(); ++i) {
                HashMap map = new HashMap();
                LinkTag linkTag = (LinkTag) nodes2.elementAt(i);
                list.add(linkTag.getAttribute("id"));
            }
        } catch (Exception var9) {
            var9.printStackTrace();
        }

        return list;
    }

    public static String getUrl(String url, Map<String, String> map) {
        StringBuffer urlString = new StringBuffer(url);
        int k = 0;

        for (Iterator i$ = map.keySet().iterator(); i$.hasNext(); ++k) {
            String key = (String) i$.next();
            if (k == 0) {
                urlString.append("?");
            } else {
                urlString.append("&");
            }

            urlString.append(key).append("=").append(map.get(key));
        }

        return urlString.toString();
    }

    public static List testTable(String url, String content) {
        NodeList nodeList = null;
        ArrayList trlist = new ArrayList();
        NodeClassFilter tableFilter = new NodeClassFilter(TableTag.class);
        HasAttributeFilter cssSelectorNodeFilter = new HasAttributeFilter("instList-degree");
        new AndFilter(tableFilter, cssSelectorNodeFilter);

        try {
            Parser e;
            if (url != null && !"".equals(url)) {
                e = new Parser(url);
            } else {
                e = new Parser();
            }

            if (content != null && !"".equals(content)) {
                e.setInputHTML(content);
            }

            nodeList = e.extractAllNodesThatMatch(tableFilter);

            for (int i = 0; i <= nodeList.size(); ++i) {
                if (nodeList.elementAt(i) instanceof TableTag) {
                    TableTag tag = (TableTag) nodeList.elementAt(i);
                    TableRow[] rows = tag.getRows();

                    for (int j = 0; j < rows.length; ++j) {
                        ArrayList tdlist = new ArrayList();
                        TableRow tr = rows[j];
                        TableColumn[] td = tr.getColumns();

                        for (int k = 0; k < td.length; ++k) {
                            tdlist.add(td[k].toPlainTextString().trim());
                        }

                        if (tdlist.size() > 0) {
                            trlist.add(tdlist);
                        }
                    }
                }
            }
        } catch (Exception var16) {
            var16.printStackTrace();
            System.out.println(var16.getMessage());
        }

        return trlist;
    }
}
