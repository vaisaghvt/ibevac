package ibevac.agent.planner;

import ibevac.agent.planner.states.State;
import ibevac.agent.planner.states.Dead;
import ibevac.agent.planner.states.Default;
import ibevac.agent.planner.states.Safe;
import ibevac.agent.IbevacAgent;
import ibevac.agent.knowledge.event.EventKnowledgeModule;
import ibevac.agent.knowledge.environment.EnvironmentKnowledgeModule;

import ibevac.agent.knowledge.event.PhaseBucket.Phase;
import ibevac.agent.navigation.level1motion.SimpleRVO2;
import java.util.Collection;
import javax.vecmath.Point2d;

/**
 * <h4>The default planning module where the agents show default kinds of behavior only.
 * </h4>
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class DefaultPlanner implements Planner {

    //TODO Think about how different planners will have different strategies.
//    public static enum State {
//
//        DEFAULT, INVESTIGATING, ESCAPING, SAFE, TRAPPED, DEAD
//    };
    private State state;
    private EventKnowledgeModule eventKnowledge;
    private EnvironmentKnowledgeModule environmentKnowledge;
    private IbevacAgent agent;
    private Point2d currentGoal;

    public DefaultPlanner(IbevacAgent agent, EventKnowledgeModule eventKnowledge,
            EnvironmentKnowledgeModule environmentKnowledge) {
//		this.eventKnowledge = eventKnowledge;
        this.environmentKnowledge = environmentKnowledge;
        this.eventKnowledge = eventKnowledge;
        this.agent = agent;

        state = new Default(this, agent);
//        state = State.ESCAPING;
    }


    @Override
    public Collection<Integer> getCurrentGoalAreaIds() {
        assert state.getCurrentGoals() != null;
        return state.getCurrentGoals();
    }

    @Override
    public void transitionToDead() {
        state = new Dead(this, agent);
    }

    @Override
    public void transitionToSafe() {
        agent.updateEvacuationTime();
        state = new Safe(this, agent);
    }

    @Override
    public boolean hasActions() {
        return state.hasActions();
    }

    @Override
    public void executeActions() {
        state.executeActions();
    }

    @Override
    public State getState() {
        return this.state;
    }

    @Override
    public void informAbout(Phase phase) {
        State previousState = this.state;
        setState(state.handlePhaseChange(phase));
        State newState = this.state;
        if(!previousState.getClass().equals(newState.getClass())){
            this.agent.getLevel1MotionPlanning().reset();
            ((SimpleRVO2)(agent.getLevel1MotionPlanning())).goalReached = false;
             
        }
    }

    @Override
    public EnvironmentKnowledgeModule getEnvironmentKnowledge() {
        return this.environmentKnowledge;
    }

    @Override
    public void requestCompleteReset() {
        eventKnowledge.resetAllPhases();
    }

    @Override
    public void setState(State state) {
        this.state = state;
       

    }

    @Override
    public void setChosenGoal(Point2d point) {
        this.currentGoal = point;
    }

    @Override
    public Point2d getChosenGoal() {
        return this.currentGoal;
    }
}
