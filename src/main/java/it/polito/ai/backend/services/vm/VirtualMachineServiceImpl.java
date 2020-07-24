package it.polito.ai.backend.services.vm;

import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.entities.*;
import it.polito.ai.backend.repositories.*;
import it.polito.ai.backend.services.team.CourseNotFoundException;
import it.polito.ai.backend.services.team.StudentNotFoundException;
import it.polito.ai.backend.services.team.TeamNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class VirtualMachineServiceImpl implements VirtualMachineService {

    // todo check if the course is enabled

    @Autowired
    VirtualMachineRepository virtualMachineRepository;
    @Autowired
    ConfigurationRepository configurationRepository;
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
    public VirtualMachineDTO createVirtualMachine(String courseId, Long teamId, String studentId, VirtualMachineDTO virtualMachineDTO) {


        Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));

        Team team = course.getTeams()
                .stream()
                .filter(t -> t.getId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()));

        Student student = team.getMembers()
                .stream()
                .filter(s -> s.getId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        /**
         * check whether the model has been defined for the given course or not
         */
        VirtualMachineModel virtualMachineModel = course.getVirtualMachineModel();
        if (virtualMachineModel == null) {
            throw new VirtualMachineModelNotDefinedException(course.getId());
        }


        /**
         * check whether the configuration has been defined for the team or not
         * and if the given configuration is owned by the given team
         */
        if (team.getConfiguration() == null) {
            throw new ConfigurationNotDefinedException(teamId.toString());
        }

        Configuration configuration = team.getConfiguration();

        /**
         * check vm instances number
         * if the current tot +1 is greater than the allowed max then throws an exception
         * otherwise check the available resources for the team of the student
         */

        int vm_tot = team.getVirtualMachines().size();

        if (vm_tot + 1 > configuration.getTot()) {
            throw new VirtualMachineNumberException(String.valueOf(vm_tot+1));
        }

        /**
         * check available resources
         */
        List<VirtualMachine> virtualMachines = team.getVirtualMachines();

        int numVcpu = virtualMachineDTO.getNum_vcpu();

        // todo check when the list is empty
        int currentNumVcpu = virtualMachines
                .stream()
                .reduce(0, (partial, current) -> partial + current.getNum_vcpu(), Integer::sum);
        if (numVcpu < configuration.getMin_vcpu()) {
            throw new InvalidNumVcpuException(String.valueOf(numVcpu), String.valueOf(configuration.getMin_vcpu()));
        } else if (numVcpu > configuration.getMax_vcpu() - currentNumVcpu) {
            throw new NumVcpuNotAvailableException(String.valueOf(numVcpu), String.valueOf(currentNumVcpu+numVcpu), String.valueOf(configuration.getMax_vcpu()));
        }

        int diskSpace = virtualMachineDTO.getDisk_space();

        int currentDiskSpace = virtualMachines
                .stream()
                .reduce(0, (partial, current) -> partial + current.getDisk_space(), Integer::sum);
        if (diskSpace < configuration.getMin_disk_space()) {
            throw new InvalidDiskSpaceException(String.valueOf(diskSpace), String.valueOf(configuration.getMin_disk_space()));
        } else if (diskSpace > configuration.getMax_disk_space() - currentDiskSpace) {
            throw new DiskSpaceNotAvailableException(String.valueOf(diskSpace), String.valueOf(currentDiskSpace+diskSpace), String.valueOf(configuration.getMax_disk_space()));
        }

        int ram = virtualMachineDTO.getRam();

        int currentRam = virtualMachines
                .stream()
                .reduce(0, (partial, current) -> partial + current.getRam(), Integer::sum);
        if (ram < configuration.getMin_ram()) {
            throw new InvalidRamException(String.valueOf(ram), String.valueOf(configuration.getMin_ram()));
        } else if (ram > configuration.getMax_ram() - currentRam) {
            throw new RamNotAvailableException(String.valueOf(ram), String.valueOf(currentRam+ram), String.valueOf(configuration.getMax_ram()));
        }

        VirtualMachine vm = VirtualMachine.builder()
                .num_vcpu(numVcpu)
                .disk_space(diskSpace)
                .ram(ram)
                .status(VirtualMachineStatus.OFF)
                .owners(new ArrayList<>())
                .build();
        vm.addOwner(student);
        vm.setTeam(team);
        vm.setVirtualMachineModel(virtualMachineModel);
        virtualMachineRepository.save(vm);
        return modelMapper.map(vm, VirtualMachineDTO.class);
    }

    @Override
    public VirtualMachineDTO updateVirtualMachine(String courseId, Long teamId, Long vmId, VirtualMachineDTO newVM) {

        VirtualMachine virtualMachine = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeams()
                .stream()
                .filter(t -> t.getId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()))
                .getVirtualMachines()
                .stream()
                .filter(vm -> vm.getId().equals(vmId))
                .findFirst()
                .orElseThrow(() -> new VirtualMachineNotFoundException(String.valueOf(vmId)));

        Configuration configuration = virtualMachine.getTeam().getConfiguration();
        if (configuration == null) {
            throw new ConfigurationNotDefinedException(virtualMachine.getTeam().getId().toString());
        }


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

        List<VirtualMachine> virtualMachines = virtualMachine.getTeam().getVirtualMachines();

        int currentNumVcpu = virtualMachines
                .stream()
                .reduce(0, (partial, current) -> partial + current.getNum_vcpu(), Integer::sum);

        int maxNumVcpu = configuration.getMax_vcpu();
        int minNumVcpu = configuration.getMin_vcpu();
        int newNumVcpu = newVM.getNum_vcpu();
        int oldNumVcpu = virtualMachine.getNum_vcpu();

        if (newNumVcpu < minNumVcpu) {
            throw new InvalidNumVcpuException(String.valueOf(newNumVcpu), String.valueOf(minNumVcpu));
        } else if ((currentNumVcpu - oldNumVcpu) + newNumVcpu > maxNumVcpu) {
            throw new NumVcpuNotAvailableException(String.valueOf(newNumVcpu), String.valueOf((currentNumVcpu - oldNumVcpu) + newNumVcpu), String.valueOf(maxNumVcpu));
        }


        int currentDiskSpace = virtualMachines
                .stream()
                .reduce(0, (partial, current) -> partial + current.getDisk_space(), Integer::sum);

        int maxDiskSpace = configuration.getMax_disk_space();
        int minDiskSpace = configuration.getMin_disk_space();
        int newDiskSpace = newVM.getDisk_space();
        int oldDiskSpace = virtualMachine.getDisk_space();

        if (newDiskSpace < minDiskSpace) {
            throw new InvalidDiskSpaceException(String.valueOf(newDiskSpace), String.valueOf(minDiskSpace));
        } else if ((currentDiskSpace - oldDiskSpace) + newDiskSpace > maxDiskSpace) {
            throw new DiskSpaceNotAvailableException(String.valueOf(newDiskSpace), String.valueOf((currentDiskSpace - oldDiskSpace) + newDiskSpace), String.valueOf(maxDiskSpace));
        }


        int currentRam = virtualMachines
                .stream()
                .reduce(0, (partial, current) -> partial + current.getRam(), Integer::sum);

        int maxRam = configuration.getMax_ram();
        int minRam = configuration.getMin_ram();
        int newRam = newVM.getRam();
        int oldRam = virtualMachine.getRam();

        if (newRam < minRam) {
            throw new InvalidRamException(String.valueOf(newRam), String.valueOf(minRam));
        } else if ((currentRam - oldRam) + newRam > maxRam) {
            throw new RamNotAvailableException(String.valueOf(newRam), String.valueOf((currentRam - oldRam) + newRam), String.valueOf(maxRam));
        }

        virtualMachine.setNum_vcpu(newVM.getNum_vcpu());
        virtualMachine.setDisk_space(newVM.getDisk_space());
        virtualMachine.setRam(newVM.getRam());

        return modelMapper.map(virtualMachineRepository.save(virtualMachine), VirtualMachineDTO.class);
    }

    @Override
    public boolean deleteVirtualMachine(String courseId, Long teamId, Long vmId) {

        VirtualMachine virtualMachine = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeams()
                .stream()
                .filter(t -> t.getId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()))
                .getVirtualMachines()
                .stream()
                .filter(vm -> vm.getId().equals(vmId))
                .findFirst()
                .orElseThrow(() -> new VirtualMachineNotFoundException(vmId.toString()));

        if (virtualMachine.getStatus() == VirtualMachineStatus.ON) {
            return false;
        }

        virtualMachine.setVirtualMachineModel(null);
        virtualMachine.setTeam(null);
        for (Student s : new ArrayList<>(virtualMachine.getOwners())) {
            s.removeVirtualMachine(virtualMachine);
        }
        // virtualMachine.getOwners().forEach(owner -> owner.removeVirtualMachine(virtualMachine)); throws java.util.ConcurrentModificationException
        virtualMachineRepository.delete(virtualMachine);
        return true;
    }

    @Override
    public void turnOnVirtualMachine(String courseId, Long teamId, Long vmId) {

        VirtualMachine virtualMachine = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeams()
                .stream()
                .filter(t -> t.getId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()))
                .getVirtualMachines()
                .stream()
                .filter(vm -> vm.getId().equals(vmId))
                .findFirst()
                .orElseThrow(() -> new VirtualMachineNotFoundException(vmId.toString()));

        if (virtualMachine.getStatus() == VirtualMachineStatus.ON) {
            return;
        }

        Team team = virtualMachine.getTeam();
        Configuration configuration = team.getConfiguration();

        if (configuration == null) {
            throw new ConfigurationNotDefinedException(team.getId().toString());
        }

        List<VirtualMachine> virtualMachines = team.getVirtualMachines();
        long activeVMs = virtualMachines
                .stream()
                .filter(vm -> vm.getStatus() == VirtualMachineStatus.ON)
                .count();

        if (activeVMs + 1 > configuration.getMax_on()) {
            throw new ActiveVirtualMachineNumberException(String.valueOf(activeVMs+1), String.valueOf(configuration.getMax_on()));
        }

        virtualMachine.setStatus(VirtualMachineStatus.ON);
    }

    @Override
    public void turnOffVirtualMachine(String courseId, Long teamId, Long vmId) {

        VirtualMachine virtualMachine = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeams()
                .stream()
                .filter(t -> t.getId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()))
                .getVirtualMachines()
                .stream()
                .filter(vm -> vm.getId().equals(vmId))
                .findFirst()
                .orElseThrow(() -> new VirtualMachineNotFoundException(vmId.toString()));

        virtualMachine.setStatus(VirtualMachineStatus.OFF);
    }

    @Override
    public boolean addOwnerToVirtualMachine(String courseId, Long teamId, String studentId, Long vmId) {

        Team team = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeams()
                .stream()
                .filter(t -> t.getId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()));

        VirtualMachine virtualMachine = team.getVirtualMachines()
                .stream()
                .filter(vm -> vm.getId().equals(vmId))
                .findFirst()
                .orElseThrow(() -> new VirtualMachineNotFoundException(vmId.toString()));

        Student student = team.getMembers()
                .stream()
                .filter(s -> s.getId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        if (!virtualMachine.getOwners().contains(student)) {
            virtualMachine.getOwners().add(student);
            return true;
        }
        return false;
    }

    @Override
    public Optional<VirtualMachineModelDTO> getVirtualMachineModelForCourse(String courseId) {
        Course course =  courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        return Optional.ofNullable(modelMapper.map(course.getVirtualMachineModel(), VirtualMachineModelDTO.class));
    }

    @Override
    public ConfigurationDTO createConfiguration(String courseId, Long teamId, ConfigurationDTO configurationDTO) {

        Team team = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeams()
                .stream()
                .filter(t -> t.getId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()));

        if (team.getConfiguration() != null) {
            throw new ConfigurationAlreadyDefinedException(teamId.toString());
        }

        int min_vcpu = configurationDTO.getMin_vcpu();
        int max_vcpu = configurationDTO.getMax_vcpu();
        int min_disk_space = configurationDTO.getMin_disk();
        int max_disk_space = configurationDTO.getMax_disk();
        int min_ram = configurationDTO.getMin_ram();
        int max_ram = configurationDTO.getMax_ram();
        int max_on = configurationDTO.getMax_on();
        int tot = configurationDTO.getTot();

        validateConfiguration(max_on, tot, min_vcpu, max_vcpu, min_disk_space, max_disk_space, min_ram, max_ram);

        Configuration configuration = Configuration.builder()
                .min_vcpu(min_vcpu)
                .max_vcpu(max_vcpu)
                .min_disk_space(min_disk_space)
                .max_disk_space(max_disk_space)
                .min_ram(min_ram)
                .max_ram(max_ram)
                .max_on(max_on)
                .tot(tot)
                .build();
        team.setConfiguration(configuration);
        configurationRepository.save(configuration);

        return modelMapper.map(configuration, ConfigurationDTO.class);
    }

    @Override
    public ConfigurationDTO updateConfiguration(String courseId, Long teamId, ConfigurationDTO configuration) {

        Team team = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeams()
                .stream()
                .filter(t -> t.getId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()));

        Configuration vmc = team.getConfiguration();
        if (vmc == null) {
            throw new ConfigurationNotDefinedException(teamId.toString());
        }

        int max_on = configuration.getMax_on();
        int tot = configuration.getTot();
        int min_vcpu = configuration.getMin_vcpu();
        int max_vcpu = configuration.getMax_vcpu();
        int min_disk_space = configuration.getMin_disk();
        int max_disk_space = configuration.getMax_disk();
        int min_ram = configuration.getMin_ram();
        int max_ram = configuration.getMax_ram();

        validateConfiguration(max_on, tot, min_vcpu, max_vcpu, min_disk_space, max_disk_space, min_ram, max_ram);

        int vm_tot = team.getVirtualMachines().size();

        if (configuration.getTot() < vm_tot) {
            throw new InvalidTotNumException(String.valueOf(configuration.getTot()), String.valueOf(vm_tot));
        }

        List<VirtualMachine> virtualMachines = team.getVirtualMachines();
        int activeVMs = Math.toIntExact(virtualMachines
                .stream()
                .filter(vm -> vm.getStatus() == VirtualMachineStatus.ON)
                .count());

        if (activeVMs > configuration.getMax_on()) {
            throw new InvalidMaxActiveException(String.valueOf(configuration.getMax_on()), String.valueOf(activeVMs));
        }

        /**
         * check if the current resource in use does not exceed the configuration one
         * and if any is lower then the minimum resource threshold
         */

        int currentNumVcpu = virtualMachines
                .stream()
                .reduce(0, (partial, current) -> partial + current.getNum_vcpu(), Integer::sum);
        if (currentNumVcpu > configuration.getMax_vcpu()) {
            throw new InvalidConfigurationException(String.format("%s max vcpu not allowed, current value %s", configuration.getMax_vcpu(), currentNumVcpu));
        } else if (virtualMachines.stream().anyMatch(vm -> vm.getNum_vcpu() < configuration.getMin_vcpu())) {
            throw new InvalidConfigurationException(String.format("a virtual machine is using less than %s vcpu", configuration.getMin_vcpu()));
        }

        int currentDiskSpace = virtualMachines
                .stream()
                .reduce(0, (partial, current) -> partial + current.getDisk_space(), Integer::sum);
        if (currentDiskSpace > configuration.getMax_disk()) {
            throw new InvalidConfigurationException(String.format("%s disk space not allowed, current value %s", configuration.getMax_disk(), currentDiskSpace));
        } else if (virtualMachines.stream().anyMatch(vm -> vm.getDisk_space() < configuration.getMin_disk())) {
            throw new InvalidConfigurationException(String.format("a virtual machine is using less than %s vcpu", configuration.getMin_vcpu()));
        }

        int currentRam = virtualMachines
                .stream()
                .reduce(0, (partial, current) -> partial + current.getRam(), Integer::sum);
        if (currentRam > configuration.getMax_ram()) {
            throw new InvalidConfigurationException(String.format("%s ram not allowed, current value %s", configuration.getMax_ram(), currentRam));
        } else if (virtualMachines.stream().anyMatch(vm -> vm.getRam() < configuration.getMin_ram())) {
            throw new InvalidConfigurationException(String.format("a virtual machine is using less than %s ram", configuration.getMin_ram()));
        }

        vmc.setTot(configuration.getTot());
        vmc.setMax_on(configuration.getMax_on());
        vmc.setMin_vcpu(configuration.getMin_vcpu());
        vmc.setMax_vcpu(configuration.getMax_vcpu());
        vmc.setMax_disk_space(configuration.getMax_disk());
        vmc.setMin_disk_space(configuration.getMin_disk());
        vmc.setMax_ram(configuration.getMax_ram());
        vmc.setMin_ram(configuration.getMin_ram());

        return modelMapper.map(configurationRepository.save(vmc), ConfigurationDTO.class);
    }

    private void validateConfiguration(int max_on, int tot, int min_vcpu, int max_vcpu, int min_disk_space, int max_disk_space, int min_ram, int max_ram) {
        if (max_on > tot) {
            throw new InvalidConfigurationException(String.format("the total number of virtual machines %s cannot be less than the maximum active ones %s", tot, max_on));
        } else if (min_vcpu > max_vcpu) {
            throw new InvalidConfigurationException(String.format("the minimum num vcpu value %s cannot be greater than the maximum number of vcpu %s", min_vcpu, max_vcpu));
        } else if (min_disk_space > max_disk_space) {
            throw new InvalidConfigurationException(String.format("the minimum disk space value %s cannot be greater than the maximum disk space %s", min_disk_space, max_disk_space));
        } else if (min_ram > max_ram) {
            throw new InvalidConfigurationException(String.format("the minimum ram value %s cannot be greater than the maximum ram %s", min_ram, max_ram));
        }
    }

    @Override
    public VirtualMachineModelDTO createVirtualMachineModel(String courseId, VirtualMachineModelDTO modelDTO) {

        Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));

        if (course.getVirtualMachineModel() != null) {
            throw new VirtualMachineModelAlreadyDefinedException(courseId);
        }

        VirtualMachineModel model = VirtualMachineModel.builder()
                .system_image(modelDTO.getOs())
                .build();
        course.setVirtualMachineModel(model);
        virtualMachineModelRepository.save(model);

        return modelMapper.map(model, VirtualMachineModelDTO.class);
    }

    @Override
    public boolean deleteVirtualMachineModel(String courseId) {

        Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        VirtualMachineModel model = course.getVirtualMachineModel();

        if (model == null) {
            throw new VirtualMachineModelNotDefinedException(courseId);
        }

        /**
         * if there is at least one active virtual machine for model course
         * do not cancel the model
         */
        if (courseRepository.countVirtualMachinesByCourseAndStatus(courseId, VirtualMachineStatus.ON) > 0) {
            return false;
        }

        model.setCourse(null);
        /**
         * this remove will remove all the virtual machines
         * and cascade the operation to all their relationships (owners and teams)
         */
        model.removeVirtualMachines();
        // model.getVirtualMachines().forEach(vm -> vm.setVirtualMachineModel(null)); throws java.util.ConcurrentModificationException

        virtualMachineModelRepository.delete(model);
        return true;
    }

    @Override
    public Optional<VirtualMachineDTO> getVirtualMachine(String courseId, Long teamId, Long vmId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeams()
                .stream()
                .filter(t -> t.getId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()))
                .getVirtualMachines()
                .stream()
                .filter(vm -> vm.getId().equals(vmId))
                .findFirst()
                .map(vm -> modelMapper.map(vm, VirtualMachineDTO.class));
    }

    @Override
    public List<StudentDTO> getOwnersForVirtualMachine(String courseId, Long teamId, Long vmId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeams()
                .stream()
                .filter(t -> t.getId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()))
                .getVirtualMachines()
                .stream()
                .filter(vm -> vm.getId().equals(vmId))
                .findFirst()
                .orElseThrow(() -> new VirtualMachineNotFoundException(vmId.toString()))
                .getOwners()
                .stream()
                .map(s -> modelMapper.map(s, StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<VirtualMachineDTO> getVirtualMachinesForTeam(String courseId, Long teamId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeams()
                .stream()
                .filter(t -> t.getId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()))
                .getVirtualMachines()
                .stream()
                .map(vm -> modelMapper.map(vm, VirtualMachineDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ConfigurationDTO> getConfigurationForTeam(String courseId, Long teamId) {
        Configuration configuration = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeams()
                .stream()
                .filter(t -> t.getId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()))
                .getConfiguration();

        return Optional.ofNullable(modelMapper.map(configuration, ConfigurationDTO.class));
    }

    @Override
    public int getActiveVcpuForTeam(String courseId, Long teamId) {
        courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeams()
                .stream()
                .filter(t -> t.getId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()));

        return teamRepository.getActiveNumVcpuByTeam(teamId);
    }

    @Override
    public int getActiveDiskSpaceForTeam(String courseId, Long teamId) {
        courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeams()
                .stream()
                .filter(t -> t.getId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()));
        return teamRepository.getActiveDiskSpaceByTeam(teamId);
    }

    @Override
    public int getActiveRAMForTeam(String courseId, Long teamId) {
        courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeams()
                .stream()
                .filter(t -> t.getId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()));
        return teamRepository.getActiveRamByTeam(teamId);
    }

    @Override
    public int getCountActiveVirtualMachinesForTeam(String courseId, Long teamId) {
        courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeams()
                .stream()
                .filter(t -> t.getId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()));
        return teamRepository.countVirtualMachinesByTeamAndStatus(teamId, VirtualMachineStatus.ON);
    }

    @Override
    public int getCountVirtualMachinesForTeam(String courseId, Long teamId) {
        courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeams()
                .stream()
                .filter(t -> t.getId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()));
        return teamRepository.countVirtualMachinesByTeam(teamId);
    }

    @Override
    public Map<String, Integer> getResourcesByTeam(String courseId, Long teamId) {
        Team team = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeams()
                .stream()
                .filter(t -> t.getId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()));

        Configuration configuration = team.getConfiguration();

        if (configuration == null) {
            throw new ConfigurationNotDefinedException(teamId.toString());
        }

        Map<String, Integer> resources = new HashMap<>();

        List<VirtualMachine> activeVirtualMachines = teamRepository.getVirtualMachinesByTeamAndStatus(teamId, VirtualMachineStatus.ON);
        int activeNumVcpu = activeVirtualMachines
                .stream()
                .reduce(0, (partial, current) -> partial + current.getNum_vcpu(), Integer::sum);
        int activeDiskSpace = activeVirtualMachines
                .stream()
                .reduce(0, (partial, current) -> partial + current.getDisk_space(), Integer::sum);
        int activeRam = activeVirtualMachines
                .stream()
                .reduce(0, (partial, current) -> partial + current.getRam(), Integer::sum);
        int activeVMs = activeVirtualMachines.size();
        int tot = team.getVirtualMachines().size();

        resources.put("activeNumVcpu", activeNumVcpu);
        resources.put("activeDiskSpace", activeDiskSpace);
        resources.put("activeRam", activeRam);
        resources.put("activeVMs", activeVMs);
        resources.put("tot", tot);

        resources.put("maxVcpu", configuration.getMax_vcpu());
        resources.put("maxDiskSpace", configuration.getMax_disk_space());
        resources.put("maxRam", configuration.getMax_ram());
        resources.put("maxOn", configuration.getMax_on());
        resources.put("maxTot", configuration.getTot());

        return resources;
    }
}
