package it.polito.ai.backend.controllers;

import it.polito.ai.backend.dtos.*;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;


public class ModelHelper {

    public static CourseDTO enrich(CourseDTO courseDTO) {
        Link selfLink = WebMvcLinkBuilder.linkTo(CourseController.class).slash(courseDTO.getName()).withSelfRel();
        Link studentsLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).enrolledStudents(courseDTO.getName())).withRel("enrolled");
        Link teachersLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getTeachers(courseDTO.getName())).withRel("taughtBy");
        Link teamsLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getTeams(courseDTO.getName())).withRel("registers");
        Link modelLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getVirtualMachineModel(courseDTO.getName())).withRel("virtualMachinesModel");
        return courseDTO.add(selfLink).add(studentsLink).add(teachersLink).add(teamsLink).add(modelLink);
    }

    public static StudentDTO enrich(StudentDTO studentDTO) {
        Link selfLink = WebMvcLinkBuilder.linkTo(StudentController.class).slash(studentDTO.getId()).withSelfRel();
        Link coursesLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getCourses(studentDTO.getId())).withRel("enrolledTo");
        Link teamsLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getTeams(studentDTO.getId())).withRel("partOf");
        Link virtualMachinesLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getVirtualMachines(studentDTO.getId())).withRel("owns");
        return studentDTO.add(selfLink).add(coursesLink).add(teamsLink).add(virtualMachinesLink);
    }

    public static TeamDTO enrich(TeamDTO teamDTO, String courseName) {
        Link selfLink = WebMvcLinkBuilder.linkTo(TeamController.class).slash(teamDTO.getId()).withSelfRel();
        Link courseLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getOne(courseName)).withRel("course");
        Link membersLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TeamController.class).getMembers(teamDTO.getId())).withRel("composedOf");
        Link configurationLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TeamController.class).getConfiguration(teamDTO.getId())).withRel("virtualMachineConfiguration");
        Link virtualMachinesLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TeamController.class).getVirtualMachines(teamDTO.getId())).withRel("virtualMachines");
        return teamDTO.add(selfLink).addIf(courseName != null, () -> courseLink).add(membersLink).add(configurationLink).add(virtualMachinesLink);
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

    public static VirtualMachineDTO enrich(VirtualMachineDTO virtualMachineDTO, Long teamId) {
        Link selfLink = WebMvcLinkBuilder.linkTo(VirtualMachineController.class).slash(virtualMachineDTO.getId()).withSelfRel();
        Link modelLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(VirtualMachineController.class).getModel(virtualMachineDTO.getId())).withRel("model");
        Link teamLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TeamController.class).getOne(teamId)).withRel("usedBy");
        Link ownersLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(VirtualMachineController.class).getOwners(virtualMachineDTO.getId())).withRel("ownedBy");
        return virtualMachineDTO.add(selfLink).add(modelLink).addIf(teamId != null, () -> teamLink).add(ownersLink);
    }

    public static VirtualMachineConfigurationDTO enrich(VirtualMachineConfigurationDTO virtualMachineConfigurationDTO, Long teamId) {
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TeamController.class).getConfiguration(teamId)).withSelfRel();
        Link teamLink = WebMvcLinkBuilder.linkTo(TeamController.class).slash(teamId).withRel("definedFor");
        return virtualMachineConfigurationDTO.add(selfLink).addIf(teamId != null, () -> teamLink);
    }

    public static VirtualMachineModelDTO enrich(VirtualMachineModelDTO virtualMachineModelDTO, String courseName) {
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(VirtualMachineController.class).getModel(virtualMachineModelDTO.getId())).withSelfRel();
        Link courseLink = WebMvcLinkBuilder.linkTo(CourseController.class).slash(courseName).withRel("definedFor");
        return virtualMachineModelDTO.add(selfLink).addIf(courseName != null, () -> courseLink);
    }
}
