package it.polito.ai.backend.services.vm;

import it.polito.ai.backend.dtos.VirtualMachineConfigurationDTO;
import it.polito.ai.backend.dtos.VirtualMachineDTO;
import it.polito.ai.backend.dtos.VirtualMachineModelDTO;
import it.polito.ai.backend.entities.*;
import it.polito.ai.backend.repositories.*;
import it.polito.ai.backend.services.team.StudentNotFoundException;
import it.polito.ai.backend.services.team.TeamNotFoundException;
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
    public VirtualMachineDTO createVirtualMachine(String studentId, Long teamId, int numVcpu, int diskSpace, int ram) {
        Student s = studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        Team t = teamRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException(teamId.toString()));

        Integer activeVcpu = this.getVcpuForTeam(teamId);
        Integer activeDiskSpace = this.getDiskSpaceForTeam(teamId);
        Integer activeRam = this.getRAMForTeam(teamId);

        if (numVcpu > activeVcpu) {
            throw new NumVcpuNotAvailableException(String.valueOf(numVcpu));
        }
        if (diskSpace > activeDiskSpace) {
            throw new DiskSpaceNotAvailableException(String.valueOf(diskSpace));
        }
        if (ram > activeRam) {
            throw new RamNotAvailableException(String.valueOf(ram));
        }

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
    }

    @Override
    public boolean deleteVirtualMachine(Long id) {
        if (virtualMachineRepository.existsById(id)) {
            virtualMachineRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public VirtualMachineDTO updateVirtualMachine(VirtualMachineDTO vm) {
        return virtualMachineRepository.findById(vm.getId())
                .map(v -> modelMapper.map(virtualMachineRepository.save(v), VirtualMachineDTO.class))
                .orElseThrow(() -> new VirtualMachineNotFoundException(String.valueOf(vm.getId())));
    }

    @Override
    public void turnOnVirtualMachine(Long id) {
        VirtualMachine vm = virtualMachineRepository.findById(id).orElseThrow(() -> new VirtualMachineNotFoundException(id.toString()));
        vm.setStatus(VirtualMachineStatus.ON);
    }

    @Override
    public void turnOffVirtualMachine(Long id) {
        VirtualMachine vm = virtualMachineRepository.findById(id).orElseThrow(() -> new VirtualMachineNotFoundException(id.toString()));
        vm.setStatus(VirtualMachineStatus.OFF);
    }

    @Override
    public boolean addOwnerToVirtualMachine(String studentId, Long vmId) {
        VirtualMachine vm = virtualMachineRepository.findById(vmId).orElseThrow(() -> new VirtualMachineNotFoundException(vmId.toString()));
        Student s = studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        if (!vm.getOwners().contains(s)) {
            vm.getOwners().add(s);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeOwnerFromVirtualMachine(String studentId, Long vmId) {
        VirtualMachine vm = virtualMachineRepository.findById(vmId).orElseThrow(() -> new VirtualMachineNotFoundException(vmId.toString()));
        Student s = studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        if (vm.getOwners().contains(s)) {
            vm.getOwners().remove(s);
            return true;
        }
        return false;
    }

    @Override
    public VirtualMachineModelDTO createVirtualMachineModel(String studentId, OperatingSystem os) {
        return null;
    }

    @Override
    public boolean deleteVirtualMachineModel(Long id) {
        return false;
    }

    @Override
    public VirtualMachineModelDTO updateVirtualMachineModel(VirtualMachineModelDTO model) {
        return null;
    }

    @Override
    public boolean addVirtualMachineModelToTeam(VirtualMachineModelDTO model, Long teamId) {
        return false;
    }

    @Override
    public VirtualMachineModelDTO getVirtualMachineModel(Long id) {
        return null;
    }

    @Override
    public VirtualMachineConfigurationDTO createVirtualMachineConfiguration(String teacherId, Long teamId, int min_vcpu, int max_vcpu, int min_disk, int max_disk, int min_ram, int max_ram, int tot, int max_on) {
        return null;
    }

    @Override
    public boolean addVirtualMachineConfigurationToTeam(VirtualMachineConfigurationDTO configuration, Long teamId) {
        return false;
    }

    @Override
    public VirtualMachineConfigurationDTO updateVirtualMachineConfiguration(VirtualMachineConfigurationDTO configuration) {
        // todo check team utilization before update
        return virtualMachineConfigurationRepository.findById(configuration.getId())
                .map(c -> modelMapper.map(virtualMachineConfigurationRepository.save(c), VirtualMachineConfigurationDTO.class))
                .orElseThrow(() -> new VirtualMachineConfigurationNotFoundException(String.valueOf(configuration.getId())));
    }

    @Override
    public Optional<VirtualMachineDTO> getVirtualMachine(Long id) {
        return Optional.ofNullable(virtualMachineRepository.findById(id)
                .map(vm -> modelMapper.map(vm, VirtualMachineDTO.class))
                .orElseThrow(() -> new VirtualMachineNotFoundException(id.toString())));
    }

    @Override
    public List<VirtualMachineDTO> getVirtualMachinesForTeam(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()))
                .getVirtual_machines()
                .stream()
                .map(vm -> modelMapper.map(vm, VirtualMachineDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<VirtualMachineConfigurationDTO> getVirtualMachineConfiguration(Long id) {
        return Optional.empty();
    }

    @Override
    public Optional<VirtualMachineConfigurationDTO> getVirtualMachineConfigurationForTeam(Long teamId) {
        return Optional.ofNullable(teamRepository.findById(teamId)
                .map(t -> modelMapper.map(t.getVm_configuration(), VirtualMachineConfigurationDTO.class))
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString())));
    }

    @Override
    public Integer getVcpuForTeam(Long teamId) {
        if (!virtualMachineRepository.existsById(teamId)) {
            throw new TeamNotFoundException(teamId.toString());
        }
        return virtualMachineRepository.getNumVcpuInUseByTeam(teamId);
    }

    @Override
    public Integer getDiskSpaceForTeam(Long teamId) {
        if (!virtualMachineRepository.existsById(teamId)) {
            throw new TeamNotFoundException(teamId.toString());
        }
        return virtualMachineRepository.getDiskSpaceInUseByTeam(teamId);
    }

    @Override
    public Integer getRAMForTeam(Long teamId) {
        if (!virtualMachineRepository.existsById(teamId)) {
            throw new TeamNotFoundException(teamId.toString());
        }
        return virtualMachineRepository.getRamInUseByTeam(teamId);
    }
}
