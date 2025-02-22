package html.to.pdf.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoVO {
    private Long id;
    private String nomeDocumento;
    private String numeroDocumento;
}
