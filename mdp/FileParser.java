package mdp;
import Enum.Value_Type;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
//import incpomdp.mdp.parse_hashIN.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
//import incpomdp.mdp.sparse_matrixIN.*;
import mdp.imm_rewardIN.*;
import java.util.ArrayList;
import java.util.Stack;
import java.util.StringTokenizer;

public class FileParser implements mdpIN{
    
    globalMDP gm ;
    imm_reward ir = new imm_reward(gm);
    public static final int NOT_PRESENT  =   -99;
    public static final double EPSILON = 0.00001;
    //public enum Value_Type{REWARD_value_type, COST_value_type } ;
    double[][][] TransProb;//trans_Prob double[action1][stateN][stateN]
    double[][][] ObsProb;//observation probl double[aciton][stateN][obsN]
    ArrayList<String> rewardPair = new ArrayList<String>();//string[action][start state][end state][observation]
    ArrayList<Double> reward = new ArrayList<Double>();
    int distT = 0;
    ArrayList<String> actions = new ArrayList<String>();
    ArrayList<String> states = new ArrayList<String>();
    ArrayList<String> observations = new ArrayList<String>();
    public FileParser(globalMDP gm){
        this.gm = gm;
    }
    public int parse(String filename)throws FileNotFoundException, IOException{
        if(filename == null){
            return 0;
        }
        FileInputStream fstream = new FileInputStream(filename);
        // Get the object of DataInputStream
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        StringTokenizer st;
        int count = 0;
        int actCount = -1;//action counter
        int obsCount = -1;//observation counter
        int rowcount = 0;
        int rewardCount = 0;
        int startflag = -1, endflag = -1;//flag to indicate whether to input matrix in arrays
        Stack prev = new Stack();
        //Read File Line By Line
        while ((strLine = br.readLine()) != null)   {
            //if(strLine.length() == 0)
              //  continue;
            //if(count < 6){
                st = new StringTokenizer(strLine);
                ArrayList<String> ta = new ArrayList<String>();
                while(st.hasMoreTokens()){
                   ta.add(st.nextToken(" "));
                }
               if(ta.isEmpty())
                   continue;
               String test = ta.get(0);
              // System.out.println(test);
               
               if(test.equals("discount:"))                
                  gm.setgDiscount(Double.parseDouble(ta.get(1)));              
               if(test.equals("values:")){
                   if(ta.get(1).equals("reward"))
                        gm.setgValueType(Value_Type.REWARD_value_type);
                   else
                       gm.setgValueType(Value_Type.COST_value_type);
               }
               if(test.equals("states:")){
                   if(ta.size() == 2){
                       gm.setgNumStates(Integer.parseInt(ta.get(1)));
                   }else{
                       for(int i=1; i < ta.size(); i++)
                           states.add(ta.get(i));
                       gm.setgNumStates(ta.size() - 1 );
                        gm.setgInitialBelief();
                   }

               }
               if(test.equals("actions:")){
                   for(int i=1; i < ta.size(); i++)
                       actions.add(ta.get(i));
                   gm.setgNumAction(ta.size()-1);
                   //System.out.println(gm.getgNumActions());
               }
               if(test.equals("observations:")){
                   for(int i=1; i < ta.size(); i++)
                       observations.add(ta.get(i));
                   gm.setgNumObservations(ta.size()-1);
                   //gm.setIR();
               }
               if(test.equals("start:")){
                   double[] t = new double[ta.size() -1];
                   for(int i = 1; i < ta.size(); i++)
                       t[i-1] = Double.parseDouble(ta.get(i));
                    gm.setgInitialBelief(t);
               }
               if(count == 6){
                    TransProb = new double[gm.getgNumActions()][gm.getgNumStates()][gm.getgNumStates()];
                    ObsProb = new double[gm.getgNumActions()][gm.getgNumStates()][gm.getgNumObservations()];
               }
               
                if(test.equals("T:")){
                    prev.push(test);
                    for(int a = 0; a < actions.size(); a++)
                        if(ta.get(1).equals(actions.get(a))){
                            actCount = a;//
                           // System.out.println(actCount);
                        }
                    startflag = count + 1;
                    endflag = count + gm.getgNumStates();
                }
                if(test.equals("O:")){
                    prev.push(test);
                    for(int a = 0; a < actions.size(); a++)
                        if(ta.get(1).equals(actions.get(a))){
                            obsCount = a;
                            //System.out.println(obsCount);
                        }
                    startflag = count + 1;
                    endflag = count + gm.getgNumStates();
                }
                //enter trans_prob
                if(count >= startflag && count <= endflag && prev.peek().equals("T:")){
                    for(int i = 0; i < ta.size(); i++){
                        //System.out.print(ta.get(i) +" ");
                        TransProb[actCount][count - startflag][i] = Double.parseDouble(ta.get(i));
                }
                   // System.out.println();
                }
                if(count >= startflag && count <= endflag && prev.peek().equals("O:")){
                    for(int i = 0; i < ta.size(); i++){
                        //System.out.print(ta.get(i) +" ");
                        ObsProb[obsCount][count - startflag][i] = Double.parseDouble(ta.get(i));
                    }
                   // System.out.println();
                }
           //Imm_reward pair action: start state: end state: observation: reward(double)
                if(test.equals("R:")){
                    
                    int ct =ta.size()-1;
                    int ax= 0;
                    for(int i = 1; i < ct; i++)
                        if(!ta.get(i).equals(":")){
                            rewardPair.add(ta.get(i));
                        }
                    reward.add(Double.parseDouble(ta.get(ct)));
                    rewardCount++;
                }
              count++;
        }
      //Close the input stream
        in.close();
        gm.setIP(TransProb);
        gm.setIR(ObsProb);
        gm.setIQ(reward);
        IRList();

      
        return 1;
}
    public void IRList(){
        if(rewardPair.isEmpty());
        int size = rewardPair.size()/4;
        int[][] RewardList = new int[size][4];
        int c = 0;
        for(int i = 0; i < size; i++){
           for( c = c + i*4; c < rewardPair.size(); c++){
                RewardList[i][0] = checkAction(rewardPair.get(c));
                
                RewardList[i][1] = checkState(rewardPair.get(c));
                c++;
                RewardList[i][2] = checkState(rewardPair.get(c));
                c++;
                RewardList[i][3] = checkObs(rewardPair.get(c));
                c++;
                
            }
        }
          
        
        /*for(int a = 0; a < RewardList.length; a++){
            for(int b = 0; b < 4; b++){
                System.out.print(RewardList[a][b]);
            }
            System.out.println();
        }*/
    }
    public int checkAction(String a){
        if(actions.isEmpty());
        for(int i = 0; i < actions.size(); i++){
            if(actions.get(i).equals(a));
              return i;
        }
        return NOT_PRESENT;
    }
    public int checkState(String a){
        if(states.isEmpty());
        for(int i = 0; i < states.size(); i++){
            if(states.get(i).equals(a));
              return i;
        }
        return NOT_PRESENT;
    }
    public int checkObs(String a){
        if(observations.isEmpty());
        for(int i = 0; i < observations.size(); i++){
            if(observations.get(i).equals(a));
              return i;
        }
        return NOT_PRESENT;
    }

   
   
}

