
package pomdp;

public interface pomdpIN {
    
/**********************************************************************/
/********************       CONSTANTS       ***************************/
/**********************************************************************/

/**********************************************************************/
/********************   EXTERNAL VARIABLES   **************************/
/**********************************************************************/



    /**********************************************************************/
    /********************   EXTERNAL FUNCTIONS    *************************/
    /**********************************************************************/

    /* Often we would like to do some max or min procedure and require
       initialization to the most extreme value.  Since the extreme value
       depends on whether or not we are using rewards or costs, we have
       encapsulated this in this routine.  */
    public double worstPossibleValue();

    /* Often we would like to do some max or min procedure and require
       initialization to the most extreme value.  Since the extreme value
       depends on whether or not we are using rewards or costs, we have
       encapsulated this in this routine.  */
    public double bestPossibleValue();

    /* Often we would like to do some max or min procedure and require
       coparing a new value to a current value.  Since the test for which
       is better depends on whether rewards or costs are being used, we
       have encapsulated this in this routine.  We also want to account
       for the precision of the current run (i.e.,
       gDoubleEqualityPrecision.)  */
    public int isBetterValue( double new_value, 
                              double current, 
                              double epsilon );

    /* Does the necessary things to read in and set-up a POMDP file.  Also
     precomputes which observations are possible and which are not.  */
    public void initializePomdp( String filename, 
                                 double obs_possible_epsilon );

    /* Deallocates the POMDP read in by initializePomdp().  */
    public void cleanUpPomdp(  );


}
