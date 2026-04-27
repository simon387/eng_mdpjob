
package it.csi.mdpjob.dto.gov;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the it.csi.epay.epaypaweb.facade.rendicontazione.dto.gov package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _FlussoRiversamento_QNAME = new QName("http://www.digitpa.gov.it/schemas/2011/Pagamenti/", "FlussoRiversamento");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: it.csi.epay.epaypaweb.facade.rendicontazione.dto.gov
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CtDatiSingoliPagamenti }
     * 
     */
    public CtDatiSingoliPagamenti createCtDatiSingoliPagamenti() {
        return new CtDatiSingoliPagamenti();
    }

    /**
     * Create an instance of {@link CtIdentificativoUnivocoPersonaG }
     * 
     */
    public CtIdentificativoUnivocoPersonaG createCtIdentificativoUnivocoPersonaG() {
        return new CtIdentificativoUnivocoPersonaG();
    }

    /**
     * Create an instance of {@link CtFlussoRiversamento }
     * 
     */
    public CtFlussoRiversamento createCtFlussoRiversamento() {
        return new CtFlussoRiversamento();
    }

    /**
     * Create an instance of {@link CtIdentificativoUnivoco }
     * 
     */
    public CtIdentificativoUnivoco createCtIdentificativoUnivoco() {
        return new CtIdentificativoUnivoco();
    }

    /**
     * Create an instance of {@link CtIstitutoMittente }
     * 
     */
    public CtIstitutoMittente createCtIstitutoMittente() {
        return new CtIstitutoMittente();
    }

    /**
     * Create an instance of {@link CtIstitutoRicevente }
     * 
     */
    public CtIstitutoRicevente createCtIstitutoRicevente() {
        return new CtIstitutoRicevente();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CtFlussoRiversamento }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.digitpa.gov.it/schemas/2011/Pagamenti/", name = "FlussoRiversamento")
    public JAXBElement<CtFlussoRiversamento> createFlussoRiversamento(CtFlussoRiversamento value) {
        return new JAXBElement<CtFlussoRiversamento>(_FlussoRiversamento_QNAME, CtFlussoRiversamento.class, null, value);
    }

}
