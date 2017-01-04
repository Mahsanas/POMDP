package mdp;
import Enum.Value_Type;
import mdp.mdpIN.*;
import java.util.ArrayList;

public class globalMDP{
   private static int  DOUBLE_DISPLAY_PRECISION    =  4;

   private  static double EPSILON = 0.00001;  /* tolerance for sum of probs == 1 */

/* To indicate whether we are using an MDP or POMDP. 
   */
   private  static Problem_Type gProblemType = Problem_Type.UNKNOWN_problem_type;

/* The discount factor to be used with the problem.  
   */
   private  static double gDiscount = 1.0;//DEFAULT_DISCOUNT_FACTOR =  1.0;

   private  static Value_Type gValueType =Value_Type.REWARD_value_type;//DEFAULT_VALUE_TYPE = Value_Type.REWARD_value_type

/* We will use this flag to indicate whether the problem has negative
   rewards or not.  It starts off FALSE and becomes TRUE if any
   negative reward is found. */
   private  static double gMinimumImmediateReward = 0.0;

/* These specify the size of the problem.  The first two are always required.
   */
   private  static int gNumStates = 0;
   private  static int gNumActions = 0;
   private  static int gNumObservations = 0;   /* remains zero for MDPs */
   private static ArrayList<String> actionName = new ArrayList<String>();
/*  We need two sets of variable for the probabilities and values.  The first
    is an intermediate representation which is filled in as the MDP file
    is parsed, and the other is the final sparse reprsentation which is
    found by converting the interemediate representation.  As aresult, we 
    only need to allocate the intermediate memory while parsing. After parsing
    is completed and we are ready to convert it into the final sparse 
    representation, then we allocate the rest of the memory.
    */

/* Intermediate variables */

  private  static  double[][][] IP = null;  /* Transition Probabilities */

  private  static  double[][][] IR = null;  /* Observation Probabilities (POMDP only) */

  private  static double[][] IQ = null;  /* Immediate action-state pair values (both MDP and POMDP) *///[action][states][value]

/* Sparse variables */

  private  static  double[][][] P = null;  /* Transition Probabilities */

  private  static  double[][][] R = null;  /* Observation Probabilities */

  /*private  static   Matrix Q = null;  /* Immediate values for state action pairs.  These are
	    expectations computed from immediate values. */

/* Normal variables */

/* Some type of algorithms want a place to start off the problem,
   especially when doing simulation type experiments.  The belief
   state is for POMDPs and the initial state for an MDP */

  private  static double[] gInitialBelief = null; 
  private  static int gInitialState = -1;//INVALID_STATE  =   -1;
  private static double[] newBeliefState = null;
   //constructor
    public globalMDP(){
        
    }
    //set methods
    public void setgProblemType(Problem_Type newGT){
        gProblemType = newGT;
    }
    public void setgDiscount(double newD ){
        gDiscount = newD;
    }
    public void setgValueType(Value_Type newVT ){
        gValueType = newVT;
    }
    public void setgMinimumImmediateReward(double newMIR ){
        gMinimumImmediateReward = newMIR;
    }
    public void setgNumStates(int newNS ){
        gNumStates = newNS;
    }
    public void setgNumAction(int newNA ){
        gNumActions = newNA;
    }
    public void setgNumObservations(int newNO ){
        gNumObservations = newNO;
    }
    public void setIP(double[][][] newIP ){
        IP = newIP;
    }
    public void setIR(double[][][] newIR ){
        IR = newIR;
    }
    public void setIQ(double[][] iq){
        IQ = iq;
    }
    public void setIQ(ArrayList<Double> t){
        if(t.isEmpty()) return;
        IQ = new double[gNumActions][gNumStates];
        for(int i = 0; i < gNumActions; i++){
            for(int j = 0; j < gNumStates; j++){                
               IQ[i][j] = t.get(i*gNumStates + j);
            }
        }
    }
    public void setIQ( ){
       
        IQ = new double[gNumActions][gNumStates];
    }
    public void setIQ(int a, int s, double val){
       // if(a < gNumActions && s < gNumStates) 
        IQ[a][s] = val;
    }
    public void setP(){
        P = new double[gNumActions][gNumStates][gNumStates];
    }
    public void setP(double[][][] newP ){
        P = newP;
    }
    public void setR(){
        R = new double[gNumActions][gNumStates][gNumObservations];
    }
    public void setR(double[][][] newR ){
        R = newR;
    }
   /* public void setQ(double[] newQ ){
        Q = newQ;
    }*/
    public void setgInitialBelief(){
        gInitialBelief = new double[gNumStates];
    }
    public void setgInitialBelief(double[] newIB ){
        gInitialBelief = newIB;
    }
    public void setgInitialBelief(int col, double value ){
        gInitialBelief[col] = value;
    }
    public void setgInitialState(int newIS ){
        gInitialState = newIS;
    }
   
    //get methods
     public Problem_Type getgProblemType(){
        return gProblemType;
    }
    public double getgDiscount(  ){
       return gDiscount;
    }
    public Value_Type getgValueType(  ){
        return gValueType;
    }
    public double getgMinimumImmediateReward( ){
        return gMinimumImmediateReward;
    }
    public int getgNumStates( ){
        return gNumStates;
    }
    public int getgNumActions(  ){
        return gNumActions;
    }
    public int getgNumObservations( ){
        return gNumObservations;
    }
    public double[][][] getIP(  ){
        return IP;
    }
    public double[][][] getIR( ){
        return IR;
    }
    public double getIR(int a, int s, int z){
        return IR[a][s][z];
    }
    public double[][] getIQ(  ){
        return IQ;
    }
    public double getIQ(int a, int s){
        return IQ[a][s];
    }
    public double[][][] getP(  ){
        return P;
    }
    public double[][][] getR( ){
        return R;
    }
    /*public Matrix getQ(  ){
        return Q;
    }*/
    public double[] getgInitialBelief( ){
        return gInitialBelief;
    }
     public double getgInitialBelief(int i ){
        return gInitialBelief[i];
    }
    public int getgInitialState( ){
        return gInitialState;
    }
    public void updatePiHat(double[] pi_hat){
        newBeliefState = pi_hat;
    }
    public double getObsProb(int a, int next_state, int obs){
        return IR[a][next_state][obs];
    }
    public double getTransProb(int a, int cur_state, int next_state){
        return IP[a][cur_state][next_state];
    }
    //sum each row of the matrix for transProb and obsProb
    public double sumIPRowValues(int action, int row){
       double sum = 0.0;
       for(int i = 0; i < IP[action][row].length; i++ )
           sum += IP[action][row][i];
       return sum;
}
    public double sumIRRowValues(int action, int row){
        double sum = 0.0;
        for(int i = 0; i < IR[action][row].length; i++)
            sum += IR[action][row][i];
        return sum;
    }
    public void setActionsName(ArrayList<String> list){
        actionName = list;
    }
    public String getActionName(int index){
        if(actionName.isEmpty());
        return actionName.get(index);
    }

    void setIR() {
        IR = new double[gNumActions][gNumStates][gNumObservations];
    }
}