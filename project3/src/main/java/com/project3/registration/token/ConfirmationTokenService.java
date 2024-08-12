package com.project3.registration.token;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@AllArgsConstructor
@Service
public class ConfirmationTokenService {
    private final ConfirmationTokenRepository confirmationTokenRepository;

    public void saveConfirmationToken(ConfirmationToken token){
        this.confirmationTokenRepository.save(token);
    }
    public Optional<ConfirmationToken> getToken(String token){
       return  this.confirmationTokenRepository.findByToken(token);
    }

    public void setConfirmedAt(String token){
        confirmationTokenRepository.updateConfirmedAt(token, LocalDateTime.now());
    }
}
