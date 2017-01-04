package mdp;
import Enum.Value_Type;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
//import src.mdp.mdp_commonIN.*;
import mdp.mdpIN.*;
import mdp.imm_rewardIN.*;
//import incpomdp.mdp.sparse_matrixIN.*;

public class mdp{
     // #define MDP_C
    imm_reward ir ;//= new imm_reward();
    public double DOUBLE_DISPLAY_PRECISION = 4;
    public static final double EPSILON = 0.00001;  /* tolerance for sum of probs == 1 */
    globalMDP gm ;//= new globalMDP();
    
    //static String[] value_type_str = VALUE_TYPE_STRINGS;
    
    double[][][] IP, IR;
    
   public mdp(){}
   public mdp(globalMDP gm){
       this.gm = gm;
   }

   
    /***************************************************************************/
    public double[] newBeliefState() {

        return (new double[gm.getgNumStates()]);
    }  /* *newBeliefState */


    /***************************************************************************/
    public double[] transformBeliefState(double[] pi,
            int a,
            int obs) {
        double[] pi_hat = new double[gm.getgNumStates()];
        double denom;
        int i, j, z, cur_state, next_state;
        /* zero out all elements since we will acumulate probabilities
          as we loop */
        for (i = 0; i < gm.getgNumStates(); i++)
            pi_hat[i] = 0.0;
      
         //function pi_hat[nextstate] = O(a, curstate, nextstate)*sigma(T(a,curstate, nextstate)*b(s))
        for (next_state = 0; next_state < gm.getgNumStates(); next_state++) {
            double obsProb = gm.getObsProb(a, next_state, obs);
            double sum = 0.0;
            for (cur_state = 0; cur_state < gm.getgNumStates(); cur_state++) {
                sum += gm.getTransProb(a,cur_state,next_state)*pi[cur_state];
            } 
            pi_hat[next_state] = obsProb*sum;
        } 

        /* Normalize */
        denom = 0.0;
        for (i = 0; i < gm.getgNumStates(); i++) {
            denom += pi_hat[i];
        }

        for (i = 0; i < gm.getgNumStates(); i++) {
            pi_hat[i] /= denom;
        }
        gm.updatePiHat(pi_hat);
        return (pi_hat);
    }  /* transformBeliefState */


    /**********************************************************************/
    public void copyBeliefState(double[] copy, double[] pi) {
        /*
         */
        int i;
        if ((pi == null) || (copy == null)) {
            return;
        }

        for (i = 0; i < gm.getgNumStates(); i++) {
            copy[i] = pi[i];
        }

    }  /* copyBeliefState */


    /**********************************************************************/
    public void displayBeliefState(File file, double[] pi) {
        int i;

        System.out.println(file + " " + DOUBLE_DISPLAY_PRECISION + " " + pi[0]);
        for (i = 1; i < gm.getgNumStates(); i++) {
            System.out.println(DOUBLE_DISPLAY_PRECISION + "  " + pi[i]);
        }  /* for i */

    }  /* displayBeliefState */


