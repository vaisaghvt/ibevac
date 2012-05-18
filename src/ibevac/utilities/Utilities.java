package ibevac.utilities;

import abmcs.agent.LineSegment;
import ibevac.datatypes.CArea;
import ibevac.datatypes.CCrowd;
import ibevac.datatypes.CEvacuationScenario;
import ibevac.datatypes.CExit;
import ibevac.datatypes.CFire;
import ibevac.datatypes.CFloor;
import ibevac.datatypes.CLink;
import ibevac.datatypes.CRoom;
import ibevac.datatypes.CStaircase;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A number of geometric functions / utilities that might be useful to the other
 * classes but are not necessarilly a part of any of the classes.
 *
 * @author <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 * @version $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public final class Utilities {

    private Utilities() {
    }

    /**
     * checks if the x value lies on one of the vertical lines of the CArea
     *
     * @param x
     * @param area
     * @return
     */
    public static boolean isOverlappingWithVLine(int x, CArea area) {
        int mnx = Math.min(area.getCorner0().getX(), area.getCorner1().getX());
        int mxx = Math.max(area.getCorner0().getX(), area.getCorner1().getX());
        return x >= mnx && x <= mxx;
    }

    /**
     * Checks if the y value passed lies on one of the horizontal edges of the
     * area
     *
     * @param y
     * @param area
     * @return
     */
    public static boolean isOverlappingWithHLine(int y, CArea area) {
        int mny = Math.min(area.getCorner0().getY(), area.getCorner1().getY());
        int mxy = Math.max(area.getCorner0().getY(), area.getCorner1().getY());
        return y >= mny && y <= mxy;
    }

    /**
     * Convert pixel units into metric units. To be more precise: [px] are
     * converted to [cm] by means of the scenario-specific scaling factor!
     * <p/>
     * WARNING: Don't call this function multiple times on the same scenario.
     * Otherwise the scenario will be scaled multiple times.
     */
    public static void convertPixelsToMetricUnits(CEvacuationScenario scenario) {
        double f = scenario.getScale(); //[px] -> [cm]

        //update the dimensions of all objects
        for (CFloor floor : scenario.getFloors()) {
            floor.setWidth((int) (floor.getWidth() * f));
            floor.setHeight((int) (floor.getHeight() * f));

            for (CCrowd area : floor.getCrowds()) {
                area.getCorner0().setX((int) (area.getCorner0().getX() * f));
                area.getCorner0().setY((int) (area.getCorner0().getY() * f));
                area.getCorner1().setX((int) (area.getCorner1().getX() * f));
                area.getCorner1().setY((int) (area.getCorner1().getY() * f));
            }

            for (CExit area : floor.getExits()) {
                area.getCorner0().setX((int) (area.getCorner0().getX() * f));
                area.getCorner0().setY((int) (area.getCorner0().getY() * f));
                area.getCorner1().setX((int) (area.getCorner1().getX() * f));
                area.getCorner1().setY((int) (area.getCorner1().getY() * f));
            }

            for (CFire area : floor.getFires()) {
                area.getCorner0().setX((int) (area.getCorner0().getX() * f));
                area.getCorner0().setY((int) (area.getCorner0().getY() * f));
                area.getCorner1().setX((int) (area.getCorner1().getX() * f));
                area.getCorner1().setY((int) (area.getCorner1().getY() * f));
            }

            for (CLink area : floor.getLinks()) {
                area.getCorner0().setX((int) (area.getCorner0().getX() * f));
                area.getCorner0().setY((int) (area.getCorner0().getY() * f));
                area.getCorner1().setX((int) (area.getCorner1().getX() * f));
                area.getCorner1().setY((int) (area.getCorner1().getY() * f));
            }

            for (CStaircase area : floor.getStaircases()) {
                area.getCorner0().setX((int) (area.getCorner0().getX() * f));
                area.getCorner0().setY((int) (area.getCorner0().getY() * f));
                area.getCorner1().setX((int) (area.getCorner1().getX() * f));
                area.getCorner1().setY((int) (area.getCorner1().getY() * f));
            }

            for (CRoom area : floor.getRooms()) {
                area.getCorner0().setX((int) (area.getCorner0().getX() * f));
                area.getCorner0().setY((int) (area.getCorner0().getY() * f));
                area.getCorner1().setX((int) (area.getCorner1().getX() * f));
                area.getCorner1().setY((int) (area.getCorner1().getY() * f));
            }
        }
    }

    /**
     * calculates the dot product of two vectors
     */
    public static double dot(double[] a, double[] b) {
        double dot = 0.0;

        assert (a.length == b.length);
        for (int i = 0; i < a.length; ++i) {
            dot += a[i] * b[i];
        }

        return dot;
    }

    /**
     * rotates a vector by an angle [deg]
     */
    public static double[] rotate(double[] vector, double angle) {
        double rad = angle * Math.PI / 180.0;

        double x = (vector[0] * Math.cos(rad)) - (vector[1] * Math.sin(rad));
        double y = (vector[1] * Math.cos(rad)) + (vector[0] * Math.sin(rad));

        return new double[]{x, y};
    }

    /**
     * determine whether two line segments intersect or not
     */
    public static boolean doLinesegmentsIntersect(LineSegment line1, LineSegment line2) {
        final double EPS = 0.00001;

        double x1 = line1.getStart().x;
        double x2 = line1.getEnd().x;
        double x3 = line2.getStart().x;
        double x4 = line2.getEnd().x;

        double y1 = line1.getStart().y;
        double y2 = line1.getEnd().y;
        double y3 = line2.getStart().y;
        double y4 = line2.getEnd().y;

        double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        double numera = (x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3);
        double numerb = (x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3);

        //are the line coincident?
        if (Math.abs(numera) < EPS && Math.abs(numerb) < EPS && Math.abs(denom) < EPS) {
            return true;
        }

        //are the lines parallel?
        if (Math.abs(denom) < EPS) {
            return false;
        }

        //is the intersection along the segment?
        double mua = numera / denom;
        double mub = numerb / denom;

        return !(mua < 0 || mua > 1 || mub < 0 || mub > 1);

    }

    /**
     * extract the four sides from an area in precisely this order:
     * double[] {left, top, right, bottom}
     */
    public static Collection<LineSegment> extractSidesFromArea(CArea area) {
        int x0 = area.getCorner0().getX();
        int x1 = area.getCorner1().getX();
        int y0 = area.getCorner0().getY();
        int y1 = area.getCorner1().getY();

        int mnx = Math.min(x0, x1);
        int mxx = Math.max(x0, x1);
        int mny = Math.min(y0, y1);
        int mxy = Math.max(y0, y1);

        LineSegment left = new LineSegment(mnx, mny, mnx, mxy);
        LineSegment right = new LineSegment(mxx, mny, mxx, mxy);
        LineSegment top = new LineSegment(mnx, mny, mxx, mny);
        LineSegment bottom = new LineSegment(mnx, mxy, mxx, mxy);

        Collection<LineSegment> sides = new ArrayList<LineSegment>();
        sides.add(left);
        sides.add(right);
        sides.add(top);
        sides.add(bottom);

        return sides;
    }

    /**
     * Calculates the time until collision of two circles with initial positions pa/pb, velocities va/vb, and radii ra/rb.
     * Returns either time until collision or null (if no collision)
     * based on: http://code.google.com/p/xna-circle-collision-detection/
     * see also: http://twobitcoder.blogspot.com/2010/04/circle-collision-detection.html
     */
    public static Double timeUntilCollision(double[] linea, double[] lineb, double ra, double rb) {
        double[] pa = new double[]{linea[0], linea[1]};
        double[] pb = new double[]{lineb[0], lineb[1]};

        double[] va = new double[]{linea[2] - linea[0], linea[3] - linea[1]};
        double[] vb = new double[]{lineb[2] - lineb[0], lineb[3] - lineb[1]};

        double[] pab = new double[]{pa[0] - pb[0], pa[1] - pb[1]};
        double[] vab = new double[]{va[0] - vb[0], va[1] - vb[1]};

        double a = dot(vab, vab);
        double b = 2 * dot(pab, vab);
        double c = dot(pab, pab) - (ra + rb) * (ra + rb);

        // The quadratic discriminant.
        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) {
            // Case 1:
            // If the discriminant is negative, then there are no real roots, so there is no collision.  The time of
            // closest approach is then given by the average of the imaginary roots, which is:  t = -b / 2a
            return null;
        } else {
            // Case 2 and 3:
            // If the discriminant is zero, then there is exactly one real root, meaning that the circles just grazed each other.  If the 
            // discriminant is positive, then there are two real roots, meaning that the circles penetrate each other.  In that case, the
            // smallest of the two roots is the initial time of impact.  We handle these two cases identically.
            double t0 = (-b + Math.sqrt(discriminant)) / (2 * a);
            double t1 = (-b - Math.sqrt(discriminant)) / (2 * a);
            double t = Math.min(t0, t1);

            // We also have to check if the time to impact is negative.  If it is negative, then that means that the collision
            // occurred in the past.  Since we're only concerned about future events, we say that no collision occurs if t < 0.
            if (t < 0) {
                return null;
            } else {
                return t;
            }
        }
    }
}
