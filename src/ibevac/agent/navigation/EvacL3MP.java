package ibevac.agent.navigation;

import abmcs.motionplanning.level2.SpatialWaypoint;
import ibevac.agent.knowledge.waypoints.IbevacLogicalWaypoint;
import ibevac.agent.IbevacAgent;
import ibevac.agent.knowledge.environment.EnvironmentKnowledgeModule;
import ibevac.datatypes.CArea;
import ibevac.datatypes.CEdge;
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
import ibevac.agent.knowledge.environment.CompleteKnowledge;
import java.util.Collection;

/**
 * <h4>The level 3 motion planning algorithm that determines the set of logical 
 * waypoints. This works using the Complete Knowledge Environment knowledge 
 * module. </h4>
 *
 *  <h4> Does not consider distance between logical waypoints. Path is 
 * determined based on number of logical waypoints alone.
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class EvacL3MP implements Level3MotionPlanning {

    private IbevacSpace space = null;
    private IbevacAgent agent = null;

    public EvacL3MP(IbevacSpace space, IbevacAgent agent) {
        super();
        this.space = space;
        this.agent = agent;
    }

    /**
     * Get the ordered list of logical waypoints for the agent to pass through 
     * to get to his current goal.
     *
     * If he is already at his goal area or on one of the links leading to his 
     * goal area then the current areas logical waypoints is 
     * returned from which the spatial waypoints can eventually be obtained.
     * 
     * Else the determineEscapePath() is called to determine best path to exit
     * 
     * 
     * @return List of logical waypoints 
     * 
     * @see EvacL3MP#determineEscapePath(java.util.Collection) 
     */
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

            //...and its associated getLink
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

                singleton.add(new IbevacLogicalWaypoint(null, 
                        agent.getEnvironmentKnowledge().resolveAreaById(roomId0), 
                        null, space, agent).
                        setInitialWayPoint(new SpatialWaypoint(agent.getPosition())));
                return singleton;
            } else if (agent.getCurrentGoalAreaIds().contains(
                    roomId1)) {
                singleton.add(new IbevacLogicalWaypoint(null, 
                        agent.getEnvironmentKnowledge().resolveAreaById(roomId1), 
                        null, space, agent).
                        setInitialWayPoint(new SpatialWaypoint(agent.getPosition())));
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

    /**
     * Determines an EscapePath for the agent to the exit.
     * 
     * This function determines the paths to all the possible goals of the agent a
     * and chooses the shortest path to the closest goal in the list. This shortest 
     * path is found using KShortestPaths class
     * @param goalAreaIds
     * @return 
     * 
     * @see EscapePath
     * @see EscapePathImpl#getLogicalWaypoints() 
     * @see KShortestPaths#KShortestPaths(org.jgrapht.Graph, java.lang.Object, int) 
     * @see KShortestPaths#getPaths(java.lang.Object) 
     */
    private EscapePath determineEscapePath(Collection<Integer> goalAreaIds) {
        //get the knowledge object and the current area id, we'll need it further down		
        CompleteKnowledge knowledge = (CompleteKnowledge) agent.getEnvironmentKnowledge();
        int currentAreaId = agent.getCurrentAreaId();
        int firstRoom = currentAreaId;
        GraphPath<Integer, CEdge> finalPath = null;
        double minWeight = Double.MAX_VALUE;
        GraphPath<Integer, CEdge> tempPath;
        int tempRoom;
        int goalArea = -1;

        for (Integer goalAreaId : goalAreaIds) {
            //is current area a room?
//            System.out.println("here" + goalAreaId + "from" + currentAreaId);
            if (knowledge.resolveAreaById(currentAreaId) != null) {
                //determine one shortest path from this room to the nearest exit
                KShortestPaths<Integer, CEdge> ksp = new KShortestPaths<Integer, CEdge>(knowledge.getGraph(), currentAreaId, 1);

                List<GraphPath<Integer, CEdge>> paths = ksp.getPaths(goalAreaId);

                //there is at least one path to the exit
                assert (paths != null && !paths.isEmpty());
                if (minWeight >= paths.get(0).getWeight()) {
                    finalPath = paths.get(0);
                    minWeight = paths.get(0).getWeight();
                    goalArea = goalAreaId;
                }
            } //if not, then it must be a getLink
            else {
                //get the edge...
                CEdge edge = knowledge.resolveEdgeById(currentAreaId);
                assert (edge != null);

                //...and its associated getLink
                CLink link = edge.link();
                assert (link != null);

                //the the connecting room ids
                int roomId0 = link.getConnectingAreas().get(0);
                int roomId1 = link.getConnectingAreas().get(1);

                //since we're in between both rooms, determine the shortest path from both 
                //rooms to the nearest exit and select the shortest...			
                KShortestPaths<Integer, CEdge> ksp0 = new KShortestPaths<Integer, CEdge>(knowledge.getGraph(), roomId0, 1);
                KShortestPaths<Integer, CEdge> ksp1 = new KShortestPaths<Integer, CEdge>(knowledge.getGraph(), roomId1, 1);
                List<GraphPath<Integer, CEdge>> paths0 = ksp0.getPaths(goalAreaId);
                List<GraphPath<Integer, CEdge>> paths1 = ksp1.getPaths(goalAreaId);

                //there should be at least one path to the exit from each room
                assert (paths0 != null && paths1 != null && !paths0.isEmpty() && !paths1.isEmpty());
                GraphPath<Integer, CEdge> route0 = paths0.get(0);
                GraphPath<Integer, CEdge> route1 = paths1.get(0);

                //determine which path is the shorter one
                double w0 = route0.getWeight();
                double w1 = route1.getWeight();


                if (w0 > w1) {
                    tempPath = route0;
                    tempRoom = roomId0;

                } else {
                    tempPath = route1;
                    tempRoom = roomId1;
                }
                assert (tempPath != null);
                if (minWeight >= tempPath.getWeight()) {
                    finalPath = tempPath;
                    firstRoom = tempRoom;
                    minWeight = tempPath.getWeight();
                    goalArea = goalAreaId;
                }
            }
        }
//        if (finalPath == null) {
//            System.out.println(((IbevacAgent) this.agent).getId() + "," + goalAreaIds.iterator().next());
//        }
//        assert (finalPath != null);
//        //TODO : change this stupid hack;
//        agent.setCurrentGoal(new IbevacLogicalWaypoint(null, knowledge.resolveAreaById(goalArea), null, space, agent).getWP1().getPoint());
        return new EscapePathImpl(finalPath, firstRoom, goalArea);

    }

    /**
     * Private extension of EscapePath which provides modularity for determining 
     * the logical waypoints from a GraphPath
     * 
     * @see GraphPath
     */
    private class EscapePathImpl implements EscapePath {

        /**
         * The path to exit determiend by the shortest path finding algorithm
         */
        private GraphPath<Integer, CEdge> path = null;
        /**
         * The first room in the path
         */
        private int areaId = -1;
        /**
         * The goal area
         */
        private final int goalAreaId;

        /**
         * 
         * 
         * @param path          GraphPath determined by Shortest Path algorithm
         * @param areaId        The starting area ID of the path
         * @param goalAreaId    The ending area ID of the path
         * 
         * @see GraphPath
         */
        public EscapePathImpl(GraphPath<Integer, CEdge> path, int areaId, int goalAreaId) {
            this.path = path;
            this.areaId = areaId;
            this.goalAreaId = goalAreaId;
        }

        /**
         * Get's the ordered list of logical waypoints for the agent to traverse 
         * through to getf from areaId to goalAreaId. This list is extracted and
         * processed from the edge list in the path
         * @return Ordered list of logical waypoints
         * @see GraphPath#getEdgeList() 
         */
        @Override
        public List<LogicalWaypoint> getLogicalWaypoints() {
            if (path == null) {
                return null;
            }
            EnvironmentKnowledgeModule knowledge = agent.getEnvironmentKnowledge();

            //convert the exit path into a list of logical waypoints
            List<LogicalWaypoint> waypoints = new ArrayList<LogicalWaypoint>();
            List<CEdge> edges = path.getEdgeList();

            /*In case of a getLink this is indeed the next areaId. In case it is in room, this area will be the 
             * current area Id. This still makes sense because the next step will be in this areaId.
             * 
             */
            int nextAreaId = areaId;
            SpatialWaypoint lastPoint = new SpatialWaypoint(agent.getPosition());
            boolean staircase = false;
            for (CEdge edge : edges) {
                //get both areas that are connected by the edge (there is at least one)
                CArea area0 = knowledge.resolveAreaById(edge.id0());
                CArea area1 = knowledge.resolveAreaById(edge.id1());

                assert (nextAreaId == area0.getId() || nextAreaId == area1.getId());

                //by definition, we want area0 to be the next one and area1 the one thereafter
                //if it's not the case swap...
                if (nextAreaId != area0.getId()) {
                    CArea temp = area0;
                    area0 = area1;
                    area1 = temp;
                }

                //get the getLink that is associated with this edge (if any)
                CLink link = edge.link();
                if (link == null) {
                    //must be a staircase. we just break here.
                    //after teleporting a new path has to be calculated
                    assert (area0 instanceof CStaircase && area1 instanceof CStaircase);

                    //create a logical waypoint, using the links and the areas					
                    IbevacLogicalWaypoint wp = new IbevacLogicalWaypoint(null, area0, area1, space, agent);
                    waypoints.add(wp);
                    lastPoint = wp.getWP1();
                    staircase = true;

                    break;
                } else {
                    //create a logical waypoint, using the links and the areas	
                    IbevacLogicalWaypoint wp1 = (new IbevacLogicalWaypoint(null, area0, null, space, agent)).setInitialWayPoint(lastPoint);
                    waypoints.add(wp1);
                    IbevacLogicalWaypoint wp = new IbevacLogicalWaypoint(link, area0, area1, space, agent);
                    waypoints.add(wp);

                    lastPoint = wp.getWP1();

                    //if there is no area1, then this can only mean the getLink is an exit					
                    if (area1 == null) {
                        assert (link instanceof CExit);

                        //nothing to do, we're done
                        break;
                    } else {
                        //continue with the next area
                        nextAreaId = area1.getId();
                    }
                }
            }
            if (goalAreaId != -1 && !staircase) {
                waypoints.add(new IbevacLogicalWaypoint(null, knowledge.resolveAreaById(this.goalAreaId), null, space, agent).setInitialWayPoint(lastPoint));
            }

            return waypoints;
        }
    }
}
