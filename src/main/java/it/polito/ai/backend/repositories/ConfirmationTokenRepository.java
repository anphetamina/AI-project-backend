package it.polito.ai.backend.repositories;

import it.polito.ai.backend.entities.ConfirmationToken;
import it.polito.ai.backend.entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, String> {
    List<ConfirmationToken> findAllByExpiryDateBefore(Timestamp t);
}
