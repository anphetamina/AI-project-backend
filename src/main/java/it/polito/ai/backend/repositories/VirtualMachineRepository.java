package it.polito.ai.backend.repositories;

import it.polito.ai.backend.entities.VirtualMachine;
import it.polito.ai.backend.entities.VirtualMachineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VirtualMachineRepository extends JpaRepository<VirtualMachine, Long> {
    @Query("select sum(v.num_vcpu) from VirtualMachine v where v.team.id=:teamId")
    Integer getNumVcpuInUseByTeam(Long teamId);
    @Query("select sum(v.disk_space) from VirtualMachine v where v.team.id=:teamId")
    Integer getDiskSpaceInUseByTeam(Long teamId);
    @Query("select sum(v.ram) from VirtualMachine v where v.team.id=:teamId")
    Integer getRamInUseByTeam(Long teamId);

    Integer countVirtualMachinesByStatusEqualsAndTeamId(VirtualMachineStatus status, Long teamId);
}
