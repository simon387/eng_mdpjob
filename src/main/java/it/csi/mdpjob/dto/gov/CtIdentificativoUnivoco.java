
package it.csi.mdpjob.dto.gov;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ctIdentificativoUnivoco complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ctIdentificativoUnivoco">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="tipoIdentificativoUnivoco" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}stTipoIdentificativoUnivoco"/>
 *         &lt;element name="codiceIdentificativoUnivoco" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}stText35"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctIdentificativoUnivoco", propOrder = {
    "tipoIdentificativoUnivoco",
    "codiceIdentificativoUnivoco"
})
public class CtIdentificativoUnivoco {

    @XmlElement(required = true)
    protected StTipoIdentificativoUnivoco tipoIdentificativoUnivoco;
    @XmlElement(required = true)
    protected String codiceIdentificativoUnivoco;

    /**
     * Gets the value of the tipoIdentificativoUnivoco property.
     * 
     * @return
     *     possible object is
     *     {@link StTipoIdentificativoUnivoco }
     *     
     */
    public StTipoIdentificativoUnivoco getTipoIdentificativoUnivoco() {
        return tipoIdentificativoUnivoco;
    }

    /**
     * Sets the value of the tipoIdentificativoUnivoco property.
     * 
     * @param value
     *     allowed object is
     *     {@link StTipoIdentificativoUnivoco }
     *     
     */
    public void setTipoIdentificativoUnivoco(StTipoIdentificativoUnivoco value) {
        this.tipoIdentificativoUnivoco = value;
    }

    /**
     * Gets the value of the codiceIdentificativoUnivoco property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodiceIdentificativoUnivoco() {
        return codiceIdentificativoUnivoco;
    }

    /**
     * Sets the value of the codiceIdentificativoUnivoco property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodiceIdentificativoUnivoco(String value) {
        this.codiceIdentificativoUnivoco = value;
    }

}
