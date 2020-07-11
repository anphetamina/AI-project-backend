package it.polito.ai.backend.repositories;

import it.polito.ai.backend.entities.Team;
import it.polito.ai.backend.entities.VirtualMachineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    @Query("")
    Integer getNumVcpuInUseByTeam(Long teamId);
    @Query("")
    Integer getDiskSpaceInUseByTeam(Long teamId);
    @Query("")
    Integer getRamInUseByTeam(Long teamId);

    Integer countVirtualMachinesByStatusAndTeam(VirtualMachineStatus status, Long teamId);
}
