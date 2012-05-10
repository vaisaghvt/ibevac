package ibevac.agent.navigation;

import abmcs.motionplanning.level2.SpatialWaypoint;
import ibevac.agent.knowledge.waypoints.IbevacLogicalWaypoint;
import ibevac.agent.IbevacAgent;
import ibevac.agent.knowledge.environment.EnvironmentKnowledgeModule;
import ibevac.datatypes.CArea;

import ibevac.datatypes.CExit;
import ibevac.datatypes.CLink;
import ibevac.datatypes.CStaircase;
import ibevac.environment.IbevacSpace;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.KShortestPaths;

import abmcs.motionplanning.level3.Level3MotionPlanning;
import abmcs.motionplanning.level3.LogicalWaypoint;
import ibevac.agent.knowledge.KnowledgeBase;
import ibevac.agent.knowledge.environment.CompleteKnowledgeInverted;
import ibevac.datatypes.RoomEdge;
import java.util.Collection;
import java.util.TreeMap;

/**
 * <h4>A level 3 motion planner to work with the inverted Compelte Knowledge
 * Module. This would consider distance between logical waypoints as well.</h4>
 * 
 *
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class IbevacL3MP implements Level3MotionPlanning {

    
    // TODO comment when fixed. 
    private IbevacSpace space = null;
    private IbevacAgent agent = null;

    public IbevacL3MP(IbevacSpace space, IbevacAgent agent) {
        super();
        this.space = space;
        this.agent = agent;
    }

 
    @Override
    public List<LogicalWaypoint> getLogicalWaypoints() {
        if (agent.getCurrentGoalAreaIds().isEmpty()) {
            return null;
        }
        //if the agent is not escaping, then there is no escape route...
        if (agent.getCurrentGoalAreaIds().contains(
                agent.getCurrentAreaId())) {
            List singleton = new ArrayList(1);
            singleton.add(new IbevacLogicalWaypoint(null, agent.getEnvironmentKnowledge().resolveAreaById(agent.getCurrentAreaId()), null, space, agent));
            return singleton;
        } else if (agent.getEnvironmentKnowledge().resolveAreaById(agent.getCurrentAreaId()) == null) { // it is a getLink
            CLink link = agent.getEnvironmentKnowledge().resolveLinkById(agent.getCurrentAreaId());

//            assert (edge != null);
//
//            //...and its associated getLink
//            CLink getLink = edge.getLink();
            assert (link != null);

            if (link.getConnectingAreas().size() == 1) {
                //exit;
                return null;
            }
            int roomId0 = link.getConnectingAreas().get(0);
            int roomId1 = link.getConnectingAreas().get(1);
            List singleton = new ArrayList(1);
            if (agent.getCurrentGoalAreaIds().contains(
                    roomId0)) {

                singleton.add(new IbevacLogicalWaypoint(null, agent.getEnvironmentKnowledge().resolveAreaById(roomId0), null, space, agent));
                return singleton;
            } else if (agent.getCurrentGoalAreaIds().contains(
                    roomId1)) {
                singleton.add(new IbevacLogicalWaypoint(null, agent.getEnvironmentKnowledge().resolveAreaById(roomId1), null, space, agent));
                return singleton;
            }


        }

        assert (agent.getCurrentGoalAreaIds() != null && !agent.getCurrentGoalAreaIds().isEmpty());

        //determine the escape path
        EscapePath escapePath = determineEscapePath(agent.getCurrentGoalAreaIds());
        assert (escapePath != null);

        List<LogicalWaypoint> waypoints = escapePath.getLogicalWaypoints();
        return waypoints;
    }

    private EscapePath determineEscapePath(Collection<Integer> goalAreaIds) {
        //get the knowledge object and the current area id, we'll need it further down		
        CompleteKnowledgeInverted knowledge = (CompleteKnowledgeInverted) agent.getEnvironmentKnowledge();
        int currentAreaId = agent.getCurrentAreaId();
//        System.out.println("deterimining an exit path");
//        int firstLink = currentAreaId;
        GraphPath<Integer, RoomEdge> finalPath = null;
        double minWeight = Double.MAX_VALUE;
//        GraphPath<Integer, RoomEdge> tempPath;
//        int tempRoom;
        int goalArea = -1;



        for (Integer goalAreaId : goalAreaIds) {
            //is current area a room?
//            System.out.println("here" + goalAreaId + "from" + currentAreaId);
            if (knowledge.resolveAreaById(currentAreaId) == null) {
                //currently on a getLink
//              
//                System.out.println("getLink");
                KShortestPaths<Integer, RoomEdge> ksp =
                        new KShortestPaths<Integer, RoomEdge>(
                        knowledge.getGraph(),
                        currentAreaId, 1);
//                KShortestPaths<Integer, RoomEdge> ksp1 = new KShortestPaths<>(knowledge.getGraph(), roomId1, 1);
                TreeMap<Double, GraphPath<Integer, RoomEdge>> paths = new TreeMap<Double, GraphPath<Integer, RoomEdge>>();
                for (CLink connectingLink : knowledge.getConnectingLinks(goalAreaId)) {
                    for (GraphPath<Integer, RoomEdge> path : ksp.getPaths(connectingLink.getId())) {
                        paths.put(path.getWeight(), path);
                    }
                }
//                List<GraphPath<Integer, RoomEdge>> paths1 = ksp1.getPaths(goalAreaId);

                //there should be at least one path to the exit from each room
                assert (paths != null && !paths.isEmpty());
                GraphPath<Integer, RoomEdge> route = paths.firstEntry().getValue();

                if (minWeight >= route.getWeight()) {
                    finalPath = route;
                    minWeight = route.getWeight();
                    goalArea = goalAreaId;
                }

            } //if not, then it must be a area
            else {
//                System.out.println("area");
                for (CLink homeLink : knowledge.getConnectingLinks(currentAreaId)) {
//System.out.println(knowledge.getGraph().edgeSet());
                    KShortestPaths<Integer, RoomEdge> ksp = new KShortestPaths<Integer, RoomEdge>(knowledge.getGraph(), homeLink.getId(), 1);
//                    System.out.println("home getLink " + homeLink.getId());
                    TreeMap<Double, GraphPath<Integer, RoomEdge>> paths = new TreeMap<Double, GraphPath<Integer, RoomEdge>>();
                    for (CLink goalLink : knowledge.getConnectingLinks(goalAreaId)) {
//                        System.out.println("goal getLink " + goalLink.getId());
                        if (goalLink.getId() == homeLink.getId()) {
                            return new EscapePathImpl(goalLink, goalAreaId);
                        }
                        if (ksp.getPaths(goalLink.getId()) == null) {
//                            System.out.println("no path to exit");
                            continue;
                        }
                        for (GraphPath<Integer, RoomEdge> path : ksp.getPaths(goalLink.getId())) {

                            paths.put(path.getWeight(), path);
                        }
                    }
//                List<GraphPath<Integer, RoomEdge>> paths1 = ksp1.getPaths(goalAreaId);

                    //there should be at least one path to the exit from each room
                    assert (paths != null && !paths.isEmpty());
                    GraphPath<Integer, RoomEdge> route = paths.firstEntry().getValue();

                    if (minWeight >= route.getWeight()) {
                        finalPath = route;
                        minWeight = route.getWeight();
//                        firstLink = homeConnectingLink;
                        goalArea = goalAreaId;
                    }

                }

                //there is at least one path to the exit

            }
        }
//        if (finalPath == null) {
//            System.out.println(((IbevacAgent) this.agent).getId() + "," + goalAreaIds.iterator().next());
//        }
//        assert (finalPath != null);
//        //TODO : change this stupid hack;
//        agent.setCurrentGoal(new IbevacLogicalWaypoint(null, knowledge.resolveAreaById(goalArea), null, space, agent).getWP1().getPoint());
        return new EscapePathImpl(finalPath, goalArea);

    }

    private class EscapePathImpl implements EscapePath {

        private GraphPath<Integer, RoomEdge> path = null;
        private final int goalAreaId;
        private final boolean singletonPath;
        private CLink onlyLink;

        public EscapePathImpl(GraphPath<Integer, RoomEdge> path, int goalAreaId) {
            this.path = path;
//            this.linkId = linkId;
            this.goalAreaId = goalAreaId;
            this.singletonPath = false;
        }

        private EscapePathImpl(CLink goalConnectingLine, int goalArea) {
            this.singletonPath = true;
            goalAreaId = goalArea;
            onlyLink = goalConnectingLine;
        }

        @Override
        public List<LogicalWaypoint> getLogicalWaypoints() {

            EnvironmentKnowledgeModule knowledge = agent.getEnvironmentKnowledge();
            //convert the exit path into a list of logical waypoints
            List<LogicalWaypoint> waypoints = new ArrayList<LogicalWaypoint>();



            if (singletonPath) {
//                System.out.println("getLink =" + onlyLink.getId());
//                System.out.println("agent area =" + agent.getCurrentAreaId());
//                System.out.println("goal area =" + goalAreaId);
                
                IbevacLogicalWaypoint linkWaypoint = new IbevacLogicalWaypoint(
                        onlyLink,
                        knowledge.resolveAreaById(agent.getCurrentAreaId()),
                        knowledge.resolveAreaById(goalAreaId),
                        space, agent);
                waypoints.add(linkWaypoint);
                if (goalAreaId != -1) {
                    IbevacLogicalWaypoint areaWaypoint =
                            (new IbevacLogicalWaypoint(
                            null,
                            knowledge.resolveAreaById(goalAreaId),
                            null,
                            space, agent)).setInitialWayPoint(linkWaypoint.getWP1());
                    waypoints.add(areaWaypoint);
                }
                return waypoints;
            }
            if (path == null) {
                return null;
            }

            List<RoomEdge> edges = path.getEdgeList();

//            System.out.println(edges);
            CLink startLink = knowledge.resolveLinkById(path.getStartVertex());
            SpatialWaypoint lastPoint = new SpatialWaypoint(agent.getPosition());

            // If the agent is currently not at a getLink then create a waypoint for the getLink
            if (agent.getCurrentAreaId() != startLink.getId()) {

                Integer otherAreaId = (agent.getCurrentAreaId() == startLink.getConnectingAreas().get(0))
                        ? startLink.getConnectingAreas().get(1)
                        : startLink.getConnectingAreas().get(0);
                IbevacLogicalWaypoint lwp = new IbevacLogicalWaypoint(
                        startLink,
                        knowledge.resolveAreaById(agent.getCurrentAreaId()),
                        knowledge.resolveAreaById(otherAreaId),
                        space, agent);
                waypoints.add(lwp);
                lastPoint = lwp.getWP1();
            }

            CLink prevLink = startLink;

            for (RoomEdge edge : edges) {
                //get both areas that are connected by the edge (there is at least one)
//                CArea area0 = knowledge.resolveAreaById(edge.id0());
//                CArea area1 = knowledge.resolveAreaById(edge.id1());
//                System.out.println(edge);
                CArea room = knowledge.resolveAreaById(edge.areaId());
                if (room instanceof CStaircase) {
                    //must be a staircase. we just break here.
                    //after teleporting a new path has to be calculated
//                    assert (area0 instanceof CStaircase && area1 instanceof CStaircase);

                    //create a logical waypoint, using the links and the areas		
                    assert edge.getRoom0() != null && edge.getRoom1() != null;
                    IbevacLogicalWaypoint wp = new IbevacLogicalWaypoint(null, edge.getRoom0(), edge.getRoom1(), space, agent);
                    waypoints.add(wp);

                    break;
                }

                CLink fromLink = knowledge.resolveLinkById(edge.id0());
                CLink toLink = knowledge.resolveLinkById(edge.id1());
//                System.out.println("prev" + prevLink.getId());
//                System.out.println(fromLink.getId());
//                System.out.println(toLink.getId());

                assert (prevLink.getId() == fromLink.getId() || prevLink.getId() == toLink.getId());

                //by definition, we want area0 to be the next one and area1 the one thereafter
                //if it's not the case swap...
                if (prevLink.getId() != fromLink.getId()) {
                    CLink temp = fromLink;
                    fromLink = toLink;
                    toLink = temp;
                } //get the getLink that is associated with this edge (if any)
                prevLink = toLink;
//                assert agent.getCurrentAreaId() == fromLink.getConnectingAreas().get(0) || agent.getCurrentAreaId() == fromLink.getConnectingAreas().get(1);

                //create a logical waypoint, using the links and the areas
                IbevacLogicalWaypoint areaWaypoint = (new IbevacLogicalWaypoint(null, edge.area(), null, space, agent)).setInitialWayPoint(lastPoint);
                waypoints.add(areaWaypoint);
                assert toLink.getConnectingAreas() != null;
                if (toLink.getConnectingAreas().size() > 1) {
                    Integer otherAreaId = (edge.areaId() == toLink.getConnectingAreas().get(0))
                            ? toLink.getConnectingAreas().get(1)
                            : toLink.getConnectingAreas().get(0);
                    IbevacLogicalWaypoint lwp = new IbevacLogicalWaypoint(
                            toLink,
                            edge.area(),
                            knowledge.resolveAreaById(otherAreaId),
                            space, agent);
                    waypoints.add(lwp);
                    lastPoint = lwp.getWP1();
                } else {
                    IbevacLogicalWaypoint lwp = new IbevacLogicalWaypoint(
                            toLink,
                            edge.area(),
                            null,
                            space, agent);
                    waypoints.add(lwp);
                    lastPoint = lwp.getWP1();
                }


            }
            if (goalAreaId != -1) {
                waypoints.add(new IbevacLogicalWaypoint(null, knowledge.resolveAreaById(this.goalAreaId), null, space, agent).setInitialWayPoint(lastPoint));
            }

            return waypoints;
        }
    }
}
