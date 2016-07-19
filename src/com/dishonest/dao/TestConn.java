/*******************************************************************************
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 ******************************************************************************/

package com.dishonest.dao;


import java.io.StringReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestConn {

    //几个数据库变量
    //dbUrl数据库连接串信息，其中“1521”为端口，“ora9”为sid
    String dbUrl = "jdbc:oracle:thin:@192.168.0.196:1521:cred";
    //theUser为数据库用户名
    String theUser = "cred";
    //thePw为数据库密码
    String thePw = "cred";
    ConnectionPool conPool;

    //初始化连接
    private TestConn() {
        try {
            conPool = new ConnectionPool("oracle.jdbc.driver.OracleDriver", dbUrl, theUser, thePw, 50);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static TestConn getInstance() {
        return SingletonFactory.testConn;
    }

    public static void main(String[] args) throws SQLException {
        Map map = TestConn.getInstance().executeQueryForMap("select * from cred_dishonesty_proxy where isusered = 1");
        System.out.println(map == null);
    }

    public Statement creatStatement() {
        Statement statement = null;
        try {
            Connection connection = conPool.getConnection();
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statement;
    }

    public PreparedStatement creatPStatement(String sql) {
        PreparedStatement ps = null;
        try {
            Connection connection = conPool.getConnection();
            ps = connection.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ps;
    }

    //执行查询
    public List executeQueryForList(String sql) throws SQLException {
        List list = new ArrayList();
        ResultSet rs;
        Statement statement = null;
        Connection connection = null;
        try {
            connection = conPool.getConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);
            ResultSetMetaData md = rs.getMetaData(); //得到结果集(rs)的结构信息，比如字段数、字段名等
            int columnCount = md.getColumnCount(); //返回此 ResultSet 对象中的列数
            Map rowData;
            while (rs.next()) {
                rowData = new HashMap(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), rs.getObject(i));
                }
                list.add(rowData);
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            if (statement != null) {
                statement.close();
            }
            conPool.returnConnection(connection);
        }
        return list;
    }

    //执行查询
    public Map executeQueryForMap(String sql) throws SQLException {
        ResultSet rs = null;
        Map rowData = new HashMap();
        Statement statement = null;
        try {
            Connection connection = conPool.getConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);
            ResultSetMetaData md = rs.getMetaData(); //得到结果集(rs)的结构信息，比如字段数、字段名等
            int columnCount = md.getColumnCount(); //返回此 ResultSet 对象中的列数
            if (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), rs.getObject(i));
                }
            } else {
                return null;
            }
            conPool.returnConnection(connection);
        } catch (SQLException e) {
            throw e;
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
        return rowData;
    }

    //执行查询
    public ResultSet executeQuery(String sql) throws SQLException {
        ResultSet rs = null;
        Statement statement = null;
        try {
            Connection connection = conPool.getConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);
            conPool.returnConnection(connection);
        } catch (SQLException e) {
            throw e;
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
        return rs;
    }

    //执行查询
    public void psAdd(String sql, List list) throws SQLException {
        PreparedStatement ps = null;
        Connection connection = null;
        try {
            connection = conPool.getConnection();
            ps = connection.prepareStatement(sql);
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) instanceof Integer) {
                    ps.setInt(i + 1, (Integer) list.get(i));
                } else if (list.get(i) instanceof Date) {
                    ps.setDate(i + 1, (Date) list.get(i));
                } else if (list.get(i) instanceof StringReader) {
                    ps.setCharacterStream(i + 1, (StringReader) list.get(i), 50000);
                } else {
                    ps.setString(i + 1, (String) list.get(i));
                }
            }
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            if (ps != null) {
                ps.close();
            }
            conPool.returnConnection(connection);
        }
    }

    public boolean executeSaveOrUpdate(String sql) throws SQLException {
        Statement statement = null;
        Connection connection = null;
        try {
            connection = conPool.getConnection();
            statement = connection.createStatement();
            if (statement.execute(sql)) {
                return true;
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            if (statement != null) {
                statement.close();
            }
            conPool.returnConnection(connection);
        }
        return false;
    }

    private static class SingletonFactory {
        private static TestConn testConn = new TestConn();
    }
}
