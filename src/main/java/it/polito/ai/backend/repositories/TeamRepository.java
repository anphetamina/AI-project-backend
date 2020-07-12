package it.polito.ai.backend.repositories;

import it.polito.ai.backend.entities.Team;
import it.polito.ai.backend.entities.VirtualMachineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    @Query("select sum(v.num_vcpu) from Team t inner join t.vm_models m inner join m.virtual_machines v where t.id=:teamId and v.status=it.polito.ai.backend.entities.VirtualMachineStatus.ON")
    Integer getNumVcpuInUseByTeam(Long teamId);

    @Query("select sum(v.disk_space) from Team t inner join t.vm_models m inner join m.virtual_machines v where t.id=:teamId and v.status=it.polito.ai.backend.entities.VirtualMachineStatus.ON")
    Integer getDiskSpaceInUseByTeam(Long teamId);

    @Query("select sum(v.ram) from Team t inner join t.vm_models m inner join m.virtual_machines v where t.id=:teamId and v.status=it.polito.ai.backend.entities.VirtualMachineStatus.ON")
    Integer getRamInUseByTeam(Long teamId);

    @Query("select count(v) from Team t inner join t.vm_models m inner join m.virtual_machines v where v.status=:status and t.id=:teamId")
    Integer countVirtualMachinesByStatusAndTeam(VirtualMachineStatus status, Long teamId);
}
