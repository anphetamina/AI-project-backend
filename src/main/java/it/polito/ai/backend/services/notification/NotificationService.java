package it.polito.ai.backend.services.notification;

import it.polito.ai.backend.dtos.StudentDTO;
import it.polito.ai.backend.dtos.TeamDTO;
import it.polito.ai.backend.dtos.TokenDTO;

import java.sql.Timestamp;
import java.util.List;

public interface NotificationService {
    void sendMessage(String address, String subject, String body, String sender);

    boolean confirm(String token);
    boolean reject(String token);
    void notifyTeam(TeamDTO dto, List<String> memberIds, Timestamp timeout, StudentDTO studentDTO);
    boolean addToken(TokenDTO token);
}
