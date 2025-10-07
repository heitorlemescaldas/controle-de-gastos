package br.ifsp.demo.controller;

import br.ifsp.demo.domain.model.Goal;
import br.ifsp.demo.domain.model.GoalEvaluation;
import br.ifsp.demo.domain.service.GoalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/v1/goals")
public class GoalController {

    private final GoalService service;
    public GoalController(GoalService service){ this.service = service; }

    @PostMapping
    public ResponseEntity<Goal> setMonthly(@RequestHeader("X-User") String userId, @RequestBody SetGoalRequest req){
        return ResponseEntity.ok(service.setMonthlyGoal(userId, req.rootCategoryId(), YearMonth.parse(req.month()), new BigDecimal(req.limit())));
    }

    @GetMapping("/evaluate")
    public ResponseEntity<GoalEvaluation> evaluate(@RequestHeader("X-User") String userId,
                                                   @RequestParam String rootCategoryId,
                                                   @RequestParam String month){
        return ResponseEntity.ok(service.evaluateMonthly(userId, rootCategoryId, YearMonth.parse(month)));
    }

    public record SetGoalRequest(String rootCategoryId, String month, String limit){}
}