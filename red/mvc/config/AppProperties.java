package red.mvc.config;

import java.io.InputStream;
import java.util.Properties;

public class AppProperties {
    private static String fileName = "/red.properties";
    private static Properties properties = new Properties();

    public static String getProperty(String key) {
        return properties.getProperty(key, null);
    }

    public static void loadProperties() throws Exception {
        try (InputStream stream = AppProperties.class.getResourceAsStream(fileName)) {
            if (stream == null) {
                System.out.println("NULL.");
                return;
            }
            System.out.println("TSY NULL !");
            properties.load(stream);
        }
    }
}
