package mdp;
//import src.mdp.mdp_commonIN.*;
import mdp.mdpIN.*;
import mdp.imm_rewardIN.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class imm_reward{
    globalMDP gm ;
    //sparse_matrix sm = new sparse_matrix();

    /* As we parse the file, we will encounter only one R : * : *.... line
   at a time, so we will keep the intermediate matrix as a global
   variable.  When we start to enter a line we will initial it and
   when we are finished we will convert it and store the sparse matrix
   into the node of the linked list.  */ 
    //I_Matrix gCurIMatrix = null;

/* We will have most of the information we need when we first start to
  parse the line, so we will create the node and put that information
  there.  After we have read all of the values, we will put it into
  the linked list.  */
   
    //ImmRewardList gImmRewardList = new ImmRewardList() ;
    LinkedList gImmRewardList = new LinkedList();
    int[] gCurImmRewardNode = null;//node
    ArrayList<Double> gRewardValues = null;
/* This is the actual list of immediate reward lines */
    
    public imm_reward(globalMDP gm){
        this.gm = gm;
    }
/**********************************************************************/
       public void destroyImmRewards() {
        
        if( gImmRewardList != null ) {
            
            gImmRewardList.iterator().remove();
            gRewardValues.clear();
        }  

    }  /* destroyImmRewardList */
    /**********************************************************************/
    public LinkedList appendImmRewardList( LinkedList list, LinkedList node ) {
        if(list.isEmpty())
            return node;
        while(node.iterator().hasNext())
            list.add(node.iterator().next());
        return( list );

    }  /* appendImmRewardList */
    /**********************************************************************/
   
    public void newImmReward( int action, int cur_state, int next_state, int obs ) {

        /* First we will allocate a new node for this entry */
        //gCurImmRewardNode = new Imm_Reward_List[gCurImmRewardNode];
        
        gCurImmRewardNode[0] = action;
        gCurImmRewardNode[1] = cur_state;
        gCurImmRewardNode[2] = next_state;
        gCurImmRewardNode[3] = obs;
        
        gImmRewardList.add(gCurImmRewardNode);
        gRewardValues.add(0.0); 
      
    }  /* newImmReward */
    /**********************************************************************/
  
    public void enterImmReward(int action, int cur_state, int next_state, int obs, 
                double value ) {

        /* cur_state is ignored for a POMDP, and obs is ignored for an MDP */

        gCurImmRewardNode[0] = action;  
        gCurImmRewardNode[1] = cur_state;
        gCurImmRewardNode[2] = next_state;
        gCurImmRewardNode[3] = obs;
        
        gImmRewardList.add(gCurImmRewardNode);
        gRewardValues.add(value);

    }  /* enterImmReward */
    /**********************************************************************/
  
    public void setgRewardValues(ArrayList<Double> reward){
        gRewardValues = reward;
    }
    public double getImmediateReward( int action, int cur_state, int next_state,
                    int obs ) {
        LinkedList temp = gImmRewardList;
        double return_value = 0.0;

        if(( action < 0) || (action > gm.getgNumStates())
             || (cur_state <0)|| (cur_state > gm.getgNumStates())
             || (next_state <0) || (next_state > gm.getgNumStates()));

       if( !temp.isEmpty()) {
            Iterator it = temp.iterator();
            int count = 0;
            while(it.hasNext()){
                int[] intArr = (int[]) it.next();
                if(intArr.length != 4);
                if(intArr[0] ==  action && intArr[1] == cur_state && intArr[2] == next_state && intArr[3] == obs){
                    return_value = gRewardValues.get(count);
                    break;
                }
                count++;
            }
        }
        return( return_value );

    }  /* getImmediateReward */

 
 
    /**********************************************************************/


    }
