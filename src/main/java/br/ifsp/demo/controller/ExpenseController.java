package br.ifsp.demo.controller;

import br.ifsp.demo.domain.model.Expense;
import br.ifsp.demo.domain.model.ExpenseType;
import br.ifsp.demo.domain.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/expenses")
public class ExpenseController {

    private final ExpenseService service;
    public ExpenseController(ExpenseService service){ this.service = service; }

    @PostMapping
    public ResponseEntity<?> create(@RequestHeader("X-User") String userId, @RequestBody CreateExpenseRequest req){
        var e = Expense.of(userId, new BigDecimal(req.amount()), ExpenseType.valueOf(req.type()),
                req.description(), Instant.parse(req.timestamp()), req.categoryId());
        return ResponseEntity.ok(service.create(e));
    }

    public record CreateExpenseRequest(String amount, String type, String description, String timestamp, String categoryId) {}
}