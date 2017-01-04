package pomdp;
import pomdp.paramsIN.*;

public interface inc_pruneIN {
    /**********************************************************************/
    /********************       CONSTANTS       ***************************/
    /**********************************************************************/

    /* We will use the 'length' field of an AlphaList as a counter.  To
       make this explicit, this macros is used. */
    public int COUNT(AlphaList X);
    //public AlphaList X = new AlphaList();
    //int y = X.length;
    /* Use the 'mark' field of the header node of a list to indicate
       whether or not it will need to be destroyed later. The use of this
       is made explicit with this macro. */
    public int SHOULD_DESTROY(AlphaList X, int mark);

    /**********************************************************************/
    /********************   DEFAULT VALUES       **************************/
    /**********************************************************************/

    /**********************************************************************/
    /********************   EXTERNAL VARIABLES   **************************/
    /**********************************************************************/

    /**********************************************************************/
    /********************   EXTERNAL FUNCTIONS    *************************/
    /**********************************************************************/

    public void initIncPrune( );

    public void cleanUpIncPrune(  );

    /* The main incremental pruning algorithm routine for finding the
      Q-function represention for value iteration with POMDPs.  */
    public AlphaList improveIncPrune( AlphaList[] projection,
                                      PomdpSolveParams param );
  
}
