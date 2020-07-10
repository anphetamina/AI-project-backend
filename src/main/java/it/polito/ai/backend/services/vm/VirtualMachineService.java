package it.polito.ai.backend.services.vm;

import it.polito.ai.backend.dtos.VirtualMachineConfigurationDTO;
import it.polito.ai.backend.dtos.VirtualMachineDTO;
import it.polito.ai.backend.dtos.VirtualMachineModelDTO;
import it.polito.ai.backend.entities.OperatingSystem;

import java.util.List;
import java.util.Optional;

public interface VirtualMachineService {

    /**
     * student
     */
    VirtualMachineDTO createVirtualMachine(String studentId, Long teamId, int numVcpu, int diskSpace, int ram);
    boolean deleteVirtualMachine(Long id);
    boolean deleteAllVirtualMachinesForTeam(Long teamId);
    // VirtualMachineDTO updateVirtualMachine(VirtualMachineDTO vm);
    void turnOnVirtualMachine(Long id);
    void turnOffVirtualMachine(Long id);
    boolean addOwnerToVirtualMachine(String studentId, Long vmId);
    boolean removeOwnerFromVirtualMachine(String studentId, Long vmId);

    VirtualMachineModelDTO createVirtualMachineModel(OperatingSystem os);
    boolean deleteVirtualMachineModel(Long id);
    // VirtualMachineModelDTO updateVirtualMachineModel(VirtualMachineModelDTO model);
    boolean addVirtualMachineModelToTeam(Long vmmId, Long teamId);
    Optional<VirtualMachineModelDTO> getVirtualMachineModel(Long id);


    /**
     * teacher
     */
    VirtualMachineConfigurationDTO createVirtualMachineConfiguration(
                                                Long teamId,
                                                int min_vcpu,
                                                int max_vcpu,
                                                int min_disk,
                                                int max_disk,
                                                int min_ram,
                                                int max_ram,
                                                int tot,
                                                int max_on
    );
    VirtualMachineConfigurationDTO updateVirtualMachineConfiguration(VirtualMachineConfigurationDTO configuration);
    boolean addVirtualMachineConfigurationToTeam(Long vmcId, Long teamId);

    /**
     * teacher/student
     */
    Optional<VirtualMachineDTO> getVirtualMachine(Long id);
    List<VirtualMachineDTO> getVirtualMachinesForTeam(Long teamId);
    Optional<VirtualMachineConfigurationDTO> getVirtualMachineConfiguration(Long id);
    Optional<VirtualMachineConfigurationDTO> getVirtualMachineConfigurationForTeam(Long teamId);
    Integer getVcpuForTeam(Long teamId);
    Integer getDiskSpaceForTeam(Long teamId);
    Integer getRAMForTeam(Long teamId);
    Integer getOnVirtualMachinesForTeam(Long teamId);
}
