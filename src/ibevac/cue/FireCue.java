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
public class FireCue implements Cue {

   
    final Point2d location;
    
    public FireCue(int x, int y) {
        this.location = new Point2d(x, y);    
    }


    @Override
    public Point2d getLocation() {
        return location;
    }

    @Override
    public boolean indicatesFire() {
       return true;
    }

    @Override
    public Ambiguity ambiguityLevel() {
        return Ambiguity.ZERO;
    }

    public String toString() {
        return "FireCue{" + "location=" + location + '}';
    }
}
