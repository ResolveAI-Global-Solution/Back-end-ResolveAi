package br.com.fiap.resolveai.service;

import br.com.fiap.resolveai.dao.BairroDAO;
import br.com.fiap.resolveai.dao.CategoriaDAO;
import br.com.fiap.resolveai.dao.HistoricoStatusDAO;
import br.com.fiap.resolveai.dao.OcorrenciaDAO;
import br.com.fiap.resolveai.dao.Transacao;
import br.com.fiap.resolveai.dao.UsuarioDAO;
import br.com.fiap.resolveai.dto.OcorrenciaRequestDTO;
import br.com.fiap.resolveai.dto.OcorrenciaResponseDTO;
import br.com.fiap.resolveai.exception.ConflitoException;
import br.com.fiap.resolveai.exception.RecursoNaoEncontradoException;
import br.com.fiap.resolveai.exception.RegraNegocioException;
import br.com.fiap.resolveai.model.Ocorrencia;
import br.com.fiap.resolveai.model.StatusOcorrencia;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/** Registro, consulta, edicao e remocao de ocorrencias, com a checagem de pedido igual por perto. */
@Service
public class OcorrenciaService {

    static final double RAIO_PARECIDAS_METROS = 30;
    private static final int MAX_PARECIDAS = 3;

    private final OcorrenciaDAO ocorrenciaDAO;
    private final HistoricoStatusDAO historicoDAO;
    private final UsuarioDAO usuarioDAO;
    private final CategoriaDAO categoriaDAO;
    private final BairroDAO bairroDAO;
    private final Transacao transacao;
    private final ModelMapper modelMapper;

    public OcorrenciaService(OcorrenciaDAO ocorrenciaDAO, HistoricoStatusDAO historicoDAO, UsuarioDAO usuarioDAO,
                            CategoriaDAO categoriaDAO, BairroDAO bairroDAO, Transacao transacao,
                            ModelMapper modelMapper) {
        this.ocorrenciaDAO = ocorrenciaDAO;
        this.historicoDAO = historicoDAO;
        this.usuarioDAO = usuarioDAO;
        this.categoriaDAO = categoriaDAO;
        this.bairroDAO = bairroDAO;
        this.transacao = transacao;
        this.modelMapper = modelMapper;
    }

    /** Lista do mapa: nunca mostra ocultas nem REJEITADA (o filtro fica no DAO). */
    public List<OcorrenciaResponseDTO> listar(StatusOcorrencia status, Long cdCategoria, Long cdBairro) {
        return paraDTO(ocorrenciaDAO.listar(status, cdCategoria, cdBairro));
    }

    public OcorrenciaResponseDTO buscarPorId(Long id) {
        return modelMapper.map(buscarOcorrencia(id), OcorrenciaResponseDTO.class);
    }

    /**
     * RN02 (UC11): ate 3 ocorrencias em aberto da mesma categoria a ate 30 m do ponto,
     * da mais proxima para a mais distante.
     */
    public List<OcorrenciaResponseDTO> buscarParecidas(Long cdCategoria, double latitude, double longitude) {
        return paraDTO(parecidasPorPerto(cdCategoria, latitude, longitude));
    }

    public OcorrenciaResponseDTO registrar(OcorrenciaRequestDTO dto) {
        validarReferencias(dto.getCdUsuario(), dto.getCdCategoria(), dto.getCdBairro());

        // RN02 (UC11): pedido igual a ate 30 m -> sugerir apoiar, a menos que o morador escolha "Registrar mesmo assim"
        if (!Boolean.TRUE.equals(dto.getRegistrarMesmoAssim())) {
            List<Ocorrencia> parecidas = parecidasPorPerto(dto.getCdCategoria(), dto.getLatitude(), dto.getLongitude());
            if (!parecidas.isEmpty()) {
                throw new RegraNegocioException("Já existe um pedido igual aqui perto (ocorrência "
                        + parecidas.get(0).getId() + "). Quer apoiar esse em vez de abrir outro?");
            }
        }

        Ocorrencia ocorrencia = modelMapper.map(dto, Ocorrencia.class);
        // UC01: a ocorrencia e o primeiro registro do historico sao gravados juntos (mesma transacao)
        long id = transacao.executar(conn -> {
            long novoId = ocorrenciaDAO.inserir(conn, ocorrencia);
            historicoDAO.inserir(conn, novoId, StatusOcorrencia.ABERTA, "Ocorrência registrada com foto no local.");
            return novoId;
        });
        return buscarPorId(id);
    }

