/**
 *
 */
package ibevac.environment;

import ibevac.cue.FireAlarmCue;
import ibevac.datatypes.SmallRoom;
import abmcs.agent.LineSegment;
import ibevac.EvacConstants;
import ibevac.agent.IbevacAgent;
import ibevac.datatypes.CArea;
import ibevac.datatypes.CEvacuationScenario;
import ibevac.datatypes.CFloor;
import ibevac.datatypes.CLink;
import ibevac.datatypes.CRoom;
import ibevac.engine.IbevacModel;
import ibevac.utilities.IbevacRNG;

import java.util.Collection;
import java.util.Set;

import javax.vecmath.Point2d;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import abmcs.motionplanning.level0.Level0MotionPlanning;
import abmcs.motionplanning.level0.PhysicalEnvironment;
import abmcs.motionplanning.level0.PhysicalObject;
import abmcs.motionplanning.level0.jbox2d.JBox2DPhysicalObject;
import ibevac.agent.AgentDescriptionModule.AgentType;
import ibevac.cue.Cue;
import ibevac.datatypes.CCrowd;

import java.lang.UnsupportedOperationException;
import java.util.ArrayList;
import java.util.List;

import sim.util.Bag;

/**
 * The main envirnment class in which all the data is stored and accessed through
 * It mostly just delegates a lot of functionality to other classes.
 *
 * @author <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 * @version $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class IbevacSpace {

    private final int numberOfFloors;
    /**
     * A reference to the level0motion planning which acts as a physical sanity
     * check
     */
    private final Level0MotionPlanning physicalMovementEngine;
    /**
     * The actual physical environment in which all the objects are stored and
     * on which the physical movement engie acts
     */
    private final PhysicalEnvironment physicalEnvironment;
    /**
     * The actual real world map in which all the obstacles and agents are stored
     * and which keeps a track of rooms, floors links, staircases, etc.
     */
    private final RealWorldLayout realWorldMap;
    private final CueSpace cueSpace;
    private final FireSpace fireSpace;
    private final SmokeSpace smokeSpace;
    private final IbevacModel model;

    /**
     * Calls all the respective spaces and passes the parameters to it to
     * initialize them in appropriate ways
     *
     * @param environment
     * @param scenario
     * @param model
     */
    public IbevacSpace(PhysicalEnvironment environment,
                       CEvacuationScenario scenario, IbevacModel model) {


        physicalEnvironment = environment;

        // determine the offset for each floor
        realWorldMap = new RealWorldLayout(scenario, 50 * scenario.getScale());


        physicalEnvironment.addStaticObstacle(realWorldMap.getAllObstacleLines());
        numberOfFloors = scenario.getFloors().size();

        physicalMovementEngine = new Level0MotionPlanning(environment);
        // create the fire model
        int fResolution = (int) (3 * scenario.getScale()); // conversion:
        // [px] -> [cm]
        int sResolution = (int) (3 * scenario.getScale()); // conversion:
        // [px] -> [cm]


        cueSpace = new CueSpace(realWorldMap.getAllRooms(),
                50 * scenario.getScale(), scenario.getFloors(), this);
        smokeSpace = new SmokeSpace(scenario, sResolution, this);

        fireSpace = new FireSpace(scenario, fResolution,
                smokeSpace, this);
        this.model = model;
    }

    /**
     * Supposed to initialize agents as said in the scenario file
     *
     * @param scenario
     */
    public Collection<IbevacAgent> initializeAgents(CEvacuationScenario scenario) {
        List<IbevacAgent> allAgents = new ArrayList<IbevacAgent>();
        // CFloor floor = scenario.getFloors().get(0);

        for (int f = 0; f < this.numberOfFloors; ++f) {
            CFloor floor = scenario.getFloors().get(f);
            int offset = realWorldMap.getOffset(f);
            Set<IbevacAgent> agents = this.getAgentsByFloor(f);

            for (CCrowd crowd : floor.getCrowds()) {
                CArea room = realWorldMap.getAreaById(crowd.getRoomId());
                for (int i = 0; i < crowd.getSize(); ++i) {
                    createAgent(room, offset, f, agents, scenario, AgentType.DEFAULT);
                }
            }
            allAgents.addAll(agents);
        }
        return allAgents;
    }

    /**
     * Initializes the agents as one in each room  with agent characteristics
     * as specified in the scenario file.
     *
     * @param scenario
     */
    public Collection<IbevacAgent> initializeAgentsDefault(CEvacuationScenario scenario) {
        List<IbevacAgent> allAgents = new ArrayList<IbevacAgent>();
        // CFloor floor = scenario.getFloors().get(0);

        for (int f = 1; f < this.numberOfFloors; ++f) {
            CFloor floor = scenario.getFloors().get(f);
            int offset = realWorldMap.getOffset(f);
            Set<IbevacAgent> agents = this.getAgentsByFloor(f);

            // for(CCrowd crowd : floor.getCrowds()) {
            // CArea area = areaByIdMapping.get(crowd.getRoomId());
            // for(int i=0; i<crowd.getDiameter(); ++i) {
            // createAgent(area, offset, f, agents, environment, level0);
            // }
            // }

            int count = 0;
            // randomly place 2 agents in every room
            for (CRoom room : floor.getRooms()) {
                count++;
                if (count < 20) {
                    continue;
                }
                createAgent(room, offset, f, agents, scenario, AgentType.DEFAULT);
//                createAgent(room, offset, f, agents, scenario);
//                createAgent(room, offset, f, agents, scenario);
//                createAgent(room, offset, f, agents, scenario);
                if (count == 20) {
                    break;
                }
            }
            allAgents.addAll(agents);
        }
        return allAgents;
    }

    //TODO : Not accurate for odd number of agents

    /**
     * Initializes agents in random locations on the map.
     *
     * @param scenario
     */
    public Collection<IbevacAgent> initializeAgentsRandomly(CEvacuationScenario scenario, int numberOfAgents) {
        List<IbevacAgent> allAgents = new ArrayList<IbevacAgent>();
        // CFloor floor = scenario.getFloors().get(0);

        for (int i = 0; i < numberOfAgents; i++) {
            int f = IbevacRNG.instance().nextInt(numberOfFloors);
            CFloor floor = scenario.getFloors().get(f);
            int offset = realWorldMap.getOffset(f);
            Set<IbevacAgent> agents = this.getAgentsByFloor(f);


            // randomly place 2 agents in every room
            CArea room = floor.getRooms().get(IbevacRNG.instance().nextInt(floor.getRooms().size()));
            allAgents.add(createAgent(room, offset, f, agents, scenario, AgentType.DEFAULT));

//          
        }

        return allAgents;
    }

    //TODO : Not accurate for odd number of agents
    public Collection<IbevacAgent> initializeManagementStaffRandomly(
            CEvacuationScenario scenario, int numberOfManagers) {
        List<IbevacAgent> allAgents = new ArrayList<IbevacAgent>();
        // CFloor floor = scenario.getFloors().get(0);

        for (int i = 0; i < numberOfManagers; i++) {
            int f = IbevacRNG.instance().nextInt(numberOfFloors);
            CFloor floor = scenario.getFloors().get(f);
            int offset = realWorldMap.getOffset(f);
            Set<IbevacAgent> agents = this.getAgentsByFloor(f);


            // randomly place 2 agents in every room
            CArea room = floor.getRooms().get(IbevacRNG.instance().nextInt(floor.getRooms().size()));
            allAgents.add(createAgent(room, offset, f, agents, scenario, AgentType.DEFAULT));

//          
        }
        return allAgents;
    }

    /**
     * Creates an individual agent at a particular location. Uses the parameters
     * in EvacConstant.
     *
     * @param area
     * @param offset
     * @param floorNumber
     * @param agents
     * @param scenario    (useful for creating the ibevac agent appropriately with
     *                    the correct amount of knowledge.
     */
    private IbevacAgent createAgent(CArea area, int offset, int floorNumber,
                                    Set<IbevacAgent> agents, CEvacuationScenario scenario, AgentType type) {
        // determine the mass of the agent;
        IbevacRNG random = IbevacRNG.instance();

        double mass = random.nextGaussian() * EvacConstants.AGENT_STDDEV_MASS
                + EvacConstants.AGENT_AVG_MASS;
        if (mass < EvacConstants.AGENT_MIN_MASS) {
            mass = EvacConstants.AGENT_MIN_MASS;
        } else if (mass > EvacConstants.AGENT_MAX_MASS) {
            mass = EvacConstants.AGENT_MAX_MASS;
        }

        // assume linear relationship between size and mass where 50kg == 40cm
        // and 120kg == 80cm
        double size = EvacConstants.AGENT_MIN_DIAMETER
                + (EvacConstants.AGENT_MAX_DIAMETER - EvacConstants.AGENT_MIN_DIAMETER)
                * ((mass - EvacConstants.AGENT_MIN_MASS) / (EvacConstants.AGENT_MAX_MASS - EvacConstants.AGENT_MIN_MASS));

        int mnx = Math.min(area.getCorner0().getX(), area.getCorner1().getX())
                + (int) (size / 2);
        int mny = Math.min(area.getCorner0().getY(), area.getCorner1().getY())
                + (int) (size / 2);
        int mxx = Math.max(area.getCorner0().getX(), area.getCorner1().getX())
                - (int) (size / 2);
        int mxy = Math.max(area.getCorner0().getY(), area.getCorner1().getY())
                - (int) (size / 2);

        Point2d pos = null;
        while (pos == null) {
            int x = mnx + random.nextInt(mxx - mnx) + offset;
            int y = mny + random.nextInt(mxy - mny);
            pos = new Point2d(x, y);

            // make sure agents are not created on top of each other
            for (IbevacAgent agent : agents) {
                double dx = x - agent.getPosition().getX();
                double dy = y - agent.getPosition().getY();
                double d = Math.hypot(dx, dy);

                double minDist = (agent.getDiameter() + size) / 2.0;

                // if d<=minDist then the agents are 'overlapping'...
                if (d <= minDist) {
                    pos = null;
                    break;
                }
            }
        }

        // create the agent and schedule it
        PhysicalObject physicalObject = physicalEnvironment.createPhysicalObject(size, mass, pos.x, pos.y);
        IbevacAgent agent = new IbevacAgent(physicalObject,
                physicalMovementEngine, scenario, this, floorNumber, area.getId(), type);
//System.out.println("creating agent with id"+ agent.getId() + "in area "+area.getId());
        // agent.schedule(this, 1000);

        this.addAgent(agent);
        return agent;
    }

    /**
     * Adds the agent to the appropriate sub spaces
     *
     * @param agent
     */
    public void addAgent(IbevacAgent agent) {
        physicalEnvironment.addAgent(agent);
        realWorldMap.addAgent(agent);
    }

    /**
     * Removes the agent from the appropriate sub spaces
     *
     * @param agent
     */
    public void removeAgent(IbevacAgent agent) {

        realWorldMap.removeAgent(agent);
        physicalEnvironment.destroyPhysicalObject(agent.getPhysicalObject());

    }

    /*
     * Returns the x-offset of a particular floor in [cm] OUT: offset [cm]
     */
    public int getOffset(int floorIdx) {
        return realWorldMap.getOffset(floorIdx);
    }

    public Set<IbevacAgent> getAgentsByFloor(int floorIdx) {
        return realWorldMap.getAgentsByFloor(floorIdx);
    }

    public Set<IbevacAgent> getAgentsByAreaId(int areaId) {
        return realWorldMap.getAgentsByAreaId(areaId);
    }

    public int getFloorByAreaId(int areaId) {
        return realWorldMap.getFloorByAreaId(areaId);
    }

    public CArea getAreaById(int areaId) {
        return realWorldMap.getAreaById(areaId);
    }

    public Set<LineSegment> getObstacleLinesByArea(int areaId) {
        return this.realWorldMap.getObstacleLinesByArea(areaId);
    }

    public Set<CLink> getLinksForRoom(int roomId) {
        return this.realWorldMap.getLinksForRoom(roomId);
    }

    public boolean isAreaARoom(int areaId) {
        return realWorldMap.isAreaARoom(areaId);
    }

    public boolean isRoomAStaircase(int roomId) {
        return realWorldMap.isRoomAStaircase(roomId);
    }

    public int getWidth(int floorLevel) {
        return realWorldMap.getWidth(floorLevel);
    }

    public int getHeight(int floorLevel) {
        return realWorldMap.getHeight(floorLevel);
    }

    public int getAreaOfPoint(int x, int y, int floorIndex) {
//        return cueSpace.getArea(x, y, floorIndex, 35);
        return realWorldMap.findAreaOfPoint(x, y, floorIndex);
    }

    public boolean isAreaLethal(int currentFloorIndex, int i, int j) {

        return fireSpace.isAreaLethal(currentFloorIndex, i, j);
    }

    public boolean exitReached(int i, int j) {
        return realWorldMap.exitReached(i, j);
    }

    public Continuous2D getObstacleSpace(int floor) {
        return realWorldMap.getObstacleSpace(floor);
    }

    public Continuous2D getAgentSpace(int floor) {
        return realWorldMap.getAgentSpace(floor);
    }

    public int getNumberOfFloors() {
        return numberOfFloors;
    }

    public PhysicalEnvironment getPhysicalEnvironment() {
        return physicalEnvironment;
    }

    public Set<IbevacAgent> getAllAgents() {
        return realWorldMap.getAllAgents();
    }

    public FireSpace getFireModel() {

        return this.fireSpace;
    }

    public SmokeSpace getSmokeModel() {

        return this.smokeSpace;
    }

    public void moveAgentFloor(IbevacAgent ibevacAgent, int fcurrent, int fnext) {
        this.realWorldMap.moveAgentFloor(ibevacAgent, fcurrent, fnext);
    }

    public void moveAgentArea(IbevacAgent agent, int fromAreaId, int toAreaId) {
        realWorldMap.moveAgentArea(agent, fromAreaId, toAreaId);
    }

    public void updateAgentPosition(IbevacAgent ibevacAgent) {
        realWorldMap.updateAgentPosition(ibevacAgent);
    }

    public Level0MotionPlanning getPhysicalMovementEngine() {
        return this.physicalMovementEngine;

    }

    public String getImage(int floorNumber) {
        return this.realWorldMap.getImage(floorNumber);
    }

    public Collection<? extends IbevacAgent> getAgentsInRadius(IbevacAgent me,
                                                               double radius) {
        return realWorldMap.getAgentsInRadius(me, radius);
    }

    public Bag getObstaclesInRadius(
            IbevacAgent me, double radius) {
        return realWorldMap.getObstaclesInRadius(me, radius);
    }

    public <T extends Cue> void putCueInArea(T cue, int id) {
        cueSpace.putCueInArea(cue, realWorldMap.getAreaById(id));
    }

    public Bag getCuesInRadius(IbevacAgent me, double sensorRange) {
        return cueSpace.getCuesInRadius(me, sensorRange);
    }

    public Set<LineSegment> getAllObstacleLines() {
        return this.realWorldMap.getAllObstacleLines();
    }

    //    public Collection<? extends Cue> getCuesInVicinity(IbevacAgent me, CArea area) {
