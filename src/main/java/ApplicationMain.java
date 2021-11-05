import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
        File logFile = new File("./LOG.txt");
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(logFile.getName());
            PrintWriter printWriter = new PrintWriter(fileWriter);
            if (logFile.createNewFile()) {
                printWriter.println("Log file created at: " + new Date());
            }

            for (String manga:mangaList) {
                String inputDirectoryPath = "./" + manga;
                String pdfFileName = manga + "_";
                String outputDirectory = "./PDFS/" + manga + "/";
                List<String> subDirectoryPaths = getAllSubDirectories(inputDirectoryPath);

                combineImagesIntoPDF(subDirectoryPaths, outputDirectory, pdfFileName, printWriter);
            }
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
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
        List<String> splitFilePath;

        for (File file : files) {
            if (file.isFile()) {
                splitFilePath = Arrays.asList(file.getPath().split("\\\\"));
                results.add(splitFilePath.get(splitFilePath.size() - 1));
            }
        }

        return results;
    }

    private static void combineImagesIntoPDF(List<String> subDirectoryPaths, String pdfPath, String pdfFileName, PrintWriter printWriter) {

        File directory = new File(pdfPath);
        try {

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
                    printWriter.println("Chapter done = " + (pdfFileName + extractChapterNumber(subDir) + ".pdf").substring(2));
                    doc.save(finalFilePath);
                    doc.close();
                }
            } else {
                List<String> mangaList = getAllExistingChapters(pdfPath);

                for (String subDir : subDirectoryPaths) {
                    String finalFilePath = pdfPath + pdfFileName + extractChapterNumber(subDir) + ".pdf";
                    if (!mangaList.contains((pdfFileName + extractChapterNumber(subDir) + ".pdf").substring(2))) {
                        PDDocument doc = new PDDocument();
                        File[] files = new File(subDir).listFiles();
                        if (files != null) {
                            for (File file : files) {
                                if (isFileImage(file.getName().toLowerCase())) {
                                    addImageAsNewPage(doc, file.getPath());
                                }
                            }
                        }
                        printWriter.println("Chapter done = " + (pdfFileName + extractChapterNumber(subDir) + ".pdf").substring(2));
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
            if ("chapter".equalsIgnoreCase(s)) {
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
