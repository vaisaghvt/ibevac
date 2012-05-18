/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ibevac.cue;

import javax.vecmath.Point2d;

/**
 * Fire Alarm
 *
 * @author <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 * @version $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class FireAlarmCue implements Cue {

    final Point2d location;
    public static Ambiguity ambiguityLevel = Ambiguity.TEN;

    public FireAlarmCue(int x, int y) {
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
        return ambiguityLevel;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return getClass() == obj.getClass();
    }

    public int hashCode() {
        return 7;
    }
}
