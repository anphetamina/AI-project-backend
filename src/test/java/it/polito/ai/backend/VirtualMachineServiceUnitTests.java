package it.polito.ai.backend;

import it.polito.ai.backend.dtos.ConfigurationDTO;
import it.polito.ai.backend.dtos.VirtualMachineDTO;
import it.polito.ai.backend.entities.*;
import it.polito.ai.backend.repositories.*;
import it.polito.ai.backend.services.team.StudentNotFoundException;
import it.polito.ai.backend.services.vm.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.opentest4j.TestAbortedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest
@Transactional
/**
 * disable preAuthorize
 */
class VirtualMachineServiceUnitTests {

    static List<Team> teams;
    static List<VirtualMachine> virtualMachines;
    static List<Configuration> configurations;
    static List<VirtualMachineModel> models;
    static List<Student> students;
    static List<Course> courses;

    @Autowired TeamRepository teamRepository;
    @Autowired VirtualMachineRepository virtualMachineRepository;
    @Autowired ConfigurationRepository configurationRepository;
    @Autowired VirtualMachineModelRepository virtualMachineModelRepository;
    @Autowired StudentRepository studentRepository;
    @Autowired CourseRepository courseRepository;
    @Autowired ModelMapper modelMapper;

    @BeforeAll
    static void beforeAll(@Autowired TeamRepository teamRepository,
                          @Autowired VirtualMachineRepository virtualMachineRepository,
                          @Autowired ConfigurationRepository configurationRepository,
                          @Autowired VirtualMachineModelRepository virtualMachineModelRepository,
                          @Autowired StudentRepository studentRepository,
                          @Autowired CourseRepository courseRepository) {

        if (virtualMachineRepository.count() > 0 &&
            teamRepository.count() > 0 &&
            configurationRepository.count() > 0 &&
            virtualMachineModelRepository.count() > 0 &&
            studentRepository.count() > 0 &&
            courseRepository.count() > 0)
        {

            teams = teamRepository.findAll();
            virtualMachines = virtualMachineRepository.findAll();
            configurations = configurationRepository.findAll();
            models = virtualMachineModelRepository.findAll();
            students = studentRepository.findAll();
            courses = courseRepository.findAll();
            return;
        }


        teams = new ArrayList<>();
        virtualMachines = new ArrayList<>();
        configurations = new ArrayList<>();
        models = new ArrayList<>();
        students = new ArrayList<>();
        courses = new ArrayList<>();

        final int nCourses = 6;
        final int nStudents = 50;
        final int nTeams = 20;

        virtualMachineModelRepository.deleteAll();
        configurationRepository.deleteAll();
        virtualMachineRepository.deleteAll();
        teamRepository.deleteAll();
        studentRepository.deleteAll();
        courseRepository.deleteAll();

        IntStream.range(0, nCourses)
                .forEach(i -> {
                    Course course = Course.builder()
                            .id("c"+i)
                            .name("course-" + i)
                            .min(4)
                            .max(6)
                            .enabled(true)
                            .students(new ArrayList<>())
                            .teachers(new ArrayList<>())
                            .teams(new ArrayList<>())
                            .assignments(new ArrayList<>())
                            .build();
                    courses.add(course);

                    VirtualMachineModel model = VirtualMachineModel.builder()
                            .os(SystemImage.values()[i%SystemImage.values().length])
                            .virtualMachines(new ArrayList<>())
                            .build();
                    models.add(model);

                    String id = "d"+i;

                    Teacher teacher = Teacher.builder()
                            .id(id)
                            .email(String.format("%s@polito.it", id))
                            .firstName("first_name-" + id)
                            .lastName("last_name-" + id)
                            .courses(new ArrayList<>())
                            .build();

                    course.setVirtualMachineModel(model);

                });

        IntStream.range(0, nStudents)
                .forEach(i -> {
                    String id = "s"+i;
                    Student student = Student.builder()
                            .id(id)
                            .firstName("first_name-" + id)
                            .lastName("last_name-" + id)
                            .email(String.format("%s@studenti.polito.it", id))
                            .courses(new ArrayList<>())
                            .teams(new ArrayList<>())
                            .virtual_machines(new ArrayList<>())
                            .papers(new ArrayList<>())
                            .build();
                    students.add(student);

                    courses.forEach(student::addCourse);
                });

        IntStream.range(0, nTeams)
                .forEach(i -> {
                    Team team = Team.builder()
                            .name("team-"+i)
                            .status(TeamStatus.ACTIVE)
                            .members(new ArrayList<>())
                            .virtualMachines(new ArrayList<>())
                            .build();
                    teams.add(team);

                    team.setCourse(courses.get(i%nCourses));

                    Configuration configuration = Configuration.builder()
                            .tot(6 + i%4)
                            .max_on(1 + i%3)
                            .min_vcpu(2)
                            .max_vcpu(12 + i%4)
                            .min_disk_space(300)
                            .max_disk_space(3000 + (i%3)*500)
                            .min_ram(4)
                            .max_ram(24)
                            .build();
                    configurations.add(configuration);

                    team.setConfiguration(configuration);

                });

        IntStream.range(0, nStudents)
                .forEach(i -> {
                    Student student = students.get(i);
                    student.getCourses().forEach(c -> {
                        int size = c.getTeams().size();
                        Team team = c.getTeams().get(i%size);
                        if (team.getMembers().size() < c.getMin()) {
                            team.addStudent(student);

                            Configuration configuration = team.getConfiguration();

                            if (team.getVirtualMachines().size() == 0) {

                                IntStream.range(0, configuration.getTot()-3)
                                        .forEach(j -> {
                                            VirtualMachine virtualMachine = VirtualMachine.builder()
                                                    .num_vcpu(configuration.getMin_vcpu())
                                                    .disk_space(configuration.getMin_disk_space())
                                                    .ram(configuration.getMin_ram())
                                                    .status(VirtualMachineStatus.OFF)
                                                    .owners(new ArrayList<>())
                                                    .build();

                                            virtualMachine.addOwner(student);
                                            virtualMachine.setTeam(team);
                                            virtualMachine.setVirtualMachineModel(c.getVirtualMachineModel());
                                            virtualMachines.add(virtualMachine);
                                        });

                                IntStream.range(0, configuration.getMax_on()-1)
                                        .forEach(k -> {
                                            team.getVirtualMachines().get(k%team.getVirtualMachines().size()).setStatus(VirtualMachineStatus.ON);
                                        });
                            }

                        }
                    });
                });

        teams.forEach(teamRepository::saveAndFlush);

    }

