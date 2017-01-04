
package pomdp;

public interface policy_graphIN {
    /* Displays the policy graph to the file handle specified.

      The policy graph will be output with the format of one line per node
      in the policy graph:

      ID  ACTION    OBS1  OBS2 OBS3 ... OBSN

      where ID is the id of the alpha vector in the current set, ACTION is
      the action for this vector, and OBS1 through OBSN are the id's of
      the vectors in the previous epoch's alpha vector set (one for each
      observation).  */
    public void displayPolicyGraph(  AlphaList list );

    /* Displays the policy graph of a set of vectors to the filename
      specified.  */
    public void writePolicyGraph( String filename, AlphaList list);

    /* Displays the policy graph for an alpha vector set to stdout.  */
    public void showPolicyGraph( AlphaList list );

}
