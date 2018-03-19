package com.graphics.lib.properties;

import java.io.IOException;
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
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private PropertyHolder() { }
    
    public static Optional<String> getProperty(String key) {
        return Optional.ofNullable(props.getProperty(key));
    }
}
