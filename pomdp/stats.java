package pomdp;

import Enum.ContextType;
import java.io.BufferedWriter;
import java.io.FileWriter;
import pomdp.globalIN.*;
import pomdp.timingIN.*;
import pomdp.statsIN.*;
public class stats implements statsIN, globalIN{
   
    /* Mnemonics and abbreviations for the various program execution
   contexts. */
    String[] context_type_str = CONTEXT_TYPE_STRINGS;
    String[] context_type_str_brief = CONTEXT_TYPE_STRINGS_BRIEF;
    timing Timing ;
     global g ;
    public stats(global g, timing T){
        this.g = g;
        Timing = T;
    }

    stats() {
        
    }
/**********************************************************************/
/*************   For the EpochStats data structure  *******************/
/**********************************************************************/

/**********************************************************************/
    public EpochStats newEpochStatNode( int epoch_num ) 
    {
      /*
        Creates a new structure node for storing solution information for a
        single epoch and initializes the values. 
      */
      EpochStats epoch_stats = new EpochStats();
      int i;

      //epoch_stats = (EpochStats) XMALLOC( sizeof( *epoch_stats ));

      epoch_stats.epoch_num = epoch_num;

      for ( i = 0; i < MAX_NUM_CONTEXT_TYPES; i++ )
        epoch_stats.epoch_time[i] = 0.0;

      epoch_stats.user_time = 0.0;
      epoch_stats.system_time = 0.0;

      epoch_stats.set_epsilon = INVALID_PRECISION;

      epoch_stats.solution_size = -1;

      epoch_stats.max_epsilon = INVALID_PRECISION;

      epoch_stats.actual_stop_delta = -1.0 * HUGE_VAL;

      epoch_stats.next = null;

      return( epoch_stats );

    }  /* newEpochStatNode */
    /**********************************************************************/
        @Override
    public EpochStats getEpochStats( SolutionStats stat,
                   int epoch_num ) 
    {
      EpochStats node;

      if ( stat == null )
        return ( null );

      node = stat.epoch_stats;
      while( node != null ) {

        /* First check if this is the node we want */
        if ( node.epoch_num == epoch_num )
          return( node );

        /* Else just move to next node */
        node = node.next;

      } /* while loop */

      return ( null );

    } /* getEpochStats */
    /**********************************************************************/
    public EpochStats getOrCreateEpochStats( SolutionStats stat,
                           int epoch_num ) 
    {
      /*
        Will return the EpochStats structure for the given epoch number if
        it exists in the stat->epoch_stats linked list, or else create a
        new structure and add it to the end of the list and return the
        pointer to it.
      */
      EpochStats node;

        assert( stat != null):
          "NULL stat" ;

      /* If no stats at all exist, then just make a node, add it and
         return it. */
      if ( stat.epoch_stats == null) {

        stat.epoch_stats = newEpochStatNode( epoch_num );
        return ( stat.epoch_stats );

      } /* i not stat at all exist. */

      node = stat.epoch_stats;
      while( node != null ) {
      /* Note: we should get kicked out of the loop before the loop
         condition ever becomes false. */

        /* First check if this is the node we want */
        if ( node.epoch_num == epoch_num )
          return( node );

        /* Next Check if this is the last node in the list. If so, kick us
         out of the loop where we will add a new one to the list. */
        if ( node.next == null )
          break;

        /* Else just move to next node */
        node = node.next;

      } /* while loop */

      /* If and when loop terminates, the 'node' should be pointing to the
         last node in the list.  Thus we just need to put the new node
         after it and return that node. */
      node.next = newEpochStatNode( epoch_num );
      return( node.next );

    }  /* getOrCreateEpochStats */
    /**********************************************************************/
    public void recordEpochSetEpsilon( int epoch_num,
                           double set_epsilon,
                           SolutionStats stat ) 
    {
      /*
        Stores the preset epsilon used for a particular epoch.
      */
      EpochStats epoch_stat;

      /* Keeping stat is always optional. */
      if ( stat == null )
        return;

      epoch_stat = getOrCreateEpochStats( stat, epoch_num );
      epoch_stat.set_epsilon = set_epsilon;

    }  /* recordEpochSetEpsilon */
    /**********************************************************************/
    public void recordEpochSolutionSize( int epoch_num,
                             int solution_size,
                             SolutionStats stat ) 
    {
      /*
        Stores the solution size for a particular epoch.
      */
      EpochStats epoch_stat;

      /* Keeping stat is always optional. */
      if ( stat == null )
        return;

      epoch_stat = getOrCreateEpochStats( stat, epoch_num );
      epoch_stat.solution_size = solution_size;

    }  /* recordEpochSolutionSize */
    /**********************************************************************/
        @Override
    public void recordEpochMaxEpsilon( int epoch_num,
                           double max_epsilon,
                           SolutionStats stat ) 
    {
      /*
        Stores the actual maximal epsilon used for a particular epoch. (For
        epsilon prune variations only.  
      */
      EpochStats epoch_stat;

      /* Keeping stat is always optional. */
      if ( stat == null )
        return;

      epoch_stat = getOrCreateEpochStats( stat, epoch_num );
      epoch_stat.max_epsilon = max_epsilon;

    }  /* recordEpochMaxEpsilon */
    /**********************************************************************/
        @Override
    public void recordEpochCurStopDelta( int epoch_num,
                             double cur_stop_delta,
                             SolutionStats stat ) 
    {
      /* 
         Stores the actual computed stopping criteria difference between the
         value functions according to either the 'weak' or 'bellman' stopping
         criteria.  
      */
      EpochStats epoch_stat;

      /* Keeping stat is always optional. */
      if ( stat == null )
        return;

      epoch_stat = getOrCreateEpochStats( stat, epoch_num );
      epoch_stat.actual_stop_delta = cur_stop_delta;

    }  /* recordEpochMaxEpsilon */
    /**********************************************************************/
    public void recordEpochTime( int epoch_num,
                     double user_time,
                     double system_time,
                     SolutionStats stat ) 
    {
      /*
        Stores the time used for a particular epoch in terms of user and
        system time.
      */
      EpochStats epoch_stat;

      /* Keeping stat is always optional. */
      if ( stat == null )
        return;

      epoch_stat = getOrCreateEpochStats( stat, epoch_num );
      epoch_stat.user_time = user_time;
      epoch_stat.system_time = system_time;

    }  /* recordEpochTime */
    /**********************************************************************/
    public void recordEpochContextTime( int epoch_num,
                            int context_num,
                            double time,
                            SolutionStats stat ) 
    {
      /* 
         Increments the time used for a particular epoch and a particular
         context.  
      */
      EpochStats epoch_stat;

      /* Keeping stat is always optional. */
      if ( stat == null )
        return;

       assert( ( context_num >= 0 )
          && ( context_num < MAX_NUM_CONTEXT_TYPES )):
          "Bad context number.";


      epoch_stat = getOrCreateEpochStats( stat, epoch_num );
      epoch_stat.epoch_time[context_num] = time;

    }  /* recordEpochContextTime */
    /**********************************************************************/
    public void incrementEpochContextTime( int epoch_num,
                               int context_num,
                               double time,
                               SolutionStats stat ) 
    {
      /*
        Stores the time used for a particular epoch and a particular context.
      */
      EpochStats epoch_stat;

      /* Keeping stat is always optional. */
      if ( stat == null)
        return;

      assert( ( context_num >= 0 )
          && ( context_num < MAX_NUM_CONTEXT_TYPES )):
          "Bad context number.";

      epoch_stat = getOrCreateEpochStats( stat, epoch_num );

      /* We just accumulate the time. */
      epoch_stat.epoch_time[context_num] += time;

    }  /* incrementEpochContextTime */
    /**********************************************************************/
    public void reportEpochSummary( SolutionStats stat ) 
    {

      EpochStats node;
      int i;
      double total_time = 0.0;
      double[] total_context_time= new double[MAX_NUM_CONTEXT_TYPES];
      try{
      // Create file 
      FileWriter fstream = new FileWriter(stat.report_file);
      BufferedWriter out = new BufferedWriter(fstream);
      out.write("\n** Statistic Summary **\n\n");

      for ( i = 0; i < MAX_NUM_CONTEXT_TYPES; i++ ) {
        /* We don't report times for the empty context. */
        if ( i == Context_None )
          continue;
        out.write(context_type_str_brief[i] + "  " + context_type_str[i] );
      } /* for i */

      /* This is the header */
      out.write( "\nEpoch  Size       Err      Time  Epsilon" ); 
      for ( i = 0; i < MAX_NUM_CONTEXT_TYPES; i++ ) {
        /* We don't report times for the empty context. */
        if ( i == Context_None )
          continue;

        total_context_time[i] = 0.0;

        out.write( context_type_str_brief[i] );
      } /* for i */

      out.write( "\n\n" );

      node = stat.epoch_stats;
      while( node != null) {

        out.write( node.epoch_num+"  "+ node.solution_size +"  "+
                 node.actual_stop_delta+"  "+
                 (node.user_time + node.system_time) );

        total_time += node.user_time + node.system_time;

        if ( Equal( node.max_epsilon, INVALID_PRECISION,
                    SMALLEST_PRECISION ))
          out.write(  "  N/A   " );
        else
          out.write( node.max_epsilon +"" );

        for ( i = 0; i < MAX_NUM_CONTEXT_TYPES; i++ ) {

          /* We don't report times for the empty context. */
          if ( i == Context_None )
            continue;

          out.write( node.epoch_time[i] +"");

          total_context_time[i] += node.epoch_time[i];

        } /* for i */

        out.write( "\n" );

        node = node.next;
      } /* while node != NULL */

      out.write( "\n    Totals  " );

        out.write( "     "+ total_time );

        for ( i = 0; i < MAX_NUM_CONTEXT_TYPES; i++ ) {

          /* We don't report times for the empty context. */
          if ( i == Context_None )
            continue;

          out.write( total_context_time[i] +"" );

        } /* for i */

      out.write("\n" );
      //Close the output stream
      out.close();
      }catch (Exception e){//Catch exception if any
      System.err.println("Error: " + e.getMessage());
      }




    }  /* reportEpochSummary */
    /**********************************************************************/

