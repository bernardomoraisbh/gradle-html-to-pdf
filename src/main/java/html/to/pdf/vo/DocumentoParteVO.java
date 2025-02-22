package html.to.pdf.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoParteVO {

    private String numeroDocumento;
    private TipoDocumentoVO tipoDocumento;
    private OrgaoExpedidorVO orgaoExpedidor;
}
