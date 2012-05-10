package ibevac.agent.perception;

import ibevac.agent.IbevacAgent;

import ibevac.cue.Cue;
import java.util.Set;


import abmcs.agent.PhysicalAgent;
import abmcs.agent.StaticObstacle;

/**
 * <h4>The Perception Module. Enables the agents perception of the environment.
 * </h4>
 * 
 * <h4>This interface enables the use of a strategy pattern so that the specific 
 * implementation used is irrelevant to the rest of the program.</h4>
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public interface Perception {

    /**
     * Perceived dynamic obstacles
     * @param <E> Any physical agent
     * @return Set of dynamic obstacles
     */
    public <E extends PhysicalAgent> Set<E> getPerceivedDynamicObstacles();

    /**
     * 
     * @return Set of static obstacles 
     */
    public Set<StaticObstacle> getPerceivedStaticObstacles();

    /**
     * This updates all the perceived objects at each time step.
     */
    public void update();

    public int getCurrentFloorIdx();

    public int getCurrentAreaId();

    /* TODO: This might be better in navigation or some other module rather than
    in Perception */
    /**
     * Handles the teleporting of the agent from one level to the next
     * 
     * @param fnext
     *            : new floor
     * @param areaId1
     *            : new area
     */
    public void handleTeleport(int newFloor, int newArea);

    /**
     * 
     * @return true if exit is reached
     *          false if exit is not reached
     */
    public boolean exitReached();

    /**
     * Checks if the current waypoint is inaccessible.
     * 
     * @return true if waaypoint is accessible else false
     */
    public boolean isCurrentWaypointAccessible();

    /**
     * This returns the set of all agents that are within a social radius i.e.
     * those that the agent can exchange messages with
     * 
     * @return set of agents within the defined social space
     */
    public Set<IbevacAgent> getSocialSpace();

    /**
     * This is used to reduce the radius of the agent in case there are a lot of
     * agents in the personal space
     * 
     * @return set of agents within the personal space
     */
    public Set<IbevacAgent> getPersonalSpace();

    /**
     * These are the agents within shouting radius of the agents
     * 
     * @return set of agents within the shouting space
     */
    public Set<IbevacAgent> getShoutingSpace();

    /**
     * 
     * @return set of all cues that are perceived 
     */
    public Set<Cue> getCues();

    public boolean hasCues();
}
