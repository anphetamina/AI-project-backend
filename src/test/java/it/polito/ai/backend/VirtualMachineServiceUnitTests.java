package it.polito.ai.backend;

import it.polito.ai.backend.entities.*;
import it.polito.ai.backend.repositories.*;
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
@AutoConfigureMockMvc/*(addFilters = false)*/
class VirtualMachineServiceUnitTests {

    static List<Team> teams;
    static List<VirtualMachine> virtualMachines;
    static List<VirtualMachineConfiguration> configurations;
    static List<VirtualMachineModel> models;
    static List<Student> students;

    @BeforeAll
    static void beforeAll(@Autowired TeamRepository teamRepository,
                          @Autowired VirtualMachineRepository virtualMachineRepository,
                          @Autowired VirtualMachineConfigurationRepository virtualMachineConfigurationRepository,
                          @Autowired VirtualMachineModelRepository virtualMachineModelRepository,
                          @Autowired StudentRepository studentRepository) {

        teams = new ArrayList<>();
        virtualMachines = new ArrayList<>();
        configurations = new ArrayList<>();
        models = new ArrayList<>();
        students = new ArrayList<>();

        final int nTeams = 20;

        /*try {
            if (teamRepository.count() == 0) {

                IntStream.range(0, nTeams)
                        .forEach(i -> {
                            VirtualMachineConfiguration configuration = VirtualMachineConfiguration.builder()
                                    .tot(5 + (i/50)%5)
                                    .max_on(3 + (i/25)%5)
                                    .min_vcpu(2)
                                    .max_vcpu(6)
                                    .min_disk_space(300)
                                    .max_disk_space(500 + (i/50)*500)
                                    .min_ram(4)
                                    .max_ram(4 + ((i/50)%2)*4)
                                    .build();
                            configurations.add(configuration);

                            Team team = Team.builder()
                                    .name("team-"+i)
                                    .status(TeamStatus.ACTIVE)
                                    .members(new ArrayList<>())
                                    .build();
                            team.setVirtualMachineConfiguration(configuration);
                            teams.add(team);

                            IntStream.range(0, 3)
                                    .forEach(j -> {
                                        VirtualMachineModel model = VirtualMachineModel.builder()
                                                .system_image(SystemImage.values()[j%SystemImage.values().length])
                                                .virtual_machines(new ArrayList<>())
                                                .build();
                                        model.setTeam(team);
                                        models.add(model);

                                        virtualMachineModelRepository.save(model);

                                    });

                            IntStream.range(0, configuration.getTot()-2)
                                    .forEach(k -> {
                                        VirtualMachine virtualMachine = VirtualMachine.builder()
                                                .num_vcpu(configuration.getMin_vcpu())
                                                .disk_space(configuration.getMin_disk_space())
                                                .ram(configuration.getMin_ram())
                                                .status(VirtualMachineStatus.OFF)
                                                .owners(new ArrayList<>())
                                                .build();

                                        virtualMachine.setVirtualMachineModel(team.getVm_models().get(k%team.getVm_models().size()));
                                        virtualMachines.add(virtualMachine);

                                        virtualMachineRepository.save(virtualMachine);

                                        String id = "s-"+i+"-"+k;
                                        Student student = Student.builder()
                                                .id(id)
                                                .name("name-" + id)
                                                .firstName("first_name-" + id)
                                                .teams(new ArrayList<>())
                                                .virtual_machines(new ArrayList<>())
                                                .build();
                                        student.addTeam(team);
                                        student.addVirtualMachine(virtualMachine);
                                        students.add(student);
                                    });
                            teamRepository.save(team);
                        });

            } else {
                teams = teamRepository.findAll();
                configurations = virtualMachineConfigurationRepository.findAll();
                models = virtualMachineModelRepository.findAll();
                virtualMachines = virtualMachineRepository.findAll();
                students = studentRepository.findAll();
            }

        } catch (Exception e) {
            e.printStackTrace();
            teamRepository.deleteAll();
            virtualMachineConfigurationRepository.deleteAll();
            virtualMachineModelRepository.deleteAll();
            virtualMachineRepository.deleteAll();
            studentRepository.deleteAll();
        }*/

        IntStream.range(0, nTeams)
                .forEach(i -> {
                    Team team = Team.builder()
                            .name("team-"+i)
                            .status(TeamStatus.ACTIVE)
                            .members(new ArrayList<>())
                            .build();
                    teams.add(team);

                    teamRepository.save(team);

                    IntStream.range(0, 3)
                            .forEach(k -> {
                                String id = "s-"+i+"-"+k;
                                Student student = Student.builder()
                                        .id(id)
                                        .name("name-" + id)
                                        .firstName("first_name-" + id)
                                        .teams(new ArrayList<>())
                                        .virtual_machines(new ArrayList<>())
                                        .build();
                                student.addTeam(team);
                                // team.addStudent(student);
                                students.add(student);
                                studentRepository.save(student);
                            });
                });

    }

    @Test
    void contextLoads() {
    }

}
