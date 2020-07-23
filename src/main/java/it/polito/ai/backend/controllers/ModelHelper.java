package it.polito.ai.backend.controllers;

import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.services.team.TeamService;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.List;


public class ModelHelper {

    public static CourseDTO enrich(CourseDTO courseDTO) {
        Link selfLink = WebMvcLinkBuilder.linkTo(CourseController.class).slash(courseDTO.getId()).withSelfRel();
        Link studentsLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).enrolledStudents(courseDTO.getId())).withRel("enrolled");
        Link teachersLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getTeachers(courseDTO.getId())).withRel("taughtBy");
        Link teamsLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getTeams(courseDTO.getId())).withRel("registers");
        Link exerciseLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getExercises(courseDTO.getId())).withRel("exercises");
        Link modelLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getVirtualMachineModel(courseDTO.getName())).withRel("virtualMachinesModel");
        return courseDTO.add(selfLink).add(studentsLink).add(teachersLink).add(teamsLink).add(modelLink).add(exerciseLink);
    }

    public static StudentDTO enrich(StudentDTO studentDTO) {
        Link selfLink = WebMvcLinkBuilder.linkTo(StudentController.class).slash(studentDTO.getId()).withSelfRel();
        Link coursesLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getCourses(studentDTO.getId())).withRel("enrolledTo");
        Link teamsLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getTeams(studentDTO.getId())).withRel("partOf");
        Link assignmentsLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getAssignments(studentDTO.getId())).withRel("assignments");
        return studentDTO.add(selfLink).add(coursesLink).add(teamsLink).add(assignmentsLink);
    }

    public static TeamDTO enrich(TeamDTO teamDTO, String courseId) {
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getTeam(courseId, teamDTO.getId())).withSelfRel();
        Link courseLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getOne(courseId)).withRel("course");
        Link membersLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getMembers(courseId, teamDTO.getId())).withRel("composedOf");
        Link configurationLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getConfiguration(courseId, teamDTO.getId())).withRel("virtualMachineConfiguration");
        Link virtualMachinesLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getVirtualMachines(courseId, teamDTO.getId())).withRel("virtualMachines");
        return teamDTO.add(selfLink).add(courseLink).add(membersLink).add(configurationLink).add(virtualMachinesLink);
    }

    public static TokenDTO enrich(TokenDTO tokenDTO, String op) {
        Link opLink = WebMvcLinkBuilder.linkTo(NotificationController.class).slash("/"+op+"/"+tokenDTO.getId()).withRel(op);
        return tokenDTO.add(opLink);
    }

    public static TeacherDTO enrich(TeacherDTO teacherDTO) {
        Link selfLink = WebMvcLinkBuilder.linkTo(TeacherController.class).slash(teacherDTO.getId()).withSelfRel();
        Link coursesLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TeacherController.class).getCourses(teacherDTO.getId())).withRel("teaches");
        return teacherDTO.add(selfLink).add(coursesLink);
    }
    public static ExerciseDTO enrich(ExerciseDTO exerciseDTO, String courseId){
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getExercises(courseId)).withSelfRel();
        Link courseLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getOne(courseId)).withRel("course");
        exerciseDTO.add(selfLink).add(courseLink);
        return exerciseDTO;
    }

    public static AssignmentDTO enrich(AssignmentDTO assignmentDTO, String studentId, Long exerciseId, String courseId){
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getAssignment(assignmentDTO.getId(),courseId,exerciseId)).withSelfRel();
        Link exerciseLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getExercise(courseId,exerciseId)).withRel("exercise");
        Link studentLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getOne(studentId)).withRel("student");
        assignmentDTO.add(selfLink).add(exerciseLink).add(studentLink);
        return  assignmentDTO;
    }

    public static VirtualMachineDTO enrich(VirtualMachineDTO virtualMachineDTO, String courseId, Long teamId) {
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getVirtualMachine(courseId, teamId, virtualMachineDTO.getId())).withSelfRel();
        Link modelLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getVirtualMachineModel(courseId)).withRel("model");
        Link teamLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getTeam(courseId, teamId)).withRel("usedBy");
        Link ownersLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getOwners(courseId, teamId, virtualMachineDTO.getId())).withRel("ownedBy");
        return virtualMachineDTO.add(selfLink).add(modelLink).add(teamLink).add(ownersLink);
    }

    public static ConfigurationDTO enrich(ConfigurationDTO configurationDTO, String courseId, Long teamId) {
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getConfiguration(courseId, teamId)).withSelfRel();
        Link teamLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getTeam(courseId, teamId)).withRel("definedFor");
        return configurationDTO.add(selfLink).add(teamLink);
    }

    public static VirtualMachineModelDTO enrich(VirtualMachineModelDTO virtualMachineModelDTO, String courseId) {
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getVirtualMachineModel(courseId)).withSelfRel();
        Link courseLink = WebMvcLinkBuilder.linkTo(CourseController.class).slash(courseId).withRel("definedFor");
        return virtualMachineModelDTO.add(selfLink).add(courseLink);
    }
}
