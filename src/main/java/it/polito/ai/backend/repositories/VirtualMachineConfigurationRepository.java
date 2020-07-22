package it.polito.ai.backend.repositories;

import it.polito.ai.backend.entities.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VirtualMachineConfigurationRepository extends JpaRepository<Configuration, Long> {
}
