package it.polito.ai.backend.repositories;

import it.polito.ai.backend.entities.Team;
import it.polito.ai.backend.entities.VirtualMachine;
import it.polito.ai.backend.entities.VirtualMachineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    @Query("select coalesce(sum(v.num_vcpu), 0) from Team t inner join t.virtualMachines v where t.id=:teamId and v.status=it.polito.ai.backend.entities.VirtualMachineStatus.ON")
    int getActiveNumVcpuByTeam(Long teamId);

    @Query("select coalesce(sum(v.disk_space), 0) from Team t inner join t.virtualMachines v where t.id=:teamId and v.status=it.polito.ai.backend.entities.VirtualMachineStatus.ON")
    int getActiveDiskSpaceByTeam(Long teamId);

    @Query("select coalesce(sum(v.ram), 0) from Team t inner join t.virtualMachines v where t.id=:teamId and v.status=it.polito.ai.backend.entities.VirtualMachineStatus.ON")
    int getActiveRamByTeam(Long teamId);

    @Query("select count(v) from Team t inner join t.virtualMachines v where v.status=:status and t.id=:teamId")
    int countVirtualMachinesByTeamAndStatus(Long teamId, VirtualMachineStatus status);

    @Query("select v from Team t inner join t.virtualMachines v where v.status=:status and t.id=:teamId")
    List<VirtualMachine> getVirtualMachinesByTeamAndStatus(Long teamId, VirtualMachineStatus status);

    @Query("select count(v) from Team t inner join t.virtualMachines v where t.id=:teamId")
    int countVirtualMachinesByTeam(Long teamId);
}
