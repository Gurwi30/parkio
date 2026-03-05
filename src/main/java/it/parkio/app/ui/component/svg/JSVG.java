package it.parkio.app.ui.component.svg;

import com.github.weisj.jsvg.SVGDocument; // rappresenta un documento SVG caricato
import com.github.weisj.jsvg.parser.SVGLoader; // parser per caricare SVG da URL
import org.jetbrains.annotations.NotNull;

import javax.swing.*; // JComponent
import java.awt.*; // Graphics, Color, Shape, AffineTransform
import java.awt.geom.AffineTransform;
import java.net.URL; // per caricare SVG da risorse
import java.util.Objects; // per requireNonNull

public class JSVG extends JComponent { // componente Swing per visualizzare SVG

    private static final SVGLoader SVG_LOADER = new SVGLoader(); // loader condiviso per tutti gli SVG

    private final SVGDocument svgDocument; // documento SVG associato al componente
    private final Shape shape; // shape calcolata dall'SVG, usata per il rendering

    private Color color = null; // colore opzionale da usare per il fill

    private JSVG(@NotNull SVGDocument svgDocument) { // costruttore privato con documento SVG
        this.svgDocument = svgDocument;
        this.shape = svgDocument.computeShape(); // calcola la shape dall'SVG
    }

    public static @NotNull JSVG from(@NotNull URL resourcePath) { // carica un SVG da URL e ritorna un JSVG
        return new JSVG(Objects.requireNonNull(SVG_LOADER.load(resourcePath))); // carica e crea componente
    }

    public static @NotNull Shape getShapeFromSVG(@NotNull URL url) { // ottiene solo la shape dall'SVG
        return Objects.requireNonNull(SVG_LOADER.load(url)).computeShape();
    }

    @Override
    protected void paintComponent(Graphics g) { // rendering del componente
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create(); // crea copia del contesto grafico

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // antialiasing
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE); // contorni precisi

        Rectangle bounds = shape.getBounds(); // ottiene i limiti della shape

        double scale = Math.min( // calcola scala per adattare shape al componente
                getWidth() / (double) bounds.width,
                getHeight() / (double) bounds.height
        );

        AffineTransform at = new AffineTransform(); // trasforma la shape
        at.scale(scale, scale); // scala
        at.translate(-bounds.x, -bounds.y); // trasla per iniziare da 0,0

        Shape transformed = at.createTransformedShape(shape); // shape trasformata

        if (color != null) g2.setColor(color); // usa colore custom se presente
        else g2.setColor(getBackground()); // altrimenti usa colore di sfondo

        g2.fill(transformed); // riempi la shape

        g2.dispose(); // rilascia il contesto grafico
    }

    public JSVG setColor(Color color) { // imposta colore custom e ritorna this per chaining
        this.color = color;
        return this;
    }

    public SVGDocument getSVGDocument() { // getter del documento SVG
        return svgDocument;
    }

    public Shape getShape() { // getter della shape calcolata
        return shape;
    }

}