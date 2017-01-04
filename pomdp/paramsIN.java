
package pomdp;
/* Need this for stat field. */
import Enum.GeneralizedIpChoice;
import Enum.PurgeOption;
import Enum.StopCriteria;
import Enum.ViVariation;
import pomdp.globalIN.*;
import pomdp.statsIN.*;


public interface paramsIN {
    /*******************************************************************/
    /**************      USEFUL MNENOMIC CONSTANTS      ****************/
    /*******************************************************************/

    /* The suffix to use for alpha vector files. */
    public static final String  ALPHA_FILE_SUFFIX = ".alpha";

    /* The suffix to use for policy graph files. */
    public static final String PG_FILE_SUFFIX   = ".pg";

    /* The suffix to use for penultimate alpha vector files. */
    public static final String PENULTIMATE_SUFFIX = ".prev";

    /* We will save the solution after each iteration in a temporary file,
       so that if the program terminates abnormally, we can recover the
       latest solution. We will use the PID of the file to make sure the
       filename is unique so multiple copies can run at the same time. */
    public static final String SAVE_FILE_NAME_FORMAT  = "##pomdp-solve-%d##";

    /*******************************************************************/
    /**************             TYPEDEFS                ****************/
    /*******************************************************************/

    /* When purging a set of alpha vectors we are faced with a number of
       choices.  We can do nothing (i.e., not purge them), we can use only
       a simple domination check, or we can use linear programs to get a
       completely parsimonious set (i.e., prune).  For the pruning, it
       always makes sense to do domination checking as well, so we
       conciously did not allow specification of LPs without domination
       checking, i.e., pruning assumes you want the domination checking,
       which will be done first. */
    public static final int MAX_NUM_PURGE_OPTIONS  =    4;
   
    
    public String[] PURGE_OPTION_STRINGS   =  { 
                                          "none", 
                                          "domonly", 
                                          "normal_prune", 
                                          "epsilon_prune" 
                                      };

    public int MAX_INC_PRUNE_TYPES    =  3;
   
    public String[] INC_PRUNE_TYPE_STRINGS  =   { 
                                          "normal", 
                                          "restricted_region", 
                                          "generalized" 
                                       };

    public int MAX_VI_VARIATION_TYPES  =      4;
   
    public String[] VI_VARIATION_TYPE_STRINGS  =   { 
                                          "normal", 
                                          "zlz", 
                                          "adjustable_epsilon", 
                                          "fixed_soln_size" 
                                       } ;

    /* We just want to encapsulate all the parameters used in the
       pomdp-solve program into a single structure for convenience. These
       do not include any parameters that are specific to a particular
       algorithm. */ 
    //public class PomdpSolveParams extends PomdpSolveParamStruct {};
    public class PomdpSolveParams {

      /* We'll keep track of the epoch (iteration) as value iteration
         progresses. */
      static int cur_epoch;

           /* Since we save the stats for each epoch, we can optionally print
         them out when the program is finished executing. */
      static int stat_summary;

      /* Name of file to print all output information to.  Defaults to
         stdout. */
      //char report_filename[MAX_FILENAME_LENGTH];
      static String report_filename;
      /* All messages will be output to the same file handle, which
         defaults to stdout if no filename is given on the command
         line. */
     // File report_file;
      
      /* The name of the POMDP file we will use. */
      //char param_filename[MAX_FILENAME_LENGTH];
     static  String param_filename;
      /* We allow the discount factor to be overridden on the command
         line. If we don't over-ride the discount factor then set the
         override variable to be negative.  This will indicate whether we
         should override the file's discount factor after we read the
         file. */
     static  double override_discount;
      StopCriteria stop_criteria;
      /* How many epoch to run the algorithm for. */
      static int horizon;

      /* Names of files to write out solutions */
      //char alpha_filename[MAX_FILENAME_LENGTH];   /* alpha vectors */
      static String alpha_filename;
      //char pg_filename[MAX_FILENAME_LENGTH];      /* policy graph  */
      static String pg_filename;
      /* Sometimes we want to initialize value iteration with an initial
         value function. */
      //char initial_policy_filename[MAX_FILENAME_LENGTH];
     static  String initial_policy_filename;
      static AlphaList initial_policy;

