package pomdp;
import Enum.Value_Type;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import mdp.globalMDP;
import mdp.mdp;

/*
 * Alpha Vectors Rountines
 */
/**********************************************************************/ 
public class alpha{
    //indicate marks
    private globalMDP gm;
    private global g ;
    private mdp MDP;
    private static AlphaNode[] array;
    int TRUE = 1, FALSE = 0;
    pomdp POMDP;
    AlphaNode best_ptr = new AlphaNode();
    double best_value = 0;
    AlphaList newlist1 = null;
/**********************************************************************/
    //Constructors
    public alpha(){}
    public alpha(globalMDP gm, global g, mdp MDP,pomdp POMDP){
        this.gm = gm;
        this.g = g;
        this.MDP = MDP;
        this.POMDP = POMDP;
    }
/**********************************************************************/
   public  double[] newAlpha( ){
    /* 
     Allocate memory for an alpha vector, whose size is determined by
     the number of states.
    */
        
        return (  new double[gm.getgNumStates()]);
    }  /* newAlpha */
/**********************************************************************/
    public double[] duplicateAlpha( double[] alpha ) 
    {
    /*
        Makes a copy of the alpha vector also allocating the memory for it. 
        */
        double[] temp;
        int i;

        if ( alpha == null)
            return null;
        temp = alpha;
        return ( temp );
    }  /* duplicateAlpha */
/**********************************************************************/  
    public void copyAlpha( double[] dest, double[] src ) 
    {
        /*
        Assumes the memory has been allocated and simply copies the values
        from the src to the dest argument.
        */
        int i;

        if (( src == null) || ( dest == null))
            return;

        for( i = 0; i < gm.getgNumStates(); i++)
            dest[i] = src[i];

    }  /* copyAlpha */
/**********************************************************************/  
    public void destroyAlpha( double[] alpha ) 
    {
        /*
        Free the memory for an alpha vector. 
        */
        if ( alpha != null )
         alpha = null;
    }  /* destroyAlpha */
/**********************************************************************/   
    public boolean sameAlpha( double[] alpha1, double[] alpha2, double epsilon ) 
    {
    /* 
     Compares two alpha vectors and determines if they are the identical
     vector.  The tricky part here is that there is floating point
     comparisons that we need to deal with and that can have a
     substantial effect on the algorithm. 
    */
        int i;

        if (( alpha1 == null) && (alpha2 == null))
            return true;
        
        if (( alpha1 == null ) || (alpha2 == null))
            return false;
        
        if(alpha1.length != alpha2.length)
            return false;
        
        for (i = 0; i < gm.getgNumStates(); i++)
            if ( ! Equal( alpha1[i], alpha2[i], epsilon ))
              return (false);
        
        return (true);
    }  /* sameAlpha */
/**********************************************************************/   
    public boolean isZeroAlpha( double[] alpha, double epsilon ) 
    {
        /* 
         Just checks to see if all components are zero.  Will return
         TRUE if it is zero of if NULL is sent in and FALSE otherwise. 
        */
        int i;

        if ( alpha == null)
            return ( true );

        for (i = 0; i < alpha.length; i++)
            if ( ! Equal( alpha[i], 0.0, epsilon ))
                 return ( false );

        return ( true );

    } /*  isZeroAlpha  */
/**********************************************************************/   
    public void displayAlpha( double[] alpha ) 
    {

        /*
        Display the vector to the file stream putting no extra whitespace
        before or after the vector.
        */
        int i;

        if ( alpha == null) {
          System.out.println( "NULL Alpha");
          return;
        }
        //System.out.print( alpha[0]+" " );
        for (i = 0; i < alpha.length; i++) {
          System.out.print(alpha[i] +" ");
        }  /* for i */
         System.out.println();
    }  /* displayAlpha */
/**********************************************************************/    
    public void showAlpha( double[] alpha ) 
    {
        /*
        Displays vector to stdout.
        */
        displayAlpha( alpha );
        
    }  /* showAlpha */
/**********************************************************************/   
    public boolean isLexicographicallyBetterAlpha( double[] first_alpha, double[] second_alpha, double epsilon )
    {
        /* 
         Does a lexicographic check on two vectors, given the two vectors.
         Return TRUE if first_alpha is lexicographically better than
         second_alpha. 
        */
         int i;

      /* This loops iterates until it finds two components that are not
         exactly the same.  */
      for ( i = 0; i < first_alpha.length; i++ ) {

        if ( isBetterValue( first_alpha[i], 
                            second_alpha[i], epsilon )) 
          return ( true );

        if ( isBetterValue( second_alpha[i], 
                            first_alpha[i], epsilon )) 
          return ( false );

      }  /* for i */

      /* If we get to here then they really are equal, so the first one is
         not better than the second. */
      return ( false );

    }  /* isLexicographicallyBetterAlpha */
/**********************************************************************/    
    public boolean isLexicographicallyBetter( AlphaNode first_alpha,
                                          AlphaNode second_alpha,
                                          double epsilon ) 
    {
    /* Does a lexicographic check on two vectors pointed two by
     the two list nodes.
    */
        return ( isLexicographicallyBetterAlpha( first_alpha.alpha,
                                           second_alpha.alpha,
                                           epsilon ));

    }  /* isLexicographicallyBetter */
/**********************************************************************/   
    public boolean isDominatedVector( double[] alpha1, double[] alpha2 ) 
    {
        /* 
         Returns true if alpha2 is component-wise dominated by alpha1. The
         assumption here is that with two identical vectors neither would be
         considered dominating toward the other.  
        */
        int i;

        assert( alpha1 != null && alpha2 != null):
          "Vector(s) is NULL isDominatedVector." ;

        /* We really can get away with an epsilon of 0.0 here; I can prove
         it!  */

        for (i = 0; i < alpha1.length; i++) 
            if ( alpha1[i] <= alpha2[i] )
              return ( false );

        return ( true );
    } /* isDominatedVector */
/**********************************************************************/

/**********************************************************************/
/*******************  Obs_Source Routines         **********************/
/**********************************************************************/
   public AlphaNode[] newObsSourceArray(  ) 
    {
    /*
    Just a convenient function for getting a pointer to an array of
    AlphaList pointers for the obs_source field of the AlphaList
    nodes. Initializes the array to have all NULL vectors.
    */
        AlphaNode[] node = new AlphaNode[gm.getgNumObservations()];

        return ( node);

    }  /* *newObsSourceArray */
    
/**********************************************************************/   
    public AlphaNode[] duplicateObsSourceArray( AlphaNode[] orig_obs_source ) 
    {
        /*
        Allocates memory for and copies the obs_source array and returns a
        pointer to the new memory.
        */
        AlphaNode[] new_obs_source = new AlphaNode[gm.getgNumObservations()];
        int z;

        for( z = 0; z < gm.getgNumObservations(); z++ )
            new_obs_source[z] = orig_obs_source[z];

        return ( new_obs_source );
    }  /* *duplicateObsSourceArray */
/**********************************************************************/

/**********************************************************************/
/******************  Alpha List Node Routines    **********************/
/**********************************************************************/    
    public AlphaNode newAlphaNode( double[] alpha, int action ) 
    {
        /*
        Allocates the memory for and sets initial values for an alpha list
        node. 
        */
        AlphaNode temp = new AlphaNode(alpha, action);

        return ( temp );
        }  /* newAlphaNode */
/**********************************************************************/       
    public AlphaNode newAlphaNodeObsSource( double[] alpha, 
                       int action ) 
    {
        AlphaNode node;

        node = newAlphaNode( alpha, action );
        node.obs_source = new AlphaNode[gm.getgNumObservations()];
        return( node );
} /* newAlphaNodeObsSource */
/**********************************************************************/   
    public void destroyAlphaNode( AlphaNode temp ) 
    {
    /*
    Frees the memory for an alpha list node. Also free some supplemental
    memory that might be hanging off this. 
    */
        assert( temp != null): "Cannot destroy NULL node." ;
        
        destroyAlpha( temp.alpha );

        /* We assume that memory was allocated for this for the sole purpose
         of this node, so we should free it now. Note that it does not
         free anything that these point to since they are actually part of
         a separate list. */
        if ( temp.obs_source != null) 
         temp.obs_source = null;

        /* We also assume that if a witness point was set here, that we need
         to free this memory also. */
        if ( temp.witness != null ) 
         temp.witness = null;

        /* Note that we *do not* clear out memory of 'source' and other
         fields because these are just pointers into other data structures
         whose memory might already have been freed or which is still in
         use. */

        temp = null;
    }  /* destroyAlphaNode */
/**********************************************************************/  
    public AlphaList appendNodeToAlphaList( AlphaList list, AlphaNode node ) 
    {
        /*
        Adds the node to the end of the list.
        */
       assert( list != null && node != null): 
          "Bad (NULL) parameter(s) appendNodeToAlphaList." ;

        if ( list.length == 0 ) {
            node.id = 0;
            list.head = list.tail = node;
        }
        else {
            node.id = list.tail.id + 1;            
            list.tail.next = node;
        }
        
        list.tail = node;
        list.length = list.length + 1;
        //System.out.println(list.length);
        return list;
    }  /* appendNodeToAlphaList */
/**********************************************************************/  
    public AlphaList prependNodeToAlphaList( AlphaList list, AlphaNode node ) 
    {
        /*
        Adds the node to the beginning of the list. 
        */
        assert( list != null && node != null): 
          "Bad (NULL) parameter(s).prependNodeToAlphaList" ;
        if(node == null)
            return list;

        if ( list.length == 0 ) {
            node.next = null;
            node.id = 0;
            list.head = node;
            list.tail = node;
        }
        else
            node.id = list.head.id - 1;

        node.next = list.head;
        list.head = node;
        (list.length)++;
       
        return list;

    }  /* prependNodeToAlphaList */
/**********************************************************************/ 
    public AlphaNode dequeueAlphaNode( AlphaList list ) 
    {
        /*
        Removes the first item in the list and returns it.
        */
        AlphaNode item;

        if ( list.length < 1 )
         return ( null );

        if ( list.length == 1 )
         list.tail = null;

        item = list.head;
        list.head = list.head.next;
        item.next = null;
        (list.length)--;
         setList(list);
        return ( item );
    }  /* dequeueAlphaList */
/**********************************************************************/  
    public AlphaList enqueueAlphaNode( AlphaList list, AlphaNode node ) 
    {
    /*
    Puts an alpha list node at the end of the list.
    */
        return appendNodeToAlphaList( list, node );

    }  /* enqueueAlphaNode */
/**********************************************************************/    
    public AlphaNode duplicateAlphaNode( AlphaNode node ) 
    {
        /*
        Allocates the memory and copies an AlphaList node. Copies pointers
        if it has any, but not objects they point to. The slight exception
        is the obs_source array.  It makes new space for this duplicate
        node's obs_source and then copies the pointers.
        */
        AlphaNode new_node;
        int z;
        assert( node != null): "Cannot duplicate NULL node." ;

        
        new_node = newAlphaNode( duplicateAlpha( node.alpha ),
                                  node.action );

        /* We will copy most everything. */
        new_node.prev_source = node.prev_source;

        new_node.id = node.id;
        new_node.obs = node.obs;

        new_node.first_source = node.first_source;
        new_node.second_source = node.second_source;
        new_node.witness = node.witness;

        /* If there is an obs_choice array, we will copy the pointers, but
         must make a new array to hold them. */
        if ( node.obs_source != null ) 
            new_node.obs_source = node.obs_source;   

        else
           new_node.obs_source = null;


        return ( new_node );
    }  /* duplicateAlphaNode */
/**********************************************************************/
   
