package pomdp;
import java.io.IOException;
import pomdp.paramsIN.PomdpSolveParams;


public interface pomdp_solveIN {
   
    public void initPomdpSolve( PomdpSolveParams param );

    public void cleanUpPomdpSolve( PomdpSolveParams param );

    public void solvePomdp( PomdpSolveParams param )throws IOException;

    /* For now our default policy is just all zeroes.  */
    public AlphaList getDefaultInitialPolicy( );

    /* Some algorithms will solve one iteration of POMDP value iteration
       by breaking the problem into a separate one for each action.  This
       routine will implement the basic structure needed and call the
       appropriate routines depending on the specific algorithm being
       used.  Current algorithms that do it this way: TwoPass, Witness and
       IncrementalPruning */
    public AlphaList improveByQ( AlphaList[][] projection,
                                 PomdpSolveParams param );
    /* This does a single DP step of value iteration for a POMDP.  It
       takes in the previous value function and parameters for solving and
       returns the next or improved solution.  */
    public AlphaList improveV( AlphaList prev_alpha_list,
                               PomdpSolveParams param );
}
