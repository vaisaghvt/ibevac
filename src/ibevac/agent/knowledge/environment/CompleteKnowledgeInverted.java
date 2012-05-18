package ibevac.agent.knowledge.environment;

import com.google.common.collect.HashMultimap;
import ibevac.datatypes.RoomEdge;
import ibevac.agent.knowledge.waypoints.IbevacLogicalWaypoint;
import ibevac.datatypes.CArea;
import ibevac.datatypes.CEvacuationScenario;
import ibevac.datatypes.CExit;
import ibevac.datatypes.CFloor;
import ibevac.datatypes.CLink;
import ibevac.datatypes.CRoom;
import ibevac.datatypes.CStaircase;

import ibevac.datatypes.CStaircaseGroup;
import ibevac.environment.IbevacSpace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.SimpleWeightedGraph;

/**
 * <h4>This implementation of the Environment Knowledge Module initializes the agent
 * with knowledge of the complete map stored in scenario. There are two points
 * of difference from CompleteKnowledge : Firstly, the edges are areas and the
 * vertices are links. Secondly, the distance to be  traversed in moving over an
 * edge is stored. This implies that path's found will be slightly smarter. </h4>
 *
 * @author <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 * @version $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class CompleteKnowledgeInverted implements EnvironmentKnowledgeModule {

    //TODO  Very buggy Either rethink the whole class or do soemthing at least.


    /**
     * Simple weighted graph storing the agent's internal representation of the
     * known environment. Edges correspond to rooms and have an integer weight
     * associated with it indicating the distance required to move along the edge.
     */
    private final SimpleWeightedGraph<Integer, RoomEdge> graph = new SimpleWeightedGraph<Integer, RoomEdge>(
            RoomEdge.class);
    /**
     * A mapping associating area Ids with actual areas/rooms
     */
    private final HashMap<Integer, CArea> roomMapping = new HashMap<Integer, CArea>();
    /**
     * A mapping associating a link with integerID. A link connects to areas or
     * rooms
     */
    private final HashMap<Integer, CLink> linkMapping = new HashMap<Integer, CLink>();
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
     * The incoming and outgoing links (doorways) for each room or area.
     */
    private final HashMultimap<Integer, CLink> linksFromRoom = HashMultimap.create();

    //TODO Think about this in particular

    /**
     * Initializes internal graph representation and all the mappings.
     *
     * @param scenario
     * @param space    Required to find links for each room.
     */
    public CompleteKnowledgeInverted(CEvacuationScenario scenario, IbevacSpace space) {
        for (CFloor floor : scenario.getFloors()) {

//            //create environment vertex

            //create links as vertices
            for (CLink link : floor.getLinks()) {
//                int id0 = link.getConnectingAreas().get(0);
//                int id1 = link.getConnectingAreas().get(1);

//                CEdge edge = new CEdge(link, roomMapping);

//                graph.addEdge(id0, id1, edge);
//				allEdges.add(edge);

                graph.addVertex(link.getId());
                linkMapping.put(link.getId(), link);
            }


            //create edges for exits
            for (CExit exit : floor.getExits()) {
//                int id0 = exit.getConnectingAreas().get(0);
//                CEdge edge = new CEdge(exit, roomMapping);
                graph.addVertex(exit.getId());
//                graph.addEdge(exit.getId(), -1, new RoomEdge(exit));
//                graph.addEdge(id0, -1, edge);
//				allEdges.add(edge);

                linkMapping.put(exit.getId(), exit);
                linksFromRoom.put(-1, exit);
//                linksFromRoom.put(exit.getConnectingAreas().get(0), exit);
//                graph.addEdge(link1.getId(), link2.getId(), new RoomEdge(room, link1, link2));
            }


            //create vertices from rooms
            for (CRoom room : floor.getRooms()) {

//                graph.addVertex(room.getId());
                roomMapping.put(room.getId(), room);
                if ((room.getId() >= 1 && room.getId() <= 13)
                        || (room.getId() >= 177 && room.getId() <= 181)
                        || (room.getId() >= 185 && room.getId() <= 189)
                        || room.getId() == 334) {
                    corridorList.add(room.getId());
                }

                Set<CLink> links = new HashSet<CLink>(space.getLinksForRoom(room.getId()));
//                System.out.println("CONsidering room :"+ room.getId());
                this.linksFromRoom.putAll(room.getId(), links);
                for (CLink link1 : links) {
                    for (CLink link2 : links) {

                        if (link1.getId() != link2.getId()) {

                            graph.addEdge(link1.getId(), link2.getId(), new RoomEdge(room, link1, link2));
//                            System.out.println(graph.edgeSet());
                        }
                    }
                }
            }

//            //create vertices from staircases
            for (CStaircase staircase : floor.getStaircases()) {


                Set<CLink> links = new HashSet<CLink>(space.getLinksForRoom(staircase.getId()));
//                assert links.size()==1;
//                for (CLink link1 : links) {
//                 
//                        graph.addEdge(link1.getId(), new RoomEdge(staircase));
//                 
//                }
//                graph.addVertex(staircase.getId());
                roomMapping.put(staircase.getId(), staircase);
                this.linksFromRoom.putAll(staircase.getId(), links);
            }
        }

        //handle staircase groups (i.e., connect staircases from different floors)
        for (CStaircaseGroup group : scenario.getStaircaseGroups()) {
            int n = group.getStaircaseIds().size();
            for (int i = 0; i < n; ++i) {
                int id0 = group.getStaircaseIds().get(i);
                ArrayList<CLink> links = new ArrayList<CLink>(space.getLinksForRoom(id0));
                assert links.size() == 1;
                CLink incomingLink = links.get(0);
                for (int j = i + 1; j < n; ++j) {
                    int id1 = group.getStaircaseIds().get(j);
                    links = new ArrayList<CLink>(space.getLinksForRoom(id1));
                    assert links.size() == 1;
                    CLink outgoingLink = links.get(0);
                    RoomEdge edge = new RoomEdge(roomMapping.get(id0), roomMapping.get(id1), incomingLink, outgoingLink);
                    graph.addEdge(incomingLink.getId(), outgoingLink.getId(), edge);

//					allEdges.add(edge);
                }
            }
        }
//        System.out.println(graph.edgeSet().size());
    }

    @Override
    public SimpleWeightedGraph<Integer, RoomEdge> getGraph() {
        return graph;
    }

    @Override
    public final CArea resolveAreaById(int areaId) {
        return roomMapping.get(areaId);
    }

    @Override
    public CLink resolveLinkById(int areaId) {
//        return linkMapping.get(areaId);
        return linkMapping.get(areaId);
    }

    @Override
    public boolean markWaypointAsInaccessible(IbevacLogicalWaypoint lwaypoint) {
        if (!inaccessibleWaypoints.contains(lwaypoint)) {
            inaccessibleWaypoints.add(lwaypoint);

//            CLink link = lwaypoint.link();
//            if (link != null) {
//                CEdge edge = linkMapping.get(link.getId());
//                assert (edge != null);
//
//                graph.setEdgeWeight(edge, Double.MAX_VALUE);
//            }
            return true;
        }
        return false;
    }

    @Override
    public Set<IbevacLogicalWaypoint> getInaccessibleWaypoints() {
        return inaccessibleWaypoints;
    }

    @Override
    public Set<Integer> getCorridors() {
        return this.corridorList;
    }

    @Override
    public Integer getExitId() {
        return -1;
    }

    /**
     * A list of connecting links for each room.
     *
     * @param areaId
     * @return Iterable over the links which can be directly iterated over.
     */
    public Iterable<CLink> getConnectingLinks(int areaId) {

        return linksFromRoom.get(areaId);
    }
}
