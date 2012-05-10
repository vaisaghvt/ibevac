/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ibevac.cue;

import ibevac.agent.AgentDescriptionModule.AgentType;
import ibevac.agent.IbevacAgent;
import ibevac.agent.knowledge.waypoints.IbevacLogicalWaypoint;
import java.util.Set;
import javax.vecmath.Point2d;

/**
 * 
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class MessageCue implements Cue {

    public static Ambiguity managementAmbiguity = Ambiguity.ZERO;
    public static Ambiguity defaultAmbiguity = Ambiguity.ZERO;
    private boolean fireIndicator;
//    private AgentType type;
    private Ambiguity ambiguity;
     

    public MessageCue(boolean indicatesFire, AgentType agentType) {
//        this.type = agentType;
        switch (agentType) {
            case MANAGEMENT:
                this.ambiguity = managementAmbiguity;
                break;
            case DEFAULT:
                this.ambiguity = defaultAmbiguity;
                break;
            default: assert false;
                this.ambiguity = null;
                break;
        }
        this.fireIndicator = indicatesFire;
    }

    @Override
    public Point2d getLocation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean indicatesFire() {
        return fireIndicator;
    }

    @Override
    public Ambiguity ambiguityLevel() {
        return ambiguity;
    }
}
