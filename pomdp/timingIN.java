
package pomdp;

public interface timingIN {
    /*******************************************************************/
/**************       EXTERNAL VARIABLES            ****************/
/*******************************************************************/

/*******************************************************************/
/**************       EXTERNAL FUNCTIONS            ****************/
/*******************************************************************/

/* Get total CPU time in seconds breaking it down by user and system
   time. */
public void getSecsDetail( );

/* Get total CPU time in seconds including user and system time. */
public double getSecs( );

/* Report the total secons time in a nicer hr, min sec format with a
   string to label what the time is for.  */
public void reportTimes( String filename,
                         double tot_secs, 
                         String str );

}
