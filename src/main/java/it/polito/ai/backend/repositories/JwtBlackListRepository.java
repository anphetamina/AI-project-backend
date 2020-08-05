package it.polito.ai.backend.repositories;

import it.polito.ai.backend.entities.JwtBlackList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JwtBlackListRepository extends JpaRepository<JwtBlackList,String> {
}
