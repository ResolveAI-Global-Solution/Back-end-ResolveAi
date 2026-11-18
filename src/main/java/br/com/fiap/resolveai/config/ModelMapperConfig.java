package br.com.fiap.resolveai.config;

import br.com.fiap.resolveai.dto.BairroResponseDTO;
import br.com.fiap.resolveai.dto.CategoriaResumoDTO;
import br.com.fiap.resolveai.dto.OcorrenciaResponseDTO;
import br.com.fiap.resolveai.dto.UsuarioResponseDTO;
import br.com.fiap.resolveai.model.Bairro;
import br.com.fiap.resolveai.model.Ocorrencia;
import br.com.fiap.resolveai.model.Usuario;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ModelMapper em modo STRICT: so copia campos com nome exatamente igual.
 * Onde o nome muda entre model e DTO, o mapeamento e declarado aqui.
 */
@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        mapper.typeMap(Ocorrencia.class, OcorrenciaResponseDTO.class)
                .addMappings(m -> {
                    m.map(Ocorrencia::getNomeBairro, OcorrenciaResponseDTO::setBairro);
                    m.map(Ocorrencia::getNomeMunicipio, OcorrenciaResponseDTO::setMunicipio);
                })
                .setPostConverter(contexto -> {
                    Ocorrencia origem = contexto.getSource();
                    contexto.getDestination().setCategoria(
                            new CategoriaResumoDTO(origem.getCdCategoria(), origem.getNomeCategoria()));
                    return contexto.getDestination();
                });

        mapper.typeMap(Usuario.class, UsuarioResponseDTO.class)
                .addMappings(m -> m.map(Usuario::getNomeBairro, UsuarioResponseDTO::setBairro));

        mapper.typeMap(Bairro.class, BairroResponseDTO.class)
                .addMappings(m -> m.map(Bairro::getNomeMunicipio, BairroResponseDTO::setMunicipio));

        return mapper;
    }
}
