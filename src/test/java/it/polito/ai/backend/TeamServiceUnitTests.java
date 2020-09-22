package it.polito.ai.backend;

import it.polito.ai.backend.dtos.CourseDTO;
import it.polito.ai.backend.entities.*;
import it.polito.ai.backend.repositories.*;
import it.polito.ai.backend.services.team.DuplicateIdException;
import it.polito.ai.backend.services.team.TeamService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SpringBootTest
@Transactional
@ActiveProfiles("dev")
public class TeamServiceUnitTests {

    @Autowired
    TeamService teamService;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    TeacherRepository teacherRepository;
    @Autowired
    StudentRepository studentRepository;
    @Autowired
    AssignmentRepository assignmentRepository;
    @Autowired
    VirtualMachineModelRepository virtualMachineModelRepository;
    @Autowired
    VirtualMachineRepository virtualMachineRepository;
    @Autowired
    PaperRepository paperRepository;

    @Test
    void updateCourse() {

        String teacherId = courseRepository.getOne("c5").getTeachers().get(0).getId();

        CourseDTO courseDTO = CourseDTO.builder()
                .id("c20")
                .enabled(true)
                .min(1)
                .max(20)
                .name("new_name")
                .teacherId(teacherId)
                .build();

        CourseDTO updatedCourse = teamService.updateCourse("c5", courseDTO);

        Assertions.assertEquals(updatedCourse.getId(), courseDTO.getId());
        Assertions.assertEquals(updatedCourse.getMin(), courseDTO.getMin());
        Assertions.assertEquals(updatedCourse.getMax(), courseDTO.getMax());
        Assertions.assertEquals(updatedCourse.getName(), courseDTO.getName());
        Assertions.assertEquals(updatedCourse.getEnabled(), courseDTO.getEnabled());
    }

    @Test
    void updateCourse_onlyName() {
        Course course = courseRepository.getOne("c5");
        String oldName = course.getName();

        CourseDTO courseDTO = CourseDTO.builder()
                .id(course.getId())
                .enabled(course.isEnabled())
                .min(course.getMin())
                .max(course.getMax())
                .name("new_name")
                .teacherId(course.getTeachers().get(0).getId())
                .build();

        CourseDTO updatedCourse = teamService.updateCourse("c5", courseDTO);

        Assertions.assertEquals(updatedCourse.getId(), courseDTO.getId());
        Assertions.assertEquals(updatedCourse.getMin(), courseDTO.getMin());
        Assertions.assertEquals(updatedCourse.getMax(), courseDTO.getMax());
        Assertions.assertEquals(updatedCourse.getName(), courseDTO.getName());
        Assertions.assertEquals(updatedCourse.getEnabled(), courseDTO.getEnabled());
        Assertions.assertFalse(oldName.equalsIgnoreCase(updatedCourse.getName()));
    }

    @Test
    void updateCourse_duplicatedId() {
        Course course = courseRepository.getOne("c5");

        CourseDTO courseDTO = CourseDTO.builder()
                .id("c0")
                .enabled(course.isEnabled())
                .min(course.getMin())
                .max(course.getMax())
                .name("new_name")
                .teacherId(course.getTeachers().get(0).getId())
                .build();

        Assertions.assertThrows(DuplicateIdException.class, () -> teamService.updateCourse("c5", courseDTO));
    }

    @Test
    void deleteCourse() {
        Course course = courseRepository.getOne("c5");

        VirtualMachineModel virtualMachineModel = course.getVirtualMachineModel();
        List<Student> students = course.getStudents();
        List<Team> teams = course.getTeams();
        List<Teacher> teachers = course.getTeachers();
        List<Assignment> assignments = course.getAssignments();
        List<VirtualMachine> virtualMachines = teams.stream().flatMap(t -> t.getVirtualMachines().stream()).collect(Collectors.toList());
        List<Paper> papers = assignments.stream().flatMap(a -> a.getPapers().stream()).collect(Collectors.toList());

        teamService.deleteCourse(course.getId());

        Assertions.assertEquals(Optional.empty(), courseRepository.findById(course.getId()));

        Assertions.assertTrue(students.stream().noneMatch(s -> s.getCourses().contains(course)));
        Assertions.assertTrue(teachers.stream().noneMatch(t -> t.getCourses().contains(course)));

        Assertions.assertTrue(teams.stream().noneMatch(t -> teamRepository.existsById(t.getId())));
        Assertions.assertTrue(assignments.stream().noneMatch(a -> assignmentRepository.existsById(a.getId())));
        Assertions.assertTrue(virtualMachines.stream().noneMatch(v -> virtualMachineRepository.existsById(v.getId())));
        Assertions.assertTrue(papers.stream().noneMatch(p -> paperRepository.existsById(p.getId())));
        Assertions.assertFalse(virtualMachineModelRepository.existsById(virtualMachineModel.getId()));
    }
}
