package com.expensetracker.controller;

import com.expensetracker.dto.TransactionDTO;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.Transaction.TransactionType;
import com.expensetracker.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:3000",
    "https://smartexpensetracker-frontend.onrender.com"
})
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Object> addTransaction(@Valid @RequestBody TransactionDTO dto) {
        try {
            // Block if expense exceeds current balance
            if (dto.getType() == TransactionType.EXPENSE) {
                BigDecimal balance = transactionService.getCurrentBalance(dto.getUserId());
                if (dto.getAmount().compareTo(balance) > 0) {
                    Map<String, Object> err = new HashMap<>();
                    err.put("error", "Insufficient balance! Your balance is ₹" +
                            balance.toPlainString() + ". Cannot add expense of ₹" +
                            dto.getAmount().toPlainString() + ". Please add income first.");
                    err.put("balance", balance);
                    return ResponseEntity.badRequest().body(err);
                }
            }
            return ResponseEntity.ok(transactionService.addTransaction(dto));
        } catch (RuntimeException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getAll(@PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getTransactionsByUser(userId));
    }

    @GetMapping("/user/{userId}/type")
    public ResponseEntity<List<Transaction>> getByType(
            @PathVariable Long userId, @RequestParam TransactionType type) {
        return ResponseEntity.ok(transactionService.getTransactionsByType(userId, type));
    }

    @GetMapping("/user/{userId}/range")
    public ResponseEntity<List<Transaction>> getByRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(transactionService.getTransactionsByDateRange(userId, startDate, endDate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @Valid @RequestBody TransactionDTO dto) {
        try {
            return ResponseEntity.ok(transactionService.updateTransaction(id, dto));
        } catch (RuntimeException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        Map<String, String> res = new HashMap<>();
        try {
            transactionService.deleteTransaction(id);
            res.put("message", "Transaction deleted successfully");
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("error", e.getMessage());
            return ResponseEntity.status(404).body(res);
        }
    }

    @GetMapping("/summary/{userId}")
    public ResponseEntity<Map<String, BigDecimal>> getSummary(@PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getSummary(userId));
    }

    @GetMapping("/summary/{userId}/monthly")
    public ResponseEntity<Map<String, BigDecimal>> getMonthly(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month) {
        if (year == 0 || month == 0) {
            LocalDate now = LocalDate.now();
            year  = now.getYear();
            month = now.getMonthValue();
        }
        return ResponseEntity.ok(transactionService.getMonthlyCategorySummary(userId, year, month));
    }

    @GetMapping("/balance/{userId}")
    public ResponseEntity<Map<String, BigDecimal>> getBalance(@PathVariable Long userId) {
        Map<String, BigDecimal> res = new HashMap<>();
        res.put("balance", transactionService.getCurrentBalance(userId));
        return ResponseEntity.ok(res);
    }
}
