package it.polito.ai.backend.services.notification;

import it.polito.ai.backend.dtos.StudentDTO;
import it.polito.ai.backend.dtos.TeamDTO;
import it.polito.ai.backend.dtos.TokenDTO;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface NotificationService {
    void sendMessage(String address, String subject, String body, String sender);

    String confirm(String token);
    String reject(String token);
    void notifyTeam(TeamDTO dto, List<String> memberIds, Timestamp timeout, String proponent);
    boolean addToken(TokenDTO token);
    List<TokenDTO> getTokenTeam(Long teamId);
}
