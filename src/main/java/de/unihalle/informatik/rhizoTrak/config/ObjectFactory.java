//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.03.14 um 08:10:59 PM CET 
//


package de.unihalle.informatik.rhizoTrak.config;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.unihalle.informatik.rhizoTrak.config package. 
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


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.unihalle.informatik.rhizoTrak.config
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Config }
     * 
     */
    public Config createConfig() {
        return new Config();
    }

    /**
     * Create an instance of {@link GlobalSettings }
     * 
     */
    public GlobalSettings createGlobalSettings() {
        return new GlobalSettings();
    }

    /**
     * Create an instance of {@link GlobalSettings.GlobalStatusList }
     * 
     */
    public GlobalSettings.GlobalStatusList createGlobalSettingsGlobalStatusList() {
        return new GlobalSettings.GlobalStatusList();
    }

    /**
     * Create an instance of {@link Config.StatusList }
     * 
     */
    public Config.StatusList createConfigStatusList() {
        return new Config.StatusList();
    }

    /**
     * Create an instance of {@link GlobalSettings.GlobalStatusList.GlobalStatus }
     * 
     */
    public GlobalSettings.GlobalStatusList.GlobalStatus createGlobalSettingsGlobalStatusListGlobalStatus() {
        return new GlobalSettings.GlobalStatusList.GlobalStatus();
    }

    /**
     * Create an instance of {@link Config.StatusList.Status }
     * 
     */
    public Config.StatusList.Status createConfigStatusListStatus() {
        return new Config.StatusList.Status();
    }

}
