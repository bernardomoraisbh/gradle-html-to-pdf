package html.to.pdf.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SexoVO {
    public static final Long COD_SEXO_MASCULINO = 1L;
    public static final Long COD_SEXO_FEMININO = 2L;
    public static final Long COD_SEXO_IGNORADO = 4L;

    private Long id;
    private Long codigo;
    private String descricao;
    private String descricaoSirc;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", locale = "pt-BR", timezone = "Brazil/East")
    private Date dataInicio;

    public String getDescricaoSexo() {
        return StringUtils.isNotBlank(getDescricaoSirc()) ? getDescricaoSirc() : "";
    }

    public String getSiglaSexo() {
        if (COD_SEXO_MASCULINO.equals(codigo)) {
            return "M";
        }
        if (COD_SEXO_FEMININO.equals(codigo)) {
            return "F";
        }
        return null;
    }

    public boolean isFeminino() {
        return COD_SEXO_FEMININO.equals(codigo);
    }

    public boolean isMasculino() {
        return COD_SEXO_MASCULINO.equals(codigo);
    }
}