    public AlphaList appendDuplicateNodeToAlphaList( AlphaList list, 
                                AlphaNode orig_node ) 
    {
    /*
    Make a copy of the node and appends it to the list.  Returns a
    pointer to the newly created node.
    */
        AlphaNode node;

        node = duplicateAlphaNode( orig_node );
        list = appendNodeToAlphaList( list, node );

        return ( list );
    }  /* appendDuplicateNodeToAlphaList */
/**********************************************************************/
    
    public void addWitnessToAlphaNode( AlphaNode node, double[] witness ) 
    {
    /*
    Adds a witness point to the alpha list.  This has to be more than
    simply setting the pointer, since typically the witness point comes
    from an LP which re-uses the memory for the solution point.  Thus we
    need to allocate the memory for the witness point and then copy it.
    */
        int i;

        assert( node != null): "Cannot add witness to NULL node." ;
        assert( witness != null): "Attempted to add NULL witness to node.";

        if (( node == null ) || ( witness == null ))
            return;

        /* If there is an existing witness point, first free this memory. At
         the moment I cannot think of why this might happen, but it
         certainly doesn't hurt to avoid a potential memory leak. */
        if ( node.witness != null)
            node.witness = null;

        node.witness = witness;
    }  /* addWitnessToAlphaNode */
/**********************************************************************/

/**********************************************************************/
/******************  Alpha List Routines         **********************/
/**********************************************************************/

/**********************************************************************/
   
