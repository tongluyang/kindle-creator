package com.tongluyang;

import com.tongluyang.entity.Book;
import com.tongluyang.entity.Chapter;
import com.tongluyang.entity.Paragraph;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class KindleCreatorTests {
    @Test
    public void testKindleCreator() throws IOException {
        Book book = KindleCreator.createSimpleBook("你好世界");

        book.setCreator("一个逗逼");
//        book.setCover(FileUtils.readFileToByteArray(new File("G:\\kindle_workspace\\test\\images\\cover.jpg")));

        Chapter chapter1 = new Chapter();
        chapter1.setChapterName("你好");
        chapter1.setId("1");
        chapter1.addParagraph(new Paragraph("哈哈"));
        chapter1.addParagraph(new Paragraph("嘿嘿"));
        book.addChapter("1", chapter1);

        Chapter chapter2 = new Chapter();
        chapter2.setId("2");
        chapter2.setChapterName("世界");
        chapter2.parse("你好吗\n世界");
        book.addChapter("2", chapter2);

        KindleCreator.configKindleGenPath("C:\\Program Files\\kindlegen_win32_v2_9\\kindlegen.exe");
        KindleCreator.saveToFile(book);
    }
}
