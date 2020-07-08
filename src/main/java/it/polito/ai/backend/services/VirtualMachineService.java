package it.polito.ai.backend.services;

import it.polito.ai.backend.dtos.VirtualMachineConfigurationDTO;
import it.polito.ai.backend.dtos.VirtualMachineDTO;
import it.polito.ai.backend.dtos.VirtualMachineModelDTO;

import java.util.List;

public interface VirtualMachineService {

    /**
     * student
     */
    VirtualMachineDTO createVM(int numVcpu, int diskSpace, int RAM);
    boolean deleteVM(Long id);
    boolean updateVM(VirtualMachineDTO vm);
    boolean turnOnVM(Long id);
    boolean turnOffVM(Long id);
    boolean addOwnerToVM(String studentId, Long vmId);
    boolean removeOwnerFromVM(String studentId, Long vmId);

    /**
     * teacher
     */
    VirtualMachineConfigurationDTO createVMConfiguration(int numVcpu, int diskSpace, int RAM);
    boolean addVMConfigurationToTeam(VirtualMachineConfigurationDTO configuration, Long teamId);
    boolean updateVMConfiguration(VirtualMachineConfigurationDTO configuration);
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
    VirtualMachineModelDTO getVMModelForCourse(String courseName);

    /**
     * teacher/student
     */
    VirtualMachineDTO getVirtualMachine(Long id);
    List<VirtualMachineDTO> getVMsForTeam(Long teamId);
    VirtualMachineConfigurationDTO getVMConfigurationForTeam(Long teamId);
    Integer getVcpuForTeam(Long teamId);
    Integer getDiskSpaceForTeam(Long teamId);
    Integer getRAMForTeam(Long teamId);
}
