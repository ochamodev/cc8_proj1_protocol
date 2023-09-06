import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtils {

    public static boolean checkIfFileExists(String filePath, Logger logger) {
        try {
            String currentWorkingDir = System.getProperty("user.dir");
            logger.log(Level.INFO, "Current Working Directory: " + currentWorkingDir);
            File file = new File(filePath);
            return file.exists();
        } catch (Exception e) {
            logger.log(Level.INFO, e.getMessage());
            return false;
        }
    }

    public static String readHtml(String filePath) {
        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            StringBuilder htmlContent = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                htmlContent.append(line).append("\n");
            }

            // Close the resources
            bufferedReader.close();
            return htmlContent.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static byte[] readFile(String path) throws IOException {
        File file = new File(path);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toByteArray();
        }
    }
}
