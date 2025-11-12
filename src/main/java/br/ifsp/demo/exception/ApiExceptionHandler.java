package br.ifsp.demo.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class ApiExceptionHandler {

    private ResponseEntity<ApiException> build(HttpStatus status, String message, Throwable e) {
        final ApiException apiException = ApiException.builder()
                .status(status)
                .message(message)
                .developerMessage(e != null ? e.getClass().getName() : null)
                .timestamp(ZonedDateTime.now(ZoneId.of("Z")))
                .build();
        return new ResponseEntity<>(apiException, status);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<?> handleNullPointerException(NullPointerException e){
        return build(BAD_REQUEST, e.getMessage(), e);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e){
        return build(BAD_REQUEST, e.getMessage(), e);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException e){
        return build(FORBIDDEN, e.getMessage(), e);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFoundException(EntityNotFoundException e){
        return build(NOT_FOUND, e.getMessage(), e);
    }

    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<?> handleEntityAlreadyExistsException(EntityAlreadyExistsException e){
        return build(CONFLICT, e.getMessage(), e);
    }

    // Parâmetros de rota/query com tipo inválido (ex.: enum/number/uuid) -> 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        String name = e.getName();
        String required = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "tipo esperado";
        return build(BAD_REQUEST, "Parâmetro \"" + name + "\" inválido (esperado: " + required + ")", e);
    }

    // Parâmetro obrigatório ausente em query -> 400
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        return build(BAD_REQUEST, "Parâmetro obrigatório ausente: " + e.getParameterName(), e);
    }

    // Bean validation em @RequestBody (se houver) -> 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        return build(BAD_REQUEST, "Requisição inválida: verifique os campos enviados", e);
    }

    // YearMonth inválido vindo em query (ex.: /goals/evaluate?month=DECEMBER) -> 400
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<?> handleDateTimeParse(DateTimeParseException e) {
        return build(BAD_REQUEST, "mês inválido: use o formato yyyy-MM (ex.: 2025-12)", e);
    }

    // JSON/body malformado ou conversões (Instant/YearMonth/BigDecimal/Enum) -> 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        final Throwable root = NestedExceptionUtils.getMostSpecificCause(e);
        final String rootMsg = String.valueOf(root);

        // Casos específicos detectáveis pelo Jackson:
        if (root instanceof InvalidFormatException ife && ife.getTargetType() != null) {
            final String target = ife.getTargetType().getSimpleName();

            // Enum (ExpenseType)
            if ("ExpenseType".equals(target)) {
                return build(BAD_REQUEST, "type inválido: use \"DEBIT\" ou \"CREDIT\"", e);
            }

            // YearMonth no corpo (DTO com YearMonth)
            if ("YearMonth".equals(target)) {
                return build(BAD_REQUEST, "mês inválido: use o formato yyyy-MM (ex.: 2025-12)", e);
            }

            // BigDecimal (amount/limit)
            if ("BigDecimal".equals(target)) {
                return build(BAD_REQUEST, "valor numérico inválido: use decimal (ex.: 300 ou 300.00)", e);
            }
        }

        // Instant (quando houver campo Instant no body)
        if (rootMsg.contains("Instant")) {
            return build(BAD_REQUEST,
                    "timestamp inválido: use ISO-8601, por exemplo \"2025-11-11T19:43:09.936Z\"",
                    e);
        }

        // BigDecimal genérico
        if (rootMsg.toLowerCase().contains("bigdecimal")) {
            return build(BAD_REQUEST,
                    "valor numérico inválido: use decimal (ex.: 300 ou 300.00)",
                    e);
        }

        // Fallback
        return build(BAD_REQUEST, "JSON inválido ou malformado", e);
    }
}