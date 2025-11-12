package br.ifsp.demo.infra.persistence.repo;

import br.ifsp.demo.domain.model.CategoryNode;
import br.ifsp.demo.infra.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, String> {

    boolean existsByIdAndUserId(String id, String userId);

    @Query("select (count(c)>0) from CategoryEntity c " +
           "where c.userId=:userId and ((:parentId is null and c.parentId is null) or c.parentId=:parentId) " +
           "and lower(c.name)=:normalized")
    boolean existsSiblingByNormalized(@Param("userId") String userId,
                                      @Param("parentId") String parentId,
                                      @Param("normalized") String normalized);

    @Query("select (count(c)>0) from CategoryEntity c where c.userId=:userId and c.path=:path")
    boolean existsByUserAndPath(@Param("userId") String userId, @Param("path") String path);

    @Query("select (count(c)>0) from CategoryEntity c where c.userId=:userId and c.parentId=:id")
    boolean hasChildren(@Param("id") String id, @Param("userId") String userId);

    void deleteByIdAndUserId(String id, String userId);

    @Query("select c.path from CategoryEntity c where c.id=:id and c.userId=:userId")
    String findPath(@Param("id") String id, @Param("userId") String userId);

    @Query("select c from CategoryEntity c where c.userId=:userId order by c.path asc")
    List<CategoryEntity> findAllOrdered(@Param("userId") String userId);

    @Modifying
    @Query("update CategoryEntity c set c.name=:newName, c.path=:newPath " +
           "where c.id=:id and c.userId=:userId")
    int rename(@Param("id") String id, @Param("userId") String userId,
               @Param("newName") String newName, @Param("newPath") String newPath);

    @Modifying
    @Query("update CategoryEntity c set c.path = concat(:newPrefix, substring(c.path, length(:oldPrefix)+1)) " +
           "where c.userId=:userId and c.path like concat(:oldPrefix, '%')")
    int updatePathPrefix(@Param("userId") String userId,
                         @Param("oldPrefix") String oldPrefix,
                         @Param("newPrefix") String newPrefix);

    @Modifying
    @Query("update CategoryEntity c set c.parentId=:newParentId, c.path=:newPath " +
           "where c.id=:id and c.userId=:userId")
    int move(@Param("id") String id, @Param("userId") String userId,
             @Param("newParentId") String newParentId, @Param("newPath") String newPath);

    // monta CategoryNode direto via JPQL constructor expression
    @Query("""
           select new br.ifsp.demo.domain.model.CategoryNode(
               c.id, c.userId, c.name, c.parentId, c.path
           )
           from CategoryEntity c
           where c.id = :id and c.userId = :userId
           """)
    Optional<CategoryNode> findNodeById(@Param("id") String id, @Param("userId") String userId);
}