      /* We can set a timer to interrupt the program after too many
         seconds have elapsed.  It is done from the command line and is
         optional. Zero or negative values turns time limits off.  */
      static int max_secs;

      /* The nature of these problems is such that they could require all
         the memory in the universe.  Therefore, to prevent them from
         going out and actually searching for all this memory, we put a
         ceiling on how much memory it can consume.  This can also be set
         via the command line if more or less is desired. Zero or negative
         values turns memory limits off. */
      static int memory_limit;

      /* Whether or not to save each epoch's solution in a separate
         file. */
      static int save_all;


      /* When creating the projection sets at the start of each epoch, we
         have a choice of purging each projectioon set.  This defines what
         purging option to use. */
      static PurgeOption proj_purge;

      /* An optimization we can use it to save witness points as we
         uncover them for a vector and use these points to help prime
         later prune operations. */
      static int use_witness_points;

      /* The filename to use as a backup file for each iteration's answer. */
      //char backup_file[1024];
      static String backup_file;
      /* The filename to use for previous epoch solution. */
      //char penultimate_filename[1024];
      static String penultimate_filename;
      /* For the algorithms which construct the value function one action
         at a time, the final step is to merge and purge all the
         single-action (Q) value functions.  This specifies how to purge
         them. This is only valid for those algorithms which build up the
         solution one action at a time. */
     static  PurgeOption q_purge_option;

      /* There are many places in the code where a simple domination check
         can be sued to save time.  This globally turns this option onor
         off for all of these places. */
      static int domination_check;

      /* An optimization of a few of the algorithms is to preceed any linear
         programming for finding the Q set with thowing a bunch of
         random points out and finding the vectors for these points.  This
         saves us from having to do LPs to find these vectors.  Not clear
         how useful it will be, but I know you cannot make any theoretical
         claims about it. */
     static  int alg_init_rand_points;

      /* An optimization of the prune() routine is to preceed linear
         programming for finding parsimonious sets with thowing a bunch of
         random points out and finding the vectors for these points.  This
         saves us from having to do LPs to find these vectors.  Not clear
         how useful it will be, but I know you cannot make any theoretical
         claims about it. */
      static int prune_init_rand_points;

      /* In this section, we gather all the values that deal with floating
         point comparison/precision problems.  */
     static  double weak_bound_delta;

      /* General epsilon to use for numerical comparisons. */
     static  double epsilon;

      /* Epsilon to use in the linear programs.  */
     static  double lp_epsilon;

      /* Epsilon to use when doing epsilon pruning. */
     static  double prune_epsilon;

      /* There are many places where we compare one alpha vector to
         another or to some specific value.  This is the epsilon that is
         used in those comparisons. */
      static double alpha_epsilon;

      /* Precision for comparison of vertices in the linear support
         algorithm. */
     static  double vertex_epsilon;

      /* Solution value precision for each iteration, used to compare the
         LP objective values. */
     static  double sparse_epsilon;

      /* Given the model parameters, it can be impossible to get a
         particular observation for a situation.  To detect this we
         compute the probability of the observation and then compare it to
         zero.  As will all our floating point comparisons, we need a
         tolerance factor. */
     static  double impossible_obs_epsilon;

     static  double double_equality_precision;

      /* Place to hang statistics off of (optional) */
    static   SolutionStats stat;

      /****************************************/
      /****  Algorithm specific section  ******/
      /****************************************/

      /* Specific for the incremental pruning algorithm, this says what
         type of incremental pruning to use. */
     static  GeneralizedIpChoice ip_type;

    static   PurgeOption enum_purge_option;

    static   PurgeOption fg_purge_option;

      /****************************************/
      /***  VI variation specific section  */
      /****************************************/

      /* This is used as temporary storage to keep the resulting maximal
         computed difference between the original and epislon pruned set
         when using the epsilon pruning option.  We store it here and move
         it elsewhere if we need it.  Because the prune algorithm can be
         called in a lot of context, it is up to the overall calling
         context to decide exactly what is does with it. */
     static  double epsilon_diff_of_last_prune;

      /* Defines exactly what value iteration variation to use. */
     static  ViVariation vi_variation;

