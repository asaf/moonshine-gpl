package org.atteo.moonshine.titan;

import org.atteo.config.Configurable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Titan Schema Config
 */
public class SchemaConfig extends Configurable{
    @XmlElement(name="default")
    private String schemaDefault;

    public String getSchemaDefault() {
        return schemaDefault;
    }
}
