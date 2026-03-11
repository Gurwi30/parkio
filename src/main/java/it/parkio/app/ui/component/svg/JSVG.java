package it.parkio.app.ui.component.svg;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.SVGLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.util.Objects;

/**
 * Componente Swing che visualizza un'icona SVG scalata all'interno di un JComponent.
 *
 * <p>Un SVG (Scalable Vector Graphics) è un formato di immagine vettoriale: a differenza
 * di PNG o JPEG, non è fatto di pixel ma di forme geometriche descritte matematicamente,
 * quindi si ridimensiona senza perdere qualità.</p>
 *
 * <p>Questa classe carica un file SVG, ne estrae la forma geometrica (Shape)
 * e la ridisegna ogni volta scalata per occupare esattamente lo spazio disponibile.</p>
 */
public class JSVG extends JComponent {

    /**
     * Loader condiviso da tutte le istanze di JSVG.
     * È statico (appartiene alla classe, non alle singole istanze) per non crearne uno
     * per ogni icona SVG caricata, risparmiando memoria.
     */
    private static final SVGLoader SVG_LOADER = new SVGLoader();

    /** Il documento SVG caricato, che contiene tutte le informazioni dell'immagine. */
    private final SVGDocument svgDocument;

    /**
     * La forma geometrica estratta dall'SVG.
     * Shape è un'interfaccia Java che rappresenta qualsiasi figura geometrica
     * (rettangolo, ellisse, percorso complesso…).
     * Viene calcolata una volta sola alla creazione per non rifarlo ad ogni ridisegno.
     */
    private final Shape shape;

    /**
     * Colore opzionale da usare per riempire la forma.
     * Se null, si usa il colore di sfondo del componente (getBackground()).
     */
    private Color color = null;

    /**
     * Costruttore privato: si crea un JSVG solo tramite il metodo statico {@link #from(URL)}.
     * Questo pattern (costruttore privato + factory method statico) permette di dare
     * un nome più descrittivo alla creazione e di gestire eventuali errori in un unico posto.
     *
     * @param svgDocument il documento SVG già caricato
     */
    private JSVG(@NotNull SVGDocument svgDocument) {
        this.svgDocument = svgDocument;
        // Calcoliamo la Shape una volta sola: è costosa da ricalcolare e non cambia mai
        this.shape = svgDocument.computeShape();
    }

    /**
     * Carica un file SVG dall'URL indicato e restituisce il componente pronto all'uso.
     *
     * <p>L'URL di solito punta a una risorsa nel classpath (es. un file nella cartella resources),
     * ottenuta con {@code MyClass.class.getResource("/icons/myicon.svg")}.</p>
     *
     * @param resourcePath l'URL del file SVG da caricare
     * @return il componente JSVG pronto all'uso
     * @throws NullPointerException se il file non viene trovato o non è un SVG valido
     */
    public static @NotNull JSVG from(@NotNull URL resourcePath) {
        // requireNonNull lancia NullPointerException se load() restituisce null
        // (ovvero se il file non esiste o non è un SVG valido)
        return new JSVG(Objects.requireNonNull(SVG_LOADER.load(resourcePath)));
    }

    /**
     * Carica un file SVG e restituisce solo la sua forma geometrica, senza creare un componente.
     * Utile quando serve la forma per calcolare aree di click o collision detection sulla mappa,
     * non per visualizzare direttamente l'SVG.
     *
     * @param url l'URL del file SVG
     * @return la forma geometrica dell'SVG
     */
    public static @NotNull Shape getShapeFromSVG(@NotNull URL url) {
        return Objects.requireNonNull(SVG_LOADER.load(url)).computeShape();
    }

    /**
     * Disegna il componente: scala la forma SVG per adattarla alle dimensioni correnti
     * e la riempie con il colore impostato.
     *
     * <p>Swing chiama questo metodo automaticamente ogni volta che il componente
     * deve essere ridisegnato (al ridimensionamento, alla prima visualizzazione, ecc.).</p>
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // chiamiamo il metodo del genitore per gestire l'opacità ecc.

        Graphics2D g2 = (Graphics2D) g.create(); // copia del contesto per non modificare l'originale

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    RenderingHints.VALUE_ANTIALIAS_ON);
        // STROKE_PURE: usa coordinate sub-pixel per contorni più precisi (meno "scalettatura")
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,  RenderingHints.VALUE_STROKE_PURE);

        // getBounds() restituisce il rettangolo che contiene tutta la forma SVG
        // (posizione e dimensioni originali, prima di qualsiasi scaling)
        Rectangle bounds = shape.getBounds();

        // Calcoliamo il fattore di scala: il minimo tra scala orizzontale e verticale
        // garantisce che la forma entri completamente nel componente senza distorcersi
        // (es. se il componente è 100×50 e la forma è 200×200, la scala sarà 0.25)
        double scale = Math.min(
                getWidth()  / (double) bounds.width,
                getHeight() / (double) bounds.height
        );

        // AffineTransform è una matrice di trasformazione 2D: può scalare, ruotare, traslare
        AffineTransform at = new AffineTransform();
        at.scale(scale, scale); // prima scala
        // Poi trasla: se la forma non inizia da (0,0) nell'SVG originale,
        // la spostiamo all'origine per evitare che appaia "spostata" nel componente
        at.translate(-bounds.x, -bounds.y);

        // Applichiamo la trasformazione alla forma originale per ottenere quella scalata
        Shape transformed = at.createTransformedShape(shape);

        // Usiamo il colore impostato esplicitamente, oppure il colore di sfondo del componente
        if (color != null) g2.setColor(color);
        else               g2.setColor(getBackground());

        g2.fill(transformed); // riempiamo la forma con il colore scelto

        g2.dispose(); // liberiamo le risorse grafiche
    }

    /**
     * Imposta il colore di riempimento dell'SVG e restituisce this.
     * Restituire this permette il "method chaining":
     * {@code jsvg.setColor(Color.RED).setPreferredSize(...)}
     *
     * @param color il colore da usare (null = usa getBackground())
     * @return this, per permettere chiamate concatenate
     */
    public JSVG setColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     * Restituisce il documento SVG originale caricato.
     * Può essere utile per operazioni avanzate sulla libreria jsvg.
     *
     * @return il documento SVG
     */
    public SVGDocument getSVGDocument() {
        return svgDocument;
    }

    /**
     * Restituisce la forma geometrica calcolata dall'SVG.
     * Può essere usata per hit-testing (capire se un click ha colpito l'icona)
     * o per calcolare bounds personalizzati.
     *
     * @return la Shape dell'SVG
     */
    public Shape getShape() {
        return shape;
    }

}