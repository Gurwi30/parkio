package it.parkio.app.ui.component.svg;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.SVGLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.util.Objects;

public class JSVG extends JComponent {

    private static final SVGLoader SVG_LOADER = new SVGLoader();

    private final SVGDocument svgDocument;
    private final Shape shape;

    private Color color = null;

    private JSVG(@NotNull SVGDocument svgDocument) {
        this.svgDocument = svgDocument;
        this.shape = svgDocument.computeShape();
    }

    public static @NotNull JSVG from(@NotNull URL resourcePath) {
        return new JSVG(Objects.requireNonNull(SVG_LOADER.load(resourcePath)));
    }

    public static @NotNull Shape getShapeFromSVG(@NotNull URL url) {
        return Objects.requireNonNull(SVG_LOADER.load(url)).computeShape();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        Rectangle bounds = shape.getBounds();

        double scale = Math.min(
                getWidth() / (double) bounds.width,
                getHeight() / (double) bounds.height
        );

        AffineTransform at = new AffineTransform();
        at.scale(scale, scale);
        at.translate(-bounds.x, -bounds.y);

        Shape transformed = at.createTransformedShape(shape);

        if (color != null) g2.setColor(color);
        else g2.setColor(getBackground());

        g2.fill(transformed);

        g2.dispose();
    }

    public JSVG setColor(Color color) {
        this.color = color;
        return this;
    }

    public SVGDocument getSVGDocument() {
        return svgDocument;
    }

    public Shape getShape() {
        return shape;
    }

}
