package html.to.pdf.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.random.RandomGenerator;

@Data
@AllArgsConstructor
public class EscrituraVO {
    private Long id;
    private String nomeEscritura;
    private List<ParteVO> partes;

    public EscrituraVO(){
        this.id = RandomGenerator.getDefault().nextLong();
        this.nomeEscritura = "escritura";
//        List<DocumentoVO> documentos = new ArrayList<>(Arrays.asList(new DocumentoVO(1L, "documento 1", "123"), new DocumentoVO(2L, "documento 2", "456")));
        List<DocumentoVO> documentos = new ArrayList<>(Arrays.asList(new DocumentoVO(1L, "documento 1", "123")));
//        List<DocumentoVO> documentosDois = new ArrayList<>(Arrays.asList(new DocumentoVO(3L, "documento 3", "789"), new DocumentoVO(4L, "documento 4", "000")));
//        this.partes = new ArrayList<>(Arrays.asList(new ParteVO(RandomGenerator.getDefault().nextLong(), "parte 1", documentos), new ParteVO(RandomGenerator.getDefault().nextLong(), "parte 2", documentosDois)));
//        this.partes = new ArrayList<>(Arrays.asList(new ParteVO(RandomGenerator.getDefault().nextLong(), "parte 1", "teste@teste.com", documentos, false)));
    }
}
