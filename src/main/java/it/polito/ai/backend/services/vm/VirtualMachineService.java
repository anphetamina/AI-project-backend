package it.polito.ai.backend.services.vm;

import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.entities.SystemImage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface VirtualMachineService {

    /**
     * student
     */
    VirtualMachineDTO createVirtualMachine(String studentId, // owner of the vm
                                           Long teamId, // to fetch the configuration
                                           int numVcpu,
                                           int diskSpace,
                                           int ram);
    VirtualMachineDTO updateVirtualMachine(Long vmId, VirtualMachineDTO virtualMachine);
    boolean deleteVirtualMachine(Long id);
    void turnOnVirtualMachine(Long id);
    void turnOffVirtualMachine(Long id);
    boolean addOwnerToVirtualMachine(String studentId, Long vmId);

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
    VirtualMachineConfigurationDTO updateVirtualMachineConfiguration(Long teamId, VirtualMachineConfigurationDTO configuration);
    VirtualMachineModelDTO createVirtualMachineModel(String courseName, SystemImage os);
    boolean deleteVirtualMachineModel(String courseName);

    /**
     * teacher/student
     */
    Optional<VirtualMachineDTO> getVirtualMachine(Long id);
    List<StudentDTO> getOwnersForVirtualMachine(Long id);
    Optional<VirtualMachineModelDTO> getVirtualMachineModelForVirtualMachine(Long id);
    Optional<CourseDTO> getCourseForVirtualMachineModel(Long modelId);
    Optional<TeamDTO> getTeamForVirtualMachine(Long id);
    List<VirtualMachineDTO> getVirtualMachinesForTeam(Long teamId);
    List<VirtualMachineDTO> getVirtualMachinesForStudent(String studentId);
    Optional<VirtualMachineModelDTO> getVirtualMachineModelForCourse(String courseName);
    Optional<VirtualMachineConfigurationDTO> getVirtualMachineConfigurationForTeam(Long teamId);
    int getActiveVcpuForTeam(Long teamId);
    int getActiveDiskSpaceForTeam(Long teamId);
    int getActiveRAMForTeam(Long teamId);
    int getCountActiveVirtualMachinesForTeam(Long teamId);
    int getCountVirtualMachinesForTeam(Long teamId);
    Map<String, Integer> getResourcesByTeam(Long teamId);
}
