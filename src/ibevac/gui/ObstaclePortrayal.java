package ibevac.gui;

import ibevac.engine.IbevacModel;
import ibevac.environment.IbevacSpace;

import ibevac.obstacle.IbevacStaticObstacle;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.vecmath.Point2d;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;

/**
 * This class is responsible for drawing (portraying) the obstacles. It essentially
 * draws a straight line since all obstacles are assumed to be straight lines
 * in the simulation
 *
 * @author <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 * @version $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class ObstaclePortrayal extends SimplePortrayal2D {

    /**
     *
     */
    private static final long serialVersionUID = 9213240403513810187L;
    final IbevacSpace space;
    static int count = 0;

    public ObstaclePortrayal(IbevacSpace space) {
        this.space = space;
    }

    @Override
    public void draw(Object object, Graphics2D gg, DrawInfo2D info) {

        IbevacStaticObstacle obstacle = (IbevacStaticObstacle) object;

        Point2d p0 = new Point2d(obstacle.getLine().getStart());
        Point2d p1 = new Point2d(obstacle.getLine().getEnd());

        p0 = IbevacSpace.translateToLogicalLocation(p0,
                space.getOffset(obstacle.getFloor()));
        p1 = IbevacSpace.translateToLogicalLocation(p1,
                space.getOffset(obstacle.getFloor()));

        gg.setColor(Color.black);
        p0.scale(1.0 / IbevacModel.scale);
        p1.scale(1.0 / IbevacModel.scale);


        gg.drawLine((int) p0.getX(), (int) p0.getY(), (int) p1.getX(), (int) p1.getY());
//		if(x1>600&&obstacle.getFloor()==0)
//			System.out.println(x1);
        count++;
    }


}
