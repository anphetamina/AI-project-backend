package it.polito.ai.backend.services.vm;

import it.polito.ai.backend.dtos.VirtualMachineConfigurationDTO;
import it.polito.ai.backend.dtos.VirtualMachineDTO;
import it.polito.ai.backend.dtos.VirtualMachineModelDTO;
import it.polito.ai.backend.entities.SystemImage;
import it.polito.ai.backend.entities.VirtualMachine;
import it.polito.ai.backend.entities.VirtualMachineModel;

import java.util.List;
import java.util.Optional;

public interface VirtualMachineService {

    /**
     * student
     */
    VirtualMachineDTO createVirtualMachine(String studentId, // owner of the vm
                                           Long teamId, // to fetch the configuration
                                           Long configurationId, // to check if the configuration is owned by the team
                                           String courseName, // to check if the model is valid
                                           Long modelId, // to create the vm
                                           int numVcpu,
                                           int diskSpace,
                                           int ram);
    VirtualMachineDTO updateVirtualMachine(String studentId, VirtualMachineDTO virtualMachine);
    boolean deleteVirtualMachine(Long id);
    void turnOnVirtualMachine(Long id);
    void turnOffVirtualMachine(Long id);
    boolean addOwnerToVirtualMachine(String studentId, Long vmId);
    boolean removeOwnerFromVirtualMachine(String studentId, Long vmId);

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
                                                int max_on,
                                                int tot
                                                );
    VirtualMachineConfigurationDTO updateVirtualMachineConfiguration(VirtualMachineConfigurationDTO configuration);
    VirtualMachineModelDTO createVirtualMachineModel(String courseName, SystemImage os);
    boolean deleteVirtualMachineModel(Long id);
    List<VirtualMachineDTO> getVirtualMachinesForModel(Long modelId);
    List<VirtualMachineDTO> getVirtualMachinesForCourse(String courseName);

    /**
     * teacher/student
     */
    Optional<VirtualMachineDTO> getVirtualMachine(Long id);
    List<VirtualMachineDTO> getVirtualMachinesForTeam(Long teamId);
    Optional<VirtualMachineModelDTO> getVirtualMachineModel(Long id);
    Optional<VirtualMachineModelDTO> getVirtualMachineModelForCourse(String courseName);
    Optional<VirtualMachineConfigurationDTO> getVirtualMachineConfiguration(Long id);
    Optional<VirtualMachineConfigurationDTO> getVirtualMachineConfigurationForTeam(Long teamId);
    int getActiveVcpuForTeam(Long teamId);
    int getActiveDiskSpaceForTeam(Long teamId);
    int getActiveRAMForTeam(Long teamId);
    int getCountActiveVirtualMachinesForTeam(Long teamId);
    List<VirtualMachine> getActiveVirtualMachinesForTeam(Long teamId);
    int getCountVirtualMachinesForTeam(Long teamId);
}
