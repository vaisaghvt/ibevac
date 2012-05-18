package ibevac.agent.navigation.level1motion;


import java.util.List;
import java.util.Set;

import abmcs.agent.MovingAgent;
import abmcs.agent.StaticObstacle;
import abmcs.motionplanning.level1.Level1MotionPlanning;
import abmcs.motionplanning.level2.SpatialWaypoint;

import javax.vecmath.Vector2d;

import utilities.Geometry;

/**
 * <h4> A kinetic energy based level 1 motion planner which does collision avoidance.
 * It avoids both static and dynamic obstacles </h4>
 *
 * @author <A HREF="mailto:heiko.aydt@tum-create.edu.sg">Heiko</A>
 * @version $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class L1MPStarPruning extends Level1MotionPlanning {

    private static final int n = 17;
    private static final int m = 8;
    private static final int deg = 10;
    private MovingAgent agent = null;
    private WaypointTracker tracker = null;
    private SpatialWaypoint waypoint = null;

    public L1MPStarPruning(MovingAgent agent) {
        this.agent = agent;
    }

    @Override
    public void reset() {
        tracker = null;
    }

    @Override
    public SpatialWaypoint getCurrentSpatialWaypoint() {
        return waypoint;
    }

    @Override
    public Vector2d getPreferredVelocity() {
        //determine our immediate waypoint
        if (tracker == null) {
            List<SpatialWaypoint> waypoints = agent.getLevel2MotionPlanning().getSpatialWaypoints();
            if (waypoints == null) {
                return new Vector2d();
            } else {
                tracker = new WaypointTracker(agent, waypoints);
            }
        }

        waypoint = tracker.getWaypoint();
        if (waypoint == null) {
            return new Vector2d();
        }

        double[] position = new double[]{this.agent.getPosition().getX(), this.agent.getPosition().getY()};
        double[] destination = new double[]{waypoint.getPoint().getX(), waypoint.getPoint().getY()};
        double lookahead = 50.0;

        //create the original candidate vectors
        double[][] vectors = this.createOriginalCandidateVectors(position, destination, lookahead);

        //identify best vector
        double[] vel = new double[]{vectors[m][0], vectors[m][1]};

        //consider static obstacles and prune/shorten candidate vectors
        this.pruneShortenWithStaticObstacles(position, destination, vectors);

        //consider dynamic obstacles and prune/shorten candidate vectors
//		this.pruneShortenWithDynamicObstacles(position, destination, vectors);

        //identify the best vector
        double[] velocity = this.identifyBestVector(position, destination, vectors);
        if (velocity == null) {
            velocity = vel;
        }

        //rescale the velocity (i.e., remove the lookahead to get the actual velocity)
        velocity[0] /= lookahead;
        velocity[1] /= lookahead;

        return new Vector2d(velocity[0], velocity[1]);
    }

    private double[][] createOriginalCandidateVectors(double[] position, double[] destination, double lookahead) {
        //determine the vector from position to destination
        double[][] vectors = new double[n][2];
        vectors[m][0] = destination[0] - position[0];
        vectors[m][1] = destination[1] - position[1];

        //determine the length of the vector 
        //(i.e., the required speed to reach the destination within one second)
        double v0 = Math.hypot(vectors[m][0], vectors[m][1]); //assumed unit: [m/s]

        //get the preferred speed
        double ps = this.agent.getPreferredSpeed(); //unit: [m/s]

        //determine the scaling factor when considering a lookahead
        double f = lookahead * (ps / v0);

        //scale the vector according to the scaling factor f
        vectors[m][0] *= f;
        vectors[m][1] *= f;

        //create more vectors based on the m-th vector plus some rotation
        for (int i = 1; i <= m; ++i) {
            vectors[m + i] = rotate(vectors[m], i * deg);
            vectors[m - i] = rotate(vectors[m], -i * deg);
        }

        return vectors;
    }

    private void pruneShortenWithStaticObstacles(double[] position, double[] destination, double[][] vectors) {
        Set<StaticObstacle> sobstacles = this.agent.getPerceivedStaticObstacles();

        double radius = 0.5 * this.agent.getEffectiveDiameter();

        //we try each vector with its original length first and shorten it in 5 steps of 20% until it does not result
        //in a collision or reaches zero length (in which case it's removed).
        for (int i = 0; i < vectors.length; ++i) {
            double dx = vectors[i][0] * 0.2;
            double dy = vectors[i][1] * 0.2;
            boolean hasCollision = true;

            for (int j = 0; j < 5 && hasCollision; ++j) {
                hasCollision = false;

                //shoulder lines
                double[][] tempLines = WaypointTracker.determineShoulderLines(position, vectors[i], radius);


//				this.lines.add(lines[0]);

                //test shoulder lines with all static obstacles
                for (StaticObstacle o : sobstacles) {
//					if(doLinesegmentsIntersect(lines[1], o.line) || doLinesegmentsIntersect(lines[2], o.line)) {
                    double[] lineSegmentAsArray = new double[]{
                            o.getLine().getStart().getX(),
                            o.getLine().getStart().getY(),
                            o.getLine().getEnd().getX(),
                            o.getLine().getEnd().getY()};
                    if (Geometry.doLinesegmentsIntersect(tempLines[1], lineSegmentAsArray)
                            && Geometry.doLinesegmentsIntersect(tempLines[2], lineSegmentAsArray)) {
//					if(doLinesegmentsIntersect(line0, o.line) || doLinesegmentsIntersect(line1, o.line) || doLinesegmentsIntersect(line2, o.line)) {
                        hasCollision = true;
                        vectors[i][0] -= dx;
                        vectors[i][1] -= dy;
                        break;
                    }
                }
            }

            //remove the vector if we still have a collision
            if (hasCollision) {
                vectors[i] = null;
            }
        }
    }

    private double[] identifyBestVector(double[] position, double[] destination, double[][] vectors) {
        double dbest = Double.MAX_VALUE;
        double[] vbest = null;

        for (double[] vector : vectors) {
            if (vector != null) {
                double dx = Math.abs(position[0] + vector[0] - destination[0]);
                double dy = Math.abs(position[1] + vector[1] - destination[1]);
                double d = Math.hypot(dx, dy);
//				double d = dx + dy;
                if (d < dbest) {
                    dbest = d;
                    vbest = vector;
                }
            }
        }

        return vbest;
    }

    //rotates a vector by an angle [deg]
    private static double[] rotate(double[] vector, double angle) {
        double rad = angle * Math.PI / 180.0;

        double x = (vector[0] * Math.cos(rad)) - (vector[1] * Math.sin(rad));
        double y = (vector[1] * Math.cos(rad)) + (vector[0] * Math.sin(rad));

        return new double[]{x, y};
    }


}
