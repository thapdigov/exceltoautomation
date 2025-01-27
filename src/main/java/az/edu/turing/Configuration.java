package az.edu.turing;

import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;
import java.util.Map;

public class Configuration {

    public static ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless");

        options.addArguments("window-size=1920,1080");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-software-rasterizer");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");

        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

        Map<String, Object> prefs = new HashMap<>();

        prefs.put("profile.managed_default_content_settings.images", 2);
        prefs.put("profile.managed_default_content_settings.stylesheets", 2);
        prefs.put("profile.managed_default_content_settings.plugins", 2);

        options.setExperimentalOption("prefs", prefs);

        return options;
    }
}
