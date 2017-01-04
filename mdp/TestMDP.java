
package mdp;

import java.io.FileNotFoundException;
import java.io.IOException;

public class TestMDP {
   
    public static void main(String[] args) throws FileNotFoundException, IOException {
         globalMDP gm = new globalMDP();
        System.out.println("testing mdp");        
        //String filename = "tiger.txt";
        String filename = "tiger.txt";
         mdp MDP = new mdp(gm);
        MDP.readMDP(filename);
        //System.out.println(gm.getIP().length);
      //  MDP.computeRewards();
      // System.out.println( MDP.verifyIntermediateMDP());
        double[][] IQ = gm.getIQ();
        for(int i = 0; i < gm.getgNumActions(); i++){
            for(int j = 0; j < gm.getgNumStates(); j++){
                System.out.print(IQ[i][j] +" ");
            }
        System.out.println();
        }
        for(int j = 0; j < gm.getgNumStates(); j++)
            System.out.print(gm.getgInitialBelief(j)+"  ");
        //System.out.println(gm.getgValueType());
         /*double[] a= gm.getgInitialBelief();
         for(int i = 0; i < a.length; i++)
             System.out.print(a[i] +"  ");*/
        /*double[][][] prob = gm.getIP();
        for(int i = 0; i < gm.getgNumActions(); i++){
            for(int j = 0; j < gm.getgNumStates(); j++){
                for(int k = 0; k < gm.getgNumStates(); k++)
                    System.out.print(prob[i][j][k]+"  ");
                System.out.println();
            }
             System.out.println();
        }*/
      
    }

}
