package pomdp;
import Enum.StopCriteria;
import mdp.mdpIN.*;
import pomdp.globalIN.*;
import pomdp.timingIN.*;
import pomdp.randomIN.*;
import pomdp.pomdpIN.*;
import pomdp.statsIN.*;
import pomdp.inc_pruneIN.*;
import pomdp.paramsIN.*;
//import incpomdp.src.pomdp_solve_optionsIN.*;

public class params implements paramsIN,globalIN{
    stats st ;
    alpha ap ;
        /* Strings for the various stopping criteria */
    String[] purge_option_str = PURGE_OPTION_STRINGS;

    /* Strings for the various incremental pruning variations. */
    String[] inc_prune_type_str = INC_PRUNE_TYPE_STRINGS;

    /* Strings for the various value iterations variations. */
    String[] vi_variation_type_str = VI_VARIATION_TYPE_STRINGS;

    /**********************************************************************/
    public params(){}
  
        @Override
    public PomdpSolveParams newPomdpSolveParams(String filename ) 
    {
      /*
        Creates the memory for the structure to hold the parameters used in
        solving a POMDP.  Also sets the fields to the default values.
      */
      PomdpSolveParams params;
      int i;
      params = new PomdpSolveParams();
      //params = (PomdpSolveParams) XMALLOC( sizeof( *params ));

      /* We do not allocate the memory for this structure.  We just ensure
            it starts with a sane NULL value.  We will clean this up if it is
            non-null in the destructor.
      */
 
      params.cur_epoch = 0;
      params.stat_summary = 0;
      params.report_filename = "results/report.txt";
      //params.report_file = "";
      params.param_filename = filename;
      params.override_discount = -1.0;
      params.horizon = DEFAULT_HORIZON;
      params.alpha_filename = "results/alphaFile.txt";
      params.pg_filename = "results/pgFile.txt";
      params.initial_policy_filename = null;
      params.initial_policy = null;
      params.max_secs = 10000;
      params.memory_limit = 0;
      params.save_all = TRUE;
      params.proj_purge = DEFAULT_PROJECTION_PURGE;
      params.use_witness_points = DEFAULT_USE_WITNESS_POINTS;
      params.backup_file = "results/backup.txt";
      params.penultimate_filename = "results/penultimate.txt";
      params.q_purge_option = DEFAULT_Q_PURGE_OPTION;
      params.domination_check = DEFAULT_DOMINATION_CHECK;
      params.alg_init_rand_points = DEFAULT_ALG_INIT_RAND_POINTS;
      params.prune_init_rand_points = DEFAULT_PRUNE_INIT_RAND_POINTS;

      params.prune_epsilon = DEFAULT_PRUNE_EPSILON; 
      params.epsilon = DEFAULT_EPSILON; 
      params.lp_epsilon = DEFAULT_LP_EPSILON; 

      params.weak_bound_delta = DEFAULT_STOP_DELTA;

      params.alpha_epsilon = DEFAULT_ALPHA_EPSILON; 
      params.vertex_epsilon = DEFAULT_VERTEX_EPSILON; 
      params.impossible_obs_epsilon = DEFAULT_IMPOSSIBLE_OBS_EPSILON;
      params.double_equality_precision = DEFAULT_DOUBLE_EQUALITY_PRECISION;

      /* Default value to use when considering whether to include a
         coefficient in a sparse representation. Note that we don't really
         want to tie this to the precision that is being used to solve the
         problem, because this value can change the problem being solved.
         Thus this should just be fixed for all time at the minimum
         precision. */
      params.sparse_epsilon = SMALLEST_PRECISION; 

      /* Place to hang statistics off of (optional) */
      params.stat = null;

      /****************************************/
      /****  Algorithm specific section  ******/
      /****************************************/

      params.ip_type = DEFAULT_INC_PRUNE_TYPE;
     
      /****************************************/
      /****  VI variation specific section  ***/
      /****************************************/

      params.vi_variation = DEFAULT_VI_VARIATION;
      params.starting_epsilon = DEFAULT_STARTING_EPSILON;
      params.ending_epsilon = DEFAULT_ENDING_EPSILON;
      params.epsilon_adjust_factor = DEFAULT_EPSILON_ADJUST_FACTOR;
      params.max_soln_size = DEFAULT_MAX_SOLN_SIZE;
      params.epoch_history_window_length = DEFAULT_HISTORY_WINDOW_LENGTH; 
      params.epoch_history_window_delta = DEFAULT_HISTORY_WINDO_DELTA;
      params.stop_criteria = StopCriteria.stop_weak;
      return ( params );

    }  /* newPomdpSolveParams */
    /**********************************************************************/
        @Override
    public void destroyPomdpSolveParams( PomdpSolveParams params ) 
    {
         
      /* 
         Frees the memory for pointers in the params and the param structure
         itself.
      */
      if ( params.stat != null )
        st.destroySolutionStats( params.stat );

      if ( params.initial_policy != null )
        ap.destroyAlphaList( params.initial_policy );

       params = null;

    }  /* destroyPomdpSolveParams */
    /**********************************************************************/
    public void parseMethodAliases( int argc, String[] argv, int[] mark_arg,
                        PomdpSolveParams param ) 
    {

      /* zzz Add aliases here and see obsolete/old-parse-alias.c */

    }  /* methodAliases */
    /**********************************************************************/
    public void enforceSmallestPrecision( double value, String name ) 
    {
      /*
        Takes a value and makes sure it is not less than the smallest
        allowable precision the program uses.  It will give a message if
        it needs to be changed.  
      */
      String msg = new String();//[MAX_MSG_LENGTH];

      if ( value >= SMALLEST_PRECISION )
        return;

      value = SMALLEST_PRECISION;

      System.out.println("The value for" +name+" is below the smallest precision.\n\tSetting to %.3e."+ 
               value );

     // Warning( msg )

    }  /* enforceSmallestPrecision */

    /**********************************************************************/
    public void doPreOptionParseActions( ) {
      /*
        The very first routine that is called. Put anything that needs to
        happen before parsing the command line in this routine. 
       */

      /****************/
      /* I used to initialize this variable during its declaration, but I
            found that when updating to version 4.1, this was no longer
            allowed. Thus, I do it here first thing. */
      //gStdErrFile = null;

    } /* doPreOptionParseActions */

    /**********************************************************************/
   /***didn't use here***/
        @Override
    public PomdpSolveParams showPomdpSolveParams( PomdpSolveParams params ) 
    {
      /*ConfigFile cfg;

      fprintf( params.report_file, 
                     " //****************\\\\\n" );
      fprintf( params.report_file, 
                     "||   %s    ||\n", params.opts->__exec_name__ );
      fprintf( params->report_file, 
                     "||     v. %s       ||\n", params->opts->__version__ );
      fprintf( params->report_file, 
      /               " \\\\****************//*\n" );
      fprintf( params->report_file, 
                     "      PID=%d\n", getpid() );

      cfg = POMDP_SOLVE_OPTS_toConfigFile( params->opts );

      fprintf( params.report_file, 
                     "- - - - - - - - - - - - - - - - - - - -\n" );

      CF_fprintf( cfg, params.report_file );

      fprintf( params.report_file, 
                     "- - - - - - - - - - - - - - - - - - - -\n" );

      CF_delete( cfg );*/
        return null;

    } /* showPomdpSolveParams */

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

    

    
}
