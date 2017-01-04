package pomdp;


public interface projectionIN {
    /* Just creates and returns the storage to hold all the projection
    sets.  They will initially all be NULL.  */
    public AlphaList[][] allocateAllProjections(  );

    /* Discards all the projection lists and memory associated with them.  */
    public void freeAllProjections( AlphaList[][] projection );

    /* Makes all the projected alpha vector lists, which amounts to a
    projected list for each action-observation pair. Stores this as a
    two dimensional array of lists where the first index is the action
    and the other is the observation.  This allocates the space for the
    projections first.  The 'impossible_obs_epsilon' specifies the
    tolerance to use when trying to determine whether or not a
    particulat observation is at all feabile.  */
    public AlphaList[][] makeAllProjections( AlphaList prev_alpha_list );

    /* Displays all projections to file stream.  */
    public void displayProjections( AlphaList[][] projection );

    /* Displays all projections to stdout.  */
    public void showProjections( AlphaList[][] projection );

}
