
package it.csi.mdpjob.dto.gov;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ctIstitutoRicevente complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ctIstitutoRicevente">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="identificativoUnivocoRicevente" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}ctIdentificativoUnivocoPersonaG"/>
 *         &lt;element name="denominazioneRicevente" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}stText70" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctIstitutoRicevente", propOrder = {
    "identificativoUnivocoRicevente",
    "denominazioneRicevente"
})
public class CtIstitutoRicevente {

    @XmlElement(required = true)
    protected CtIdentificativoUnivocoPersonaG identificativoUnivocoRicevente;
    protected String denominazioneRicevente;

    /**
     * Gets the value of the identificativoUnivocoRicevente property.
     * 
     * @return
     *     possible object is
     *     {@link CtIdentificativoUnivocoPersonaG }
     *     
     */
    public CtIdentificativoUnivocoPersonaG getIdentificativoUnivocoRicevente() {
        return identificativoUnivocoRicevente;
    }

    /**
     * Sets the value of the identificativoUnivocoRicevente property.
     * 
     * @param value
     *     allowed object is
     *     {@link CtIdentificativoUnivocoPersonaG }
     *     
     */
    public void setIdentificativoUnivocoRicevente(CtIdentificativoUnivocoPersonaG value) {
        this.identificativoUnivocoRicevente = value;
    }

    /**
     * Gets the value of the denominazioneRicevente property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDenominazioneRicevente() {
        return denominazioneRicevente;
    }

    /**
     * Sets the value of the denominazioneRicevente property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDenominazioneRicevente(String value) {
        this.denominazioneRicevente = value;
    }

}
