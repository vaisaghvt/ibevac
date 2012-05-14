package ibevac.agent.navigation;

import ibevac.agent.knowledge.waypoints.IbevacLogicalWaypoint;
import ibevac.agent.IbevacAgent;

import java.util.ArrayList;
import java.util.List;

import abmcs.motionplanning.level2.Level2MotionPlanning;
import abmcs.motionplanning.level2.SpatialWaypoint;
import abmcs.motionplanning.level3.LogicalWaypoint;
/**
 * <h4>An implementation of level 2 motion planning. It simply extracts the 
 * spatial waypoints from the logical  waypoints generated by level 3 motion 
 * planning</h4>
 *
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class EvacL2MP extends Level2MotionPlanning {

    
    private IbevacAgent agent = null;

    public EvacL2MP(IbevacAgent agent) {
        this.agent = agent;
    }

    /**
     * Extracts spatial waypoints from the current set of logical waypoints.
     * These spatial waypoints are not stored because this function is called 
     * only when there is rerouting and the logical waypoints have changed.
     * @return List of Spatial waypoints
     */
    @Override
    public List<SpatialWaypoint> getSpatialWaypoints() {
        //get the logical waypoints from level 3 (if any)
        List<LogicalWaypoint> lwaypoints = this.agent.getLevel3MotionPlanning().getLogicalWaypoints();
        if (lwaypoints == null) {
            return null;
        }

        //create a sequence of spatial waypoints
        List<SpatialWaypoint> waypoints = new ArrayList<SpatialWaypoint>();
        int i = 0;
        for (LogicalWaypoint wp : lwaypoints) {
            assert (wp instanceof IbevacLogicalWaypoint);

            IbevacLogicalWaypoint ewp = (IbevacLogicalWaypoint) wp;


            waypoints.add(ewp.getWP0());
            if (ewp.getWP1().getFloorIndex() == agent.getCurrentFloorId()) {
                waypoints.add(ewp.getWP1());
            }

            if (i == lwaypoints.size() - 1) {
                agent.setCurrentGoal(ewp.getWP1().getPoint());
            }
            i++;
        }
        return waypoints;
    }
}