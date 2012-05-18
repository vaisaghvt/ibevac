/**
 *
 */
package ibevac.agent.perception;

import abmcs.agent.LineSegment;
import ibevac.EvacConstants;
import ibevac.agent.IbevacAgent;
import ibevac.agent.knowledge.waypoints.IbevacSpatialWaypoint;
import ibevac.datatypes.CArea;
import ibevac.datatypes.CExit;
import ibevac.datatypes.CLink;
import ibevac.environment.IbevacSpace;
import ibevac.utilities.Utilities;

import java.util.HashSet;
import java.util.Set;

import abmcs.agent.StaticObstacle;
import ibevac.cue.Cue;

import java.util.ArrayList;
import javax.vecmath.Point2d;

/**
 * @author heiko
 * @deprecated
 */
public class NormalPerception implements Perception {

    private final IbevacSpace space;
    private final IbevacAgent me;
    // private EnvironmentKnowledgeBase environmentKnowledge;
    // private EventKnowledgeBase eventKnowledge;
    // private double sensorRange;
    private int currentFloorIdx = -1;
    private int currentAreaId = -1;
    /**
     * If another agent is within this space the agent shrinks to create space
     * for movement
     */
    private Set<IbevacAgent> personalSpace = new HashSet<IbevacAgent>();
    /**
     * Currently the set of agents occupying space in which agents can exchange information about inaccessible links
     */
    private Set<IbevacAgent> socialSpace = new HashSet<IbevacAgent>();
    /**
     * Set of agents in shouting distance
     */
    private Set<IbevacAgent> shoutingSpace = new HashSet<IbevacAgent>();
    /**
     * Set of agents that are perceived and utilised for collision avoidance
     */
    private Set<IbevacAgent> perceivedPeople = new HashSet<IbevacAgent>();
    /**
     * Set of static obstacles that the agent tries to avoid a collission with
     */
    private Set<StaticObstacle> perceivedStaticObstacles = new HashSet<StaticObstacle>();
    /**
     * Set of cues that are perceived by the agent
     */
    private Set<Cue> perceivedCues = new HashSet<Cue>();

    private Set<IbevacAgent> peers = new HashSet<IbevacAgent>();

    public NormalPerception(IbevacSpace space, IbevacAgent agent,
                            int currentFloorIdx, int currentAreaId) {
        this.space = space;
        me = agent;

        this.currentFloorIdx = currentFloorIdx;
        this.currentAreaId = currentAreaId;
    }

    @Override
    public void update() {
        // determine the current area of the agent
        this.determineCurrentAreaId();

        peers = new HashSet<IbevacAgent>();
        personalSpace = new HashSet<IbevacAgent>();
        socialSpace = new HashSet<IbevacAgent>();
        perceivedPeople = new HashSet<IbevacAgent>();
        perceivedStaticObstacles = new HashSet<StaticObstacle>();
        perceivedCues = new HashSet<Cue>();

        CArea area = space.getAreaById(currentAreaId);

        Point2d position = me.getPosition();
        for (IbevacAgent agent : space.getAgentsByAreaId(area.getId())) {
            if (me == agent) {
                continue;
            }

            // what's the hull-to-hull distance to the other agent?
            Point2d p = agent.getPosition();
            double dx = position.getX() - p.getX();
            double dy = position.getY() - p.getY();
            double d = Math.hypot(dx, dy); // [cm]

            // determine whether the agent should be added to the personal space
            double r = 0.5 * agent.getDiameter();
            if (d - EvacConstants.PERSONAL_SPACE_RADIUS - r <= 0) {
                personalSpace.add(agent);
            }
            if (d - EvacConstants.PERSONAL_SOCIAL_RADIUS - r <= 0) {
                socialSpace.add(agent);
            }

            peers.add(agent);
            perceivedPeople.add(agent);
        }

//        perceivedCues.addAll(space.getCuesForAreaId(currentAreaId));

        // add the obstacle lines for this area
        perceivedStaticObstacles.addAll(getObstacleLines(area));

        if (area instanceof CLink) {
            // if the area is a link, also consider the connecting room areas
            CLink link = (CLink) area;
            for (int connectingAreaId : link.getConnectingAreas()) {
                CArea connectingArea = space.getAreaById(connectingAreaId);
                perceivedStaticObstacles.addAll(getClosedRoomObstacleLines(
                        connectingArea, link));
            }
        } else {
            for (CLink link : space.getLinksForRoom(area.getId())) {
                perceivedStaticObstacles.addAll(getObstacleLines(link));

                for (int connectingAreaId : link.getConnectingAreas()) {
                    if (connectingAreaId == area.getId()) {
                        continue;
                    }

                    CArea connectingArea = space.getAreaById(connectingAreaId);
                    perceivedStaticObstacles.addAll(getClosedRoomObstacleLines(
                            connectingArea, link));
                }
            }
        }

    }