    @Autowired
    VirtualMachineService virtualMachineService;

    @Test
    void createVirtualMachine() {

        Course course = courses.get(0);
        String courseId = course.getId();
        Team team = course.getTeams().get(0);
        Long teamId = team.getId();
        Student student = team.getMembers().get(0);
        String studentId = student.getId();
        Configuration configuration = team.getConfiguration();
        VirtualMachineModel virtualMachineModel = team.getCourse().getVirtualMachineModel();
        Long modelId = virtualMachineModel.getId();
        int vcpu = configuration.getMin_vcpu();
        int diskSpace = configuration.getMin_disk_space();
        int ram = configuration.getMin_ram();

        VirtualMachineDTO vm = VirtualMachineDTO.builder()
                .num_vcpu(vcpu)
                .disk_space(diskSpace)
                .ram(ram)
                .build();

        VirtualMachineDTO virtualMachineDTO = virtualMachineService.createVirtualMachine(studentId, teamId, modelId, vm);

        Assertions.assertTrue(virtualMachineDTO.getId() > 0);

        VirtualMachine virtualMachine = virtualMachineRepository.getOne(virtualMachineDTO.getId());
        Assertions.assertEquals(virtualMachine.getId(), virtualMachineDTO.getId());
        Assertions.assertSame(virtualMachine.getStatus(), VirtualMachineStatus.OFF);
        Assertions.assertEquals(virtualMachine.getNum_vcpu(), vcpu);
        Assertions.assertEquals(virtualMachine.getDisk_space(), diskSpace);
        Assertions.assertEquals(virtualMachine.getRam(), ram);
        Assertions.assertEquals(virtualMachine.getTeam(), team);
        Assertions.assertEquals(virtualMachine.getVirtualMachineModel(), virtualMachineModel);
        Assertions.assertTrue(virtualMachine.getOwners().contains(students.get(0)));

    }

