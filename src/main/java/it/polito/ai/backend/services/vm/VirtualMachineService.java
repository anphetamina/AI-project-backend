package it.polito.ai.backend.services.vm;

import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.entities.SystemImage;
import it.polito.ai.backend.entities.VirtualMachineModel;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface VirtualMachineService {

    /**
     * student
     */
    VirtualMachineDTO createVirtualMachine(String studentId, Long teamId, Long modelId, VirtualMachineDTO virtualMachine);
    VirtualMachineDTO updateVirtualMachine(VirtualMachineDTO virtualMachine);
    boolean deleteVirtualMachine(Long vmId);
    void turnOnVirtualMachine(Long vmId);
    void turnOffVirtualMachine(Long vmId);
    boolean addOwnerToVirtualMachine(String studentId, Long vmId);

    /**
     * teacher
     */
    ConfigurationDTO createConfiguration(Long teamId, ConfigurationDTO configuration);
    ConfigurationDTO updateConfiguration(ConfigurationDTO configuration);
    VirtualMachineModelDTO createVirtualMachineModel(String courseId, VirtualMachineModelDTO model);
    boolean deleteVirtualMachineModel(Long modelId);

    /**
     * teacher/student
     */
    Optional<VirtualMachineDTO> getVirtualMachine(Long vmId);
    Optional<VirtualMachineModelDTO> getVirtualMachineModel(Long modelId);
    Optional<ConfigurationDTO> getConfiguration(Long configurationId);

    List<StudentDTO> getOwnersForVirtualMachine(Long vmId);
    List<VirtualMachineDTO> getVirtualMachinesForTeam(Long teamId);
    List<VirtualMachineDTO> getVirtualMachinesForStudent(String studentId);
    Optional<VirtualMachineModelDTO> getVirtualMachineModelForCourse(String courseId);
    Optional<ConfigurationDTO> getConfigurationForTeam(Long teamId);
    Optional<TeamDTO> getTeamForVirtualMachine(Long vmId);
    Optional<VirtualMachineModelDTO> getVirtualMachineModelForVirtualMachine(Long vmId);
    Optional<CourseDTO> getCourseForVirtualMachineModel(Long modelId);
    Optional<TeamDTO> getTeamForConfiguration(Long configurationId);

    int getActiveVcpuForTeam(Long teamId);
    int getActiveDiskSpaceForTeam(Long teamId);
    int getActiveRAMForTeam(Long teamId);
    int getCountActiveVirtualMachinesForTeam(Long teamId);
    int getCountVirtualMachinesForTeam(Long teamId);
    Map<String, Integer> getResourcesByTeam(Long teamId);
}
