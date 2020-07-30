package it.polito.ai.backend.repositories;

import it.polito.ai.backend.entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, String> {
    List<Token> findAllByExpiryDateBefore(Timestamp t);

    List<Token> findAllByTeamId(Long teamId);

}