    @Test
    void createVirtualMachine_exceedsTot() {
        Course course = courses.get(0);
        String courseId = course.getId();
        Team team = course.getTeams().get(0);
        Long teamId = team.getId();
        Student student = team.getMembers().get(0);
        String studentId = student.getId();
        Configuration configuration = team.getConfiguration();
        Long modelId = course.getVirtualMachineModel().getId();
        int vcpu = configuration.getMin_vcpu();
        int diskSpace = configuration.getMin_disk_space();
        int ram = configuration.getMin_ram();
        VirtualMachineDTO virtualMachineDTO = VirtualMachineDTO.builder()
                .num_vcpu(vcpu)
                .disk_space(diskSpace)
                .ram(ram)
                .build();
        IntStream.range(0, configuration.getTot()-team.getVirtualMachines().size())
                .forEach(i -> {
                    VirtualMachineDTO vm = VirtualMachineDTO.builder()
                            .num_vcpu(vcpu)
                            .disk_space(diskSpace)
                            .ram(ram)
                            .build();
                    virtualMachineService.createVirtualMachine(studentId, teamId, modelId, vm);
                });

        Assertions.assertThrows(VirtualMachineNumberException.class, () -> virtualMachineService.createVirtualMachine(studentId, teamId, modelId, virtualMachineDTO));
    }

    @Test
    void createVirtualMachine_configurationNotDefined() {
        Course course = courses.get(0);
        String courseId = course.getId();
        Team team = course.getTeams().get(0);
        Long teamId = team.getId();
        Configuration configuration = team.getConfiguration();
        Long modelId = course.getVirtualMachineModel().getId();
        int vcpu = configuration.getMin_vcpu();
        int diskSpace = configuration.getMin_disk_space();
        int ram = configuration.getMin_ram();
        VirtualMachineDTO vm = VirtualMachineDTO.builder()
                .num_vcpu(vcpu)
                .disk_space(diskSpace)
                .ram(ram)
                .build();

        Team team1 = teamRepository.getOne(teamId);
        team1.setConfiguration(null);
        teamRepository.save(team1);
        Student student = team.getMembers().get(0);
        String studentId = student.getId();

        Assertions.assertThrows(ConfigurationNotDefinedException.class, () -> virtualMachineService.createVirtualMachine(studentId, teamId, modelId, vm));
    }

    @Test
    void createVirtualMachine_modelNotDefined() {
        Course course = courses.get(0);
        String courseId = course.getId();
        Team team = course.getTeams().get(0);
        Long teamId = team.getId();
        Configuration configuration = team.getConfiguration();
        Long modelId = course.getVirtualMachineModel().getId();
        int vcpu = configuration.getMin_vcpu();
        int diskSpace = configuration.getMin_disk_space();
        int ram = configuration.getMin_ram();
        VirtualMachineDTO vm = VirtualMachineDTO.builder()
                .num_vcpu(vcpu)
                .disk_space(diskSpace)
                .ram(ram)
                .build();

        Course course1 = courseRepository.getOne(courseId);
        course1.setVirtualMachineModel(null);
        courseRepository.save(course1);
        Student student = team.getMembers().get(0);
        String studentId = student.getId();

        Assertions.assertThrows(VirtualMachineModelNotDefinedException.class, () -> virtualMachineService.createVirtualMachine(studentId, teamId, modelId, vm));

    }

