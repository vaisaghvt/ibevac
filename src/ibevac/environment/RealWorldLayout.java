package ibevac.environment;

import abmcs.agent.LineSegment;
import ibevac.agent.IbevacAgent;
import ibevac.datatypes.CArea;
import ibevac.datatypes.CEvacuationScenario;
import ibevac.datatypes.CExit;
import ibevac.datatypes.CFloor;
import ibevac.datatypes.CLink;
import ibevac.datatypes.CRoom;
import ibevac.datatypes.CStaircase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Point2d;

import sim.field.continuous.Continuous2D;
import abmcs.agent.StaticObstacle;
import sim.util.Bag;

/**
 * This class stores the majority of  the environment information available from
 * the scenario file like the image of the environment, rooms, the floors the staircases, the links
 * and the connections and the mappings. It also stored agent and obstacle information by delegating
 * these functions to the Agent and Obstacle Space.
 *
 * @author vaisagh
 */
public class RealWorldLayout {

    private final ObstacleSpace obstacleSpace;
    private final AgentSpace agentSpace;
    /**
     * The offsets for each floor. The physical environment is a 2D map even if
     * there are multiple floors. Thisis handlaed by storing the offset for each
     * floor. i.e. the first floor is in physical terms stored to the right of
     * the 0th floor.
     */
    private final List<Integer> offsets = new ArrayList<Integer>();
    /**
     * The location of the file storing the image of the environment.
     */
    private final List<String> images = new ArrayList<String>();
    private final ArrayList<Integer> floorXSize = new ArrayList<Integer>();
    private final ArrayList<Integer> floorYSize = new ArrayList<Integer>();
    private final ArrayList<CArea> exits = new ArrayList<CArea>();
    /**
     * The CArea associated with each Area id.
     */
    private final Map<Integer, CArea> areaByIdMapping = new HashMap<Integer, CArea>();
    /**
     * Returns the floor for a particular area.
     */
    private final Map<Integer, Integer> floorByAreaIdMapping = new HashMap<Integer, Integer>();
    /**
     * Returns the staircase for a particulaar staircase ID
     */
    private final Map<Integer, CStaircase> staircases = new HashMap<Integer, CStaircase>();
    /**
     * Returns the set of links connected to each room or area ID
     */
    private final Map<Integer, Set<CLink>> linksByRoomIdMapping = new HashMap<Integer, Set<CLink>>();

    public RealWorldLayout(CEvacuationScenario scenario, double d) {

        obstacleSpace = new ObstacleSpace(this, scenario.getFloors().size());
        agentSpace = new AgentSpace(this, scenario.getFloors().size(), d);

        int offset = 0;
        for (int floorNumber = 0; floorNumber < scenario.getFloors().size(); ++floorNumber) {

            CFloor floor = scenario.getFloors().get(floorNumber);

            floorXSize.add(floor.getWidth());
            floorYSize.add(floor.getHeight());
            obstacleSpace.addSpace(floor.getWidth(), floor.getHeight());
            agentSpace.addSpace(floor.getWidth(), floor.getHeight());

            offsets.add(offset);
            images.add(floor.getImage());

            agentSpace.addEmptyAgentListForFloorId(floorNumber);

            // add mappings for easy access

            // add floor information to each link in foor by areaId mapping
            // add links to area list to get areas by id
            // add the link to the links list of each connecting room.

            for (CLink area : floor.getLinks()) {
                floorByAreaIdMapping.put(area.getId(), floorNumber);
                areaByIdMapping.put(area.getId(), area);

                for (int roomId : area.getConnectingAreas()) {
                    Set<CLink> links = linksByRoomIdMapping.get(roomId);
                    if (links == null) {
                        links = new HashSet<CLink>();
                        linksByRoomIdMapping.put(roomId, links);
                    }
                    links.add(area);
                }
            }

            // Treat exits the same way as links they're practically the same.
            for (CExit area : floor.getExits()) {
                floorByAreaIdMapping.put(area.getId(), floorNumber);
                areaByIdMapping.put(area.getId(), area);
                exits.add(area);

                for (int roomId : area.getConnectingAreas()) {
                    Set<CLink> links = linksByRoomIdMapping.get(roomId);
                    if (links == null) {
                        links = new HashSet<CLink>();
                        linksByRoomIdMapping.put(roomId, links);
                    }
                    links.add(area);
                }
            }

            // add floor information for each room
            // add information so that room can be obtained by ID
            for (CRoom area : floor.getRooms()) {
                floorByAreaIdMapping.put(area.getId(), floorNumber);
                areaByIdMapping.put(area.getId(), area);
            }

            // add floor information for each staircase
            // add areaID for each staircase
            // There 's also a seperate staircase list in which this had to be
            // stored...
            for (CStaircase staircase : floor.getStaircases()) {
                floorByAreaIdMapping.put(staircase.getId(), floorNumber);
                areaByIdMapping.put(staircase.getId(), staircase);

                staircases.put(staircase.getId(), staircase);
            }

            // create some empty sets
            for (int areaId : floorByAreaIdMapping.keySet()) {
                agentSpace.addEmptyAgentListForAreaId(areaId);
            }

            // create obstacle lines
            createObstacleLines(floor, offset);

            // update the offset for the next floor
            offset += floor.getWidth();
        }
    }

