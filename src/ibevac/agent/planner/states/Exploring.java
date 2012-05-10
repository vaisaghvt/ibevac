/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ibevac.agent.planner.states;

import ibevac.agent.AgentDescriptionModule.AgentType;
import ibevac.agent.IbevacAgent;
import ibevac.agent.knowledge.event.PhaseBucket.Phase;
import ibevac.agent.planner.Planner;
import ibevac.cue.Cue;
import ibevac.utilities.IbevacRNG;
import java.util.Collection;
import java.util.HashSet;

/**
 * The state of the exploring agent. During this state the agent chooses a 
 * random corridor from the list of corridor as his current goal. Most cues force it to start escaping. 
 * However there are some cues that might get the agent to shift back to deefault behavior
 * 
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class Exploring extends State {

    private final HashSet<Integer> currentGoals;
    private int timer;

    public Exploring(Planner planner, IbevacAgent agent) {

        super(planner, agent);
//        System.out.println(agent.getId()+" Exploring");
        currentGoals = new HashSet<Integer>();

        currentGoals.clear();
//        currentGoals.addAll(planner.getEnvironmentKnowledge().getCorridors());
//        currentGoals.remove(agent.getCurrentAreaId());
        /*
         * Choose any one of the corridors as a goal for exploration
         */
        currentGoals.add((Integer) planner.getEnvironmentKnowledge().getCorridors().
                toArray()[IbevacRNG.instance().nextInt(
                planner.getEnvironmentKnowledge().getCorridors().size())]);
//        timer = 300 + IbevacRNG.instance().nextInt(5000);
        timer =3000;
        
    }

    @Override
    public Collection<Integer> getCurrentGoals() {
        return this.currentGoals;
    }

    @Override
    public State handlePhaseChange(Phase phase) {

        switch (phase) {
            case FIRE:
                return new Escaping(planner, agent);
//return this;
            case UNUSUAL:
//                System.out.println("Escape");
//                return new Escaping(planner, agent);
                return this;
            case NONE:
                return new Default(planner, agent);


        }
        assert false;
        return null;
    }

    @Override
    public void executeActions() {
        timer--;
        if(timer == 0 &&agent.getType() == AgentType.MANAGEMENT){
            planner.setState(new Escaping(planner, agent));
        }
        if (agent.isGoalReached()) {
//            timer--;
//
//            if (timer == 0) {
//                planner.setState(new Default(planner, agent));
//            }
            currentGoals.clear();
//        currentGoals.addAll(planner.getEnvironmentKnowledge().getCorridors());
//        currentGoals.remove(agent.getCurrentAreaId());
              /*
             * Choose any one of the corridors as a goal for exploration
             */
            currentGoals.add((Integer) planner.getEnvironmentKnowledge().getCorridors().
                    toArray()[IbevacRNG.instance().nextInt(
                    planner.getEnvironmentKnowledge().getCorridors().size())]);
        }
    }

    @Override
    public boolean hasActions() {
        return true;
    }
}
