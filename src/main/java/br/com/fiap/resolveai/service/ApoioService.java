package br.com.fiap.resolveai.service;

import br.com.fiap.resolveai.dao.ApoioDAO;
import br.com.fiap.resolveai.dao.HistoricoStatusDAO;
import br.com.fiap.resolveai.dao.OcorrenciaDAO;
import br.com.fiap.resolveai.dao.Transacao;
import br.com.fiap.resolveai.dao.UsuarioDAO;
import br.com.fiap.resolveai.dto.ApoioRequestDTO;
import br.com.fiap.resolveai.dto.ApoioResponseDTO;
import br.com.fiap.resolveai.exception.RecursoNaoEncontradoException;
import br.com.fiap.resolveai.exception.RegraNegocioException;
import br.com.fiap.resolveai.model.Apoio;
import br.com.fiap.resolveai.model.Ocorrencia;
import br.com.fiap.resolveai.model.StatusOcorrencia;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/** "Eu tambem vi": apoio de vizinhos, com as regras de distancia e a confirmacao automatica. */
@Service
public class ApoioService {

    private static final double RAIO_APOIO_METROS = 500;
    private static final int APOIOS_PARA_CONFIRMAR = 2;

    private final ApoioDAO apoioDAO;
    private final OcorrenciaDAO ocorrenciaDAO;
    private final HistoricoStatusDAO historicoDAO;
    private final UsuarioDAO usuarioDAO;
    private final OcorrenciaService ocorrenciaService;
    private final Transacao transacao;
    private final ModelMapper modelMapper;

    public ApoioService(ApoioDAO apoioDAO, OcorrenciaDAO ocorrenciaDAO, HistoricoStatusDAO historicoDAO,
                        UsuarioDAO usuarioDAO, OcorrenciaService ocorrenciaService, Transacao transacao,
                        ModelMapper modelMapper) {
        this.apoioDAO = apoioDAO;
        this.ocorrenciaDAO = ocorrenciaDAO;
        this.historicoDAO = historicoDAO;
        this.usuarioDAO = usuarioDAO;
        this.ocorrenciaService = ocorrenciaService;
        this.transacao = transacao;
        this.modelMapper = modelMapper;
    }

    public List<ApoioResponseDTO> listarPorOcorrencia(Long cdOcorrencia) {
        ocorrenciaService.buscarOcorrencia(cdOcorrencia);
        return apoioDAO.listarPorOcorrencia(cdOcorrencia).stream()
                .map(a -> modelMapper.map(a, ApoioResponseDTO.class))
                .toList();
    }

    public ApoioResponseDTO apoiar(Long cdOcorrencia, ApoioRequestDTO dto) {
        // RN01 a (UC02): a ocorrencia precisa existir
        Ocorrencia ocorrencia = ocorrenciaService.buscarOcorrencia(cdOcorrencia);
        if (usuarioDAO.buscarPorId(dto.getCdUsuario()).isEmpty()) {
            throw new RecursoNaoEncontradoException("Não encontramos o usuário " + dto.getCdUsuario() + ".");
        }

        // RN01 b: ocorrencia encerrada nao recebe apoio
        if (ocorrencia.getStatus() == StatusOcorrencia.RESOLVIDA) {
            throw new RegraNegocioException("Esta ocorrência já foi resolvida.");
        }
        if (ocorrencia.getStatus() == StatusOcorrencia.REJEITADA) {
            throw new RegraNegocioException("Esta ocorrência foi removida e não recebe apoios.");
        }

        // RN01 c: o autor ja conta como quem viu
        if (ocorrencia.getCdUsuario().equals(dto.getCdUsuario())) {
            throw new RegraNegocioException("Você registrou esta ocorrência, então ela já conta com você.");
        }

        // RN01 d: um apoio por pessoa
        if (apoioDAO.existe(dto.getCdUsuario(), cdOcorrencia)) {
            throw new RegraNegocioException("Você já apoia esta ocorrência.");
        }

        // RN01 e: so apoia quem esta a ate 500 m do local
        double distancia = GeoUtils.distanciaMetros(
                ocorrencia.getLatitude(), ocorrencia.getLongitude(), dto.getLatitude(), dto.getLongitude());
        if (distancia > RAIO_APOIO_METROS) {
            throw new RegraNegocioException("Só quem está a até 500 m do local pode apoiar.");
        }

        Apoio apoio = new Apoio(dto.getCdUsuario(), cdOcorrencia, null);
        Apoio gravado = transacao.executar(conn -> {
            apoioDAO.inserir(conn, apoio);
            // RN01 f: com 2 vizinhos apoiando, a ocorrencia ABERTA passa a CONFIRMADA (mesma transacao)
            if (ocorrencia.getStatus() == StatusOcorrencia.ABERTA
                    && apoioDAO.contarPorOcorrencia(conn, cdOcorrencia) >= APOIOS_PARA_CONFIRMAR) {
                ocorrenciaDAO.atualizarStatus(conn, cdOcorrencia, StatusOcorrencia.CONFIRMADA);
                historicoDAO.inserir(conn, cdOcorrencia, StatusOcorrencia.CONFIRMADA,
                        "Dois vizinhos confirmaram o problema.");
            }
            return apoioDAO.buscar(conn, dto.getCdUsuario(), cdOcorrencia).orElseThrow();
        });
        return modelMapper.map(gravado, ApoioResponseDTO.class);
    }

    /** Retirar o apoio (o morador pode voltar atras). */
    public void remover(Long cdOcorrencia, Long cdUsuario) {
        ocorrenciaService.buscarOcorrencia(cdOcorrencia);
        if (!apoioDAO.remover(cdUsuario, cdOcorrencia)) {
            throw new RecursoNaoEncontradoException("Não encontramos apoio do usuário " + cdUsuario + " nesta ocorrência.");
        }
    }
}
