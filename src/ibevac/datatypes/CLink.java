//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.08.04 at 03:00:45 PM SGT 
//
package ibevac.datatypes;

import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point2d;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for CLink complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CLink">
 *   &lt;complexContent>
 *     &lt;extension base="{http://datatypes.evac}CArea">
 *       &lt;sequence>
 *         &lt;element name="connectingAreas" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded"/>
 *         &lt;element name="orientation" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CLink", propOrder = {
    "connectingAreas",
    "orientation"
})
@XmlSeeAlso({
    CExit.class
})
public class CLink
        extends CArea {

    @XmlElement(type = Integer.class)
    protected List<Integer> connectingAreas;
    protected int orientation;

    /**
     * Gets the value of the connectingAreas property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the connectingAreas property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConnectingAreas().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getConnectingAreas() {
        if (connectingAreas == null) {
            connectingAreas = new ArrayList<Integer>();
        }
        return this.connectingAreas;
    }

    /**
     * Gets the value of the orientation property.
     * 
     */
    public int getOrientation() {
        return orientation;
    }

    /**
     * Sets the value of the orientation property.
     * 
     */
    public void setOrientation(int value) {
        this.orientation = value;
    }

    public Point2d getCenter() {
        return new Point2d(
                (this.corner0.x + this.corner1.x) / 2.0, 
                (this.corner0.y + this.corner1.y) / 2.0);
    }
}
