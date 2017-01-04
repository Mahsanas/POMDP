
package pomdp;
/*
 * Node of Alpha List
 */       
     public class AlphaNode {
  
    /* Each vector can have an associated action, or the header of the
         list can have an action for all vectors in the list. */ 
      public int action = -1;

      /* When dealing with a set that represents a projection of the
         previous value function, we may also want to keep track of the
         observation used in the projection.  For othert sets this
         variable will have no useful value. This will only have meaning
         in the header of a list. */
       int obs = -1 ;

      /* A vector of length gNumStates representing the actual vector */
      public double[] alpha = null;

      /* A unique identifier for elements, usually the list position */
       int id = -1;

      /* Pointer to next vector i the list. */
        AlphaNode next = null;

      /* Sometimes we will want to save a witness point for a vector. This
         is the point that we used to determine that this vector was indeed
         a useful vector.  This will only exist for vectors in a
         parsimonious representation. */
       double[] witness = null ;//= new double[100];

      /* Also, when we have a projection set, we will want to maintain a
         pointer into the previous alpha vector set which this vector is
         the projection of. This field is used when the vector is a
         projection of a previous alpha list vector. */
       AlphaNode prev_source = null;

      /* It will also be useful (especially for the generalized cross-sum)
         to  know the two vectors that were immediately used to create a
         vector.  These two fields hold this information when a vector is
         created via a cross sum operation. */
       AlphaNode first_source = null;
       AlphaNode second_source = null;

      /* When constructing V_t from V_{t-1} we will use a vector from
         V_{t-1} for each observation in the construction of a vector
         in V_t.  We would like to keep track of which ones we used
         so that we can trace out the policy tree.  This array will be
         pointers, but the confusing part is that at one point they are
         pointers into the projection sets and at another they are
         pointers into the previous alpha list.  They are mostly the
         former, but just before the policy graph stuff is used these are
         set to point directly into the previous alpha list.*/
       AlphaNode[] obs_source = null;

      /* A flag to use to mark and unmark vectors.  Right now Sondik uses
         this to determine which vectors have already been looked at. */
       int mark = -1; 
      //constructor 
    public AlphaNode(double[] alpha, int action){
        this.alpha = alpha;
        this.action = action;
    }

    public AlphaNode() {
       
    }
    
    
}