    @Test
    void createVirtualMachine_numVcpuNotAvailable() {
        Course course = courses.get(0);
        String courseId = course.getId();
        Team team = course.getTeams().get(0);
        Long teamId = team.getId();
        Configuration configuration = team.getConfiguration();
        Long modelId = course.getVirtualMachineModel().getId();
        int vcpu = configuration.getMax_vcpu()-(team.getVirtualMachines().stream().mapToInt(VirtualMachine::getNum_vcpu).sum());
        int diskSpace = configuration.getMin_disk_space();
        int ram = configuration.getMin_ram();
        VirtualMachineDTO vm = VirtualMachineDTO.builder()
                .num_vcpu(vcpu)
                .disk_space(diskSpace)
                .ram(ram)
                .build();
        Student student = team.getMembers().get(0);
        String studentId = student.getId();

        Assertions.assertDoesNotThrow(() -> virtualMachineService.createVirtualMachine(studentId, teamId, modelId, vm));

        Assertions.assertThrows(NumVcpuNotAvailableException.class, () -> virtualMachineService.createVirtualMachine(studentId, teamId, modelId, vm));
    }

    @Test
    void createVirtualMachine_diskSpaceNotAvailable() {
        Course course = courses.get(0);
        String courseId = course.getId();
        Team team = course.getTeams().get(0);
        Long teamId = team.getId();
        Configuration configuration = team.getConfiguration();
        Long modelId = course.getVirtualMachineModel().getId();
        int vcpu = configuration.getMin_vcpu();
        int diskSpace = configuration.getMax_disk_space()-(team.getVirtualMachines().stream().mapToInt(VirtualMachine::getDisk_space).sum());
        int ram = configuration.getMin_ram();
        VirtualMachineDTO vm = VirtualMachineDTO.builder()
                .num_vcpu(vcpu)
                .disk_space(diskSpace)
                .ram(ram)
                .build();
        Student student = team.getMembers().get(0);
        String studentId = student.getId();

        Assertions.assertDoesNotThrow(() -> virtualMachineService.createVirtualMachine(studentId, teamId, modelId, vm));

        Assertions.assertThrows(DiskSpaceNotAvailableException.class, () -> virtualMachineService.createVirtualMachine(studentId, teamId, modelId, vm));

    }

    @Test
    void createVirtualMachine_ramNotAvailable() {
        Course course = courses.get(0);
        String courseId = course.getId();
        Team team = course.getTeams().get(0);
        Long teamId = team.getId();
        Configuration configuration = team.getConfiguration();
        Long modelId = course.getVirtualMachineModel().getId();
        int vcpu = configuration.getMin_vcpu();
        int diskSpace = configuration.getMin_disk_space();
        int ram = (configuration.getMax_ram()-(team.getVirtualMachines().stream().mapToInt(VirtualMachine::getRam).sum()))+1;
        VirtualMachineDTO vm = VirtualMachineDTO.builder()
                .num_vcpu(vcpu)
                .disk_space(diskSpace)
                .ram(ram)
                .build();
        Student student = team.getMembers().get(0);
        String studentId = student.getId();

        Assertions.assertThrows(RamNotAvailableException.class, () -> virtualMachineService.createVirtualMachine(studentId, teamId, modelId, vm));

    }

    @Test
    void updateVirtualMachine() {

        VirtualMachine virtualMachine = virtualMachines.get(0);

        VirtualMachineDTO virtualMachineDTO = VirtualMachineDTO.builder()
                .id(virtualMachine.getId())
                .num_vcpu(virtualMachine.getNum_vcpu()+1)
                .disk_space(virtualMachine.getDisk_space()+1)
                .ram(virtualMachine.getRam()+1)
                .build();

        VirtualMachineDTO updated = virtualMachineService.updateVirtualMachine(virtualMachine.getId(), virtualMachineDTO);

        Assertions.assertEquals(virtualMachineDTO.getNum_vcpu(), updated.getNum_vcpu());
        Assertions.assertEquals(virtualMachineDTO.getDisk_space(), updated.getDisk_space());
        Assertions.assertEquals(virtualMachineDTO.getRam(), updated.getRam());
        Assertions.assertEquals(virtualMachineDTO.getId(), updated.getId());
    }

