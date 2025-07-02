package cn.nbmly.ai.service;

import cn.nbmly.ai.dto.BillDTO;
import cn.nbmly.ai.dto.CategoryDTO;

import java.util.List;

public interface AiBillService {

    /**
     * 创建账单
     */
    BillDTO createBill(String description, Long userId);

    /**
     * 查询账单
     */
    List<BillDTO> queryBills(String query, Long userId);

    /**
     * 更新账单
     */
    BillDTO updateBill(Long billId, String description, Long userId);

    /**
     * 删除账单
     */
    void deleteBill(Long billId, Long userId);

    /**
     * 获取用户的所有分类
     */
    List<CategoryDTO> getUserCategories(Long userId);

    /**
     * 获取账单统计信息
     */
    String getBillStatistics(Long userId);

    /**
     * 删除指定分类的所有账单
     */
    List<BillDTO> deleteBillsByCategory(String categoryName, Long userId);

    /**
     * 查询特定分类的账单
     */
    List<BillDTO> queryBillsByCategory(String categoryName, Long userId);

    /**
     * 按时间范围查询账单
     */
    List<BillDTO> queryBillsByTimeRange(String startDate, String endDate, Long userId);
}