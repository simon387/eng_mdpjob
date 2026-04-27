
package it.csi.mdpjob.dto.gov;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ctIstitutoMittente complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ctIstitutoMittente">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="identificativoUnivocoMittente" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}ctIdentificativoUnivoco"/>
 *         &lt;element name="denominazioneMittente" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}stText70" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctIstitutoMittente", propOrder = {
    "identificativoUnivocoMittente",
    "denominazioneMittente"
})
public class CtIstitutoMittente {

    @XmlElement(required = true)
    protected CtIdentificativoUnivoco identificativoUnivocoMittente;
    protected String denominazioneMittente;

    /**
     * Gets the value of the identificativoUnivocoMittente property.
     * 
     * @return
     *     possible object is
     *     {@link CtIdentificativoUnivoco }
     *     
     */
    public CtIdentificativoUnivoco getIdentificativoUnivocoMittente() {
        return identificativoUnivocoMittente;
    }

    /**
     * Sets the value of the identificativoUnivocoMittente property.
     * 
     * @param value
     *     allowed object is
     *     {@link CtIdentificativoUnivoco }
     *     
     */
    public void setIdentificativoUnivocoMittente(CtIdentificativoUnivoco value) {
        this.identificativoUnivocoMittente = value;
    }

    /**
     * Gets the value of the denominazioneMittente property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDenominazioneMittente() {
        return denominazioneMittente;
    }

    /**
     * Sets the value of the denominazioneMittente property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDenominazioneMittente(String value) {
        this.denominazioneMittente = value;
    }

}
