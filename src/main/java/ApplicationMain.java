import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class ApplicationMain {

    public ApplicationMain()
    {
        convertManga();
    }

    public static void main(String[] args) {
        new ApplicationMain();
    }

    public static void convertManga(){

        List<String> mangaList = getAllSubDirectories("./");

        File pdfDirectory = new File("./PDFS");
        pdfDirectory.mkdir();

        for (String manga:mangaList) {
            String inputDirectoryPath = "./" + manga;
            String pdfFileName = manga + "_";
            String outputDirectory = "./PDFS/" + manga + "/";
            List<String> subDirectoryPaths = getAllSubDirectories(inputDirectoryPath);

            combineImagesIntoPDF(subDirectoryPaths, outputDirectory, pdfFileName, inputDirectoryPath);
        }

    }

    private static List<String> getAllSubDirectories(String directoryPath) {
        List<String> results = new ArrayList<>();
        File[] files = new File(directoryPath).listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                if (!file.getName().contains("PDFS") && !file.getName().contains("Read") && !file.getName().contains("Not Read")) {
                    results.add(file.getPath());
                }
            }
        }

        return results;
    }

    private static List<String> getAllExistingChapters(String directoryPath) {
        List<String> results = new ArrayList<>();
        File[] files = new File(directoryPath).listFiles();

        for (File file : files) {
            if (file.isFile()) {
                results.add(file.getPath());
            }
        }

        return results;
    }

    private static void combineImagesIntoPDF(List<String> subDirectoryPaths, String pdfPath, String pdfFileName, String inputDirectoryPath) {
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
            } else {
                List<String> mangaList = getAllExistingChapters(pdfPath);

                for (String subDir : subDirectoryPaths) {
                    String finalFilePath = pdfPath + pdfFileName + extractChapterNumber(subDir) + ".pdf";
                    if (!mangaList.contains(finalFilePath)) {
                        PDDocument doc = new PDDocument();
                        File[] files = new File(subDir).listFiles();
                        if (files != null) {
                            for (File file : files) {
                                if (isFileImage(file.getName().toLowerCase())) {
                                    addImageAsNewPage(doc, file.getPath());
                                }
                            }
                        }
                        doc.save(finalFilePath);
                        doc.close();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String extractChapterNumber(String subDirPath) {
        String chapterNumber = "";
        String[] splitDir = subDirPath.split("\\\\");
        List<String> splitName = Arrays.asList(splitDir[splitDir.length - 1].split(" "));
        for (String s:splitName) {
            if ("chapter".equals(s.toLowerCase())) {
                chapterNumber = splitName.get(splitName.indexOf(s) + 1);
            }
        }
        return chapterNumber;
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
