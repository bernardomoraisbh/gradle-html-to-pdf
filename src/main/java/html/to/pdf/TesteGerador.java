package html.to.pdf;

import html.to.pdf.converters.HtmlToPdfConverter;
import html.to.pdf.converters.TestTagConverter;
import html.to.pdf.vo.EscrituraPublicaVO;
import lib.gerador.documentos.utils.ParametroUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/teste")
public class TesteGerador {

    private static final Logger logger = LoggerFactory.getLogger(TesteGerador.class);

    @Autowired
    private ParametroUtils parametroUtils;

    @PostMapping(path = "/gerador", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> htmlToPdf(@RequestBody String html) throws Exception {
        logger.info("HtmlToPdfController.htmlToPdf()");
        var headers = new LinkedMultiValueMap<String, String>();
        headers.add("Content-disposition", "attachment;filename=html_to_pdf" + LocalDateTime.now() + ".pdf");
//        var escritura = new EscrituraVO();
        var escrituraPublica = new EscrituraPublicaVO();
        html = TestTagConverter.processTemplate(escrituraPublica, html);
        return new ResponseEntity<>(HtmlToPdfConverter.htmlToPdf(html), HttpStatus.OK);
    }
}
