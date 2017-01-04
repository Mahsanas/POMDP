package pomdp;

public interface globalIN {
    
    /*******************************************************************/
    /**************      USEFUL MNENOMIC CONSTANTS      ****************/
    /*******************************************************************/
    public static final double HUGE_VAL = Double.POSITIVE_INFINITY;
    public static final int TRUE = 1;
    public static final int FALSE = 0;
    /* Useful mnemonics. */
    /*public static final int FALSE = 0;
    public static final int TRUE = 1;*/
    public static final char NULL_CHAR = '\0';
    /* Use this mnemonic for integer values which need to be >= 0 (i.e.,
       counts, obs, action, state numbers.  */
    public static final int UNINITIALIZED = -1;

    /* When using strings as temporary space for holding messages to be
       printed, define the string to be of this length. Shouldn't need it
       larger than this, but if so, feel free to increase this. */
    public static final int MAX_MSG_LENGTH = 80;

    /* For places where we statically allocate filename character arrays,
       use this for the length. */
    public static final int MAX_FILENAME_LENGTH = 100;

    /*******************************************************************/
    /**************              MACROS                 ****************/
    /*******************************************************************/
    /*public double Max(double x, double y);
    public double Min(double x, double y);*/
    /* Comparisons using a tolerance. */
    public boolean Equal(double x,double y, double e);
    public int LessThan(double x, double y, double e);
    public boolean GreaterThan(double x, double y, double e);

    /*******************************************************************/
    /**************             TYPEDEFS                ****************/
    /*******************************************************************/

    public String[] BOOLEAN_STRINGS = {  "false", "true" } ;


    /*******************************************************************/
    /**************        OVERALL PROGRAM CONSTANTS    ****************/
    /*******************************************************************/

    /* Do not allow any precision parameters to be smaller than
       this. Change this if you think the code and machine can handle more
       precision or if it cannot handle this precision. */
    public static final double SMALLEST_PRECISION  = 1E-12;

    /* Sometimes it is handy to have a value that makes no sense as a
       time.  This negative number serves that purpose. */
    public static final double INVALID_TIME  = -1.0;

    /* Sometimes it is handy to have a value that makes no sense as a
       precision factor.  This negative number serves that purpose. */
    public static final double INVALID_PRECISION  =  -1.0;

    /* When displaying floats, how many decimal points do you want 
       to show? */
    public static final int NUM_DECIMAL_DISPLAY =  6;

    /* Main command line arguments independent of solution stuff. */
    public static final String CMD_ARG_HELP_SHORT =  "-h";
    public static final String CMD_ARG_HELP_LONG    =        "-help";
    public static final String CMD_ARG_VERSION_SHORT   =     "-v";
    public static final String CMD_ARG_VERSION_LONG     =    "-version";
    public static final String CMD_ARG_VERBOSE         =     "-verbose";

    /*******************************************************************/
    /**************         DEFAULT VALUES              ****************/
    /*******************************************************************/

    /*******************************************************************/
    /**************     VERBOSE CMD LINE OPTIONS        ****************/
    /*******************************************************************/

    /* Each module in the program will be assigned a unique number.  This
       is used in the verboseness level.  We have an array of TRUE/FALSE
       values indicating whether the module should operate in verbose
       mode.  In addition to each module, some other special purpose
       functionality can get its own verboseness level. */

    /* There are three things that need to change if you want to add or
       remove a verboseness option. First the set of constants with the V_
       prefix needs to be modified, and the final result must still be a
       sequential list of integeres starting at zero.  Next, you must
       change the NUM_VERBOSE_OPTIONS to reflect the new number. Finally,
       you must add a string for the command line.  The array of strings
       *must* match up to the order in the sequential numbering. */

    public static final int NUM_VERBOSE_MODES     =              26;

    public static final int V_CONTEXT              =             0;
    public static final int V_LP                    =            1;
    public static final int V_GLOBAL                 =           2;
    public static final int V_TIMING                  =          3;
    public static final int V_STATS      =                       4;
    public static final int V_CMD_LINE    =                      5;
    public static final int V_POMDP_SOLVE  =                     6;
    public static final int V_ALPHA         =                    7;
    public static final int V_PROJECTION     =                   8;
    public static final int V_CROSS_SUM       =                  9;
    public static final int V_AGENDA           =                 10;
    public static final int V_ENUMERATE         =                11;
    public static final int V_TWO_PASS           =               12;
    public static final int V_LIN_SUPPORT         =              13;
    public static final int V_WITNESS              =             14;
    public static final int V_INC_PRUNE             =            15;
    public static final int V_LP_INTERFACE           =           16;
    public static final int V_VERTEX_ENUM    =                   17;
    public static final int V_MDP             =                  18;
    public static final int V_POMDP            =                 19;
    public static final int V_PARAM             =                20;
    public static final int V_PARSIMONIOUS       =               21;
    public static final int V_REGION              =              22;
    public static final int V_APPROX_MCGS          =             23;
    public static final int V_ZLZ_SPEEDUP           =            24;
    public static final int V_FINITE_GRID            =           25;

    public String[] VERBOSE_MODE_STRINGS = { 
                                     "context", 
                                     "lp", 
                                     "global", 
                                     "timing", 
                                     "stats", 
                                     "cmdline",
                                     "main", 
                                     "alpha",
                                     "proj", 
                                     "crosssum", 
                                     "agenda", 
                                     "enum", 
                                     "twopass", 
                                     "linsup", 
                                     "witness", 
                                     "incprune", 
                                     "lpinterface", 
                                     "vertexenum", 
                                     "mdp", 
                                     "pomdp", 
                                     "param", 
                                     "parsimonious", 
                                     "region", 
                                     "approx_mcgs", 
                                     "zlz_speedup", 
                                     "finite_grid" 
    };

    /*******************************************************************/
    /**************       EXTERNAL VARIABLES            ****************/
    /*******************************************************************/

    /*public String[] boolean_str = null;
    public String[] verbose_mode_str= null;

    public int[] gVerbose = null;
    public String gExecutableName = "";
    public String gStdErrFile = null;

    public double[] gTempValue = new double[10];
    public double[] gTempBelief = new double[10];
    public double[] gTempAlpha = new double[10];

    /*******************************************************************/
    /**************       EXTERNAL FUNCTIONS            ****************/
    /*******************************************************************/

    /* Sets up and allocates variables that are used globally across
      modules in the program. Currently just allocates a bunch of scratch
      memory areas.  */
    public void initGlobal(  );

    /* Cleans up after problem is solved to free any resources and reset
      anything that the initGlobal() routine did.  */
    public void cleanUpGlobal(  );

    /* Just a wrapper to the UN*X getpid() function to isolate it in case
       this gets ported to another platform.  Note that for POSIX, the
       'pid_t' type returned by getpid() is an 'int'.  */
    public int getPid(  );

    /* Just a wrapper to the UN*X unlink() function to isolate it in case
       this gets ported to another platform.  */
    public void removeFile( String filename );


}
