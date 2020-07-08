package it.polito.ai.backend.services;

import it.polito.ai.backend.dtos.VirtualMachineConfigurationDTO;
import it.polito.ai.backend.dtos.VirtualMachineDTO;
import it.polito.ai.backend.dtos.VirtualMachineModelDTO;
import it.polito.ai.backend.entities.VirtualMachine;
import it.polito.ai.backend.entities.VirtualMachineConfiguration;
import it.polito.ai.backend.entities.VirtualMachineStatus;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class VirtualMachineServiceImpl implements VirtualMachineService {
    @Override
    public VirtualMachineDTO createVM(int numVcpu, int diskSpace, int RAM) {
        return null;
    }

    @Override
    public boolean deleteVM(Long id) {
        return false;
    }

    @Override
    public boolean updateVM(VirtualMachineDTO vm) {
        return false;
    }

    @Override
    public boolean turnOnVM(Long id) {
        return false;
    }

    @Override
    public boolean turnOffVM(Long id) {
        return false;
    }

    @Override
    public boolean addOwnerToVM(String studentId, Long vmId) {
        return false;
    }

    @Override
    public boolean removeOwnerFromVM(String studentId, Long vmId) {
        return false;
    }

    @Override
    public VirtualMachineConfigurationDTO createVMConfiguration(int numVcpu, int diskSpace, int RAM) {
        return null;
    }

    @Override
    public boolean addVMConfigurationToTeam(VirtualMachineConfigurationDTO configuration, Long teamId) {
        return false;
    }

    @Override
    public boolean updateVMConfiguration(VirtualMachineConfigurationDTO configuration) {
        return false;
    }

    @Override
    public VirtualMachineModelDTO createVMModel(int min_vcpu, int max_vcpu, int min_disk, int max_disk, int min_ram, int max_ram, int tot_vcpu, int tot_disk, int tot_ram, int tot, int max_on) {
        return null;
    }

    @Override
    public VirtualMachineModelDTO updateVMModel(VirtualMachineModelDTO model) {
        return null;
    }

    @Override
    public boolean addVMConfigurationToCourse(VirtualMachineConfigurationDTO configuration, String courseName) {
        return false;
    }

    @Override
    public VirtualMachineModelDTO getVMModelForCourse(String courseName) {
        return null;
    }

    @Override
    public VirtualMachineDTO getVirtualMachine(Long id) {
        return null;
    }

    @Override
    public List<VirtualMachineDTO> getVMsForTeam(Long teamId) {
        return null;
    }

    @Override
    public VirtualMachineConfigurationDTO getVMConfigurationForTeam(Long teamId) {
        return null;
    }

    @Override
    public Integer getVcpuForTeam(Long teamId) {
        return null;
    }

    @Override
    public Integer getDiskSpaceForTeam(Long teamId) {
        return null;
    }

    @Override
    public Integer getRAMForTeam(Long teamId) {
        return null;
    }
}
