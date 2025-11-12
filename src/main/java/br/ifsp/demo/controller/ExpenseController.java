package br.ifsp.demo.controller;

import br.ifsp.demo.controller.dto.ExpenseResponse;
import br.ifsp.demo.domain.model.Expense;
import br.ifsp.demo.domain.model.ExpenseType;
import br.ifsp.demo.domain.service.ExpenseService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/expenses")
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    @ApiResponse(responseCode = "201", description = "Despesa registrada com sucesso")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExpenseResponse> create(
            @Parameter(
                    name = "X-User",
                    description = "E-mail do usuário autenticado",
                    in = ParameterIn.HEADER,
                    required = true,
                    example = "test@ex.com")
            @RequestHeader("X-User") String userId,
            @RequestBody CreateExpenseRequest req
    ) {
        var expense = Expense.of(
                userId,
                req.amount(),
                req.type(),
                req.description(),
                req.timestamp(),
                req.categoryId()
        );

        var saved = service.create(expense);
        return ResponseEntity.status(HttpStatus.CREATED).body(ExpenseResponse.fromDomain(saved));
    }

    public record CreateExpenseRequest(
            BigDecimal amount,
            ExpenseType type,
            String description,
            Instant timestamp,
            String categoryId
    ) {}
}