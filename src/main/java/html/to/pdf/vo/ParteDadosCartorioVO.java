package html.to.pdf.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParteDadosCartorioVO {
    private Long id;
    private String nome;
    private String cpfCnpj;
    private SexoVO sexo;

    private Boolean possuiSexoMasculino;
    private Boolean possuiSexoFeminino;

    public void preencheRegraDeNegocioParte() {
        setPossuiSexoMasculino(sexo.isMasculino());
        setPossuiSexoFeminino(sexo.isFeminino());
    }
}
