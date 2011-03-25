package javatools.mydb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Mymysql {

	/**
	 * @param args
	 */
	Connection conn = null;
	
	public void Mymysql(String name_database) {
		try {
			String userName = "clzhang";
			String password = "clzhang";
			String url = "jdbc:mysql://localhost/"+name_database;
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(url, userName, password);
//			Statement statement = conn.createStatement();
//			statement.executeUpdate("insert into table01 values"+"(2,'joke')");
			System.out.println("Database connection established");
		} catch (Exception e) {
			System.err.println("Cannot connect to database server");
		} finally {
			if (conn != null) {
				try {
					conn.close();
					System.out.println("Database connection terminated");
				} catch (Exception e) { /* ignore close errors */
				}
			}
		}
	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
