package com.example.afra.myandroidmp3;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;



/**

 * Database tool class

 */

public class Util {

    // url is database connection string,the format of url is:Database protocol :// <database server address> [<: port>]/<Database name>?Character Settings
    // 127.0.0.1 represent that the database server is in local computer, usually configured the localhost represent 127.0.0.1  in hosts file
    private static String url = "jdbc:mysql://47.95.10.11:3306/mydb"; // Database connection string,the character encoding is utf8
    private static String user = "root"; // Database account
    private static String password = "123456"; // Database password

    // Static code block,realized to load driver,executed once only when the class is loaded
    static {
        try {
            // Load driver
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the database connection, create a new connection for each time the method is called
     *
     * @return If create the connection is successful, returns the database connection object; otherwise returns null
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Create a database connection
            conn = DriverManager.getConnection(url, user, password);
            // If successful get the database connection, print a success message
            System.out.println("connect success!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }


    public static void closeConnection(Connection conn) {
        try {
            if ((conn != null) && (!conn.isClosed())) {
                conn.close();
                conn = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void closeStatement(Statement st) {
        try {
            if ((st != null) && (!st.isClosed())) {
                st.close();
                st = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public static void closeResultSet(ResultSet rs) {
        try {
            if ((rs != null) && (!rs.isClosed())) {
                rs.close();
                rs = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test whether the database connection is successful
     */
    public static void main(String[] args) {
        getConnection();
    }
}