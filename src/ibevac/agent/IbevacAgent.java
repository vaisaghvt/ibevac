package ibevac.agent;

import ibevac.agent.planner.DefaultPlanner;
import ibevac.EvacConstants;
import ibevac.agent.knowledge.environment.EnvironmentKnowledgeModule;
import ibevac.agent.knowledge.KnowledgeBase;
import ibevac.agent.navigation.EvacL2MP;
import ibevac.agent.navigation.EvacL3MP;
import ibevac.agent.knowledge.waypoints.IbevacLogicalWaypoint;
import ibevac.agent.knowledge.waypoints.IbevacSpatialWaypoint;
import ibevac.agent.navigation.NavigationModule;
import ibevac.agent.perception.NormalPerception;
import ibevac.agent.perception.Perception;
import ibevac.datatypes.CEvacuationScenario;
import ibevac.engine.IbevacModel;
import ibevac.environment.IbevacSpace;
import ibevac.utilities.IbevacRNG;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Proxiable;
import abmcs.agent.MovingAgent;
import abmcs.agent.PhysicalAgent;
import abmcs.agent.StaticObstacle;
import abmcs.motionplanning.level0.Level0MotionPlanning;
import abmcs.motionplanning.level0.PhysicalObject;

import abmcs.motionplanning.level1.Level1MotionPlanning;

import abmcs.motionplanning.level2.Level2MotionPlanning;
import abmcs.motionplanning.level3.Level3MotionPlanning;
import abmcs.motionplanning.level3.LogicalWaypoint;
import ibevac.agent.AgentDescriptionModule.AgentType;
import ibevac.agent.navigation.IbevacL3MP;
import ibevac.agent.navigation.level1motion.L1MPStarPruning;
import ibevac.agent.perception.IbevacPerception;
import ibevac.cue.Cue;
import ibevac.cue.FastAgentCue;
import ibevac.agent.navigation.level1motion.SimpleRVO2;
import ibevac.agent.planner.Planner;
import ibevac.agent.planner.states.Dead;
import ibevac.agent.planner.states.Escaping;
import ibevac.agent.planner.states.Safe;
import ibevac.agent.planner.states.State;
import ibevac.agent.planner.states.Trapped;
import ibevac.cue.MessageCue;
import ibevac.cue.ShoutedCue;
import ibevac.cue.SmokeCue;
import ibevac.datatracker.DatabaseHandler;
import ibevac.datatypes.SmallRoom;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

