package it.polito.ai.backend.services;

import it.polito.ai.backend.dtos.VirtualMachineConfigurationDTO;
import it.polito.ai.backend.dtos.VirtualMachineDTO;
import it.polito.ai.backend.dtos.VirtualMachineModelDTO;
import it.polito.ai.backend.entities.Student;
import it.polito.ai.backend.entities.Team;
import it.polito.ai.backend.entities.VirtualMachine;
import it.polito.ai.backend.entities.VirtualMachineStatus;
import it.polito.ai.backend.repositories.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class VirtualMachineServiceImpl implements VirtualMachineService {

    @Autowired
    VirtualMachineRepository virtualMachineRepository;
    @Autowired
    VirtualMachineConfigurationRepository virtualMachineConfigurationRepository;
    @Autowired
    VirtualMachineModelRepository virtualMachineModelRepository;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    StudentRepository studentRepository;
    @Autowired
    ModelMapper modelMapper;

    @Override
    public VirtualMachineDTO createVM(String studentId, Long teamId, int numVcpu, int diskSpace, int ram) {
        Student s = studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        Team t = teamRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException(teamId.toString()));
        if (areResourcesAvailable(teamId, numVcpu, diskSpace, ram)) {
            VirtualMachine vm = VirtualMachine.builder()
                    .num_vcpu(numVcpu)
                    .disk_space(diskSpace)
                    .ram(ram)
                    .status(VirtualMachineStatus.OFF)
                    .build();
            vm.addOwner(s);
            vm.setTeam(t);
            virtualMachineRepository.save(vm);
            return modelMapper.map(vm, VirtualMachineDTO.class);
        } else {
            throw new ResourcesNotAvailableException(numVcpu + " cpu " + diskSpace + " disk space " + ram + " ram");
        }
    }

    private boolean areResourcesAvailable(Long teamId, int numVcpu, int diskSpace, int ram) {
        Integer activeVcpu = this.getVcpuForTeam(teamId);
        Integer activeDiskSpace = this.getDiskSpaceForTeam(teamId);
        Integer activeRam = this.getRAMForTeam(teamId);

        return numVcpu <= activeVcpu && diskSpace <= activeDiskSpace && ram <= activeRam;
    }

    @Override
    public boolean deleteVM(Long id) {
        if (virtualMachineRepository.existsById(id)) {
            virtualMachineRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public VirtualMachineDTO updateVM(VirtualMachineDTO vm) {
        return virtualMachineRepository.findById(vm.getId())
                .map(v -> modelMapper.map(virtualMachineRepository.save(v), VirtualMachineDTO.class))
                .orElseThrow(() -> new VirtualMachineNotFoundException(String.valueOf(vm.getId())));
    }

    @Override
    public void turnOnVM(Long id) {
        VirtualMachine vm = virtualMachineRepository.findById(id).orElseThrow(() -> new VirtualMachineNotFoundException(id.toString()));
        vm.setStatus(VirtualMachineStatus.ON);
    }

    @Override
    public void turnOffVM(Long id) {
        VirtualMachine vm = virtualMachineRepository.findById(id).orElseThrow(() -> new VirtualMachineNotFoundException(id.toString()));
        vm.setStatus(VirtualMachineStatus.OFF);
    }

    @Override
    public boolean addOwnerToVM(String studentId, Long vmId) {
        VirtualMachine vm = virtualMachineRepository.findById(vmId).orElseThrow(() -> new VirtualMachineNotFoundException(vmId.toString()));
        Student s = studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        if (!vm.getOwners().contains(s)) {
            vm.getOwners().add(s);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeOwnerFromVM(String studentId, Long vmId) {
        VirtualMachine vm = virtualMachineRepository.findById(vmId).orElseThrow(() -> new VirtualMachineNotFoundException(vmId.toString()));
        Student s = studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        if (vm.getOwners().contains(s)) {
            vm.getOwners().remove(s);
            return true;
        }
        return false;
    }

    @Override
    public VirtualMachineConfigurationDTO createVMConfiguration(String teacherId, Long teamId, int numVcpu, int diskSpace, int ram) {
        return null;
    }

    @Override
    public boolean addVMConfigurationToTeam(VirtualMachineConfigurationDTO configuration, Long teamId) {
        return false;
    }

    @Override
    public VirtualMachineConfigurationDTO updateVMConfiguration(VirtualMachineConfigurationDTO configuration) {
        return virtualMachineConfigurationRepository.findById(configuration.getId())
                .map(c -> modelMapper.map(virtualMachineConfigurationRepository.save(c), VirtualMachineConfigurationDTO.class))
                .orElseThrow(() -> new VirtualMachineConfigurationNotFoundException(String.valueOf(configuration.getId())));
    }

    @Override
    public VirtualMachineModelDTO createVMModel(int min_vcpu, int max_vcpu, int min_disk, int max_disk, int min_ram, int max_ram, int tot_vcpu, int tot_disk, int tot_ram, int tot, int max_on) {
        return null;
    }

    @Override
    public VirtualMachineModelDTO updateVMModel(VirtualMachineModelDTO model) {
        return virtualMachineModelRepository.findById(model.getId())
                .map(m -> modelMapper.map(virtualMachineModelRepository.save(m), VirtualMachineModelDTO.class))
                .orElseThrow(() -> new VirtualMachineModelNotFoundException(String.valueOf(model.getId())));
    }

    @Override
    public boolean addVMConfigurationToCourse(VirtualMachineConfigurationDTO configuration, String courseName) {
        return false;
    }

    @Override
    public Optional<VirtualMachineModelDTO> getVMModelForCourse(String courseName) {
        return Optional.ofNullable(courseRepository.findById(courseName)
                .map(c -> modelMapper.map(c.getVm_model(), VirtualMachineModelDTO.class))
                .orElseThrow(() -> new CourseNotFoundException(courseName)));
    }

    @Override
    public Optional<VirtualMachineDTO> getVirtualMachine(Long id) {
        return Optional.ofNullable(virtualMachineRepository.findById(id)
                .map(vm -> modelMapper.map(vm, VirtualMachineDTO.class))
                .orElseThrow(() -> new VirtualMachineNotFoundException(id.toString())));
    }

    @Override
    public List<VirtualMachineDTO> getVMsForTeam(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()))
                .getVirtual_machines()
                .stream()
                .map(vm -> modelMapper.map(vm, VirtualMachineDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<VirtualMachineConfigurationDTO> getVMConfigurationForTeam(Long teamId) {
        return Optional.ofNullable(teamRepository.findById(teamId)
                .map(t -> modelMapper.map(t.getVm_configuration(), VirtualMachineConfigurationDTO.class))
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString())));
    }

    @Override
    public Integer getVcpuForTeam(Long teamId) {
        return virtualMachineRepository.getNumVcpuInUseByTeam(teamId);
    }

    @Override
    public Integer getDiskSpaceForTeam(Long teamId) {
        return virtualMachineRepository.getDiskSpaceInUseByTeam(teamId);
    }

    @Override
    public Integer getRAMForTeam(Long teamId) {
        return virtualMachineRepository.getRamInUseByTeam(teamId);
    }
}
