package it.polito.ai.backend.repositories;

import it.polito.ai.backend.entities.VirtualMachineConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VirtualMachineConfigurationRepository extends JpaRepository<VirtualMachineConfiguration, Long> {
}
