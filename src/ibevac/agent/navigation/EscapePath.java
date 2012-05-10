package ibevac.agent.navigation;

import java.util.List;

import abmcs.motionplanning.level3.LogicalWaypoint;

/**
 * <h4>Returns a  list of logical waypoints that will be created in level 3 motion planning
 * and used in level 2 of motion planning.</h4>
 *
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public interface EscapePath {
    public List<LogicalWaypoint> getLogicalWaypoints();
}
