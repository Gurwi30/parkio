package it.parkio.app.ui;

import java.net.URL;

public interface Assets {

    String BASE_PATH = "/assets";
    String ICONS_PATH = "/icons";

    URL ACCESSIBILITY_ICON = Assets.class.getResource(BASE_PATH + ICONS_PATH + "/accessibility.svg");
    URL ZAP_ICON = Assets.class.getResource(BASE_PATH + ICONS_PATH + "/zap.svg");

}
