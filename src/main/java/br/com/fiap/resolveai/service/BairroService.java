package br.com.fiap.resolveai.service;

import br.com.fiap.resolveai.dao.BairroDAO;
import br.com.fiap.resolveai.dto.BairroResponseDTO;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/** Consulta dos bairros atendidos (dados de referencia, somente leitura). */
@Service
public class BairroService {

    private final BairroDAO bairroDAO;
    private final ModelMapper modelMapper;

    public BairroService(BairroDAO bairroDAO, ModelMapper modelMapper) {
        this.bairroDAO = bairroDAO;
        this.modelMapper = modelMapper;
    }

    public List<BairroResponseDTO> listar() {
        return bairroDAO.listar().stream()
                .map(b -> modelMapper.map(b, BairroResponseDTO.class))
                .toList();
    }
}
