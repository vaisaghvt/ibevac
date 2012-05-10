package ibevac.agent.perception;

import ibevac.EvacConstants;
import ibevac.agent.IbevacAgent;
import ibevac.agent.knowledge.waypoints.IbevacSpatialWaypoint;
import ibevac.cue.Cue;
import ibevac.datatypes.CArea;
import ibevac.datatypes.CExit;
import ibevac.environment.IbevacSpace;

import java.util.HashSet;
import java.util.Set;

import abmcs.agent.StaticObstacle;
import ibevac.cue.FastAgentCue;
import ibevac.cue.SlowAgentCue;
import javax.vecmath.Point2d;

/**
 * <h4> In this implementation of perception the other people or dynamic obstacles
 * are perceived from only the agent's own room. Cues are perceived from the 
 * small room. and static obstacles are perceived within a circular radius of 
 * the agent using obstacle space.</h4>
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class IbevacPerception implements Perception {

    private IbevacSpace space;
    private IbevacAgent me;
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
     * 
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
     * 
     */
    private Set<StaticObstacle> perceivedStaticObstacles = new HashSet<StaticObstacle>();
    /**
     * Set of cues that are perceived by the agent
     */
    private Set<Cue> perceivedCues = new HashSet<Cue>();

    public IbevacPerception(IbevacSpace space, IbevacAgent agent,
            int currentFloorIdx, int currentAreaId) {
        this.space = space;
        me = agent;

        this.currentFloorIdx = currentFloorIdx;
        this.currentAreaId = currentAreaId;
    }

    /**
     *  Other people or dynamic obstacles are perceived from only the agent's 
     * own room. Cues are perceived from the small room. and static obstacles 
     * are perceived within a circular radius of the agent using obstacle space.
     */
    @Override
    public void update() {
        // determine the current area of the agent
        // determine the current area of the agent
        this.determineCurrentAreaId();


        personalSpace = new HashSet<IbevacAgent>();
        socialSpace = new HashSet<IbevacAgent>();
        shoutingSpace = new HashSet<IbevacAgent>();
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


            perceivedPeople.add(agent);
        }

        perceivedCues.addAll(space.getCuesInRadius(me, me.getSensorRange()));
//        perceivedCues.addAll(space.getCuesInVicinity(me, area));

        // add the obstacle lines regardless of area
        perceivedStaticObstacles.addAll(space.getObstaclesInRadius(me, me.getSensorRange()));
//        shoutingSpace.addAll(space.getAgentsInRadius(me, me.getSensorRange()));

    }

    @Override
    public synchronized int getCurrentFloorIdx() {
        return currentFloorIdx;
    }

    @Override
    public synchronized int getCurrentAreaId() {
        return currentAreaId;
    }

    /**
     * @see Perception#handleTeleport(int, int) 
     * @param floorIdx
     * @param areaId 
     */
    @Override
    public synchronized void handleTeleport(int floorIdx, int areaId) {
        this.currentFloorIdx = floorIdx;
        this.currentAreaId = areaId;
    }

    /**
     * @see Perception#exitReached() 
     * @return 
     */
    @Override
    public synchronized boolean exitReached() {
        CArea area = space.getAreaById(currentAreaId);
        return area instanceof CExit;
    }

    /**
     * @see Perception#getPersonalSpace() 
     * @see #update() 
     * @return 
     */
    @Override
    public synchronized Set<IbevacAgent> getPersonalSpace() {
        return personalSpace;
    }

    /**
     * @see Perception#getSocialSpace() 
     * @see #update() 
     * @return 
     */
    @Override
    public synchronized Set<IbevacAgent> getSocialSpace() {
        return socialSpace;
    }

    /**
     * @see Perception#getShoutingSpace() 
     * @see #update() 
     * @return 
     */
    @Override
    public synchronized Set<IbevacAgent> getShoutingSpace() {
        return shoutingSpace;
    }

    /**
     * @see Perception#getPerceivedDynamicObstacles() 
     * @see #update() 
     * @return 
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<IbevacAgent> getPerceivedDynamicObstacles() {
        return perceivedPeople;
    }

    /**
     * @see Perception#getPerceivedStaticObstacles() 
     * @see #update() 
     * @return 
     */
    @Override
    public synchronized Set<StaticObstacle> getPerceivedStaticObstacles() {
        return perceivedStaticObstacles;
    }

    /**Determines the accessibility of the current Waypoint by checking it's 
     * lethality at both the fire and smoke space
     * @see Perception#isCurrentWaypointAccessible() 
     * @see #update() 
     * @return 
     */
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

        for (int i = 0; i < (int) d; i++) {

            int x3 = (int) (x1 + i * ddx);
            int y3 = (int) (y1 + i * ddy);
            if (space.getFireModel().isAreaLethal(currentFloorIdx, x3, y3)) {
                return false;
            }
            if (space.getSmokeModel().isAreaSmoky(currentFloorIdx, x3, y3)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Determines the current area Id and moves the agent to it's correct 
     * logical position if it's physical moveent has caused it to move from one 
     * area to the next.
     */
    private void determineCurrentAreaId() {
        int previousAreaId = currentAreaId;

        Point2d pos = me.translateToLogicalLocation(me.getPosition(),
                space.getOffset(this.currentFloorIdx));


        currentAreaId = space.getAreaOfPoint((int) pos.getX(), (int) pos.getY(), this.currentFloorIdx);

        if (previousAreaId != currentAreaId) {
//            System.out.println("here");
            space.moveAgentArea(me, previousAreaId, currentAreaId);
        }
    }

    /** 
     * @see #update() 
     * @return 
     */
    @Override
    public Set<Cue> getCues() {
        return this.perceivedCues;
    }

    @Override
    public boolean hasCues() {
//        System.out.println(this.perceivedCues.size());
        return !this.perceivedCues.isEmpty();
    }
}