    @Test
    void updateVirtualMachine_numVcpuNotAvailable() {
        VirtualMachine virtualMachine = virtualMachines.get(0);

        VirtualMachineDTO virtualMachineDTO = VirtualMachineDTO.builder()
                .id(virtualMachine.getId())
                .num_vcpu(virtualMachine.getTeam().getConfiguration().getMax_vcpu())
                .disk_space(virtualMachine.getDisk_space())
                .ram(virtualMachine.getRam())
                .build();

        Assertions.assertThrows(NumVcpuNotAvailableException.class, () -> virtualMachineService.updateVirtualMachine(virtualMachine.getId(), virtualMachineDTO));
    }

    @Test
    void updateVirtualMachine_diskSpaceNotAvailable() {
        VirtualMachine virtualMachine = virtualMachines.get(0);
        VirtualMachineDTO virtualMachineDTO = VirtualMachineDTO.builder()
                .id(virtualMachine.getId())
                .num_vcpu(virtualMachine.getNum_vcpu())
                .disk_space(virtualMachine.getTeam().getConfiguration().getMax_disk_space())
                .ram(virtualMachine.getRam())
                .build();

        Assertions.assertThrows(DiskSpaceNotAvailableException.class, () -> virtualMachineService.updateVirtualMachine(virtualMachine.getId(), virtualMachineDTO));
    }

    @Test
    void updateVirtualMachine_ramNotAvailable() {
        VirtualMachine virtualMachine = virtualMachines.get(0);

        VirtualMachineDTO virtualMachineDTO = VirtualMachineDTO.builder()
                .id(virtualMachine.getId())
                .num_vcpu(virtualMachine.getNum_vcpu())
                .disk_space(virtualMachine.getDisk_space())
                .ram(virtualMachine.getTeam().getConfiguration().getMax_ram())
                .build();

        Assertions.assertThrows(RamNotAvailableException.class, () -> virtualMachineService.updateVirtualMachine(virtualMachine.getId(), virtualMachineDTO));
    }

    @Test
    void deleteVirtualMachine() {
        Team team = teams.get(0);
        VirtualMachine virtualMachine = team.getVirtualMachines().get(0);

        virtualMachineService.deleteVirtualMachine(virtualMachine.getId());
        Team team1 = teamRepository.getOne(team.getId());

        Assertions.assertFalse(team1.getVirtualMachines().contains(virtualMachine));
        List<Student> owners = virtualMachine.getOwners().stream().map(s -> studentRepository.getOne(s.getId())).collect(Collectors.toList());
        Assertions.assertTrue(owners.stream().noneMatch(o -> o.getVirtual_machines().contains(virtualMachine)));
        Assertions.assertEquals(team.getCourse().getVirtualMachineModel().getVirtualMachines().size()-1, team1.getCourse().getVirtualMachineModel().getVirtualMachines().size());
        Assertions.assertFalse(team1.getCourse().getVirtualMachineModel().getVirtualMachines().contains(virtualMachine));
        Assertions.assertTrue(team1.getCourse().getVirtualMachineModel().getVirtualMachines().stream().noneMatch(vm -> vm.getId().equals(virtualMachine.getId())));
    }

    @Test
    void turnOnVirtualMachine() {
        VirtualMachine virtualMachine = virtualMachines.get(0);
        Long vmId = virtualMachine.getId();

        virtualMachineService.turnOnVirtualMachine(vmId);
        VirtualMachineStatus actual = virtualMachineRepository.getOne(vmId).getStatus();

        Assertions.assertEquals(VirtualMachineStatus.ON, actual);
    }

