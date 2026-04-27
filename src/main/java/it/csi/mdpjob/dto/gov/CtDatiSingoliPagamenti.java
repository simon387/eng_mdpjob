
package it.csi.mdpjob.dto.gov;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;


/**
 * <p>Java class for ctDatiSingoliPagamenti complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ctDatiSingoliPagamenti">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="identificativoUnivocoVersamento" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}stText35"/>
 *         &lt;element name="identificativoUnivocoRiscossione" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}stText35"/>
 *         &lt;element name="indiceDatiSingoloPagamento" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}stIndice" minOccurs="0"/>
 *         &lt;element name="singoloImportoPagato" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}stImporto"/>
 *         &lt;element name="codiceEsitoSingoloPagamento" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}stCodiceEsitoPagamento"/>
 *         &lt;element name="dataEsitoSingoloPagamento" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}stISODate"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctDatiSingoliPagamenti", propOrder = {
    "identificativoUnivocoVersamento",
    "identificativoUnivocoRiscossione",
    "indiceDatiSingoloPagamento",
    "singoloImportoPagato",
    "codiceEsitoSingoloPagamento",
    "dataEsitoSingoloPagamento"
})
public class CtDatiSingoliPagamenti {

    @XmlElement(required = true)
    protected String identificativoUnivocoVersamento;
    @XmlElement(required = true)
    protected String identificativoUnivocoRiscossione;
    protected Integer indiceDatiSingoloPagamento;
    @XmlElement(required = true)
    protected BigDecimal singoloImportoPagato;
    @XmlElement(required = true)
    protected String codiceEsitoSingoloPagamento;
    @XmlElement(required = true)
    protected XMLGregorianCalendar dataEsitoSingoloPagamento;

    /**
     * Gets the value of the identificativoUnivocoVersamento property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentificativoUnivocoVersamento() {
        return identificativoUnivocoVersamento;
    }

    /**
     * Sets the value of the identificativoUnivocoVersamento property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentificativoUnivocoVersamento(String value) {
        this.identificativoUnivocoVersamento = value;
    }

    /**
     * Gets the value of the identificativoUnivocoRiscossione property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentificativoUnivocoRiscossione() {
        return identificativoUnivocoRiscossione;
    }

    /**
     * Sets the value of the identificativoUnivocoRiscossione property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentificativoUnivocoRiscossione(String value) {
        this.identificativoUnivocoRiscossione = value;
    }

    /**
     * Gets the value of the indiceDatiSingoloPagamento property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getIndiceDatiSingoloPagamento() {
        return indiceDatiSingoloPagamento;
    }

    /**
     * Sets the value of the indiceDatiSingoloPagamento property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setIndiceDatiSingoloPagamento(Integer value) {
        this.indiceDatiSingoloPagamento = value;
    }

    /**
     * Gets the value of the singoloImportoPagato property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getSingoloImportoPagato() {
        return singoloImportoPagato;
    }

    /**
     * Sets the value of the singoloImportoPagato property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setSingoloImportoPagato(BigDecimal value) {
        this.singoloImportoPagato = value;
    }

    /**
     * Gets the value of the codiceEsitoSingoloPagamento property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodiceEsitoSingoloPagamento() {
        return codiceEsitoSingoloPagamento;
    }

    /**
     * Sets the value of the codiceEsitoSingoloPagamento property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodiceEsitoSingoloPagamento(String value) {
        this.codiceEsitoSingoloPagamento = value;
    }

    /**
     * Gets the value of the dataEsitoSingoloPagamento property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDataEsitoSingoloPagamento() {
        return dataEsitoSingoloPagamento;
    }

    /**
     * Sets the value of the dataEsitoSingoloPagamento property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDataEsitoSingoloPagamento(XMLGregorianCalendar value) {
        this.dataEsitoSingoloPagamento = value;
    }

}
