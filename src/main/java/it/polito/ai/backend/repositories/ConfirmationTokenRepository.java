package it.polito.ai.backend.repositories;

import it.polito.ai.backend.entities.ConfirmationToken;
import it.polito.ai.backend.entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;

public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, String> {
    List<ConfirmationToken> findAllByExpiryDateBefore(Timestamp t);
}