    public AlphaList initAlphaList( AlphaList list ) 
    {
        /*
        Sets the initial values form the node representing the header of an
        AlphaList. 
        */
        assert( list != null): "List is NULL." ;


        list.head = null;
        list.tail = null;
        list.length = 0;

        return list;

    }  /* inittAlphaList */
/**********************************************************************/
  
    public AlphaList newAlphaList( ) 
    {
        /*
        Allocates the memory for the header node of a new alpha list. 
        */
        AlphaList list = new AlphaList();

       list = initAlphaList( list );

        return ( list );
    }  /* newAlphaList */
/**********************************************************************/
    
    public AlphaList renumberAlphaList( AlphaList list ) 
    {
        /*
        Renumbers the alpha list so vectors are numbered sequentially.
        */
        int list_num = 0;

        assert( list != null): "List is NULL." ;

        AlphaNode Node = list.head;
        while( Node != null) {
            Node.id = list_num++;
            Node = Node.next;
        }  /* while */
        return list;
    }  /* renumberAlphaList */
/**********************************************************************/
    
    public AlphaList prependAlphaList( AlphaList list,
                  double[] alpha,
                  int action ) 
    {
        /*
        Puts an alpha node at the beginning of the list and retruns a
        pointer to the node added.
        */
        AlphaNode temp;

        assert( list != null): "List is NULL." ;


        temp = newAlphaNode( alpha, action );

        list = prependNodeToAlphaList( list, temp );

        return ( list);
    }  /* prependAlphaList */
/**********************************************************************/
    
    public AlphaList appendAlphaList( AlphaList list,
                 double[] alpha,
                 int action ) 
    {
        /*
        Puts an alpha node at the end of the list and retruns a
        pointer to the node added.
        */
        AlphaNode temp;
      assert( list != null): "List is NULL.";

        temp = newAlphaNode( alpha, action );

       list = appendNodeToAlphaList( list, temp );
      
        return ( list );
    }  /* appendAlphaList */
/**********************************************************************/
    
    public void clearAlphaList( AlphaList orig_list ) 
    {
        /*
        Frees the memory for each node in the list and resets the header
        node to reflect an empty list.
        */
        AlphaNode list, temp;
       assert( orig_list != null): "List is NULL." ;


        list = orig_list.head;
        while( list != null ) {
            temp = list; 
            destroyAlphaNode( temp );
            list = list.next;           
        }  /* while */

        initAlphaList( orig_list );

    }  /* clearAlphaList */
/**********************************************************************/
   
