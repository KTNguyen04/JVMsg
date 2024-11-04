package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppConfig {
    private static final String configPath = "./src/config.properties";
    public static String appName;
    public static int appWidth;
    public static int appHeight;
    public static String bannerPath;
    public static int bannerWidth;
    public static int bannerHeight;

    AppConfig() {
    }

    static {
        readConfig();
    }

    static void readConfig() {
        try (FileInputStream fin = new FileInputStream(configPath)) {
            Properties props = new Properties();
            props.load(fin);
            appName = props.getProperty("app.title");
            appWidth = Integer.valueOf(props.getProperty("app.width"));
            appHeight = Integer.valueOf(props.getProperty("app.height"));
            bannerPath = props.getProperty("banner.path");
            bannerWidth = Integer.valueOf(props.getProperty("banner.width"));
            bannerHeight = Integer.valueOf(props.getProperty("banner.height"));

        } catch (IOException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
