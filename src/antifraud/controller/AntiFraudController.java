package antifraud.controller;

import antifraud.request.TransactionFeedbackRequest;
import antifraud.response.AntifraudDeleteResponse;
import antifraud.model.dto.TransactionDto;
import antifraud.response.TransactionResponse;
import antifraud.model.entity.SuspiciousIp;
import antifraud.model.entity.StolenCard;
import antifraud.model.entity.Transaction;
import antifraud.service.TransactionService;
import antifraud.service.StolenCardService;
import antifraud.service.SuspiciousIpService;
import antifraud.validator.CardNumber;
import antifraud.validator.Ipv4;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/antifraud")
public class AntiFraudController {

    private final TransactionService transactionService;
    private final SuspiciousIpService suspiciousIpService;
    private final StolenCardService stolenCardService;

    @Autowired
    public AntiFraudController(TransactionService transactionService, SuspiciousIpService suspiciousIpService, StolenCardService stolenCardService) {
        this.transactionService = transactionService;
        this.suspiciousIpService = suspiciousIpService;
        this.stolenCardService = stolenCardService;
    }

    @PostMapping("/transaction")
    public TransactionResponse transaction(@Valid @RequestBody TransactionDto request) {
        return transactionService.transaction(request);
    }

    @PutMapping("/transaction")
    public Transaction putTransactionFeedback(@Valid @RequestBody TransactionFeedbackRequest transactionFeedbackRequest) {
        return transactionService.putTransactionFeedback(transactionFeedbackRequest);
    }

    @GetMapping("/history")
    public List<TransactionDto> getTransactionHistory() {
        return transactionService.findTransactionHistory();
    }

    @GetMapping("/history/{number}")
    public List<TransactionDto> getTransactionHistoryByNumber(@CardNumber @PathVariable String number) {
        return transactionService.findTransactionHistoryByNumber(number);
    }

    @PostMapping("/suspicious-ip")
    public SuspiciousIp saveSusIp(@Valid @RequestBody SuspiciousIp suspiciousIp) {
        return suspiciousIpService.saveSusIp(suspiciousIp);
    }

    @DeleteMapping("/suspicious-ip/{ip}")
    public AntifraudDeleteResponse deleteSusIp(@Ipv4 @PathVariable String ip) {
        return suspiciousIpService.deleteSusIp(ip);
    }

    @GetMapping("/suspicious-ip")
    public List<SuspiciousIp> getAllSusIps() {
        return suspiciousIpService.getAllSusIps();
    }

    @PostMapping("/stolencard")
    public StolenCard saveStolenCard(@Valid @RequestBody StolenCard stolenCard) {
        return stolenCardService.saveStolenCard(stolenCard);
    }

    @DeleteMapping("/stolencard/{number}")
    public AntifraudDeleteResponse deleteStolenCard(@CardNumber @PathVariable String number) {
        return stolenCardService.deleteStolenCard(number);
    }

    @GetMapping("/stolencard")
    public List<StolenCard> getAllStolenCards() {
        return stolenCardService.getAllStolenCards();
    }
}
