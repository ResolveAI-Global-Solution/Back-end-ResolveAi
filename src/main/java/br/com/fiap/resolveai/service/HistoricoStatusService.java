package br.com.fiap.resolveai.service;

import br.com.fiap.resolveai.dao.HistoricoStatusDAO;
import br.com.fiap.resolveai.dao.OcorrenciaDAO;
import br.com.fiap.resolveai.dao.Transacao;
import br.com.fiap.resolveai.dao.UsuarioDAO;
import br.com.fiap.resolveai.dto.HistoricoStatusRequestDTO;
import br.com.fiap.resolveai.dto.HistoricoStatusResponseDTO;
import br.com.fiap.resolveai.dto.ObservacaoHistoricoRequestDTO;
import br.com.fiap.resolveai.exception.RecursoNaoEncontradoException;
import br.com.fiap.resolveai.exception.RegraNegocioException;
import br.com.fiap.resolveai.exception.RequisicaoInvalidaException;
import br.com.fiap.resolveai.model.HistoricoStatus;
import br.com.fiap.resolveai.model.Ocorrencia;
import br.com.fiap.resolveai.model.StatusOcorrencia;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static br.com.fiap.resolveai.model.StatusOcorrencia.*;

/** Acompanhamento da ocorrencia: mudancas de status validadas pela maquina de estados. */
@Service
public class HistoricoStatusService {

    // RN03: para onde cada status pode ir
    private static final Map<StatusOcorrencia, Set<StatusOcorrencia>> TRANSICOES = new EnumMap<>(StatusOcorrencia.class);

    static {
        TRANSICOES.put(ABERTA, EnumSet.of(CONFIRMADA, EM_ANALISE, REJEITADA, RESOLVIDA));
        TRANSICOES.put(CONFIRMADA, EnumSet.of(EM_ANALISE, ENCAMINHADA, RESOLVIDA, REJEITADA));
        TRANSICOES.put(EM_ANALISE, EnumSet.of(ENCAMINHADA, REJEITADA, RESOLVIDA));
        TRANSICOES.put(ENCAMINHADA, EnumSet.of(RESOLVIDA));
        TRANSICOES.put(RESOLVIDA, EnumSet.of(REABERTA));
        TRANSICOES.put(REABERTA, EnumSet.of(EM_ANALISE, ENCAMINHADA, RESOLVIDA));
        TRANSICOES.put(REJEITADA, EnumSet.noneOf(StatusOcorrencia.class));
    }

    private static final double RAIO_FOTO_METROS = 30;

    private final HistoricoStatusDAO historicoDAO;
    private final OcorrenciaDAO ocorrenciaDAO;
    private final UsuarioDAO usuarioDAO;
    private final OcorrenciaService ocorrenciaService;
    private final Transacao transacao;
    private final ModelMapper modelMapper;

    public HistoricoStatusService(HistoricoStatusDAO historicoDAO, OcorrenciaDAO ocorrenciaDAO, UsuarioDAO usuarioDAO,
                                  OcorrenciaService ocorrenciaService, Transacao transacao, ModelMapper modelMapper) {
        this.historicoDAO = historicoDAO;
        this.ocorrenciaDAO = ocorrenciaDAO;
        this.usuarioDAO = usuarioDAO;
        this.ocorrenciaService = ocorrenciaService;
        this.transacao = transacao;
        this.modelMapper = modelMapper;
    }

    /** Linha do tempo da ocorrencia (Ocorrencia 1:N HistoricoStatus). */
    public List<HistoricoStatusResponseDTO> listarPorOcorrencia(Long cdOcorrencia) {
        ocorrenciaService.buscarOcorrencia(cdOcorrencia);
        return historicoDAO.listarPorOcorrencia(cdOcorrencia).stream()
                .map(h -> modelMapper.map(h, HistoricoStatusResponseDTO.class))
                .toList();
    }

    public HistoricoStatusResponseDTO registrar(Long cdOcorrencia, HistoricoStatusRequestDTO dto) {
        Ocorrencia ocorrencia = ocorrenciaService.buscarOcorrencia(cdOcorrencia);
        StatusOcorrencia novo = dto.getStatus();

        // RN03: so transicoes previstas na maquina de estados
        if (!TRANSICOES.get(ocorrencia.getStatus()).contains(novo)) {
            throw new RegraNegocioException("Não dá para passar de " + ocorrencia.getStatus() + " para " + novo + ".");
        }

        // RN03 + UC03/UC10: "Foi resolvido" e "Contestar com foto" exigem quem informa e a posicao da foto, a ate 30 m
        if (novo == RESOLVIDA || novo == REABERTA) {
            validarFotoNoLocal(ocorrencia, dto);
        }

        long id = transacao.executar(conn -> {
            ocorrenciaDAO.atualizarStatus(conn, cdOcorrencia, novo);
            return historicoDAO.inserir(conn, cdOcorrencia, novo, dto.getObservacao());
        });
        return buscarPorId(id);
    }

    public HistoricoStatusResponseDTO buscarPorId(Long id) {
        return modelMapper.map(buscarHistorico(id), HistoricoStatusResponseDTO.class);
    }

    /** Corrige so a observacao: o status registrado faz parte do acompanhamento e nao muda. */
    public HistoricoStatusResponseDTO atualizar(Long id, ObservacaoHistoricoRequestDTO dto) {
        HistoricoStatus historico = buscarHistorico(id);
        historico.setObservacao(dto.getObservacao());
        historicoDAO.atualizar(historico);
        return modelMapper.map(historico, HistoricoStatusResponseDTO.class);
    }

    public void remover(Long id) {
        buscarHistorico(id);
        historicoDAO.remover(id);
    }

    private void validarFotoNoLocal(Ocorrencia ocorrencia, HistoricoStatusRequestDTO dto) {
        if (dto.getCdUsuario() == null || dto.getLatitude() == null || dto.getLongitude() == null) {
            throw new RequisicaoInvalidaException(
                    "Para informar que foi resolvido ou contestar, envie quem está informando e a localização da foto.");
        }
        if (usuarioDAO.buscarPorId(dto.getCdUsuario()).isEmpty()) {
            throw new RecursoNaoEncontradoException("Não encontramos o usuário " + dto.getCdUsuario() + ".");
        }
        double distancia = GeoUtils.distanciaMetros(
                ocorrencia.getLatitude(), ocorrencia.getLongitude(), dto.getLatitude(), dto.getLongitude());
        if (distancia > RAIO_FOTO_METROS) {
            throw new RegraNegocioException("Você está longe do local. Chegue mais perto para tirar a foto.");
        }
    }

    private HistoricoStatus buscarHistorico(Long id) {
        return historicoDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Não encontramos o registro de histórico " + id + "."));
    }
}
