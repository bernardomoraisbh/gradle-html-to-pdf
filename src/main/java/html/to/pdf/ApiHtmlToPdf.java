package html.to.pdf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"html.to.pdf", "lib.gerador.documentos"})
public class ApiHtmlToPdf {

    public static void main(String[] args) {
        System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(ApiHtmlToPdf.class, args);
    }

}
