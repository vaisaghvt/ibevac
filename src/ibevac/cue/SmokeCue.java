/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ibevac.cue;

import javax.vecmath.Point2d;

/**
 *
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class SmokeCue implements Cue {

   
    private final Point2d location;
    private final float smokeValue;
    
    public SmokeCue(int x, int y, float smoke) {
        this.location = new Point2d(x, y);    
        this.smokeValue = smoke;
    }

    @Override
    public Point2d getLocation() {
        return location;
    }
    
    public float getSmoke(){
        return smokeValue;
    }

    @Override
    public boolean indicatesFire() {
       return true;
    }

    @Override
    public Ambiguity ambiguityLevel() {
       return Ambiguity.TWO;
    }

    public String toString() {
        return "FireCue{" + "location=" + location + '}';
    }
}
