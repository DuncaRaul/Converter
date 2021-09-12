import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        String mangaName = "Zettai Ni Yatte Wa Ikenai Isekai Shoukan";
        String outputDirectory = "E:\\Manga\\PDFS\\Not Read\\" + mangaName + "\\";
        String pdfFileName = mangaName + "_";
        String inputDirectoryPath = "E:\\Manga\\Manga Mitsu\\" + mangaName;

        List<String> subDirectoryPaths = getAllSubDirectories(inputDirectoryPath);

        combineImagesIntoPDF(subDirectoryPaths, outputDirectory, pdfFileName);

    }

    private static List<String> getAllSubDirectories(String directoryPath) {
        List<String> results = new ArrayList<>();
        File[] files = new File(directoryPath).listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                results.add(file.getPath());
            }
        }

        return results;
    }

    private static void combineImagesIntoPDF(List<String> subDirectoryPaths, String pdfPath, String pdfFileName) throws IOException {
        try {
            File directory = new File(pdfPath);
            if(directory.mkdir()) {
                for (String subDir : subDirectoryPaths) {
                    PDDocument doc = new PDDocument();
                    File[] files = new File(subDir).listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (isFileImage(file.getName().toLowerCase())) {
                                addImageAsNewPage(doc, file.getPath());
                            }
                        }
                    }
                    String finalFilePath = pdfPath + pdfFileName + extractChapterNumber(subDir) + ".pdf";
                    doc.save(finalFilePath);
                    doc.close();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String extractChapterNumber(String subDirPath) {
        String[] splitDir = subDirPath.split("\\\\");
        return splitDir[splitDir.length - 1].split(" ")[1];
    }

    private static boolean isFileImage(String fileName) {
        return fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg") || fileName.toLowerCase().endsWith(".png");
    }

    private static void addImageAsNewPage(PDDocument doc, String imagePath) {
        try {
            PDImageXObject image          = PDImageXObject.createFromFile(imagePath, doc);
            PDRectangle    pageSize       = PDRectangle.A4;

            int            originalWidth  = image.getWidth();
            int            originalHeight = image.getHeight();
            float          pageWidth      = pageSize.getWidth();
            float          pageHeight     = pageSize.getHeight();
            float          ratio          = Math.min(pageWidth / originalWidth, pageHeight / originalHeight);
            float          scaledWidth    = originalWidth  * ratio;
            float          scaledHeight   = originalHeight * ratio;
            float          x              = (pageWidth  - scaledWidth ) / 2;
            float          y              = (pageHeight - scaledHeight) / 2;

            PDPage         page           = new PDPage(pageSize);
            doc.addPage(page);
            try (PDPageContentStream contents = new PDPageContentStream(doc, page)) {
                contents.drawImage(image, x, y, scaledWidth, scaledHeight);
            }
            System.out.println("Added: " + imagePath);
        } catch (IOException e) {
            System.err.println("Failed to process: " + imagePath);
            e.printStackTrace(System.err);
        }
    }
}
