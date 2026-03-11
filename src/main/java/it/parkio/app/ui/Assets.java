package it.parkio.app.ui;

import java.net.URL;

/**
 * Contiene riferimenti centralizzati alle risorse grafiche dell'applicazione.
 *
 * <p>Usare un punto unico per i path evita stringhe duplicate nel codice
 * e rende più facile modificare la struttura delle risorse in futuro.</p>
 */
public interface Assets {

    /**
     * Cartella base delle risorse nel classpath.
     */
    String BASE_PATH = "/assets";

    /**
     * Sottocartella che contiene le icone SVG.
     */
    String ICONS_PATH = "/icons";

    /**
     * Icona relativa all'accessibilità/disabilità.
     */
    URL ACCESSIBILITY_ICON = getIcon("/accessibility.svg");

    /**
     * Icona usata per i posti elettrici.
     */
    URL ZAP_ICON = getIcon("/zap.svg");

    /**
     * Restituisce l'URL di una risorsa generica partendo dal path relativo.
     *
     * @param path percorso relativo sotto {@link #BASE_PATH}
     * @return URL della risorsa nel classpath
     */
    static URL getAsset(String path) {
        return Assets.class.getResource(BASE_PATH + path);
    }

    /**
     * Restituisce l'URL di un'icona SVG.
     *
     * @param path nome o percorso relativo dell'icona
     * @return URL dell'icona richiesta
     */
    static URL getIcon(String path) {
        return getAsset(ICONS_PATH + path);
    }

}