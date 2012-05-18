package ibevac.agent.knowledge.environment;

import ibevac.agent.knowledge.waypoints.IbevacLogicalWaypoint;
import ibevac.datatypes.CArea;
import ibevac.datatypes.CEdge;
import ibevac.datatypes.CEvacuationScenario;
import ibevac.datatypes.CExit;
import ibevac.datatypes.CFloor;
import ibevac.datatypes.CLink;
import ibevac.datatypes.CRoom;
import ibevac.datatypes.CStaircase;
import ibevac.datatypes.CStaircaseGroup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.SimpleWeightedGraph;

/**
 * <h4>This implementation of the Environment Knowledge Module initializes the agent
 * with knowledge of the complete map stored in scenario. The graph is stored
 * with the edges being links between the rooms and the vertices being rooms or
 * areas. Distances between rooms are not stored in the graph and as such aren't
 * used in path planning.</h4>
 *
 * @author <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 * @version $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class CompleteKnowledge implements EnvironmentKnowledgeModule {

    /**
     * The graph based internal representation of the environment. In this simple
     * implementation all edges have equal weight. And inaccesible edges have
     * infinite weight. The edges are links between rooms and the nodes are rooms
     * or areas
     *
     * @see CEdge
     */
    private final SimpleWeightedGraph<Integer, CEdge> graph = new SimpleWeightedGraph<Integer, CEdge>(
            CEdge.class);
    /**
     * A mapping associating area Ids with actual areas/rooms
     */
    private final HashMap<Integer, CArea> roomMapping = new HashMap<Integer, CArea>();
    /**
     * A mapping associating a getLink with integerID. A getLink connects to areas or
     * rooms
     */
    private final HashMap<Integer, CEdge> linkMapping = new HashMap<Integer, CEdge>();
    /**
     * The set of inaccessible logical waypoints.
     */
    private final HashSet<IbevacLogicalWaypoint> inaccessibleWaypoints = new HashSet<IbevacLogicalWaypoint>();
    /**
     * The area Ids of those rooms which are meeting points. All corridors are
     * set to be meeting points.
     */
    private final HashSet<Integer> corridorList = new HashSet<Integer>();

    /**
     * Initializes the graph and mappings from the scenario file. Corridor information
     * is also stored (explicitly).
     *
     * @param scenario xml file storing all environment information
     */
    public CompleteKnowledge(CEvacuationScenario scenario) {
        for (CFloor floor : scenario.getFloors()) {


            //create environment vertex
            graph.addVertex(-1);
            //create vertices from rooms
            for (CRoom room : floor.getRooms()) {
                graph.addVertex(room.getId());

                roomMapping.put(room.getId(), room);
                if ((room.getId() >= 1 && room.getId() <= 13)
                        || (room.getId() >= 177 && room.getId() <= 181)
                        || (room.getId() >= 185 && room.getId() <= 189)
                        || room.getId() == 334) {
                    corridorList.add(room.getId());
                }
            }

            //create vertices from staircases
            for (CStaircase staircase : floor.getStaircases()) {
                graph.addVertex(staircase.getId());
                roomMapping.put(staircase.getId(), staircase);
            }

            //create edges for links
            for (CLink link : floor.getLinks()) {
                int id0 = link.getConnectingAreas().get(0);
                int id1 = link.getConnectingAreas().get(1);

                CEdge edge = new CEdge(link, roomMapping);
                graph.addEdge(id0, id1, edge);
//				allEdges.add(edge);

                linkMapping.put(link.getId(), edge);
            }

            //create edges for exits
            for (CExit exit : floor.getExits()) {
                int id0 = exit.getConnectingAreas().get(0);
                CEdge edge = new CEdge(exit, roomMapping);
                graph.addEdge(id0, -1, edge);
//				allEdges.add(edge);

                linkMapping.put(exit.getId(), edge);
            }
        }

        //handle staircase groups (i.e., connect staircases from different floors)
        for (CStaircaseGroup group : scenario.getStaircaseGroups()) {
            int n = group.getStaircaseIds().size();
            for (int i = 0; i < n; ++i) {
                int id0 = group.getStaircaseIds().get(i);
                for (int j = i + 1; j < n; ++j) {
                    int id1 = group.getStaircaseIds().get(j);
                    CEdge edge = new CEdge(id0, id1, roomMapping);
                    graph.addEdge(id0, id1, edge);
//					allEdges.add(edge);
                }
            }
        }
    }

    /**
     * @return the cognitive map graph
     */
    @Override
    public SimpleWeightedGraph<Integer, CEdge> getGraph() {
        return graph;
    }

    /**
     * @param areaId
     * @return associated edge
     */
    public CEdge resolveEdgeById(int areaId) {
        return linkMapping.get(areaId);
    }

    /**
     * @param areaId
     * @return the associated area
     */
    @Override
    public CArea resolveAreaById(int areaId) {
        return roomMapping.get(areaId);
    }

    /**
     * @param areaId
     * @return associated getLink
     */
    @Override
    public CLink resolveLinkById(int areaId) {
        return linkMapping.get(areaId).link();
    }

    /**
     * Marks the waypoint as inaccesible in own graph
     *
     * @param lwaypoint
     * @return true    successfullly marked as inaccessible
     *         false   already inaccessible
     */
    @Override
    public boolean markWaypointAsInaccessible(IbevacLogicalWaypoint lwaypoint) {
        if (!inaccessibleWaypoints.contains(lwaypoint)) {
            inaccessibleWaypoints.add(lwaypoint);

            CLink link = lwaypoint.getLink();
            if (link != null) {
                CEdge edge = linkMapping.get(link.getId());
                assert (edge != null);
                graph.setEdgeWeight(edge, Double.MAX_VALUE);
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the set of the logical waypoints that the agent knows are
     * inaccessible
     *
     * @return Set of IbevacLogicalWaypoints that are innaccessible
     */
    @Override
    public Set<IbevacLogicalWaypoint> getInaccessibleWaypoints() {
        return inaccessibleWaypoints;
    }

    /**
     * Returns the set of corridors that the agent knows about. These are
     * generally the gathering/ milling points in the map
     *
     * @return Set of integer areaIDs of the corridors
     */
    @Override
    public Set<Integer> getCorridors() {
        return this.corridorList;
    }

    /**
     * Integer value of the exit
     *
     * @return integer value of the exit.
     */
    @Override
    public Integer getExitId() {
        return -1;
    }
}
