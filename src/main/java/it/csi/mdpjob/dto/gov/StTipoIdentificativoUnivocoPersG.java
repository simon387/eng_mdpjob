
package it.csi.mdpjob.dto.gov;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for stTipoIdentificativoUnivocoPersG.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="stTipoIdentificativoUnivocoPersG">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="G"/>
 *     &lt;length value="1"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "stTipoIdentificativoUnivocoPersG")
@XmlEnum
public enum StTipoIdentificativoUnivocoPersG {

    G;

    public String value() {
        return name();
    }

    public static StTipoIdentificativoUnivocoPersG fromValue(String v) {
        return valueOf(v);
    }

}
