package it.polito.ai.backend.repositories;

import it.polito.ai.backend.entities.VirtualMachine;
import it.polito.ai.backend.entities.VirtualMachineModel;
import it.polito.ai.backend.entities.VirtualMachineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VirtualMachineModelRepository extends JpaRepository<VirtualMachineModel, Long> {
}
