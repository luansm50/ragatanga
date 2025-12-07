package ragatanga.com.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.text.Normalizer;
import java.util.Base64;
import java.util.UUID;

public class FileUtil {

    public static String normalizeFilename(String filename) {
        return Normalizer
                .normalize(filename, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-zA-Z0-9._-]", " ");
    }

    public static File base64ToFile(String base64, String filename) throws IOException {
        String extension = FilenameUtils.getExtension(filename);
        File tempFile = File.createTempFile(String.valueOf(UUID.randomUUID()), "." + extension, FileUtils.getTempDirectory());
        tempFile.deleteOnExit();

        try (
            InputStream base64Input = new ByteArrayInputStream(base64.getBytes());
            InputStream decodedStream = Base64.getDecoder().wrap(base64Input);
            OutputStream fileOut = new FileOutputStream(tempFile)
        ) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = decodedStream.read(buffer)) != -1) {
                fileOut.write(buffer, 0, bytesRead);
            }
        }

        return tempFile;
    }

    public static String fileToBase64(File file) throws IOException {
        try (InputStream fileIn = new FileInputStream(file);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = fileIn.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        }
    }
}