    private Set<StaticObstacle> getObstacleLines(CArea area) {
        Set<StaticObstacle> obstacles = new HashSet<StaticObstacle>();

        Set<LineSegment> oLines = space.getObstacleLinesByArea(area.getId());
        if (oLines != null) {
            for (LineSegment line : oLines) {
                StaticObstacle o = new StaticObstacle(line);
                perceivedStaticObstacles.add(o);
            }
        }

        return obstacles;
    }

    private Set<StaticObstacle> getClosedRoomObstacleLines(CArea area,
                                                           CLink exclusion) {
        Set<StaticObstacle> obstacles = new HashSet<StaticObstacle>();

        // first, add the obstacle lines for this area
        obstacles.addAll(getObstacleLines(area));

        int offset = space.getOffset(space.getFloorByAreaId(area.getId()));

        // second, add the 3 sides of the room that do NOT intersect with the
        // exclusion link
        ArrayList<LineSegment> lsides = new ArrayList(Utilities.extractSidesFromArea(exclusion));
        ArrayList<LineSegment> rsides = new ArrayList(Utilities.extractSidesFromArea(area));
        for (LineSegment rside : rsides) {
            boolean b0 = Utilities.doLinesegmentsIntersect(rside, lsides.get(0));
            boolean b1 = Utilities.doLinesegmentsIntersect(rside, lsides.get(1));
            boolean b2 = Utilities.doLinesegmentsIntersect(rside, lsides.get(2));
            boolean b3 = Utilities.doLinesegmentsIntersect(rside, lsides.get(3));

            if (b0 || b1 || b2 || b3) {
                continue;
            }

            Point2d p0 = new Point2d(rside.getStart());
            Point2d p1 = new Point2d(rside.getEnd());

            p0 = me.translateToPhysicalLocation(p0, offset);
            p1 = me.translateToPhysicalLocation(p1, offset);

            obstacles.add(new StaticObstacle(new LineSegment(p0, p1)));
        }

        return obstacles;
    }

    @Override
    public synchronized int getCurrentFloorIdx() {
        return currentFloorIdx;
    }

    @Override
    public synchronized int getCurrentAreaId() {
        return currentAreaId;
    }

    @Override
    public synchronized void handleTeleport(int floorIdx, int areaId) {
        this.currentFloorIdx = floorIdx;
        this.currentAreaId = areaId;
    }

    @Override
    public synchronized boolean exitReached() {
        CArea area = space.getAreaById(currentAreaId);
        return area instanceof CExit;
    }

    @Override
    public synchronized Set<IbevacAgent> getPersonalSpace() {
        return personalSpace;
    }

    @Override
    public synchronized Set<IbevacAgent> getSocialSpace() {
        return socialSpace;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<IbevacAgent> getPerceivedDynamicObstacles() {
        return perceivedPeople;
    }

    @Override
    public synchronized Set<StaticObstacle> getPerceivedStaticObstacles() {
        return perceivedStaticObstacles;
    }

    @Override
    public synchronized boolean isCurrentWaypointAccessible() {
        IbevacSpatialWaypoint waypoint = (IbevacSpatialWaypoint) me.getLevel1MotionPlanning().getCurrentSpatialWaypoint();
        if (waypoint == null) {
            return true;
        }

        Point2d destination = new Point2d(waypoint.getPoint());
        destination = me.translateToLogicalLocation(destination,
                space.getOffset(currentFloorIdx));

        Point2d position = me.getPosition();
        position = me.translateToLogicalLocation(position,
                space.getOffset(currentFloorIdx));

        int x1 = (int) position.getX();
        int y1 = (int) position.getY();

        double dx = destination.getX() - x1;
        double dy = destination.getY() - y1;
        double d = Math.hypot(dx, dy);

        double ddx = dx / (int) d;
        double ddy = dy / (int) d;

        /*
         * Check a few poiunts from here to the next waypoint for fire presence. 
         * If there is a fire presence, then we get a notification that the area is lethal.
         */
        for (int i = 0; i < (int) d; i++) {

            int x3 = (int) (x1 + i * ddx);
            int y3 = (int) (y1 + i * ddy);
            if (space.getFireModel().isAreaLethal(currentFloorIdx, x3, y3)) {
                return false;
            }
        }

        return true;
    }

    private void determineCurrentAreaId() {
        int previousAreaId = currentAreaId;

        Point2d pos = IbevacSpace.translateToLogicalLocation(me.getPosition(),
                space.getOffset(this.currentFloorIdx));
        int x = (int) pos.getX();
        int y = (int) pos.getY();

        currentAreaId = space.getAreaOfPoint(x, y, this.currentFloorIdx);

        if (currentAreaId == -1) {
            System.out.println(x + "," + y);
        }
        assert previousAreaId != -1 && currentAreaId != -1;

        if (previousAreaId != currentAreaId) {
            space.moveAgentArea(me, previousAreaId, currentAreaId);
        }
    }

    @Override
    public Set<Cue> getCues() {
        return this.perceivedCues;
    }

    @Override
    public boolean hasCues() {
        return !this.perceivedCues.isEmpty();
    }

    @Override
    public synchronized Set<IbevacAgent> getShoutingSpace() {
        return socialSpace;
    }
}
