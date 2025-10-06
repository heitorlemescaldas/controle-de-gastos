package br.ifsp.demo.domain.service;

import br.ifsp.demo.domain.model.*;
import br.ifsp.demo.domain.port.ExpenseRepositoryPort;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public class ReportService {

    private final ExpenseRepositoryPort expenseRepo;
    private final CategoryRepositoryPort categoryRepo;

    public ReportService(ExpenseRepositoryPort expenseRepo, CategoryRepositoryPort categoryRepo) {
        this.expenseRepo = expenseRepo;
        this.categoryRepo = categoryRepo;
    }

    public Report generate(String userId, Instant start, Instant end) {
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("userId obrigatório");
        if (start == null || end == null || start.isAfter(end))
            throw new IllegalArgumentException("período inválido");

        List<Expense> txs = expenseRepo.findByUserAndPeriod(userId, start, end);

        Map<String, ReportItem> map = new HashMap<>();
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (Expense e : txs) {
            String path = "Sem categoria";
            if (e.categoryId() != null) {
                String p = categoryRepo.findPathById(e.categoryId(), e.userId());
                if (p != null && !p.isBlank()) path = p;
            }
            ReportItem current = map.getOrDefault(path, new ReportItem(path, BigDecimal.ZERO, BigDecimal.ZERO));
            if (e.type() == ExpenseType.DEBIT) {
                current = current.addDebit(e.amount());
                totalDebit = totalDebit.add(e.amount());
            } else {
                current = current.addCredit(e.amount());
                totalCredit = totalCredit.add(e.amount());
            }
            map.put(path, current);
        }

        List<ReportItem> items = new ArrayList<>(map.values());
        Collections.sort(items); // ordena por path

        BigDecimal balance = totalCredit.subtract(totalDebit);

        return new Report(userId, start, end, totalDebit, totalCredit, balance, items);
    }
}