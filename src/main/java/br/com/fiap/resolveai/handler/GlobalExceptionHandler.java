package br.com.fiap.resolveai.handler;

import br.com.fiap.resolveai.dto.CampoErroDTO;
import br.com.fiap.resolveai.dto.ErroResponseDTO;
import br.com.fiap.resolveai.exception.ConflitoException;
import br.com.fiap.resolveai.exception.PersistenciaException;
import br.com.fiap.resolveai.exception.RecursoNaoEncontradoException;
import br.com.fiap.resolveai.exception.RegraNegocioException;
import br.com.fiap.resolveai.exception.RequisicaoInvalidaException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Converte toda excecao no JSON de erro padronizado (ErroResponseDTO).
 * Nenhuma resposta de erro sai como HTML, stack trace ou SQL.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String DADOS_INVALIDOS = "Confira os dados enviados.";
    private static final String ERRO_INTERNO = "Algo deu errado do nosso lado. Tente de novo em instantes.";

    /** 400: Bean Validation do @Valid, com a lista de campos que falharam. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> tratarValidacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<CampoErroDTO> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new CampoErroDTO(erro.getField(), erro.getDefaultMessage()))
                .toList();
        return montar(HttpStatus.BAD_REQUEST, DADOS_INVALIDOS, request, campos);
    }

    /** 400: JSON quebrado, tipo errado no path/parametro ou valor de enum que nao existe. */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ErroResponseDTO> tratarDadosInvalidos(Exception ex, HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST, DADOS_INVALIDOS, request, List.of());
    }

    /** 400: parametro obrigatorio de consulta nao enviado. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponseDTO> tratarParametroFaltando(MissingServletRequestParameterException ex,
                                                                   HttpServletRequest request) {
        List<CampoErroDTO> campos = List.of(new CampoErroDTO(ex.getParameterName(), "Informe este parâmetro."));
        return montar(HttpStatus.BAD_REQUEST, DADOS_INVALIDOS, request, campos);
    }

    /** 400: dado obrigatorio ou filtro inconsistente detectado pelo service. */
    @ExceptionHandler(RequisicaoInvalidaException.class)
    public ResponseEntity<ErroResponseDTO> tratarRequisicaoInvalida(RequisicaoInvalidaException ex,
                                                                    HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST, ex.getMessage(), request, List.of());
    }

    /** 404: recurso do dominio nao encontrado. */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> tratarNaoEncontrado(RecursoNaoEncontradoException ex,
                                                               HttpServletRequest request) {
        return montar(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
    }

    /** 404: rota que nao existe na API. */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErroResponseDTO> tratarRotaInexistente(Exception ex, HttpServletRequest request) {
        return montar(HttpStatus.NOT_FOUND, "Não encontramos esta rota.", request, List.of());
    }

    /** 405: metodo HTTP nao aceito na rota (ex.: DELETE de denuncia). Informa os metodos aceitos no Allow. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponseDTO> tratarMetodoNaoSuportado(HttpRequestMethodNotSupportedException ex,
                                                                    HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        if (ex.getSupportedHttpMethods() != null) {
            headers.setAllow(ex.getSupportedHttpMethods());
        }
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .headers(headers)
                .body(corpo(HttpStatus.METHOD_NOT_ALLOWED, "Este método não é aceito nesta rota.", request, List.of()));
    }

    /** 415: corpo enviado sem Content-Type application/json. */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponseDTO> tratarMidiaNaoSuportada(HttpMediaTypeNotSupportedException ex,
                                                                   HttpServletRequest request) {
        return montar(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Envie os dados em JSON (application/json).", request, List.of());
    }

    /** 409: regra de negocio ou conflito de dados. */
    @ExceptionHandler({RegraNegocioException.class, ConflitoException.class})
    public ResponseEntity<ErroResponseDTO> tratarConflito(RuntimeException ex, HttpServletRequest request) {
        return montar(HttpStatus.CONFLICT, ex.getMessage(), request, List.of());
    }

    /** 500: falha no banco. O detalhe vai so para o log do servidor. */
    @ExceptionHandler(PersistenciaException.class)
    public ResponseEntity<ErroResponseDTO> tratarPersistencia(PersistenciaException ex, HttpServletRequest request) {
        log.error("Falha de persistencia em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, ERRO_INTERNO, request, List.of());
    }

    /** 500: qualquer erro inesperado, sempre com mensagem generica. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> tratarErroInesperado(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, ERRO_INTERNO, request, List.of());
    }

    private ResponseEntity<ErroResponseDTO> montar(HttpStatus status, String mensagem, HttpServletRequest request,
                                                   List<CampoErroDTO> campos) {
        return ResponseEntity.status(status).body(corpo(status, mensagem, request, campos));
    }

    private ErroResponseDTO corpo(HttpStatus status, String mensagem, HttpServletRequest request,
                                  List<CampoErroDTO> campos) {
        return new ErroResponseDTO(
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                request.getRequestURI(),
                campos);
    }
}
