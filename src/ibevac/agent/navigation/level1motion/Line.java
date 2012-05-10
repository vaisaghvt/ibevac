package ibevac.agent.navigation.level1motion;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

/**
 * <h4>A Line class to be used by RVO2 for proper fuctioning.</h4>
 * 
 *
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class Line {

    /**
     * A point through which the lines passes
     */
    public Point2d point;
    
    /**
     * The direction of the line
     */
    public Vector2d direction;

    /**
     * Copy constructor
     * @param line 
     */
    public Line(Line line) {
        this.point = new Point2d(line.point);
        this.direction = new Vector2d(line.direction);
    }

    public Line() {
        this.point = new Point2d();
        this.direction = new Vector2d();
    }
}
