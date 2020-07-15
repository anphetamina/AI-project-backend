package it.polito.ai.backend.services.vm;

import it.polito.ai.backend.dtos.VirtualMachineConfigurationDTO;
import it.polito.ai.backend.dtos.VirtualMachineDTO;
import it.polito.ai.backend.dtos.VirtualMachineModelDTO;
import it.polito.ai.backend.entities.*;
import it.polito.ai.backend.repositories.*;
import it.polito.ai.backend.services.team.CourseNotFoundException;
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
    public VirtualMachineDTO createVirtualMachine(String studentId, Long teamId, Long configurationId, String courseName, Long modelId, int numVcpu, int diskSpace, int ram) {
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        Course course = courseRepository.findById(courseName).orElseThrow(() -> new CourseNotFoundException(courseName));
        VirtualMachineModel virtualMachineModel = course.getVirtualMachineModel();

        /**
         * check whether the model has been defined for the given course or not
         */
        if (!virtualMachineModel.getId().equals(modelId)) {
            throw new InvalidVirtualMachineModelException(modelId.toString());
        }

        Team team = teamRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException(teamId.toString()));

        /**
         * check whether the configuration has been defined for the team or not
         * and if the given configuration is owned by the given team
         */
        if (team.getVirtualMachineConfiguration() == null) {
            throw new ConfigurationNotDefinedException(teamId.toString());
        } else if (!team.getVirtualMachineConfiguration().getId().equals(configurationId)) {
            throw new InvalidConfigurationException(configurationId.toString());
        }

        VirtualMachineConfiguration configuration = team.getVirtualMachineConfiguration();

        /**
         * check vm instances number
         * if the current tot +1 is greater than the allowed max then throws an exception
         * otherwise check the available resources for the team of the student
         */

        int vm_tot = this.getCountVirtualMachinesForTeam(teamId);

        if (vm_tot + 1 > configuration.getTot()) {
            throw new VirtualMachineNumberException(String.valueOf(vm_tot));
        }

        /**
         * check available resources
         */

        List<VirtualMachine> activeVMs = this.getActiveVirtualMachinesForTeam(teamId);

        int currentNumVcpu = activeVMs
                .stream()
                .reduce(0, (partial, current) -> partial + current.getNum_vcpu(), Integer::sum);
        if (numVcpu < configuration.getMin_vcpu()) {
            throw new InvalidNumVcpuException(String.valueOf(numVcpu));
        } else if (numVcpu > configuration.getMax_vcpu() - currentNumVcpu) {
            throw new NumVcpuNotAvailableException(String.valueOf(numVcpu));
        }

        int currentDiskSpace = activeVMs
                .stream()
                .reduce(0, (partial, current) -> partial + current.getDisk_space(), Integer::sum);
        if (currentDiskSpace < configuration.getMin_disk_space()) {
            throw new InvalidDiskSpaceException(String.valueOf(diskSpace));
        } else if (diskSpace > configuration.getMax_disk_space() - currentDiskSpace) {
            throw new DiskSpaceNotAvailableException(String.valueOf(diskSpace));
        }

        int currentRam = activeVMs
                .stream()
                .reduce(0, (partial, current) -> partial + current.getRam(), Integer::sum);
        if (ram < configuration.getMin_ram()) {
            throw new InvalidRamException(String.valueOf(ram));
        } else if (ram > configuration.getMax_ram() - currentRam) {
            throw new RamNotAvailableException(String.valueOf(ram));
        }

        VirtualMachine vm = VirtualMachine.builder()
                .num_vcpu(numVcpu)
                .disk_space(diskSpace)
                .ram(ram)
                .status(VirtualMachineStatus.OFF)
                .build();
        vm.addOwner(student);
        vm.setTeam(team);
        vm.setVirtualMachineModel(virtualMachineModel);
        virtualMachineRepository.save(vm);
        return modelMapper.map(vm, VirtualMachineDTO.class);
    }

    @Override
    public VirtualMachineDTO updateVirtualMachine(Long vmId, VirtualMachineDTO vm) {

        // todo check

        VirtualMachine virtualMachine = virtualMachineRepository.findById(vmId).orElseThrow(() -> new VirtualMachineNotFoundException(String.valueOf(vmId)));


        /**
         * check if the vm has been turned off before the update
         */


        if (virtualMachine.getStatus() != VirtualMachineStatus.OFF) {
            throw new VirtualMachineStillActiveException(String.valueOf(virtualMachine.getId()));
        }

        /**
         * checks if, excluding the old resource, the sum of the new one does not exceeds the maximum allowed in the configuration
         * and is not lower than the minimum
         */

        VirtualMachineConfiguration configuration = virtualMachine.getTeam().getVirtualMachineConfiguration();

        if (configuration == null) {
            throw new ConfigurationNotDefinedException(virtualMachine.getTeam().getId().toString());
        }

        List<VirtualMachine> activeVMs = this.getActiveVirtualMachinesForTeam(virtualMachine.getTeam().getId());

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

        if (virtualMachine.getStatus() == VirtualMachineStatus.ON) {
            return false;
        }

        virtualMachine.setVirtualMachineModel(null);
        virtualMachine.setTeam(null);
        virtualMachine.getOwners().forEach(owner -> owner.removeVirtualMachine(virtualMachine));
        virtualMachineRepository.delete(virtualMachine);
        return true;
    }

    @Override
    public void turnOnVirtualMachine(Long id) {

        VirtualMachine virtualMachine = virtualMachineRepository.findById(id).orElseThrow(() -> new VirtualMachineNotFoundException(id.toString()));

        if (virtualMachine.getStatus() == VirtualMachineStatus.ON) {
            return;
        }

        Team team = virtualMachine.getTeam();
        VirtualMachineConfiguration configuration = team.getVirtualMachineConfiguration();
        List<VirtualMachine> activeVMs = this.getActiveVirtualMachinesForTeam(team.getId());

        if (activeVMs.size() + 1 > configuration.getMax_on()) {
            throw new ActiveVirtualMachineNumberException(String.valueOf(activeVMs.size()));
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
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        if (!virtualMachine.getOwners().contains(student)) {
            virtualMachine.getOwners().add(student);
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
    public List<VirtualMachineDTO> getVirtualMachinesForCourse(String courseName) {
        // todo
        return null;
    }

    @Override
    public Optional<VirtualMachineModelDTO> getVirtualMachineModelForCourse(String courseName) {
        return Optional.empty();
    }

    @Override
    public VirtualMachineConfigurationDTO createVirtualMachineConfiguration(Long teamId, int min_vcpu, int max_vcpu, int min_disk_space, int max_disk_space, int min_ram, int max_ram, int max_on, int tot) {

        Team team = teamRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException(teamId.toString()));

        if (team.getVirtualMachineConfiguration() != null) {
            throw new ConfigurationAlreadyDefinedException(teamId.toString());
        }

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
        team.setVirtualMachineConfiguration(configuration);
        virtualMachineConfigurationRepository.save(configuration);

        return modelMapper.map(configuration, VirtualMachineConfigurationDTO.class);
    }

    @Override
    public VirtualMachineConfigurationDTO updateVirtualMachineConfiguration(Long configurationId, VirtualMachineConfigurationDTO configuration) {

        // todo check

        VirtualMachineConfiguration vmc = virtualMachineConfigurationRepository.findById(configurationId).orElseThrow(() -> new ConfigurationNotFoundException(configurationId.toString()));

        int vm_tot = this.getCountVirtualMachinesForTeam(vmc.getTeam().getId());

        if (configuration.getTot() > vm_tot) {
            throw new InvalidTotNumException(String.valueOf(configuration.getTot()));
        }

        List<VirtualMachine> activeVMs = this.getActiveVirtualMachinesForTeam(vmc.getTeam().getId());

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
    public VirtualMachineModelDTO createVirtualMachineModel(String courseName, SystemImage os) {

        Course course = courseRepository.findById(courseName).orElseThrow(() -> new CourseNotFoundException(courseName));

        if (course.getVirtualMachineModel() != null) {
            throw new VirtualMachineModelAlreadyDefinedException(courseName);
        }

        VirtualMachineModel model = VirtualMachineModel.builder()
                .system_image(os)
                .build();
        course.setVirtualMachineModel(model);
        virtualMachineModelRepository.save(model);

        return modelMapper.map(model, VirtualMachineModelDTO.class);
    }

    @Override
    public boolean deleteVirtualMachineModel(Long id, String courseName) {

        // todo check

        VirtualMachineModel model = virtualMachineModelRepository.findById(id).orElseThrow(() -> new VirtualMachineModelNotFoundException(id.toString()));

        /**
         * if there is at least one active virtual machine for model course
         * do not cancel the model
         */
        if (courseRepository.countVirtualMachinesByCourseAndStatus(model.getCourse().getName(), VirtualMachineStatus.ON) > 0) {
            return false;
        }

        model.setCourse(null);
        model.getVirtualMachines().forEach(vm -> vm.setVirtualMachineModel(null));

        virtualMachineModelRepository.delete(model);
        return true;
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
                .getVirtualMachines()
                .stream()
                .map(vm -> modelMapper.map(vm, VirtualMachineDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<VirtualMachineConfigurationDTO> getVirtualMachineConfigurationForTeam(Long teamId) {
        return Optional.ofNullable(teamRepository.findById(teamId)
                .map(t -> modelMapper.map(t.getVirtualMachineConfiguration(), VirtualMachineConfigurationDTO.class))
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
        return teamRepository.countVirtualMachinesByTeamAndStatus(teamId, VirtualMachineStatus.ON);
    }

    private List<VirtualMachine> getActiveVirtualMachinesForTeam(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new TeamNotFoundException(teamId.toString());
        }
        return teamRepository.getVirtualMachinesByTeamAndStatus(teamId, VirtualMachineStatus.ON);
    }

    private int getCountVirtualMachinesForTeam(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new TeamNotFoundException(teamId.toString());
        }
        return teamRepository.countVirtualMachinesByTeam(teamId);
    }
}
