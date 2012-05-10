/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ibevac.datatypes;


import org.jgrapht.graph.DefaultWeightedEdge;

/**
 *
 * @author vaisagh
 */
public class RoomEdge extends DefaultWeightedEdge {

    CArea room;
    CArea room2;
    CLink link1;
    CLink link2;
    double weight;

    public RoomEdge(CArea room, CLink link1, CLink link2) {
        this.room = room;
        this.link1 = link1;
        this.link2 = link2;
        assert link1 != null && link2 != null;
        weight = link1.getCenter().distance(link2.getCenter());
//        throw new UnsupportedOperationException("Not yet implemented");
    }

    public RoomEdge(CArea room1, CArea room2, CLink link1, CLink link2) {
        this.room = room1;
        this.room2 = room2;
        this.link1 = link1;
        this.link2 = link2;
        assert link1 != null && link2 != null;
        weight = link1.getCenter().distance(room1.getCenter())+room2.getCenter().distance(link2.getCenter());
//        throw new UnsupportedOperationException("Not yet implemented");
    }

    public RoomEdge(CExit exit) {
        link1 = exit;
        this.room = new CRoom();
        link2 = null;
        weight = 0;
    }

    @Override
    protected double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        String roomId = (room!=null)?new Integer(room.getId()).toString():new String();
        String link1Id  = (link1!=null)?new Integer(link1.getId()).toString():new String();
        String link2Id  = (link2!=null)?new Integer(link2.getId()).toString():new String();
        return "RoomEdge{" + "link1=" + link1Id +", room=" + roomId +  ", link2=" + link2Id+'}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RoomEdge other = (RoomEdge) obj;
        if (!this.room.equals(other.room)) {
            return false;
        }
        if (!this.link1.equals(other.link1)) {
            return false;
        }
        if (!this.link2.equals(other.link2)) {
            return false;
        }
        if (Double.doubleToLongBits(this.weight) != Double.doubleToLongBits(other.weight)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + this.room.hashCode();
        hash = 23 * hash + this.link1.hashCode();
        hash = 23 * hash + this.link2.hashCode();
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.weight) ^ (Double.doubleToLongBits(this.weight) >>> 32));
        return hash;
    }

    public int id0() {
        return link1.id;
    }

    public int id1() {
        return link2.id;
    }

    public int areaId() {
        return room.id;
    }

    public CArea area() {
        return room;
    }

    public CArea getRoom0() {
        return room;
    }

    public CArea getRoom1() {
        return room2;
    }
    
}
