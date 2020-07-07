package it.polito.ai.backend.services;

import it.polito.ai.backend.entities.Token;
import it.polito.ai.backend.repositories.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;

@Component
@Transactional
@EnableScheduling
public class ScheduledTasks {

    @Autowired
    TokenRepository tokenRepository;

    /*
    * every day at 4am
    * */
    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanTokens() {
        List<Token> tokens = tokenRepository.findAllByExpiryDateBefore(Utils.getNow());
        System.out.println("Found "+tokens.size()+" tokens to be removed");
        tokenRepository.deleteAll(tokens);
        System.out.println("Token repository cleaned");
    }

}
