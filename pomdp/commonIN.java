
package pomdp;
import pomdp.paramsIN.*;


public interface commonIN {
    /**********************************************************************/
    /********************       CONSTANTS       ***************************/
    /**********************************************************************/

    /**********************************************************************/
    /********************   DEFAULT VALUES       **************************/
    /**********************************************************************/

    /**********************************************************************/
    /********************   EXTERNAL VARIABLES   **************************/
    /**********************************************************************/

    /**********************************************************************/
    /********************   EXTERNAL FUNCTIONS    *************************/
    /**********************************************************************/

    /* Many places in the code can use temporary memory storage for
      calculations. This just allocates some useful data structures for
      this purpose.  */
    public void initCommon(  );

    /* Free up any temporary memory that was allocated.  */
    public void cleanUpCommon(  );

    /* Computes the Bellman residual between two successive value
       funcitons, finding the point of maximal difference between the two
       sets.  */
   /* public double bellmanError( AlphaList prev_list, 
                                AlphaList cur_list,
                                PomdpSolveParams param );

    /* This routine will first create all of the alpha vectors (one for
       each action) for this point in the global array gCurAlphaVector,
       then it will determine which one is best for this point.  It will
       return the best value and set the parameter 'action' to be the
       action that was best.  If there are ties...(they are currently
       deterministically broken) Assumes gValueType is reward.  */
    public double oneStepValue( double[] b, 
                                AlphaList[][] projection,
                                double epsilon );

    /* This routine will actually create the new alpha vector for the
       point 'b' sent in.  It will add the vector to the list if it is not
       already there, and either way it will return the pointer into
       new_alpha_list for the vector.  It first constructs all the vectors
       (for each action), then it finds which one is best (via dot
       product) finally it checks to see if the vector is in the list or
       not, and adds if if it isn't.  */
    public AlphaList makeAlphaVector( AlphaList new_alpha_list, 
                                      AlphaList[][] projection,
                                      double[] b,
                                      double epsilon );

    /* This initializes the given list with vectors that are constructed
      from the projection sets sent in at the belief simplex corners.
      Will loop through all the belief simplex vertices and add the
      vectors at these points to the list.  Only adds the vector if they
      are not already in the list and returns the number of vectors that
      were added. Essentially this routine just calls addVectorAtBeliefQ()
      for each simplex corner.  */
    public AlphaNode addVectorAtBeliefQ( AlphaList list, double[] belief,
                                         AlphaList[] projection,
                                         int save_witness_point,
                                         double epsilon );

    /* This initializes the given list with vectors that are constructed
      from the projection sets sent in at the belief simplex corners.
      Will loop through all the belief simplex vertices and add the
      vectors at these points to the list.  Only adds the vector if they
      are not already in the list and returns the number of vectors that
      were added. Essentially this routine just calls addVectorAtBeliefQ()
      for each simplex corner.  */
    public int initWithSimplexCornersQ( AlphaList list, 
                                        AlphaList[] projection,
                                        int save_witness_point,
                                        double epsilon );

    /* Will generate 'num_points' random belief points and add the vectors
      at these points to the list, if they are not already there.  */
    public int initWithRandomBeliefPointsQ( AlphaList list, 
                                            int num_points,
                                            AlphaList[] projection,
                                            int save_witness_point,
                                            double epsilon );

    /* For algorithms that search belief space incremntally (e.g.,
       witness, two-pass) adding vectors, we usually have the ability to
       initialize the set with vectors known to be in the final
       parsimonious set. This routine encapsulates all the ways in which
       this set could be initialized.  This includes checking the simplex
       corners and optionally checking an arbitrary set of random points.
       It uses the 'param' argument to decided how to initialize the
       set. This routine returns the number of vectors added.  */
    public int initListSimpleQ( AlphaList list, 
                                AlphaList[] projection,
                                PomdpSolveParams param );

   // public int shouldTerminateEarly( AlphaList list,  PomdpSolveParams param );

}
