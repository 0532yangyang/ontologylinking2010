package javatools.mydb;

import java.sql.SQLException;
import java.sql.Statement;

public class MymysqlTable {

	/**
	 * @param args
	 */
	Mymysql mymysql;
	Statement stat;
	String tableName;
	int[] indexColumn;

	public MymysqlTable(Mymysql mymysql, String tableName, int numColumns, int[] indexColumn, boolean createNew)
			throws SQLException {
		this.tableName = tableName;
		stat = mymysql.conn.createStatement();
		if (createNew) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE ").append(tableName).append("(");
			for(int i=0;i<numColumns;i++){
				sb.append("c"+i+" varchar(255)");
			}
			stat.executeUpdate("CREATE TABLE " + tableName + " ("+
					
			")");
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
