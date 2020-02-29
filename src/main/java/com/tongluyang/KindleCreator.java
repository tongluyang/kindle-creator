package com.tongluyang;

import com.tongluyang.entity.Book;
import com.tongluyang.entity.Chapter;
import com.tongluyang.entity.Paragraph;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class KindleCreator {

    private static String kindleGenPath = "kindlegen";

    public static Book createSimpleBook(String name) {
        return new Book(name);
    }

    public static void configKindleGenPath(String kindleGenPath) {
        KindleCreator.kindleGenPath = kindleGenPath;
    }

    public static File saveToFile(Book book) {
        File workspace = createWorkspace(book);
        saveChapters(book, workspace);
        saveCss(workspace);
        saveContents(book, workspace);
        saveNcx(book, workspace);
        File opfFile = saveOpf(book, workspace);
        saveCover(book, workspace);

        return kindleGen(opfFile.getAbsolutePath());
    }

    private static File createWorkspace(Book book) {
        String tmpPath = System.getProperty("java.io.tmpdir");
        String name = book.getName();
        File workspace = new File(tmpPath + File.separator + name);
        if (!workspace.exists()) {
            if (!workspace.mkdir()) {
                throw new RuntimeException("cannot create workspace");
            }
        }
        return workspace;
    }

    private static void saveChapters(Book book, File workspace) {
        for (Chapter chapter : book.getChapters().values()) {
            saveChapter(book, chapter, workspace);
        }
    }

    private static void saveChapter(Book book, Chapter chapter, File workspace) {

        StringBuilder html = getHeader(book.getLanguage(), book.getName());

        html.append("<h2>").append(chapter.getChapterName()).append("</h2>\n\n");

        List<Paragraph> paragraphs = chapter.getParagraphs();
        for (Paragraph paragraph : paragraphs) {
            html.append("<").append(paragraph.getHtmlTag()).append(">")
                    .append(paragraph.getText())
                    .append("</").append(paragraph.getHtmlTag()).append(">\n");
        }

        appendEnd(html);

        File chapterFile = newFile(workspace, chapter.getId() + "-" + chapter.getChapterName() + ".html");

        writeFile(chapterFile, html.toString());
    }

    private static void writeFile(File file, String str) {
        try {
            final byte[] bom = new byte[] { (byte)0xEF, (byte)0xBB, (byte)0xBF };
            FileUtils.writeByteArrayToFile(file, bom);
            FileUtils.writeStringToFile(file, str, "utf-8", true);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("cannot write file:" + file.getAbsolutePath());
        }
    }

    private static File newFile(File workspace, String fileName) {
        return new File(workspace.getAbsoluteFile() + File.separator + fileName);
    }

    private static StringBuilder getHeader(String language, String title) {
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html>\n")
                .append("<html lang=\"").append(language).append("\">\n")
                .append("<head>\n" +
                        "\t<meta charset=\"utf-8\" />\n")
                .append("\t<title>").append(title).append("</title>\n")
                .append("\t<link rel=\"stylesheet\" href=\"styles.css\"  type=\"text/css\" />\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "<!-- Your book goes here -->\n");

        return html;
    }

    private static void appendEnd(StringBuilder html) {
        html.append("\n" +
                "<div class=\"pagebreak\"></div>\n" +
                "</body>\n" +
                "</html>");
    }

    private static void saveCss(File workspace) {
        File destination = newFile(workspace, "styles.css");
        try {
            FileUtils.copyInputStreamToFile(KindleCreator.class.getResourceAsStream("/styles.css"), destination);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("cannot save css");
        }
    }

    private static void saveContents(Book book, File workspace) {
        StringBuilder html = getHeader(book.getLanguage(), "目录");

        html.append("<div id=\"toc\">\n" +
                "\t<h2>\n" +
                "\t\t目录\n" +
                "\t</h2>\n");

        html.append("\t<ul>\n");

        for (Chapter chapter : book.getChapters().values()) {
            html.append("\t\t<li><a href=\"").append(chapter.getId()).append("-").append(chapter.getChapterName()).append(".html\">")
                    .append(chapter.getChapterName())
                    .append("</a></li>\n");
        }

        html.append("\t</ul>\n");

        html.append("</div>\n");

        appendEnd(html);

        File toc = newFile(workspace, "toc.html");
        writeFile(toc, html.toString());
    }

    private static void saveNcx(Book book, File workspace) {
        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE ncx PUBLIC \"-//NISO//DTD ncx 2005-1//EN\" \n" +
                "   \"http://www.daisy.org/z3986/2005/ncx-2005-1.dtd\">\n" +
                "<ncx xmlns=\"http://www.daisy.org/z3986/2005/ncx/\" version=\"2005-1\">\n" +
                "  <head>\n" +
                "  </head>\n" +
                "  <docTitle>\n")
            .append("    <text>").append(book.getName()).append("</text>\n")
            .append("  </docTitle>\n");

        xml.append("<navMap>\n");

        xml.append("<navPoint id=\"navpoint-1\" playOrder=\"1\"><navLabel><text>目录</text></navLabel><content src=\"toc.html#toc\"/></navPoint>\n");
        int offset = 2;
        for (Chapter chapter : book.getChapters().values()) {
            xml.append("<navPoint id=\"navpoint-").append(offset).append("\" playOrder=\"").append(offset++).append("\">" +
                    "<navLabel><text>").append(chapter.getChapterName()).append("</text></navLabel>" +
                    "<content src=\"").append(chapter.getId()).append("-").append(chapter.getChapterName()).append(".html\"/>" +
                    "</navPoint>\n");
        }

        xml.append("</navMap>\n");

        File toc = newFile(workspace, "toc.ncx");
        writeFile(toc, xml.toString());
    }

    private static void saveCover(Book book, File workspace) {
        if (book.getCover() == null) {
            return;
        }
        String fileName = "images" + File.separator + "cover.jpg";
        try {
            FileUtils.writeByteArrayToFile(newFile(workspace, fileName), book.getCover());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("cannot write cover");
        }

    }

    private static File saveOpf(Book book, File workspace) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<package unique-identifier=\"uid\" xmlns:opf=\"http://www.idpf.org/2007/opf\" xmlns:asd=\"http://www.idpf.org/asdfaf\">\n" +
                "\t<metadata>\n" +
                "\t\t<dc-metadata  xmlns:dc=\"http://purl.org/metadata/dublin_core\" xmlns:oebpackage=\"http://openebook.org/namespaces/oeb-package/1.0/\">\n")

                .append("\t\t\t<dc:Title>").append(book.getName()).append("</dc:Title>\n")
                .append("\t\t\t<dc:Language>").append(book.getLanguage()).append("</dc:Language>\n")
                .append("\t\t\t<dc:Creator>").append(book.getCreator()).append("</dc:Creator>\n")
                .append("\t\t\t<dc:Copyrights>").append(book.getCreator()).append("</dc:Copyrights>\n")
                .append("\t\t\t<dc:Publisher>").append(book.getCreator()).append("</dc:Publisher>\n")
                .append("\t\t\t<x-metadata>\n" +
                        "\t\t\t\t<EmbeddedCover>images/cover.jpg</EmbeddedCover>\n" +
                        "\t\t\t</x-metadata>\n" +
                        "\t\t</dc-metadata>\n" +
                        "\t\t<meta name=\"Cover ThumbNail Image\" content=\"images/cover.jpg\" />\n" +
                        "\t</metadata>\n" +
                        "\t<manifest>\n" +
                        "\t\t<item id=\"cimage\" media-type=\"image/jpeg\" href=\"images/cover.jpg\" properties=\"coverimage\"/>\n" +
                        "\t\t<item id=\"content\" media-type=\"text/x-oeb1-document\" href=\"toc.html\"></item>\n" +
                        "\t\t<item id=\"ncx\" media-type=\"application/x-dtbncx+xml\" href=\"toc.ncx\"/>\n");

        for (Chapter chapter : book.getChapters().values()) {
            xml.append("\t\t<item id=\"").append(chapter.getId()).append("-").append(chapter.getChapterName())
                    .append("\" media-type=\"text/x-oeb1-document\" href=\"").append(chapter.getId()).append("-").append(chapter.getChapterName()).append(".html\"></item>\n");
        }

        xml.append("\t</manifest>\n" +
                "\t<spine toc=\"ncx\">\n" +
                "\t\t<itemref idref=\"content\"/>\n");

        for (Chapter chapter : book.getChapters().values()) {
            xml.append("\t\t<itemref idref=\"").append(chapter.getId()).append("-").append(chapter.getChapterName()).append("\"/>\n");
        }

        xml.append("\t</spine>\n" +
                "\t<guide>\n" +
                "\t\t<reference type=\"toc\" title=\"目录\" href=\"toc.html\"/>\n");
        Chapter firstChapter = book.getChapters().values().iterator().next();
        xml.append("\t\t<reference type=\"text\" title=\"Book\" href=\"").append(firstChapter.getId()).append("-").append(firstChapter.getChapterName()).append(".html\"/>\n");
        xml.append("\t</guide>\n" +
                "</package>");

        File opfFile = newFile(workspace, book.getName() + ".opf");
        writeFile(opfFile, xml.toString());
        return opfFile;
    }

    private static File kindleGen(String opfFile) {
        CommandLine cmd = new CommandLine(kindleGenPath);
        cmd.addArgument(opfFile);
        try {
            new DefaultExecutor().execute(cmd);
        } catch (IOException e) {
            // do nothing
        }
        File mobiFile = new File(opfFile.substring(0, opfFile.length() - 3) + ".mobi");
        if (mobiFile.exists()) {
            return mobiFile;
        }
        return null;
    }
}
