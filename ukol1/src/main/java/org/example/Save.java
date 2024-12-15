package org.example;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Save {

    public static boolean saveImageAsJPG(BufferedImage image, File file) {
        try {
            String path = file.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".jpg")) {
                file = new File(path + ".jpg");
            }

            return ImageIO.write(image, "jpg", file);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