    @Test
    void turnOnVirtualMachine_exceedsMaxOn() {

        Team team = teams.get(0);
        Team team1 = teamRepository.getOne(team.getId());
        Configuration configuration = team.getConfiguration();
        IntStream.range(0, team.getConfiguration().getMax_on())
                .forEach(i -> {
                    VirtualMachine virtualMachine = VirtualMachine.builder()
                            .num_vcpu(configuration.getMin_vcpu())
                            .disk_space(configuration.getMin_disk_space())
                            .ram(configuration.getMin_ram())
                            .status(VirtualMachineStatus.ON)
                            .build();
                    team1.addVirtualMachine(virtualMachine);
                });
        teamRepository.save(team1);

        Long vmId = team.getVirtualMachines().stream().filter(vm -> vm.getStatus() == VirtualMachineStatus.OFF).findFirst().orElseThrow(() -> new TestAbortedException("no turned off vm for the team")).getId();

        Assertions.assertThrows(ActiveVirtualMachineNumberException.class, () -> virtualMachineService.turnOnVirtualMachine(vmId));
    }

    @Test
    void turnOffVirtualMachine() {
        VirtualMachine virtualMachine = virtualMachines.get(0);
        Long vmId = virtualMachine.getId();

        virtualMachineService.turnOffVirtualMachine(vmId);
        VirtualMachineStatus actual = virtualMachineRepository.getOne(vmId).getStatus();

        Assertions.assertEquals(VirtualMachineStatus.OFF, actual);
    }

    @Test
    void addOwnerToVirtualMachine() {
        Team team = teams.get(0);
        VirtualMachine virtualMachine = team.getVirtualMachines().get(0);
        Student student = team.getMembers().stream().filter(m -> !virtualMachine.getOwners().contains(m)).findFirst().orElseThrow(() -> new TestAbortedException("all students own the first vm"));
        Long vmId = virtualMachine.getId();

        virtualMachineService.addOwnerToVirtualMachine(student.getId(), vmId);

        Assertions.assertTrue(virtualMachineRepository.getOne(vmId).getOwners().contains(student));
        Assertions.assertFalse(virtualMachineService.addOwnerToVirtualMachine(student.getId(), vmId));
    }

    @Test
    void addOwnerToVirtualMachine_invalidParameters() {
        Team team = teams.get(0);
        VirtualMachine virtualMachine = team.getVirtualMachines().get(0);
        Student student = team.getMembers().stream().filter(m -> !virtualMachine.getOwners().contains(m)).findFirst().orElseThrow(() -> new TestAbortedException("all students own the first vm"));
        Long vmId = virtualMachine.getId();

        Assertions.assertThrows(StudentNotFoundException.class, () -> virtualMachineService.addOwnerToVirtualMachine("  ", vmId));
        // Assertions.assertThrows(ConstraintViolationException.class, () -> virtualMachineService.addOwnerToVirtualMachine(null, vmId));
    }

    @Test
    void updateVirtualMachineConfiguration() {
        Team team = teams.get(0);
        Long teamId = team.getId();
        Long configurationId = team.getConfiguration().getId();
        ConfigurationDTO configuration = modelMapper.map(team.getConfiguration(), ConfigurationDTO.class);

        int minVcpu = team.getVirtualMachines().stream().mapToInt(VirtualMachine::getNum_vcpu).min().orElseThrow(NoSuchElementException::new);
        int minDiskSpace = team.getVirtualMachines().stream().mapToInt(VirtualMachine::getDisk_space).min().orElseThrow(NoSuchElementException::new);
        int minRam = team.getVirtualMachines().stream().mapToInt(VirtualMachine::getRam).min().orElseThrow(NoSuchElementException::new);

        configuration.setMin_vcpu(minVcpu-1);
        configuration.setMin_disk(minDiskSpace-1);
        configuration.setMin_ram(minRam-1);
        configuration.setMax_vcpu(configuration.getMax_vcpu()+1);
        configuration.setMax_disk(configuration.getMax_disk()+1);
        configuration.setMax_ram(configuration.getMax_ram()+1);
        configuration.setMax_on(configuration.getMax_on()+1);
        configuration.setTot(configuration.getTot()+1);

        ConfigurationDTO configurationDTO = virtualMachineService.updateConfiguration(configurationId, configuration);

        Assertions.assertEquals(configurationDTO.getMin_vcpu(), configuration.getMin_vcpu());
        Assertions.assertEquals(configurationDTO.getMin_disk(), configuration.getMin_disk());
        Assertions.assertEquals(configurationDTO.getMin_ram(), configuration.getMin_ram());
        Assertions.assertEquals(configurationDTO.getMax_vcpu(), configuration.getMax_vcpu());
        Assertions.assertEquals(configurationDTO.getMax_disk(), configuration.getMax_disk());
        Assertions.assertEquals(configurationDTO.getMax_ram(), configuration.getMax_ram());
        Assertions.assertEquals(configurationDTO.getMax_on(), configuration.getMax_on());
        Assertions.assertEquals(configurationDTO.getTot(), configuration.getTot());
        Assertions.assertEquals(configurationDTO.getId(), configuration.getId());

    }