    /**
     * Create obstacle lines for a given floor
     */
    private void createObstacleLines(CFloor floor, int offset) {
        HashMap<Integer, CArea> areaMapping = new HashMap<Integer, CArea>();
        HashMap<CArea, HashSet<CLink>> areaLinksMapping = new HashMap<CArea, HashSet<CLink>>();
        HashMap<CLink, HashSet<CArea>> linkAreasMapping = new HashMap<CLink, HashSet<CArea>>();

        // put rooms in area to ID mapping
        // also create empty links list for each room
        for (CArea area : floor.getRooms()) {
            areaMapping.put(area.getId(), area);
            areaLinksMapping.put(area, new HashSet<CLink>());
        }

        // put staircases also in the area to ID mapping
        // create similar empty links for staircase areas
        for (CArea area : floor.getStaircases()) {
            areaMapping.put(area.getId(), area);
            areaLinksMapping.put(area, new HashSet<CLink>());
        }

        // the links are also areas themselves so they are also put in the local
        // mapping
        // Add the areas of a link to the linkAreas mapping
        // Add links to areas in the Area links mapping
        for (CLink link : floor.getLinks()) {
            areaMapping.put(link.getId(), link);

            for (int caid : link.getConnectingAreas()) {
                CArea area = areaMapping.get(caid);
                HashSet<CArea> areas = linkAreasMapping.get(link);
                if (areas == null) {
                    areas = new HashSet<CArea>();
                    linkAreasMapping.put(link, areas);
                }
                areas.add(area);

                areaLinksMapping.get(area).add(link);
            }
        }

        // Exits are also links but with one connecting area only
        // Even if only one connecting area, the same method as the other is
        // used...
        for (CLink link : floor.getExits()) {
            areaMapping.put(link.getId(), link);

            for (int caid : link.getConnectingAreas()) {
                CArea area = areaMapping.get(caid);
                HashSet<CArea> areas = linkAreasMapping.get(link);
                if (areas == null) {
                    areas = new HashSet<CArea>();
                    linkAreasMapping.put(link, areas);
                }
                areas.add(area);

                areaLinksMapping.get(area).add(link);
            }
        }

        HashMap<CLink, LinkedList<int[]>> hIntersectionPoints = new HashMap<CLink, LinkedList<int[]>>();
        HashMap<CLink, LinkedList<int[]>> vIntersectionPoints = new HashMap<CLink, LinkedList<int[]>>();

        for (CArea area : areaLinksMapping.keySet()) {
            HashSet<CLink> links = areaLinksMapping.get(area);

            int mnx = Math.min(area.getCorner0().getX(), area.getCorner1().getX());
            int mny = Math.min(area.getCorner0().getY(), area.getCorner1().getY());
            int mxx = Math.max(area.getCorner0().getX(), area.getCorner1().getX());
            int mxy = Math.max(area.getCorner0().getY(), area.getCorner1().getY());

            Set<CLink> left = new HashSet<CLink>();
            Set<CLink> right = new HashSet<CLink>();
            Set<CLink> top = new HashSet<CLink>();
            Set<CLink> bottom = new HashSet<CLink>();

            for (CLink link : links) {
                if (isOverlappingWithVLine(mnx, link)) {
                    left.add(link);
                } else if (isOverlappingWithVLine(mxx, link)) {
                    right.add(link);
                } else if (isOverlappingWithHLine(mny, link)) {
                    top.add(link);
                } else if (isOverlappingWithHLine(mxy, link)) {
                    bottom.add(link);
                } else {
                    try {
                        throw new Exception(
                                "ERROR: link is not overlapping with any border of the room!");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            this.addHObstacleLine(area, mny, mnx, mxx, offset, top,
                    vIntersectionPoints);
            this.addHObstacleLine(area, mxy, mnx, mxx, offset, bottom,
                    vIntersectionPoints);
            this.addVObstacleLine(area, mnx, mny, mxy, offset, left,
                    hIntersectionPoints);
            this.addVObstacleLine(area, mxx, mny, mxy, offset, right,
                    hIntersectionPoints);
        }

        for (CLink link : hIntersectionPoints.keySet()) {
            LinkedList<int[]> points = hIntersectionPoints.get(link);

            while (!points.isEmpty()) {
                int[] p0 = points.removeFirst();

                for (int[] p1 : points) {
                    if (p0[1] == p1[1]) {
                        double line[] = new double[4];
                        line[0] = p0[0] + offset;
                        line[1] = -p0[1];
                        line[2] = p1[0] + offset;
                        line[3] = -p1[1];

                        obstacleSpace.addNewObstacle(new StaticObstacle(line),
                                link);

                        points.remove(p1);
                        break;
                    }
                }
            }
        }

        for (CLink link : vIntersectionPoints.keySet()) {
            LinkedList<int[]> points = vIntersectionPoints.get(link);

            while (!points.isEmpty()) {
                int[] p0 = points.removeFirst();

                for (int[] p1 : points) {
                    if (p0[0] == p1[0]) {
                        double line[] = new double[4];
                        line[0] = p0[0] + offset;
                        line[1] = -p0[1];
                        line[2] = p1[0] + offset;
                        line[3] = -p1[1];

                        obstacleSpace.addNewObstacle(new StaticObstacle(line),
                                link);

                        points.remove(p1);
                        break;
                    }
                }
            }
        }
    }

    private void addVObstacleLine(CArea area, int x, int mny, int mxy,
                                  int offset, Set<CLink> links,
                                  HashMap<CLink, LinkedList<int[]>> intersectionPoints) {
        ArrayList<Integer> yPoints = new ArrayList<Integer>();
        yPoints.add(mny);
        yPoints.add(mxy);

        for (CLink link : links) {
            yPoints.add(link.getCorner0().getY());
            yPoints.add(link.getCorner1().getY());

            LinkedList<int[]> points = intersectionPoints.get(link);
            if (points == null) {
                points = new LinkedList<int[]>();
                intersectionPoints.put(link, points);
            }

            points.add(new int[]{x, link.getCorner0().getY()});
            points.add(new int[]{x, link.getCorner1().getY()});
        }
        Collections.sort(yPoints);

        assert (yPoints.get(0) == mny);
        assert (yPoints.get(yPoints.size() - 1) == mxy);

        int y1 = -1;
        for (int y2 : yPoints) {
            if (y1 != -1) {

                double line[] = new double[4];
                line[0] = x + offset;
                line[1] = -y1;
                line[2] = x + offset;
                line[3] = -y2;

                obstacleSpace.addNewObstacle(new StaticObstacle(line), area);

                y1 = -1;
            } else {
                y1 = y2;
            }
        }
    }

    private void addHObstacleLine(CArea area, int y, int mnx, int mxx,
                                  int offset, Set<CLink> links,
                                  HashMap<CLink, LinkedList<int[]>> intersectionPoints) {
        ArrayList<Integer> xPoints = new ArrayList<Integer>();
        xPoints.add(mnx);
        xPoints.add(mxx);

        for (CLink link : links) {
            xPoints.add(link.getCorner0().getX());
            xPoints.add(link.getCorner1().getX());

            LinkedList<int[]> points = intersectionPoints.get(link);
            if (points == null) {
                points = new LinkedList<int[]>();
                intersectionPoints.put(link, points);
            }

            points.add(new int[]{link.getCorner0().getX(), y});
            points.add(new int[]{link.getCorner1().getX(), y});
        }
        Collections.sort(xPoints);

        assert (xPoints.get(0) == mnx);
        assert (xPoints.get(xPoints.size() - 1) == mxx);

        int x1 = -1;
        for (int x2 : xPoints) {
            if (x1 != -1) {

                double line[] = new double[4];
                line[0] = x1 + offset;
                line[1] = -y;
                line[2] = x2 + offset;
                line[3] = -y;

                obstacleSpace.addNewObstacle(new StaticObstacle(line), area);

                x1 = -1;
            } else {
                x1 = x2;
            }
        }
    }

    private static boolean isOverlappingWithVLine(int x, CArea area) {
        int mnx = Math.min(area.getCorner0().getX(), area.getCorner1().getX());
        int mxx = Math.max(area.getCorner0().getX(), area.getCorner1().getX());
        return x >= mnx && x <= mxx;
    }

    private static boolean isOverlappingWithHLine(int y, CArea area) {
        int mny = Math.min(area.getCorner0().getY(), area.getCorner1().getY());
        int mxy = Math.max(area.getCorner0().getY(), area.getCorner1().getY());
        return y >= mny && y <= mxy;
    }

    public int getOffset(int floorIdx) {
        return offsets.get(floorIdx);
    }

    public String getImage(int floorIdx) {
        return images.get(floorIdx);
    }

    public int getFloorByAreaId(int areaId) {
        if (floorByAreaIdMapping.containsKey(areaId)) {
            return floorByAreaIdMapping.get(areaId);
        } else {
            System.out.println(areaId + " not found in list of size "
                    + floorByAreaIdMapping.keySet().size());
            return -1;
        }

    }

    public CArea getAreaById(int areaId) {
        return areaByIdMapping.get(areaId);
    }

    public Set<LineSegment> getObstacleLinesByArea(int areaId) {
        return obstacleSpace.getObstacleLinesByAreaId(areaId);
    }

    public Set<CLink> getLinksForRoom(int roomId) {
        return linksByRoomIdMapping.get(roomId);
    }

    // private void moveAgentFloor(EvacAgent agent, int fromFloorIdx, int
    // toFloorIdx) {
    // synchronized(agentsByFloor) {
    // Set<EvacAgent> fromAgents = agentsByFloor.get(fromFloorIdx);
    // Set<EvacAgent> toAgents = agentsByFloor.get(toFloorIdx);
    //
    // fromAgents.remove(agent);
    // toAgents.add(agent);
    //
    // agent.getPerception().setCurrentFloorIdx(toFloorIdx);
    // }
    // }
    public boolean isAreaARoom(int areaId) {
        return linksByRoomIdMapping.containsKey(areaId);
    }

    public boolean isRoomAStaircase(int roomId) {
        return staircases.containsKey(roomId);
    }

    public int findAreaOfPoint(int x, int y, int floorIndex) {

        Set<Integer> areaIds = floorByAreaIdMapping.keySet();
        int currentAreaId = -1;
        for (int areaID : areaIds) {
            if (floorByAreaIdMapping.get(areaID) == floorIndex) {
                CArea area = areaByIdMapping.get(areaID);
                int mnx = Math.min(area.getCorner0().getX(), area.getCorner1().getX());
                int mny = Math.min(area.getCorner0().getY(), area.getCorner1().getY());
                int mxx = Math.max(area.getCorner0().getX(), area.getCorner1().getX());
                int mxy = Math.max(area.getCorner0().getY(), area.getCorner1().getY());

                if (x >= mnx && x <= mxx && y >= mny && y <= mxy) {
                    currentAreaId = area.getId();
                    break;
                }
            }
        }
//        if (currentAreaId == -1) {
////            System.out.println(x + "," + y + " not found in any area!");
////            assert false;
//        }
        return currentAreaId;
    }

    int findAreaOfPoint(int x, int y, int floorIndex, Set<Integer> areas) {

        int currentAreaId = -1;
        for (Integer areaID : areas) {

            CArea area = areaByIdMapping.get(areaID);
            int mnx = Math.min(area.getCorner0().getX(), area.getCorner1().getX());
            int mny = Math.min(area.getCorner0().getY(), area.getCorner1().getY());
            int mxx = Math.max(area.getCorner0().getX(), area.getCorner1().getX());
            int mxy = Math.max(area.getCorner0().getY(), area.getCorner1().getY());

            if (x >= mnx && x <= mxx && y >= mny && y <= mxy) {
                currentAreaId = area.getId();
                break;
            }

        }
//        if (currentAreaId == -1) {
////            System.out.println(x + "," + y + " not found in any area!");
////            assert false;
//        }
        return currentAreaId;
    }

    public boolean exitReached(int x, int y) {

        return false;
    }

    public int getWidth(int floorLevel) {
        return floorXSize.get(floorLevel);
    }

    public int getHeight(int floorLevel) {
        return floorYSize.get(floorLevel);
    }

    public void addAgent(IbevacAgent agent) {
        // add the agent to the various sets for fast access...
        this.agentSpace.addNewAgent(agent);

    }

    /**
     * @return the obstacleSpace
     */
    public Continuous2D getObstacleSpace(int floor) {
        return obstacleSpace.getField(floor);
    }

    public Continuous2D getAgentSpace(int floor) {
        return agentSpace.getAgentField(floor);
    }

    public Set<IbevacAgent> getAgentsByAreaId(int areaId) {
        return agentSpace.getAgentsByAreaId(areaId);
    }

    public Set<IbevacAgent> getAllAgents() {

        return agentSpace.getAllAgents();
    }

    public Set<LineSegment> getAllObstacleLines() {
        return obstacleSpace.getAllObstacles();
    }

    public Set<IbevacAgent> getAgentsByFloor(int floorNumber) {
        return agentSpace.getAgentsByFloor(floorNumber);
    }

    public Point2d findValidPointForAgent(int mnx, int mny, int mxx, int mxy,
                                          double size, int floorIndex) {
        return agentSpace.findValidPointForAgent(mnx, mny, mxx, mxy, size, floorIndex);
    }

    public void moveAgentFloor(IbevacAgent ibevacAgent, int fcurrent, int fnext) {
        this.agentSpace.moveAgentFloor(ibevacAgent, fcurrent, fnext);

    }

    public void moveAgentArea(IbevacAgent agent, int fromAreaId, int toAreaId) {
        agentSpace.moveAgentArea(agent, fromAreaId, toAreaId);
    }

    public void removeAgent(IbevacAgent agent) {
        agentSpace.removeAgent(agent);

    }

    public void updateAgentPosition(IbevacAgent agent) {
        agentSpace.updateAgentPosition(agent);

    }

    public Collection<? extends IbevacAgent> getAgentsInRadius(IbevacAgent me,
                                                               double radius) {

        return agentSpace.getAgentsInRadius(me, radius);
    }

    public Bag getObstaclesInRadius(
            IbevacAgent me, double radius) {
        return obstacleSpace.getObstaclesInRadius(me, radius);
    }

    public Collection<CArea> getAllRooms() {
        return this.areaByIdMapping.values();
    }

    Collection<LineSegment> getObstacleLinesByFloor(int floorNumber) {
        return obstacleSpace.getObstaclesLinesByFloor(floorNumber);
    }

    boolean areAllExitsBurning(FireSpace fireSpace, SmokeSpace smokeSpace) {
        for (CArea exit : exits) {
            int floorIdx = floorByAreaIdMapping.get(exit.getId());

            int mnx = Math.min(exit.getCorner0().getX(), exit.getCorner1().getX());
            int mxx = Math.max(exit.getCorner0().getX(), exit.getCorner1().getX());
            int mny = Math.min(exit.getCorner0().getY(), exit.getCorner1().getY());
            int mxy = Math.max(exit.getCorner0().getY(), exit.getCorner1().getY());

            int x = mnx;
            int y = (int) (0.5 * (mny + mxy));
//               if(!fireSpace.isAreaLethal(floorIdx, x, y)) {
//                    return false;
//               }else 
            if (!smokeSpace.isAreaSmoky(floorIdx, x, y)) {
                return false;
            }

        }
        return true;

    }

    boolean areStairCasesBurning(FireSpace fireSpace, SmokeSpace smokeSpace) {
        for (CStaircase scase : staircases.values()) {
            int floorIdx = floorByAreaIdMapping.get(scase.getId());
            if (floorIdx > 0) {
                continue;
            }
            int mnx = Math.min(scase.getCorner0().getX(), scase.getCorner1().getX());
            int mxx = Math.max(scase.getCorner0().getX(), scase.getCorner1().getX());
            int mny = Math.min(scase.getCorner0().getY(), scase.getCorner1().getY());
            int mxy = Math.max(scase.getCorner0().getY(), scase.getCorner1().getY());

            int x = (int) (0.5 * (mnx + mxx));
            int y = (int) (0.5 * (mny + mxy));
//               if(!fireSpace.isAreaLethal(floorIdx, x, y)) {
//                    return false;
//               }else 
            if (!smokeSpace.isAreaSmoky(floorIdx, x, y)) {
                return false;
            }

        }
        return true;
    }
}