    /***************************************************************************/
    public int readMDP(String filename) {
        /*
        This routine returns 1 if the file is successfully parsed and 0 if not.
         */
        if (filename.isEmpty()) {
            System.out.println("<NULL> MDP filename: %s.\n " + filename);
            return (0);
        }
        //FILE *file;
       
        FileParser ps = new FileParser(gm);
        try {
            if (ps.parse(filename) == 0) {
                System.out.println("MDP file"+filename+" was not successfully parsed!");
                return (0);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(mdp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(mdp.class.getName()).log(Level.SEVERE, null, ex);
        }
        
            
        /* After the file has been parsed, we should have everything we need
        in the final representation.  
         */
        return (1);
    }  /* readMDP */



    /************************************************************************/
   
    public int verifyIntermediateMDP() {
        /*
        This routine will make sure that the intermediate form for the MDP
        is valid.  It will check to make sure that the transition and
        observation matrices do indeed specify probabilities.
        
        There is a similar routine in the parser.y file, which is nicer
        when parsing a POMDP file, but this routine is needed when we 
        are creating the POMDP through a program.  In this case there
        will be no parsing and thus no logging of errors.
         */
        int a, i, j, obs;
        double sum;
        //check TransProb
        for (a = 0; a < gm.getgNumActions(); a++) {
            for (i = 0; i < gm.getgNumStates(); i++) {
                sum = gm.sumIPRowValues( a,  i);
                if ((sum < (1.0 - EPSILON)) || (sum > (1.0 + EPSILON))) {
                    return (0);
                }
            } /* for i */
        }

       //check obsProb
            for (a = 0; a < gm.getgNumActions(); a++) {
                for (j = 0; j < gm.getgNumStates(); j++) {
                    sum = gm.sumIRRowValues(a, j);
                    if ((sum < (1.0 - EPSILON)) || (sum > (1.0 + EPSILON))) {
                        return (0);
                    } /* if sum not == 1 */
                }  /* for j */
            }
        //}

        return (1);
    }  /* verifyIntermediateMDP */


    /************************************************************************/
   
    public void deallocateIntermediateMDP() {
        /*
        This routine is made available in case something goes wrong
        before converting the matrices from the intermediate form
        to the final form.  Normally the conversion routine convertMatrices()
        will deallocate the intermediate matrices, but it might be desirable
        to get rid of them before converting (especially if something
        has gone wrong) so that things can be started over.
         */
        gm.setIP(null);
        gm.setIR(null);
        gm.setgInitialBelief(null);
        gm.setIQ();

    }  /* deallocateIntermediateMDP */


    /**********************************************************************/
    public void computeRewards() {
        int a, i, j, z, obs,next_state;
        double sum, inner_sum;

        /* For the some problems, where we may want to shift all the reward
        values to remove negative rewards, it will help to maintain the
        minimum reward. Because all unrepresented values are zero, this
        is our starting point. */
        //double this.gMinimumImmediateReward = gMinimumImmediateReward;

        /* Now do the expectation thing for action-state reward values */
        gm.setIQ();//initialize IQ
        IP = gm.getIP();
        IR = gm.getIR();
       if(IP.length == 0 || IR.length == 0)
           return;
        for (a = 0; a < gm.getgNumActions(); a++) {
            for (i = 0; i < gm.getgNumStates(); i++) {

                sum = 0.0;

                /* Note: 'j' is not a state. It is an index into an array */
                for (j = 0; j < gm.getgNumStates(); j++) {

                    next_state = j;

                    if (gm.getgProblemType() == Problem_Type.POMDP_problem_type) {

                        inner_sum = 0.0;

                        /* Note: 'z' is not a state. It is an index into an array */
                        for (z = 0; z < (IR[a][next_state].length); z++) {

                            obs = z;

                            inner_sum += IR[a][next_state][z]
                                    * ir.getImmediateReward(a, i, next_state, obs);
                        }  /* for z */
                     /* if POMDP */ /*else /* it is an MDP  {
                        inner_sum = ir.getImmediateReward(a, i, next_state, 0);
                    }*/

                    sum += IP[a][i][j] * inner_sum;
                    }
                //}  /* for j */

                /* Update the minimum reward we are maintaining. */
                if (gm.getgMinimumImmediateReward() > sum) {
                    gm.setgMinimumImmediateReward(sum);
                }


                gm.setIQ( a, i, sum);
                }    
            }  /* for i */
        }

    }  /* computeRewards */

    /**********************************************************************/
    public int writeMDP(String filename) {

        int a, i, j, obs;
        IP = gm.getIP();
        IR = gm.getIR();
        File fs = new File(filename);
        FileWriter fw = null;
        BufferedWriter mywriter;
        try {
            fw = new FileWriter(fs);
            mywriter = new BufferedWriter(fw);
            mywriter.write("discount: "+ gm.getgDiscount());
            mywriter.newLine();
            
            if (gm.getgValueType() == Value_Type.COST_value_type) {
                mywriter.write("values: cost");
                mywriter.newLine();
            } else {
                mywriter.write("values: reward\n");
                mywriter.newLine();
            }

           mywriter.write("states: " + gm.getgNumStates());
           mywriter.newLine();
           mywriter.write("actions: " + gm.getgNumActions());
           mywriter.newLine();
            if (gm.getgProblemType() == Problem_Type.POMDP_problem_type) {
                mywriter.write("observations: " + gm.getgNumObservations());
            }
            //write transProb
            for (a = 0; a < gm.getgNumActions(); a++) {
                mywriter.write("T: "+ gm.getActionName(a));
                mywriter.newLine();
                for (i = 0; i < gm.getgNumStates(); i++) {
                    for (j = 0;j < gm.getgNumStates();j++) {
                        mywriter.write(String.valueOf(IP[a][i][j]) +"  ");
                    }
                    mywriter.newLine();
                }
                mywriter.newLine();
            }

       // if (gm.getgProblemType() == Problem_Type.POMDP_problem_type) {
            for (a = 0; a < gm.getgNumActions(); a++) {
                mywriter.write("O: " +gm.getActionName(a));
                mywriter.newLine();
                for (j = 0; j < gm.getgNumStates(); j++) {
                    for (obs = 0; obs < gm.getgNumObservations(); obs++) {
                        mywriter.write(String.valueOf(IR[a][j][obs]) +"  ");
                    }
                    mywriter.newLine();
                }
                mywriter.newLine();
            }
        //}
      
            fw.close();
        } catch (IOException ex) {
            System.out.println("writeMDP file errors!");
            return 0;            
        }
        return 1;
    }
    /* writeMDP */

    /**********************************************************************/
    public void deallocateMDP() {
        
        gm.setP(null);
        gm.setR(null);
        gm.setgInitialBelief(null);        
        ir.destroyImmRewards();
    }  /* deallocateMDP */


    /**********************************************************************/
   
    public void displayMDPSlice(int state) {
        /*
        Shows the transition and observation probabilites (and rewards) for
        the given state.
         */
        int a, j, obs;

        if ((state < 0) || (state >= gm.getgNumStates()) || (gm.getgNumStates() < 1)) {
            return;
        }
        IP = gm.getIP();
        IR = gm.getIR();
        System.out.println("MDP slice for state: " + state);

        //show transProb
        System.out.println("Show Transtion probabilites for state = "+ state);
        for (a = 0; a < gm.getgNumActions(); a++) {
            for (j = 0; j < IP[a].length; j++) {
                System.out.println(IP[a][j][state]);
            }
        }
        
        //show obsProb
        System.out.println("Show observation probabilities for state = " + state);
            for (a = 0; a < gm.getgNumActions(); a++) {
                for (obs = 0; obs < IR[a].length; obs++) {
                    System.out.print(IR[a][state][obs]);
                }
            }
     

    }  /* displayMDPSlice */

    /**********************************************************************/
}
