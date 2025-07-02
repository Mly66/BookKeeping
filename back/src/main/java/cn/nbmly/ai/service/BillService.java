package cn.nbmly.ai.service;

import cn.nbmly.ai.dto.BillDTO;
import cn.nbmly.ai.dto.BillRequest;

import java.util.List;

public interface BillService {
    BillDTO createBill(BillRequest billRequest, Long userId);

    BillDTO getBillById(Long id, Long userId);

    List<BillDTO> getBillsByUser(Long userId);

    BillDTO updateBill(Long id, BillRequest billRequest, Long userId);

    void deleteBill(Long id, Long userId);
}