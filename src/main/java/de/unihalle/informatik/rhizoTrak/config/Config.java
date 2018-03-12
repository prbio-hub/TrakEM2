//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.03.12 um 10:04:37 AM CET 
//


package de.unihalle.informatik.rhizoTrak.config;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="statusList"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="status" maxOccurs="unbounded"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="fullName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                             &lt;element name="abbreviation" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                             &lt;element name="red" type="{http://www.w3.org/2001/XMLSchema}integer"/&gt;
 *                             &lt;element name="green" type="{http://www.w3.org/2001/XMLSchema}integer"/&gt;
 *                             &lt;element name="blue" type="{http://www.w3.org/2001/XMLSchema}integer"/&gt;
 *                             &lt;element name="alpha" type="{http://www.w3.org/2001/XMLSchema}integer"/&gt;
 *                             &lt;element name="selectable" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "statusList"
})
@XmlRootElement(name = "config")
public class Config {

    @XmlElement(required = true)
    protected Config.StatusList statusList;

    /**
     * Ruft den Wert der statusList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Config.StatusList }
     *     
     */
    public Config.StatusList getStatusList() {
        return statusList;
    }

    /**
     * Legt den Wert der statusList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Config.StatusList }
     *     
     */
    public void setStatusList(Config.StatusList value) {
        this.statusList = value;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="status" maxOccurs="unbounded"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="fullName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *                   &lt;element name="abbreviation" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *                   &lt;element name="red" type="{http://www.w3.org/2001/XMLSchema}integer"/&gt;
     *                   &lt;element name="green" type="{http://www.w3.org/2001/XMLSchema}integer"/&gt;
     *                   &lt;element name="blue" type="{http://www.w3.org/2001/XMLSchema}integer"/&gt;
     *                   &lt;element name="alpha" type="{http://www.w3.org/2001/XMLSchema}integer"/&gt;
     *                   &lt;element name="selectable" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "status"
    })
    public static class StatusList {

        @XmlElement(required = true)
        protected List<Config.StatusList.Status> status;

        /**
         * Gets the value of the status property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the status property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getStatus().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Config.StatusList.Status }
         * 
         * 
         */
        public List<Config.StatusList.Status> getStatus() {
            if (status == null) {
                status = new ArrayList<Config.StatusList.Status>();
            }
            return this.status;
        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;sequence&gt;
         *         &lt;element name="fullName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
         *         &lt;element name="abbreviation" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
         *         &lt;element name="red" type="{http://www.w3.org/2001/XMLSchema}integer"/&gt;
         *         &lt;element name="green" type="{http://www.w3.org/2001/XMLSchema}integer"/&gt;
         *         &lt;element name="blue" type="{http://www.w3.org/2001/XMLSchema}integer"/&gt;
         *         &lt;element name="alpha" type="{http://www.w3.org/2001/XMLSchema}integer"/&gt;
         *         &lt;element name="selectable" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
         *       &lt;/sequence&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "fullName",
            "abbreviation",
            "red",
            "green",
            "blue",
            "alpha",
            "selectable"
        })
        public static class Status {

            @XmlElement(required = true)
            protected String fullName;
            @XmlElement(required = true)
            protected String abbreviation;
            @XmlElement(required = true)
            protected BigInteger red;
            @XmlElement(required = true)
            protected BigInteger green;
            @XmlElement(required = true)
            protected BigInteger blue;
            @XmlElement(required = true)
            protected BigInteger alpha;
            protected boolean selectable;

            /**
             * Ruft den Wert der fullName-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getFullName() {
                return fullName;
            }

            /**
             * Legt den Wert der fullName-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setFullName(String value) {
                this.fullName = value;
            }

            /**
             * Ruft den Wert der abbreviation-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getAbbreviation() {
                return abbreviation;
            }

            /**
             * Legt den Wert der abbreviation-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setAbbreviation(String value) {
                this.abbreviation = value;
            }

            /**
             * Ruft den Wert der red-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link BigInteger }
             *     
             */
            public BigInteger getRed() {
                return red;
            }

            /**
             * Legt den Wert der red-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *     
             */
            public void setRed(BigInteger value) {
                this.red = value;
            }

            /**
             * Ruft den Wert der green-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link BigInteger }
             *     
             */
            public BigInteger getGreen() {
                return green;
            }

            /**
             * Legt den Wert der green-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *     
             */
            public void setGreen(BigInteger value) {
                this.green = value;
            }

            /**
             * Ruft den Wert der blue-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link BigInteger }
             *     
             */
            public BigInteger getBlue() {
                return blue;
            }

            /**
             * Legt den Wert der blue-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *     
             */
            public void setBlue(BigInteger value) {
                this.blue = value;
            }

            /**
             * Ruft den Wert der alpha-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link BigInteger }
             *     
             */
            public BigInteger getAlpha() {
                return alpha;
            }

            /**
             * Legt den Wert der alpha-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *     
             */
            public void setAlpha(BigInteger value) {
                this.alpha = value;
            }

            /**
             * Ruft den Wert der selectable-Eigenschaft ab.
             * 
             */
            public boolean isSelectable() {
                return selectable;
            }

            /**
             * Legt den Wert der selectable-Eigenschaft fest.
             * 
             */
            public void setSelectable(boolean value) {
                this.selectable = value;
            }

        }

    }

}
