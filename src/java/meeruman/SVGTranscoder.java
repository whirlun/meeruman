package meeruman;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import java.awt.image.BufferedImage;

public class SVGTranscoder extends ImageTranscoder {
    private BufferedImage image = null;

    @Override
    public BufferedImage createImage(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        return image;
    }

    public BufferedImage getImage() {
        return image;
    }

    @Override
    public void writeImage(BufferedImage img, TranscoderOutput output) throws TranscoderException {

    }
}
