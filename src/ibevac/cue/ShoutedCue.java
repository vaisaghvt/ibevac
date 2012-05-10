/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ibevac.cue;

import ibevac.agent.IbevacAgent;
import javax.vecmath.Point2d;

/**
 * 
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class ShoutedCue implements Cue{

    public ShoutedCue(ShoutedCue cue, IbevacAgent aThis) {
        throw new UnsupportedOperationException("Not yet implemented");
    }



    @Override
    public Point2d getLocation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean indicatesFire() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Ambiguity ambiguityLevel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
