package br.com.fiap.resolveai.service;

import br.com.fiap.resolveai.dao.CategoriaDAO;
import br.com.fiap.resolveai.dto.CategoriaRequestDTO;
import br.com.fiap.resolveai.dto.CategoriaResponseDTO;
import br.com.fiap.resolveai.exception.ConflitoException;
import br.com.fiap.resolveai.exception.RecursoNaoEncontradoException;
import br.com.fiap.resolveai.model.Categoria;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/** Cadastro das categorias de problema, sem nome repetido e sem apagar categoria em uso. */
@Service
public class CategoriaService {

    private final CategoriaDAO categoriaDAO;
    private final ModelMapper modelMapper;

    public CategoriaService(CategoriaDAO categoriaDAO, ModelMapper modelMapper) {
        this.categoriaDAO = categoriaDAO;
        this.modelMapper = modelMapper;
    }

    public List<CategoriaResponseDTO> listar() {
        return categoriaDAO.listar().stream()
                .map(c -> modelMapper.map(c, CategoriaResponseDTO.class))
                .toList();
    }

    public CategoriaResponseDTO buscarPorId(Long id) {
        return modelMapper.map(buscarCategoria(id), CategoriaResponseDTO.class);
    }

    public CategoriaResponseDTO cadastrar(CategoriaRequestDTO dto) {
        // RN04: nome de categoria repetido -> 409
        if (categoriaDAO.buscarPorNome(dto.getNome()).isPresent()) {
            throw new ConflitoException("Já existe uma categoria com esse nome.");
        }
        Categoria categoria = categoriaDAO.inserir(modelMapper.map(dto, Categoria.class));
        return modelMapper.map(categoria, CategoriaResponseDTO.class);
    }

    public CategoriaResponseDTO atualizar(Long id, CategoriaRequestDTO dto) {
        Categoria categoria = buscarCategoria(id);
        // RN04: o novo nome nao pode ser de outra categoria
        categoriaDAO.buscarPorNome(dto.getNome())
                .filter(outra -> !outra.getId().equals(id))
                .ifPresent(outra -> {
                    throw new ConflitoException("Já existe uma categoria com esse nome.");
                });
        categoria.setNome(dto.getNome());
        categoria.setDescricao(dto.getDescricao());
        categoriaDAO.atualizar(categoria);
        return modelMapper.map(categoria, CategoriaResponseDTO.class);
    }

    public void remover(Long id) {
        buscarCategoria(id);
        // RN04: categoria com ocorrencias ligadas nao pode sumir
        if (categoriaDAO.contarOcorrencias(id) > 0) {
            throw new ConflitoException("Não dá para remover: existem registros ligados a este item.");
        }
        categoriaDAO.remover(id);
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Não encontramos a categoria " + id + "."));
    }
}
