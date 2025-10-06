package br.ifsp.demo.infra.persistence.repo;

import br.ifsp.demo.infra.persistence.entity.GoalEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GoalJpaRepository extends JpaRepository<GoalEntity, String> {

    @Query("select g from GoalEntity g where g.userId=:userId and g.categoryId=:categoryId and g.month=:month")
    Optional<GoalEntity> findMonthly(@Param("userId") String userId,
                                     @Param("categoryId") String categoryId,
                                     @Param("month") String month);
}