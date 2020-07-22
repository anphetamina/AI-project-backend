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
    VirtualMachineDTO createVirtualMachine(String courseId, Long teamId, String studentId, int numVcpu, int diskSpace, int ram);
    VirtualMachineDTO updateVirtualMachine(String courseId, Long teamId, Long vmId, VirtualMachineDTO virtualMachine);
    boolean deleteVirtualMachine(String courseId, Long teamId, Long vmId);
    void turnOnVirtualMachine(String courseId, Long teamId, Long vmId);
    void turnOffVirtualMachine(String courseId, Long teamId, Long vmId);
    boolean addOwnerToVirtualMachine(String courseId, Long teamId, String studentId, Long vmId);

    /**
     * teacher
     */
    ConfigurationDTO createVirtualMachineConfiguration(
                                                String courseId,
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
    ConfigurationDTO updateVirtualMachineConfiguration(String courseId, Long teamId, ConfigurationDTO configuration);
    VirtualMachineModelDTO createVirtualMachineModel(String courseId, SystemImage os);
    boolean deleteVirtualMachineModel(String courseId);

    /**
     * teacher/student
     */
    Optional<VirtualMachineDTO> getVirtualMachine(String courseId, Long teamId, Long vmId);
    List<StudentDTO> getOwnersForVirtualMachine(String courseId, Long teamId, Long vmId);
    List<VirtualMachineDTO> getVirtualMachinesForTeam(String courseId, Long teamId);
    Optional<VirtualMachineModelDTO> getVirtualMachineModelForCourse(String courseId);
    Optional<ConfigurationDTO> getVirtualMachineConfigurationForTeam(String courseId, Long teamId);
    int getActiveVcpuForTeam(String courseId, Long teamId);
    int getActiveDiskSpaceForTeam(String courseId, Long teamId);
    int getActiveRAMForTeam(String courseId, Long teamId);
    int getCountActiveVirtualMachinesForTeam(String courseId, Long teamId);
    int getCountVirtualMachinesForTeam(String courseId, Long teamId);
    Map<String, Integer> getResourcesByTeam(String courseId, Long teamId);
}
