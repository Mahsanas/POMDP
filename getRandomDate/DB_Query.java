/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package genrandomdata;
import java.sql.Connection;
import java.sql.*;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 *
 * @author mahsa
 */
public class DB_Query {
    
    public Connection conn;
    public DB_Query(String Con_String)
    {
        try
        {
        try
         {  
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(Con_String);
         }
         catch(SQLException se)
         {
             System.out.println(se.getSQLState());
             System.out.println(se.getMessage());
         }
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
    
    public ResultSet Execute_Query(String query)
    {
        try
         {   
             Statement stmt = conn.createStatement();
             return stmt.executeQuery(query);
                      
         }
         catch(SQLException se)
         {
             System.out.println(se.getSQLState());
             System.out.println(se.getMessage());
             return null;
         }
    }

    public int Execute_Upd(String query)
    {
        try
        {
            Statement stmt = conn.createStatement();
            return stmt.executeUpdate(query);
        }
        catch(SQLException se)
        {
          System.out.println(se.getMessage());
          return 0;
        }
    }
}
