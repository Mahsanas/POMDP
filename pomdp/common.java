package pomdp;
import mdp.globalMDP;
import pomdp.globalIN.*;
import pomdp.pomdpIN.*;
import pomdp.projectionIN.*;
import pomdp.paramsIN.*;
import pomdp.randomIN.*;
import pomdp.inc_pruneIN.*;
import pomdp.randomIN.*;
import pomdp.commonIN.*;

public class common implements commonIN{
  
    /* These two variables comprise a little tricky business that goes on
   for creating alpha vectors from belief points.  Since this is a
   common operation, we do not want to have to allocate memory each
   time we need to do this calculation.  Therefore, we will use these
   as global temporary work spaces.  Essentially, we want a vector for
   each action, but as we build the vectors, we also want to keep
   other information about it.  This information can be kept in a
   AlphaList node so we will have one of these for each action.  We
   create an array gCurAlphaVector so we can easily access them.
   Howevere, it will also prove useful to be able to deal with these
   vectors a an normal AlphaList (e.g., for finding the maximal
   vector.) For this reason we define gCurAlphaHeader to be a header
   node which will contain all those alpha vectors.  All this is all
   set up in the routine allocateCommonTempMemory() */
    int TRUE = 1, FALSE = 0;
    AlphaList gCurAlphaHeader = null;
    AlphaNode[] gCurAlphaVector = null;
    private region rg;
    private random rd;
    private alpha ap ;
    private global g;
    private  globalMDP gm;
    AlphaNode best_vector = new AlphaNode();
/**********************************************************************/
    //Constructor
    public common(globalMDP gm, alpha ap){
        this.gm = gm;
         this.ap = ap;
    }
    
/**********************************************************************/
    public common(region rg, random rd, alpha ap, global g, globalMDP gm){
        this.rg = rg;;
        this.rd = rd;
        this.ap = ap;
        this.g = g;
        this.gm = gm;
        this.ap = ap;
    }
/**********************************************************************/
    public void relinkObsSources( AlphaList list ) 
    {
      /* 
         Changes the obs_source array from pointing at projection nodes to
         pointing at the actual sources of the projection vectors in the
         previous iteration's alpha list.  During construction of the Q_a
         sets we need the obs_source array to point to the projection, but
         we will be destroying these sets.  However, the projection
         elements themselves have a pointer into the previous alpha list
         for which they were created from. Before destroying the projection
         sets, we want to redirect the choice pointers to their previous
         alpha list sources.  This is the basis for coming up with the
         policy graph. 
      */
      int z;

      assert( list != null): "List is NULL." ;
      if (list.length < 1 )
        return;
      
      AlphaNode temp = list.head;
      while ( temp != null && temp.obs_source!= null) { 
       
        for ( z = 0; z < gm.getgNumObservations(); z++ ) 
            if(temp.obs_source[z] != null)
            temp.obs_source[z] = temp.obs_source[z].prev_source;

        temp = temp.next;
      }  /* while */

    }  /* relinkObsSources */
/**********************************************************************/
    @Override
    public void initCommon() {
        /*
        Many places in the code can use temporary memory storage for
        calculations. This just allocates some useful data structures for
        this purpose.
      */
      int a;

      /* We want to have an AlphaList node for each action to temporarily
         hold all necessary information as we build vectors from belief
         points. In addition, we will want to treat this as a list.  We
         thus create an array of these nodes so we can access them as an
         array, but then also create a header node for them to be able to
         access them as a list. */
       gCurAlphaVector = new AlphaNode[gm.getgNumActions()];

       for ( a = 0; a < gm.getgNumActions(); a++ ) {
         gCurAlphaVector[a] = new AlphaNode( new double[gm.getgNumStates()], a );
         gCurAlphaVector[a].obs_source = new AlphaNode[gm.getgNumActions()];
       } /* for a */

       /* Make the array of nodes look like a list. */
       gCurAlphaHeader = new AlphaList();
       for ( a = 0; a < gm.getgNumActions(); a++ )
         ap.appendNodeToAlphaList( gCurAlphaHeader, gCurAlphaVector[a] );
    }
/**********************************************************************/
        @Override
        public void cleanUpCommon() {
        /*
        Free up any temporary memory that was allocated.
        */

      /* This will get rid of the individual nodes, the arrays they
         contain and the header as well. */
            ap.destroyAlphaList ( gCurAlphaHeader );
            gCurAlphaVector = null;
        }//end cleanUpCommon
       
/**********************************************************************/
   public int bestAlphaForBeliefQ( AlphaNode node, double[] b, 
                         AlphaList[] projection,
                         double epsilon ) 
    {
    /*
      Constructs the alpha vector for the point 'b' using the projection
      sets for a particular action.  It will use an existing AlphaList
      node with allocated 'alpha' and 'obs_source' fields.' Returns TRUE
      if the node had its values set and FALSE if there was a problem.
    */
       AlphaNode best_proj_vector;
       int i, z;
       double best_value = 0.0;

       /* In theory, for a properly specified POMDP, it should not be
             possible for all the projections to be NULL, since there has to be
             some observation that is possible. However, just as a sanity check,
             we will track to ensure this is true using this flag. */
       int non_null_proj = 0;

        assert ( ( node != null ) 
            && ( b != null )
            && ( projection != null )
            && ( node.alpha != null)
            && ( node.obs_source != null )):
            "Bad (NULL) parameter(s)." ;
       /* Initialize the alpha vector to all zeroes. */
       for ( i = 0; i < gm.getgNumStates(); i++ ) 
         node.alpha[i] = 0.0;

       /* Now pick out the best vector for the projection set for each
          observation.  The best overall vector is just the sum of these
          individual best vectors. */
       for ( z = 0; z < gm.getgNumObservations(); z++ ) {

         if ( projection[z] != null) {

              /* Find the best vector for all the observation 'z' projections.
                    If projection[z] is NULL, then this returns NULL. */
              best_proj_vector = ap.bestVector( projection[z], b, epsilon  );

              non_null_proj = 1;  /* The sanity check flag gets set to 'ok' */	  
            }

            else {
              /* By defnition, if projection[z] is NULL, then this means
            that the observation is not possible for this action. In
            this case, that observation will not contribute anything to
            the value of the state, so we can safely skip it.  Since it
            is impossible for all observations not to occur, we don't
            have to worry about all the projection[z] being NULL. Note
            that we also need to set the obs_source to NULL to indicate
            this. */
           node.obs_source[z] = null;
           continue;
         } /* if observation not possible */

         /* We want to see where each projection source came from and set
            this vectors obs_choice to that vector.  This gives us the
            policy graph information we desire. */
         node.obs_source[z] = best_proj_vector;

         /* Now add this best projection vector's component values to the
            components in the node. */
         for ( i = 0; i < gm.getgNumStates(); i++ ) 
           node.alpha[i] += best_proj_vector.alpha[i];

         /* Note that the immediate rewards have already been taken into 
            account for the projection vectors. */

       }  /* for z */

       /* Here is where we put the sanity check. */
      if ( non_null_proj == 0)
          System.out.println("All projections are NULL." );

       return ( TRUE );
    }  /* bestAlphaForBeliefQ */
   
/**********************************************************************/
    public int setBestAlphaForBeliefQ( double[] b, AlphaList[] projection, double epsilon ) 
    {
      /*
        Just uses the bestAlphaForBeliefQ() routine, but uses the global
        temporary variable gCurAlphaVector[0] to hold it.
      */

      /* Note that we are using gCurAlphaVector[0] just for the storage
         and that this does not necessarily mean we are computing for
         action '0'. */
      return ( bestAlphaForBeliefQ( gCurAlphaVector[0], b, projection,
                                    epsilon ));

    }  /* setBestAlphaForBeliefQ */
/**********************************************************************/
    public int setBestAlphasForBeliefV( double[] b, AlphaList[][] projection, double epsilon )
    {
      /*
        Loops through each action and constructs the alpha vector for the point
        'b' for each action.  It sets the global variable gCurAlphaVector
        which can then be used for other purposes (e.g., adding a vector
        to the list, finding the best action for the belief point, etc.)
        Returns TRUE if all vectors were created successfully and FALSE if
        any one of them had a problem.
      */
      int a;
      int result = 1;//TRUE = 1

      assert( b != null && projection != null):
          "Bad (NULL) parameter(s).setBestAlphasForBeliefV" ;
      for( a = 0; a < gm.getgNumActions(); a++ )
        result = result * bestAlphaForBeliefQ( gCurAlphaVector[a], b, 
                                  projection[a], epsilon );
      return ( result );
    }  /* setBestAlphasForBeliefV */

/**********************************************************************/        
    public double oneStepValue( double[] b, AlphaList[][] projection,
                  double epsilon ) 
    {
         
      /*
        This routine will first create all of the alpha vectors (one for
        each action) for this point in the global array gCurAlphaVector,
        then it will determine which one is best for this point.  It will
        return the best value and set the parameter 'action' to be the action
        that was best.  

        If there are ties...(they are currently deterministically broken)

        Assumes gValueType is reward (which is true when rewards are
        accessed through the getImmediateReward() routine in global.c.)
      */
      double best_value = 0.0;

      /* Construct the vector for each action and put them in global
         array gCurAlphaVector and gCurAlphaHeader list.
      */
      setBestAlphasForBeliefV( b, projection, epsilon );

      /* Use the global array as a list and get the best one. */
      best_vector = ap.bestVector( gCurAlphaHeader, b, epsilon );

      return( ap.getbest_value() );
    }  /* oneStepValue */
    
/**********************************************************************/        
    public AlphaList makeAlphaVector( AlphaList new_alpha_list, 
                     AlphaList[][] projection,
                     double[] b, double epsilon ) 
    {
      /* This routine will actually create the new alpha vector for the
         point 'b' sent in.  It will add the vector to the list if it is not
         already there, and either way it will return the pointer into
         new_alpha_list for the vector.

         It first constructs all the vectors (for each action), then it finds
         which one is best (via dot product) finally it checks to see if
         the vector is in the list or not, and adds if if it isn't.
      */
      
      AlphaNode new_alpha_node = null;

      /* This has the effect of creating the vectors in the global array
         gCurAlphaVector and returning an AlphaList pointer to the one
         that was best. We aren't really interested in the value here. */
      oneStepValue( b, projection, epsilon );

      /* See if this vector is already in the list or not. */
      ap.displayAlpha(best_vector.alpha);
      new_alpha_node = ap.findAlphaVector( new_alpha_list, 
                                        best_vector.alpha,
                                        epsilon );
      //ap.displayAlpha(new_alpha_node.alpha);
      if ( new_alpha_node != null )
        return ( null );

      /* Otherwise it isn't in the list yet. Here we are essentially just
         copying the stuff from the gCurAlphaVector temporary space to
         something we can use to put into a list.  Then we add it to the
         list. */
      new_alpha_list
        = ap.appendDuplicateNodeToAlphaList( new_alpha_list, best_vector );

      return( new_alpha_list );

    }  /* makeAlphaVector */
    
/**********************************************************************/        
    public AlphaNode addVectorAtBeliefQ( AlphaList list, 
                        double[] belief,
                        AlphaList[] projection,
                        int save_witness_point ,
                        double epsilon ) 
    {
      /*
        This routine will construct the vector for the belief point sent in
        and add this vector to 'list' if it is better than all the other
        vectors at this point.  The routine returns TRUE if the vector ws
        added.  Note that this routine will *not* remove vectors from
        'list'.  It assumes that anything in list has been demonstrated to
        be the best vector for at least some belief point.  This does this
        considering only the action for the projections sent in.

        Returns a pointer to the new node added.
      */
      AlphaNode node;

       assert ( ( list != null )
           && ( belief != null )
           && ( projection != null )) :
           "Bad (NULL) parameter(s).addVectorAtBeliefQ" ;


      /* Construct the vector for this point. Note that we are using
         gCurAlphaVector[0] just for the storage and that this does
         not necessarily mean we are computing for action '0'. */
      setBestAlphaForBeliefQ( belief, projection, epsilon );

      /* So we know that at this point, the vector stored in
         gCurAlphaVector[0] is the absolute best vector. The only question
         now is whether or not this vector is already in the list or
         not. */

      node = ap.findAlphaVector( list, gCurAlphaVector[0].alpha,
                              epsilon );

      if ( node != null )
        return ( null );

      /* This will make a copy of the node with the vector and append it
         to the list. */
      list = ap.appendDuplicateNodeToAlphaList( list, gCurAlphaVector[0] );

      /* Want every vector we add to have the proper action set. Since the
         projection sets have the action stored, we can use this to tell
         what action we are processing. */
      node.action = projection[0].head.action;

      /* If we have specified the use-witness-points option, then we will
         save this point as a witness for this vector. */
      if ( save_witness_point == TRUE )
        ap.addWitnessToAlphaNode( node, belief );

      return ( node );

    }  /* addVectorAtBeliefQ */
    
/**********************************************************************/
        @Override
    public int initWithSimplexCornersQ( AlphaList list, AlphaList[] projection,
                             int save_witness_point,
                             double epsilon ) 
    {
      /*
        This initializes the given list with vectors that are constructed
        from the projection sets sent in at the belief simplex corners.

        Will loop through all the belief simplex vertices and add the
        vectors at these points to the list.  Only adds the vector if they
        are not already in the list and returns the number of vectors that
        were added. Essentially this routine just calls addVectorAtBeliefQ()
        for each simplex corner. 
      */
      int i;
      int num_added = 0;
      
      assert( list != null && projection != null):
              "Bad (NULL) parameter(s) at initWithSimplexCornersQ.";
      /* We will actually need a belief point to generate the vector, so
         we will initialize it to all zeroes and set each component to 1.0
         as we need it. */
      for( i = 0; i < gm.getgNumStates(); i++ ) 
        g.setgTempBelief(i, 0.0);

      for( i = 0; i < gm.getgNumStates(); i++ ) {

        /* In normal initWithSimplexCorners() we loop over the full list
           to find the vector that has the highest value for component
           'i'.  Here we just need to see if the vector generated from
           this simplex corner is any better than what we have. */

        /* Set this so we actually have a simplex corner in 'b'. */
        g.setgTempBelief(i, 1.0);//gTempBelief[i] = 1.0;

        /* Note that if we are using the option of saving witness points,
           this addVectorAtBeliefQ() will do this. */
        if ( addVectorAtBeliefQ( list, g.getgTempBelief(), projection,
                                 save_witness_point, epsilon  ) != null )
          num_added++;

        /* Clear the 'i'th component so we maintain a belief corner point
            during i+1. */
          g.setgTempBelief(i, 0.0);//gTempBelief[i] = 0.0;

       }  /* for i */

      return ( num_added );

    }  /* initWithSimplexCornersQ */
        
/**********************************************************************/
        @Override
    public int initWithRandomBeliefPointsQ( AlphaList list, int num_points,
                                 AlphaList[] projection,
                                 int save_witness_point,
                                 double epsilon) 
    {
      /*
        Will generate 'num_points' random belief points and add the vectors
        at these points to the list, if they are not already there.
      */
      int i;
      int num_added = 0;
      
      assert ( ( list != null )
               && ( projection != null )):
               "Bad (NULL) parameter(s).initWithRandomBeliefPointsQ" ;
      
      if ( num_points < 1 )
        return ( 0 );

      for( i = 0; i < num_points; i++ ) {

        /* Get a random belief point, uniformly distributed over the
           belief simplex. */
        rd.setRandomDistribution( gm.getgNumStates() );

        /* Note that if we are using the option of saving witness points,
           this addVectorAtBeliefQ() will do this. */
        if ( addVectorAtBeliefQ( list, g.getgTempBelief(), projection,
                                 save_witness_point, epsilon ) != null )
          num_added++;

      }  /* for i */

      return ( num_added );

    }  /* initWithRandomBeliefPointsQ */
        
/**********************************************************************/
        @Override
    public int initListSimpleQ( AlphaList list, 
                     AlphaList[] projection,
                     PomdpSolveParams param ) 
    {
      /*
        For algorithms that search belief space incremntally (e.g., witness,
        two-pass) adding vectors, we usually have the ability to initialize
        the set with vectors known to be in the final parsimonious set. This
        routine encapsulates all the ways in which this set could be
        initialized.  This includes checking the simplex corners and
        optionally checking an arbitrary set of random points.  It uses the
        'param' argument to decided how to initialize the set. This routine
        returns the number of vectors added.
      */

      /* Initialize using simplex corners. Assume we always want
         this. Note that if we only get one vector, then this vector is
         maximal at every belief simplex vertex and thus must be maximal
         everywhere. For this case, we can bail out right here knowing
         that the Q-function list must be of size '1'. */
      if ( initWithSimplexCornersQ( list, 
                                    projection,
                                    param.use_witness_points,
                                    param.alpha_epsilon ) < 2 )
        return( list.length );

      /* Use random points to initialize the list, but this will only do
         something if param->init_rand_points > 0 */
      initWithRandomBeliefPointsQ( list, 
                                   param.alg_init_rand_points,
                                   projection,
                                   param.use_witness_points,
                                   param.alpha_epsilon );

      return ( list.length );

    }  /* initListSimple */

   
    
}
