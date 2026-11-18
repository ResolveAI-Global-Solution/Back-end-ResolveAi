package br.com.fiap.resolveai.service;

import br.com.fiap.resolveai.dao.PlacarDAO;
import br.com.fiap.resolveai.dto.PlacarItemResponseDTO;
import br.com.fiap.resolveai.exception.RequisicaoInvalidaException;
import br.com.fiap.resolveai.model.PlacarItem;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/** Placar por bairro (UC05): quanto foi resolvido e em quanto tempo. */
@Service
public class PlacarService {

    private static final String FONTE = "ResolveAI (dados do app)";

    private final PlacarDAO placarDAO;
    private final ModelMapper modelMapper;

    public PlacarService(PlacarDAO placarDAO, ModelMapper modelMapper) {
        this.placarDAO = placarDAO;
        this.modelMapper = modelMapper;
    }

    public List<PlacarItemResponseDTO> listar(Long cdCategoria, LocalDate inicio, LocalDate fim) {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new RequisicaoInvalidaException("A data de início precisa vir antes da data de fim.");
        }
        LocalDate hoje = LocalDate.now();
        return placarDAO.listar(cdCategoria, inicio, fim).stream()
                .map(item -> paraDTO(item, hoje))
                .toList();
    }

    private PlacarItemResponseDTO paraDTO(PlacarItem item, LocalDate hoje) {
        PlacarItemResponseDTO dto = modelMapper.map(item, PlacarItemResponseDTO.class);
        int total = item.getAbertas() + item.getResolvidas();
        double percentual = total == 0 ? 0 : Math.round(item.getResolvidas() * 1000.0 / total) / 10.0;
        dto.setPercentualResolvido(percentual);
        dto.setFonte(FONTE);
        dto.setAtualizadoEm(hoje);
        return dto;
    }
}
