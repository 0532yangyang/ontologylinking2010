package freebase.relmatch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;

public class SX_searchQueryResult {

	/**
	 * @param args
	 */

	// CREATE TABLE queryFbGraphResult ( startId int, endId int, nellrelation
	// varchar(100), fbrelationNum varchar(100), fbrelationName varchar(255),
	// entityInvolve varchar(255), entityNames varchar(1000))

	static void insert() {
		Connection conn = null;
		try {
			String userName = "clzhang";
			String password = "clzhang";
			String url = "jdbc:mysql://localhost/ontologylinking2010";
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(url, userName, password);
			Statement statement = conn.createStatement();

			String[] relationName = new String[20000];
			{
				// init the relationNames
				DelimitedReader dr = new DelimitedReader(Main.file_fbedge);
				String[] l;
				while ((l = dr.read()) != null) {
					int id = Integer.parseInt(l[1]);
					String r = l[0];
					relationName[id] = r;
				}
				dr.close();
			}
			{
				DelimitedReader dr = new DelimitedReader(Main.file_queryresult_name);
				String[] l;
				while ((l = dr.read()) != null) {
					try {
						String nellrelation = l[0];
						String fbrelId = l[1];
						String fbrelIdSplit[] = l[1].split(",");
						StringBuilder fbrelName = new StringBuilder();
						for (String a : fbrelIdSplit) {
							fbrelName.append(relationName[Integer.parseInt(a)] + "|");
						}
						String entityInvolve = l[2];
						String entityInvolveSplit[] = l[2].split(" ");
						int startId = Integer.parseInt(entityInvolveSplit[0]);
						int endId = Integer.parseInt(entityInvolveSplit[entityInvolveSplit.length - 1]);
						String entityNames = l[3];
						//D.p(entityNames);
						StringBuilder sqlsb = new StringBuilder();
						sqlsb.append("insert into queryFbGraphResult values (").
						append(startId+", ")
						.append(endId+", ").
						append("'"+nellrelation+"', ")
						.append("'"+fbrelId+"', ")
						.append("'"+fbrelName.toString()+"', ")
						.append("'"+entityInvolve+"', ")
						.append("'"+entityNames.replaceAll("'", "''")+"'")
						.append(")");
						statement.executeUpdate(sqlsb.toString());
					} catch (Exception e) {
						System.err.println(l);
						e.printStackTrace();
					}
				}
				dr.close();
			}
			statement.executeUpdate("insert into table01 values" + "(2,'joke')");
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
		insert();
	}

}
