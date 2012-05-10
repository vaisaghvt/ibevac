package ibevac.environment;

import com.google.common.collect.HashMultimap;
import ibevac.EvacConstants;
import ibevac.agent.IbevacAgent;
import ibevac.datatracker.PEDDataTracker;
import ibevac.datatypes.CArea;
import ibevac.datatypes.SmallRoom;
import ibevac.engine.IbevacModel;
import ibevac.utilities.IbevacRNG;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Point2d;

import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

/**
 * This class is responsible for handling and storing agents.
 * 
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class AgentSpace {

    /**
     * The continuous2D field with two purposes. It can be used to check for 
     * nearby agents. More importantly it is used by portrayals to draw agents at 
     * the appropriate locations
     */
    private ArrayList<Continuous2D> agentSpaces;
    /**
     * The grid size in continuous2D field. The right choice of this value ensures
     * that sensing of neighbours is done more efficiently and faster.
     */
    private double gridSize;
    /**
     * As the name suggests it gives the set of all agents in each area ID
     */
    private Map<Integer, Set<IbevacAgent>> agentsByAreaId;
    /**
     * The set of all agents in each floor
     */
    private Map<Integer, Set<IbevacAgent>> agentsByFloor;
    /**
     * A reference to the RealWorldLayout
     */
    private final RealWorldLayout realWorld;
    /**
     * Divides each larger room to smaller Rooms.  And gives the set of agents
     * in each small room. Helps in more efficient neighbour finding behavior
     */
    private HashMultimap<SmallRoom, IbevacAgent> agentsForSmallRoom;
    /**
     * A mapping of the small rooms for each area.
     */
    private HashMultimap<Integer, SmallRoom> smallRoomsForArea;
    /**
     * The fields in which the small ROoms are stored so that neighbour checks
     * can be easily done to find nearby rooms.
     */
    private ArrayList<Continuous2D> smallRoomSpaces;
    /**
     * A parameter that determines the size of hte samll room. Which in turn 
     * determines what size of rooms are broken down into smaller rooms.
     */
    private final double roomSize;

    public AgentSpace(RealWorldLayout realWorld, int numberOfFloors, double d) {
        agentSpaces = new ArrayList<Continuous2D>();
        agentsByAreaId = new HashMap<Integer, Set<IbevacAgent>>();
        agentsByFloor = new HashMap<Integer, Set<IbevacAgent>>();

        this.realWorld = realWorld;
        gridSize = EvacConstants.MAX_SENSOR_RANGE;

        agentsForSmallRoom = HashMultimap.create();
        smallRoomsForArea = HashMultimap.create();


        smallRoomSpaces = new ArrayList<Continuous2D>();

        roomSize = d;
    }

    /**
     * Initializes the agent and the small room spaces.
     * @param width
     * @param height 
     */
    public void addSpace(int width, int height) {
        agentSpaces.add(new Continuous2D(
                width / IbevacModel.scale,
                height / IbevacModel.scale,
                gridSize / IbevacModel.scale));
        smallRoomSpaces.add(new Continuous2D(
                width / IbevacModel.scale,
                height / IbevacModel.scale,
                gridSize / IbevacModel.scale));


        for (CArea area : realWorld.getAllRooms()) {
//            assert area.getCorner0().getX() < area.getCorner1().getX();
//            assert area.getCorner1().getY() < area.getCorner1().getY();

            int mnx = Math.min(area.getCorner0().getX(), area.getCorner1().getX());
            int mny = Math.min(area.getCorner0().getY(), area.getCorner1().getY());

            int roomWidth = Math.abs(area.getCorner1().getX() - area.getCorner0().getX());
            int roomLength = Math.abs(area.getCorner1().getY() - area.getCorner0().getY());
            if (roomLength <= roomSize && roomWidth <= roomSize) {
                //Room size is within limits
                double locationX = mnx + ((double) roomWidth / 2.0);
                double locationY = mny + ((double) roomLength / 2.0);
                SmallRoom tempRoom = new SmallRoom(roomLength, roomWidth, 
                        new Point2d(locationX, locationY),
                        area,realWorld.getFloorByAreaId(area.getId()));
                this.smallRoomsForArea.put(area.getId(), tempRoom);
                smallRoomSpaces.get(realWorld.getFloorByAreaId(area.getId())).
                        setObjectLocation(tempRoom,
                        new Double2D(locationX / IbevacModel.scale,
                        locationY / IbevacModel.scale));
            } else {
                int idealNumberOfRoomsOnLength = 1;
                int idealNumberOfRoomsOnWidth = 1;
                double idealLength = roomLength;
                double idealWidth = roomWidth;
                while (idealWidth >= roomSize) {


                    idealWidth = (double) roomWidth / (++idealNumberOfRoomsOnWidth);
                }
                while (idealLength >= roomSize) {
                    idealLength = (double) roomLength / (++idealNumberOfRoomsOnLength);
                }
//                System.out.println(area.getId() + ":" + idealNumberOfRoomsOnLength + "," + idealNumberOfRoomsOnWidth);
                for (int i = 0; i < idealNumberOfRoomsOnWidth; i++) {
                    for (int j = 0; j < idealNumberOfRoomsOnLength; j++) {
                        double locationX = mnx + i * idealWidth + ((double) idealWidth / 2.0);
                        double locationY = mny + j * idealLength + ((double) idealLength / 2.0);
                        SmallRoom tempRoom = new SmallRoom(idealLength, idealWidth, 
                                new Point2d(locationX, locationY), 
                                area,realWorld.getFloorByAreaId(area.getId()) );
                        this.smallRoomsForArea.put(area.getId(), tempRoom);
                        smallRoomSpaces.get(realWorld.getFloorByAreaId(area.getId())).
                                setObjectLocation(tempRoom,
                                new Double2D(locationX / IbevacModel.scale,
                                locationY / IbevacModel.scale));
                    }
                }
            }

//            int mxx = Math.max(area.getCorner0().getX(), area.getCorner1().getX());
//            int mxy = Math.max(area.getCorner0().getY(), area.getCorner1().getY());
//           System.out.println(area.getId());
//            System.out.println(mnx + "," + mny);
//            System.out.println(mxx + "," + mxy);
//            for (SmallRoom room : this.smallRoomsForArea.get(area.getId())) {
//                System.out.println(room);
//            }
        }


    }

    /**
     * Adds a new agent to the environment
     * @param agent 
     */
    public void addNewAgent(IbevacAgent agent) {

        CArea area = this.realWorld.getAreaById(agent.getCurrentAreaId());
        Set<IbevacAgent> agents = this.agentsByAreaId.get(area.getId());
        if (agents == null) {
            agents = new HashSet<IbevacAgent>();

            this.agentsByAreaId.put(area.getId(), agents);
        }

        this.agentsByAreaId.get(area.getId()).add(agent);

        // System.out.println("adding to"+
        // agent.getCurrentFloorIdx()+"floor"+"agent"+ agent.getId());
        this.agentsByFloor.get(agent.getCurrentFloorId()).add(agent);

        this.agentsForSmallRoom.put(this.determineSmallRoomOfLocation(
                (int) agent.getPosition().x,
                (int) agent.getPosition().y,
                area.getId()),
                agent);
        // System.out.println("agent"+ agent.getId());

        agentSpaces.get(agent.getCurrentFloorId()).setObjectLocation(
                agent,
                new Double2D(agent.getLogicalPosition().getX() / IbevacModel.scale, agent.getLogicalPosition().getY() / IbevacModel.scale));

    }

    public void addEmptyAgentListForAreaId(int areaId) {
        agentsByAreaId.put(areaId, new HashSet<IbevacAgent>());
    }

    public void addEmptyAgentListForFloorId(int floor) {
        agentsByFloor.put(floor, new HashSet<IbevacAgent>());
    }

    /**
     * Determine a random valid location for an agent within the bounds passed to
     * the function.
     * @param mnx
     * @param mny
     * @param mxx
     * @param mxy
     * @param size
     * @param floorIndex
     * @return 
     */
    public Point2d findValidPointForAgent(int mnx, int mny, int mxx, int mxy,
            double size, int floorIndex) {

        Point2d pos = null;
        IbevacRNG random = IbevacRNG.instance();
        while (pos == null) {
            int x = mnx + random.nextInt(mxx - mnx);
            int y = mny + random.nextInt(mxy - mny);
            pos = new Point2d(x, y);

            // make sure agents are not created on top of each other
            int currentAreaId = realWorld.findAreaOfPoint(x, y, floorIndex);
            for (IbevacAgent agent : agentsByAreaId.get(currentAreaId)) {
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
        return pos;
    }

    /**
     * Updates the agents logical location by updating the agents floor.
     * @param agent
     * @param fcurrent
     * @param fnext 
     */
    public void moveAgentFloor(IbevacAgent agent, int fcurrent, int fnext) {
        Set<IbevacAgent> fromAgents = agentsByFloor.get(fcurrent);
        Set<IbevacAgent> toAgents = agentsByFloor.get(fnext);

        fromAgents.remove(agent);
        toAgents.add(agent);
        agentSpaces.get(fcurrent).remove(agent);

        agentSpaces.get(fnext).setObjectLocation(
                agent,
                new Double2D(agent.getLogicalPosition().getX() / IbevacModel.scale, agent.getLogicalPosition().getY() / IbevacModel.scale));

    }

    /**
     * Updates the agents logical location by updating the agents area.
     * @param agent
     * @param fcurrent
     * @param fnext 
     */
    public void moveAgentArea(IbevacAgent agent, int fromAreaId, int toAreaId) {

        for (SmallRoom sr : this.smallRoomsForArea.get(fromAreaId)) {
            if (agentsForSmallRoom.remove(sr, agent)) {
                break;
            }
        }


        Set<IbevacAgent> fromAgents = agentsByAreaId.get(fromAreaId);
        Set<IbevacAgent> toAgents = agentsByAreaId.get(toAreaId);

        assert fromAgents.contains(agent);

        this.agentsForSmallRoom.put(this.determineSmallRoomOfLocation(
                (int) agent.getPosition().x,
                (int) agent.getPosition().y,
                toAreaId),
                agent);
        fromAgents.remove(agent);
        toAgents.add(agent);
        
//        PEDDataTracker.instance.addAgentNewAreaId(agent.getId(), toAreaId);

    }

    /**
     * Removes an agent from the environment
     * @param agent 
     */
    public void removeAgent(IbevacAgent agent) {
        int floorIdx = agent.getCurrentFloorId();
        Set<IbevacAgent> fAgents = agentsByFloor.get(floorIdx);

        int areaId = agent.getCurrentAreaId();
        Set<IbevacAgent> aAgents = agentsByAreaId.get(areaId);

        for (SmallRoom sr : this.smallRoomsForArea.get(agent.getCurrentAreaId())) {
            if (agentsForSmallRoom.remove(sr, agent)) {
                break;
            }
        }

        assert (fAgents.contains(agent) && aAgents.contains(agent));
        fAgents.remove(agent);
        aAgents.remove(agent);


//        int size = agentSpaces.get(floorIdx).getAllObjects().size();

        Object result = this.agentSpaces.get(floorIdx).remove(agent);

        assert result != null;
    }

    /**
     * Updates an agent's physical location on the map in the two fields.
     * For portrayal and for perception
     * @param agent 
     */
    public void updateAgentPosition(IbevacAgent agent) {
        if (!agent.isSafe()) {
            this.agentsForSmallRoom.put(this.determineSmallRoomOfLocation(
                    (int) agent.getPosition().x,
                    (int) agent.getPosition().y,
                    agent.getCurrentAreaId()),
                    agent);

            this.agentSpaces.get(agent.getCurrentFloorId()).setObjectLocation(
                    agent,
                    new Double2D(agent.getLogicalPosition().getX() / IbevacModel.scale, agent.getLogicalPosition().getY() / IbevacModel.scale));
        }
    }

    /**
     * Determines the small room associated with a particular xy location.
     * @param x
     * @param y
     * @param areaId
     * @return 
     */
    SmallRoom determineSmallRoomOfLocation(int x, int y, int areaId) {

        for (SmallRoom room : this.smallRoomsForArea.get(areaId)) {
//            System.out.println(k++);
            if (room.contains(new Point2d(x, y))) {
                return room;

            }
        }
        return null;
    }

//    @SuppressWarnings("unchecked")
//    public Bag getAgentsInRadius(IbevacAgent me,
//            double radius) {
//
//        return agentSpaces.get(me.getCurrentFloorId()).getObjectsExactlyWithinDistance(
//                new Double2D(me.getLogicalPosition().getX() / IbevacModel.scale,
//                me.getLogicalPosition().getY() / IbevacModel.scale),
//                radius);
//    }
    /**
     * Returns the agents within a particular radius of a specified location. For
     * efficiency 's sake this is done through the small rooms.
     * @param me
     * @param radius
     * @return 
     */
    public Collection<IbevacAgent> getAgentsInRadius(IbevacAgent me, double radius) {

        Bag smallRooms = this.smallRoomSpaces.get(me.getCurrentFloorId()).getObjectsExactlyWithinDistance(
                new Double2D(
                me.getLogicalPosition().getX() / IbevacModel.scale,
                me.getLogicalPosition().getY() / IbevacModel.scale),
                radius);
        HashSet<IbevacAgent> agentsToReturn = new HashSet<IbevacAgent>();
        for (Object object : smallRooms) {
            SmallRoom tempRoom = (SmallRoom) object;
            agentsToReturn.addAll(this.agentsForSmallRoom.get(tempRoom));
        }
        return agentsToReturn;
    }

    /**
     * Set of agents ocuppying a particular area/room
     * @param areaId
     * @return 
     */
    public Set<IbevacAgent> getAgentsByAreaId(int areaId) {
        return agentsByAreaId.get(areaId);
    }

    /**
     * Get's the agent field for a particular floor
     * @param floor
     * @return 
     */
    public Continuous2D getAgentField(int floor) {
        return agentSpaces.get(floor);
    }

    /**
     * Gets all the agents in a particular floor.
     * @param floor
     * @return 
     */
    public Set<IbevacAgent> getAgentsByFloor(int floor) {
        return agentsByFloor.get(floor);
    }

    /**
     * Returns a set of all agents
     * @return 
     */
    public Set<IbevacAgent> getAllAgents() {
        HashSet<IbevacAgent> allAgents = new HashSet<IbevacAgent>();
        for (Set<IbevacAgent> agents : this.agentsByFloor.values()) {
            allAgents.addAll(agents);
        }
        return allAgents;
    }
}
