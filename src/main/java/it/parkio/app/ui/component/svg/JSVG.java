package it.parkio.app.ui.component.svg;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.renderer.SVGRenderingHints;
import com.github.weisj.jsvg.view.ViewBox;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.Objects;

public class JSVG extends JComponent {

    private static final SVGLoader SVG_LOADER = new SVGLoader();

    private final SVGDocument svgDocument;

    private JSVG(SVGDocument svgDocument) {
        this.svgDocument = svgDocument;
    }

    public static @NotNull JSVG from(@NotNull URL resourcePath) {
        return new JSVG(SVG_LOADER.load(resourcePath));
    }

    public static @NotNull Shape getShapeFromSVG(@NotNull URL url) {
        return Objects.requireNonNull(SVG_LOADER.load(url)).computeShape();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D graphics2D = (Graphics2D) g;

        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        graphics2D.setRenderingHint(SVGRenderingHints.KEY_IMAGE_ANTIALIASING, SVGRenderingHints.VALUE_IMAGE_ANTIALIASING_ON);
        graphics2D.setRenderingHint(SVGRenderingHints.KEY_SOFT_CLIPPING, SVGRenderingHints.VALUE_SOFT_CLIPPING_ON);

        svgDocument.render(this, (Graphics2D) g, new ViewBox(0, 0, getWidth(), getHeight()));
    }

    public SVGDocument getSVGDocument() {
        return svgDocument;
    }

}
