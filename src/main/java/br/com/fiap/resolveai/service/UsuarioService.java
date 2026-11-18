package br.com.fiap.resolveai.service;

import br.com.fiap.resolveai.dao.BairroDAO;
import br.com.fiap.resolveai.dao.OcorrenciaDAO;
import br.com.fiap.resolveai.dao.UsuarioDAO;
import br.com.fiap.resolveai.dto.OcorrenciaResponseDTO;
import br.com.fiap.resolveai.dto.UsuarioRequestDTO;
import br.com.fiap.resolveai.dto.UsuarioResponseDTO;
import br.com.fiap.resolveai.exception.ConflitoException;
import br.com.fiap.resolveai.exception.RecursoNaoEncontradoException;
import br.com.fiap.resolveai.model.Usuario;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/** Cadastro de moradores: e-mail unico, senha so como hash BCrypt e nunca devolvida. */
@Service
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;
    private final BairroDAO bairroDAO;
    private final OcorrenciaDAO ocorrenciaDAO;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public UsuarioService(UsuarioDAO usuarioDAO, BairroDAO bairroDAO, OcorrenciaDAO ocorrenciaDAO,
                          BCryptPasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.usuarioDAO = usuarioDAO;
        this.bairroDAO = bairroDAO;
        this.ocorrenciaDAO = ocorrenciaDAO;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    public List<UsuarioResponseDTO> listar() {
        return usuarioDAO.listar().stream()
                .map(u -> modelMapper.map(u, UsuarioResponseDTO.class))
                .toList();
    }

    public UsuarioResponseDTO buscarPorId(Long id) {
        return modelMapper.map(buscarUsuario(id), UsuarioResponseDTO.class);
    }

    public UsuarioResponseDTO cadastrar(UsuarioRequestDTO dto) {
        // RN08 (UC14): um e-mail, uma conta
        if (usuarioDAO.buscarPorEmail(dto.getEmail()).isPresent()) {
            throw new ConflitoException("Este e-mail já tem conta.");
        }
        validarBairro(dto.getCdBairro());
        Usuario usuario = modelMapper.map(dto, Usuario.class);
        // RN08 (UC14): senha guardada so como hash BCrypt
        usuario.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
        return modelMapper.map(usuarioDAO.inserir(usuario), UsuarioResponseDTO.class);
    }

    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = buscarUsuario(id);
        // RN08: o novo e-mail nao pode ser de outra conta
        usuarioDAO.buscarPorEmail(dto.getEmail())
                .filter(outro -> !outro.getId().equals(id))
                .ifPresent(outro -> {
                    throw new ConflitoException("Este e-mail já tem conta.");
                });
        validarBairro(dto.getCdBairro());
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setCdBairro(dto.getCdBairro());
        usuario.setPlano(dto.getPlano());
        usuario.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
        usuarioDAO.atualizar(usuario);
        return buscarPorId(id);
    }

    public void remover(Long id) {
        buscarUsuario(id);
        // RN04: usuario com ocorrencias, apoios ou denuncias nao pode ser apagado
        if (usuarioDAO.contarVinculos(id) > 0) {
            throw new ConflitoException("Não dá para remover: existem registros ligados a este item.");
        }
        usuarioDAO.remover(id);
    }

    /** "Minhas ocorrencias" da tela Perfil (Usuario 1:N Ocorrencia). */
    public List<OcorrenciaResponseDTO> listarOcorrencias(Long id) {
        buscarUsuario(id);
        return ocorrenciaDAO.listarPorUsuario(id).stream()
                .map(o -> modelMapper.map(o, OcorrenciaResponseDTO.class))
                .toList();
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Não encontramos o usuário " + id + "."));
    }

    private void validarBairro(Long cdBairro) {
        if (bairroDAO.buscarPorId(cdBairro).isEmpty()) {
            throw new RecursoNaoEncontradoException("Não encontramos o bairro " + cdBairro + ".");
        }
    }
}
