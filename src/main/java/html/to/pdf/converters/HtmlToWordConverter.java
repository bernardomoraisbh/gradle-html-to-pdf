package html.to.pdf.converters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlToWordConverter {

    public static byte[] htmlToWord(String html) throws IOException {
        html = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"><html>"
            + html + "</html>";
        Document htmlDoc = Jsoup.parse(html);
        htmlDoc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        XWPFDocument wordDoc = new XWPFDocument();
        Elements elements = htmlDoc.body().select("*");

        for (Element element : elements) {
            if (element.tagName().equals("p")) {
                XWPFParagraph paragraph = wordDoc.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(element.text());
            } else if (element.tagName().equals("h1")) {
                XWPFParagraph header = wordDoc.createParagraph();
                header.setStyle("Heading1");
                XWPFRun run = header.createRun();
                run.setText(element.text());
            } else if (element.tagName().equals("h2")) {
                XWPFParagraph header = wordDoc.createParagraph();
                header.setStyle("Heading2");
                XWPFRun run = header.createRun();
                run.setText(element.text());
            }
            // Add more logic for other HTML elements like tables, images, etc.
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        wordDoc.write(outputStream);
        outputStream.flush();
        outputStream.close();
        return outputStream.toByteArray();
    }
}
