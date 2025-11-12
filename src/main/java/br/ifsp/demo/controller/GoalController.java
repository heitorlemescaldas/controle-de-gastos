package br.ifsp.demo.controller;

import br.ifsp.demo.controller.dto.GoalResponse;
import br.ifsp.demo.domain.model.Goal;
import br.ifsp.demo.domain.model.GoalEvaluation;
import br.ifsp.demo.domain.service.GoalService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/v1/goals")
public class GoalController {

    private final GoalService service;

    public GoalController(GoalService service){
        this.service = service;
    }

    @ApiResponse(responseCode = "201", description = "Meta mensal criada/atualizada")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GoalResponse> setMonthly(
            @Parameter(name = "X-User", description = "E-mail do usuário", in = ParameterIn.HEADER, required = true, example = "test@ex.com")
            @RequestHeader("X-User") String userId,
            @RequestBody SetGoalRequest req
    ){
        // Converte string "2025-12" em YearMonth
        YearMonth ym;
        try {
            ym = YearMonth.parse(req.month());
        } catch (Exception ex) {
            throw new IllegalArgumentException("mês inválido: use o formato yyyy-MM (ex.: 2025-12)");
        }

        Goal goal = service.setMonthlyGoal(userId, req.rootCategoryId(), ym, req.limit());
        return ResponseEntity.status(HttpStatus.CREATED).body(GoalResponse.fromDomain(goal));
    }

    @GetMapping(path = "/evaluate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GoalEvaluation> evaluate(
            @Parameter(name = "X-User", description = "E-mail do usuário", in = ParameterIn.HEADER, required = true, example = "test@ex.com")
            @RequestHeader("X-User") String userId,
            @RequestParam String rootCategoryId,
            @Parameter(
                description = "Mês no formato yyyy-MM (ex.: 2025-12)",
                example = "2025-12",
                required = true
            )
            @RequestParam String month
    ){
        YearMonth ym;
        try {
            ym = YearMonth.parse(month);
        } catch (Exception ex) {
            throw new IllegalArgumentException("mês inválido: use o formato yyyy-MM (ex.: 2025-12)");
        }

        return ResponseEntity.ok(service.evaluateMonthly(userId, rootCategoryId, ym));
    }

    // DTO ajustado — mês string simples
    public record SetGoalRequest(
            @Schema(description = "ID da categoria raiz", example = "b52e906f-709a-4616-a7a2-42ee00e2a5cb")
            String rootCategoryId,

            @Schema(description = "Mês no formato yyyy-MM (ex.: 2025-12)", example = "2025-12")
            String month,

            @Schema(description = "Limite de gastos em reais", example = "800.00")
            BigDecimal limit
    ) {}
}