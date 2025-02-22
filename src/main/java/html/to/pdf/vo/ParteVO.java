package html.to.pdf.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParteVO {

    private Long id;
    private String nome;
    private String email;
    private NacionalidadeVO nacionalidade;
    private ProfissaoVO profissao;
    private List<DocumentoVO> documentos;
    private List<DocumentoParteVO> documentoParte;
    private EstadoCivilVO estadoCivil;
    private EnderecoVO endereco;
    private String profissaoTextoLivre;
    private String cpf;

    private Boolean possuiParteOutorgante;
    private Boolean possuiParteOutorgado;
    private Boolean possuiMenorDeIdade;
    private Boolean possuiMaiorDeIdade;
    private Boolean possuiUniaoEstavel;
    private Boolean possuiNaoConviveUniaoEstavel;
    private Boolean possuiMaisDeUmaParte;

    public DocumentoParteVO criarDocumentoParteUm() {
        return DocumentoParteVO.builder()
            .numeroDocumento("1")
            .tipoDocumento(TipoDocumentoVO.builder().id(1L).descricao("CPF").build())
            .orgaoExpedidor(OrgaoExpedidorVO.builder().id(1L).nome("SSP/MG").build())
            .build();
    }

    public DocumentoParteVO criarDocumentoParteDois() {
        return DocumentoParteVO.builder()
            .numeroDocumento("2")
            .tipoDocumento(TipoDocumentoVO.builder().id(2L).descricao("RG").build())
            .orgaoExpedidor(OrgaoExpedidorVO.builder().id(2L).nome("SSP/SP").build())
            .build();
    }
}
