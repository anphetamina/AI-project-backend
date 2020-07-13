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

    // todo change exception messages
    // todo repository optimizations

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
    public VirtualMachineDTO createVirtualMachine(String studentId, Long modelId, int numVcpu, int diskSpace, int ram) {
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        VirtualMachineModel model = virtualMachineModelRepository.findById(modelId).orElseThrow(() -> new VirtualMachineModelNotFoundException(modelId.toString()));
        Team team = model.getTeam();

        if (team.getVm_configuration() == null) {
            throw new ConfigurationNotDefinedException(team.getId().toString());
        }

        VirtualMachineConfiguration configuration = team.getVm_configuration();

        /**
         * check vm instances number
         * if the current tot +1 is greater than the allowed max then throws an exception
         * otherwise check the available resources for the team of the student
         */

        long vm_tot = model.getTeam().getVm_models()
                .stream()
                .mapToLong(m -> m.getVirtual_machines().size())
                .sum();

        if (vm_tot + 1 > configuration.getTot()) {
            throw new TooManyVirtualMachinesException(String.valueOf(vm_tot));
        }

        /**
         * check available resources
         */

        List<VirtualMachine> activeVMs = model.getTeam().getVm_models()
                .stream()
                .flatMap(m -> m.getVirtual_machines().stream().filter(vm -> vm.getStatus() == VirtualMachineStatus.ON))
                .collect(Collectors.toList());

        int currentNumVcpu = activeVMs
                .stream()
                .reduce(0, (partial, current) -> partial + current.getNum_vcpu(), Integer::sum);
        if (numVcpu < configuration.getMin_vcpu()) {
            throw new InvalidNumVcpuException(String.valueOf(numVcpu));
        }
        if (numVcpu > configuration.getMax_vcpu() - currentNumVcpu) {
            throw new NumVcpuNotAvailableException(String.valueOf(numVcpu));
        }

        int currentDiskSpace = activeVMs
                .stream()
                .reduce(0, (partial, current) -> partial + current.getDisk_space(), Integer::sum);
        if (currentDiskSpace < configuration.getMin_disk_space()) {
            throw new InvalidDiskSpaceException(String.valueOf(diskSpace));
        }
        if (diskSpace > configuration.getMax_disk_space() - currentDiskSpace) {
            throw new DiskSpaceNotAvailableException(String.valueOf(diskSpace));
        }

        int currentRam = activeVMs
                .stream()
                .reduce(0, (partial, current) -> partial + current.getRam(), Integer::sum);
        if (ram < configuration.getMin_ram()) {
            throw new InvalidRamException(String.valueOf(ram));
        }
        if (ram > configuration.getMax_ram() - currentRam) {
            throw new RamNotAvailableException(String.valueOf(ram));
        }

        VirtualMachine vm = VirtualMachine.builder()
                .num_vcpu(numVcpu)
                .disk_space(diskSpace)
                .ram(ram)
                .status(VirtualMachineStatus.OFF)
                .build();
        vm.addOwner(student);
        vm.setVirtualMachineModel(model);
        virtualMachineRepository.save(vm);
        return modelMapper.map(vm, VirtualMachineDTO.class);
    }

    @Override
    public VirtualMachineDTO updateVirtualMachine(String studentId, VirtualMachineDTO vm) {
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        VirtualMachine virtualMachine = virtualMachineRepository.findById(vm.getId()).orElseThrow(() -> new VirtualMachineNotFoundException(String.valueOf(vm.getId())));


        /**
         * first check if the current student is one of the owner of the vm
         * and if the vm has been turned off before the update
         */

        if (!virtualMachine.getOwners().contains(student)) {
            throw new OwnerNotFoundException(studentId);
        }

        if (virtualMachine.getStatus() != VirtualMachineStatus.OFF) {
            throw new VirtualMachineStillActiveException(String.valueOf(virtualMachine.getId()));
        }

        /**
         * checks if, excluding the old resource, the sum of the new one does not exceeds the maximum allowed in the configuration
         * and is not lower than the minimum
         */

        VirtualMachineConfiguration configuration = virtualMachine.getVm_model().getTeam().getVm_configuration();
        List<VirtualMachine> activeVMs = virtualMachine.getVm_model().getTeam().getVm_models()
                .stream()
                .flatMap(m -> m.getVirtual_machines().stream().filter(v -> v.getStatus() == VirtualMachineStatus.ON))
                .collect(Collectors.toList());

        int currentNumVcpu = activeVMs
                .stream()
                .reduce(0, (partial, current) -> partial + current.getNum_vcpu(), Integer::sum);

        int maxNumVcpu = configuration.getMax_vcpu();
        int minNumVcpu = configuration.getMin_vcpu();
        int newNumVcpu = vm.getNum_vcpu();
        int oldNumVcpu = virtualMachine.getNum_vcpu();

        if (newNumVcpu < minNumVcpu || (currentNumVcpu - oldNumVcpu) + newNumVcpu > maxNumVcpu) {
            throw new InvalidNumVcpuException(String.valueOf(newNumVcpu));
        }

        int currentDiskSpace = activeVMs
                .stream()
                .reduce(0, (partial, current) -> partial + current.getDisk_space(), Integer::sum);

        int maxDiskSpace = configuration.getMax_disk_space();
        int minDiskSpace = configuration.getMin_disk_space();
        int newDiskSpace = vm.getDisk_space();
        int oldDiskSpace = virtualMachine.getDisk_space();

        if (newDiskSpace < minDiskSpace || (currentDiskSpace - oldDiskSpace) + newDiskSpace > maxDiskSpace) {
            throw new InvalidNumVcpuException(String.valueOf(newDiskSpace));
        }

        int currentRam = activeVMs
                .stream()
                .reduce(0, (partial, current) -> partial + current.getRam(), Integer::sum);

        int maxRam = configuration.getMax_ram();
        int minRam = configuration.getMin_ram();
        int newRam = vm.getRam();
        int oldRam = virtualMachine.getRam();

        if (newRam < minRam || (currentRam - oldRam) + newRam > maxRam) {
            throw new InvalidNumVcpuException(String.valueOf(newRam));
        }

        virtualMachine.setNum_vcpu(newNumVcpu);
        virtualMachine.setDisk_space(newDiskSpace);
        virtualMachine.setRam(newRam);

        return modelMapper.map(virtualMachineRepository.save(virtualMachine), VirtualMachineDTO.class);
    }

    @Override
    public boolean deleteVirtualMachine(Long id) {
        VirtualMachine virtualMachine = virtualMachineRepository.findById(id).orElseThrow(() -> new VirtualMachineNotFoundException(id.toString()));
        virtualMachine.setVirtualMachineModel(null);
        virtualMachine.getOwners().forEach(virtualMachine::removeOwner); // todo check
        virtualMachineRepository.delete(virtualMachine);
        return false;
    }

    @Override
    public void turnOnVirtualMachine(Long id) {

        VirtualMachine virtualMachine = virtualMachineRepository.findById(id).orElseThrow(() -> new VirtualMachineNotFoundException(id.toString()));
        Team team = virtualMachine.getVm_model().getTeam();
        VirtualMachineConfiguration configuration = team.getVm_configuration();
        List<VirtualMachine> activeVMs = team.getVm_models()
                .stream()
                .flatMap(m -> m.getVirtual_machines().stream().filter(v -> v.getStatus() == VirtualMachineStatus.ON))
                .collect(Collectors.toList());

        if (activeVMs.size() + 1 > configuration.getMax_on()) {
            throw new TooManyOnVirtualMachinesException(String.valueOf(activeVMs.size()));
        }

        int currentNumVcpu = activeVMs
                .stream()
                .reduce(0, (partial, current) -> partial + current.getNum_vcpu(), Integer::sum);
        if (currentNumVcpu + virtualMachine.getNum_vcpu() > configuration.getMax_vcpu()) {
            throw new NumVcpuNotAvailableException(String.valueOf(virtualMachine.getNum_vcpu()));
        } else if (virtualMachine.getNum_vcpu() < configuration.getMin_vcpu()) {
            throw new InvalidNumVcpuException(String.valueOf(virtualMachine.getNum_vcpu()));
        }

        int currentDiskSpace = activeVMs
                .stream()
                .reduce(0, (partial, current) -> partial + current.getDisk_space(), Integer::sum);
        if (currentDiskSpace + virtualMachine.getDisk_space() > configuration.getMax_disk_space()) {
            throw new DiskSpaceNotAvailableException(String.valueOf(virtualMachine.getDisk_space()));
        } else if (virtualMachine.getDisk_space() < configuration.getMin_disk_space()) {
            throw new InvalidDiskSpaceException(String.valueOf(virtualMachine.getDisk_space()));
        }

        int currentRam = activeVMs
                .stream()
                .reduce(0, (partial, current) -> partial + current.getRam(), Integer::sum);
        if (currentRam + virtualMachine.getRam() > configuration.getMax_ram()) {
            throw new RamNotAvailableException(String.valueOf(virtualMachine.getRam()));
        } else if (virtualMachine.getRam() < configuration.getMin_ram()) {
            throw new InvalidRamException(String.valueOf(virtualMachine.getRam()));
        }

        virtualMachine.setStatus(VirtualMachineStatus.ON);
    }

    @Override
    public void turnOffVirtualMachine(Long id) {
        VirtualMachine virtualMachine = virtualMachineRepository.findById(id).orElseThrow(() -> new VirtualMachineNotFoundException(id.toString()));
        virtualMachine.setStatus(VirtualMachineStatus.OFF);
    }

    @Override
    public boolean addOwnerToVirtualMachine(String studentId, Long vmId) {
        VirtualMachine virtualMachine = virtualMachineRepository.findById(vmId).orElseThrow(() -> new VirtualMachineNotFoundException(vmId.toString()));
        Student s = studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        if (!virtualMachine.getOwners().contains(s)) {
            virtualMachine.getOwners().add(s);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeOwnerFromVirtualMachine(String studentId, Long vmId) {
        VirtualMachine virtualMachine = virtualMachineRepository.findById(vmId).orElseThrow(() -> new VirtualMachineNotFoundException(vmId.toString()));
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        if (virtualMachine.getOwners().contains(student)) {
            virtualMachine.removeOwner(student);
            return true;
        }
        return false;
    }

    @Override
    public VirtualMachineModelDTO createVirtualMachineModel(SystemImage os) {

        VirtualMachineModel model = VirtualMachineModel.builder()
                .system_image(os)
                .build();
        virtualMachineModelRepository.save(model);

        return modelMapper.map(model, VirtualMachineModelDTO.class);
    }

    @Override
    public boolean deleteVirtualMachineModel(Long id) {

        VirtualMachineModel model = virtualMachineModelRepository.findById(id).orElseThrow(() -> new VirtualMachineModelNotFoundException(id.toString()));

        /**
         * if there is at least one virtual machine using this model
         * do not cancel the model
         */
        if (model.getVirtual_machines().size() > 0) {
            return false;
        }

        model.setTeam(null);

        virtualMachineModelRepository.delete(model);
        return true;
    }

    @Override
    public boolean addVirtualMachineModelToTeam(Long modelId, Long teamId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException(teamId.toString()));
        VirtualMachineModel model = virtualMachineModelRepository.findById(modelId).orElseThrow(() -> new VirtualMachineModelNotFoundException(modelId.toString()));

        team.addVirtualMachineModel(model);

        return true;
    }

    @Override
    public Optional<VirtualMachineModelDTO> getVirtualMachineModel(Long id) {
        return Optional.ofNullable(virtualMachineModelRepository.findById(id)
                .map(vmm -> modelMapper.map(vmm, VirtualMachineModelDTO.class))
                .orElseThrow(() -> new VirtualMachineModelNotFoundException(id.toString())));
    }

    @Override
    public VirtualMachineConfigurationDTO createVirtualMachineConfiguration(Long teamId, int min_vcpu, int max_vcpu, int min_disk_space, int max_disk_space, int min_ram, int max_ram, int tot, int max_on) {
        VirtualMachineConfiguration configuration = VirtualMachineConfiguration.builder()
                .min_vcpu(min_vcpu)
                .max_vcpu(max_vcpu)
                .min_disk_space(min_disk_space)
                .max_disk_space(max_disk_space)
                .min_ram(min_ram)
                .max_ram(max_ram)
                .max_on(max_on)
                .tot(tot)
                .build();
        virtualMachineConfigurationRepository.save(configuration);

        return modelMapper.map(configuration, VirtualMachineConfigurationDTO.class);
    }

    @Override
    public boolean addVirtualMachineConfigurationToTeam(Long configurationId, Long teamId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException(teamId.toString()));
        VirtualMachineConfiguration configuration = virtualMachineConfigurationRepository.findById(configurationId).orElseThrow(() -> new VirtualMachineConfigurationNotFoundException(configurationId.toString()));

        if (team.getVm_configuration() != null) {
            return false;
        }

        team.setVirtualMachineConfiguration(configuration);
        return true;
    }

    @Override
    public VirtualMachineConfigurationDTO updateVirtualMachineConfiguration(VirtualMachineConfigurationDTO configuration) {

        VirtualMachineConfiguration vmc = virtualMachineConfigurationRepository.findById(configuration.getId()).orElseThrow(() -> new VirtualMachineConfigurationNotFoundException(configuration.getId().toString()));

        long vm_tot = vmc.getTeam().getVm_models().stream()
                .mapToLong(m -> m.getVirtual_machines().size())
                .sum();

        if (configuration.getTot() > vm_tot) {
            throw new InvalidTotNumException(String.valueOf(configuration.getTot()));
        }

        List<VirtualMachine> activeVMs = vmc.getTeam().getVm_models()
                .stream()
                .flatMap(m -> m.getVirtual_machines().stream())
                .filter(vm -> vm.getStatus() == VirtualMachineStatus.ON)
                .collect(Collectors.toList());

        if (activeVMs.size() > configuration.getMax_on()) {
            throw new InvalidMaxOnNumException(String.valueOf(configuration.getMax_on()));
        }

        /**
         * check if the current num of vcpu in use does not exceed the configuration one
         * and if any of the active vm is lower then the minimum resource threshold
         */

        int currentNumVcpu = activeVMs
                .stream()
                .reduce(0, (partial, current) -> partial + current.getNum_vcpu(), Integer::sum);
        if (currentNumVcpu > configuration.getMax_vcpu() || activeVMs.stream().anyMatch(vm -> vm.getNum_vcpu() < configuration.getMin_vcpu())) {
            throw new InvalidNumVcpuException(String.valueOf(configuration.getMax_vcpu()));
        }

        int currentDiskSpace = activeVMs
                .stream()
                .reduce(0, (partial, current) -> partial + current.getDisk_space(), Integer::sum);
        if (currentDiskSpace > configuration.getMax_disk() || activeVMs.stream().anyMatch(vm -> vm.getDisk_space() < configuration.getMin_disk())) {
            throw new InvalidDiskSpaceException(String.valueOf(configuration.getMax_disk()));
        }

        int currentRam = activeVMs
                .stream()
                .reduce(0, (partial, current) -> partial + current.getRam(), Integer::sum);
        if (currentRam > configuration.getMax_ram() || activeVMs.stream().anyMatch(vm -> vm.getRam() < configuration.getMin_ram())) {
            throw new InvalidRamException(String.valueOf(configuration.getMax_ram()));
        }

        vmc.setTot(configuration.getTot());
        vmc.setMax_on(configuration.getMax_on());
        vmc.setMin_vcpu(configuration.getMin_vcpu());
        vmc.setMax_vcpu(configuration.getMax_vcpu());
        vmc.setMax_disk_space(configuration.getMax_disk());
        vmc.setMin_disk_space(configuration.getMin_disk());
        vmc.setMax_ram(configuration.getMax_ram());
        vmc.setMin_ram(configuration.getMin_ram());

        return modelMapper.map(virtualMachineConfigurationRepository.save(vmc), VirtualMachineConfigurationDTO.class);
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
                .getVm_models()
                .stream()
                .flatMap(m -> m.getVirtual_machines().stream().map(vm -> modelMapper.map(vm, VirtualMachineDTO.class)))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<VirtualMachineConfigurationDTO> getVirtualMachineConfiguration(Long id) {
        return Optional.ofNullable(virtualMachineConfigurationRepository.findById(id)
                .map(vmc -> modelMapper.map(vmc, VirtualMachineConfigurationDTO.class))
                .orElseThrow(() -> new VirtualMachineConfigurationNotFoundException(id.toString())));
    }

    @Override
    public Optional<VirtualMachineConfigurationDTO> getVirtualMachineConfigurationForTeam(Long teamId) {
        return Optional.ofNullable(teamRepository.findById(teamId)
                .map(t -> modelMapper.map(t.getVm_configuration(), VirtualMachineConfigurationDTO.class))
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString())));
    }

    @Override
    public int getActiveVcpuForTeam(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new TeamNotFoundException(teamId.toString());
        }
        return teamRepository.getActiveNumVcpuByTeam(teamId);
    }

    @Override
    public int getActiveDiskSpaceForTeam(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new TeamNotFoundException(teamId.toString());
        }
        return teamRepository.getActiveDiskSpaceByTeam(teamId);
    }

    @Override
    public int getActiveRAMForTeam(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new TeamNotFoundException(teamId.toString());
        }
        return teamRepository.getActiveRamByTeam(teamId);
    }

    @Override
    public int getCountActiveVirtualMachinesForTeam(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new TeamNotFoundException(teamId.toString());
        }
        return teamRepository.countVirtualMachinesByStatusAndTeam(VirtualMachineStatus.ON, teamId);
    }

    @Override
    public List<VirtualMachine> getActiveVirtualMachinesForTeam(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new TeamNotFoundException(teamId.toString());
        }
        return teamRepository.getVirtualMachinesByStatusAndTeam(VirtualMachineStatus.ON, teamId);
    }
}
