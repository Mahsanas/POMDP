package pomdp;


public interface cross_sumIN {
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

/* Takes the cross sum of two sets of vectors and returns the
  resulting set. If either A or B is null, then NULL is returned.  If
  either list is empty, then an empty list is returned. The
  save_obs_sources argument deterines whther we do the bookkeeping
  required to develop a policy graph or not.  */
    public AlphaList crossSum( AlphaList A, 
                           AlphaList B, 
                           int save_obs_sources );
 
}
