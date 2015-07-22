package connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;

/**
 * Represents the connection to the database.
 * @author mlulich
 *
 */

public class DBConn
{
	private Connection connection;
	static private String connectString		= "jdbc:jtds:sqlserver://PC20167:1433;instance=MSSQLSERVER";
	static private String connectUserName           = "sa";
	static private String connectPassWord           = "Pentium4!";
	static private String connectDBName		= "BMS";
	
	/**
	 * Constructor for a Database Connection.
	 * @param db_connect_string     The string defining the instance, port, etc.
	 * @param db_userid             The user name
	 * @param db_password           The password to get into the database with.
         * @param db_name               The name of the database.
	 */
	public DBConn(String db_connect_string, String db_userid, String db_password, String db_name)
	{
		// Set up the connection...
		DBConn.connectString = db_connect_string;
		DBConn.connectUserName = db_userid;
		DBConn.connectPassWord = db_password;
		DBConn.connectDBName = db_name;
		
		checkClass();
	}
	
	public DBConn()
	{
		checkClass();
	}
	
	private boolean checkClass()
	{
		// Check that the jtds Driver class exists.
		try
		{
			Class.forName("net.sourceforge.jtds.jdbc.Driver");
			return true;
		}
		catch (ClassNotFoundException e)
		{
			return false;
		}
	}

	public String getDBName()
	{
		return connectDBName;
	}
	
	/**
	 * Sets up a connection to the database using the connection object's set values.
	 * @return
	 */
	public boolean SetUpConnection()
	{
		return SetUpConnection(DBConn.connectString, DBConn.connectUserName, DBConn.connectPassWord);
	}
	
	/**
	 * Attempts to set up the connection to the database.
	 * @param db_connect_string
	 * @param db_userid
	 * @param db_password
	 * @return Returns true if the connection is successfully set up, false otherwise.
	 */
	private boolean SetUpConnection(String db_connect_string, String db_userid, String db_password)
	{		
		try
		{
			// Set up the connection.
			this.connection = DriverManager.getConnection(db_connect_string, db_userid, db_password);
			return true;
		}
		catch (SQLException e)
		{
			// Reset the connection to null.
			Close();
			this.connection = null;
			// Print out an error message to the console.
			System.out.println("DBConn.SetUpConnection() error: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * Closes the database connection.
	 */
	public void Close()
	{
		if (this.IsConnected())
		{
			try
			{
				this.connection.close();
				this.connection = null;
			}
			catch (SQLException e)
			{
				// Print out an error message to the console.
				System.out.println("DBConn.Close() error: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Checks to see if the database connection is active or not.
	 * @return True if the connection is active, false otherwise.
	 */
	public boolean IsConnected()
	{
		if (this.connection != null)
			return true;
		return false;
	}
	
	private PreparedStatement generatePreparedStatement(String pQuery, ArrayList<Object> pVars)
	{
		return generatePreparedStatement(pQuery, pVars, false);
	}
	
	/**
	 * Creates a prepared statement from a provided string form and a list of variables.
	 * @param pQuery
	 * @param pVars
	 * @return
	 */
	private PreparedStatement generatePreparedStatement(String pQuery, ArrayList<Object> pVars, boolean pCreate)
	{
		try
		{
			PreparedStatement ps = null;
			if (pCreate)
			{
				ps = this.connection.prepareStatement(pQuery, Statement.RETURN_GENERATED_KEYS);
			}
			else
			{
				ps = this.connection.prepareStatement("USE " + DBConn.connectDBName + "; " + pQuery,
						Statement.RETURN_GENERATED_KEYS);
			}
			// Insert the variables only if we were provided with any.
			if (pVars != null)
			{
				// Place the variables into the prepared statement based on type.
				for (int i = 1; i <= pVars.size(); i++)
				{
					Object obj = pVars.get(i - 1);
					if (obj == null)
						ps.setNull(i, java.sql.Types.NULL);
					else if (obj instanceof Integer)
						ps.setInt(i, (Integer)obj);
					else if (obj instanceof Float)
						ps.setFloat(i, (Float)obj);
					else if (obj instanceof Double)
						ps.setDouble(i, (Double)obj);
					else if (obj instanceof Boolean)
						ps.setBoolean(i, (Boolean)obj);
					else if (obj instanceof String)
						ps.setString(i, (String)obj);
					else if (obj instanceof java.sql.Date)
						ps.setDate(i, (java.sql.Date)obj);
					else
						ps.setObject(i, obj);
				}
			}
			return ps;
		}
		catch (SQLException e)
		{
			// Print out an error message to the console.
			System.out.println("DBConn.generatePreparedStatement() error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Runs a given query on the database we're connected to.
	 * @param pQuery		A string defining the form of the query.
	 * @param pVars			An ArrayList of Objects to be inserted into the query.
	 * @return Returns a ResultSet of the results of the query.
	 */
	public ResultSet RunQuery(String pQuery, ArrayList<Object> pVars)
	{
		// If the connection hasn't been set up, return null immediately.
		//if (!this.IsConnected()) return null;
		try
		{
			// Prepare the query.
			PreparedStatement ps = generatePreparedStatement(pQuery, pVars);
			
			// Execute the query and keep the results.
			ResultSet rs = ps.executeQuery();
			return rs;
		}
		catch (SQLException e)
		{
			// Print out an error message to the console.
			System.out.println("DBConn.RunQuery() error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		catch (NullPointerException e)
		{
			// Print out an error message to the console.
			System.out.println("DBConn.RunQuery() error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean RunUpdate(String pUpdate, ArrayList<Object> pVars)
	{
		return RunUpdate(pUpdate, pVars, false);
	}
	
	/**
	 * Runs a given update to a database.
	 * @param pUpdate
	 * @param pVars
	 * @return
	 */
	public boolean RunUpdate(String pUpdate, ArrayList<Object> pVars, boolean pCreate)
	{
		if (!this.IsConnected()) return false;
		try
		{
			PreparedStatement ps = generatePreparedStatement(pUpdate, pVars, pCreate);
			ps.executeUpdate();
			return true;
		}
		catch (SQLException e)
		{
			// Print out an error message to the console.
			System.out.println("DBConn.RunUpdate() error: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		catch (NullPointerException e)
		{
			// Print out an error message to the console.
			System.out.println("DBConn.RunUpdate() error: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
}
