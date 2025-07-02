package cn.nbmly.ai.repository;

import cn.nbmly.ai.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query(value = "SELECT * FROM category WHERE user_id = ?1 AND type = ?2", nativeQuery = true)
    List<Category> findByUserIdAndType(Long userId, String type);

    @Query(value = "SELECT * FROM category WHERE user_id = ?1", nativeQuery = true)
    List<Category> findByUserId(Long userId);
}