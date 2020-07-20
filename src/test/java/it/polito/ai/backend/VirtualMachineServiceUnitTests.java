package it.polito.ai.backend;

import it.polito.ai.backend.dtos.VirtualMachineConfigurationDTO;
import it.polito.ai.backend.dtos.VirtualMachineDTO;
import it.polito.ai.backend.entities.*;
import it.polito.ai.backend.repositories.*;
import it.polito.ai.backend.services.vm.VirtualMachineService;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.opentest4j.TestAbortedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc(addFilters = false)
class VirtualMachineServiceUnitTests {

    static List<Team> teams;
    static List<VirtualMachine> virtualMachines;
    static List<VirtualMachineConfiguration> configurations;
    static List<VirtualMachineModel> models;
    static List<Student> students;
    static List<Course> courses;

    @Autowired TeamRepository teamRepository;
    @Autowired VirtualMachineRepository virtualMachineRepository;
    @Autowired VirtualMachineConfigurationRepository virtualMachineConfigurationRepository;
    @Autowired VirtualMachineModelRepository virtualMachineModelRepository;
    @Autowired StudentRepository studentRepository;
    @Autowired CourseRepository courseRepository;
    @Autowired ModelMapper modelMapper;

    @BeforeAll
    static void beforeAll(@Autowired TeamRepository teamRepository,
                          @Autowired VirtualMachineRepository virtualMachineRepository,
                          @Autowired VirtualMachineConfigurationRepository virtualMachineConfigurationRepository,
                          @Autowired VirtualMachineModelRepository virtualMachineModelRepository,
                          @Autowired StudentRepository studentRepository,
                          @Autowired CourseRepository courseRepository) {

        if (virtualMachineRepository.count() > 0 &&
            teamRepository.count() > 0 &&
            virtualMachineConfigurationRepository.count() > 0 &&
            virtualMachineModelRepository.count() > 0 &&
            studentRepository.count() > 0 &&
            courseRepository.count() > 0)
        {

            teams = teamRepository.findAll();
            virtualMachines = virtualMachineRepository.findAll();
            configurations = virtualMachineConfigurationRepository.findAll();
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
        virtualMachineConfigurationRepository.deleteAll();
        virtualMachineRepository.deleteAll();
        teamRepository.deleteAll();
        studentRepository.deleteAll();
        courseRepository.deleteAll();

        IntStream.range(0, nCourses)
                .forEach(i -> {
                    Course course = Course.builder()
                            .id("c-"+i)
                            .name("course-" + i)
                            .min(4)
                            .max(6)
                            .enabled(true)
                            .students(new ArrayList<>())
                            .teachers(new ArrayList<>())
                            .teams(new ArrayList<>())
                            .build();
                    courses.add(course);

                    VirtualMachineModel model = VirtualMachineModel.builder()
                            .system_image(SystemImage.values()[i%SystemImage.values().length])
                            .virtualMachines(new ArrayList<>())
                            .build();
                    models.add(model);

                    course.setVirtualMachineModel(model);

                });

        IntStream.range(0, nStudents)
                .forEach(i -> {
                    String id = "s-"+i;
                    Student student = Student.builder()
                            .id(id)
                            .name("name-" + id)
                            .firstName("first_name-" + id)
                            .courses(new ArrayList<>())
                            .teams(new ArrayList<>())
                            .virtual_machines(new ArrayList<>())
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

                    VirtualMachineConfiguration configuration = VirtualMachineConfiguration.builder()
                            .tot(5 + i%4)
                            .max_on(6 + i%3)
                            .min_vcpu(2)
                            .max_vcpu(12 + i%4)
                            .min_disk_space(300)
                            .max_disk_space(3000 + (i%3)*500)
                            .min_ram(4)
                            .max_ram(24)
                            .build();
                    configurations.add(configuration);

                    team.setVirtualMachineConfiguration(configuration);

                });

        IntStream.range(0, nStudents)
                .forEach(i -> {
                    Student student = students.get(i);
                    student.getCourses().forEach(c -> {
                        int size = c.getTeams().size();
                        Team team = c.getTeams().get(i%size);
                        if (team.getMembers().size() < c.getMin()) {
                            team.addStudent(student);

                            VirtualMachineConfiguration configuration = team.getVirtualMachineConfiguration();

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

                                /*IntStream.range(0, configuration.getMax_on()-1)
                                        .forEach(k -> {
                                            team.getVirtualMachines().get(k%team.getVirtualMachines().size()).setStatus(VirtualMachineStatus.ON);
                                        });*/
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

        Team team = teams.get(0);
        Long teamId = team.getId();
        Student student = team.getMembers().get(0);
        String studentId = student.getId();
        VirtualMachineConfiguration virtualMachineConfiguration = team.getVirtualMachineConfiguration();
        VirtualMachineModel virtualMachineModel = team.getCourse().getVirtualMachineModel();
        int vcpu = virtualMachineConfiguration.getMin_vcpu();
        int diskSpace = virtualMachineConfiguration.getMin_disk_space();
        int ram = virtualMachineConfiguration.getMin_ram();

        VirtualMachineDTO virtualMachineDTO = virtualMachineService.createVirtualMachine(studentId, teamId, vcpu, diskSpace, ram);

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
        Long vmId = virtualMachines.get(0).getId();

        virtualMachineService.turnOnVirtualMachine(vmId);
        VirtualMachineStatus actual = virtualMachineRepository.getOne(vmId).getStatus();

        Assertions.assertEquals(VirtualMachineStatus.ON, actual);
    }

    @Test
    void turnOffVirtualMachine() {
        Long vmId = virtualMachines.get(0).getId();

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
    }

    @Test
    void updateVirtualMachineConfiguration() {
        Team team = teams.get(0);
        Long teamId = team.getId();

        VirtualMachineConfigurationDTO configuration = modelMapper.map(team.getVirtualMachineConfiguration(), VirtualMachineConfigurationDTO.class);

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

        VirtualMachineConfigurationDTO virtualMachineConfigurationDTO = virtualMachineService.updateVirtualMachineConfiguration(teamId, configuration);

        Assertions.assertEquals(virtualMachineConfigurationDTO.getMin_vcpu(), configuration.getMin_vcpu());
        Assertions.assertEquals(virtualMachineConfigurationDTO.getMin_disk(), configuration.getMin_disk());
        Assertions.assertEquals(virtualMachineConfigurationDTO.getMin_ram(), configuration.getMin_ram());
        Assertions.assertEquals(virtualMachineConfigurationDTO.getMax_vcpu(), configuration.getMax_vcpu());
        Assertions.assertEquals(virtualMachineConfigurationDTO.getMax_disk(), configuration.getMax_disk());
        Assertions.assertEquals(virtualMachineConfigurationDTO.getMax_ram(), configuration.getMax_ram());
        Assertions.assertEquals(virtualMachineConfigurationDTO.getMax_on(), configuration.getMax_on());
        Assertions.assertEquals(virtualMachineConfigurationDTO.getTot(), configuration.getTot());
        Assertions.assertEquals(virtualMachineConfigurationDTO.getId(), configuration.getId());

    }

    @Test
    void deleteVirtualMachineModel() {
        Course course = courses.get(0);
        virtualMachineService.deleteVirtualMachineModel(course.getId());

        Course course1 = courseRepository.getOne(course.getId());

        Assertions.assertNull(course1.getVirtualMachineModel());
        /**
         * all teams have no vm for the given virtual machine model of the course
         */
        Assertions.assertEquals(0, course1.getTeams().stream().mapToInt(t -> t.getVirtualMachines().size()).sum());
        /**
         * there is no student owning any virtual machine of the given model
         */
        Assertions.assertTrue(course1.getStudents().stream().noneMatch(s -> s.getVirtual_machines().containsAll(course.getVirtualMachineModel().getVirtualMachines())));
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
