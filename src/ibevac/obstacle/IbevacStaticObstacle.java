package ibevac.obstacle;

import abmcs.agent.LineSegment;

import abmcs.agent.StaticObstacle;

/**
 * This class stores the obstacles and the logical information like the 
 * associated area and floor for each static obstacle. Essentially it is just a
 * decorator for a line.
 * 
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class IbevacStaticObstacle extends StaticObstacle {
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */

	private final int area;
	private final int floor;

	public IbevacStaticObstacle(LineSegment line, int currentAreaId,
			int currentFloorId) {
		super(line);
		area = currentAreaId;
		floor = currentFloorId;
	}

	@Override
	public String toString() {
		return "StaticObstacle [line=" + super.getLine() + "]";
	}

	public int getArea() {
		return area;
	}

	public int getFloor() {
		return floor;
	}

}