    @Test
    void updateVirtualMachineConfiguration_invalidNumVcpu() {
        Team team = teams.get(0);
        Long teamId = team.getId();
        Long configurationId = team.getConfiguration().getId();
        ConfigurationDTO configuration = modelMapper.map(team.getConfiguration(), ConfigurationDTO.class);

        int minVcpu = team.getVirtualMachines().stream().mapToInt(VirtualMachine::getNum_vcpu).min().orElseThrow(NoSuchElementException::new);
        int minDiskSpace = team.getVirtualMachines().stream().mapToInt(VirtualMachine::getDisk_space).min().orElseThrow(NoSuchElementException::new);
        int minRam = team.getVirtualMachines().stream().mapToInt(VirtualMachine::getRam).min().orElseThrow(NoSuchElementException::new);

        configuration.setMin_vcpu(minVcpu+1);
        configuration.setMin_disk(minDiskSpace-1);
        configuration.setMin_ram(minRam-1);
        configuration.setMax_vcpu(configuration.getMax_vcpu()+1);
        configuration.setMax_disk(configuration.getMax_disk()+1);
        configuration.setMax_ram(configuration.getMax_ram()+1);
        configuration.setMax_on(configuration.getMax_on()+1);
        configuration.setTot(configuration.getTot()+1);

        Assertions.assertThrows(InvalidConfigurationException.class, () -> virtualMachineService.updateConfiguration(configurationId, configuration));

        int totNumVcpu = team.getVirtualMachines().stream().mapToInt(VirtualMachine::getNum_vcpu).sum();
        configuration.setMax_vcpu(totNumVcpu-1);
        Assertions.assertThrows(InvalidConfigurationException.class, () -> virtualMachineService.updateConfiguration(configurationId, configuration));

    }

    @Test
    void updateVirtualMachineConfiguration_invalidDiskSpace() {
        Team team = teams.get(0);
        Long teamId = team.getId();
        Long configurationId = team.getConfiguration().getId();
        ConfigurationDTO configuration = modelMapper.map(team.getConfiguration(), ConfigurationDTO.class);

        int minVcpu = team.getVirtualMachines().stream().mapToInt(VirtualMachine::getNum_vcpu).min().orElseThrow(NoSuchElementException::new);
        int minDiskSpace = team.getVirtualMachines().stream().mapToInt(VirtualMachine::getDisk_space).min().orElseThrow(NoSuchElementException::new);
        int minRam = team.getVirtualMachines().stream().mapToInt(VirtualMachine::getRam).min().orElseThrow(NoSuchElementException::new);

        configuration.setMin_vcpu(minVcpu-1);
        configuration.setMin_disk(minDiskSpace+1);
        configuration.setMin_ram(minRam-1);
        configuration.setMax_vcpu(configuration.getMax_vcpu()+1);
        configuration.setMax_disk(configuration.getMax_disk()+1);
        configuration.setMax_ram(configuration.getMax_ram()+1);
        configuration.setMax_on(configuration.getMax_on()+1);
        configuration.setTot(configuration.getTot()+1);

        Assertions.assertThrows(InvalidConfigurationException.class, () -> virtualMachineService.updateConfiguration(configurationId, configuration));

        int totDiskSpace = team.getVirtualMachines().stream().mapToInt(VirtualMachine::getDisk_space).sum();
        configuration.setMax_vcpu(totDiskSpace-1);
        Assertions.assertThrows(InvalidConfigurationException.class, () -> virtualMachineService.updateConfiguration(configurationId, configuration));

    }

