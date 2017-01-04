package pomdp;
import Enum.ContextType;
import Enum.PurgeOption;
import Enum.StopCriteria;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mdp.globalMDP;
import mdp.imm_reward;
import mdp.mdp;
import pomdp.paramsIN.PomdpSolveParams;
import pomdp.statsIN.EpochStats;


public class pomdp_solve implements pomdp_solveIN{
    /**********************************************************************/
    /************* Routines for beginning and end of solving   ************/
    /**********************************************************************/

    /**********************************************************************/
    String solutionFile = "results/Solution.txt";
    BufferedWriter out;
    int FALSE = 0, TRUE = 1;
    double HUGE_VAL = Double.POSITIVE_INFINITY;
    mdp MDP;
    globalMDP gm;// = new globalMDP();
    common cm ;//= new common();
    global g ;//= new global();
    alpha ap ;//= new alpha();
    inc_prune ip ;//= new inc_prune();
    //lp_interface lp ;//= new lp_interface();
    pomdp POMDP ;//= new pomdp();
    params pm ;//= new params();
    stats st;// = new stats();
    projection pj;// = new projection();
   // signal_handler sh ;//= new signal_handler();
    region rg; random rd;
    policy_graph pg;
   // pg PG ;//= new pg();
    parsimonious psm ;//= new parsimonious();
    timing t;
    imm_reward ir;
    public pomdp_solve(){
        gm = new globalMDP();
        g = new global(gm);
        MDP = new mdp(gm);
        POMDP = new pomdp(MDP, gm, g);
        ap = new alpha(gm, g,MDP,POMDP);
       
       // lp = new lp_interface();
        rg = new region(gm);
        rd = new random();
        pg = new policy_graph(gm);
        //sm = new sparse_matrix();
        
        
        cm = new common(rg, rd, ap, g, gm);
        
        psm = new parsimonious(ap, rg, rd, g, gm);
        
        
        
        t = new timing();
        st = new stats( g,  t);
        pm = new params();
        pj = new projection(ap, gm, g);
        //sh = new signal_handler();
        ip = new inc_prune(cm, ap,  psm, rg,  gm, g);        
    }
     
