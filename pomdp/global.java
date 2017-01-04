package pomdp;
import mdp.mdpIN.*;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import lpS.SolverResults;
import mdp.globalMDP;
/*
 * Global Constants after initilizing the program.
 */
public class global implements globalIN{

   private static ArrayList<SolverResults> allResults = new ArrayList<SolverResults>();
   private static ArrayList<AlphaList> crossSum = new ArrayList<AlphaList>();
/* Nice to have pretty names for the boolean values 0 and 1. */
   private static String[] boolean_str = BOOLEAN_STRINGS;

/* Strings for the various stopping criteria */
   private static  String[] verbose_mode_str = VERBOSE_MODE_STRINGS;

/* For some reason I could not assign this to 'stderr' here after
   1998.  I am moving this to be one of the first things that gets
   done in main(). */ 
   private static File gStdErrFile = null;

/* The name of the executable of this program. */
   private static String gExecutableName;

/* There are various ways to turn verboseness on and off.  Each elemnt
   of the array defines whether one of these is on or off.  The
   mnemonics in the header file show which ones are which. */
   private static int[] gVerbose = new int[NUM_VERBOSE_MODES];

/**********************************************************************/
/* Temporary variables usefule for scratch work. */
/**********************************************************************/

/* There are times when we need an array of doubles for temporary
   workspace.  These vectors will be gNumStates in length. */
    private static double[] gTempValue = null;
    private static double[] gTempBelief = null;
    private static double[] gTempAlpha = null;
    private static int[][] gObservationPossible = null;
    private static int[] gNumPossibleObservations = null;
    private static int gRequireNonNegativeRewards = 0;
    globalMDP gm ;
/**********************************************************************/
    //Constructors
    public global(){}
    public global(globalMDP gm){
        this.gm = gm;
        for(int x = 0; x < NUM_VERBOSE_MODES; x++)
            gVerbose[x] = 0;
    }
/**********************************************************************/
    @Override
    public void initGlobal(  ) 
    {
      /*
        Sets up and allocates variables that are used globally across
        modules in the program. Currently just allocates a bunch of scratch
        memory areas.
      */

      setgTempBelief(new double[gm.getgNumStates()]);
      setgTempAlpha(new double[gm.getgNumStates()]);
      setgTempValue(new double[gm.getgNumStates()]);

}  /* initGlobal */
/**********************************************************************/
    public void setgTempBelief(double[] tb){
        gTempBelief = tb;
    }
    public void setgTempBelief(int i, double d) {
        gTempBelief[i] = d;
    }
    public void setgTempAlpha(double[] ta){
        gTempAlpha = ta;
    }
    public void setgTempValue(double[] tv){
        gTempValue = tv;
    }
    public void setgObservationPossible(int size1, int size2){
        gObservationPossible = new int[size1][size2];
    }
    public void cleangObservationPossible(){
        gObservationPossible = null;
    }
    public void setgObservationPossible(int a, int z, int b){
        gObservationPossible[a][z] = b;
    }
    public void setgNumPossibleObservations(int size){
        gNumPossibleObservations = new int[size];
    }
    public void setgNumPossibleObservations(int a, int i){
        gNumPossibleObservations[a] = gNumPossibleObservations[a] + i;
    }
    public void cleangNumPossibleObservations(){
        gNumPossibleObservations = null;
    }
    public void setgRequireNonNegativeRewards(int i){
        gRequireNonNegativeRewards = i;
    }
    public int getgVerbose(int a){
        return gVerbose[a];
    }
    public double[] getgTempBelief(){
        return gTempBelief;
    }
    public double[] getgTempAlpha(){
        return gTempAlpha;
    }
    public double[] getgTempValue(){
        return gTempValue;
    }
    public int[][] getgObservationPossible(){
        return gObservationPossible;
    }
    public int getgObservationPossible(int a, int z){
        return gObservationPossible[a][z];
    }
    public int[] getgNumPossibleObservations(){
        return gNumPossibleObservations;
    }
    public int getgNumPossibleObservations(int a){
        return gNumPossibleObservations[a];
    }
    public int getgRequireNonNegativeRewards(){
        return gRequireNonNegativeRewards;
    }
    public void setLpResutl(SolverResults s){
        allResults.add(s);
    }
    public ArrayList<SolverResults> getLpResult(){
        return allResults;
    }

    public void displaygTempBelief() {
        System.out.println("Display gTempBelief");
        for(int i = 0; i < gTempBelief.length; i++)
            System.out.print(gTempBelief[i] +" ");
        System.out.println();
    }
/**********************************************************************/
    @Override
    public void cleanUpGlobal(  ) 
    {
      /*
        Cleans up after problem is solved to free any resources and reset
        anything that the initGlobal() routine did.
      */

       setgTempBelief(null);
       setgTempAlpha(null);
       setgTempValue(null);

    }  /* cleanUpGlobal */
/**********************************************************************/
        @Override
    public int getPid(  ) 
    {
      /* 
         Just a wrapper to the UN*X getpid() function to isolate it in case
         this gets ported to another platform.  Note that for POSIX, the
         'pid_t' type returned by getpid() is an 'int'.
      */
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();  
        String name = runtime.getName(); // format: "pid@hostname"  
        try {  
           return Integer.parseInt(name.substring(0, name.indexOf('@')));  
       } catch (Exception e) {  
            return -1;  
        }  

     // return( (int) getPid() );
    }  /* getPid */
/**********************************************************************/
        @Override
    public void removeFile( String filename ) 
    {
      /* 
         Just a wrapper to the UN*X unlink() function to isolate it in case
         this gets ported to another platform.  
      */

      //unlink( filename );???????????????????????????????????????????????????????????????????????????

    }  /* removeFile */
  
/**********************************************************************/       
    @Override
    public boolean Equal(double x, double y, double e) {
        if(Math.abs(x-y) >= e)
            return true;
        return false;
    }
/**********************************************************************/
    @Override
    public int LessThan(double x, double y, double e) {
        if((x+e)<= y)
            return 1;
        return 0;
    }
/**********************************************************************/
    @Override
    public boolean GreaterThan(double x, double y, double e) {
        if((x +e) > y)
            return true;
        return false;
    }

    
  /**********************************************************************/
    }
