package it.polito.ai.backend;

import it.polito.ai.backend.entities.*;
import it.polito.ai.backend.repositories.*;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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

    @BeforeAll
    static void beforeAll(@Autowired TeamRepository teamRepository,
                          @Autowired VirtualMachineRepository virtualMachineRepository,
                          @Autowired VirtualMachineConfigurationRepository virtualMachineConfigurationRepository,
                          @Autowired VirtualMachineModelRepository virtualMachineModelRepository,
                          @Autowired StudentRepository studentRepository,
                          @Autowired CourseRepository courseRepository) {

        teams = new ArrayList<>();
        virtualMachines = new ArrayList<>();
        configurations = new ArrayList<>();
        models = new ArrayList<>();
        students = new ArrayList<>();
        courses = new ArrayList<>();

        final int nCourses = 6;
        final int nStudents = 50;
        final int nTeams = 20;
        final int nVirtualMachines = nStudents*2;
        final int nConfigurations = nTeams;
        final int nModels = nCourses;

        virtualMachineModelRepository.deleteAll();
        virtualMachineConfigurationRepository.deleteAll();
        virtualMachineRepository.deleteAll();
        teamRepository.deleteAll();
        studentRepository.deleteAll();
        courseRepository.deleteAll();

        IntStream.range(0, nCourses)
                .forEach(i -> {
                    Course course = Course.builder()
                            .name("course-" + i)
                            .min(2)
                            .max(4)
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

                    courseRepository.save(course);
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

                    /**
                     * adding each student to all courses
                     */
                    courses.forEach(student::addCourse);

                    studentRepository.save(student);
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

                    VirtualMachineConfiguration configuration = VirtualMachineConfiguration.builder()
                            .tot(5 + i%4)
                            .max_on(6 + i%3)
                            .min_vcpu(2)
                            .max_vcpu(6 + i%4)
                            .min_disk_space(300)
                            .max_disk_space(1000 + (i%3)*500)
                            .min_ram(4)
                            .max_ram(8)
                            .build();
                    configurations.add(configuration);

                    team.setVirtualMachineConfiguration(configuration);


                    IntStream.range(0, configuration.getTot()-2)
                            .forEach(j -> {
                                VirtualMachine virtualMachine = VirtualMachine.builder()
                                        .num_vcpu(configuration.getMin_vcpu())
                                        .disk_space(configuration.getMin_disk_space())
                                        .ram(configuration.getMin_ram())
                                        .status(VirtualMachineStatus.OFF)
                                        .owners(new ArrayList<>())
                                        .build();

                                virtualMachine.setTeam(team);
                                virtualMachines.add(virtualMachine);
                            });

                    teamRepository.save(team);

                });

    }

    @Test
    void contextLoads() {
    }

}
