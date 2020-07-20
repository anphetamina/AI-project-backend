package it.polito.ai.backend.controllers;

import it.polito.ai.backend.dtos.*;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;


public class ModelHelper {

    public static CourseDTO enrich(CourseDTO courseDTO) {
        Link selfLink = WebMvcLinkBuilder.linkTo(CourseController.class).slash(courseDTO.getId()).withSelfRel();
        Link studentsLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).enrolledStudents(courseDTO.getId())).withRel("enrolled");
        Link teachersLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getTeachers(courseDTO.getId())).withRel("taughtBy");
        Link teamsLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getTeams(courseDTO.getId())).withRel("registers");
        Link exerciseLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getExercises(courseDTO.getId())).withRel("exercises");
        return courseDTO.add(selfLink).add(studentsLink).add(teachersLink).add(teamsLink).add(exerciseLink);
    }

    public static StudentDTO enrich(StudentDTO studentDTO) {
        Link selfLink = WebMvcLinkBuilder.linkTo(StudentController.class).slash(studentDTO.getId()).withSelfRel();
        Link coursesLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getCourses(studentDTO.getId())).withRel("enrolledTo");
        Link teamsLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getTeams(studentDTO.getId())).withRel("partOf");
        Link assignmentsLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getAssignments(studentDTO.getId())).withRel("assignments");
        return studentDTO.add(selfLink).add(coursesLink).add(teamsLink).add(assignmentsLink);
    }

    public static TeamDTO enrich(TeamDTO teamDTO, String courseName) {
        Link selfLink = WebMvcLinkBuilder.linkTo(TeamController.class).slash(teamDTO.getId()).withSelfRel();
        Link courseLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getOne(courseName)).withRel("course");
        Link membersLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TeamController.class).getMembers(teamDTO.getId())).withRel("composedOf");
        return teamDTO.add(selfLink).add(courseLink).add(membersLink);
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
        Link selfLink = WebMvcLinkBuilder.linkTo(ExerciseController.class).slash(exerciseDTO.getId()).withSelfRel();
        Link courseLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getOne(courseId)).withRel("course");
        Link assignmentsLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ExerciseController.class).getLastAssignments(exerciseDTO.getId())).withRel("assignments");
        exerciseDTO.add(selfLink).add(courseLink,assignmentsLink);
        return exerciseDTO;
    }

    public static AssignmentDTO enrich(AssignmentDTO assignmentDTO, String studentId, Long exerciseId){
        Link selfLink = WebMvcLinkBuilder.linkTo(ExerciseController.class).slash(assignmentDTO.getId()).withSelfRel();
        Link exerciseLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ExerciseController.class).getOne(exerciseId)).withRel("exercise");
        Link studentLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getOne(studentId)).withRel("student");
        assignmentDTO.add(selfLink).add(exerciseLink).add(studentLink);
        return  assignmentDTO;
    }
}
