package atri.aws.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TwittTubeDAL {
	private Connection connect = null;
	private Statement statement = null;
	private ResultSet resultSet = null;
	private String DB_END_POINT = "atridbinstance.ceibfhanh104.us-east-1.rds.amazonaws.com";
	private final String DB_USER_NAME = "pavan";
	private final String DB_PWD = "mysql123";
	private final String DB_NAME = "TwittTubeDB";
	private final int DB_PORT = 3306;

	public static void main(String[] args) {
		TwittTubeDAL obj = new TwittTubeDAL();
		obj.createConnectionAndStatement();
//		obj.insertRecord("aleiuflkjewf",1,"abcd.flv");
		
	}

	public void createConnectionAndStatement()
	{
		try{
			// This will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			// Setup the connection with the DB
			connect = DriverManager
					.getConnection("jdbc:mysql://"+DB_END_POINT+":"+DB_PORT+"/"+DB_NAME,DB_USER_NAME,DB_PWD);

			// Statements allow to issue SQL queries to the database
			statement = connect.createStatement();
		} catch (Exception e) {
			e.printStackTrace();
			close();
		}
	}

	public void insertRecord(String convId, int isBroadcastMsg, String videoFileName)
	{
		try {
			createConnectionAndStatement();
			String sql = "INSERT INTO CONVERSATION (conversationId, isBroadcastMsg, videoFileName, createTime)" +
					"VALUES ('"+convId+"', "+isBroadcastMsg+", '"+videoFileName+"', NOW())";
			statement.executeUpdate(sql);
			System.out.println("Inserted records into the table...");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}

	}
	public ArrayList<String> selectConversations(String convId)
	{
		ArrayList<String> convAl = null;
		try {
			createConnectionAndStatement();
			convAl = new ArrayList<String>();
			System.out.println("ConvId:"+convId);
			String sql = "SELECT * FROM CONVERSATION WHERE conversationId='"+convId+"'";
			ResultSet rs = statement.executeQuery(sql);
			while(rs.next())
			{
				String videoFileName = rs.getString("videoFileName");
				System.out.println("VideoFileName:"+videoFileName);
				convAl.add(videoFileName);				
			}
			Collections.sort(convAl);
			return convAl;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
			return convAl;
		}

	}

//	public void createTable()
//	{
//		try {
//			createConnectionAndStatement();
//			String createTableSql = "CREATE TABLE VIDEO_INFO (name VARCHAR(255) not NULL, timestamp TIMESTAMP, " + 
//					" s3link VARCHAR(255), cflink VARCHAR(255), rating INTEGER, totalvotes INTEGER)";
//			statement.executeUpdate(createTableSql);
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			close();
//		}
//
//	}
	// You need to close the resultSet
	private void close() {
		try {
			if (resultSet != null) {
				resultSet.close();
			}

			if (statement != null) {
				statement.close();
			}

			if (connect != null) {
				connect.close();
			}
		} catch (Exception e) {

		}
	}

}