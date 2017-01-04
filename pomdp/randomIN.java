package pomdp;


public interface randomIN {
    
/**********************************************************************/
/********************       CONSTANTS       ***************************/
/**********************************************************************/

/* How big the array of short int's should be for the random seed */
public static final int SEED_SIZE  =  3;

/**********************************************************************/
/********************   EXTERNAL VARIABLES   **************************/
/**********************************************************************/

/**********************************************************************/
/********************   EXTERNAL FUNCTIONS    *************************/
/**********************************************************************/

/* Seeds the psuedo-random number generated if it has not already been
  seeded.  */
public void randomize();

/* Returns a uniform psuedo-random number between 0 and 1 */
public double fran();

/* Returns the current random seed.  Useful if you want to reproduce 
   the sequence (e.g., debugging) */
public static int[] seed= new int[SEED_SIZE] ;
public void getRandomSeed( /*unsigned*/ int[] seed );

/* Allows you to reproduce a psuedo-random sequence by explicitly 
   setting the seed value for the random number generator */
public void setRandomSeed( /*unsigned*/ short seed );

/* Sets seed from a string specification. */
public void setRandomSeedFromString ( String str );

/* Displays the current random seed to file stream. */
public void displayRandomSeed( );

/* Displays the current random seed on stdout. */
public void showRandomSeed( );

/* Returns a uniform psuedo-random number between min and max in the 
 form of a double precision number */
public double getRandomDouble( double min, 
                               double max );

/* Returns a uniform psuedo-random number between min and max in the 
 form of an integer */
public int getRandomInt( int min, 
                         int max );

/* This routine sets the discrete probability distribution so that
   each distribution is equally likely.  */
public double[] setRandomDistribution(int num_probs );

/* Sets the given vector of doubles with random values betwen the min
  and max values given.  'num' is the number of elements in the
  vector.  */
public void setRandomDoubleVector( double[] vect, 
                                   int num, 
                                   double min, 
                                   double max );

}