//        Set<Cue> perceivedCues = new HashSet<>();
//        perceivedCues.addAll(cueSpace.getCuesForAreaId(area.getId()));
//        if (area instanceof CLink) {
//            // if the area is a link, also consider the connecting room areas
//            CLink link = (CLink) area;
//            for (int connectingAreaId : link.getConnectingAreas()) {
//
//                perceivedCues.addAll(cueSpace.getCuesForAreaId(connectingAreaId));
//            }
//        } else {
//            for (CLink link : getLinksForRoom(area.getId())) {
//                perceivedCues.addAll(cueSpace.getCuesForAreaId(link.getId()));
//
//                for (int connectingAreaId : link.getConnectingAreas()) {
//                    if (connectingAreaId == area.getId()) {
//                        continue;
//                    }
//                    perceivedCues.addAll(cueSpace.getCuesForAreaId(connectingAreaId));
//                }
//            }
//        }
//        return perceivedCues;
//    }
    public <T extends Cue> void putCueInSmallRoom(T cue, SmallRoom smallRoom) {
        cueSpace.putCueInSmallRoom(cue, smallRoom);
    }

    public SmallRoom getSmallRoomOfPoint(int x, int y, int floorNumber) {
        return cueSpace.determineSmallRoomOfLocation(x, y, this.getAreaOfPoint(x, y, floorNumber));
    }

    public <T extends Cue> void removeCueFromSmallRoom(T cue, SmallRoom room) {
        cueSpace.removeCueFromSmallRoom(cue, room);
    }

    public Collection<CArea> getAllRooms() {
        return this.realWorldMap.getAllRooms();
    }

    void removeCueFromRoom(FireAlarmCue cue, int areaId) {
        this.cueSpace.removeCueFromRoom(cue, areaId);
    }

    public Collection<LineSegment> getObstacleLinesByFloor(int floorNumber) {
        return realWorldMap.getObstacleLinesByFloor(floorNumber);
    }

    public float getSmokeAtPoint(int x, int y, int floorNumber) {
        SmallRoom room = this.getSmallRoomOfPoint(x, y, floorNumber);

        return this.smokeSpace.getSmokeInRoom(room);
    }

    /**
     * Returns the physical location of a given logical location. The discrepancy
     * is because the JBox2D environment cannot store 3D environments.
     *
     * @param p
     * @param offset
     * @return
     */
    public static Point2d translateToPhysicalLocation(Point2d p, int offset) {
        return JBox2DPhysicalObject.TranslateToPhysicalLocation(p, offset);
    }

    /**
     * Returns the logical location of a given physical location. The discrepancy
     * is because the JBox2D environment cannot store 3D environments.
     *
     * @param pos
     * @param offset
     * @return
     */
    public static Point2d translateToLogicalLocation(Point2d pos, int offset) {
        return JBox2DPhysicalObject.TranslateToLogicalLocation(pos, offset);
    }

    public void scheduleFireSpace(IbevacModel ibevacModel, int ordering,
                                  double interval) {
        System.out.println("Fire Scheduled");
        fireSpace.schedule(ibevacModel, ordering, interval); // every 1 seconds

    }

    public void scheduleSmokeSpace(IbevacModel ibevacModel, int ordering,
                                   double interval) {
        System.out.println("Smoke Scheduled");
        smokeSpace.schedule(ibevacModel, ordering, interval); // every 1 seconds

    }

    public void schedulePhysicalEngine(SimState state, int ordering) {
        double timestep = physicalMovementEngine.getTimestep(); // [s]
        // ->
        // [msec]
        physicalMovementEngine.stoppable = state.schedule.scheduleRepeating(0.0, ordering, physicalMovementEngine, timestep);

    }

    public boolean areAllExitsBurning() {
        return realWorldMap.areAllExitsBurning(this.fireSpace, this.smokeSpace);
    }

    public SimState getModel() {
        return this.model;
    }

    public boolean areStairCasesBurning() {

        return realWorldMap.areStairCasesBurning(this.fireSpace, this.smokeSpace);
    }
}
