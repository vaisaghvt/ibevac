/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ibevac.datatracker;


import sim.engine.Steppable;

/**
 *
 * @author vaisaghvt
 */
public interface DataTracker extends Steppable{
       
    public void storeToFile(); 
    
    public void storeToDatabase();
    
    public String trackerType();

}