        @Override
    public void initPomdpSolve( PomdpSolveParams param ) 
    {
      String msg ="";

      /* We need to do this first, since there are other things to come
         which need to know the POMDP parameters (or at least the sizes
         of the states, action, observation, etc. Will also do the
         precomputation of which observations are possible, so we need to
         provide the epsilon value to use to determine this. */
      System.out.println( "[Initializing POMDP ... " );
      //if(param.param_filename != null)
      POMDP.initializePomdp( param.param_filename, 
                       param.impossible_obs_epsilon );
     
      
     System.out.println(  "done.]\n" );

       /* We allow the discount factor to be over-ridden by a command line
          option.  To make sure we get the right discount, we have to
          check for the over-ridden value *AFTER* we parse the POMDP
          file, which is done in the initializePOMDP() routine. If
          we do override it, just set its value to the new value. */
       if ( param.override_discount >= 0.0 ) 
         gm.setgDiscount( param.override_discount);

       /* We will save the solution after each iteration in a temporary
          file, so that if the program terminates abnormally, we can
          recover the latest solution. We will use the PID of the file to
          make sure the filename is unique so multiple copies can run at
          the same time. Note that for POSIX, the 'pid_t' type returned by
          getpid() is an 'int'. */
       //sprintf( param->backup_file, SAVE_FILE_NAME_FORMAT, getPid() );

       /* For saving penultimate alpha file (if selected). */
      /* param.penultimate_filename = param.opts.prefix_str ;
       param.penultimate_filename = param.penultimate_filename + PENULTIMATE_SUFFIX;
       param.penultimate_filename = param.penultimate_filename + ALPHA_FILE_SUFFIX;

       /* Set the output file names based upon the gPrefixStr */
       //param.alpha_filename = param.opts.prefix_str ;
      /* param.alpha_filename = param.alpha_filename + ALPHA_FILE_SUFFIX;
       param.pg_filename = param.pg_filename + param.opts.prefix_str ;
       param.pg_filename = param.pg_filename + PG_FILE_SUFFIX ;*/

       if ( param.initial_policy_filename != null) {
            try {
                if (( param.initial_policy 
                      = ap.readAlphaList( param.initial_policy_filename, 
                                       -1 )) == null) {
                  msg ="Cannot open initial policy file name: " + param.initial_policy_filename;
                  System.out.println( msg );
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(pomdp_solve.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(pomdp_solve.class.getName()).log(Level.SEVERE, null, ex);
            }
       }

       /* Do the right initialization for whichever algorithm was chosen.
          We really want to only call appropriate initialization, but we
          may need Lark for the purging of the S_z sets.  Therefore, to
          ensure that we always have the proper memory allocated, we call
          initLark() in all algorithms. */
         ip.initIncPrune( );
        
       /* Some of the routines in common.c need a hunk of memory to
          compute things, thus we allocate this memory once up front to
          save the malloc/free calls. */ 
       cm.initCommon();

       /* Set up any globally used things just before solving. Must do
          this after we have read in the POMDP problem, since it uses the
          problem size when allocating memory. */
       g.initGlobal();

    }  /* initPomdpSolve */
    /**********************************************************************/
    @Override
    public void cleanUpPomdpSolve( PomdpSolveParams param ) 
    {
      /*Undo whatever incPrune algorithm does*/
      ip.cleanUpIncPrune( ); 

      /* Undo whatever initCommon() does. */
      cm.cleanUpCommon();

      /* Undo whatever initGlobal() does. */
      g.cleanUpGlobal();

      /* Deallocate the POMDP problem parameters. */
      POMDP.cleanUpPomdp();

      /* Don't need this structure anymore. */ 
      pm.destroyPomdpSolveParams ( param );

    }  /* cleanUpPomdpSolve */
    /**********************************************************************/
   public void endPomdpSolve( PomdpSolveParams param,
                   AlphaList solution ) 
    {
      /* 
         The gSuccinct variable is used when we want a very concise reporting
         of the results of the program.  This is useful for running series
         of experiments and combining the results. Thus, we only output
         out final report if we are not being brief. 
      */
      AlphaList scaled_list;

      assert( param != null && solution != null): 
          "Bad (NULL) parameters.endPomdpSolve";

     /* Write the solution files, but note that this must be done before
     we clear the prev_alpha_list to ensure policy graph information
     is preserved. */
     // ap.writeAlphaList( param.alpha_filename, solution );
    // pg.writePolicyGraph("outpolicy.pg",solution );

     

      /* Show all program stats depending upon execution parameters.. */
      st.reportStats( param.stat );

    }  /* endPomdpSolve */
    /**********************************************************************/
    public AlphaList getDefaultInitialPolicy(  ) 
    {
      /*
        For now our default policy is just all zeroes for exact solving,
        and for finite grid it is gotten from that module since it needs
        to ensure the starting values are a lower bound on the solution.
      */
      AlphaList alpha_list;
      double[] alpha;
      int i;

        alpha_list = ap.newAlphaList();

        alpha = ap.newAlpha();
        for ( i = 0; i < gm.getgNumStates(); i++ )
             alpha[i] = 0.0;

        ap.appendAlphaList( alpha_list, alpha, 0 );

        return ( alpha_list );

    }  /* getInitialSolution */
    /**********************************************************************/
   public double weakBound( AlphaList cur_alpha_list,
                  AlphaList prev_alpha_list,
                  double delta ) {
/*
  Computes the weak bound error difference between two (the current
  and previous)  alpha lists.  Used as a form of stopping condition
  for value iteration.
*/
   double max_p_x, min_p_y, max_s, val;
   AlphaNode p_x, p_y;
   int s;

  assert( prev_alpha_list != null && cur_alpha_list != null):
          "Bad (NULL) parameter(s).";

   max_p_x = -1.0*HUGE_VAL;
   p_x = cur_alpha_list.head;
   while( p_x != null) {

      min_p_y = HUGE_VAL;
      p_y = prev_alpha_list.head;
      while( p_y != null ) {

         max_s = -1.0*HUGE_VAL;
         for ( s = 0; s < gm.getgNumStates(); s++ ) {

            val = Math.abs( p_x.alpha[s] - p_y.alpha[s] );

            if ( val > max_s )
               max_s = val;
         }
            
         if ( max_s < min_p_y )
            min_p_y = max_s;

         p_y = p_y.next;
      }  /* while p_y */

      if ( min_p_y > max_p_x ) 
         max_p_x = min_p_y;
      
      /* This is an optimization to exit the loop early */
      if ( max_p_x > delta )
         return HUGE_VAL;

      p_x = p_x.next;
   }  /* while p_x */

   return ( max_p_x );
}  /* weakBound */

    /**********************************************************************/
    public int meetStopCriteria( AlphaList prev_alpha_list, 
                               AlphaList cur_alpha_list,
                               PomdpSolveParams param ) 
    {
      /* 
         Determines whether or not we can stop value iteration.  There are
         different stopping criteria and we want to see if the one selected
         is met.  This is done by comparing the current and previous value
         function (set of alpha vectors).  
      */

      assert( prev_alpha_list != null && cur_alpha_list != null):
          "Bad (NULL) parameter(s).";

       if(param.stop_criteria == StopCriteria.stop_exact ) 
           return ( ap.sameAlphaList( prev_alpha_list, 
                        cur_alpha_list,
                        param.alpha_epsilon ));
       if(param.stop_criteria == StopCriteria.stop_weak){
           if ( weakBound( cur_alpha_list, prev_alpha_list,
                param.weak_bound_delta ) <= param.weak_bound_delta )
              return TRUE;
            else
              return FALSE;

       }
       return FALSE;
    }  /* meetStopCriteria */
    /**********************************************************************/
    public double getSolvePrecision( PomdpSolveParams param ) 
    {
      /*
        This routine will get the precision factor to use during solving of
        the POMDP.  Exactly what it does depends on the variation of value
        iteration.  For normal operation, we use the param->epsilon value
        directly.  For the epsilon pruning of the q-functions version, we
        use the pruning epsilon.  
      */

      if ( param.q_purge_option == PurgeOption.purge_epsilon_prune ) 
        return (  param.prune_epsilon );

      else
        return ( param.epsilon );

    }  /* getSolvePrecision */
    /**********************************************************************/
    public void setSolvePrecision( double epsilon, PomdpSolveParams param ) 
    {
      /*
        This routine will set the precision factor to use during solving of
        the POMDP.  Exactly what it does depends on the variation of value
        iteration.  For normal operation, changing the precision
        actually involves changing a lot of parameters. For the epsilon
        pruning of the q-functions version, we only need to change the
        pruning epsilon. 
      */

      if ( param.q_purge_option == PurgeOption.purge_epsilon_prune )
        param.prune_epsilon = epsilon;

      else {

        param.epsilon = epsilon;

        param.lp_epsilon = Math.min( param.lp_epsilon, param.epsilon );

       // lp.LP_setPrecision( param.lp_epsilon );

        /* param->alpha_epsilon = param->epsilon; */
        param.vertex_epsilon = param.epsilon;
        param.double_equality_precision = param.epsilon;

        param.impossible_obs_epsilon = Math.min( param.epsilon, 1E-9 );//DEFAULT_IMPOSSIBLE_OBS_EPSILON = 1E-9        
      } /* else using non epsilon prune of Q functions */

    }  /* setSolvePrecision */
    /**********************************************************************/
    public void doAdjustableEpsilonVariation( PomdpSolveParams param ) 
    {
      
      double cur_epsilon;
      int epoch, min_vects, max_vects;
      EpochStats epoch_stats;

      assert ( param != null):
           "NULL parameters.doAdjustableEpsilonVariation" ;

      /* Cannot do the adjustable epsilon if there are no stats because we
         need to access the epoch stats to tell when and how to adjust the
         epsilon. */
      if ( param.stat == null )
        return;

      /* If we are using the epsilon pruning for the Q sets, then we will
         adjust the epsilon prune parameter.  Otherwise, we will adjust
         the main program epsilon parameter directly. */
      cur_epsilon = getSolvePrecision( param );

      /* First log in the epsilon that was used for this epoch before
         making any changes. */
      st.recordEpochMaxEpsilon( param.cur_epoch, cur_epsilon, param.stat );

      /* If we have reached the end epsilon, then we don't have to worry
         about adjusting the epsilon at all. */
      if ( cur_epsilon <= param.ending_epsilon )
        return;

      /* If there haven't been enough epochs to have enough history for
         computing whether or not we should adjust the epsilon, then just
         bail out also. */
      if ( param.cur_epoch < param.epoch_history_window_length )
        return;

      /* Now we get the minimum and maximum sizes of the last bunch of
         history of the vector sizes to determine if we can adjust the
         epsilon downwards or not. */
      max_vects = 0;
      min_vects = 99999999;
      for ( epoch = (param.cur_epoch 
                     - param.epoch_history_window_length
                     + 1);
            epoch <= param.cur_epoch;
            epoch++ ) {

        epoch_stats = st.getEpochStats( param.stat, epoch );

        /* This really shouldn't return NULL, but in case it does, give a
           warning and bail out. */
        if ( epoch_stats == null ) {
          System.out.println( "Could not get the epoch stats." );
          return;
        } /* if couldn't find the epoch stats. */

        /* Maintain the maximum and minium vectors for each epoch */
        min_vects = Math.min( min_vects, epoch_stats.solution_size );
        max_vects = Math.max( max_vects, epoch_stats.solution_size );

      } /* for epoch */

      /* If the max and the min differ by more than the desired amount,
         then we do not need to adjust the epsilon. */
      if ( (max_vects - min_vects) > param.epoch_history_window_delta )
        return;

      /* If we get to here, then that means we should decreemnt the
         epsilon. */
      cur_epsilon = param.epsilon_adjust_factor;

      /* If we are using the epsilon pruning for the Q sets,
         then we will adjust the epsilon prune parameter.  Otherwise, we
         will adjust the main program epsilon parameter directly. */
      setSolvePrecision( cur_epsilon, param );

      System.out.println( param.report_filename+
               ">>Adjusted epsilon to << "+ cur_epsilon );

    }  /* doAdjustableEpsilonVariation */
    /**********************************************************************/
    public void  doFixedSolnSizeVariation( PomdpSolveParams param ) 
    {
      /*
        zzz Add description here when it solidifies.
      */
      double cur_epsilon;
      assert ( param != null):
           "NULL parameters." ;

      /* Cannot do the fixed soln size variation if there are no stats
         because we need to access the history of vector sizes to tell
         when and how to adjust the epsilon. */
      if ( param.stat == null )
        return;

      /* If we are using the epsilon pruning for the Q sets, then we will
         adjust the epsilon prune parameter.  Otherwise, we will adjust
         the main program epsilon parameter directly. */
      cur_epsilon = getSolvePrecision( param );

      /* First log in the epsilon that was used for this epoch before
         making any changes. */
      st.recordEpochMaxEpsilon( param.cur_epoch,
                               cur_epsilon,
                               param.stat );

      System.out.println("doFixedSolnSizeVariation() says:" );
      System.out.println("!!! Implement me !!!" );
      System.exit( 0 );

    }  /* doFixedSolnSizeVariation */
    /**********************************************************************/
    public void startViEpoch( PomdpSolveParams param ) 
    {
      /* 
         This is called at the beginning of each epoch of value iteration so
         that value iteration variations can adjust anything that needs it
         prior to the next iteration. 
    *  */

      /* One thing we always will adjust is the epoch number. */
      (param.cur_epoch)++;
      
      switch ( param.vi_variation ) {
      case NormalVi:
        /* Normal value iteration doesn't need to do anything else. */
        return;

      case AdjustableEpsilonVi:
      case FixedSolnSizeVi:
        /* If this is not the first epoch, then we do not need to do
           anything else. */
        if ( param.cur_epoch != 1 ) 
          return;

        /* If this is the first epoch, then we need to set the starting
           epsilon. If we are using the epsilon pruning for the Q sets,
           then we will adjust the epsilon prune parameter.  Otherwise, we
           will adjust the main program epsilon parameter directly. */
        setSolvePrecision( param.starting_epsilon, param );

        System.out.println( param.report_filename+
                 ">>Starting epsilon set to %.3e<<\n"+
                 param.starting_epsilon );

        return;

      default:
        break;
      } /* switch */

    }  /* startViEpoch */
    /**********************************************************************/
    public void endViEpoch( PomdpSolveParams param ) 
    {
      /*
        This is called at the end of each epoch of value iteration so that
        value iteration variations can adjust anything that needs it prior
        to the next iteration. 
      */

      switch ( param.vi_variation ) {
      case NormalVi:
        /* Normal value iteration doesn't need to do anything. */
        return;

      case AdjustableEpsilonVi:
        doAdjustableEpsilonVariation( param );
        return;

      case FixedSolnSizeVi:
        doFixedSolnSizeVariation( param );
        return;

      default:
        break;
      } /* switch */

    }  /* endViEpoch */
    /**********************************************************************

    /**********************************************************************/
    /**************     High Level Solution Routines      *****************/
    /**********************************************************************/

    public AlphaList improveByQ( AlphaList[][] projection,
                PomdpSolveParams param ) 
    {
      /* 
         Some algorithms will solve one iteration of POMDP value iteration
         by breaking the problem into a separate one for each action.
         This routine will implement the basic structure needed and call the
         appropriate routines depending on the specific algorithm being used.

         Current algorithms that do it this way:
           TwoPass
           Witness
           IncrementalPruning
    */
      AlphaList new_list , cur_list = ap.newAlphaList();
      int a;
      int start_lps = 0, start_constraints = 0, end_lps = 0, end_constraints = 0;

      /* Nothing to do if no projections. */
     assert ( projection != null && param != null):
           "Bad (NULL) parameters.improveByQ";

      new_list = ap.newAlphaList();
    
      for( a = 0; a < gm.getgNumActions(); a++ ) {
       
        st.startContext( param.stat, ContextType.Context_Q_a_build );
       
        /*******Using Incprune***************************************************************************************/
        cur_list = ip.improveIncPrune( projection[a], param );
        System.out.println("new List for action "+a);
        ap.displayAlphaList(cur_list);
        /************************************************************************************************************/
        st.endContext( param.stat, ContextType.Context_Q_a_build );
        /* Must do this *after* referencing cur_list since this is a
           destructive union that will obliterate cur_list. */
        new_list = ap.unionTwoAlphaLists( new_list, cur_list );
        cur_list = null;

      }  /* for a */
      System.out.println("after inc pruning ");
      ap.displayAlphaList(new_list);
     st.startContext( param.stat, ContextType.Context_Q_a_merge );
     new_list = psm.purgeAlphaList( new_list, 
                      param.q_purge_option, 
                      param );
     System.out.println("after another purge Alphalist");
     ap.displayAlphaList(new_list);
      /* If we used epsilon pruning, then record the computed difference
         which was temporarily stored in the 'param' structure. */
      if (( param.stat != null ) 
          && ( param.q_purge_option == PurgeOption.purge_epsilon_prune ))
        st.recordEpochMaxEpsilon( param.stat.cur_epoch,
                               param.epsilon_diff_of_last_prune,
                               param.stat );

      st.endContext( param.stat, ContextType.Context_Q_a_merge );
      return ( new_list );

    }  /* improveByQ */
    /**********************************************************************/
    @Override
    public AlphaList improveV( AlphaList prev_alpha_list,
                    PomdpSolveParams param ) 
    {
      /*
        This does a single DP step of value iteration for a POMDP.  It takes
        in the previous value function and parameters for solving and
        returns the next or improved solution.  
      */
      AlphaList next_alpha_list = null;
      AlphaList[][] projection;

     assert( prev_alpha_list != null && param != null):
          "Bad (NULL) parameters." ;

      /* No matter what the algorithm, we will use the projection sets
         to construct the solutions, so make all the projections
         vector sets now. */
      st.startContext( param.stat, ContextType.Context_Projection_build );
      projection = pj.makeAllProjections( prev_alpha_list );
      System.out.println("Display all projections");
      pj.displayProjections(projection);
      st.endContext( param.stat, ContextType.Context_Projection_build );
     
      st.startContext( param.stat, ContextType.Context_Projection_purge );
      projection = psm.purgeProjections( projection, param );
      System.out.println("Display Projections after purge Projection " +projection.length);
      pj.displayProjections(projection);
      st.endContext( param.stat, ContextType.Context_Projection_purge );
        /* The witness, incremental pruning and two-pass algorithms
           construct the next alpha list one action at a time.  As a
           result, they share a lot of common structure.  Therefore,
           we will just call the improveByQ() routine which will do
           the right thing for each algorithm. */
      next_alpha_list = improveByQ( projection, param );
      
      /* While building the next alpha list, the 'obs_source' points
         into the projection vectors, but now we want them to point
         directly into prev_alpha_list for purposes of the policy
         graph stuff. */
     // ap.displayAlphaList(next_alpha_list);
      cm.relinkObsSources( next_alpha_list );

      /* Having redirected the obs_source pointer in next_alpha_list
         from projection to prev_alpha_list, we can now free up the
         memory for the projections, since we no longer need them and
         we don't have to worry about leaving pointers to nowhere. */
      pj.freeAllProjections( projection );

      return ( next_alpha_list );

    }  /* improveV */
    /**********************************************************************/
    @Override
    public void solvePomdp( PomdpSolveParams param ) throws IOException
    {
      /*
        If horizon < 0 then it will run until it converges or until
        SIGINT signal is received.  If initial_policy is NULL, then the default
        initial policy will be used.
      */
      // ArrayList<AlphaList> sol = new ArrayList<AlphaList>();
      AlphaList prev_alpha_list = null;
      AlphaList next_alpha_list = ap.newAlphaList();
      boolean done = false;//FALSE
      String alpha_filename = null,pg_filename;
      double cur_error = 0.0 ;
      out = new BufferedWriter(new FileWriter(solutionFile));
      assert( param != null):
          "Parameter structure is NULL.";


      /* Set up name for saving alpha vectors and policy tree
         in case -save_all flag used.  Calling this routine with a NULL
         list serves to initialize it. */
      if ( param.save_all == 1 ){
        alpha_filename = param.alpha_filename;
         pg_filename = param.pg_filename;

        /* This is a memory address calculation to provide a pointer into
           the strings where the NULL terminator exists.  We will be
           appending unique ids for each epoch and want to just
           over-write the suffix each time. */
      }
        

      /* Get the initial policy/value function to use. Note that we set
         these to next_alpha_list because the first thing the loop does
         is to swap in the next_alpha_list for the current_alpha_list. */
      if ( param.initial_policy == null )
        next_alpha_list = getDefaultInitialPolicy( );
      else
        next_alpha_list = ap.duplicateAlphaList( param.initial_policy );
      //ap.displayAlphaList(next_alpha_list);
     // sol.add(next_alpha_list);
      /* Just report the initial policy used. */
     System.out.println( "[Initial policy has: "+ ap.sizeAlphaList( next_alpha_list) +" vectors.] ");
         
      /* Make a structure to hold a place to accumulate the solution
         statistics and initialize the global solution time and counters.
         Note this needs to be done just before starting the solution
         process so that we do not include spurious computations in the
         solution time. Also initializes the epoch to '0'. We want the
         iterations to end with this set to the last epoch computed.
         Thus, we increment this at the start of the loop, and need this
         to start at 0 to get '1' for the first epoch. */
      
      param.stat = st.newSolutionStats( param.report_filename, 
                                      param.stat_summary );
      
      /* Set the epoch number to zero.  It gets incremented at start of
         loop so '1' will be the first epoch. */
      param.cur_epoch = 0;

      /* Ok, hold on now, 'cause here we go! */

       System.out.println( "++++++++++++++++++ Start Solving ++++++++++++++++++++++" );
       writeToFile(next_alpha_list,param.cur_epoch);
      while(!done) {

        /* Some variations of value iteration need to adjust things at the
           startb of an iteration. This routine handles these things.
           This also increments the current epoch number. */
        startViEpoch( param );

        /* Delete the obs_source pointer array from next_alpha_list into
           prev_alpha_list since we are about to delete
           prev_alpha_list. The first time through the loop this will not
           do anything, since there will be no obs_source array for the
           initial alpha_list. */
       next_alpha_list = ap.clearObsSourceAlphaList( next_alpha_list );
       
        /* Get rid of old value function, but make sure this is done
           after the policy graph information is extracted from
           next_alpha_list, since there are pointers from
           next_alpha_list into prev_alpha_list. First time through the
           loop this will be NULL. */
        if ( prev_alpha_list != null )
          ap.destroyAlphaList( prev_alpha_list );

        /* We are at the top of the loop so now the next_alpha_list
           becomes the prev_alpha_list. */
        prev_alpha_list = next_alpha_list;
        next_alpha_list = null;

        /* Start the clock ticking on this epoch time and show some
           information indicating the epoch as started. */
        st.epochStartStats( param.stat );
        //ap.displayAlphaList(prev_alpha_list);
        /* This is the heart of solution process: computing one value
           function from the other. */
        next_alpha_list = improveV( prev_alpha_list, param );
       /* System.out.println("display new list");
        ap.displayAlphaList(next_alpha_list);
        /* Although the 'epsilon' precision value is used to determine
              equality in an approximate manner, the solutions that are saved
              and/or fed into the next value iteration epoch retain their
              computed values.  Thus, two value that are considered the same
              from an epilon-approximate viewpoint might actually have
              different values from a machine-precision viewpoint, and thus
              wehn used to seed the next iteration may not lead to the same
              values.  This flag will force the alpha vector coef values to
              be rounded in accordance with the 'epilson' value after each
              iteration.
        */
        

        /* In case the -save_all option was selected, save the files. We
           need to do this *before* we do the ZLZ update, otherwise the
           policy graph and value functions might not make much sense. 

              Note that with the -save_all option, we incur the sorting and
              file I/O time inside the timing of each epoch.  Thus, one
              should not use this if the timing will matter.  A future change
              might fix this so this time does not get included. 
        */
       /* if ( param.save_all == TRUE ) {
          ap.sortAlphaList( next_alpha_list );
          ap.writeAlphaList( alpha_filename,next_alpha_list );
          pg.writePolicyGraph( "results/outPolicy.pg",next_alpha_list);
        }*/
       
       

        /* We will always save the current alpha list to a file so that
       abnormal termination will leave the lastest epoch's
       solution.  This will allow you to start it with the last
           solution and not have to re-run it. */
        /*ap.writeAlphaList( param.backup_file,next_alpha_list  ); 
        System.out.println("prev Policy is :::::::::::::::::::::");
        ap.displayAlphaList(prev_alpha_list);*/
        next_alpha_list = ap.sortAlphaList(next_alpha_list);
        writeToFile(next_alpha_list,param.cur_epoch);
       /* System.out.println("new Policy is :::::::::::::::::::::::::");
        ap.displayAlphaList(next_alpha_list);*/
        
        /* Check for stopping condition. */
        if (( param.cur_epoch == param.horizon )
            || ( meetStopCriteria( prev_alpha_list, 
                                   next_alpha_list,
                                   param ) == TRUE))
          done = true;

        /* If we are using a variation on value iteration, then we may
           need to adjust some parameters (epsilon) after an epoch.
           Either way we have to at least check whether parameters need to
           change. This function handles this case. */
        if ( !done)
          endViEpoch( param );

      }  /* while( done == FALSE && gInterrupt = FALSE ) */

      /* Give some final tally information, such as solution location and
         time. Also write the solution files, but note that this must be
         done before we clear the prev_alpha_list to ensure policy graph
         information is preserved. */
  
      endPomdpSolve( param, next_alpha_list );
      pg.writePolicyGraph( "results/outPolicy.pg",next_alpha_list);
      /* Get rid of previous and next value functions.  Note that at this
         point there are obs_source pointers from next_alpha_list into
         prev_alpha_list, so free'ing prev_alpha_list first would leave
         pointers to nowhere.  Thus, we free them in the other order to
         show this dependency explicitly. */
      ap.destroyAlphaList( next_alpha_list );
      ap.destroyAlphaList( prev_alpha_list );

      out.close();/*close file writer */
    }  /* solvePomdp */  
/**********************************************************************/
    //write each epoch sulutions into file
    public void writeToFile(AlphaList list, int epoch ) throws IOException{
        assert(list != null):"Null List";
        out.write("Epoch: "+epoch +"          "+list.length +" vectors     "+ t.getUser() +" seconds");
        out.write("\n");
        AlphaNode temp = list.head;
        
        while(temp != null){
            out.write("Action = "+temp.action +" : ");
            for(int i = 0; i < gm.getgNumStates(); i++)
                out.write(temp.alpha[i]+"  ");
            out.write("\n");
            temp = temp.next;
        }        
        
        out.write("..........................................................");
        out.write("\n");

    }
    /**********************************************************************/
}