    @Test
    void updateVirtualMachineConfiguration_invalidRam() {
        Team team = teams.get(0);
        Long teamId = team.getId();
        Long configurationId = team.getConfiguration().getId();
        ConfigurationDTO configuration = modelMapper.map(team.getConfiguration(), ConfigurationDTO.class);

        int minVcpu = team.getVirtualMachines().stream().mapToInt(VirtualMachine::getNum_vcpu).min().orElseThrow(NoSuchElementException::new);
        int minDiskSpace = team.getVirtualMachines().stream().mapToInt(VirtualMachine::getDisk_space).min().orElseThrow(NoSuchElementException::new);
        int minRam = team.getVirtualMachines().stream().mapToInt(VirtualMachine::getRam).min().orElseThrow(NoSuchElementException::new);

        configuration.setMin_vcpu(minVcpu-1);
        configuration.setMin_disk(minDiskSpace-1);
        configuration.setMin_ram(minRam+1);
        configuration.setMax_vcpu(configuration.getMax_vcpu()+1);
        configuration.setMax_disk(configuration.getMax_disk()+1);
        configuration.setMax_ram(configuration.getMax_ram()+1);
        configuration.setMax_on(configuration.getMax_on()+1);
        configuration.setTot(configuration.getTot()+1);

        Assertions.assertThrows(InvalidConfigurationException.class, () -> virtualMachineService.updateConfiguration(configurationId, configuration));

        int totRam = team.getVirtualMachines().stream().mapToInt(VirtualMachine::getRam).sum();
        configuration.setMax_vcpu(totRam-1);
        Assertions.assertThrows(InvalidConfigurationException.class, () -> virtualMachineService.updateConfiguration(configurationId, configuration));

    }

    @Test
    void deleteVirtualMachineModel() {
        Course course = courses.get(0);
        Long modelId = course.getVirtualMachineModel().getId();
        virtualMachineService.deleteVirtualMachineModel(modelId);

        Course course1 = courseRepository.getOne(course.getId());

        Assertions.assertNull(course1.getVirtualMachineModel());
        Assertions.assertEquals(0, course1.getTeams().stream().mapToInt(t -> t.getVirtualMachines().size()).sum());
        Assertions.assertTrue(course1.getStudents().stream().noneMatch(s -> s.getVirtual_machines().containsAll(course.getVirtualMachineModel().getVirtualMachines())));
    }

    @Test
    void getVirtualMachine_notFound() {
        Course course = courses.get(0);
        Long teamId = course.getTeams().get(0).getId();

        Assertions.assertEquals(Optional.empty(), virtualMachineService.getVirtualMachine(999L));
    }

    @Test
    void getActiveVcpuForTeam() {
        Team team = teams.get(0);
        long activeVcpu = team.getVirtualMachines()
                .stream()
                .filter(vm -> vm.getStatus() == VirtualMachineStatus.ON)
                .reduce(0, (partial, current) -> partial + current.getNum_vcpu(), Integer::sum);

        Assertions.assertEquals(activeVcpu, virtualMachineService.getActiveVcpuForTeam(team.getId()));

    }

    @Test
    void getCountActiveVMsForTeam() {
        Team team = teams.get(0);
        long activeVMs = team.getVirtualMachines()
                .stream()
                .filter(vm -> vm.getStatus() == VirtualMachineStatus.ON)
                .count();

        Assertions.assertEquals(activeVMs, virtualMachineService.getCountActiveVirtualMachinesForTeam(team.getId()));
    }

    @Test
    void getCountVMsForTeam() {
        Team team = teams.get(0);
        long count = team.getVirtualMachines().size();

        Assertions.assertEquals(count, virtualMachineService.getCountVirtualMachinesForTeam(team.getId()));
    }

    /*@Test
    void contextLoad() {

    }*/

}