    public void destroyAlphaList( AlphaList list ) 
    {
        /*
        Comletely frees up the memory for the entire list, including all
        nodes and the header node.
        */

        assert( list !=null): "List is NULL.";
        clearAlphaList( list );
        list = null;

    }  /* destroyAlphaList */
/**********************************************************************/
    public double bestVectorValuePrimed( AlphaList list, 
                                     double[] belief_state,
                                     double initial_value,
                                     double epsilon )
    {
    /* 
     Takes a list of alpha vectors and a belief state and returns the
     value and vector in the list that gives the maximal value.  If there
     are ties, then we must invoke the tie breaking scheme using the
     lexicographic comparison of the vectors.  The function returns the
     value, and the best_ptr returns the vector, the initial value
     serves as the initial value to use.  If no vectors are better than
     the initial value, then NULL is returned.

     We arbitrarily define that a vector that is equal to the initial
     value is automatically *not* better (since lexicographic check
     cannot be done with no vector.)
    */

        double cur_best_value;
        double cur_value;
        int i;
        
        assert( list != null && belief_state != null):
          "List or belief state is NULL." ;

        /* The worst possible value depends upon whether the state-action
         values of the model are costs or rewards. */
        cur_best_value = initial_value;
        best_ptr = null;

        AlphaNode temp = list.head;
        while( temp != null) {

        /* Get dot product value */
        cur_value = 0.0;
       // if(temp.alpha!= null)
        for ( i = 0; i < gm.getgNumStates(); i++)
          cur_value += belief_state[i] * temp.alpha[i];
        
        /* We must break ties in the values by using the lexicographic
           maximum criteria. Because we have an initial value for whch no
           vector might be beter than, we have to make sure that best_ptr
           is not NULL and handle it properly. Because we cannot know
           whether a vector is lexicographically better than an arbitrary
           value, we define that it is not. */
        if ( Equal( cur_value, cur_best_value, epsilon ) && ( best_ptr != null )
             && isLexicographicallyBetter( temp, best_ptr, epsilon )) {

          /* This is the currently controversial line.  Should this value
             be reset to the current value or should we leave it? 

             cur_best_value = cur_value;
          */
          best_ptr = temp;
        } /* if values and vector is lexicographically better. */

       else if ( isBetterValue( cur_value, cur_best_value, epsilon )) {
          cur_best_value = cur_value;
          best_ptr = temp; 
        }

        temp = temp.next;
        } /* while */

        return ( cur_best_value );   

    }  /* bestVectorValuePrimed */
    /**********************************************************************/
    public AlphaNode BestVector(AlphaList list, 
                                     double[] belief_state,
                                     double epsilon)
    {
        AlphaNode bestV = null;
        int i;
        double cur_value, cur_best_value;
        assert(list != null):"Null list";
        if(list.length == 1){
            list.head.mark = TRUE;
            return list.head;
        }
        double InitialValue = POMDP.worstPossibleValue();
        cur_best_value = InitialValue;
        
        AlphaNode temp = list.head;
        while( temp != null) {

        /* Get dot product value */
        cur_value = 0.0;
       // if(temp.alpha!= null)
        for ( i = 0; i < gm.getgNumStates(); i++)
          cur_value += belief_state[i] * temp.alpha[i];
        
        /* We must break ties in the values by using the lexicographic
           maximum criteria. Because we have an initial value for whch no
           vector might be beter than, we have to make sure that best_ptr
           is not NULL and handle it properly. Because we cannot know
           whether a vector is lexicographically better than an arbitrary
           value, we define that it is not. */
        if ( Equal( cur_value, cur_best_value, epsilon ) && ( bestV != null )
             && isLexicographicallyBetter( temp, bestV, epsilon )) {

          /* This is the currently controversial line.  Should this value
             be reset to the current value or should we leave it? 

             cur_best_value = cur_value;
          */
          bestV = temp;
        } /* if values and vector is lexicographically better. */

       else if ( isBetterValue( cur_value, cur_best_value, epsilon )) {
          cur_best_value = cur_value;
          bestV = temp; 
        }        
        temp = temp.next;
        } /* while */
        if(cur_best_value <= InitialValue)
            return null;
        return bestV;
    }
/**********************************************************************/
    public double bestVectorValue( AlphaList list, 
                 double[] belief_state,
                 double epsilon ) 
    {
        /*
        Just calls bestVectorValuePrimed with the worst possible value to
        ensure some vector will be the best. 
        */
       // pomdp POMDP = new pomdp(MDP,gm,g);
        return ( bestVectorValuePrimed( list, belief_state, 
                                      POMDP.worstPossibleValue(),
                                      epsilon ) );

    }  /* bestVectorValue */
/**********************************************************************/
    
    public AlphaNode bestVectorPrimed( AlphaList list, 
                  double[] belief_state, 
                  double initial_value,
                  double epsilon ) 
    {
        /*
        Takes a list of alpha vectors and a belief state and returns the
        vector in the list that gives the maximal value.  If there are ties,
        then we must invoke the tie breaking scheme using the lexicographic
        comparison of the vectors.
        */

        

        assert( list != null && belief_state != null):
          "List or belief state is NULL." ;


        best_value = bestVectorValuePrimed( list, belief_state, initial_value, epsilon );


        return ( best_ptr );   

    }  /* bestVectorPrimed */

/**********************************************************************/
    
    public AlphaNode bestVector( AlphaList list, 
            double[] belief_state,
            double epsilon ) {

    /*
    Takes a list of alpha vectors and a belief state and returns the
    vector in the list that gives the maximal value.  If there are ties,
    then we must invoke the tie breaking scheme using the lexicographic
    comparison of the vectors.
    */


        assert( list != null && belief_state != null):
          "List or belief state is NULL." ;

        best_value = bestVectorValue( list, belief_state, 
                                      epsilon );

        return ( best_ptr );   

    }  /* bestVector */
/**********************************************************************/
    
    public AlphaNode findAlphaVector( AlphaList list, 
                 double[] alpha,
                 double epsilon ) 
    {
        /*
        This routine returns a pointer to the list node that contains
        the vector 'alpha' of interest if it is found.  It returns NULL
        if the vector is not in the list.
        */

        assert( list != null): "List is NULL." ;


        AlphaNode temp = list.head;

        while( temp != null ) {

            if( sameAlpha( temp.alpha, alpha, epsilon ) ){
              return( temp );
            }
            temp = temp.next;
        }  /* while */

        return( null);
    }  /* findAlphaVector */
/**********************************************************************/
    
    public boolean queryAlphaList( AlphaList list, 
                double[] alpha,
                double epsilon ) 
    {
    /*
    Returns TRUE if the alpha vector parameter is in the list. 
    */

        return ( findAlphaVector( list, alpha, epsilon ) != null );

    }  /* queryAlphaList */
/**********************************************************************/
    
    public int sizeAlphaList( AlphaList list ) 
    {
        /*
        Just get the number of alpha vectors in the list by accessing the
        variable in the header.
        */
     assert( list != null): "List is NULL." ;

        return ( list.length );
    }  /* sizeAlphaList */
/**********************************************************************/
    
    public AlphaList copyAlphaList(AlphaList src_list ) 
    {
        /*
        Doesn't copy the obs_source or witness point fields, leaves them
        blank.
        */
        AlphaNode  temp, node;
        AlphaList dest_list;
        double[] alpha;
        
        assert( src_list != null): "Source list is NULL.";


        /* It is wrong to copy these fields, since they will want to point
         to whole new fragments of memory where the copy resides. */
        dest_list = new AlphaList();
        dest_list.head = null;
        dest_list.tail = null;
        dest_list.length = 0;
        
       

        node = src_list.head;
        while( node != null) {

            //alpha = duplicateAlpha( list.alpha );
           // dest_list = appendAlphaList( dest_list, alpha, list.action );

            temp = newAlphaNode(node.alpha, node.action);
            /* Copy all the fields, though the burden on the being sane values
               lies in the orginal list. */
            temp.id = node.id;
            temp.obs = node.obs;
            temp.prev_source = node.prev_source;
            temp.next = node.next;
            temp.first_source = node.first_source;
            temp.second_source = node.second_source;
            temp.mark = node.mark;
            temp.obs_source = null;
            temp.witness = null;
            dest_list = appendNodeToAlphaList(dest_list, temp);
            node = node.next;
        } /* while */

        return dest_list;
    }  /* copyAlphaList */
/**********************************************************************/
    
