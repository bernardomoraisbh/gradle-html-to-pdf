package html.to.pdf.controllers;

import html.to.pdf.converters.HtmlToPdfConverter;
import html.to.pdf.converters.HtmlToWordConverter;
import java.io.IOException;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/html")
public class HtmlToPdfController {

    private static final Logger logger = LoggerFactory.getLogger(HtmlToPdfController.class);

    @PostMapping(path = "/to-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> htmlToPdf(@RequestBody String html) throws IOException {
        logger.info("HtmlToPdfController.htmlToPdf()");
        var headers = new LinkedMultiValueMap<String, String>();
        headers.add("Content-disposition", "attachment;filename=html_to_pdf" + LocalDateTime.now() + ".pdf");
        return new ResponseEntity<>(HtmlToPdfConverter.htmlToPdf(html), HttpStatus.OK);
    }

    @PostMapping(path = "/to-word")
    public ResponseEntity<byte[]> htmlToWord(@RequestBody String html) throws IOException {
        logger.info("HtmlToPdfController.htmlToPdf()");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "msword"));
        headers.setContentDispositionFormData("attachment", "document.doc");
        headers.add("Content-disposition", "attachment;filename=html_to_pdf" + LocalDateTime.now() + ".pdf");
        return new ResponseEntity<>(HtmlToWordConverter.htmlToWord(html), headers, HttpStatus.OK);
    }
}
