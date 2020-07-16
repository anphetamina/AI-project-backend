package it.polito.ai.backend.controllers;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.entities.SystemImage;
import it.polito.ai.backend.services.notification.NotificationService;
import it.polito.ai.backend.services.team.*;
import it.polito.ai.backend.services.vm.*;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.modelmapper.ConfigurationException;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/courses")
public class CourseController {

    @Autowired
    TeamService teamService;
    @Autowired
    NotificationService notificationService;
    @Autowired
    VirtualMachineService virtualMachineService;
    @Autowired
    ModelMapper modelMapper;

    @GetMapping({"", "/"})
    CollectionModel<CourseDTO> all() {
        try {
            List<CourseDTO> courses = teamService.getAllCourses().stream().map(ModelHelper::enrich).collect(Collectors.toList());
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).all()).withSelfRel();
            return CollectionModel.of(courses, selfLink);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GetMapping("/{name}")
    CourseDTO getOne(@PathVariable String name) {
        try {
            return ModelHelper.enrich(teamService.getCourse(name).orElseThrow(() -> new CourseNotFoundException(name)));
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GetMapping("/{name}/enrolled")
    CollectionModel<StudentDTO> enrolledStudents(@PathVariable String name) {
        try {
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).enrolledStudents(name)).withSelfRel();
            List<StudentDTO> enrolledStudents = teamService.getEnrolledStudents(name).stream().map(ModelHelper::enrich).collect(Collectors.toList());
            return CollectionModel.of(enrolledStudents, selfLink);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GetMapping("/{name}/teams")
    CollectionModel<TeamDTO> getTeams(@PathVariable String name) {
        try {
            List<TeamDTO> teams = teamService.getTeamsForCourse(name).stream().map(t -> ModelHelper.enrich(t, name)).collect(Collectors.toList());
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getTeams(name)).withSelfRel();
            return CollectionModel.of(teams, selfLink);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GetMapping("/{name}/teachers")
    CollectionModel<TeacherDTO> getTeachers(@PathVariable String name) {
        try {
            List<TeacherDTO> teachers = teamService.getTeachersForCourse(name).stream().map(ModelHelper::enrich).collect(Collectors.toList());
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getTeachers(name)).withSelfRel();
            return CollectionModel.of(teachers, selfLink);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @PostMapping({"", "/"})
    CourseDTO addCourse(@RequestBody @Valid CourseDTO courseDTO) {
        try {
            if (!teamService.addCourse(courseDTO)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, courseDTO.getName());
            }
            return ModelHelper.enrich(courseDTO);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GetMapping("/{name}/teams/students")
    CollectionModel<StudentDTO> getStudentsInTeams(@PathVariable String name) {
        try {
            List<StudentDTO> studentsInTeams = teamService.getStudentsInTeams(name).stream().map(ModelHelper::enrich).collect(Collectors.toList());
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getStudentsInTeams(name)).withSelfRel();
            return CollectionModel.of(studentsInTeams, selfLink);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GetMapping("/{name}/teams/availableStudents")
    CollectionModel<StudentDTO> getAvailableStudents(@PathVariable String name) {
        try {
            List<StudentDTO> availableStudents = teamService.getAvailableStudents(name).stream().map(ModelHelper::enrich).collect(Collectors.toList());
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getAvailableStudents(name)).withSelfRel();
            return CollectionModel.of(availableStudents, selfLink);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @PostMapping("/{name}/enable")
    @ResponseStatus(HttpStatus.OK)
    void enable(@PathVariable String name) {
        try {
            teamService.enableCourse(name);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @PostMapping("/{name}/disable")
    @ResponseStatus(HttpStatus.OK)
    void disable(@PathVariable String name) {
        try {
            teamService.disableCourse(name);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }


    // http -v POST http://localhost:8080/API/courses/ai/enrollOne studentId=265000
    @PostMapping("/{name}/enrollOne")
    @ResponseStatus(HttpStatus.CREATED)
    void addStudent(@RequestBody Map<String, String> map, @PathVariable String name) {
        if (map.containsKey("studentId")) {
            try {
                if (!teamService.addStudentToCourse(map.get("studentId"), name)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, map.get("studentId"));
                }
            }/* catch (AccessDeniedException exception) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
            }*/ catch (TeamServiceException exception) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
            } catch (ResponseStatusException exception) {
                throw exception;
            } catch (Exception exception) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/{name}/addTeacher")
    @ResponseStatus(HttpStatus.CREATED)
    void addTeacher(@RequestBody Map<String, String> map, @PathVariable String name) {
        if (map.containsKey("teacherId")) {
            try {
                if (!teamService.addTeacherToCourse(map.get("teacherId"), name)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, map.get("teacherId"));
                }
            }/* catch (AccessDeniedException exception) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
            }*/ catch (TeamServiceException exception) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
            } catch (ResponseStatusException exception) {
                throw exception;
            } catch (Exception exception) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    /*List<String> mimeTypes = Arrays.asList(
            "text/plain",
            "text/x-csv",
            "application/vnd.ms-excel",
            "application/csv",
            "application/x-csv",
            "text/csv",
            "text/comma-separated-values",
            "text/x-comma-separated-values",
            "text/tab-separated-values");*/

    @PostMapping("/{name}/enrollMany")
    List<Boolean> enrollStudents(@RequestParam("file") MultipartFile file, @PathVariable String name) {

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        try {
            TikaConfig tika = new TikaConfig();
            Metadata metadata = new Metadata();
            metadata.set(Metadata.RESOURCE_NAME_KEY, file.getOriginalFilename());
            MediaType mimeType = tika.getDetector().detect(TikaInputStream.get(file.getBytes()), metadata);
            String type = mimeType.toString();
            if (!type.equalsIgnoreCase("text/csv")) {
                throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, type);
            }

            List<Boolean> addedAndEnrolledStudents = new ArrayList<>();

            if (!file.isEmpty()) {
                Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
                addedAndEnrolledStudents = teamService.addAndEnroll(reader, name);
            }

            return addedAndEnrolledStudents;
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (CourseNotFoundException | StudentNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }


    }

    @PostMapping("/{name}/enrollAll")
    List<Boolean> enrollAll(@RequestParam("file") MultipartFile file, @PathVariable String name) {

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        try {
            TikaConfig tika = new TikaConfig();
            Metadata metadata = new Metadata();
            metadata.set(Metadata.RESOURCE_NAME_KEY, file.getOriginalFilename());
            MediaType mimeType = tika.getDetector().detect(TikaInputStream.get(file.getBytes()), metadata);
            String type = mimeType.toString();
            if (!type.equalsIgnoreCase("text/csv")) {
                throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, type);
            }

            List<Boolean> enrolledStudents = new ArrayList<>();

            if (!file.isEmpty()) {
                Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
                CsvToBean<StudentDTO> csvToBean = new CsvToBeanBuilder(reader)
                        .withType(StudentDTO.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .build();
                List<StudentDTO> students = csvToBean.parse();
                List<String> studentIds = students.stream().map(StudentDTO::getId).collect(Collectors.toList());
                enrolledStudents = teamService.enrollAll(studentIds, name);
            }

            return enrolledStudents;
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (CourseNotFoundException | StudentNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }


    }

    // http -v POST http://localhost:8080/API/courses/ase/createTeam teamName=aseTeam0 memberIds:=[\"264000\",\"264001\",\"264002\",\"264004\"]
    @PostMapping("/{name}/createTeam")
    TeamDTO createTeam(@RequestBody Map<String, Object> map, @PathVariable String name) {
        if (map.containsKey("teamName") && map.containsKey("memberIds")) {
            try {
                String teamName = modelMapper.map(map.get("teamName"), String.class);
                if (!teamName.isEmpty() && teamName.matches("[a-zA-Z0-9]+")) {
                    Type listType = new TypeToken<List<String>>() {}.getType();
                    List<String> memberIds = modelMapper.map(map.get("memberIds"), listType);
                    if (memberIds.stream().noneMatch(id -> id == null) && memberIds.stream().allMatch(id -> id.matches("[0-9]+"))) {
                        TeamDTO team = teamService.proposeTeam(name, teamName, memberIds);
                        notificationService.notifyTeam(team, memberIds);
                        return ModelHelper.enrich(team, name);
                    } else {
                        throw new InvalidRequestException(memberIds.toString());
                    }
                } else {
                    throw new InvalidRequestException(teamName);
                }
            }/* catch (AccessDeniedException exception) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
            }*/ catch (CourseNotFoundException | StudentNotFoundException exception) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
            } catch (InvalidRequestException | IllegalArgumentException | MappingException | ConfigurationException exception) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
            } catch (TeamServiceException exception) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
            } catch (Exception exception) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/{courseName}/model")
    @ResponseStatus(HttpStatus.CREATED)
    VirtualMachineModelDTO addVirtualMachineModel(@PathVariable @NotBlank String courseName, @NotNull Map<String, Object> map) {

        if (!map.containsKey("systemImage")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } else {
            try {
                SystemImage os = modelMapper.map(map.get("systemImage"), SystemImage.class);
                VirtualMachineModelDTO virtualMachineModel = virtualMachineService.createVirtualMachineModel(courseName, os);
                return ModelHelper.enrich(virtualMachineModel, courseName);
            } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

    }

    @DeleteMapping("/{courseName}/deleteModel")
    void deleteVirtualMachineModel(@PathVariable @NotBlank String courseName) {
        try {
            if (!virtualMachineService.deleteVirtualMachineModel(courseName)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Please turn off all the virtual machines using this model");
            }
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("{courseName}/model")
    VirtualMachineModelDTO getVirtualMachineModel(@PathVariable @NotBlank String courseName) {
        try {
            return ModelHelper.enrich(virtualMachineService.getVirtualMachineModelForCourse(courseName).orElseThrow(() -> new VirtualMachineModelNotDefinedException(courseName)), courseName);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