    public AlphaList duplicateAlphaList( AlphaList src_list ) 
    {
        /* 
         Allocates a new list and copies the src_list into it.
        */
        AlphaList dest_list;

        assert( src_list != null): "Source list is NULL.";

        dest_list = newAlphaList( );

        dest_list = copyAlphaList( src_list );

        return ( dest_list );

    }  /* duplicateAlphaList */
/**********************************************************************/    
    public int sameAlphaList( AlphaList l1, AlphaList l2, double epsilon ) 
    {
        /* 
         Just checks if the two lists contain the same alpha_vectors in
         exactly the same order.  
        */
        AlphaNode list1, list2;
        assert( l1 != null && l2 != null): "List(s) is NULL." ;


        if ( l1.length != l2.length )
        return ( 0 );

        list1 = l1.head;
        list2 = l2.head;
        
        while( list1 != null ) {
            if ( !sameAlpha( list1.alpha, list2.alpha, epsilon )  )
              return ( 0);

            list1 = list1.next;
            list2 = list2.next;
        } /* while */

        return ( 1 );
    }  /* sameAlphaList*/
/**********************************************************************/
  
    public boolean similarAlphaList( AlphaList list1, 
                  AlphaList list2,
                  double epsilon )
    {
        /*
        Returns true if the two alpha lists contains the same alpha vectors,
        though the order is not important.  
        */
        AlphaNode node1, node2;
        assert( list1 != null && list2 != null): 
          "Bad (NULL) parameter(s).";
        if ( list1.length != list2.length )
            return ( false );

        /* We do not want to make any assumptions about the lists containing
         unique vectors, so we must go through both lists to ensure there
         is a vector in the other list. */

        node1 = list1.head;
        while( node1 != null ) {

            if ( ! queryAlphaList( list2, node1.alpha, epsilon ))
              return ( false );

            node1 = node1.next;
            } /* while */

            node2 = list2.head;
            while( node2 != null ) {

            if ( ! queryAlphaList( list1, node2.alpha, epsilon ))
              return ( false );

            node2 = node2.next;
        } /* while */

        return ( true );
    }  /* similarAlphaList */
/**********************************************************************/
    public void displayAlphaList( AlphaList list ) 
    {
        /*
        Printout a textual version of the list.
        */
        assert( list != null): "List is NULL." ;
        
        System.out.println( "Alpha List: Length= " + list.length );

       AlphaNode temp = list.head;
        while(temp != null ) {

            System.out.print( "<id= "+ temp.id +" ");
            System.out.print(" a="+ temp.action +" ");
            if ( temp.obs >= 0 )
                 System.out.print(" z= "+  temp.obs +" ");

            if ( temp.mark == 1)
              System.out.print( " m  " );

            if ( temp.witness != null )
              System.out.println( " w  " );

            if ( temp.obs_source != null )
              System.out.print( " s  " );
            System.out.print( " > " );

            displayAlpha(temp.alpha );


            temp = temp.next;
        }  /* while */

    }  /* displayAlphaList */
/**********************************************************************/
  
    public void showAlphaList( AlphaList list ) 
    {
    /*
    Printout to stdout, Especially useful in debugger.
    */
        displayAlphaList( list );
    }  /* showAlphaList */
/**********************************************************************/
   
    public AlphaList readAlphaList( String filename, int max_alphas ) throws FileNotFoundException, IOException
    {
    /*
    Reads a list of alpha vectors from a file.  The format of the file
    is very specific and does not allow comments.  It simply reads a
    sequence of numbers which are assumed to be in the correct order.
    This should only be used to read in things written out by the code.
    Also, there is no checking to make sure the file is consistent with
    the problem. i.e., if the probelm has 3 states and you read a file
    of 4 component vectors, this will not notice and might result in
    strange things.  It does a simple check of this by seeing if it is
    in the middle of a vector when the file ends.  However, this does
    not guarantee anything.

    It can also read only a subset of the file of vectors.
    Set max_alphas to <= 0 if you want the whole file read, otherwise
    it will only read the first max_alphas in the file.
    */
        //FILE *file;
       // filename = "tigerAlpha.txt";
        int a = 0, i;
        double[] alpha;
        AlphaList list = new AlphaList();
        initAlphaList(list);
        //Reading alpha file
        FileInputStream fstream = new FileInputStream(filename);        
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        StringTokenizer st;
        ArrayList<Integer> action = new ArrayList<Integer>();
        ArrayList<double[]> allAlpha = new ArrayList<double[]>();
        //Read File Line By Line
        while ((strLine = br.readLine()) != null)   {
            if(strLine.length() == 0)
                continue;
            if(strLine.length() == 1)
               action.add(Integer.parseInt(strLine));
            else{
                
            st = new StringTokenizer(strLine);
            double[] temp = new double[2];
            int index = 0;
            while(st.hasMoreTokens()){
                temp[index++] = Double.parseDouble(st.nextToken(" "));
            }
            allAlpha.add(temp);
        }
        }//End while
        in.close();//Finish reading file
        //System.out.println("list size: "+allAlpha.size() +" "+ action.size());
        for(int j = 0; j < action.size(); j++)
            list = appendAlphaList(list, allAlpha.get(j), action.get(j));
        //System.out.println(list.length);
        return ( list );
    }  /* readAlphaList */
/**********************************************************************/
  
