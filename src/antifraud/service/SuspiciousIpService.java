package antifraud.service;

import antifraud.response.AntifraudDeleteResponse;
import antifraud.model.entity.SuspiciousIp;
import antifraud.repository.SuspiciousIpRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SuspiciousIpService {

    private final SuspiciousIpRepository suspiciousIpRepository;
    private final static String IP_DELETE_MESSAGE = "IP %s successfully removed!";

    @Autowired
    public SuspiciousIpService(SuspiciousIpRepository suspiciousIpRepository) {
        this.suspiciousIpRepository = suspiciousIpRepository;
    }

    public SuspiciousIp saveSusIp(SuspiciousIp suspiciousIp) {
        if (suspiciousIpRepository.findByIp(suspiciousIp.getIp()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "This IP address has already been saved: " + suspiciousIp.getIp());
        }

        SuspiciousIp savedSuspiciousIp = suspiciousIpRepository.save(suspiciousIp);
        return savedSuspiciousIp;
    }

    public List<SuspiciousIp> getAllSusIps() {
        return suspiciousIpRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }


    @Transactional
    public AntifraudDeleteResponse deleteSusIp(String ip) {

        if (!suspiciousIpRepository.findByIp(ip).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Cannot found the IP address: " + ip);
        }
        suspiciousIpRepository.deleteByIp(ip);

        return new AntifraudDeleteResponse(IP_DELETE_MESSAGE.formatted(ip));
    }

}
