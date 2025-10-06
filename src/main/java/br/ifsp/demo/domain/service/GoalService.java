import br.ifsp.demo.domain.model.Expense;
import br.ifsp.demo.domain.model.ExpenseType;
import br.ifsp.demo.domain.model.GoalEvaluation;
import java.time.ZoneOffset;
import java.time.Instant;
import java.util.List;
import java.math.BigDecimal;

public GoalEvaluation evaluateMonthly(String userId, String rootCategoryId, YearMonth month) {
    if (userId == null || userId.isBlank())           throw new IllegalArgumentException("userId obrigatório");
    if (rootCategoryId == null || rootCategoryId.isBlank())
        throw new IllegalArgumentException("categoryId obrigatório");
    if (month == null)                                 throw new IllegalArgumentException("mês obrigatório");

    var goal = goalRepo.findByUserAndCategoryAndMonth(userId, rootCategoryId, month)
            .orElseThrow(() -> new IllegalArgumentException("meta inexistente"));

    String rootPath = categoryRepo.findPathById(rootCategoryId, userId);
    if (rootPath == null || rootPath.isBlank()) {
        throw new IllegalArgumentException("categoria raiz inexistente");
    }

    // intervalo [start, end] do mês em UTC
    Instant start = month.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    Instant end   = month.plusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant().minusNanos(1);

    List<Expense> txs = expenseRepo.findByUserAndPeriod(userId, start, end);

    BigDecimal spent = BigDecimal.ZERO;
    for (Expense e : txs) {
        if (e.type() != ExpenseType.DEBIT) continue;
        if (e.categoryId() == null) continue;
        String path = categoryRepo.findPathById(e.categoryId(), e.userId());
        if (path == null || path.isBlank()) continue;

        if (path.equals(rootPath) || path.startsWith(rootPath + "/")) {
            spent = spent.add(e.amount());
        }
    }

    boolean exceeded = spent.compareTo(goal.limitAmount()) > 0;
    BigDecimal diff = exceeded ? spent.subtract(goal.limitAmount()) : BigDecimal.ZERO;

    return new GoalEvaluation(
            userId, rootCategoryId, month,
            goal.limitAmount(), spent, exceeded, diff
    );
}