    public void writeAlphaList(String filename, AlphaList list ) 
    {
        int i;

        //Assert( file != NULL, "File handle is NULL." );
        if( list == null){
          System.out.println("Alpha list is NULL at writeAlphaList" );
          return;
        }
        AlphaNode temp = list.head;
        while( temp != null ) {
          try{
            // Create file 
                FileWriter fstream = new FileWriter(new File(filename));
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(temp.action + "   ");
                for ( i = 0; i < gm.getgNumStates(); i++ ){
                    out.write(Double.toString(temp.alpha[i]) +"  " );
                  
                  out.write("  \n");
            }
            temp = temp.next;
            //Close the output stream
            out.close();
            }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
            }
    }  /* while */

    } /* writeAlphaList */
/**********************************************************************/ 
    public AlphaList unionTwoAlphaLists( AlphaList list, AlphaList other_list ) 
    {
        /*
        Takes the union of the two lists sent in and returns the union in
        the 'list' argument.  It is a destructive union, since effectively
        all nodes in the other_list are moved to this list.
        */
        if(list == null && other_list == null)
            return null;
        if (other_list == null || ( other_list.length == 0 ))
            return list;

        if ( list.length < 1 ) {
            list = copyAlphaList(other_list);
        }

        else {
            AlphaNode temp = other_list.head;
            while(temp != null){
                list = appendNodeToAlphaList(list, temp);
                temp = temp.next;
            }
        }
        return list;
    } /* unionTwoAlphaLists */
/**********************************************************************/
  
    public AlphaList clearObsSourceAlphaList( AlphaList list ) 
    {
        /*
        Clears any memory for the 'choice' list for the nodes in the list.
        */
        if ( list == null )
            return null;

        AlphaNode temp = list.head;
        while( temp != null ) {

        if ( temp.obs_source != null)
           temp.obs_source = null;

        temp.obs_source = null;
        temp = temp.next;
        }
        return list;
    }  /* clearObsSourceAlphaList */
/**********************************************************************/
   
    public AlphaList appendUniqueAlphaList( AlphaList list,
                       double[] alpha,
                       int action, 
                       double epsilon )
    {
        /*
        Appends a new node to the alpha list for a vector but only if this
        vector does not already exist in the list.  Returns a pointer to the
        new node created. 
        */
        AlphaList temp;

        if ( queryAlphaList( list, alpha, epsilon )  )
          return ( null );

        temp = appendAlphaList( list, alpha, action );

        return ( temp );

    }  /* appendUniqueAlphaList */
/**********************************************************************/    
    public boolean dominatedAlphaList( double[] alpha, AlphaList list ) 
    {
        /*
        Returns TRUE if any of the alphas in the list dominate
        (component-wise) the first argument alpha vector.  
        */
        assert( alpha != null && list != null):
          "Vector and/or list is NULL.dominatedAlphaList" ;

        AlphaNode temp = list.head;
        while( temp != null ) {

          if ( isDominatedVector( temp.alpha, alpha ))
            return ( true );

          temp = temp.next;
        }  /* while */

        return ( false);
        }  /* dominatedAlphaList */
/**********************************************************************/      
    public AlphaList clearMarkAlphaList( AlphaList list ) 
    {
    /*
    Sets all the nodes in the list to have their 'mark' field FALSE.
    */
     //  assert( list != null): "List is NULL. ";
    if ( list == null )
        return null;

    AlphaNode temp = list.head;
    while( temp != null ) {

        temp.mark = 0;

        temp = temp.next;
    }  /* while */
    return list;

}  /* clearMarkAlphaList */
/**********************************************************************/
   public int sizeUnmarkedAlphaList( AlphaList list ) 
    {
        /*
        Returns the number of nodes whose 'mark' field is FALSE.
        */
        int count = 0;

        assert( list != null): "List is NULL. sizeUnmarkedAlphaList";
             
        AlphaNode temp = list.head;
        while ( temp != null) {

            if ( temp.mark == FALSE )
              count++;

            temp = temp.next;
        } /* while */

        return ( count );
    }  /* sizeUnmarkedAlphaList */
/**********************************************************************/
 
    public boolean allMarkedAlphaList( AlphaList list ) 
    {
        /*
        Retruns true if all the nodes in the list have their marked fields
        set. 
        */
        if ( list == null )
        return ( true);

        AlphaNode temp = list.head;
        while( temp != null ) {

            if ( temp.mark == FALSE )
              return ( false);

            temp = temp.next;
    }  /* while */

    return ( true );

    }  /* clearMarkAlphaList */
/**********************************************************************/
    
    public AlphaNode findUnmarkedVector(  AlphaList list ) 
    {
        /* 
         Returns a pointer to the first vector in the list where the 'mark'
         field is FALSE. If none exist, or the list is empty, it returns
         NULL.  
        */
        assert( list != null): "List is NULL. findUnmarkedVector." ;
        
        AlphaNode temp = list.head;
        while ( temp != null ) {

            if ( temp.mark == FALSE )
              return ( temp );

        temp = temp.next;
        }  /* while list != NULL */

        return (null );

    }  /* findUnmarkedVector */
/**********************************************************************/
    
    public AlphaNode extractUnmarkedVector( AlphaList list ) 
    {
    /*
    Finds a vector node with the 'mark' field set to FALSE and extracts
    that node from the list returning a pointer to the node.
    */

    /* This will return NULL if no node is found. */
        return ( extractAlphaNode( list, findUnmarkedVector( list ) ));

    }  /* extractUnmarkedVector */
