package html.to.pdf.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class EscrituraPublicaVO {

    private Long id;
    private ParteCartorioVO nomeParteCartorio;
    private List<ParteVO> partes = new ArrayList<>();

    private void instanciarParteUm() {
        var parte = new ParteVO();
        parte.setId(1559L);
        parte.setNome("francisco");
        parte.setEmail("francisco@francisco.com");
        var documentoParte = DocumentoVO.builder().id(866L).numeroDocumento("123455").build();
        var documentoParteDois = DocumentoVO.builder().id(867L).numeroDocumento("6789").build();

        parte.setDocumentos(new ArrayList<>(List.of(documentoParte, documentoParteDois)));
        parte.setPossuiParteOutorgante(true);
        parte.setPossuiParteOutorgado(false);
        parte.setPossuiMenorDeIdade(true);
        parte.setPossuiMaiorDeIdade(false);
        parte.setPossuiUniaoEstavel(false);
        parte.setEstadoCivil(EstadoCivilVO.builder().id(1L).descricao("SOLTEIRO").build());
        parte.setProfissao(ProfissaoVO.builder().id(1L).descricao("PINTOR").build());
        parte.setProfissaoTextoLivre("PEDREIRO E PINTOR");
        parte.setDocumentoParte(new ArrayList<>(List.of(parte.criarDocumentoParteUm())));
        parte.setCpf("");

        parte.setEndereco(EnderecoVO.builder().enderecoCompleto("RUA CASA TESTE").build());

        partes.add(parte);
    }

    public EscrituraPublicaVO() {
        setId(14277L);
        var parteCartorio = new ParteCartorioVO();
        parteCartorio.setId(86L);

        var parteDado = new ParteDadosCartorioVO();
        parteDado.setId(99L);
        parteDado.setNome("francisco");
        parteDado.setCpfCnpj("16136959690");

        var sexo = new SexoVO();
        sexo.setId(1L);
        sexo.setCodigo(1L);
        sexo.setDescricao("Masculino");
        sexo.setDescricaoSirc("MASCULINO");
        parteDado.setSexo(sexo);
        parteDado.preencheRegraDeNegocioParte();
        parteCartorio.setNomeParte(new ArrayList<>(List.of(parteDado)));
        setNomeParteCartorio(parteCartorio);

        instanciarParteUm();
    }
}
