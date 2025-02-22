package html.to.pdf.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParteCartorioVO {
    private Long id;
    private List<ParteDadosCartorioVO> nomeParte;
    private Boolean isMudancaNome;
    private Boolean isNomeDisabled;
}