/**********************************************************************/
   
    public AlphaList markDominatedAlphaList( double[] alpha, AlphaList list ) 
    {
        /*
        Checks and marks those vectors in 'list' which are dominated by the
        vector 'alpha' sent it.  Does not delete them yet, just sets their
        'mark' field to TRUE.  Assumes that the 'mark' field has already
        been cleared and it will not set any 'mark' fields to FALSE.  Any
        existing TRUE 'mark' fields will remain that way regardless of
        whether they are dominated or not.
        */
        int num_marked = 0;

         assert( list != null): "List is NULL.markDominatedAlphaList." ;
         

        AlphaNode temp = list.head;
        while( temp != null ) {

        /* We might as well only check those nodes whose 'mark' field is
           not yet set, since the only result, even if it is dominated,
           would be to set the 'mark' field anyway. */
            if ( temp.mark != TRUE )
              if ( isDominatedVector( alpha, temp.alpha )) {
                temp.mark = TRUE;
                num_marked++;
              }

            temp = temp.next;
        }  /* while */

        return (list);
    }  /* markDominatedAlphaList */
    /**********************************************************************/
    
    public AlphaList extractMarkedAlphaList( AlphaList list ) 
    {
        /*
        Removes all the nodes which have their 'mark' fields set to TRUE
        and puts them in a separate list which is returned.
        */
        AlphaList extracted_list;
        AlphaNode walk, temp, trail;

        assert( list != null): "List is NULL.extractMarkedAlphaList." ;
         

        /* This is where we will put all the extracted nodes. */
        extracted_list = newAlphaList();
       
        trail = walk = list.head;
        while( walk != null ) {

            /***** Case 1: Item is not marked. */
            if ( walk.mark == FALSE ) {
              trail = walk;
              walk = walk.next;
              continue;
            }  /* if not marked */

            temp = walk;

            /***** Case 2: Only one item in list. */
            if ( list.length == 1 ) {
              list.head = list.tail = null;
              walk = null;
            }  /* if only 1 in list */

            /***** Case 3: Item is last element of list. */
            else if ( list.tail == walk ) {
              list.tail = trail;
              trail.next = null;
              walk = null;
            }  /* if last element in list */

            /***** Case 4: Item is first element in list. */
            else if ( list.head == walk ) {
              list.head = walk.next;
              trail = walk = list.head;
            }  /* if first element of list 

            /***** Case 5: Item not first or last in list. */
            else {  /* not first in list */
             trail.next = walk.next;
              walk = walk.next;    }  /* else not first in list */

            /* We'll clear its mark'ed flag and add it to the list. */
            temp.mark = FALSE;
            temp.next = null;
            extracted_list = appendNodeToAlphaList( extracted_list, temp );

            (list.length)--;

        }  /* while */

        return ( extracted_list );
    }  /* extractMarkedAlphaList */
/**********************************************************************/
    
    public int removeMarkedAlphaList( AlphaList list ) 
    {
        /*
        Removes all the nodes which have their 'mark' fields set to TRUE.
        Returns the number of nodes removed.
        */
        AlphaList removed_list;
        int num_removed = 0;
        assert( list != null): "List is NULL." ;
        /* First extract the marked nodes. */
        removed_list = extractMarkedAlphaList( list );

        /* See how many there are and save it. */
        num_removed = removed_list.length;

        /* Free up the memory for removed nodes. */
        destroyAlphaList( removed_list );

        return ( num_removed );
    }  /* removeMarkedAlphaList */
/**********************************************************************/
    
    public int removeDominatedAlphaList( double[] alpha, AlphaList list ) 
    {
        /*
        Removes all vectors in the list that are component-wise dominated 
        by the first argument alpha vector.
        */

        /* CLear the 'mark' flags. */
        clearMarkAlphaList( list );

        /* First mark all the dominated vectors. */
        markDominatedAlphaList( alpha, list );

        /* Then removed all those that were marked. */
        return ( removeMarkedAlphaList( list ));

    }  /* removeDominatedAlphaList */
/**********************************************************************/
    
    public AlphaNode extractAlphaNode( AlphaList list, AlphaNode extract_ptr ) 
    {
        /*
        Take a pointer to one of the elements in the list and removes that
        node from the list.  It returns a pointer to the removed node 
        (memory is not freed) or NULL if something goes wrong.
        */
        AlphaNode walk_ptr, trail_ptr;

        assert( list != null): "List is NULL." ;
       
        if (( extract_ptr == null )
           || ( list.length == 0 ))
         return ( null );

        /* see if it is the only one in list */
        if ( list.length == 1 ) {
          if ( list.head == extract_ptr ) {
             list.head = list.tail = null;
             list.length = 0;
             setList(list);
             return ( extract_ptr );
          }

          else /* there's only one element, but it isn't the one we want */
             return ( null );
        }  /* if only one element in list */

        /* see if first element of list */
        if ( extract_ptr == list.head ) {
          list.head = extract_ptr.next;
          (list.length)--;
          extract_ptr.next = null;
          setList(list);
          return ( extract_ptr );
        }

        /* At this point we know that there are 2 or more elements
          in the list and the one we want is not the first one */

        trail_ptr = list.head;
        walk_ptr = list.head.next;

        while( walk_ptr != null ) {
          if ( walk_ptr == extract_ptr ) {
             if ( extract_ptr == list.tail )
                /* See if it is the last one in list */
                list.tail = trail_ptr;

             trail_ptr.next = extract_ptr.next;
             (list.length)--;
             extract_ptr.next = null;
             setList(list);
             return ( extract_ptr );
          }  /* if found */

          trail_ptr = walk_ptr;
          walk_ptr = walk_ptr.next;
        } /* while */
       
        return ( null );
    }  /* extractAlphaNode */
/**********************************************************************/
    public void setList(AlphaList list){
        newlist1 = list;
    }
 /**********************************************************************/   
    public AlphaList getList(){
        return newlist1;
    }
/**********************************************************************/    
    public AlphaNode removebestVectorNode( AlphaList list, 
                      double[] b, 
                      double epsilon ) 
    {
        /*
        Finds the vector with the highest dot-product value with 'b' and
        removes that node from the list.  It doesn't deallocate any 
        memory and returns a pointer to the removed node, or NULL if
        something goes wrong.
        */
        

        best_ptr = BestVector( list, b,epsilon  );
        extractAlphaNode( list, best_ptr );
        return best_ptr;
        //return ( extractAlphaNode( list, best_ptr ));

    }  /* removebestVectorNode */
