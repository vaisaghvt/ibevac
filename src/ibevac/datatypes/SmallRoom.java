/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ibevac.datatypes;

import javax.vecmath.Point2d;

/**
 * @author vaisagh
 */
public class SmallRoom {

    final double length;
    final double width;
    final Point2d center;
    final CArea area;
    final int floor;
    private final double mnx;
    private final double mxx;
    private final double mny;
    private final double mxy;
    private final int hashcode;

    public SmallRoom(double length, double width, Point2d center, CArea area, int floor) {
        this.length = length;
        this.width = width;
        this.center = new Point2d(center);
        this.area = area;
        this.floor = floor;


        mnx = Math.floor(center.x - width / 2);
        mny = Math.floor(center.y - length / 2);
        mxx = Math.ceil(center.x + width / 2);
        mxy = Math.ceil(center.y + length / 2);
        int hash = 7;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.length) ^ (Double.doubleToLongBits(this.length) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.width) ^ (Double.doubleToLongBits(this.width) >>> 32));
        hash = 97 * hash + this.center.hashCode();
        hash = 97 * hash + this.area.hashCode();
        hash = 97 * hash + this.floor;


        hashcode = hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SmallRoom other = (SmallRoom) obj;
        if (Double.doubleToLongBits(this.length) != Double.doubleToLongBits(other.length)) {
            return false;
        }
        if (Double.doubleToLongBits(this.width) != Double.doubleToLongBits(other.width)) {
            return false;
        }
        if (!this.center.equals(other.center)) {
            return false;
        }
        return this.area.equals(other.area);
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    @Override
    public String toString() {
        return "SmallRoom{" + "length=" + length + ", width=" + width + ", center=" + center + ", area=" + area + '}';
    }

    public boolean contains(Point2d location) {

        return location.x >= mnx && location.x <= mxx
                && location.y >= mny && location.y <= mxy;
    }

    public Integer getAreaId() {
        return this.area.getId();
    }
}