/**
 *  <h4>This class describes the actual IBEVAC agent and implements the IBEVAC
 *  Agent architecture</h4>
 *  
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class IbevacAgent extends MovingAgent implements Steppable, Proxiable {

    /**
     * The number of agents created. Used to give the agent an ID.
     */
    public static int numberOfAgents = 0;
    /**
     * Used to create checkpoints for restarting simulation at a particular 
     * point
     */
    private static final long serialVersionUID = -5878351774781203487L;
    /**
     * Determines if the agent dies when in a lethal area.
     */
    private final double DEATH_PROBABILITY = 0.1;
    /**
     * Reference to the agents steppable so that the agent can be removed from 
     * the schedule when the agent dies, escapes or becomes safe.
     */
    private Stoppable stoppable = null;
    /**
     * Reference to the environment that the agent is placed in. Useful for many
     * of the submodules to work properly by interfacing with the environment.
     */
    private IbevacSpace space;
    /**
     * Used to identify an agent uniquely.
     */
    private final int id;
    /**
     * Identifies the agent's home area.
     */
    private final int homeId;
    /**
     * Initialize the preferred speed of the agent from the constants in 
     * EvacConstants
     */
    private double preferredSpeed;
    /**
     * Agent's Perception Module
     */
    private Perception perception;
    /**
     * Agent's Knowledge module
     */
    KnowledgeBase knowledge;
    /**
     * Agent's strategy and planning module.
     */
    Planner planner;
    /**
     * Agent's Navigation module
     */
    private NavigationModule navigationModule;
    /**
     * local variable for smoke speed adjusment
     */
    private double speedBeforeSmoke;
    /**
     * Adjusts the agent's sensor range.
     */
    private final double sensorRange;
    private final AgentType type;
    private MessageCue message;
    private int runid;
    private Point2d prevPosition;
    private int prevFloorIdx;
    private DescriptiveStatistics speedStat = new DescriptiveStatistics();
    private double lifetime =-1;
    private double evacStartTime;
    private double evacuationTime = -1;

    /**
     * Constructs the Ibevac agent.
     * 
     * @param physicalObject  The object that is used by the physical engine
     * to ensure that the model produces sensible results.
     * 
     * @param level0          The level0 motionPlanning Engine that does the 
     * actual physical sanity checks.
     * 
     * @param scenario        The CEvacuationScenario extracted from xml which describes
     *  the environment to be used for initializing environment knowledge.
     * 
     * @param space            Used by submodules which need to know the space. 
     * layout
     * 
     * @param floorIndex       The floor at which the agent was created.
     * @param areaIndex        The area at which the agent was created.
     */
    public IbevacAgent(PhysicalObject physicalObject,
            Level0MotionPlanning level0, CEvacuationScenario scenario,
            IbevacSpace space, int floorIndex, int areaIndex, AgentType type) {
        super(physicalObject);
        this.runid = runid;
        sensorRange = EvacConstants.SENSOR_RANGE * this.getDiameter() / 2.0;
        id = numberOfAgents++;
        this.space = space;



//        navigationModule = new NavigationModule(this,
//                new L1MPStarPruning(this), new EvacL2MP(this), new EvacL3MP(
//                space, this));

//        navigationModule = new NavigationModule(this,
//                new RVO2(this), new EvacL2MP(this), new EvacL3MP(
//                space, this));
        navigationModule = new NavigationModule(this,
                new SimpleRVO2(this), new EvacL2MP(this), new EvacL3MP(
                space, this));
//        navigationModule = new NavigationModule(this,
//                new SimpleRVO2(this), new EvacL2MP(this), new IbevacL3MP(
//                space, this));


        this.type = type;
        switch (type) {
            case DEFAULT:
                AgentDescriptionModule.createDefaultAgent(scenario, this);
                break;
            case MANAGEMENT:
                AgentDescriptionModule.createManagementAgent(scenario, this);
                break;
        }
        this.homeId = areaIndex;

//        perception = new NormalPerception(space, this, floorIndex, areaIndex);
        perception = new IbevacPerception(space, this, floorIndex, areaIndex);

        initializePreferredSpeed();
        speedBeforeSmoke = -1;

        if (this.message == null) {
            message = new MessageCue(true, type);
        }
    }

    /**
     * Initializes the agent's preferred speed using the vales in EvacConstants.
     */
    private void initializePreferredSpeed() {
        double meanPS = EvacConstants.AGENT_AVG_SPEED; // [m]
        double stddevPS = EvacConstants.AGENT_STDDEV_SPEED; // [m]
        preferredSpeed = IbevacRNG.instance().nextGaussian() * stddevPS
                + meanPS;
        if (preferredSpeed < EvacConstants.AGENT_MIN_SPEED) {
            preferredSpeed = EvacConstants.AGENT_MIN_SPEED;
        } else if (preferredSpeed > EvacConstants.AGENT_MAX_SPEED) {
            preferredSpeed = EvacConstants.AGENT_MAX_SPEED;
        }
    }

    /**
     * Shout about stuff to other agents. Besides information, these are also 
     * cues which affect the current state of agents that hear this shout.
     * @param cues This is a set of ShoutedCues that are passed to other agents.
    
     */
    private void conveyShoutedInformation(Set<ShoutedCue> cues) {
        for (ShoutedCue cue : cues) {
            this.knowledge.getEventKnowledge().perceiveCue(new ShoutedCue(cue, this));
        }
    }

    private void sendMessage(Set<IbevacAgent> peers, Set<IbevacLogicalWaypoint> inaccessibleWaypoints, Set<Cue> cues) {
        //TODO : Always sending message of escape only!

        for (IbevacAgent peer : peers) {
//        peer.receiveInformationAboutCues(cues);
            peer.receiveMessage(message);
            peer.receiveInformationAboutInaccessibleWaypoints(inaccessibleWaypoints);
        }
    }

    private void receiveMessage(MessageCue message) {
        if (type != AgentType.MANAGEMENT) {
            this.knowledge.getEventKnowledge().perceiveCue(message);
        }
    }

    /**
     * Process information about inaccessible links provided by other agents
     * @param inaccessibleWaypoints The set of inaccessible waypoints to be 
     * added to current list of inaccessible points
     */
    private void receiveInformationAboutInaccessibleWaypoints(
            Set<IbevacLogicalWaypoint> inaccessibleWaypoints) {

        boolean needReset = false;
        for (IbevacLogicalWaypoint lwaypoint : inaccessibleWaypoints) {
            needReset = needReset
                    || this.getEnvironmentKnowledge().markWaypointAsInaccessible(lwaypoint);
        }
        if (needReset) {
            this.getLevel1MotionPlanning().reset();
        }
    }

    /**
     * Process information about cues form other agents. This will strengthen or
     * weaken the beliefs and conviction of the agent.
     * 
     * @param cues The cues that the other agents tells this agent about
     */
    private void receiveInformationAboutCues(Iterable<Cue> cues) {
        for (Cue cue : cues) {
            if (!perception.getCues().contains(cue)) {
                this.knowledge.getEventKnowledge().perceiveCue(cue);
            }
        }
    }

    /**
     * Updates an agents state. This is done in the following steps: 1) Perceive
     * the environment 2) Update the physical condition(s) 3) Process sensor
     * information 4) Act
     */
    @Override
    public void step(SimState state) {
//planner.setEscaping();
//         System.out.println("Agent"+id);


        // perceive the environment
        this.perception.update();

        for (IbevacAgent neighbour : perception.getShoutingSpace()) {
            neighbour.conveyShoutedInformation(this.knowledge.getShoutedInformation());
        }


        // System.out.println("agent="+ this.id +
        // " area="+perception.getCurrentAreaId() +
        // " floor="+this.getCurrentFloorIdx());

        // determine current logical position
        Point2d pos = this.translateToLogicalLocation(this.getPosition(),
                space.getOffset(this.perception.getCurrentFloorIdx()));



        // is the position within a staircase?
        int currentAreaId = this.perception.getCurrentAreaId();
        if (!perception.hasCues()) {
            this.knowledge.getEventKnowledge().perceiveLackOfCues();
        }
        for (Cue cue : perception.getCues()) {
            this.knowledge.getEventKnowledge().perceiveCue(cue);
        }
        float smoke = space.getSmokeAtPoint((int) pos.x, (int) pos.y, getCurrentFloorId());
        if (smoke > 400) {
            if (speedBeforeSmoke == -1) {
                speedBeforeSmoke = preferredSpeed;
            }
            if (preferredSpeed > 0.5) {
                preferredSpeed -= 0.01 * preferredSpeed;
            }
        } else if (speedBeforeSmoke != -1) {
            preferredSpeed = speedBeforeSmoke;
            speedBeforeSmoke = 1;
        }

        this.knowledge.getEventKnowledge().updateState();


        if (space.isRoomAStaircase(currentAreaId)) {
            List<LogicalWaypoint> waypoints = this.getLevel3MotionPlanning().getLogicalWaypoints();
            IbevacLogicalWaypoint lwp = (IbevacLogicalWaypoint) waypoints.get(0);

            IbevacSpatialWaypoint wp0 = lwp.getWP0();
            IbevacSpatialWaypoint wp1 = lwp.getWP1();

            // do we need to do a teleport?
            int f0 = wp0.getFloorIndex();
            int f1 = wp1.getFloorIndex();
            if (f0 != f1) {
                int fcurrent = perception.getCurrentFloorIdx();
                assert (f0 == fcurrent || f1 == fcurrent);

                int fnext = (f0 == fcurrent) ? f1 : f0;

                // remove the offset from the current floor and add the offset
                // of the new floor
                double[] position = new double[]{this.getPosition().getX(), this.getPosition().getY()};
                position[0] -= space.getOffset(fcurrent);
                position[0] += space.getOffset(fnext);
                this.setPosition(position[0], position[1]);

                space.moveAgentArea(this, lwp.getAreaId0(), lwp.getAreaId1());
                space.moveAgentFloor(this, fcurrent, fnext);

                this.perception.handleTeleport(fnext, lwp.getAreaId1());
                this.getLevel1MotionPlanning().reset();
            }
        }

        // update the physical condition, i.e., for now this means determine
        // whether the
        // agent is dead...
        if (space.getFireModel().isAreaLethal(
                this.perception.getCurrentFloorIdx(), (int) pos.getX(),
                (int) pos.getY())) {
            // the agent dies with a certain probability when it is in a lethal
            // area
            if (IbevacRNG.instance().nextDouble() < DEATH_PROBABILITY) {
                // System.out.println("agent died!");
                this.stoppable.stop();
                planner.transitionToDead();
                space.removeAgent(this);
                return;
            }
        }
        // update the physical condition, i.e., for now this means determine
        // whether the
        // agent is dead...
        if (space.getSmokeModel().isAreaSmoky(
                this.perception.getCurrentFloorIdx(), (int) pos.getX(),
                (int) pos.getY())) {
            // the agent dies with a certain probability when it is in a lethal
            // area
            if (IbevacRNG.instance().nextDouble() < DEATH_PROBABILITY) {
                // System.out.println("agent died!");
                this.stoppable.stop();
                planner.transitionToDead();
                space.removeAgent(this);
                return;
            }
        }

        // have we reached the exit?
        if (perception.exitReached()) {
            // good for us. looks like we've survived...
            // System.out.println("agent escaped!");
            this.stoppable.stop();
            planner.transitionToSafe();
            space.removeAgent(this);
            return;
        }

        // any agents in our personal space?
        Set<IbevacAgent> personalSpace = this.perception.getPersonalSpace();



        double scale = this.getScale();

//        if (scale == 1.0 && personalSpace.size() > 0) {
//            // let's try to make ourselves smaller (i.e., squeeze a little)
//            if (expandTime == 0) {
//                shrinkTime = 10;
//                scale = 1.0 - IbevacRNG.instance().nextDouble() * 0.5;
//                this.setScale(scale);
//            } else {
//                expandTime--;
//            }
//        } else if (scale != 1.0) {
//            if (shrinkTime == 0 || personalSpace.isEmpty()) {
//                shrinkTime = 0;
//                this.setScale(1.0);
//                expandTime = 60;
//            } else {
//                shrinkTime--;
//            }
//        }
        if (scale == 1.0 && personalSpace.size() > 0 && this.getVelocity().length() < 0.5) {
            //let's try to make ourselves smaller (i.e., squeeze a little)
            scale = 1.0 - IbevacRNG.instance().nextDouble() * 0.5;
            this.setScale(scale);
        } else if (scale != 1.0) {
            this.setScale(1.0);
        }

        // is the current waypoint still accessible?
        if (!this.perception.isCurrentWaypointAccessible()) {
            // mark waypoint as inaccessible
            IbevacSpatialWaypoint swaypoint =
                    (IbevacSpatialWaypoint) this.getLevel1MotionPlanning().getCurrentSpatialWaypoint();
            this.getEnvironmentKnowledge().markWaypointAsInaccessible(
                    swaypoint.getLogicalWaypoint());

            this.getLevel1MotionPlanning().reset();
        }

        // share knowledge about inaccessible waypoints with peers
        Set<IbevacLogicalWaypoint> inaccessibleWaypoints = this.getEnvironmentKnowledge().getInaccessibleWaypoints();
//        if (!inaccessibleWaypoints.isEmpty()) {

//                peer.receiveInformationAboutInaccessibleWaypoints(inaccessibleWaypoints);
//                peer.receiveInformationAboutCues(perception.getCues());
        if (this.type == AgentType.MANAGEMENT || this.getState() instanceof Escaping) {
            sendMessage(this.perception.getSocialSpace(), inaccessibleWaypoints, perception.getCues());
        }

        if (planner.hasActions()) {
            planner.executeActions();
        }


        prevPosition = new Point2d(this.getPosition());
        prevFloorIdx = getCurrentFloorId();
    }

    public State getState() {
        return planner.getState();
    }

    public void kill() {
        try{
            this.stoppable.stop();
            this.planner.transitionToDead();
            space.removeAgent(this);
            
            lifetime = space.getModel().schedule.getTime();
            
        }catch(Exception e){
            e. printStackTrace();
        }
    }

    public Point2d getPrevPosition() {
        return this.prevPosition;
    }

    public DescriptiveStatistics getSpeedStat() {
        return this.speedStat;
    }
    
    public int getEvacStartTime(){
        return (int) evacStartTime;
    }

    public int getLifeTime() {
        return (int) lifetime;
    }

    public int getPrevFloorIdx() {
        return this.prevFloorIdx;
    }

    public void updateEvacStartTime() {
        this.evacStartTime = space.getModel().schedule.getTime();
    }

    public void updateEvacuationTime() {
        this.evacuationTime = space.getModel().schedule.getTime();
    }



    /**
     * This inner class enables probing of agents to inspect and analyse them 
     * through the inspector. The working is determined by MASON's internal
     * workings
     */
    public class IbevacAgentProxy {

        //
        // public double[] getVelocity() {
        // return IbevacAgent.this.getVelocity();
        // }
        public String getName() {
            return "Agent " + IbevacAgent.this.getId();
        }

        public String getLogicalPosition() {
            return IbevacAgent.this.getLogicalPosition().getX() / IbevacModel.scale
                    + "," + IbevacAgent.this.getLogicalPosition().getY()
                    / IbevacModel.scale;
        }

        public int getRoomId() {
            return IbevacAgent.this.getCurrentAreaId();
        }

        public int getFloorId() {
            return IbevacAgent.this.getCurrentFloorId();
        }

        public Vector2d getPreferredVelocity() {
            return IbevacAgent.this.getLevel1MotionPlanning().getPreferredVelocity();
        }

        public Vector2d getVelocity() {
            return IbevacAgent.this.getVelocity();
        }

        public String getCurrentGoal() {
            return IbevacAgent.this.getLevel1MotionPlanning().getCurrentSpatialWaypoint().
                    getPoint().toString();
        }
    }

    /**
     * Internally used by the inspector
     * @return the proxy object which is used for analysing the agent.
     */
    @Override
    public Object propertiesProxy() {
        return new IbevacAgentProxy();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "IbevacAgent [id=" + id + "]";
    }

    ////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////GETTERS AND SETTERS//////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    public AgentType getType() {
        return this.type;
    }

    /**
     * Increases the agent's speed to the maximum possible speed so that it looks 
     * like he's running
     */
    public void setToRunningSpeed() {
        preferredSpeed = preferredSpeed * 1.5;
        if (preferredSpeed > EvacConstants.AGENT_MAX_SPEED) {
            preferredSpeed = EvacConstants.AGENT_MAX_SPEED;
        }
//        preferredSpeed = EvacConstants.AGENT_MAX_SPEED;
    }

    /**
     * Sets the current goal of the agent so that it can be queried by the 
     * inspector
     * 
     * @param point The point coordinates of the current goal. 
     */
    public void setCurrentGoal(Point2d point) {
        planner.setChosenGoal(point);
    }

    /**
     * 
     * @return The unique id of the agent 
     */
    public int getId() {
        return this.id;
    }

    /**
     * 
     * @return the logical coordinates of the agent's location. 
     */
    public Point2d getLogicalPosition() {
        return IbevacSpace.translateToLogicalLocation(this.getPosition(),
                space.getOffset(perception.getCurrentFloorIdx()));
    }

    /**
     * Returns the current set of possible goals from which the agent should 
     * choose one to go to
     * @return Collection of integers which are the areaID's of the goals.
     */
    public Collection<Integer> getCurrentGoalAreaIds() {
        return planner.getCurrentGoalAreaIds();
    }

    /**This function cheks whether the agent has reached near it's chosen goal.
     * 
     * @return true  goal is reached
     *         false goal hasn't been reached
     */
    public boolean isGoalReached() {
        if (planner.getChosenGoal() != null) {
            return getPosition().distance(planner.getChosenGoal()) < 3 * getDiameter();
        } else {
            return true;
        }
    }

    /**
     * 
     * @return  true agent is trapped
     *          false agent is not trapped
     */
    public boolean isTrapped() {
        return planner.getState() instanceof Trapped;
    }

    /**
     * 
     * @return  true agent is dead
     *          false agent is not dead
     */
    public boolean isDead() {
        return planner.getState() instanceof Dead;
    }

    /**
     * 
     * @return  true agent is safe
     *          false agent is not safe
     */
    public boolean isSafe() {
        return planner.getState() instanceof Safe;
    }

    /**
     * A reference to the environment to be used by sub modules
     * @return space reference
     */
    public IbevacSpace getSpace() {
        return space;
    }

    /**
     * This function returns the set of dynamic obstacles (other people) that 
     * are perceived by the agent.
     * 
     * @param <E> PhysicalAgent objects or subclasses of it
     * @return The set of agents that are perceived by this agent.
     */
    @Override
    public <E extends PhysicalAgent> Set<E> getPerceivedDynamicObstacles() {
        return perception.getPerceivedDynamicObstacles();
    }

    /**
     * This function returns the set of static obstacles that are perceived by 
     * the agent.
     * @return Set of static obstacles that are perceived by the agent.
     */
    @Override
    public Set<StaticObstacle> getPerceivedStaticObstacles() {
        return perception.getPerceivedStaticObstacles();
    }

    /**
     * 
     * @return The preferred speed of the agent 
     */
    @Override
    public double getPreferredSpeed() {
        return preferredSpeed;
    }

    /**
     * 
     * @return The ID of the floor the agent is currently in
     */
    public int getCurrentFloorId() {
        return perception.getCurrentFloorIdx();
    }

    /**
     * 
     * @return The id of the area the agent the currently in. 
     */
    public int getCurrentAreaId() {
        return perception.getCurrentAreaId();
    }

    /**
     * This function returns the id of the area in which the agent is originally 
     * created which is defined as his "home".
     * @return 
     */
    public int getHomeId() {
        return homeId;
    }

    /**
     * 
     * @return A reference to the agents environmentKnowledge. 
     */
    public EnvironmentKnowledgeModule getEnvironmentKnowledge() {
        return knowledge.getEnvironmentKnowledge();
    }

    /**
     * 
     * @return A reference to the planning module. 
     */
    public Planner getPlanner() {
        return this.planner;
    }

    /**
     * This returns a reference to the  physical consistency check layer.
     * 
     * @return The level0 motion planning algorithm 
     */
    @Override
    public Level0MotionPlanning getLevel0MotionPlanning() {
        return space.getPhysicalMovementEngine();
    }

    /**
     * This returns a reference to the collision avoidance system used.
     * 
     * @return The level1 motion planning algorithm 
     */
    @Override
    public Level1MotionPlanning getLevel1MotionPlanning() {
        return navigationModule.getLevel1MotionPlanning();
    }

    /**
     * This returns a reference to the spatial waypoint navigator used.
     * 
     * @return The level2 motion planning algorithm 
     */
    @Override
    public Level2MotionPlanning getLevel2MotionPlanning() {
        return navigationModule.getLevel2MotionPlanning();
    }

    /**
     *  This returns a reference to the logical waypoint navigator used.
     * 
     * @return The level3 motion planning algorithm 
     */
    @Override
    public Level3MotionPlanning getLevel3MotionPlanning() {
        return navigationModule.getLevel3MotionPlanning();
    }

    /**
     * Returns the agent's sensor range which is a product of the Sensor Range 
     * set in EvacConstants and the agent's diameter
     * @return the agent's sensor range 
     */
    public double getSensorRange() {
        return sensorRange;
    }

    public int getEvacuationTime() {
        return (int)evacuationTime;
    }

    ////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////Scheduling Functions/////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Schedules the agent and stores the steppable to eventually kill the steppable.
     * 
     * @param state     The IbevacModel with the scheduler
     * @param ordering  The order in terms of other elements in the scheduler
     * @param timestep  The timestep that is used for this steppable.
     */
    public void schedule(SimState state, int ordering, double timestep) {
        // System.out.println("scheduling");
        this.stoppable = state.schedule.scheduleRepeating(this, ordering,
                timestep);
    }

    /**
     * Schedules another steppable to ensure that the agent's positions get 
     * updated on the actual map. 
     * 
     * @param state     The IbevacModel with the scheduler
     * @param ordering  The order in terms of other elements in the scheduler
     * @param timestep  The timestep that is used for this steppable.
     */
    public void scheduleUpdate(SimState state, int ordering, double timestep) {
        // System.out.println("scheduling");
        state.schedule.scheduleRepeating(new Steppable() {

            @Override
            public void step(SimState ss) {
                space.updateAgentPosition(IbevacAgent.this);
            }
        }, ordering, timestep);


    }
}
