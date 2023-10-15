import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import net.coobird.thumbnailator.Thumbnails;

public class ImageUtils {
    private static final String DEFAULT_FORMAT = "JPEG";
    public static byte[] getScaledImage(String path, int width, int height) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new FileInputStream(path));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(bufferedImage)
        .size(width, height)
        .outputFormat(DEFAULT_FORMAT)
        .outputQuality(0.5)
        .toOutputStream(outputStream);

        return outputStream.toByteArray();

    }
}