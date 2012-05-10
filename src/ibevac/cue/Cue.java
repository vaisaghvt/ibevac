/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ibevac.cue;

import ibevac.agent.knowledge.event.PhaseBucket.Phase;
import javax.vecmath.Point2d;


/**
 * An interface to define cues. Each cue is defined by its ambiguity, consistency 
 * with current observations and it's location.
 * 
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public interface Cue {

    /**
     * This is the ambiguity on a scale of one to ten.
     */
    public enum Ambiguity {
        ZERO(-1,-1), 
        ONE(-1,500), 
        TWO(-1,250), 
        THREE(-1,150), 
        FOUR(-1,100), 
        FIVE(1000,50), 
        SIX(500,35), 
        SEVEN(250,20), 
        EIGHT(150,10), 
        NINE(100,5), 
        TEN(50,0);
        private final int UNUSUAL;
        private final int FIRE;
        
        Ambiguity(int unusual, int fire){
            this.UNUSUAL = unusual;
            this.FIRE = fire;
        }
        
        public int getUnusual(){
            return this.UNUSUAL;
        }
        
        public int getFire() {
            return this.FIRE;
        }
        
        
    };

//    public Iterable<PhaseEffect> getEffects();

    public Point2d getLocation();
    
    /**
     * Consistency
     * @return  true if the cue indicates fire
     *          false if cue indicates that it isn't a fire
     */
    public boolean indicatesFire();

    /**
     * 
     * @return Ambiguity level  
     *          
     */
    public Ambiguity ambiguityLevel();
}
