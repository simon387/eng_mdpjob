
package it.csi.mdpjob.dto.gov;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for ctFlussoRiversamento complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ctFlussoRiversamento">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="versioneOggetto" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}stVersioneOggetto"/>
 *         &lt;element name="identificativoFlusso" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}stText35"/>
 *         &lt;element name="dataOraFlusso" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}stISODateTime"/>
 *         &lt;element name="identificativoUnivocoRegolamento" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}stText35"/>
 *         &lt;element name="dataRegolamento" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}stISODate"/>
 *         &lt;element name="istitutoMittente" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}ctIstitutoMittente"/>
 *         &lt;element name="codiceBicBancaDiRiversamento" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}stText35" minOccurs="0"/>
 *         &lt;element name="istitutoRicevente" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}ctIstitutoRicevente"/>
 *         &lt;element name="numeroTotalePagamenti" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}stNumeroTotalePagamenti"/>
 *         &lt;element name="importoTotalePagamenti" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}stImportoTotalePagamenti"/>
 *         &lt;element name="datiSingoliPagamenti" type="{http://www.digitpa.gov.it/schemas/2011/Pagamenti/}ctDatiSingoliPagamenti" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctFlussoRiversamento", propOrder = {
    "versioneOggetto",
    "identificativoFlusso",
    "dataOraFlusso",
    "identificativoUnivocoRegolamento",
    "dataRegolamento",
    "istitutoMittente",
    "codiceBicBancaDiRiversamento",
    "istitutoRicevente",
    "numeroTotalePagamenti",
    "importoTotalePagamenti",
    "datiSingoliPagamenti"
})
public class CtFlussoRiversamento {

    @XmlElement(required = true)
    protected String versioneOggetto;
    @XmlElement(required = true)
    protected String identificativoFlusso;
    @XmlElement(required = true)
    protected XMLGregorianCalendar dataOraFlusso;
    @XmlElement(required = true)
    protected String identificativoUnivocoRegolamento;
    @XmlElement(required = true)
    protected XMLGregorianCalendar dataRegolamento;
    @XmlElement(required = true)
    protected CtIstitutoMittente istitutoMittente;
    protected String codiceBicBancaDiRiversamento;
    @XmlElement(required = true)
    protected CtIstitutoRicevente istitutoRicevente;
    @XmlElement(required = true)
    protected BigDecimal numeroTotalePagamenti;
    @XmlElement(required = true)
    protected BigDecimal importoTotalePagamenti;
    @XmlElement(required = true)
    protected List<CtDatiSingoliPagamenti> datiSingoliPagamenti;

    /**
     * Gets the value of the versioneOggetto property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersioneOggetto() {
        return versioneOggetto;
    }

    /**
     * Sets the value of the versioneOggetto property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersioneOggetto(String value) {
        this.versioneOggetto = value;
    }

    /**
     * Gets the value of the identificativoFlusso property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentificativoFlusso() {
        return identificativoFlusso;
    }

    /**
     * Sets the value of the identificativoFlusso property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentificativoFlusso(String value) {
        this.identificativoFlusso = value;
    }

    /**
     * Gets the value of the dataOraFlusso property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDataOraFlusso() {
        return dataOraFlusso;
    }

    /**
     * Sets the value of the dataOraFlusso property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDataOraFlusso(XMLGregorianCalendar value) {
        this.dataOraFlusso = value;
    }

    /**
     * Gets the value of the identificativoUnivocoRegolamento property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentificativoUnivocoRegolamento() {
        return identificativoUnivocoRegolamento;
    }

    /**
     * Sets the value of the identificativoUnivocoRegolamento property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentificativoUnivocoRegolamento(String value) {
        this.identificativoUnivocoRegolamento = value;
    }

    /**
     * Gets the value of the dataRegolamento property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDataRegolamento() {
        return dataRegolamento;
    }

    /**
     * Sets the value of the dataRegolamento property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDataRegolamento(XMLGregorianCalendar value) {
        this.dataRegolamento = value;
    }

    /**
     * Gets the value of the istitutoMittente property.
     * 
     * @return
     *     possible object is
     *     {@link CtIstitutoMittente }
     *     
     */
    public CtIstitutoMittente getIstitutoMittente() {
        return istitutoMittente;
    }

    /**
     * Sets the value of the istitutoMittente property.
     * 
     * @param value
     *     allowed object is
     *     {@link CtIstitutoMittente }
     *     
     */
    public void setIstitutoMittente(CtIstitutoMittente value) {
        this.istitutoMittente = value;
    }

    /**
     * Gets the value of the codiceBicBancaDiRiversamento property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodiceBicBancaDiRiversamento() {
        return codiceBicBancaDiRiversamento;
    }

    /**
     * Sets the value of the codiceBicBancaDiRiversamento property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodiceBicBancaDiRiversamento(String value) {
        this.codiceBicBancaDiRiversamento = value;
    }

    /**
     * Gets the value of the istitutoRicevente property.
     * 
     * @return
     *     possible object is
     *     {@link CtIstitutoRicevente }
     *     
     */
    public CtIstitutoRicevente getIstitutoRicevente() {
        return istitutoRicevente;
    }

    /**
     * Sets the value of the istitutoRicevente property.
     * 
     * @param value
     *     allowed object is
     *     {@link CtIstitutoRicevente }
     *     
     */
    public void setIstitutoRicevente(CtIstitutoRicevente value) {
        this.istitutoRicevente = value;
    }

    /**
     * Gets the value of the numeroTotalePagamenti property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getNumeroTotalePagamenti() {
        return numeroTotalePagamenti;
    }

    /**
     * Sets the value of the numeroTotalePagamenti property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setNumeroTotalePagamenti(BigDecimal value) {
        this.numeroTotalePagamenti = value;
    }

    /**
     * Gets the value of the importoTotalePagamenti property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getImportoTotalePagamenti() {
        return importoTotalePagamenti;
    }

    /**
     * Sets the value of the importoTotalePagamenti property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setImportoTotalePagamenti(BigDecimal value) {
        this.importoTotalePagamenti = value;
    }

    /**
     * Gets the value of the datiSingoliPagamenti property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the datiSingoliPagamenti property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDatiSingoliPagamenti().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CtDatiSingoliPagamenti }
     * 
     * 
     */
    public List<CtDatiSingoliPagamenti> getDatiSingoliPagamenti() {
        if (datiSingoliPagamenti == null) {
            datiSingoliPagamenti = new ArrayList<CtDatiSingoliPagamenti>();
        }
        return this.datiSingoliPagamenti;
    }

}