    /**********************************************************************/
    /***********  Main routines for handling solution stats    ************/
    /**********************************************************************/

    /**********************************************************************/
        @Override
    public SolutionStats newSolutionStats( String report_file, 
                      int stat_summary ) 
    {
      /*
        Allocates memory and sets the default values for all variables
        related to tracking POMDP solving statistics.  This should be called
        just before starting to solve, because it gets the time for the
        start of the solution.

        Since there are a number of places where statistic reporting can be
        used, we also take in arguments to specify where to write the
        statistics.  
      */
      SolutionStats stat = new SolutionStats();
      int i;

      //stat = (SolutionStats) XMALLOC( sizeof( *stat ));

      stat.report_file = report_file;
      stat.stat_summary = stat_summary;

      stat.cur_context = DEFAULT_CONTEXT;

      for ( i = 0; i < MAX_NUM_CONTEXT_TYPES; i++ ) {

        stat.tot_time[i] = 0.0;
        stat.lp_count[i] = 0;
        stat.constraint_count[i] = 0;

      } /* for i */

      /* We want the iterations to end with this set to the last epoch
         computed.  Thus, we increment this at the start of the loop, and
         need this to start at 0 to get '1' for the first epoch. */
      stat.tot_epochs = 0;
      stat.cur_epoch = 0;

      Timing.getSecsDetail( );
      stat.start_time_user = Timing.getUserTime();
      stat.start_time_system = Timing.getSys();
      /* Start the individual epoch stat as an empty list. */
      stat.epoch_stats = null;

      return ( stat );
    }  /* newSolutionStats */
    /**********************************************************************/
    public void destroySolutionStats( SolutionStats stat ) 
    {
      /*
        Frees the allocated memory for this structure.
      */
      EpochStats temp;

      /* Stats are always optional */
      if ( stat == null )
        return;

      /* First frre the individual epoch stats linked list if one
         exists. */
      while( stat.epoch_stats != null ) {

        temp = stat.epoch_stats;
        stat.epoch_stats = temp.next;

         temp = null;

      } /* while epoch_stats not NULL */

      stat = null;
    }  /* destroySolutionStats */
    /**********************************************************************/
        @Override
    public void epochStartStats( SolutionStats stat ) 
    {
      /*
        Called just prior to doing the value iteration new value function
        computation.  Sets up the stats in preparation for this iteration. 
      */
      int i;

      /* In all phases of solution, keeping stats is optional.  Thus, when
         the stat struct is NULL, this just means we are not interested in
         keeping stats.  Thus we just ignore calls to this when stats is
         NULL. */
      if ( stat == null )
        return;

      /* Increment the epoch number */
      (stat.cur_epoch)++;

      /* If we want to track per-epoch LPs per context, we need to save
         the total we have at the start of the epoch. */
      for ( i = 0; i < MAX_NUM_CONTEXT_TYPES; i++ ) {
        stat.epoch_start_lps[i] = stat.lp_count[i];
        stat.epoch_start_constraints[i] = stat.constraint_count[i];
      } /* for i */
      
    System.out.println( "**************     EPOCH # " + stat.cur_epoch   +" *************"  );
   
      

      Timing.getSecsDetail( );
      stat.epoch_start_time_user = Timing.getUser();
      stat.epoch_start_time_system = Timing.getSys();

    }  /* epochStartStats */
    /**********************************************************************/
        @Override
    public void epochEndStats( SolutionStats stat, 
                   int solution_size,
                   double cur_error ) 
    {
      /*
        Called after an iteration of value iteration.  Will update the
        statistics, report for this epoch.  Need the solution size in the
        reporting.
      */
      double epoch_time_user, epoch_time_system,
        stop_time_user = 0.0, stop_time_system = 0.0;

      /* In all phases of solution, keeping stats is optional.  Thus, when
         the stat struct is NULL, this just means we are not interested in
         keeping stats.  Thus we just ignore calls to this when stats is
         NULL. */
      if ( stat == null)
        return;

      recordEpochSolutionSize( stat.cur_epoch,
                               solution_size,
                               stat );

      recordEpochCurStopDelta( stat.cur_epoch, 
                               cur_error, stat );

      /* First thing is to get the time to mark the end of the
         epoch's contribution. */
      Timing.getSecsDetail( );

      stop_time_user = Timing.getUser();
      stop_time_system = Timing.getSys();
      /* Compute the time taken for this epoch. */
      epoch_time_user 
        = stop_time_user - stat.epoch_start_time_user;
      epoch_time_system 
        = stop_time_system - stat.epoch_start_time_system;

      recordEpochTime( stat.cur_epoch,
                       epoch_time_user,
                       epoch_time_system,
                       stat );
      try{
      // Create file 
          FileWriter fstream = new FileWriter("out.txt");
          BufferedWriter out = new BufferedWriter(fstream);
          out.write("%d vectors in: "+solution_size+ "secs " +
                  (epoch_time_user + epoch_time_system)+ "total "+
                  (stop_time_user - stat.start_time_user
                  + stop_time_system - stat.start_time_system) +"err= "+cur_error );
          //Close the output stream
          out.close();
      }catch (Exception e){//Catch exception if any
          System.err.println("Error: " + e.getMessage());
      }


      if ( g.getgVerbose(V_CONTEXT) == 1)
        reportContextDetails( stat );

    }  /* epochEndStats */
    /**********************************************************************/
        @Override
    public void recordLpStats( SolutionStats stat,
                   int num_variables, 
                   int num_constraints ) 
    {
      /*
        Called just prior to solving an LP, this routine records the size of
        the LP for the given context that the program is runing in.
      */

      /* In all phases of solution, keeping stats is optional.  Thus, when
         the stat struct is NULL, this just means we are not interested in
         keeping stats.  Thus we just ignore calls to this when stats is
         NULL. */
      if ( stat == null )
        return;
    /*
      stat.lp_count[stat.cur_context]++;
      stat.constraint_count[stat.cur_context] += num_constraints;

    #ifdef DEBUG_LP_STATS
      fprintf( stat->report_file, "\t\tLP: %d constraints (%s).\n", 
               num_constraints, context_type_str[stat->cur_context] );
    #endif*/

    }  /* recordLpStats */
    /**********************************************************************/
    public void getLpStats( SolutionStats stat, 
                int tot_lps, 
                int tot_constraints ) 
    {
      /*
        Retrieves the stores LP stats for all the different program
        contexts.  Puts them in two arrays corresponding to the total number
        of LPs and the total number of constraints.
      */
      ContextType context;

      tot_lps = 0;
      tot_constraints = 0;

      /* In all phases of solution, keeping stats is optional.  Thus, when
         the stat struct is NULL, this just means we are not interested in
         keeping stats.  Thus we just ignore calls to this when stats is
         NULL. */
      if ( stat == null )
        return;

      /*for ( context = 0; context < MAX_NUM_CONTEXT_TYPES; context++ ) {

        tot_lps += stat.lp_count[context];
        tot_constraints += stat.constraint_count[context];

      }  /* for context */

    }  /* getLpStats */
    /**********************************************************************/
        @Override
    public void reportLpStats( SolutionStats stat ) 
    {
      /*
        Prints out the current LP stats that have accumulated.  If 'sucinct'
        is TRUE, then use a very terse reporting style.
      */
      ContextType context;
      int tot_lps = 0;
      int tot_constraints = 0;

      /* In all phases of solution, keeping stats is optional.  Thus, when
         the stat struct is NULL, this just means we are not interested in
         keeping stats.  Thus we just ignore calls to this when stats is
         NULL. */
      if ( stat == null )
        return;

     /* for ( context = 0; context < MAX_NUM_CONTEXT_TYPES; context++ ) {

        tot_lps += stat.lp_count[context];
        tot_constraints += stat.constraint_count[context];

        /* We won't report any LPs in the empty context directly, but
           we will include it in the totals we accumulate. */
      /*  if ( context == ContextType.Context_None )
          continue;

        fprintf( stat.report_file,
                 "\t%s LPs: %d,  Constraints: %d\n", 
                 context_type_str[context], 
                 stat->lp_count[context], 
                 stat->constraint_count[context] );

      }  /* for context */

     /* fprintf( stat->report_file,
               "  Total LPs: %d,  Constraints: %d\n", 
               tot_lps, tot_constraints );*/

    }  /* reportLpStats */
    /**********************************************************************/
        @Override
    public void startContext( SolutionStats stat, ContextType context ) 
    {
      /*
        As the program executes, it makes its way through various
        conceptual portions of the code.  We have chosen to break up the
        code's execution into a few distinct contexts.  This routine is
        called to indicate that a new context has started.

        You cannot do nesting of contexts at this time. 
      */

      /* In all phases of solution, keeping stats is optional.  Thus, when
         the stat struct is NULL, this just means we are not interested in
         keeping stats.  Thus we just ignore calls to this when stats is
         NULL. */
      if ( stat == null )
        return;

      assert ( stat.cur_context == ContextType.Context_None):
           "Nesting of program contexts not implemented." ;


      stat.cur_context = context;

      stat.cur_context_start_time = Timing.getSecs();

    }  /* startContext */
    /**********************************************************************/
        @Override
    public void endContext( SolutionStats stat, ContextType context ) 
    {
      /*
        Ends the program execution context indicated.
      */
      double time;
      int Context = checkContext(context);
      
      /* In all phases of solution, keeping stats is optional.  Thus, when
         the stat struct is NULL, this just means we are not interested in
         keeping stats.  Thus we just ignore calls to this when stats is
         NULL. */
      if ( stat == null )
        return;

       assert ( stat.cur_context == context): 
           "Trying to end a context that wasn't started." ;

      time = Timing.getSecs() - stat.cur_context_start_time;

      stat.tot_time[Context] += time;

      incrementEpochContextTime( stat.cur_epoch,
                                 Context, time, stat );

      stat.cur_context = ContextType.Context_None;

    }  /* endContext */
    /**********************************************************************/
        public int checkContext(ContextType c){
            if(c == ContextType.Context_None)
                return 0;
            else if(c == ContextType.Context_Projection_build)
                return 1; 
            else if(c ==   ContextType.Context_Projection_purge)
                return 2; 
            else if(c ==    ContextType.Context_Q_a_build)
                return 3; 
            else if(c ==    ContextType.Context_Q_a_merge)
                return 4;
            else if(c ==   ContextType.Context_Zlz_Speedup)
                return 5;
            else if(c ==    ContextType.Context_Stop_Criteria)
                return 6;
            return 0;
        }
        @Override
    public void reportContextDetails( SolutionStats stat ) 
    {
      /* 
         Shows both the execution time and LP statistics broken down by the
         various program contexts.  Prints out the current LP stats that have
         accumulated.  If 'sucinct' is TRUE, then use a very terse reporting
         style. 
      */
      ContextType context = null;
      int c;
      double end_time = 0;
      double time;
      double tot_time = 0.0;

      /* In all phases of solution, keeping stats is optional.  Thus, when
         the stat struct is NULL, this just means we are not interested in
         keeping stats.  Thus we just ignore calls to this when stats is
         NULL. */
      if ( stat == null )
        return;

      /* If we are in the middle of a context and report the times, we
         must ensure we report the time that has been done in this current
         context.  Since the time doesn't get added until the end of the
         context, we will have to account for it here. */
      if ( stat.cur_context != ContextType.Context_None )
        end_time = Timing.getSecs();

      for ( c = 0; c < MAX_NUM_CONTEXT_TYPES; c++ ) {

        /* We don't report times for the empty context. */
        if ( context == ContextType.Context_None )
          continue;

        time = stat.tot_time[c];

        /* Adjust time if we are in the middle of a context. */
        if ( stat.cur_context == context )
          time += end_time - stat.cur_context_start_time;

        tot_time += time;

        System.out.println( stat.report_file+ "\t%s time: %.2f secs.\n"+ 
                 context_type_str[c] +" "+time );
      }  /* for context */

      System.out.println( stat.report_file + "  Total context time: %.2f secs.\n"+
               tot_time );

    }  /* reportContextDetails */
    /**********************************************************************/
    public void reportStats( SolutionStats stat ) 
    {
      /*
        Reports the stats in the structure sent in.
      */
      double stop_time_user = 0, stop_time_system = 0;

      /* In all phases of solution, keeping stats is optional.  Thus, when
         the stat struct is NULL, this just means we are not interested in
         keeping stats.  Thus we just ignore calls to this when stats is
         NULL. */
      if ( stat == null )
        return;

      /* First thing is to get the time to mark the end of the
         program;s execution. */
      Timing.getSecsDetail(  );
      stop_time_user = Timing.getUser();
      stop_time_system = Timing.getSys();
      Timing.reportTimes(stat.report_file,
                  stop_time_user - stat.start_time_user,
                  "User time =" );
      Timing.reportTimes(stat.report_file,
                  stop_time_system - stat.start_time_system,
                  "System time =" );
      Timing.reportTimes(stat.report_file,
                  stop_time_user - stat.start_time_user
                  + stop_time_system - stat.start_time_system, 
                  "Total execution time =" );


      if ( g.getgVerbose(V_LP) == 1 ) 
        reportLpStats( stat );

      if ( stat.stat_summary == 1) {

        reportContextDetails( stat );
        reportEpochSummary( stat );

      }
    }  /* reportStats */

    @Override
    public void getLpStats(SolutionStats stat, int[] tot_lps, int[] tot_constraints) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean Equal(double x, double y, double e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int LessThan(double x, double y, double e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean GreaterThan(double x, double y, double e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void initGlobal() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void cleanUpGlobal() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getPid() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeFile(String filename) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
/**********************************************************************/



}
