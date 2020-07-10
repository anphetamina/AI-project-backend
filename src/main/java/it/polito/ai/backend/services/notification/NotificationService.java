package it.polito.ai.backend.services.notification;

import it.polito.ai.backend.dtos.TeamDTO;
import it.polito.ai.backend.dtos.TokenDTO;

import java.util.List;

public interface NotificationService {
    void sendMessage(String address, String subject, String body);

    boolean confirm(String token);
    boolean reject(String token);
    void notifyTeam(TeamDTO dto, List<String> memberIds);
    boolean addToken(TokenDTO token);
}
