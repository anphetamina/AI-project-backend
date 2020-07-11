package it.polito.ai.backend.services.vm;

import it.polito.ai.backend.dtos.VirtualMachineConfigurationDTO;
import it.polito.ai.backend.dtos.VirtualMachineDTO;
import it.polito.ai.backend.dtos.VirtualMachineModelDTO;
import it.polito.ai.backend.entities.SystemImage;

import java.util.List;
import java.util.Optional;

public interface VirtualMachineService {

    /**
     * student
     */
    VirtualMachineDTO createVirtualMachine(String studentId, Long modelId, int numVcpu, int diskSpace, int ram);
    VirtualMachineDTO updateVirtualMachine(String studentId, VirtualMachineDTO vm);
    boolean deleteVirtualMachine(Long id);
    void turnOnVirtualMachine(Long id);
    void turnOffVirtualMachine(Long id);
    boolean addOwnerToVirtualMachine(String studentId, Long vmId);
    boolean removeOwnerFromVirtualMachine(String studentId, Long vmId);

    VirtualMachineModelDTO createVirtualMachineModel(SystemImage os);
    boolean deleteVirtualMachineModel(Long id);
    boolean addVirtualMachineModelToTeam(Long modelId, Long teamId);
    Optional<VirtualMachineModelDTO> getVirtualMachineModel(Long id);


    /**
     * teacher
     */
    VirtualMachineConfigurationDTO createVirtualMachineConfiguration(
                                                Long teamId,
                                                int min_vcpu,
                                                int max_vcpu,
                                                int min_disk_space,
                                                int max_disk_space,
                                                int min_ram,
                                                int max_ram,
                                                int tot,
                                                int max_on
    );
    VirtualMachineConfigurationDTO updateVirtualMachineConfiguration(VirtualMachineConfigurationDTO configuration);
    boolean addVirtualMachineConfigurationToTeam(Long configurationId, Long teamId);

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
