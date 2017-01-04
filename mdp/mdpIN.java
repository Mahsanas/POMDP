package mdp;
import Enum.Value_Type;
import java.io.File;

public interface mdpIN {
    /* Use this type for a variable that indicated whether we have a 
    POMDP or an MDP.
    */
    
    public enum Problem_Type{  UNKNOWN_problem_type, 
                MDP_problem_type, 
                POMDP_problem_type 
              };

    /* Use this to determine if the problems values are rewards or costs.
    */
    public  int NUM_VALUE_TYPES    =      2;
    
    public String[]  VALUE_TYPE_STRINGS    =   { "cost", "reward"};

    public double DEFAULT_DISCOUNT_FACTOR =  1.0;

    Value_Type DEFAULT_VALUE_TYPE = Value_Type.REWARD_value_type;

    public static final int INVALID_STATE  =   -1;
    public static final int INVALID_OBS  =  -1;
    public static final int INVALID_ACTION = -1;


    /* Exported variables */
   /* public String[] value_type_str = null;
    public double gDiscount = 0.0;
    public Problem_Type gProblemType = null;
    public Value_Type gValueType = null;

    /* We will use this flag to indicate whether the problem has negative
    rewards or not.  It starts off FALSE and becomes TRUE if any
    negative reward is found. */
    /*public double gMinimumImmediateReward = 0.0;

   /* public int gNumStates = 0;
    public int gNumActions = 0;
    public int gNumObservations = 0;

    /* Intermediate variables */

   //public I_Matrix[] IP = null;  /* Transition Probabilities */
   // public I_Matrix[] IR = null;  /* Observation Probabilities */
   // public I_Matrix[] IQ = null;  ;  /* Immediate values for MDP only */

    /* Sparse variables */

    //public Matrix[] P = null;  /* Transition Probabilities */
    //public Matrix[] R = null;  /* Observation Probabilities */
    //public Matrix[] QI = null;  /* The immediate values, for MDPs only */
    //public Matrix Q = null;  /* Immediate values for state action pairs.  These
     /*                are expectations computed from immediate values:
                     either the QI for MDPs or the special
                     representation for the POMDPs */

    //public double[] gInitialBelief = null;   /* For POMDPs */
    //public int gInitialState = 0;        /* For MDPs   */

    /* Exported functions */
    /*public double[] newBeliefState();
    public int transformBeliefState( double[] pi,
                                double[] pi_hat,
                                int a,
                                int obs );
    public void copyBeliefState( double[] copy, double[] pi );
    public void displayBeliefState( File file, double[] pi );
    public int readMDP( String filename );
    public void convertMatrices();
    public void deallocateMDP();
    //public void convertMatrices();
    public int verifyIntermediateMDP();
    public void deallocateIntermediateMDP();
    public void allocateIntermediateMDP();
    public int writeMDP( String filename );
    public void displayMDPSlice( int state );*/

}
