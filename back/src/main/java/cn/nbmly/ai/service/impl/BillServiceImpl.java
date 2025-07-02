package cn.nbmly.ai.service.impl;

import cn.nbmly.ai.dto.BillDTO;
import cn.nbmly.ai.dto.BillRequest;
import cn.nbmly.ai.entity.Bill;
import cn.nbmly.ai.entity.Category;
import cn.nbmly.ai.repository.BillRepository;
import cn.nbmly.ai.repository.CategoryRepository;
import cn.nbmly.ai.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public BillDTO createBill(BillRequest billRequest, Long userId) {
        Category category = categoryRepository.findById(billRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        if (!category.getUserId().equals(userId)) {
            throw new RuntimeException("不能使用他人的分类");
        }
        if (!category.getType().equals(billRequest.getType())) {
            throw new RuntimeException("账单类型与分类类型不匹配");
        }

        Bill bill = new Bill();
        bill.setUserId(userId);
        bill.setCategoryId(billRequest.getCategoryId());
        bill.setAmount(billRequest.getAmount());
        bill.setType(billRequest.getType());
        bill.setRemarks(billRequest.getRemarks());
        bill.setBillTime(billRequest.getBillTime());

        Bill savedBill = billRepository.save(bill);
        return toDto(savedBill, category.getName());
    }

    @Override
    public BillDTO getBillById(Long id, Long userId) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("账单不存在"));
        if (!bill.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问该账单");
        }
        Category category = categoryRepository.findById(bill.getCategoryId())
                .orElse(null); // Category might be deleted
        return toDto(bill, category != null ? category.getName() : "未知分类");
    }

    @Override
    public List<BillDTO> getBillsByUser(Long userId) {
        List<Bill> bills = billRepository.findByUserId(userId);
        List<Long> categoryIds = bills.stream().map(Bill::getCategoryId).distinct().toList();
        Map<Long, Category> categoryMap = categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        return bills.stream()
                .map(bill -> {
                    Category category = categoryMap.get(bill.getCategoryId());
                    return toDto(bill, category != null ? category.getName() : "未知分类");
                })
                .collect(Collectors.toList());
    }

    @Override
    public BillDTO updateBill(Long id, BillRequest billRequest, Long userId) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("账单不存在"));
        if (!bill.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改该账单");
        }

        Category category = categoryRepository.findById(billRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        if (!category.getUserId().equals(userId)) {
            throw new RuntimeException("不能使用他人的分类");
        }
        if (!category.getType().equals(billRequest.getType())) {
            throw new RuntimeException("账单类型与分类类型不匹配");
        }

        bill.setCategoryId(billRequest.getCategoryId());
        bill.setAmount(billRequest.getAmount());
        bill.setType(billRequest.getType());
        bill.setRemarks(billRequest.getRemarks());
        bill.setBillTime(billRequest.getBillTime());

        Bill updatedBill = billRepository.save(bill);
        return toDto(updatedBill, category.getName());
    }

    @Override
    public void deleteBill(Long id, Long userId) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("账单不存在"));
        if (!bill.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该账单");
        }
        billRepository.delete(bill);
    }

    private BillDTO toDto(Bill bill, String categoryName) {
        BillDTO dto = new BillDTO();
        dto.setId(bill.getId());
        dto.setUserId(bill.getUserId());
        dto.setCategoryId(bill.getCategoryId());
        dto.setCategoryName(categoryName);
        dto.setAmount(bill.getAmount());
        dto.setType(bill.getType());
        dto.setRemarks(bill.getRemarks());
        dto.setBillTime(bill.getBillTime());
        return dto;
    }
}