package pomdp;
import pomdp.globalIN.*;
import pomdp.randomIN.*;
import java.io.Serializable.*;
import java.util.Random;

public class random implements randomIN{

    /* Globally keep the seed */
    static  int[] random_seed48 = new int[SEED_SIZE];
    /* static short random_seed48[SEED_SIZE]; */

    /* To keep track of whether we have initialized the psuedo random 
       number generator. */
    static int seeded = 0;
    /* static int seeded = 0; */
    global g = new global();
    /**********************************************************************/
    public static int create(int maxnum)
    {
        Random rd = new Random();
        long temp = rd.nextLong();
      //return nrand48(random_seed48)%Math.max(1,maxnum);
      return (int) temp%Math.max(1,maxnum);
    }  /* create */
    /**********************************************************************/
    public static void init_randomizer()
    {

      int i;
 
      for (i=0;i<87;++i)            /* exercise out any startup transients */
        create(10);

      seeded = 1;
    }  /* init_randomizer */
    /**********************************************************************/
        @Override
    public void randomize()
    {
    /*
      Seeds the psuedo-random number generated if it has not already been
      seeded. 
    */
      if( seeded == 0 )
        init_randomizer();
    }  /* randomize */
    /**********************************************************************/
        @Override
    public double fran() 
    { 
      /* Returns a uniform psuedo-random number between 0 and 1 */

      if( seeded == 0 )
        init_randomizer();
        
      return Math.random();//erand48(random_seed48);
    }  /* fran */
    /**********************************************************************/
        @Override
    public void getRandomSeed(  int[] seed ) 
    {
      /* Returns the current random seed.  Useful if you want to reproduce 
         the sequence (e.g., debugging) */
      int i;

      for( i = 0; i < SEED_SIZE; i++ )
        seed[i] = random_seed48[i];

    }  /* getRandomSeed */
    /**********************************************************************/
    public void setRandomSeed( int[] seed ) 
    {
      /* Allows you to reproduce a psuedo-random sequence by explicitly 
         setting the seed value for the random number generator */
      int i;

      for( i = 0; i < SEED_SIZE; i++ )
        random_seed48[i] = seed[i];

      seeded = 1;
    }  /* getRandomSeed */
    /**********************************************************************/
        @Override
    public void setRandomSeedFromString (String str ) 
    {
      int[] s = new int[SEED_SIZE];
      int i;

      /* zzz Need to add something to check for too many and too few
         values. Also need to make this a function of SEED_SIZE and not
         hard coded as '3'. */  

      //sscanf( str, "%d:%d:%d", &s[0], &s[1], &s[2] );	
      s[0] = str.charAt(0);
      s[1] = str.charAt(1);
      s[2] = str.charAt(2);
      for( i = 0; i < SEED_SIZE; i++ )
        random_seed48[i] = (int) s[i];

    }  /* setRandomSeedFromString  */
    /**********************************************************************/
        @Override
    public void displayRandomSeed( ) 
    {
      /* 
         Display random seed to file stream. 
      */
      int i;

      System.out.println((int) random_seed48[0] );
      for( i = 1; i < SEED_SIZE; i++ )
        System.out.println((int) random_seed48[i] );

    }  /* displayRandomSeed */
    /**********************************************************************/
        @Override
    public void showRandomSeed(  ) 
    {
      /* 
         Display random seed to stdout. 
      */
      //fprintf( stdout, "\t" );
      displayRandomSeed( );
      //fprintf( stdout, "\n" );

    }  /* showRandomSeed */
    /**********************************************************************/
        @Override
    public double getRandomDouble( double min, double max ) 
    {
      /* Returns a uniform psuedo-random number between min and max in the 
         form of a double precision number */
      return( fran() * (max - min) + min );
    }  /* getRandomDouble */
    /**********************************************************************/
        @Override
    public int getRandomInt( int min, int max ) 
    {
      /* Returns a uniform psuedo-random number between min and max in the 
         form of an integer */
      return( ((int) (fran() * (max - min + 1 ))) + min );
    } /* getRandomInt */
    /**********************************************************************/
    /* This routine sets the discrete probability distribution so that
       each belief state is equally likely.  */
    
    public double[] setRandomDistribution(int num_probs ) 
    {
      /* This routine sets the discrete probability distribution so that
         each distribution is equally likely.  */
      int i, j;
      double[] x = new double[num_probs];
       x[0] = 1.0;

       for( i = 1; i < num_probs; i++ ) {
          x[i] = 1.0 - Math.exp( 1.0/i * Math.log( getRandomDouble( 0.0, 1.0)) );

          for( j = 0; j < i; j++ )
             x[j] *= 1.0 - x[i];
       }  /* for i */
       g.setgTempBelief(x);
       return x;
    }  /* setRandomDistribution */
    /**********************************************************************/
        @Override
    public void setRandomDoubleVector( double[] vect, int num, 
                           double min, double max ) 
    {
      /*
        Sets the given vector of doubles with random values  betwen the min
        and max values given.  'num' is the number of elements in the
        vector.
      */
      int i;

      for ( i = 0; i < num; i++ )
        vect[i] = getRandomDouble(min, max );

    }  /* setRandomDoubleVector */

    @Override
    public void setRandomSeed(short seed) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
/**********************************************************************/
}
