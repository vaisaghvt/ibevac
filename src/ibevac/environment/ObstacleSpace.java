package ibevac.environment;

import abmcs.agent.LineSegment;
import ibevac.agent.IbevacAgent;
import ibevac.datatypes.CArea;
import ibevac.engine.IbevacModel;
import ibevac.obstacle.IbevacStaticObstacle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import abmcs.agent.StaticObstacle;
import ibevac.EvacConstants;
import javax.vecmath.Point2d;
import sim.util.Bag;

/**
 * This class is responsible for handling and storing agents.
 * 
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class ObstacleSpace {

    /**
     * The fields on which the obstacles are stored
     */
    private ArrayList<Continuous2D> obstacleSpaces;
    
    /**
     * The obstacle lines in each area
     */
    private Map<Integer, Set<LineSegment>> obstacleLinesByArea;
    
    /**
     * The set of obstacle lines in each floor
     */
    private Map<Integer, Set<LineSegment>> obstacleLinesByFloor;
    private RealWorldLayout realWorld;

    public ObstacleSpace(RealWorldLayout realWorld, int numberOfFloors) {
        obstacleSpaces = new ArrayList<Continuous2D>(numberOfFloors);
        obstacleLinesByArea = new HashMap<Integer, Set<LineSegment>>();
        obstacleLinesByFloor = new HashMap<Integer, Set<LineSegment>>();
        this.realWorld = realWorld;
    }

    public void addNewObstacle(StaticObstacle obstacle, CArea area) {

        IbevacStaticObstacle tempObstacle = new IbevacStaticObstacle(
                obstacle.getLine(), area.getId(), realWorld.getFloorByAreaId(area.getId()));

        if (this.obstacleLinesByArea.get(area.getId()) == null) {

            this.obstacleLinesByArea.put(area.getId(),
                    new HashSet<LineSegment>());
        }
        this.obstacleLinesByArea.get(area.getId()).add(tempObstacle.getLine());

        int floor = realWorld.getFloorByAreaId(area.getId());
        if (this.obstacleLinesByFloor.get(floor) == null) {

            this.obstacleLinesByFloor.put(floor,
                    new HashSet<LineSegment>());
        }
        this.obstacleLinesByFloor.get(floor).add(tempObstacle.getLine());



        Point2d startPos = IbevacSpace.translateToLogicalLocation(tempObstacle.getLine().getStart(),
                realWorld.getOffset(floor));

        obstacleSpaces.get(floor).setObjectLocation(
                tempObstacle,
                new Double2D(startPos.getX() / IbevacModel.scale,
                startPos.getY() / IbevacModel.scale));

        // obstacleSpaces.get(floor).setObjectLocation(
        // tempObstacle,
        // new Double2D(tempObstacle.line[2] / IbevacModel.scale,
        // tempObstacle.line[3] / IbevacModel.scale));

    }

    public void addSpace(int width, int height) {
        obstacleSpaces.add(new Continuous2D(Math.ceil(width / IbevacModel.scale), Math.ceil(height
                / IbevacModel.scale), EvacConstants.MAX_SENSOR_RANGE
                / IbevacModel.scale));

    }

    public Set<LineSegment> getObstacleLinesByAreaId(int areaId) {

//        Set<LineSegment> returnedObstacles = new HashSet<>();
//        if (obstacleLinesByArea.get(areaId) != null) {
//            for (StaticObstacle obstacle : obstacleLinesByArea.get(areaId)) {
//                returnedObstacles.add(obstacle.getLine());
//            }
//        }
//        return returnedObstacles;
        return obstacleLinesByArea.get(areaId);
    }

    Collection<LineSegment> getObstaclesLinesByFloor(int floorNumber) {

//        Set<LineSegment> returnedObstacles = new HashSet<>();
//        if (obstacleLinesByFloor.get(floorNumber) != null) {
//            for (StaticObstacle obstacle : obstacleLinesByFloor.get(floorNumber)) {
//                returnedObstacles.add(obstacle.getLine());
//            }
//        }
        return obstacleLinesByFloor.get(floorNumber);

    }

    public Continuous2D getField(int floor) {

        // for (Object obstacle : obstacleSpaces.get(floor).getAllObjects()) {
        // StaticObstacle statObstacle = (StaticObstacle) obstacle;
        //
        // System.out.println(statObstacle);
        // System.out.println(obstacleSpaces.get(floor).getObjectLocation(
        // obstacle));
        // }
        return obstacleSpaces.get(floor);
    }

    public Set<LineSegment> getAllObstacles() {
        HashSet<LineSegment> allObstacles = new HashSet<LineSegment>();
        for (Set<LineSegment> obstaclesByFloor : obstacleLinesByFloor.values()) {
//            for (StaticObstacle obstacle : obstaclesByArea) {
                allObstacles.addAll(obstaclesByFloor);
//            }
        }
        return allObstacles;
    }

    public Bag getObstaclesInRadius(
            IbevacAgent me, double radius) {

        return obstacleSpaces.get(me.getCurrentFloorId()).getObjectsExactlyWithinDistance(
                new Double2D(me.getLogicalPosition().getX() / IbevacModel.scale,
                me.getLogicalPosition().getY() / IbevacModel.scale),
                radius);
    }
}
