import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import service.Setting;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLManager {

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/nro_data";
    private static final String nick = "jdbc:mysql://127.0.0.1:3306/nro_acc";
    private static final String USER = "root";
    private static final String PASS = "";
    private static final HikariConfig config = new HikariConfig();
    private static final HikariConfig config2 = new HikariConfig();
    private static HikariDataSource ds = null;
    private static HikariDataSource ds2 = null;
    public static Connection conn;
    public static Statement stat;

    static {
        config2.setJdbcUrl(nick);
        config2.setUsername(USER);
        config2.setPassword(PASS);
      ///  config2.setAutoCommit(false);
        config2.setMaximumPoolSize(150);
        config2.setConnectionTimeout(3000);
        config2.addDataSourceProperty("cachePrepStmts", "false");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config2.addDataSourceProperty("prepStmtCacheSize", "50");
        config2.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        ds2 = new HikariDataSource(config2);

    }

    public static Connection getConnection2(String abc) throws SQLException {
        //  System.out.println("SIZE POOL: " + ds.getHikariPoolMXBean().getActiveConnections());

        return ds2.getConnection();
    }

    public static synchronized void create(String host, int port, String database, String user, String pass) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e2) {
            System.out.println("Driver mysql not found!");
            System.exit(0);
        }
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        System.out.println("MySQL connect: " + url);
        try {
            SQLManager.conn = DriverManager.getConnection(url, user, pass);
            SQLManager.stat = SQLManager.conn.createStatement();
            System.out.println("Connect Success!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static synchronized boolean close() {
        System.out.println("Close connection to database");
        try {
            if (SQLManager.stat != null) {
                SQLManager.stat.close();
            }
            if (SQLManager.conn != null) {
                SQLManager.conn.close();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int execute(String sql, int id) {
        Connection conn = null;
        Statement s = null;
        ResultSet rs = null;
        try {
            conn = SQLManager.getConnection2(Setting.DB_NICK);
            s = conn.createStatement();
            s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            rs = s.getGeneratedKeys();
            rs.first();
            final int r = rs.getInt(id);
            rs.close();
            s.close();
            s = null;
            rs = null;
            return r;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (s != null) {
                try {
                    s.close();
                } catch (SQLException ex) {
                    Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
//    protected static Connection connDATA;
//    protected static Connection connNICK;
//    protected static Connection connFROM;
//
//    public static synchronized void create()
//    {
//        final String url = "jdbc:mysql://" + Setting.DB_HOST + "/" + Setting.DB_NICK + "?useUnicode=true&characterEncoding=utf-8";
//        try
//        {
//            SQLManager.connNICK = DriverManager.getConnection(url, Setting.DB_USER, Setting.DB_PASS);
//        }
//        catch (SQLException e)
//        {
//            Util.logException(SQLManager.class, e);
//            System.exit(0);
//        }
//    }
//    
//    public static synchronized void createDATA()
//    {
//        final String url = "jdbc:mysql://" + Setting.DB_HOST + "/" + Setting.DB_DATE + "?useUnicode=true&characterEncoding=utf-8";
//        try
//        {
//            SQLManager.connDATA = DriverManager.getConnection(url, Setting.DB_USER, Setting.DB_PASS);
//        }
//        catch (SQLException e) {
//            Util.logException(SQLManager.class, e);
//            System.exit(0);
//        }
//    }
//    
//    public static synchronized void createFROM()
//    {
//        final String url = "jdbc:mysql://" + Setting.DB_HOST + "/" + Setting.DB_NICK + "?useUnicode=true&characterEncoding=utf-8";
//        try
//        {
//            SQLManager.connFROM = DriverManager.getConnection(url, Setting.DB_USER, Setting.DB_PASS);
//        }
//        catch (SQLException e)
//        {
//            Util.logException(SQLManager.class, e);
//        }
//    }
//    
//    // SQL NICK
//    public static boolean execute(String sql) {
//        try
//        {
//            return connNICK.createStatement().execute(sql);
//        }
//        catch (SQLException e) {
////            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public static ResultSet executeQuery(String sql) {
//        try {
//            return connNICK.createStatement().executeQuery(sql);
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    public static int executeUpdate(String sql) {
//        try {
//            return connNICK.createStatement().executeUpdate(sql);
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return 0;
//        }
//    }
//    
//    public static int executeUpdate1(String sql, Object[] objs) {
//        try {
//            return connNICK.createStatement().executeUpdate(sql);
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return 0;
//        }
//    }
//
//    public static int execute(String sql, int id) {
//        try {
//            Statement s = connNICK.createStatement();
//            s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
//            ResultSet rs = s.getGeneratedKeys();
//            rs.first();
//            final int r = rs.getInt(id);
//            rs.close();
//            s.close();
//            s = null;
//            rs = null;
//            return r;
//        } catch (SQLException e) {
////            e.printStackTrace();
//            return -1;
//        }
//    }
//    public static String execute1(String sql, String user) {
//        try {
//            Statement s = connNICK.createStatement();
//            s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
//            ResultSet rs = s.getGeneratedKeys();
//            rs.first();
//            final String r = rs.getString(user);
//            rs.close();
//            s.close();
//            s = null;
//            rs = null;
//            return r;
//        } catch (SQLException e) {
////            e.printStackTrace();
//            return user;
//        }
//    }
//
//    public static synchronized boolean close() {
//        try
//        {
//            if (connNICK != null) {
//                connNICK.close();
//            }
//            return true;
//        }
//        catch (SQLException e) {
//            return false;
//        }
//    }
//    
//    // SQL DATA SERVER
//    public static boolean executeDATA(String sql) {
//        try {
//            Statement s = connDATA.createStatement();
//            boolean r = s.execute(sql);
//            s.close();
//            s = null;
//            return r;
//        } catch (SQLException e) {
////            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public static ResultSet executeQueryDATA(String sql) {
//        try {
//            return connDATA.createStatement().executeQuery(sql);
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    public static int executeUpdateDATA(String sql) {
//        try {
//            Statement s = connDATA.createStatement();
//            final int r = s.executeUpdate(sql);
//            s.close();
//            s = null;
//            return r;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return 0;
//        }
//    }
//
//    public static int executeDATA(String sql, int id) {
//        try
//        {
//            Statement s = connDATA.createStatement();
//            s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
//            ResultSet rs = s.getGeneratedKeys();
//            rs.first();
//            final int r = rs.getInt(id);
//            rs.close();
//            s.close();
//            s = null;
//            rs = null;
//            return r;
//        }
//        catch (SQLException e) {
////            e.printStackTrace();
//            return -1;
//        }
//    }
//
//    public static synchronized boolean closeDATA() {
//        try {
//            if (connDATA != null) {
//                connDATA.close();
//            }
//            return true;
//        } catch (SQLException e) {
//            return false;
//        }
//    }
//    
//    public static boolean executeFROM(String sql) {
//        try
//        {
//            boolean isRequest = connFROM.createStatement().execute(sql);
//            return isRequest;
//        }
//        catch (SQLException e) {
////            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public static ResultSet executeQueryFROM(String sql) {
//        try {
//            ResultSet Request = connFROM.createStatement().executeQuery(sql);
//            return Request;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    public static int executeUpdateFROM(String sql) {
//        try {
//            int Request = connFROM.createStatement().executeUpdate(sql);
//            return Request;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return 0;
//        }
//    }
//
//    public static int executeFROM(String sql, int id) {
//        try {
//            Statement s = connFROM.createStatement();
//            s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
//            ResultSet rs = s.getGeneratedKeys();
//            rs.first();
//            final int r = rs.getInt(id);
//            rs.close();
//            s.close();
//            s = null;
//            rs = null;
//            return r;
//        } catch (SQLException e) {
////            e.printStackTrace();
//            return -1;
//        }
//    }
//    
//    public static synchronized boolean closeFROM() {
//        try {
//            if (connFROM != null) {
//                connFROM.close();
//            }
//            return true;
//        } catch (SQLException e) {
//            return false;
//        }
//    }
}
