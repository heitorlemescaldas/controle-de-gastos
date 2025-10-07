package br.ifsp.demo.controller;

import br.ifsp.demo.domain.model.Report;
import br.ifsp.demo.domain.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService service;
    public ReportController(ReportService service){ this.service = service; }

    @GetMapping("/period")
    public ResponseEntity<Report> byPeriod(@RequestHeader("X-User") String userId,
                                           @RequestParam String start, @RequestParam String end){
        return ResponseEntity.ok(service.generate(userId, Instant.parse(start), Instant.parse(end)));
    }

    @GetMapping("/category-tree")
    public ResponseEntity<Report> byCategoryTree(@RequestHeader("X-User") String userId,
                                                 @RequestParam String start, @RequestParam String end,
                                                 @RequestParam String rootCategoryId){
        return ResponseEntity.ok(service.generateForCategoryTree(userId, Instant.parse(start), Instant.parse(end), rootCategoryId));
    }
}
