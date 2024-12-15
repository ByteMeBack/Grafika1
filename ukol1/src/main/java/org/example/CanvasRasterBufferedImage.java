package org.example;

import java.awt.image.BufferedImage;

public class CanvasRasterBufferedImage extends Canvas {
    private BufferedImage rasterImage;

    public CanvasRasterBufferedImage(int width, int height) {
        rasterImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public void addPixel(int x, int y) {
        if (x >= 0 && x < rasterImage.getWidth() && y >= 0 && y < rasterImage.getHeight()) {
            rasterImage.setRGB(x, y, getCurrentColor().getRGB());
        }
        super.addPixel(x, y);
    }

}
