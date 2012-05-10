/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ibevac.cue;


import ibevac.agent.IbevacAgent;
import ibevac.environment.IbevacSpace;
import javax.vecmath.Point2d;


/**
 * Agents that are running fast create fast agent cues
 * 
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class FastAgentCue implements Cue {

//    final Set<PhaseEffect> phaseEffects = new HashSet<>();
    Point2d location;
 
    IbevacSpace space;
    IbevacAgent agent;
    
    public FastAgentCue(IbevacAgent agent, IbevacSpace space) {
        this.location = new Point2d(agent.getLogicalPosition());
//        phaseEffects.add(new PhaseEffect(Phase.UNUSUAL, Effect.INCREASE_TO_FULL));
        this.space = space;
        this.agent = agent;
    }


    @Override
    public Point2d getLocation() {
        return location;
    }

//    public void setLocation(Point2d logicalPosition) {
//        this.location = logicalPosition;
//        SmallRoom tempRoom = space.getSmallRoomOfPoint((int)logicalPosition.x, (int)logicalPosition.y,agent.getCurrentFloorId());
//        if(previousRoom == null){
//            previousRoom = currentRoom = tempRoom;        
//        }else if(!currentRoom.equals(tempRoom)){
//            previousRoom = currentRoom;
//            currentRoom = tempRoom;
//            space.removeCueFromSmallRoom(this, previousRoom);
//            space.putCueInSmallRoom(this, currentRoom);
//        }
//    }
//
//    public SmallRoom getCurrentRoom() {
//        return currentRoom;
//    }
//
//    public void setCurrentRoom(SmallRoom currentRoom) {
//        this.currentRoom = currentRoom;
//    }
//
//    public SmallRoom getPreviousRoom() {
//        return previousRoom;
//    }
//
//    public void setPreviousRoom(SmallRoom previousRoom) {
//        this.previousRoom = previousRoom;
//    }

    @Override
    public boolean indicatesFire() {
        return true;
    }

    @Override
    public Ambiguity ambiguityLevel() {
        return Ambiguity.FIVE;
    }
}
