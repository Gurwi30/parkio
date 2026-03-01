package it.parkio.app.ui;

import java.net.URL;

public interface Assets {

    String BASE_PATH = "/assets";
    String ICONS_PATH = "/icons";

    URL ACCESSIBILITY_ICON = getIcon("/accessibility.svg");
    URL ZAP_ICON = getIcon("/zap.svg");

    static URL getAsset(String path) {
        return Assets.class.getResource(BASE_PATH + path);
    }

    static URL getIcon(String path) {
        return getAsset(ICONS_PATH + path);
    }

}
