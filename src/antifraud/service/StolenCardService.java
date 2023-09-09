package antifraud.service;

import antifraud.response.AntifraudDeleteResponse;
import antifraud.model.entity.StolenCard;
import antifraud.repository.StolenCardRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class StolenCardService {
    private final StolenCardRepository stolenCardRepository;
    private final static String CARD_DELETE_MESSAGE = "Card %s successfully removed!";


    @Autowired
    public StolenCardService(StolenCardRepository stolenCardRepository) {
        this.stolenCardRepository = stolenCardRepository;
    }

    public StolenCard saveStolenCard(StolenCard stolenCard) {

        if (stolenCardRepository.findByNumber(stolenCard.getNumber()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "This stolen card has already been saved: " + stolenCard.getNumber());
        }


        StolenCard savedStolenCard = stolenCardRepository.save(stolenCard);

        return savedStolenCard;
    }

    public List<StolenCard> getAllStolenCards() {
        return stolenCardRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Transactional
    public AntifraudDeleteResponse deleteStolenCard(String number) {

        if (!stolenCardRepository.findByNumber(number).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Cannot found this stolen card: " + number);
        }

        stolenCardRepository.deleteByNumber(number);

        return new AntifraudDeleteResponse(CARD_DELETE_MESSAGE.formatted(number));
    }

}
