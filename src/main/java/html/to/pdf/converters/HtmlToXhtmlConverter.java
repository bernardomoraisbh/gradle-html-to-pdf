package html.to.pdf.converters;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HtmlToXhtmlConverter {

    public static String htmlToXhtml(String html) {
        String doctype = "<!DOCTYPE html>";
        html = doctype + "<html>" + html + "</html>";

        Document document = Jsoup.parse(html);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml); // Force XHTML compliance
        return document.html();
    }

}
