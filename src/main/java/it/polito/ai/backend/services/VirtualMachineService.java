package it.polito.ai.backend.services;

import it.polito.ai.backend.dtos.VirtualMachineConfigurationDTO;
import it.polito.ai.backend.dtos.VirtualMachineDTO;
import it.polito.ai.backend.dtos.VirtualMachineModelDTO;

import java.util.List;
import java.util.Optional;

public interface VirtualMachineService {

    /**
     * student
     */
    VirtualMachineDTO createVM(String studentId, Long teamId, int numVcpu, int diskSpace, int ram);
    boolean deleteVM(Long id);
    VirtualMachineDTO updateVM(VirtualMachineDTO vm);
    void turnOnVM(Long id);
    void turnOffVM(Long id);
    boolean addOwnerToVM(String studentId, Long vmId);
    boolean removeOwnerFromVM(String studentId, Long vmId);

    /**
     * teacher
     */
    VirtualMachineConfigurationDTO createVMConfiguration(String teacherId, Long teamId, int numVcpu, int diskSpace, int ram);
    boolean addVMConfigurationToTeam(VirtualMachineConfigurationDTO configuration, Long teamId);
    VirtualMachineConfigurationDTO updateVMConfiguration(VirtualMachineConfigurationDTO configuration);
    VirtualMachineModelDTO createVMModel(
                            int min_vcpu,
                            int max_vcpu,
                            int min_disk,
                            int max_disk,
                            int min_ram,
                            int max_ram,
                            int tot_vcpu,
                            int tot_disk,
                            int tot_ram,
                            int tot,
                            int max_on);
    VirtualMachineModelDTO updateVMModel(VirtualMachineModelDTO model);
    boolean addVMConfigurationToCourse(VirtualMachineConfigurationDTO configuration, String courseName);
    Optional<VirtualMachineModelDTO> getVMModelForCourse(String courseName);

    /**
     * teacher/student
     */
    Optional<VirtualMachineDTO> getVirtualMachine(Long id);
    List<VirtualMachineDTO> getVMsForTeam(Long teamId);
    Optional<VirtualMachineConfigurationDTO> getVMConfigurationForTeam(Long teamId);
    Integer getVcpuForTeam(Long teamId);
    Integer getDiskSpaceForTeam(Long teamId);
    Integer getRAMForTeam(Long teamId);
}
