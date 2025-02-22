package html.to.pdf.converters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;

public class HtmlToPdfConverter {

    public static byte[] htmlToPdf(String html) throws IOException {
        html = "<html>" + html + "</html>";

        Document document = Jsoup.parse(html);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml); // Force XHTML compliance
        String xhtml = document.html(); // Get the fixed XHTML

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(xhtml);
        renderer.layout();
        renderer.createPDF(out);
        out.flush();
        out.close();
        return out.toByteArray();
    }

}
