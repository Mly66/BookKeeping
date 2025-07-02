package cn.nbmly.ai.repository;

import cn.nbmly.ai.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    @Query(value = "SELECT * FROM bill WHERE user_id = ?1 ORDER BY bill_time DESC", nativeQuery = true)
    List<Bill> findByUserId(Long userId);
}