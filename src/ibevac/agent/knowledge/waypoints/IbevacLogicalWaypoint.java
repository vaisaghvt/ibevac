package ibevac.agent.knowledge.waypoints;

import abmcs.agent.LineSegment;
import abmcs.motionplanning.level2.SpatialWaypoint;
import ibevac.agent.IbevacAgent;
import ibevac.datatypes.CArea;
import ibevac.datatypes.CExit;
import ibevac.datatypes.CLink;
import ibevac.environment.IbevacSpace;
import ibevac.utilities.Utilities;
import abmcs.motionplanning.level3.LogicalWaypoint;
import ibevac.EvacConstants;
import ibevac.utilities.IbevacRNG;
import java.util.Collection;
import javax.vecmath.Point2d;

/**
 * <h4>An extension of LogicalWayPoint with more Ibevac specific knowledge
 * stored for each waypoint. This includes information on associated links and 
 * areas.</h4>
 * 
 * 
 *
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class IbevacLogicalWaypoint implements LogicalWaypoint {

    /**
     * The getLink associated with this waypoint. 
     * null for staircases and rooms.
     * non-null for links and exits
     */
    private CLink link = null;
    /**
     * The left or top area associated with this waypoint.
     * null only for a a particular type of exit
     */
    private CArea area0 = null;
    /**
     * The right or bottom area associated with this waypoint. 
     * Null only for a particular kind of exit
     */
    private CArea area1 = null;
    /**
     * The first spatial waypoint associated with this logical waypoint. What it 
     * stores depends on the type of logical way point. It is generally the 
     * entering spatial waypoint
     * 
     */
    private IbevacSpatialWaypoint wp0 = null;
    /**
     * The first spatial waypoint associated with this logical waypoint. What it 
     * stores depends on the type of logical way point. It is generally the 
     * exitting spatial waypoint
     * 
     */
    private IbevacSpatialWaypoint wp1 = null;
    /**
     * A reference to space
     */
    private IbevacSpace space;

    /**
     * Redirects to the appropriate function to create the logical waypoint based 
     * on the parameters passed.
     * 
     * @param getLink
     * @param area0
     * @param area1
     * @param space
     * @param agent 
     */
    public IbevacLogicalWaypoint(CLink link, CArea area0, CArea area1, IbevacSpace space, IbevacAgent agent) {
        this.link = link;
        this.space = space;
        if (link == null && area0 != null && area1 != null) {
            assert (area0 != null);
            assert (area1 != null);
            this.processStaircase(area0, area1, space, agent);
        } else if (link == null && area0 != null && area1 == null) {
            // This is an area and can only be 
            this.processArea(area0, space, agent);
        } else if (area0 != null && area1 != null) {
            assert (link != null);
            this.processLink(link, area0, area1, space, agent);
        } else if (area0 != null) {
            assert (link != null);
            this.processExit(link, area0, space, agent);
        } else if (area1 != null) {
            assert (link != null);
            this.processExit(link, area1, space, agent);
        } else {
            //shouldn't happen...
            assert (false);
        }
    }

    /**
     * Create a logical waypoint for this getLink. Each spatial waypoint is initially
     * set on the intersection of the getLink with each of the associated areas. 
     * Following this the links are moved a certain distance outward to help in 
     * easier and better path planning.
     * 
     * @param getLink
     * @param area0
     * @param area1
     * @param space
     * @param agent 
     */
    private void processLink(CLink link, CArea area0, CArea area1, IbevacSpace space, IbevacAgent agent) {
        this.area0 = area0;
        this.area1 = area1;

        int mnx0 = Math.min(area0.getCorner0().getX(), area0.getCorner1().getX());
        int mxx0 = Math.max(area0.getCorner0().getX(), area0.getCorner1().getX());
        int mny0 = Math.min(area0.getCorner0().getY(), area0.getCorner1().getY());
        int mxy0 = Math.max(area0.getCorner0().getY(), area0.getCorner1().getY());

        int mnx1 = Math.min(area1.getCorner0().getX(), area1.getCorner1().getX());
        int mxx1 = Math.max(area1.getCorner0().getX(), area1.getCorner1().getX());
        int mny1 = Math.min(area1.getCorner0().getY(), area1.getCorner1().getY());
        int mxy1 = Math.max(area1.getCorner0().getY(), area1.getCorner1().getY());

        //find the sides of the getLink that is contained by the areas
        Collection<LineSegment> linkSides = Utilities.extractSidesFromArea(link);
//     System.out.println(linkSides.size());
        for (LineSegment linkSide : linkSides) {
            //is this side contained by area0?
//                 System.out.println("general");
            if (linkSide.getStart().x >= mnx0 && linkSide.getStart().x <= mxx0
                    && linkSide.getEnd().x >= mnx0 && linkSide.getEnd().x <= mxx0
                    && linkSide.getStart().y >= mny0 && linkSide.getStart().y <= mxy0
                    && linkSide.getEnd().y >= mny0 && linkSide.getEnd().y <= mxy0) {
                assert (wp0 == null);
//   System.out.println("getWP0");
                int f0 = space.getFloorByAreaId(area0.getId());
                int offset0 = space.getOffset(f0);

                Point2d temp0 = new Point2d(0.5 * (linkSide.getStart().getX() + linkSide.getEnd().getX()),
                        0.5 * (linkSide.getStart().getY() + linkSide.getEnd().getY()));
                temp0 = agent.translateToPhysicalLocation(temp0, offset0);

                this.wp0 = new IbevacSpatialWaypoint(temp0, f0, this);
            } //is this side contained by area1?
            else if (linkSide.getStart().x >= mnx1 && linkSide.getStart().x <= mxx1
                    && linkSide.getEnd().x >= mnx1 && linkSide.getEnd().x <= mxx1
                    && linkSide.getStart().y >= mny1 && linkSide.getStart().y <= mxy1
                    && linkSide.getEnd().y >= mny1 && linkSide.getEnd().y <= mxy1) {
                assert (wp1 == null);
// System.out.println("getWP1");
                int f1 = space.getFloorByAreaId(area1.getId());
                int offset1 = space.getOffset(f1);

                Point2d temp1 = new Point2d(0.5 * (linkSide.getStart().getX() + linkSide.getEnd().getX()),
                        0.5 * (linkSide.getStart().getY() + linkSide.getEnd().getY()));
                temp1 = agent.translateToPhysicalLocation(temp1, offset1);

                this.wp1 = new IbevacSpatialWaypoint(temp1, f1, this);
            }

        }
        assert (wp0 != null && wp1 != null);

        /*Add a distance equivalent to agent radius to either side of line 
         * segment connecting two spatial waypoints
         * 
         */
        //horizontally aligned?
        double radius = 0.6 * EvacConstants.AGENT_MAX_DIAMETER;
        Point2d beforewp0 = new Point2d(wp0.getPoint());

//        Point2d beforwp1 = getWP1.getPoint();
        if (wp0.getPoint().y == wp1.getPoint().y) {
            if (wp0.getPoint().x < wp1.getPoint().x) {
                wp0.setX(wp0.getPoint().x - radius);
                wp1.setX(wp1.getPoint().x + radius);
//                
//                getWP0.getPoint().x -= radius;
//                getWP1.getPoint().x += radius;
            } else {
                wp0.setX(wp0.getPoint().x + radius);
                wp1.setX(wp1.getPoint().x - radius);
//                getWP0.getPoint().x += radius;
//                getWP1.getPoint().x -= radius;
            }
        } //vertically aligned
        else if (wp0.getPoint().x == wp1.getPoint().x) {
            if (wp0.getPoint().y < wp1.getPoint().y) {
                wp0.setY(wp0.getPoint().y - radius);
                wp1.setY(wp1.getPoint().y + radius);
//                getWP0.getPoint().y -= radius;
//                getWP1.getPoint().y += radius;
            } else {
                wp0.setY(wp0.getPoint().y + radius);
                wp1.setY(wp1.getPoint().y - radius);
//                getWP0.getPoint().y += radius;
//                getWP1.getPoint().y -= radius;
            }
        } else {
            assert (false);
        }
        assert (wp0.getPoint().getX() != beforewp0.getX())
                || (wp0.getPoint().getY() != beforewp0.getY());
    }

    /**
     * Creates a logical waypoint that is associated with an exit. The first 
     * waypoint is at the intesection of the side of the getLink with the connected 
     * area. The second waypoint is at the center of the getLink. 
     * @param getLink
     * @param area
     * @param space
     * @param agent 
     */
    private void processExit(CLink link, CArea area, IbevacSpace space, IbevacAgent agent) {
        assert (link instanceof CExit);

        int mnx0 = Math.min(area.getCorner0().getX(), area.getCorner1().getX());
        int mxx0 = Math.max(area.getCorner0().getX(), area.getCorner1().getX());
        int mny0 = Math.min(area.getCorner0().getY(), area.getCorner1().getY());
        int mxy0 = Math.max(area.getCorner0().getY(), area.getCorner1().getY());

        //find the sides of the getLink that is contained by the area
        Collection<LineSegment> linkSides = Utilities.extractSidesFromArea(link);
        for (LineSegment linkSide : linkSides) {
            //is this side contained by area0?
            if (linkSide.getStart().x >= mnx0 && linkSide.getStart().x <= mxx0
                    && linkSide.getEnd().x >= mnx0 && linkSide.getEnd().x <= mxx0
                    && linkSide.getStart().y >= mny0 && linkSide.getStart().y <= mxy0
                    && linkSide.getEnd().y >= mny0 && linkSide.getEnd().y <= mxy0) {
                assert (wp0 == null);


                int offset = space.getOffset(space.getFloorByAreaId(area.getId()));
                Point2d temp = new Point2d(0.5 * (linkSide.getStart().getX() + linkSide.getEnd().getX()),
                        0.5 * (linkSide.getStart().getY() + linkSide.getEnd().getY()));
                temp = agent.translateToPhysicalLocation(temp, offset);


                int f0 = space.getFloorByAreaId(area.getId());
                this.wp0 = new IbevacSpatialWaypoint(temp, f0, this);
            }
        }

        int mnx1 = Math.min(link.getCorner0().getX(), link.getCorner1().getX());
        int mxx1 = Math.max(link.getCorner0().getX(), link.getCorner1().getX());
        int mny1 = Math.min(link.getCorner0().getY(), link.getCorner1().getY());
        int mxy1 = Math.max(link.getCorner0().getY(), link.getCorner1().getY());

        Point2d temp = new Point2d(0.5 * (mnx1 + mxx1), 0.5 * (mny1 + mxy1));
        int offset = space.getOffset(space.getFloorByAreaId(area.getId()));

        temp = agent.translateToPhysicalLocation(temp, offset);

        int f1 = space.getFloorByAreaId(link.getId());
        this.wp1 = new IbevacSpatialWaypoint(temp, f1, this);

        assert (wp0 != null && wp1 != null);
    }

    /**
     * The logical waypoint for a staircase. It has two associated areas and the
     * spatial waypoints are the midpoints of these two areas.
     * @param area0
     * @param area1
     * @param space
     * @param agent 
     */
    private void processStaircase(CArea area0, CArea area1, IbevacSpace space, IbevacAgent agent) {
        this.area0 = area0;
        this.area1 = area1;

        int mnx0 = Math.min(area0.getCorner0().getX(), area0.getCorner1().getX());
        int mxx0 = Math.max(area0.getCorner0().getX(), area0.getCorner1().getX());
        int mny0 = Math.min(area0.getCorner0().getY(), area0.getCorner1().getY());
        int mxy0 = Math.max(area0.getCorner0().getY(), area0.getCorner1().getY());

        int mnx1 = Math.min(area1.getCorner0().getX(), area1.getCorner1().getX());
        int mxx1 = Math.max(area1.getCorner0().getX(), area1.getCorner1().getX());
        int mny1 = Math.min(area1.getCorner0().getY(), area1.getCorner1().getY());
        int mxy1 = Math.max(area1.getCorner0().getY(), area1.getCorner1().getY());

        int f0 = space.getFloorByAreaId(area0.getId());
        int f1 = space.getFloorByAreaId(area1.getId());

        int offset0 = space.getOffset(f0);
        int offset1 = space.getOffset(f1);

        Point2d temp0 = new Point2d(0.5 * (mnx0 + mxx0), 0.5 * (mny0 + mxy0));
        temp0 = agent.translateToPhysicalLocation(temp0, offset0);
        this.wp0 = new IbevacSpatialWaypoint(temp0, f0, this);

        Point2d temp1 = new Point2d(0.5 * (mnx1 + mxx1), 0.5 * (mny1 + mxy1));
        temp1 = agent.translateToPhysicalLocation(temp1, offset1);
        this.wp1 = new IbevacSpatialWaypoint(temp1, f1, this);
    }

    /**
     * The logical waypoint associated with an area. Both waypoints are set to
     * the center of the room. This is generally used in conjunction with 
     * the setInitialWayPoint(SpatialWaypoint lastPoint)
     * @param area
     * @param space
     * @param agent
     * 
     * @see IbevacLogicalWaypoint#setInitialWayPoint(abmcs.motionplanning.level2.SpatialWaypoint) 
     */
    private void processArea(CArea area, IbevacSpace space, IbevacAgent agent) {

        this.area0 = area;
        int mnx = Math.min(area.getCorner0().getX(), area.getCorner1().getX());
        int mxx = Math.max(area.getCorner0().getX(), area.getCorner1().getX());
        int mny = Math.min(area.getCorner0().getY(), area.getCorner1().getY());
        int mxy = Math.max(area.getCorner0().getY(), area.getCorner1().getY());
//        IbevacRNG random = IbevacRNG.instance();
//            int x = mnx + random.nextInt(mxx - mnx) + space.get;
//            int y = mny + random.nextInt(mxy - mny);
        Point2d pos = new Point2d(0.5 * (mnx + mxx), 0.5 * (mny + mxy));

        Point2d temp = new Point2d(pos);
        int offset = space.getOffset(space.getFloorByAreaId(area.getId()));

        temp = agent.translateToPhysicalLocation(temp, offset);

        int f1 = space.getFloorByAreaId(area.getId());
        this.wp1 = new IbevacSpatialWaypoint(temp, f1, this);
        this.wp0 = new IbevacSpatialWaypoint(temp, f1, this);

        assert (wp0 != null && wp1 != null);
    }

    /**
     * Sets the initial waypoint of this area. The initial waypoint is set such 
     * that there is an intermediate stopping point after the previous spatial 
     * waypoint and before this area's central spatial waypoint.
     * 
     * Only to be used for area logical waypoints
     * @param lastPoint
     * @return modified logical waypoint so that this can be chained.
     */
    public IbevacLogicalWaypoint setInitialWayPoint(SpatialWaypoint lastPoint) {
        assert this.link == null && this.area1 == null;

        if (lastPoint != null) {
            double xDifference = Math.abs(lastPoint.getPoint().x - wp1.getPoint().x);
            double yDifference = Math.abs(lastPoint.getPoint().y - wp1.getPoint().y);
            if (xDifference < yDifference) {// if distance is shorter in y direction set to same y as lastPoint
                this.wp0 = new IbevacSpatialWaypoint(wp1.getPoint().x, lastPoint.getPoint().y, space.getFloorByAreaId(area0.getId()), this);

            } else {
                this.wp0 = new IbevacSpatialWaypoint(lastPoint.getPoint().x, wp1.getPoint().y, space.getFloorByAreaId(area0.getId()), this);
            }
        }
        return this;
    }

    public CLink getLink() {
        return link;
    }

    public int getAreaId0() {
        if (area0 != null) {
            return area0.getId();
        } else {
            return -1;
        }
    }

    public int getAreaId1() {
        if (area1 != null) {
            return area1.getId();
        } else {
            return -1;
        }


    }

    public IbevacSpatialWaypoint getWP0() {
        return wp0;
    }

    public IbevacSpatialWaypoint getWP1() {
        return wp1;
    }

    public String toString() {
        if (area0 != null && area1 != null) {
            return "Link{area0=" + area0.getId() + ", area1=" + area1.getId() + ", wp0=" + wp0 + ", wp1=" + wp1 + "'}'";
        } else if (area0 != null && area1 == null) {
            return "Area{area0=" + area0.getId();// + ", getWP0=" + getWP0 + ", getWP1=" + getWP1 + "'}'";
        } else if (area0 == null && area1 != null) {
            return "Area{area1=" + area1.getId();
        }// + ", getWP0=" + getWP0 + ", getWP1=" + getWP1 + "'}'";
//        } else {
//            return "LP{getWP0=" + getWP0 + ", getWP1=" + getWP1 + "'}'";
//        }
        return "";
    }
}
