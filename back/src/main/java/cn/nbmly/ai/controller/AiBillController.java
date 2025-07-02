package cn.nbmly.ai.controller;

import cn.nbmly.ai.dto.BillDTO;
import cn.nbmly.ai.dto.CategoryDTO;
import cn.nbmly.ai.service.AiBillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/ai/bill")
public class AiBillController {

    private final AiBillService aiBillService;

    public AiBillController(AiBillService aiBillService) {
        this.aiBillService = aiBillService;
    }

    /**
     * 创建账单
     */
    @PostMapping("/create")
    public ResponseEntity<BillDTO> createBill(
            @RequestParam String description,
            @RequestParam Long userId) {
        try {
            BillDTO bill = aiBillService.createBill(description, userId);
            return ResponseEntity.ok(bill);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 查询账单
     */
    @GetMapping("/query")
    public ResponseEntity<List<BillDTO>> queryBills(
            @RequestParam String query,
            @RequestParam Long userId) {
        try {
            List<BillDTO> bills = aiBillService.queryBills(query, userId);
            return ResponseEntity.ok(bills);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新账单
     */
    @PutMapping("/update/{billId}")
    public ResponseEntity<BillDTO> updateBill(
            @PathVariable Long billId,
            @RequestParam String description,
            @RequestParam Long userId) {
        try {
            BillDTO bill = aiBillService.updateBill(billId, description, userId);
            return ResponseEntity.ok(bill);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除账单
     */
    @DeleteMapping("/delete/{billId}")
    public ResponseEntity<String> deleteBill(
            @PathVariable Long billId,
            @RequestParam Long userId) {
        try {
            aiBillService.deleteBill(billId, userId);
            return ResponseEntity.ok("账单删除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户分类
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getUserCategories(@RequestParam Long userId) {
        try {
            List<CategoryDTO> categories = aiBillService.getUserCategories(userId);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取账单统计
     */
    @GetMapping("/statistics")
    public ResponseEntity<String> getBillStatistics(@RequestParam Long userId) {
        try {
            String statistics = aiBillService.getBillStatistics(userId);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取统计失败：" + e.getMessage());
        }
    }
}