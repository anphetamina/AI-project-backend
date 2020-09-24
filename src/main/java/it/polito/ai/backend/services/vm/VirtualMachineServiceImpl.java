package it.polito.ai.backend.services.vm;

import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.entities.*;
import it.polito.ai.backend.repositories.*;
import it.polito.ai.backend.services.team.CourseNotEnabledException;
import it.polito.ai.backend.services.team.CourseNotFoundException;
import it.polito.ai.backend.services.team.StudentNotFoundException;
import it.polito.ai.backend.services.team.TeamNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
/**
 * the validation of the arguments is done inside the controllers
 */
// @Validated
public class VirtualMachineServiceImpl implements VirtualMachineService {

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
    @PreAuthorize("hasRole('STUDENT') and @securityServiceImpl.isAuthorized(#studentId) and @securityServiceImpl.isPartOf(#teamId)")
    public VirtualMachineDTO createVirtualMachine(String studentId, Long teamId, Long modelId, VirtualMachineDTO virtualMachineDTO) {

        Student student = studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException(teamId.toString()));
        VirtualMachineModel model = virtualMachineModelRepository.findById(modelId).orElseThrow(() -> new VirtualMachineModelNotFoundException(modelId.toString()));

        Course course = team.getCourse();

        /**
         * check if the team is part of a course
         * if not check if that is not disabled
         */
        if (course == null) {
            throw new CourseNotFoundException(String.format("for team %s", teamId));
        } else if (!course.isEnabled()) {
            throw new CourseNotEnabledException(course.getId());
        }

        /**
         * check whether the model has been defined for the given course or not
         * check if the model matches the model of the team course
         */

        if (course.getVirtualMachineModel() == null) {
            throw new VirtualMachineModelNotDefinedException(course.getId());
        } else if (!course.getVirtualMachineModel().getId().equals(modelId)) {
            throw new InvalidVirtualMachineModelException(modelId.toString());
        }

