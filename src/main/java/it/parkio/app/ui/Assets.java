package it.parkio.app.ui;

import java.net.URL; // URL usato per caricare risorse dal classpath

public interface Assets { // interfaccia contenente riferimenti a risorse statiche

    String BASE_PATH = "/assets"; // percorso base delle risorse
    String ICONS_PATH = "/icons"; // sottocartella per le icone

    URL ACCESSIBILITY_ICON = getIcon("/accessibility.svg"); // URL dell'icona di accessibilità
    URL ZAP_ICON = getIcon("/zap.svg"); // URL dell'icona elettrica (zap)

    static URL getAsset(String path) { // ottiene l'URL di una risorsa generica
        return Assets.class.getResource(BASE_PATH + path); // carica la risorsa dal classpath
    }

    static URL getIcon(String path) { // ottiene l'URL di un'icona nella cartella icone
        return getAsset(ICONS_PATH + path); // costruisce path completo e chiama getAsset
    }

}