
package pomdp;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.management.*;
import pomdp.timingIN.*;

public class timing implements timingIN{
    /**********************************************************************/
    public double NanoSecFactor = Math.pow(10,9);
    public double user_time, system_time;
    public timing(){
        
    }
    @Override
    public void getSecsDetail( ) 
    {
      /* Get total CPU time in seconds breaking it down by user
         and system time. */


      user_time = getUserTime()/NanoSecFactor;
      system_time = getSystemTime()/NanoSecFactor;

    }  /* getSecsDetail */
    /**********************************************************************/
   public double getUser(){
       return user_time;
   }
   public double getSys(){
       return system_time;
   }
        @Override
    public double getSecs( ) 
    {
      /* Get total CPU time in seconds including user and system time. */
      double cpuTime = getCpuTime()/NanoSecFactor;

      return ( cpuTime );

    }  /* getSecs */
    /*************************************************getCpuTime()*********************/
        @Override
    public void reportTimes( String filename, double tot_secs, String str ) 
    {
      /* 
         Report the total secons time in a nicer hr, min sec format with a
         string to label what the time is for. 
      */
    //String filename = "reportTime.txt";
    File file = new File(filename);
    try{
        int hrs, mins;
       double secs;

       mins = (int)(tot_secs / 60.0) % 60;
       hrs = (int)(tot_secs / 3600.0) % 60;

       secs = tot_secs - 3600*hrs - 60*mins;
      // Create file 
      FileWriter fstream = new FileWriter(file);
      BufferedWriter out = new BufferedWriter(fstream);
      out.write(str+"hrs: "+ hrs+"mins "+ mins+"secs "+ secs +"tot_secs " +tot_secs);
      out.close();
      }catch (Exception e){//Catch exception if any
      System.err.println("Error: " + e.getMessage());
      }


    }  /* reportTimes */
    /** Get CPU time in nanoseconds. */
    public long getCpuTime( ) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
        return bean.isCurrentThreadCpuTimeSupported( ) ?
            bean.getCurrentThreadCpuTime( ) : 0L;
    }

    /** Get user time in nanoseconds. */
    public long getUserTime( ) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
        return bean.isCurrentThreadCpuTimeSupported( ) ?
            bean.getCurrentThreadUserTime( ) : 0L;
    }

    /** Get system time in nanoseconds. */
    public long getSystemTime( ) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
        return bean.isCurrentThreadCpuTimeSupported( ) ?
            (bean.getCurrentThreadCpuTime() - bean.getCurrentThreadUserTime( )) : 0L;
    }
    /**********************************************************************/
}
