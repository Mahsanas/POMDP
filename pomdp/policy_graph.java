
package pomdp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import mdp.globalMDP;

public class policy_graph implements policy_graphIN {
    globalMDP gm;
    
    public policy_graph(globalMDP gm){
        this.gm = gm;
    }
    @Override
    public void displayPolicyGraph(AlphaList list ) {
    /*
      Displays the policy graph to the file handle specified.

      The policy graph will be output with the format of one line per node
      in the policy graph:

      ID  ACTION    OBS1  OBS2 OBS3 ... OBSN

      where ID is the id of the alpha vector in the current set, ACTION is
      the action for this vector, and OBS1 through OBSN are the id's of
      the vectors in the previous epoch's alpha vector set (one for each
      observation). 
    */
      int z;

     assert( list != null): "List is NULL.displayPolicyGraph" ;
      System.out.println("Display Policy Graph: ");
      AlphaNode temp = list.head;

      while( temp != null ) {

        System.out.print( temp.id +"-> "+ temp.action  +" -> " + temp.obs);

        if ( temp.obs_source != null )
          for ( z = 0; z < gm.getgNumObservations(); z++ ) {

            if ( temp.obs_source[z] != null)
              System.out.print(temp.obs_source[z].id +" " );
            else
              /* We put an 'X' when that observation is impossible. */
              System.out.print(  "    X" );
          }  /* for z */          

        else
          System.out.print("[No information available]" );

        System.out.println();
        temp = temp.next;
      }  /* while */

    }  /* displayPolicyGraph */
    /**********************************************************************/
    @Override
    public void writePolicyGraph( String filename, AlphaList list ) {
    /*
      Displays the policy graph of a set of vectors to the filename
      specified. 
    */
        try{
       // Create file 
           FileWriter fstream = new FileWriter(filename);
           BufferedWriter out = new BufferedWriter(fstream);
            AlphaNode temp = list.head;

          while( temp != null ) {

            out.write( temp.id +"-> "+ temp.action +" ");

            if ( temp.obs_source != null )
              for (int z = 0; z < gm.getgNumObservations(); z++ ) {

                if ( temp.obs_source[z] != null)
                  out.write(temp.obs_source[z].id );
                else
                  /* We put an 'X' when that observation is impossible. */
                 out.write(  "    X" );
              }  /* for z */          

            else
              out.write("[No information available]" );

            out.write("\n");
            temp = temp.next;
          }

           out.close();
       }catch (Exception e){//Catch exception if any
           System.err.println("Error: " + e.getMessage());
       }
   
 
       displayPolicyGraph( list );

    }  /* writePolicyGraph */
    /**********************************************************************/
    @Override
   public void showPolicyGraph( AlphaList list ) {
    /*
      Displays the policy graph for an alpha vector set to stdout.
    */
      displayPolicyGraph( list );
    }  /* showPolicyGraph */

    
    
}
