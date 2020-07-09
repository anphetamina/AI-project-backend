package it.polito.ai.backend.repositories;

import it.polito.ai.backend.entities.VirtualMachine;
import it.polito.ai.backend.entities.VirtualMachineModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VirtualMachineModelRepository extends JpaRepository<VirtualMachineModel, Long> {
}