      /* These are use for automatic epsilon adjustment. */
     static  double starting_epsilon;
     static  double ending_epsilon;
     static  double epsilon_adjust_factor;

      /* Sets the maximum number of vectors for a given epoch.  A value
         less than '1' mean that there is no upper limit. */
     static  int max_soln_size;

      /* When testing whether to increment/decrement the epsilon, we use
         some window of the past hostory of the epochs to decide.
         Specifically, we look at the recent history of the solution sizes
         over the last few epochs.  These define the hostory window length
         and the amount by which we assume a change is not important. */
    static   int epoch_history_window_length; 
    static   int epoch_history_window_delta;

    };

    /*******************************************************************/
    /**************         DEFAULT VALUES              ****************/
    /*******************************************************************/

    public static final double DEFAULT_STOP_DELTA   =    1E-9;
    public static final int  DEFAULT_VERBOSE  =      0;
    public static final int DEFAULT_HORIZON    = 10;
    public static final String DEFAULT_PREFIX    =    "solution";
    public static final boolean DEFAULT_SAFE   =     true;
    public static final int DEFAULT_CONTEXT_DETAILS =  0;
    public static final  PurgeOption DEFAULT_PROJECTION_PURGE =  PurgeOption.purge_prune;
    public static final int DEFAULT_USE_WITNESS_POINTS  =  0;
    public static final  PurgeOption DEFAULT_Q_PURGE_OPTION   =   PurgeOption.purge_prune;
    public static final int DEFAULT_DOMINATION_CHECK   =    0 ;

    /* Main epsilon parameter which many other epsilons need to be derived
       from. */
    public static final double DEFAULT_EPSILON  =   1E-9;

    /* Main epsilon parameter which many other epsilons need to be derived
       from. */
    public static final double DEFAULT_LP_EPSILON         =   1E-9;

    /* Epsilon parameter for considering model parameters zero or non-zero
       when using sparse representations. */
    public static final double DEFAULT_SPARSE_EPSILON        =   1E-9;

    /* Main epsilon parameter which many other epsilons need to be derived
       from. */
    public static final double DEFAULT_PRUNE_EPSILON  =         1E-3;

    /* For comparisons that involve alpha vector and components. */
    public static final double DEFAULT_ALPHA_EPSILON      =   1E-9;

    /* Epsilon for the vertex comparison in the vertex enumeration. */
    public static final double DEFAULT_VERTEX_EPSILON      =   1E-9;

    /* When comparing numbers, how accurate should we be? */
    public static final double DEFAULT_DOUBLE_EQUALITY_PRECISION    =   1E-9;

    /* When summing all probablity for a particular observation, if it is
       close enough to zero, then this observation is deemed not
       possible.  This epsilon factor says how close it has to be. */
    public static final double DEFAULT_IMPOSSIBLE_OBS_EPSILON       =   1E-9;

    /* An optimization is to preceed linear programming for finding
       parsimonious sets with thowing a bunch of random points out and
       finding the vectors for these points.  This saves us from having to
       do LPs to find these vectors.  Not clear how useful it will be, but
       I know you cannot make any theoretical claims about it. One is for
       the algorithm set initialization and the other for the prune()
       routine. */
    public static final int DEFAULT_ALG_INIT_RAND_POINTS    =   0;
    public static final int DEFAULT_PRUNE_INIT_RAND_POINTS   =   0;

    /****************************************/
    /****  Algorithm specific section  ******/
    /****************************************/

    public GeneralizedIpChoice DEFAULT_INC_PRUNE_TYPE  =  GeneralizedIpChoice.NormalIp;

    /* How to purge the set of vectors that are enumerated. */
    public PurgeOption DEFAULT_ENUM_PURGE_OPTION    =  PurgeOption.purge_prune;

    /* How to purge the set of vectors that are created from the finite
       grid. */  
    public PurgeOption DEFAULT_FG_PURGE_OPTION = PurgeOption.purge_prune;

    /****************************************/
    /***  VI variation specific section  */
    /****************************************/