        /**
         * check whether the configuration has been defined for the team or not
         * and if the given configuration is owned by the given team
         */
        Configuration configuration = team.getConfiguration();
        if (configuration == null) {
            throw new ConfigurationNotDefinedException(teamId.toString());
        }


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
        vm.setVirtualMachineModel(model);
        virtualMachineRepository.save(vm);
        return modelMapper.map(vm, VirtualMachineDTO.class);
    }

    @Override
    @PreAuthorize("hasRole('STUDENT') and @securityServiceImpl.isOwnerOf(#vmId)")
    public VirtualMachineDTO updateVirtualMachine(Long vmId, VirtualMachineDTO virtualMachineDTO) {

        if (!vmId.equals(virtualMachineDTO.getId())) {
            throw new VirtualMachineIdNotCorrespondingException(vmId.toString(), virtualMachineDTO.getId().toString());
        }

        VirtualMachine virtualMachine = virtualMachineRepository.findById(virtualMachineDTO.getId()).orElseThrow(() -> new VirtualMachineNotFoundException(virtualMachineDTO.getId().toString()));

        Team team = virtualMachine.getTeam();
        if (team == null) {
            throw new TeamNotFoundException(String.format("for virtual machine %s", virtualMachine.getId()));
        }

        Course course = team.getCourse();
        if (course == null) {
            throw new CourseNotFoundException(String.format("for team %s", team.getId()));
        } else if (!course.isEnabled()) {
            throw new CourseNotEnabledException(course.getId());
        }

        Configuration configuration = team.getConfiguration();
        if (configuration == null) {
            throw new ConfigurationNotDefinedException(team.getId().toString());
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

        List<VirtualMachine> virtualMachines = team.getVirtualMachines();

        int currentNumVcpu = virtualMachines
                .stream()
                .reduce(0, (partial, current) -> partial + current.getNum_vcpu(), Integer::sum);

        int maxNumVcpu = configuration.getMax_vcpu();
        int minNumVcpu = configuration.getMin_vcpu();
        int newNumVcpu = virtualMachineDTO.getNum_vcpu();
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
        int newDiskSpace = virtualMachineDTO.getDisk_space();
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
        int newRam = virtualMachineDTO.getRam();
        int oldRam = virtualMachine.getRam();

        if (newRam < minRam) {
            throw new InvalidRamException(String.valueOf(newRam), String.valueOf(minRam));
        } else if ((currentRam - oldRam) + newRam > maxRam) {
            throw new RamNotAvailableException(String.valueOf(newRam), String.valueOf((currentRam - oldRam) + newRam), String.valueOf(maxRam));
        }

        virtualMachine.setNum_vcpu(virtualMachineDTO.getNum_vcpu());
        virtualMachine.setDisk_space(virtualMachineDTO.getDisk_space());
        virtualMachine.setRam(virtualMachineDTO.getRam());

        return modelMapper.map(virtualMachineRepository.save(virtualMachine), VirtualMachineDTO.class);
    }

    @Override
    @PreAuthorize("hasRole('STUDENT') and @securityServiceImpl.isOwnerOf(#vmId)")
    public boolean deleteVirtualMachine(Long vmId) {

        VirtualMachine virtualMachine = virtualMachineRepository.findById(vmId).orElseThrow(() -> new VirtualMachineNotFoundException(vmId.toString()));

        Team team = virtualMachine.getTeam();
        if (team == null) {
            throw new TeamNotFoundException(String.format("for virtual machine %s", virtualMachine.getId()));
        }

        Course course = team.getCourse();
        if (course == null) {
            throw new CourseNotFoundException(String.format("for team %s", team.getId()));
        } else if (!course.isEnabled()) {
            throw new CourseNotEnabledException(course.getId());
        }

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
    @PreAuthorize("hasRole('STUDENT') and @securityServiceImpl.canUse(#vmId)")
    public void turnOnVirtualMachine(Long vmId) {

        VirtualMachine virtualMachine = virtualMachineRepository.findById(vmId).orElseThrow(() -> new VirtualMachineNotFoundException(vmId.toString()));

        Team team = virtualMachine.getTeam();
        if (team == null) {
            throw new TeamNotFoundException(String.format("for virtual machine %s", virtualMachine.getId()));
        }

        Course course = team.getCourse();
        if (course == null) {
            throw new CourseNotFoundException(String.format("for team %s", team.getId()));
        } else if (!course.isEnabled()) {
            throw new CourseNotEnabledException(course.getId());
        }

        if (virtualMachine.getStatus() == VirtualMachineStatus.ON) {
            return;
        }

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
    @PreAuthorize("hasRole('STUDENT') and @securityServiceImpl.canUse(#vmId)")
    public void turnOffVirtualMachine(Long vmId) {

        VirtualMachine virtualMachine = virtualMachineRepository.findById(vmId).orElseThrow(() -> new VirtualMachineNotFoundException(vmId.toString()));

        Team team = virtualMachine.getTeam();
        if (team == null) {
            throw new TeamNotFoundException(String.format("for virtual machine %s", virtualMachine.getId()));
        }

        Course course = team.getCourse();
        if (course == null) {
            throw new CourseNotFoundException(String.format("for team %s", team.getId()));
        } else if (!course.isEnabled()) {
            throw new CourseNotEnabledException(course.getId());
        }

        virtualMachine.setStatus(VirtualMachineStatus.OFF);
    }

    @Override
    @PreAuthorize("hasRole('STUDENT') and @securityServiceImpl.isOwnerOf(#vmId)")
    public boolean addOwnerToVirtualMachine(String studentId, Long vmId) {

        VirtualMachine virtualMachine = virtualMachineRepository.findById(vmId).orElseThrow(() -> new VirtualMachineNotFoundException(vmId.toString()));

        Team team = virtualMachine.getTeam();
        if (team == null) {
            throw new TeamNotFoundException(String.format("for virtual machine %s", virtualMachine.getId()));
        }

        Course course = team.getCourse();
        if (course == null) {
            throw new CourseNotFoundException(String.format("for team %s", team.getId()));
        } else if (!course.isEnabled()) {
            throw new CourseNotEnabledException(course.getId());
        }

        Student student = studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));

        if (!virtualMachine.getOwners().contains(student)) {
            virtualMachine.addOwner(student);
            return true;
        }
        return false;
    }

    @Override
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.isTaught(#courseId)) or (hasRole('STUDENT') and @securityServiceImpl.isEnrolled(#courseId))")
    public Optional<VirtualMachineModelDTO> getVirtualMachineModelForCourse(String courseId) {
        VirtualMachineModel virtualMachineModel =  courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getVirtualMachineModel();

        if (virtualMachineModel == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(modelMapper.map(virtualMachineModel, VirtualMachineModelDTO.class));
    }

    @Override
    @PreAuthorize("hasRole('TEACHER') and @securityServiceImpl.isHelping(#teamId)")
    public ConfigurationDTO createConfiguration(Long teamId, ConfigurationDTO configurationDTO) {

        Team team = teamRepository.findById(teamId).filter(t -> t.getStatus().equals(TeamStatus.ACTIVE) )
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()));

        Course course = team.getCourse();
        if (course == null) {
            throw new CourseNotFoundException(String.format("for team %s", team.getId()));
        } else if (!course.isEnabled()) {
            throw new CourseNotEnabledException(course.getId());
        }

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
    @PreAuthorize("hasRole('TEACHER') and @securityServiceImpl.canManage(#configurationId)")
    public ConfigurationDTO updateConfiguration(Long configurationId, ConfigurationDTO configurationDTO) {

        if (!configurationId.equals(configurationDTO.getId())) {
            throw new ConfigurationIdNotCorrespondingException(configurationId.toString(), configurationDTO.getId().toString());
        }

        Configuration configuration = configurationRepository.findById(configurationDTO.getId()).orElseThrow(() -> new ConfigurationNotFoundException(configurationDTO.getId().toString()));

        Team team = configuration.getTeam();

        if (team == null) {
            throw new TeamNotFoundException(String.format("for configuration %s", configuration.getId()));
        } else if (!team.getCourse().isEnabled()) {
            throw new CourseNotEnabledException(team.getCourse().getId());
        }

        int max_on = configurationDTO.getMax_on();
        int tot = configurationDTO.getTot();
        int min_vcpu = configurationDTO.getMin_vcpu();
        int max_vcpu = configurationDTO.getMax_vcpu();
        int min_disk_space = configurationDTO.getMin_disk();
        int max_disk_space = configurationDTO.getMax_disk();
        int min_ram = configurationDTO.getMin_ram();
        int max_ram = configurationDTO.getMax_ram();

        validateConfiguration(max_on, tot, min_vcpu, max_vcpu, min_disk_space, max_disk_space, min_ram, max_ram);

        int vm_tot = team.getVirtualMachines().size();

        if (configurationDTO.getTot() < vm_tot) {
            throw new InvalidTotNumException(String.valueOf(configurationDTO.getTot()), String.valueOf(vm_tot));
        }

        List<VirtualMachine> virtualMachines = team.getVirtualMachines();
        int activeVMs = Math.toIntExact(virtualMachines
                .stream()
                .filter(vm -> vm.getStatus() == VirtualMachineStatus.ON)
                .count());

        if (activeVMs > configurationDTO.getMax_on()) {
            throw new InvalidMaxActiveException(String.valueOf(configurationDTO.getMax_on()), String.valueOf(activeVMs));
        }

        /**
         * check if the current resource in use does not exceed the configuration one
         * and if any is lower then the minimum resource threshold
         */

        int currentNumVcpu = virtualMachines
                .stream()
                .reduce(0, (partial, current) -> partial + current.getNum_vcpu(), Integer::sum);
        if (currentNumVcpu > configurationDTO.getMax_vcpu()) {
            throw new InvalidConfigurationException(String.format("%s max vcpu not allowed, current value %s", configurationDTO.getMax_vcpu(), currentNumVcpu));
        } else if (virtualMachines.stream().anyMatch(vm -> vm.getNum_vcpu() < configurationDTO.getMin_vcpu())) {
            throw new InvalidConfigurationException(String.format("a virtual machine is using less than %s vcpu", configurationDTO.getMin_vcpu()));
        }

        int currentDiskSpace = virtualMachines
                .stream()
                .reduce(0, (partial, current) -> partial + current.getDisk_space(), Integer::sum);
        if (currentDiskSpace > configurationDTO.getMax_disk()) {
            throw new InvalidConfigurationException(String.format("%s disk space not allowed, current value %s", configurationDTO.getMax_disk(), currentDiskSpace));
        } else if (virtualMachines.stream().anyMatch(vm -> vm.getDisk_space() < configurationDTO.getMin_disk())) {
            throw new InvalidConfigurationException(String.format("a virtual machine is using less than %s vcpu", configurationDTO.getMin_vcpu()));
        }

        int currentRam = virtualMachines
                .stream()
                .reduce(0, (partial, current) -> partial + current.getRam(), Integer::sum);
        if (currentRam > configurationDTO.getMax_ram()) {
            throw new InvalidConfigurationException(String.format("%s ram not allowed, current value %s", configurationDTO.getMax_ram(), currentRam));
        } else if (virtualMachines.stream().anyMatch(vm -> vm.getRam() < configurationDTO.getMin_ram())) {
            throw new InvalidConfigurationException(String.format("a virtual machine is using less than %s ram", configurationDTO.getMin_ram()));
        }

        configuration.setTot(configurationDTO.getTot());
        configuration.setMax_on(configurationDTO.getMax_on());
        configuration.setMin_vcpu(configurationDTO.getMin_vcpu());
        configuration.setMax_vcpu(configurationDTO.getMax_vcpu());
        configuration.setMax_disk_space(configurationDTO.getMax_disk());
        configuration.setMin_disk_space(configurationDTO.getMin_disk());
        configuration.setMax_ram(configurationDTO.getMax_ram());
        configuration.setMin_ram(configurationDTO.getMin_ram());

        return modelMapper.map(configurationRepository.save(configuration), ConfigurationDTO.class);
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
    @PreAuthorize("hasRole('TEACHER') and @securityServiceImpl.isTaught(#courseId)")
    public VirtualMachineModelDTO createVirtualMachineModel(String courseId, VirtualMachineModelDTO modelDTO) {

        Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));

        if (!course.isEnabled()) {
            throw new CourseNotEnabledException(courseId);
        }

        if (course.getVirtualMachineModel() != null) {
            throw new VirtualMachineModelAlreadyDefinedException(courseId);
        }

        VirtualMachineModel model = VirtualMachineModel.builder()
                .os(modelDTO.getOs())
                .build();
        course.setVirtualMachineModel(model);
        virtualMachineModelRepository.save(model);

        return modelMapper.map(model, VirtualMachineModelDTO.class);
    }

    @Override
    @PreAuthorize("hasRole('TEACHER') and @securityServiceImpl.hasDefined(#modelId)")
    public boolean deleteVirtualMachineModel(Long modelId) {

        VirtualMachineModel virtualMachineModel = virtualMachineModelRepository.findById(modelId).orElseThrow(() -> new VirtualMachineModelNotFoundException(modelId.toString()));

        Course course = virtualMachineModel.getCourse();

        if (course == null) {
            throw new CourseNotFoundException(String.format("for virtual machine model %s", modelId));
        } else if (!course.isEnabled()) {
            throw new CourseNotEnabledException(course.getId());
        }

        /**
         * if there is at least one active virtual machine for model course
         * do not cancel the model
         */
        if (courseRepository.countVirtualMachinesByCourseAndStatus(course.getId(), VirtualMachineStatus.ON) > 0) {
            return false;
        }

        virtualMachineModel.setCourse(null);
        /**
         * this will remove all the virtual machines
         * and cascade the operation to all their relationships (owners and teams)
         */
        virtualMachineModel.removeVirtualMachines();
        // virtualMachineModel.getVirtualMachines().forEach(vm -> vm.setVirtualMachineModel(null)); throws java.util.ConcurrentModificationException

        virtualMachineModelRepository.delete(virtualMachineModel);
        return true;
    }

    @Override
    @PreAuthorize("(hasRole('STUDENT') and @securityServiceImpl.canUse(#vmId)) or (hasRole('TEACHER') and @securityServiceImpl.canConnect(#vmId))")
    public Optional<VirtualMachineDTO> getVirtualMachine(Long vmId) {
        return virtualMachineRepository.findById(vmId).map(vm -> modelMapper.map(vm, VirtualMachineDTO.class));
    }

    @Override
    @PreAuthorize("(hasRole('STUDENT') and @securityServiceImpl.canAccess(#modelId)) or (hasRole('TEACHER') and @securityServiceImpl.hasDefined(#modelId))")
    public Optional<VirtualMachineModelDTO> getVirtualMachineModel(Long modelId) {
        return virtualMachineModelRepository.findById(modelId).map(m -> modelMapper.map(m, VirtualMachineModelDTO.class));
    }

    @Override
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.canManage(#configurationId)) or (hasRole('STUDENT') and @securityServiceImpl.canSee(#configurationId))")
    public Optional<ConfigurationDTO> getConfiguration(Long configurationId) {
        return configurationRepository.findById(configurationId).map(c -> modelMapper.map(c, ConfigurationDTO.class));
    }

    @Override
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.canConnect(#vmId)) or (hasRole('STUDENT') and @securityServiceImpl.canUse(#vmId))")
    public List<StudentDTO> getOwnersForVirtualMachine(Long vmId) {
        return virtualMachineRepository.findById(vmId)
                .orElseThrow(() -> new VirtualMachineNotFoundException(vmId.toString()))
                .getOwners()
                .stream()
                .map(o -> modelMapper.map(o, StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.isHelping(#teamId)) or (hasRole('STUDENT') and @securityServiceImpl.isPartOf(#teamId))")
    public List<VirtualMachineDTO> getVirtualMachinesForTeam(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()))
                .getVirtualMachines()
                .stream()
                .map(vm -> modelMapper.map(vm, VirtualMachineDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasRole('STUDENT') and @securityServiceImpl.isAuthorized(#studentId)")
    public List<VirtualMachineDTO> getVirtualMachinesForStudent(String studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId))
                .getVirtual_machines()
                .stream()
                .map(vm -> modelMapper.map(vm, VirtualMachineDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("(hasRole('TEACHER') ) or (hasRole('STUDENT') and @securityServiceImpl.isPartOf(#teamId))")
    public Optional<ConfigurationDTO> getConfigurationForTeam(Long teamId) {
        Configuration configuration = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()))
                .getConfiguration();

        if (configuration == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(modelMapper.map(configuration, ConfigurationDTO.class));
    }

    @Override
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.canConnect(#vmId)) or (hasRole('STUDENT') and @securityServiceImpl.canUse(#vmId))")
    public Optional<TeamDTO> getTeamForVirtualMachine(Long vmId) {
        Team team = virtualMachineRepository.findById(vmId)
                .orElseThrow(() -> new VirtualMachineNotFoundException(vmId.toString()))
                .getTeam();

        if (team == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(modelMapper.map(team, TeamDTO.class));
    }

    @Override
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.canConnect(#vmId)) or (hasRole('STUDENT') and @securityServiceImpl.canUse(#vmId))")
    public Optional<VirtualMachineModelDTO> getVirtualMachineModelForVirtualMachine(Long vmId) {
        VirtualMachineModel virtualMachineModel = virtualMachineRepository.findById(vmId)
                .orElseThrow(() -> new VirtualMachineNotFoundException(vmId.toString()))
                .getVirtualMachineModel();

        if (virtualMachineModel == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(modelMapper.map(virtualMachineModel, VirtualMachineModelDTO.class));
    }

    @Override
    @PreAuthorize("(hasRole('STUDENT') and @securityServiceImpl.canAccess(#modelId)) or (hasRole('TEACHER') and @securityServiceImpl.hasDefined(#modelId))")
    public Optional<CourseDTO> getCourseForVirtualMachineModel(Long modelId) {
        Course course = virtualMachineModelRepository.findById(modelId)
                .orElseThrow(() -> new VirtualMachineModelNotFoundException(modelId.toString()))
                .getCourse();

        if (course == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(modelMapper.map(course, CourseDTO.class));
    }

    @Override
    @PreAuthorize("(hasRole('STUDENT') and @securityServiceImpl.canSee(#configurationId)) or (hasRole('TEACHER') and @securityServiceImpl.canManage(#configurationId))")
    public Optional<TeamDTO> getTeamForConfiguration(Long configurationId) {
        Team team = configurationRepository.findById(configurationId)
                .orElseThrow(() -> new ConfigurationNotFoundException(configurationId.toString()))
                .getTeam();

        if (team == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(modelMapper.map(team, TeamDTO.class));
    }

    @Override
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.isHelping(#teamId)) or (hasRole('STUDENT') and @securityServiceImpl.isPartOf(#teamId))")
    public int getActiveVcpuForTeam(Long teamId) {
        return teamRepository.getActiveNumVcpuByTeam(teamId);
    }

    @Override
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.isHelping(#teamId)) or (hasRole('STUDENT') and @securityServiceImpl.isPartOf(#teamId))")
    public int getActiveDiskSpaceForTeam(Long teamId) {
        return teamRepository.getActiveDiskSpaceByTeam(teamId);
    }

    @Override
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.isHelping(#teamId)) or (hasRole('STUDENT') and @securityServiceImpl.isPartOf(#teamId))")
    public int getActiveRAMForTeam(Long teamId) {
        return teamRepository.getActiveRamByTeam(teamId);
    }

    @Override
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.isHelping(#teamId)) or (hasRole('STUDENT') and @securityServiceImpl.isPartOf(#teamId))")
    public int getCountActiveVirtualMachinesForTeam(Long teamId) {
        return teamRepository.countVirtualMachinesByTeamAndStatus(teamId, VirtualMachineStatus.ON);
    }

    @Override
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.isHelping(#teamId)) or (hasRole('STUDENT') and @securityServiceImpl.isPartOf(#teamId))")
    public int getCountVirtualMachinesForTeam(Long teamId) {
        return teamRepository.countVirtualMachinesByTeam(teamId);
    }

    @Override
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.isHelping(#teamId)) or (hasRole('STUDENT') and @securityServiceImpl.isPartOf(#teamId))")
    public ResourcesResponse getResourcesByTeam(Long teamId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException(teamId.toString()));

        Configuration configuration = team.getConfiguration();

        if (configuration == null) {
            throw new ConfigurationNotDefinedException(teamId.toString());
        }

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

        ResourcesResponse response = ResourcesResponse.builder()
                .teamId(teamId)
                .activeNumVcpu(activeNumVcpu)
                .activeDiskSpace(activeDiskSpace)
                .activeRam(activeRam)
                .activeVMs(activeVMs)
                .tot(tot)
                .maxVcpu(configuration.getMax_vcpu())
                .maxDiskSpace(configuration.getMax_disk_space())
                .maxRam(configuration.getMax_ram())
                .maxOn(configuration.getMax_on())
                .maxTot(configuration.getTot())
                .build();

        return response;
    }

    @Override
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public List<SystemImage> getImages() {
        return Arrays.asList(SystemImage.values());
    }
}