/**********************************************************************/
    
    public AlphaNode extractFromAlphaList( AlphaList list, 
                      double[] alpha, 
                      double epsilon ) 
    {
        /*
        If the vector sent in already exists in the list, then it is removed
        from the list and the pointer to the node is returned. 
        */
        return ( extractAlphaNode( list, findAlphaVector( list, alpha, 
                                                  epsilon )));

    }  /* extractFromAlphaList */
/**********************************************************************/
   
    public boolean removeFromAlphaList( AlphaList list, 
                     double[] alpha,
                     double epsilon ) 
    {
        /*
        If the alpha vector sent in is already in the list, then the node in
        the list is removed, and deallocated.  This routine returns TRUE if
        a node was removed and FALSE if the vector is not in the list. 
        */
        AlphaNode node;

        node = extractFromAlphaList( list, alpha, epsilon );

        if ( node == null )
            return ( false );

        destroyAlphaNode( node );

        return ( true );
    }  /* removeFromAlphaList */
/**********************************************************************/
/**********************************************************************/
/*************** Sorting Alpha Lists              *********************/
/**********************************************************************/

/**********************************************************************/
    //Didn't use here in Java version
   /* public static void swapPointersAlphaList( AlphaNode x, AlphaNode y ) 
    {
        AlphaNode temp;

        temp = x;
        x = y;
        y = temp;

    }  /* swapPointersAlphaList */
/**********************************************************************/
    public void quicksortAlphaList( AlphaNode[] array, int left, int right ) 
    {
        /* 
         Implementation of Quicksort algorithm for an array of AlphaList
         pointers.  Adapted from Kernighan and Ritchie, p.120 (second
         edition) 
        */
        int i, last;

        if( left >= right )
            return;
       
        //swapPointersAlphaList( (array[left]), (array[(left + right)/2]) );
        AlphaNode temp1;
        temp1 = array[left];
        array[left] = array[(left + right)/2];
        array[(left + right)/2] = temp1;
        
        last = left;

        /* Just sorting the list means we are not to picky about the epsilon
         value we use.  */
        for( i = left+1; i <= right; i++ ){
            if( isLexicographicallyBetterAlpha( array[left].alpha,
                                                array[i].alpha,
                                                1e-15 )){
             // swapPointersAlphaList( (array[(++last)]), (array[i]) );
                last++;
              AlphaNode temp2 =array[(last)];
              array[(last)] = array[i];
              array[i] = temp2;
            }
            
        }
               
       // swapPointersAlphaList( (array[left]), (array[last]) );
        AlphaNode temp3 = array[left];
        array[left] = array[last];
        array[last] = temp3;
        quicksortAlphaList( array, left, last-1 );
        quicksortAlphaList( array, last+1, right );

    }  /* quicksortAlphaList */
/**********************************************************************/
    
    public AlphaList sortAlphaList( AlphaList list ) 
    {
        /*
        Sorts the list lexicographically.
        */
        
        int i, num_vectors;

        /* We set up the list in an array, sort it, then put it back into a
         list. */
        assert(list != null):"Null list";
        array = new AlphaNode[ list.length];

        num_vectors = list.length;

        for ( i = 0; i < num_vectors; i++ )
            array[i] = dequeueAlphaNode( list );
        printArray();
        assert( list.length == 0):
          "List length not what it should be." ;

        quicksortAlphaList( array, 0, num_vectors-1 );

        for ( i = 0; i < num_vectors; i++ )
          list =  enqueueAlphaNode( list, array[i] );

        printArray();
        array = null;
        return list;
    }  /* sortAlphaList */
/**********************************************************************/
   public void printArray(){
       System.out.println("print out the Arrays");
       for(int i = 0; i < array.length; i++)
           System.out.println(array[i].alpha[0] +" "+array[i].alpha[1]);
       
   }
/**********************************************************************/

/**********************************************************************/
/*************** Arrays of Alpha List Routines    *********************/
/**********************************************************************/

/**********************************************************************/
    
    public int maxSizeAlphaLists( AlphaList[] list, int num_lists ) 
    {
        /*
        Takes an array of alpha lists and returns the maximum size over all
        lists. 
        */
        int max_size = 0;
        int i;

        if (( list == null )
          || ( num_lists < 0 ))
        return ( 0 );

        for( i = 0; i < num_lists; i++ )
            if ( sizeAlphaList( list[i] ) > max_size )
              max_size = sizeAlphaList( list[i] );

        return ( max_size );

    }  /* maxSizeAlphaLists */
/**********************************************************************/
    public double getbest_value(){
        return best_value;
    }
/**********************************************************************/
    public boolean Equal(double d, double d0, double epsilon) {
        if(Math.abs(d - d0) <= epsilon)
            return true;
        return false;
    }

/**********************************************************************/  
    public boolean GreaterThan(double d, double d0, double epsilon) {
        if((d - d0) > epsilon)
            return true;
        return false;
    }
/**********************************************************************/
   public AlphaNode getNode(AlphaList list, int id){
         AlphaNode a;
         assert(list != null):"null list";
         a= list.head;
         while(a.id != id){
             a = a.next;
         }
         return a;
     }
/**********************************************************************/
   public boolean isBetterValue( double new_value, double current, double epsilon ) {
/* 
   Often we would like to do some max or min procedure and require
   coparing a new value to a current value.  Since the test for which
   is better depends on whether rewards or costs are being used, we
   have encapsulated this in this routine.  We also want to account
   for the precision of the current run (i.e.,
   gDoubleEqualityPrecision.) 
*/

  if( gm.getgValueType() == Value_Type.REWARD_value_type )
    return( LessThan( current, new_value, epsilon ));
  else
    return( LessThan( new_value, current, epsilon ));

}  /* isBetterValue */
/**********************************************************************/

 public boolean LessThan(double a1, double a2, double epsilon){
     if((a1 - a2) <= epsilon)
         return true;
     else
         return false;
 }
/**********************************************************************/ 
}
