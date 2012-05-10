package ibevac.agent.knowledge.waypoints;

import abmcs.motionplanning.level2.SpatialWaypoint;
import javax.vecmath.Point2d;

/**
 * <h4>An extension of spatial waypoint which stores floor index and the
 * associated logical waypoint.</h4>
 * 
 * <h4> Being a spatial waypoint it stores point information which is used by
 * level 2 motion planning</h4>
 *
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class IbevacSpatialWaypoint extends SpatialWaypoint {

    private int floorIdx = -1;
    private IbevacLogicalWaypoint logicalWaypoint = null;

    public IbevacSpatialWaypoint(double x, double y, int floorIdx, IbevacLogicalWaypoint logicalWaypoint) {
        super(x, y);

        this.floorIdx = floorIdx;
        this.logicalWaypoint = logicalWaypoint;
    }

    public IbevacSpatialWaypoint(Point2d pos, int floorIdx, IbevacLogicalWaypoint logicalWaypoint) {
        super(pos);

        this.floorIdx = floorIdx;
        this.logicalWaypoint = logicalWaypoint;
    }

    public int getFloorIndex() {
        assert floorIdx != -1;
        return floorIdx;
    }

    public IbevacLogicalWaypoint getLogicalWaypoint() {
        assert logicalWaypoint!=null;
        return logicalWaypoint;
    }
}
