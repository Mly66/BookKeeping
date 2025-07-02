package cn.nbmly.ai.controller;

import cn.nbmly.ai.dto.BillDTO;
import cn.nbmly.ai.dto.BillRequest;
import cn.nbmly.ai.entity.User;
import cn.nbmly.ai.service.BillService;
import cn.nbmly.ai.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Slf4j
public class BillController {

    private final BillService billService;
    private final UserService userService;

    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username);
    }

    @PostMapping
    public ResponseEntity<BillDTO> createBill(@Valid @RequestBody BillRequest billRequest,
            Authentication authentication) {
        log.info("Creating bill with request: {}", billRequest);
        User currentUser = getCurrentUser(authentication);
        log.info("Current user: {}", currentUser.getUsername());
        BillDTO createdBill = billService.createBill(billRequest, currentUser.getId());
        return new ResponseEntity<>(createdBill, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BillDTO>> getUserBills(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<BillDTO> bills = billService.getBillsByUser(currentUser.getId());
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillDTO> getBillById(@PathVariable Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        BillDTO bill = billService.getBillById(id, currentUser.getId());
        return ResponseEntity.ok(bill);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BillDTO> updateBill(@PathVariable Long id, @Valid @RequestBody BillRequest billRequest,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        BillDTO updatedBill = billService.updateBill(id, billRequest, currentUser.getId());
        return ResponseEntity.ok(updatedBill);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBill(@PathVariable Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        billService.deleteBill(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}