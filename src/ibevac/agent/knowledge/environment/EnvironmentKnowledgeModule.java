package ibevac.agent.knowledge.environment;

import ibevac.agent.knowledge.waypoints.IbevacLogicalWaypoint;
import ibevac.datatypes.CArea;

import ibevac.datatypes.CLink;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

/**
 * <h4>The Environment Knowledge Module. Stores the agent's knowledge of the 
 * environment.</h4>
 * 
 * <h4>This interface enables the use of a strategy pattern so that the specific 
 * implementation used is irrelevant to the rest of the program.</h4>
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public interface EnvironmentKnowledgeModule {

    /**The simple weighted graph representation of the agent's knowledge of the 
     * environment
     * 
     * @param <E> A subclass of weighted edge
     * @return A simple weighted graph
     * 
     * @see org.jgrapht.graph.SimpleWeightedGraph
     */
    public <E extends DefaultWeightedEdge> SimpleWeightedGraph<Integer, E> getGraph();

    /**
     * Finds the area associated with a particular ID
     * @param areaID the integer id of the area
     * @return the actual CArea
     * 
     * @see ibevac.datatypes.CArea
     */
    public CArea resolveAreaById(int areaId);

    /**
     * Finds the link associated with a particular ID
     * @param areaId the integer id of the link
     * @return the actual CLink
     * 
     * @see ibevac.datatypes.CLink
     */
    public CLink resolveLinkById(int areaId);

    /**
     * Marks the passed logical way point as innaccessible
     * @param logicalWayPoint
     * @return true : was accessible; set to innaccessible
     *          false : if already inaccessible
     */
    public boolean markWaypointAsInaccessible(IbevacLogicalWaypoint logicalWayPoint);

    /**
     * Returns a set of the logical waypoints that the agent knows are 
     * inaccessible
     * 
     * @return Set of IbevacLogicalWaypoints that are innaccessible
     */
    public Set<IbevacLogicalWaypoint> getInaccessibleWaypoints();

    /**
     * Returns the set of corridors that the agent knows about. These are 
     * generally the gathering/ milling points in the map
     * 
     * @return Set of integer areaIDs of the corridors
     */
    public Set<Integer> getCorridors();

    /**
     * Integer value of the exit
     * 
     * @return integer value of the exit.
     */
    public Integer getExitId();
}
