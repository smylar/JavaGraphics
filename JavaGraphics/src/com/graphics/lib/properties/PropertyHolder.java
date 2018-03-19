package com.graphics.lib.properties;

import java.util.Optional;
import java.util.Properties;

/**
 * Read in properties from the application.properties resource file
 * 
 * @author paul.brandon
 *
 */
public class PropertyHolder {
    private static Properties props = new Properties();
    
    static {
        try {
            props.load(PropertyHolder.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (Exception e) {
            System.out.println("No property file, will use defaults");
            e.printStackTrace();
        }
    }
    
    private PropertyHolder() { }
    
    public static Optional<String> getProperty(String key) {
        return Optional.ofNullable(props.getProperty(key));
    }
}