    public ViVariation DEFAULT_VI_VARIATION = ViVariation.NormalVi;
    public static final double DEFAULT_STARTING_EPSILON   =   1E-1;
    public static final double DEFAULT_ENDING_EPSILON     =   1E-3;
    public static final double  DEFAULT_EPSILON_ADJUST_FACTOR   =   0.0;
    public static final int DEFAULT_MAX_SOLN_SIZE   =  10;
    public static final int DEFAULT_HISTORY_WINDOW_LENGTH  =   5;
    public static final int  DEFAULT_HISTORY_WINDO_DELTA   =   3;

    /*******************************************************************/
    /**************       COMMAND LINE OPTIONS          ****************/
    /*******************************************************************/

    public static final String CMD_ARG_POMDP_FILE    =       "-p";
    public static final String CMD_ARG_DISCOUNT       =      "-discount";

    public static final String CMD_ARG_TERMINAL_VALUES =     "-terminal_values";
    public static final String CMD_ARG_HORIZON          =    "-horizon";
    public static final String CMD_ARG_DELTA             =   "-stop_delta";

    public static final String CMD_ARG_OUTPUT       =        "-o";
    public static final String CMD_ARG_SAVE_ALL     =        "-save_all";
    public static final String CMD_ARG_REPORT_FILE  =        "-stdout";
    public static final String CMD_ARG_STAT_SUMMARY =        "-stat_summary";

    public static final String CMD_ARG_MEMORY_LIMIT =        "-memory_limit";
    public static final String CMD_ARG_MAX_SECS     =        "-time_limit";

    public static final String CMD_ARG_Q_PURGE      =        "-q_purge";
    public static final String CMD_ARG_DOM_CHECK    =        "-dom_check";

    public static final String CMD_ARG_PROJ_PURGE   =        "-proj_purge";
    public static final String CMD_ARG_WITNESS_POINTS =      "-witness_points";

    public static final String CMD_ARG_RAND_SEED      =      "-rand_seed";

    public static final String CMD_ARG_ALG_INIT_RAND  =      "-alg_rand";
    public static final String CMD_ARG_PRUNE_INIT_RAND =     "-prune_rand";

    public static final String CMD_ARG_EPSILON         =     "-epsilon";
    public static final String CMD_ARG_LP_EPSILON      =     "-lp_epsilon";
    public static final String CMD_ARG_PRUNE_EPSILON   =     "-prune_epsilon";

    /****************************************/
    /****  Algorithm specific section  ******/
    /****************************************/

    public static final String CMD_ARG_INC_PRUNE_TYPE   =          "-inc_prune";
    public static final String CMD_ARG_ENUM_PURGE_OPTION =         "-enum_purge";
    public static final String CMD_ARG_FG_PURGE_OPTION   =         "-fg_purge";

    /****************************************/
    /****  VI variation specific section  ***/
    /****************************************/

    public static final String CMD_ARG_VI_VARIATION      =              "-vi_variation";
    public static final String CMD_ARG_STARTING_EPSILON  =              "-start_epsilon";
    public static final String CMD_ARG_ENDING_EPSILON    =              "-end_epsilon";
    public static final String CMD_ARG_EPSILON_ADJUST_FACTOR =          "-epsilon_adjust";
    public static final String CMD_ARG_SOLN_SIZE             =          "-max_soln_size";
    public static final String CMD_ARG_HISTORY_WINDOW_LENGTH =          "-history_length";
    public static final String CMD_ARG_HISTORY_WINDO_DELTA   =          "-history_delta";

    /*******************************************************************/
    /**************       EXTERNAL VARIABLES            ****************/
    /*******************************************************************/

    public String purge_option_str = null;
    public String inc_prune_type_str = null;

    /*******************************************************************/
    /**************       EXTERNAL FUNCTIONS            ****************/
    /*******************************************************************/

    /* Creates the memory for the structure to hold the parameters used in
       solving a POMDP.  Also sets the fields to the default values.  */
    public PomdpSolveParams newPomdpSolveParams(String filename);

    /* Frees the memory for pointers in the params and the param structure
       itself.
    */
     public void destroyPomdpSolveParams( PomdpSolveParams param );

    /*
      Main routine for parsing config file and command line. 
    */
  // public PomdpSolveParams parseCmdLineAndCfgFile( int argc, String argv );

    /* Display to stdout the current program parameters. */
    public PomdpSolveParams showPomdpSolveParams( PomdpSolveParams params );

}
