
package it.csi.mdpjob.dto.gov;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for stTipoIdentificativoUnivoco.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="stTipoIdentificativoUnivoco">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="G"/>
 *     &lt;enumeration value="A"/>
 *     &lt;enumeration value="B"/>
 *     &lt;length value="1"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "stTipoIdentificativoUnivoco")
@XmlEnum
public enum StTipoIdentificativoUnivoco {

    G,
    A,
    B;

    public String value() {
        return name();
    }

    public static StTipoIdentificativoUnivoco fromValue(String v) {
        return valueOf(v);
    }

}
