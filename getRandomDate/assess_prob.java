
package genrandomdata;
import java.sql.ResultSet;
import java.sql.SQLException;

//NOTE: for simplicity of the code the states are considered the same as the web service names (which are actions)
// If you need to seperate the states from the action names you need to make simple changes to the code,
//the changes are gonna be straightforward

//Two function that are to be used are :

//1: assess_State_prob which asseses the probabilities of type P(St1=Assembel_order | st0 = check_avail)


//2: assess_Obs_Prob which assesses the probabilities of type P(St1=Assembel_Order | Observation=yes , action = check_avail)


// agian use web service name given in database as states , if you might need to seperate states from action as we have more states than action names
// change small changes to the code

/* NOTE : remember to change the connectionstring to work with your own database*/

public class assess_prob
{
    private String ConnectionString = "jdbc:mysql://localhost:3306/wfcomp_db?"+ "user=root&password=123";
     //function to assess the probabilities of P(st1=b|st0=a)

    public double assess_State_prob(String st0,String st1)
    {
        
          double total = 0;     
          try
        {
           DB_Query db = new DB_Query(ConnectionString);
           double count =0;
           ResultSet rs = db.Execute_Query("select count(*) as cnt from invocation as inv1,invocation as inv2,service as srv1, service as srv2 where srv1.id=inv1.srv_id and srv2.id=inv2.srv_id and srv1.name="+st0+" and srv2.name="+st1+" and inv2.exec_time=inv1.exec_time+inv1.resp_time");
           
           if(rs.next())
              count =  Integer.parseInt(rs.getString("cnt"));
          total = assess_single(st1);

          if(total!=0)
            return (double)(count/total);
          else
              return 0;
        }
        catch(SQLException ex)
        {
            System.out.println(ex.getMessage());
            return 0;
        }
    }

    //function to assess single probabilities of the form P(a=0), (the # of rows having a=0)
    private double assess_single(String value)
    {
         try
        {
            // The Workflow instance is generated
           DB_Query db = new DB_Query(ConnectionString);
           ResultSet rs = db.Execute_Query("select count(*) as cnt from invocation,service where service.id=srv_id and service.name="+value);
           
           int count =0 ;
           if(rs.next())
              count =  Integer.parseInt(rs.getString("cnt"));
           
           return count;
        }
        catch(SQLException ex)
        {
            System.out.println(ex.getMessage());
             return 0;
        }
    }

    //function to assess the probabilities of type P(st=st1 | obs = obs1, action = a)
    public double assess_Obs_prob(String state,String obs,String action)
    {
         try
        {
           DB_Query db = new DB_Query(ConnectionString);
           double count =0;
           ResultSet rs = db.Execute_Query("select count(*) as cnt from invocation as inv1,invocation as inv2,service as srv1, service as srv2 where srv1.id=inv1.srv_id and srv2.id=inv2.srv_id and srv1.name="+action+" and inv1.observation="+obs+" and srv2.name="+state+" and inv2.exec_time=inv1.exec_time+inv1.resp_time");

           if(rs.next())
              count =  Integer.parseInt(rs.getString("cnt"));

          // calculate the divider
          double total = 0;
          rs = db.Execute_Query("select count(*) as cnt from invocation ,service as srv1 where srv1.id= srv_id and srv1.name="+action+" and observation="+obs);
          if(rs.next())
              total =  Integer.parseInt(rs.getString("cnt"));

          if(total!=0)
            return (double)(count/total);
          else
              return 0;
        }
        catch(SQLException ex)
        {
            System.out.println(ex.getMessage());
            return 0;
        }
    }
}
