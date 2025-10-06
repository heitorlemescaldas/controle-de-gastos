package br.ifsp.demo.infra.persistence.repo;

import br.ifsp.demo.infra.persistence.entity.ExpenseEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

@Repository
public interface ExpenseJpaRepository extends JpaRepository<ExpenseEntity, String> {

    @Query("select (count(e)>0) from ExpenseEntity e where e.userId=:userId and e.categoryId=:categoryId")
    boolean existsByUserAndCategory(@Param("userId") String userId, @Param("categoryId") String categoryId);

    @Query("select e from ExpenseEntity e where e.userId=:userId and e.timestamp between :start and :end")
    List<ExpenseEntity> findByUserAndPeriod(@Param("userId") String userId,
                                            @Param("start") Instant start,
                                            @Param("end") Instant end);
}