package com.ddhigh.mybatis.util;


import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class DbUtil {
    private Connection connection;
    private String host;
    private String port;
    private String user;
    private String password;
    private Type type;
    private String database;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

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

    public DbUtil(String host, String port, String user, String password, Type type, String database) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.type = type;
        this.database = database;
    }

    public String buildConnectionString(DbUtil dbUtil) {
        if (dbUtil.type.equals(Type.MySQL)) {
            return "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=Asia%2FShanghai";
        } else if (type.equals(Type.Oracle)) {
            return "jdbc:oracle:thin:@//" + host + ":" + port + "/" + database;
        }
        throw new UnsupportedOperationException("不支持的数据库类型");
    }

    private void connect() throws ClassNotFoundException, SQLException {
        String connectString = buildConnectionString(this);
        if (type.equals(Type.MySQL)) {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } else if (type.equals(Type.Oracle)) {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } else {
            throw new UnsupportedOperationException("不支持的数据库类型");
        }
        connection = DriverManager.getConnection(connectString, user, password);
    }

    public ResultSet query(String sql, Map<Integer, Object> params) throws SQLException, ClassNotFoundException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        if (params != null) {
            for (Map.Entry<Integer, Object> entry : params.entrySet()) {
                preparedStatement.setObject(entry.getKey(), entry.getValue());
            }
        }
        return preparedStatement.executeQuery();
    }

    public int execute(String sql, Map<Integer, Object> params) throws SQLException, ClassNotFoundException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
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
            connection = null;
        }
    }
}
