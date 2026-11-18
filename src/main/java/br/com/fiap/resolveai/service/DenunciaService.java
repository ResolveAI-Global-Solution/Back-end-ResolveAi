package br.com.fiap.resolveai.service;

import br.com.fiap.resolveai.dao.DenunciaDAO;
import br.com.fiap.resolveai.dao.HistoricoStatusDAO;
import br.com.fiap.resolveai.dao.OcorrenciaDAO;
import br.com.fiap.resolveai.dao.Transacao;
import br.com.fiap.resolveai.dao.UsuarioDAO;
import br.com.fiap.resolveai.dto.DecisaoDenunciaRequestDTO;
import br.com.fiap.resolveai.dto.DenunciaRequestDTO;
import br.com.fiap.resolveai.dto.DenunciaResponseDTO;
import br.com.fiap.resolveai.exception.RecursoNaoEncontradoException;
import br.com.fiap.resolveai.exception.RegraNegocioException;
import br.com.fiap.resolveai.exception.RequisicaoInvalidaException;
import br.com.fiap.resolveai.model.Denuncia;
import br.com.fiap.resolveai.model.StatusDenuncia;
import br.com.fiap.resolveai.model.StatusOcorrencia;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/** Denuncia de publicacoes pelos moradores e decisao da moderacao. */
@Service
public class DenunciaService {

    private static final int DENUNCIAS_PARA_OCULTAR = 3;
    private static final int TAMANHO_OBSERVACAO = 200;

    private final DenunciaDAO denunciaDAO;
    private final OcorrenciaDAO ocorrenciaDAO;
    private final HistoricoStatusDAO historicoDAO;
    private final UsuarioDAO usuarioDAO;
    private final OcorrenciaService ocorrenciaService;
    private final Transacao transacao;
    private final ModelMapper modelMapper;

    public DenunciaService(DenunciaDAO denunciaDAO, OcorrenciaDAO ocorrenciaDAO, HistoricoStatusDAO historicoDAO,
                           UsuarioDAO usuarioDAO, OcorrenciaService ocorrenciaService, Transacao transacao,
                           ModelMapper modelMapper) {
        this.denunciaDAO = denunciaDAO;
        this.ocorrenciaDAO = ocorrenciaDAO;
        this.historicoDAO = historicoDAO;
        this.usuarioDAO = usuarioDAO;
        this.ocorrenciaService = ocorrenciaService;
        this.transacao = transacao;
        this.modelMapper = modelMapper;
    }

    public DenunciaResponseDTO denunciar(Long cdOcorrencia, DenunciaRequestDTO dto) {
        // RN06 (UC06): a ocorrencia precisa existir
        ocorrenciaService.buscarOcorrencia(cdOcorrencia);
        if (usuarioDAO.buscarPorId(dto.getCdUsuario()).isEmpty()) {
            throw new RecursoNaoEncontradoException("Não encontramos o usuário " + dto.getCdUsuario() + ".");
        }
        // RN06: cada conta denuncia uma publicacao uma vez so
        if (denunciaDAO.existe(dto.getCdUsuario(), cdOcorrencia)) {
            throw new RegraNegocioException("Você já denunciou esta publicação.");
        }

        Denuncia denuncia = modelMapper.map(dto, Denuncia.class);
        denuncia.setCdOcorrencia(cdOcorrencia);
        Denuncia gravada = transacao.executar(conn -> {
            long id = denunciaDAO.inserir(conn, denuncia);
            // RN06: com 3 denuncias pendentes a ocorrencia sai do mapa e do placar ate a moderacao decidir
            if (denunciaDAO.contarPendentesPorOcorrencia(conn, cdOcorrencia) >= DENUNCIAS_PARA_OCULTAR) {
                ocorrenciaDAO.marcarOculta(conn, cdOcorrencia, true);
            }
            return denunciaDAO.buscarPorId(conn, id).orElseThrow();
        });
        return modelMapper.map(gravada, DenunciaResponseDTO.class);
    }

    /** Fila da moderacao: da denuncia mais antiga para a mais nova. */
    public List<DenunciaResponseDTO> listar(StatusDenuncia status) {
        return denunciaDAO.listar(status).stream()
                .map(d -> modelMapper.map(d, DenunciaResponseDTO.class))
                .toList();
    }

    public DenunciaResponseDTO buscarPorId(Long id) {
        return modelMapper.map(buscarDenuncia(id), DenunciaResponseDTO.class);
    }

    public DenunciaResponseDTO decidir(Long id, DecisaoDenunciaRequestDTO dto) {
        Denuncia denuncia = buscarDenuncia(id);
        if (dto.getDecisao() == StatusDenuncia.PENDENTE) {
            throw new RequisicaoInvalidaException("A decisão precisa ser MANTIDA ou REMOVIDA.");
        }
        // RN07 (UC07): decisao tomada nao se refaz
        if (denuncia.getStatus() != StatusDenuncia.PENDENTE) {
            throw new RegraNegocioException("Esta denúncia já foi decidida.");
        }

        Long cdOcorrencia = denuncia.getCdOcorrencia();
        Denuncia decidida = transacao.executar(conn -> {
            denunciaDAO.registrarDecisao(conn, id, dto.getDecisao(), dto.getModerador(), dto.getMotivo());

            if (dto.getDecisao() == StatusDenuncia.REMOVIDA) {
                // RN07: publicacao removida vira REJEITADA, com registro no historico, e fica oculta
                if (denuncia.getStatusOcorrencia() != StatusOcorrencia.REJEITADA) {
                    ocorrenciaDAO.atualizarStatus(conn, cdOcorrencia, StatusOcorrencia.REJEITADA);
                    historicoDAO.inserir(conn, cdOcorrencia, StatusOcorrencia.REJEITADA,
                            limitar("Removida pela moderação: " + dto.getMotivo()));
                }
                ocorrenciaDAO.marcarOculta(conn, cdOcorrencia, true);
            } else if (denunciaDAO.contarPendentesPorOcorrencia(conn, cdOcorrencia) == 0
                    && denuncia.getStatusOcorrencia() != StatusOcorrencia.REJEITADA) {
                // RN07: mantida e sem denuncia pendente, a ocorrencia volta ao mapa
                ocorrenciaDAO.marcarOculta(conn, cdOcorrencia, false);
            }
            return denunciaDAO.buscarPorId(conn, id).orElseThrow();
        });
        return modelMapper.map(decidida, DenunciaResponseDTO.class);
    }

    private Denuncia buscarDenuncia(Long id) {
        return denunciaDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Não encontramos a denúncia " + id + "."));
    }

    private String limitar(String texto) {
        return texto.length() <= TAMANHO_OBSERVACAO ? texto : texto.substring(0, TAMANHO_OBSERVACAO);
    }
}
