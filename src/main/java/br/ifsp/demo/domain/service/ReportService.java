package br.ifsp.demo.domain.service;

import br.ifsp.demo.domain.model.*;
import br.ifsp.demo.domain.port.ExpenseRepositoryPort;
import br.ifsp.demo.domain.port.CategoryRepositoryPort;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final ExpenseRepositoryPort expenseRepo;
    private final CategoryRepositoryPort categoryRepo;

    @Autowired
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
                if (p != null && !p.isBlank()) {
                    path = p;
                } else {
                    continue;
                }
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

    public Report generateForCategoryTree(String userId, Instant start, Instant end, String rootCategoryId) {
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("userId obrigatório");
        if (rootCategoryId == null || rootCategoryId.isBlank())
            throw new IllegalArgumentException("rootCategoryId obrigatório");
        if (start == null || end == null || start.isAfter(end))
            throw new IllegalArgumentException("período inválido");

        String rootPath = categoryRepo.findPathById(rootCategoryId, userId);
        if (rootPath == null || rootPath.isBlank())
            throw new IllegalArgumentException("categoria raiz inexistente");

        List<Expense> txs = expenseRepo.findByUserAndPeriod(userId, start, end);

        Map<String, ReportItem> map = new HashMap<>();
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (Expense e : txs) {
            if (e.categoryId() == null) continue; // fora da árvore
            String path = categoryRepo.findPathById(e.categoryId(), e.userId());
            if (path == null || path.isBlank()) continue;

            // filtra por árvore: path == rootPath OU começa com rootPath + "/"
            if (!(path.equals(rootPath) || path.startsWith(rootPath + "/"))) continue;

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
        Collections.sort(items);

        BigDecimal balance = totalCredit.subtract(totalDebit);
        return new Report(userId, start, end, totalDebit, totalCredit, balance, items);
    }
}