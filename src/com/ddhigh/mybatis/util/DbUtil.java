package com.ddhigh.mybatis.util;

import com.sun.istack.internal.Nullable;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class DbUtil {
    private Connection connection;

    public static Map<Type, Integer> portMap = new HashMap<>();

    static {
        portMap.put(Type.MySQL, 3306);
        portMap.put(Type.Oracle, 1521);
    }

    public enum Type {
        MySQL,
        Oracle
    }

    public DbUtil() {
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public DbUtil(String host, String port, String user, String password, Type type, String database) throws ClassNotFoundException, SQLException {
        if (type.equals(Type.MySQL)) {
            Class.forName("com.mysql.jdbc.Driver");
            String sb = "jdbc:mysql://" +
                    host +
                    ":" +
                    port +
                    "/" +
                    database;
            connection = DriverManager.getConnection(sb, user, password);
        } else if (type.equals(Type.Oracle)) {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            String sb = "jdbc:oracle:thin:@//" +
                    host +
                    ":" +
                    port +
                    "/" +
                    database;
            connection = DriverManager.getConnection(sb, user, password);
        } else {
            throw new UnsupportedOperationException("不支持的数据库类型");
        }
    }

    public ResultSet query(String sql, @Nullable Map<Integer, Object> params) throws SQLException, ClassNotFoundException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        if (params != null) {
            for (Map.Entry<Integer, Object> entry : params.entrySet()) {
                preparedStatement.setObject(entry.getKey(), entry.getValue());
            }
        }
        return preparedStatement.executeQuery();
    }

    public int execute(String sql, @Nullable Map<Integer, Object> params) throws SQLException, ClassNotFoundException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        if (params != null) {
            for (Map.Entry<Integer, Object> entry : params.entrySet()) {
                preparedStatement.setObject(entry.getKey(), entry.getValue());
            }
        }
        return preparedStatement.executeUpdate();
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}
