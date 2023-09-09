package antifraud.service;

import antifraud.model.Enum.TransactionAction;
import antifraud.model.dto.TransactionDto;
import antifraud.model.entity.DetectionLimit;
import antifraud.request.TransactionFeedbackRequest;
import antifraud.response.TransactionResponse;
import antifraud.model.entity.Transaction;
import antifraud.model.entity.User;
import antifraud.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AntiFraudService {

    private final static String INFO_NONE = "none";
    private final static String INFO_AMOUNT = "amount";
    private final static String INFO_IP = "ip";
    private final static String INFO_CARD_NUMBER = "card-number";
    private final static String INFO_IP_CORRELATION = "ip-correlation";
    private final static String INFO_REGION_CORRELATION = "region-correlation";
    private final static int DEFAULT_ALLOWED_LIMIT = 200;
    private final static int DEFAULT_MANUAL_LIMIT = 1500;
    private final UserDetailRepository userDetailRepository;
    private final SuspiciousIpRepository suspiciousIpRepository;
    private final StolenCardRepository stolenCardRepository;
    private final TransactionRepository transactionRepository;
    private final DetectionLimitRepository detectionLimitRepository;

    @Autowired
    public AntiFraudService(UserDetailRepository userDetailRepository, SuspiciousIpRepository suspiciousIpRepository,
                            StolenCardRepository stolenCardRepository, TransactionRepository transactionRepository, DetectionLimitRepository detectionLimitRepository) {
        this.userDetailRepository = userDetailRepository;
        this.suspiciousIpRepository = suspiciousIpRepository;
        this.stolenCardRepository = stolenCardRepository;
        this.transactionRepository = transactionRepository;
        this.detectionLimitRepository = detectionLimitRepository;
    }


    public TransactionResponse transaction(TransactionDto request) {

        // check if the current user is locked
        checkIsCurrentUserLocked();

        Transaction tempTransaction;

        // save transaction and check format
        try {
            tempTransaction = transactionRepository.save(new
                    Transaction(request.getAmount(), request.getIp(), request.getNumber(),
                    request.getRegion(), request.getDate()));
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format!");
        }

        // create utils
        TransactionResponse transactionResponse = new TransactionResponse();
        List<String> infos = new ArrayList<>();

        // blacklist checking
        checkSuspiciousIp(request, transactionResponse, infos);
        checkStolenCard(request, transactionResponse, infos);

        // region or ip checking
        List<Transaction> transactionsInLastHour =
                transactionRepository.findAllByNumberAndDateTimeBetween(
                        request.getNumber(),
                        request.getDate().minusHours(1),
                        request.getDate());

        long distinctIpCount = countDistinctIp(transactionsInLastHour);
        long distinctRegionCount = countDistinctRegion(transactionsInLastHour);

        checkDistinctIp(distinctIpCount, transactionResponse, infos);
        checkDistinctRegion(distinctRegionCount, transactionResponse, infos);


        // getting the detection limit
        String cardNumber = tempTransaction.getNumber();
        Optional<DetectionLimit> detectionLimitOptional = detectionLimitRepository.findByCardNumber(cardNumber);

        long allowedLimit = detectionLimitOptional.map(DetectionLimit::getAllowedLimit).orElse((long) DEFAULT_ALLOWED_LIMIT);
        long manualLimit = detectionLimitOptional.map(DetectionLimit::getManualLimit).orElse((long) DEFAULT_MANUAL_LIMIT);

        // amount checking
        checkAmount(request.getAmount(), transactionResponse, infos, allowedLimit, manualLimit);

        // sort the info and return the response
        String info = infos.stream()
                .sorted().collect(Collectors.joining(", "));

        transactionResponse.setInfo(info);

        // save the result into the transaction in the database
        tempTransaction.setResult(transactionResponse.getResult());
        transactionRepository.save(tempTransaction);

        return transactionResponse;
    }

    private void checkSuspiciousIp(TransactionDto request,
                                   TransactionResponse response,
                                   List<String> infos) {

        boolean isIPBlacklisted = suspiciousIpRepository.findByIp(request.getIp()).isPresent();
        if (isIPBlacklisted) {
            response.setResult(TransactionAction.PROHIBITED);
            infos.add(INFO_IP);
        }
    }

    private void checkStolenCard(TransactionDto request,
                                 TransactionResponse response,
                                 List<String> infos) {

        boolean isCardBlacklisted = stolenCardRepository.findByNumber(request.getNumber()).isPresent();
        if (isCardBlacklisted) {
            response.setResult(TransactionAction.PROHIBITED);
            infos.add(INFO_CARD_NUMBER);
        }
    }


    private void checkDistinctIp(long distinctIpCount,
                                 TransactionResponse response,
                                 List<String> infos) {
        if (distinctIpCount > 3) {
            response.setResult(TransactionAction.PROHIBITED);
            infos.add(INFO_IP_CORRELATION);
        } else if (distinctIpCount == 3) {
            response.setResult(TransactionAction.MANUAL_PROCESSING);
            infos.add(INFO_IP_CORRELATION);
        }

    }

    private void checkDistinctRegion(long distinctRegionCount,
                                 TransactionResponse response,
                                 List<String> infos) {
        if (distinctRegionCount > 3) {
            response.setResult(TransactionAction.PROHIBITED);
            infos.add(INFO_REGION_CORRELATION);
        } else if (distinctRegionCount == 3) {
            response.setResult(TransactionAction.MANUAL_PROCESSING);
            infos.add(INFO_REGION_CORRELATION);
        }

    }

    private void checkAmount(long amount, TransactionResponse response,
                             List<String> infos,
                             long allowedLimit,
                             long manualLimit) {

        if (amount <= allowedLimit) {
            if (infos.isEmpty()) {
                response.setResult(TransactionAction.ALLOWED);
                infos.add(INFO_NONE);
            }

        } else if (amount > allowedLimit && amount <= manualLimit) {
            if (response.getResult() != TransactionAction.PROHIBITED) {
                response.setResult(TransactionAction.MANUAL_PROCESSING);
                infos.add(INFO_AMOUNT);
            }

        } else {
            response.setResult(TransactionAction.PROHIBITED);
            infos.add(INFO_AMOUNT);
        }
    }

    private void checkIsCurrentUserLocked() {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User savedUser = userDetailRepository.findByUsername(user.getUsername()).get();

        if (!savedUser.isAccountNonLocked()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Your account is locked");
        }
    }

    private long countDistinctIp(List<Transaction> transactions) {
        return transactions.stream()
                .map(Transaction::getIp)
                .distinct()
                .count();
    }

    private long countDistinctRegion(List<Transaction> transactions) {
        return transactions.stream()
                .map(Transaction::getRegion)
                .distinct()
                .count();
    }

    public List<TransactionDto> findTransactionHistoryByNumber(String number) {

        checkIsCurrentUserLocked();

        if (transactionRepository.findAllByNumber(number).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Cannot found transaction history for card number: " + number);
        }

        return transactionRepository.findAllByNumber(number)
                .stream()
                .map(t -> new TransactionDto(
                        t.getId(),
                        t.getAmount(),
                        t.getIp(),
                        t.getNumber(),
                        t.getRegion(),
                        t.getDateTime(),
                        Optional.ofNullable(t.getResult()).map(Enum::name).orElse(""),
                        Optional.ofNullable(t.getFeedback()).map(Enum::name).orElse("")))
                .collect(Collectors.toList());
    }

    public List<TransactionDto> findTransactionHistory() {

        checkIsCurrentUserLocked();

        return transactionRepository.findAll().stream()
                .map(t -> new TransactionDto(
                        t.getId(),
                        t.getAmount(),
                        t.getIp(),
                        t.getNumber(),
                        t.getRegion(),
                        t.getDateTime(),
                        Optional.ofNullable(t.getResult()).map(Enum::name).orElse(""),
                        Optional.ofNullable(t.getFeedback()).map(Enum::name).orElse("")))
                .collect(Collectors.toList());
    }

    public Transaction putTransactionFeedback(TransactionFeedbackRequest request) {

        checkIsCurrentUserLocked();

        if (!transactionRepository.findById(request.getTransactionId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "The transaction is not found, id: " + request.getTransactionId());
        }

        Transaction savedTransaction = transactionRepository.findById(request.getTransactionId()).get();

        if (savedTransaction.getFeedback() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The transaction already have a feedback");
        }

        // if the feedback and the status are the same
        if (savedTransaction.getResult() == request.getFeedback()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
        }


        DetectionLimit tempDetectionLimit;

        // find the DetectionLimit for the card number
        // if no saved detection limit -> create an empty one
        if (!detectionLimitRepository.findByCardNumber(savedTransaction.getNumber()).isPresent()) {
            tempDetectionLimit = new DetectionLimit(
                    savedTransaction.getNumber(),
                    DEFAULT_ALLOWED_LIMIT,
                    DEFAULT_MANUAL_LIMIT);
        } else {
            tempDetectionLimit = detectionLimitRepository.findByCardNumber(savedTransaction.getNumber()).get();
        }


        TransactionAction transactionResult = savedTransaction.getResult();
        TransactionAction transactionFeedback = request.getFeedback();

        handleDetectionLimitChange(tempDetectionLimit, transactionResult, transactionFeedback, savedTransaction.getAmount());

        savedTransaction.setFeedback(request.getFeedback());

        return transactionRepository.save(savedTransaction);
    }

    private void handleDetectionLimitChange(DetectionLimit tempDetectionLimit,
                                   TransactionAction transactionResult,
                                   TransactionAction transactionFeedback,
                                   long amount) {

        long currentAllowedLimit = tempDetectionLimit.getAllowedLimit();
        long currentManualLimit = tempDetectionLimit.getManualLimit();

        if (transactionResult == TransactionAction.ALLOWED
                && transactionFeedback == TransactionAction.MANUAL_PROCESSING) {

            tempDetectionLimit.setAllowedLimit(
                    changeLimit(currentAllowedLimit, amount, false));


        } else if (transactionResult == TransactionAction.ALLOWED
                && transactionFeedback == TransactionAction.PROHIBITED) {

            tempDetectionLimit.setAllowedLimit(
                    changeLimit(currentAllowedLimit, amount, false));

            tempDetectionLimit.setManualLimit(
                    changeLimit(currentManualLimit, amount, false));


        } else if (transactionResult == TransactionAction.MANUAL_PROCESSING
                && transactionFeedback == TransactionAction.ALLOWED) {

            tempDetectionLimit.setAllowedLimit(
                    changeLimit(currentAllowedLimit, amount, true));


        } else if (transactionResult == TransactionAction.MANUAL_PROCESSING
                && transactionFeedback == TransactionAction.PROHIBITED) {

            tempDetectionLimit.setManualLimit(
                    changeLimit(currentManualLimit, amount, false));

        } else if (transactionResult == TransactionAction.PROHIBITED
                && transactionFeedback == TransactionAction.ALLOWED) {

            tempDetectionLimit.setAllowedLimit(
                    changeLimit(currentAllowedLimit, amount, true));

            tempDetectionLimit.setManualLimit(
                    changeLimit(currentManualLimit, amount, true));

        } else if (transactionResult == TransactionAction.PROHIBITED
                && transactionFeedback == TransactionAction.MANUAL_PROCESSING) {

            tempDetectionLimit.setManualLimit(
                    changeLimit(currentManualLimit, amount, true));

        }

        detectionLimitRepository.save(tempDetectionLimit);

    }

    private long changeLimit(long currentLimit, long amount, boolean isIncreaseLimit) {

        if (!isIncreaseLimit) {
            amount *= -1;
        }

        return (long) Math.ceil(0.8 * currentLimit + 0.2 * amount);
    }





}
