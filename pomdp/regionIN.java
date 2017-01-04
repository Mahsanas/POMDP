package pomdp;

import pomdp.paramsIN.PomdpSolveParams;

public interface regionIN {
    /**********************************************************************/
/********************       CONSTANTS       ***************************/
/**********************************************************************/

/* There are two ways to set up a region LP.  The original way and the
   way proposed by Bob Givan.  There is a global constant, when is set
   when you want to use the Givan approach. This results in
   conditional compilation of some parts of lp-interface.c. */

/*  #define USE_OLD_LP_FORMULATION  */


/* We can choose between representing things using the sparse LP
   representation or making the thing dense, adding in the zero
   entries. Defining this variable forces the use fo dense LP
   representation. */

/*  #define USE_DENSE_LPS  */

/**********************************************************************/
/********************   DEFAULT VALUES       **************************/
/**********************************************************************/

/**********************************************************************/
/********************   EXTERNAL VARIABLES   **************************/
/**********************************************************************/

/**********************************************************************/
/********************   EXTERNAL FUNCTIONS    *************************/
/**********************************************************************/

/* Checks to see if the alpha vector 'alpha' has a non-empty region
  (measurable area) where it is better than all the other vectors in
  the 'list'. If the region is non-empty the routine returns TRUE with
  the witness_point set to a point in that region.  If there is no
  point where alpha is better, then FALSE is returned.  */
public int findRegionPoint( double[] alpha, 
                            AlphaList list, 
                            double[] witness_point, boolean flag,
                            PomdpSolveParams param );

}
