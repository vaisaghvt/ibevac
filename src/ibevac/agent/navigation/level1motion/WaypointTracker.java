package ibevac.agent.navigation.level1motion;

import abmcs.agent.LineSegment;
import abmcs.agent.MovingAgent;
import abmcs.agent.StaticObstacle;
import abmcs.motionplanning.level2.SpatialWaypoint;
import ibevac.agent.IbevacAgent;
import ibevac.agent.knowledge.waypoints.IbevacSpatialWaypoint;
import ibevac.datatypes.CArea;
import ibevac.engine.IbevacModel;
import ibevac.environment.IbevacSpace;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import utilities.Geometry;

/**
 * <h4> Given a list of logical waypoints this class calculates the next waypoint
 * that the agent should try to aim for. It basically determines the farthest
 * visible waypoint.
 * </h4>
 *
 * @author <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 * @version $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class WaypointTracker {

    private boolean goalReached;
    /**
     * The list of spatial waypoints available from the level 2 motion planning
     * system
     */
    private final List<SpatialWaypoint> roadMap;
    //    private SpatialWaypoint current = null;
    private final IbevacAgent agent;
    /**
     * The Collection of Obstacle Line segments on each floor to determine
     * visibility of waypoints.
     */
    private final HashMap<Integer, Collection<LineSegment>> actualObstaclesByFloor;
    private final HashMap<Point2d, LineSegment> problemLineForPoint;

    /**
     * Initializes an internal reference to the agent and gets the list of actual
     * obstacle line segments.
     *
     * @param agent
     */
    public WaypointTracker(MovingAgent agent, List<SpatialWaypoint> wps) {
        this.agent = (IbevacAgent) agent;
        actualObstaclesByFloor = new HashMap<Integer, Collection<LineSegment>>();
        for (int floorNumber = 0; floorNumber < this.agent.getSpace().getNumberOfFloors(); floorNumber++) {
            actualObstaclesByFloor.put(floorNumber, this.agent.getSpace().getObstacleLinesByFloor(floorNumber));
        }
        problemLineForPoint = new HashMap<Point2d, LineSegment>();
        this.roadMap = wps;
        goalReached = false;
//        reset();
    }

    /**
     * Get's the nearest visible waypoint. It first tries a more strict check
     * where the waypoint should be visible from both shoulder lines. Next it
     * tries a check where the point needs to be visible only from the center.
     * If still not possible it returns a null which causes teh agent to stay still.
     *
     * @return
     */
    public SpatialWaypoint getWaypoint() {
//        if(agent.getPosition().distance(roadMap.get(roadMap.size() - 1).getPoint())<agent.getDiameter()){
//            roadMap=null;
//            System.out.println(((IbevacAgent)agent).getId()+"Goal Reached");
//            return null;
//        }
//        if (roadMap == null) {
//            System.out.println("here");
//            reset();
//            if (roadMap == null) {
//                return null;
//            }
//        }
        if (roadMap.get(roadMap.size() - 1).getPoint().distance(agent.getPosition()) < 3 * IbevacModel.scale) {
            goalReached = true;
            return null;
        }
        SpatialWaypoint result = null;
        int numberOfTries = 0;
//       if(((IbevacAgent)this.agent).isInvestigating()){
//           System.out.println();
//       }
        do {
            /**
             * numberOfTries = 1  : 1st try without calculating waypoints

             * numberOfTries = 3  : weaker test with all points

             */
            numberOfTries++;

            if (numberOfTries == 1) {
                for (int i = roadMap.size() - 1; i >= 0; i--) {

                    Point2d currentGoal = roadMap.get(i).getPoint();
                    int areaId0 = ((IbevacSpatialWaypoint) (roadMap.get(i))).getLogicalWaypoint().getAreaId0();
                    int areaId1 = ((IbevacSpatialWaypoint) (roadMap.get(i))).getLogicalWaypoint().getAreaId1();
                    Vector2d agentToCurrentGoalVector = new Vector2d(agent.getPosition());

                    agentToCurrentGoalVector.sub(currentGoal);

                    Vector2d agentUnitVelocityNormal = new Vector2d(agentToCurrentGoalVector.getY(), -agentToCurrentGoalVector.getX());
                    if (agentToCurrentGoalVector.getX() != 0.0f
                            || agentToCurrentGoalVector.getY() != 0.0f) {
                        agentUnitVelocityNormal.normalize();
                    }
                    // First try without resest and second try with reset
                    Point2d agentTopPosition = new Point2d();
                    agentTopPosition.setX(agent.getPosition().getX()
                            - agentUnitVelocityNormal.getX() * agent.getDiameter() / 2.0);
                    agentTopPosition.setY(agent.getPosition().getY()
                            + agentUnitVelocityNormal.getY() * agent.getDiameter() / 2.0);

                    Point2d agentBottomPosition = new Point2d();
                    agentBottomPosition.setX(agent.getPosition().getX() + agentUnitVelocityNormal.getX() * agent.getDiameter() / 2.0);
                    agentBottomPosition.setY(agent.getPosition().getY() - agentUnitVelocityNormal.getY() * agent.getDiameter() / 2.0);

                    if (visibleFrom(currentGoal, agentTopPosition, areaId0, areaId1)
                            && visibleFrom(currentGoal, agentBottomPosition, areaId0, areaId1)) {
                        result = roadMap.get(i);
                        break;
                    }
                }

            } else { // For third try just do a weak visibility check

                if (visibleFrom(roadMap.get(0).getPoint(), agent.getPosition(), -1, -1)) {
                    result = roadMap.get(0);
                    break;
                }
            }

            if (result == null && numberOfTries == 2) {
                return null;
            }
        } while (result == null);


        return result;
    }

    /**
     * Calculates whether a goal point is visible form another position point
     *
     * @param goal
     * @param position
     * @return true visible
     *         false not visible
     */
    private boolean visibleFrom(Point2d goal, Point2d position, int areaId0, int areaId1) {
        Point2d p1 = new Point2d(position.getX(), position.getY());
        Point2d p2 = new Point2d(goal.getX(), goal.getY());
        if (problemLineForPoint.containsKey(goal)
                && Geometry.lineSegmentIntersectionTest(p1, p2,
                problemLineForPoint.get(goal).getStart(), problemLineForPoint.get(goal).getEnd())) {

            return false;
        }

//        int areaId = this.agent.getSpace().getAreaOfPoint((int) goal.x, (int) goal.y, agent.getCurrentFloorId());
        Set<LineSegment> oLines = this.agent.getSpace().getObstacleLinesByArea(areaId0);
        Set<LineSegment> tempSet = this.agent.getSpace().getObstacleLinesByArea(areaId1);
        if (tempSet != null) {
            oLines.addAll(tempSet);
        }
        if (oLines != null) {
            for (LineSegment obstacle : oLines) {
                if (Geometry.lineSegmentIntersectionTest(p1, p2,
                        obstacle.getStart(), obstacle.getEnd())) {
                    problemLineForPoint.put(goal, obstacle);
                    return false;
                }
            }
        }

//        for (StaticObstacle obstacle : ((IbevacAgent) agent).getPerceivedStaticObstacles()) {
        for (LineSegment obstacle : this.actualObstaclesByFloor.get(agent.getCurrentFloorId())) {
            if (Geometry.lineSegmentIntersectionTest(p1, p2,
                    obstacle.getStart(), obstacle.getEnd())) {
                problemLineForPoint.put(goal, obstacle);
                return false;
            }
        }
        return true;
    }

    /**
     * Used to calc shoulder lines for StarPruning algorithm
     *
     * @param position
     * @param velocity
     * @param radius
     * @return
     */
    static double[][] determineShoulderLines(double[] position, double[] velocity, double radius) {
        double len = Math.hypot(velocity[0], velocity[1]);

        double[] line0 = new double[]{position[0], position[1], position[0] + velocity[0], position[1] + velocity[1], len};
        double[] line1 = new double[]{position[0], position[1], position[0] + velocity[0], position[1] + velocity[1], len};
        double[] line2 = new double[]{position[0], position[1], position[0] + velocity[0], position[1] + velocity[1], len};

        if (len > Geometry.EPSILON) {
            double[] unit = new double[]{velocity[0] / len, velocity[1] / len};
            double[] unit2 = new double[]{unit[0] * radius, unit[1] * radius};

            double[] p1 = new double[]{-unit2[1], unit2[0]};
            double[] p2 = new double[]{unit2[1], -unit2[0]};

            //the left and right shoulder lines
            line1 = new double[]{line0[0] + p1[0], line0[1] + p1[1], line0[2] + p1[0], line0[3] + p1[1], len};
            line2 = new double[]{line0[0] + p2[0], line0[1] + p2[1], line0[2] + p2[0], line0[3] + p2[1], len};
        }

        return new double[][]{line0, line1, line2};
    }

    boolean reachedGoal() {
        return goalReached;
    }
}