    public OcorrenciaResponseDTO atualizar(Long id, OcorrenciaRequestDTO dto) {
        Ocorrencia ocorrencia = buscarOcorrencia(id);
        // RN05: ocorrencia encerrada (RESOLVIDA ou REJEITADA) nao pode ser editada
        if (ocorrencia.getStatus() == StatusOcorrencia.RESOLVIDA || ocorrencia.getStatus() == StatusOcorrencia.REJEITADA) {
            throw new RegraNegocioException("Esta ocorrência já foi encerrada e não pode mais ser editada.");
        }
        validarReferencias(null, dto.getCdCategoria(), dto.getCdBairro());

        ocorrencia.setCdCategoria(dto.getCdCategoria());
        ocorrencia.setCdBairro(dto.getCdBairro());
        ocorrencia.setTitulo(dto.getTitulo());
        ocorrencia.setDescricao(dto.getDescricao());
        ocorrencia.setEndereco(dto.getEndereco());
        ocorrencia.setLatitude(dto.getLatitude());
        ocorrencia.setLongitude(dto.getLongitude());
        ocorrenciaDAO.atualizar(ocorrencia);
        return buscarPorId(id);
    }

    public void remover(Long id) {
        buscarOcorrencia(id);
        // RN04: com apoios, denuncias ou acompanhamento alem do registro inicial, a ocorrencia fica
        if (ocorrenciaDAO.contarFilhos(id) > 0) {
            throw new ConflitoException("Não dá para remover: existem registros ligados a este item.");
        }
        transacao.executar(conn -> {
            historicoDAO.removerPorOcorrencia(conn, id);
            ocorrenciaDAO.remover(conn, id);
            return null;
        });
    }

    Ocorrencia buscarOcorrencia(Long id) {
        return ocorrenciaDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Não encontramos a ocorrência " + id + "."));
    }

    private List<Ocorrencia> parecidasPorPerto(Long cdCategoria, double latitude, double longitude) {
        return ocorrenciaDAO.buscarParecidas(cdCategoria).stream()
                .filter(o -> distancia(o, latitude, longitude) <= RAIO_PARECIDAS_METROS)
                .sorted(Comparator.comparingDouble(o -> distancia(o, latitude, longitude)))
                .limit(MAX_PARECIDAS)
                .toList();
    }

    private double distancia(Ocorrencia o, double latitude, double longitude) {
        return GeoUtils.distanciaMetros(o.getLatitude(), o.getLongitude(), latitude, longitude);
    }

    /** FKs inexistentes viram 404 dizendo qual nao existe. cdUsuario nulo = nao conferir. */
    private void validarReferencias(Long cdUsuario, Long cdCategoria, Long cdBairro) {
        if (cdUsuario != null && usuarioDAO.buscarPorId(cdUsuario).isEmpty()) {
            throw new RecursoNaoEncontradoException("Não encontramos o usuário " + cdUsuario + ".");
        }
        if (categoriaDAO.buscarPorId(cdCategoria).isEmpty()) {
            throw new RecursoNaoEncontradoException("Não encontramos a categoria " + cdCategoria + ".");
        }
        if (bairroDAO.buscarPorId(cdBairro).isEmpty()) {
            throw new RecursoNaoEncontradoException("Não encontramos o bairro " + cdBairro + ".");
        }
    }

    private List<OcorrenciaResponseDTO> paraDTO(List<Ocorrencia> ocorrencias) {
        return ocorrencias.stream()
                .map(o -> modelMapper.map(o, OcorrenciaResponseDTO.class))
                .toList();
    }
}
