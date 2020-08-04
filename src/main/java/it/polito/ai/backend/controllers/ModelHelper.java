package it.polito.ai.backend.controllers;

import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.services.notification.NotificationService;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;


public class ModelHelper {

    public static CourseDTO enrich(CourseDTO courseDTO, Long modelId) {
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getOne(courseDTO.getId())).withSelfRel();
        Link studentsLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).enrolledStudents(courseDTO.getId())).withRel("enrolled");
        Link teachersLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getTeachers(courseDTO.getId())).withRel("taughtBy");
        Link teamsLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getTeams(courseDTO.getId())).withRel("registers");
        Link exerciseLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getExercises(courseDTO.getId())).withRel("exercises");
        Link modelLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(VirtualMachineController.class).getOne(modelId)).withRel("virtualMachineModel");
        return courseDTO.add(selfLink, studentsLink, teachersLink, teamsLink, exerciseLink).addIf(modelId != null, () -> modelLink);
    }

    public static StudentDTO enrich(StudentDTO studentDTO) {
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getOne(studentDTO.getId())).withSelfRel();
        Link coursesLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getCourses(studentDTO.getId())).withRel("enrolledTo");
        Link teamsLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getTeams(studentDTO.getId())).withRel("partOf");
        Link virtualMachinesLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getVirtualMachines(studentDTO.getId())).withRel("owns");
        return studentDTO.add(selfLink).add(coursesLink).add(teamsLink).add(virtualMachinesLink);
    }

    public static TeamDTO enrich(TeamDTO teamDTO, String courseId, Long configurationId) {
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TeamController.class).getOne(teamDTO.getId())).withSelfRel();
        Link courseLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getOne(courseId)).withRel("course");
        Link membersLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TeamController.class).getMembers(teamDTO.getId())).withRel("composedOf");
        Link configurationLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ConfigurationController.class).getOne(configurationId)).withRel("virtualMachineConfiguration");
        Link virtualMachinesLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TeamController.class).getVirtualMachines(teamDTO.getId())).withRel("virtualMachines");
        return teamDTO.add(selfLink, membersLink, virtualMachinesLink).addIf(courseId != null, () -> courseLink).addIf(configurationId != null, () -> configurationLink);
    }



    public static TokenDTO enrich(TokenDTO tokenDTO, String op) {
        Link opLink = WebMvcLinkBuilder.linkTo(NotificationController.class).slash("/teams/"+op+"/"+tokenDTO.getId()).withRel(op);
        return tokenDTO.add(opLink);
    }
    public static ConfirmationTokenDTO enrich(ConfirmationTokenDTO confirmationTokenDTO) {
        Link opLink = WebMvcLinkBuilder.linkTo(AuthController.class).slash("/sign-up/confirm/"+confirmationTokenDTO.getId()).withRel("confirmation");
        return confirmationTokenDTO.add(opLink);
    }

    public static TeacherDTO enrich(TeacherDTO teacherDTO) {
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TeacherController.class).getOne(teacherDTO.getId())).withSelfRel();
        Link coursesLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TeacherController.class).getCourses(teacherDTO.getId())).withRel("teaches");
        return teacherDTO.add(selfLink, coursesLink);
    }

    public static ExerciseDTO enrich(ExerciseDTO exerciseDTO, String courseId){
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ExerciseController.class).getOne(exerciseDTO.getId())).withSelfRel();
        Link courseLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getOne(courseId)).withRel("course");
        Link assignmentsLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ExerciseController.class).getLastAssignments(exerciseDTO.getId())).withRel("lastAssignmentsForEachStudent");
        exerciseDTO.add(selfLink, assignmentsLink).addIf(courseId != null, () -> courseLink);
        return exerciseDTO;
    }

    public static AssignmentDTO enrich(AssignmentDTO assignmentDTO, String studentId, Long exerciseId){
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AssignmentController.class).getOne(assignmentDTO.getId())).withSelfRel();
        Link exerciseLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ExerciseController.class).getOne(exerciseId)).withRel("exercise");
        Link studentLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getOne(studentId)).withRel("student");
        Link history = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ExerciseController.class).getHistoryAssignments(exerciseId,studentId)).withRel("history");
        assignmentDTO.add(selfLink).addIf(studentId != null, () -> studentLink).addIf(exerciseId != null, () -> exerciseLink).add(history);
        return assignmentDTO;
    }

    public static VirtualMachineDTO enrich(VirtualMachineDTO virtualMachineDTO, Long teamId, Long modelId) {
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(VirtualMachineController.class).getOne(virtualMachineDTO.getId())).withSelfRel();
        Link modelLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(VirtualMachineModelController.class).getOne(modelId)).withRel("model");
        Link teamLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TeamController.class).getOne(teamId)).withRel("usedBy");
        Link ownersLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(VirtualMachineController.class).getOwners(virtualMachineDTO.getId())).withRel("ownedBy");
        return virtualMachineDTO.add(selfLink, ownersLink).addIf(teamId != null, () -> teamLink).addIf(modelId != null, () -> modelLink);
    }

    public static ConfigurationDTO enrich(ConfigurationDTO configurationDTO, Long teamId) {
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ConfigurationController.class).getOne(configurationDTO.getId())).withSelfRel();
        Link teamLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TeamController.class).getOne(teamId)).withRel("definedFor");
        return configurationDTO.add(selfLink).addIf(teamId != null, () -> teamLink);
    }

    public static VirtualMachineModelDTO enrich(VirtualMachineModelDTO virtualMachineModelDTO, String courseId) {
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(VirtualMachineModelController.class).getOne(virtualMachineModelDTO.getId())).withSelfRel();
        Link courseLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getOne(courseId)).withRel("definedFor");
        return virtualMachineModelDTO.add(selfLink).addIf(courseId != null, () -> courseLink);
    }
}
