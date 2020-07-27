package it.polito.ai.backend.controllers;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.services.Utils;
import it.polito.ai.backend.services.exercise.ExerciseService;
import it.polito.ai.backend.services.notification.NotificationService;
import it.polito.ai.backend.services.team.*;
import it.polito.ai.backend.services.vm.VirtualMachineService;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/courses")
@Validated
public class CourseController {

    @Autowired
    TeamService teamService;
    @Autowired
    NotificationService notificationService;
    @Autowired
    ExerciseService exerciseService;
    @Autowired
    VirtualMachineService virtualMachineService;
    @Autowired
    ModelMapper modelMapper;

    @GetMapping({"", "/"})
    CollectionModel<CourseDTO> all() {
        List<CourseDTO> courses = teamService.getAllCourses()
                .stream()
                .map(c -> {
                    Long modelId = virtualMachineService.getVirtualMachineModelForCourse(c.getId()).map(VirtualMachineModelDTO::getId).orElse(null);
                    return ModelHelper.enrich(c, modelId);
                })
                .collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).all()).withSelfRel();
        return CollectionModel.of(courses, selfLink);
    }

    @GetMapping("/{courseId}")
    CourseDTO getOne(@PathVariable @NotBlank String courseId) {
        CourseDTO courseDTO = teamService.getCourse(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        Long modelId = virtualMachineService.getVirtualMachineModelForCourse(courseDTO.getId()).map(VirtualMachineModelDTO::getId).orElse(null);
        return ModelHelper.enrich(courseDTO, modelId);
    }

    @GetMapping("/{courseId}/enrolled")
    CollectionModel<StudentDTO> enrolledStudents(@PathVariable @NotBlank String courseId) {
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).enrolledStudents(courseId)).withSelfRel();
        List<StudentDTO> enrolledStudents = teamService.getEnrolledStudents(courseId).stream().map(ModelHelper::enrich).collect(Collectors.toList());
        return CollectionModel.of(enrolledStudents, selfLink);
    }

    @GetMapping("/{courseId}/teams")
    CollectionModel<TeamDTO> getTeams(@PathVariable @NotBlank String courseId) {
        List<TeamDTO> teams = teamService.getTeamsForCourse(courseId)
                .stream()
                .map(t -> {
                    Long configurationId = virtualMachineService.getConfigurationForTeam(t.getId()).map(ConfigurationDTO::getId).orElse(null);
                    return ModelHelper.enrich(t, courseId, configurationId);
                })
                .collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getTeams(courseId)).withSelfRel();
        return CollectionModel.of(teams, selfLink);
    }

    @GetMapping("/{courseId}/teachers")
    CollectionModel<TeacherDTO> getTeachers(@PathVariable @NotBlank String courseId) {
        List<TeacherDTO> teachers = teamService.getTeachersForCourse(courseId).stream().map(ModelHelper::enrich).collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getTeachers(courseId)).withSelfRel();
        return CollectionModel.of(teachers, selfLink);
    }

    @DeleteMapping({"/{courseId}"})
    void deleteCourse(@PathVariable @NotBlank String courseId){
        if (!teamService.deleteCourse(courseId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "course is enabled or is associated with one or more entities");
        }
    }

    @PutMapping("/{courseId}")
    CourseDTO updateCourse(@RequestBody @Valid CourseDTO courseDTO, @PathVariable @NotBlank String courseId){
        CourseDTO courseDTO1 = teamService.updateCourse(courseId, courseDTO);
        Long modelId = virtualMachineService.getVirtualMachineModelForCourse(courseId).map(VirtualMachineModelDTO::getId).orElse(null);
        return ModelHelper.enrich(courseDTO1, modelId);

    }

    @PostMapping({"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    CourseDTO addCourse(@RequestBody @Valid CourseDTO courseDTO) {
        if (!teamService.addCourse(courseDTO)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("course %s already exists", courseDTO.getId()));
        }
        return ModelHelper.enrich(courseDTO, null);
    }

    @GetMapping("/{courseId}/teams/students")
    CollectionModel<StudentDTO> getStudentsInTeams(@PathVariable @NotBlank String courseId) {
        List<StudentDTO> studentsInTeams = teamService.getStudentsInTeams(courseId).stream().map(ModelHelper::enrich).collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getStudentsInTeams(courseId)).withSelfRel();
        return CollectionModel.of(studentsInTeams, selfLink);
    }

    @GetMapping("/{courseId}/teams/available-students")
    CollectionModel<StudentDTO> getAvailableStudents(@PathVariable @NotBlank String courseId) {
        List<StudentDTO> availableStudents = teamService.getAvailableStudents(courseId).stream().map(ModelHelper::enrich).collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getAvailableStudents(courseId)).withSelfRel();
        return CollectionModel.of(availableStudents, selfLink);
    }

    @PostMapping("/{courseId}/enable")
    void enable(@PathVariable @NotBlank String courseId) {
        teamService.enableCourse(courseId);
    }

    @PostMapping("/{courseId}/disable")
    void disable(@PathVariable @NotBlank String courseId) {
        teamService.disableCourse(courseId);
    }

    @PostMapping("/{courseId}/enrollOne")
    void addStudent(@RequestBody Map<String, String> map, @PathVariable @NotBlank String courseId) {
        if (!map.containsKey("studentId")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid request");
        }

        String studentId = map.get("studentId");

        if (studentId == null || studentId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid student id");
        }

        if (!teamService.addStudentToCourse(studentId, courseId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("student %s already inserted", studentId));
        }
    }

    @DeleteMapping("/{courseId}/enrolled/{studentId}")
    void removeStudent(@PathVariable @NotBlank String courseId, @PathVariable @NotBlank String studentId) {
        if (!teamService.removeStudentFromCourse(studentId, courseId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "the student is part of a team");
        }
    }

    @PostMapping("/{courseId}/teachers")
    void addTeacher(@RequestBody Map<String, String> map, @PathVariable @NotBlank String courseId) {
        if (!map.containsKey("teacherId")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid request");
        }

        String teacherId = map.get("teacherId");

        if (teacherId == null || teacherId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid teacher id");
        }

        if (!teamService.addTeacherToCourse(teacherId, courseId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("teacher %s already inserted", teacherId));
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

    @PostMapping("/{courseId}/enrollMany")
    List<Boolean> enrollStudents(@RequestParam("file") MultipartFile file, @PathVariable @NotBlank String courseId) {

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file empty");
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
                addedAndEnrolledStudents = teamService.addAndEnroll(reader, courseId);
            }

            return addedAndEnrolledStudents;
        } catch (TikaException | IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "invalid file content");
        }


    }

    @PostMapping("/{courseId}/enrollAll")
    List<Boolean> enrollAll(@RequestParam("file") MultipartFile file, @PathVariable @NotBlank String courseId) {

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file empty");
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
                enrolledStudents = teamService.enrollAll(studentIds, courseId);
            }

            return enrolledStudents;
        } catch (TikaException | IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "invalid file content");
        }


    }

    // http -v POST http://localhost:8080/API/courses/ase/createTeam teamName=aseTeam0 memberIds:=[\"264000\",\"264001\",\"264002\",\"264004\"]
    @PostMapping("/{courseId}/createTeam")
    @ResponseStatus(HttpStatus.CREATED)
    TeamDTO createTeam(@RequestBody Map<String, Object> map, @PathVariable @NotBlank String courseId) {
        if (map.containsKey("teamName") && map.containsKey("memberIds")) {
            try {
                String teamName = modelMapper.map(map.get("teamName"), String.class);
                if (!teamName.isEmpty() && teamName.matches("[a-zA-Z0-9]+")) {
                    Type listType = new TypeToken<List<String>>() {}.getType();
                    List<String> memberIds = modelMapper.map(map.get("memberIds"), listType);
                    if (memberIds.stream().noneMatch(id -> id == null) && memberIds.stream().allMatch(id -> id.matches("[0-9]+"))) {
                        TeamDTO team = teamService.proposeTeam(courseId, teamName, memberIds);
                        notificationService.notifyTeam(team, memberIds);
                        return ModelHelper.enrich(team, courseId, null);
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


    @PostMapping("/{courseId}/exercises")
    void createExercise(@RequestParam("image") MultipartFile file, @RequestParam Map<String, String> map, @PathVariable @NotBlank String courseId){
        if (!map.containsKey("expired")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid request");
        }
        try {
            Utils.checkTypeImage(file);
            System.out.println("Original Image Byte Size - " + file.getBytes().length);
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            Timestamp expired = new Timestamp(format.parse(map.get("expired")).getTime());
            Timestamp published = Utils.getNow();
            exerciseService.addExerciseForCourse(courseId,published,expired,file);
        } catch (ParseException | IOException | TikaException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "invalid file content");
        }
    }

    @GetMapping("/{courseId}/exercises")
    List<ExerciseDTO> getExercises(@PathVariable @NotBlank String courseId){

        // todo collection model
        List<ExerciseDTO> list = exerciseService.getExercisesForCourse(courseId);
        List<ExerciseDTO> exerciseDTOS = new ArrayList<>();
        for (ExerciseDTO exerciseDTO:list) {
            exerciseDTOS.add(ModelHelper.enrich(exerciseDTO,courseId));
        }
        return exerciseDTOS;

    }

}
