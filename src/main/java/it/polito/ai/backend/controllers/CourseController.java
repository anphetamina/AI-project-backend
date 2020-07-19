package it.polito.ai.backend.controllers;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.services.Utils;
import it.polito.ai.backend.services.exercise.AssignmentStatus;
import it.polito.ai.backend.services.exercise.ExerciseService;
import it.polito.ai.backend.services.notification.NotificationService;
import it.polito.ai.backend.services.team.*;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/courses")

public class CourseController {

    @Autowired
    TeamService teamService;
    @Autowired
    NotificationService notificationService;
    @Autowired
    ExerciseService exerciseService;
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

    @GetMapping("/{courseId}")
    CourseDTO getOne(@PathVariable String courseId) {
        try {
            return ModelHelper.enrich(teamService.getCourse(courseId).orElseThrow(() -> new CourseNotFoundException(courseId)));
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GetMapping("/{courseId}/enrolled")
    CollectionModel<StudentDTO> enrolledStudents(@PathVariable String courseId) {
        try {
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).enrolledStudents(courseId)).withSelfRel();
            List<StudentDTO> enrolledStudents = teamService.getEnrolledStudents(courseId).stream().map(ModelHelper::enrich).collect(Collectors.toList());
            return CollectionModel.of(enrolledStudents, selfLink);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GetMapping("/{courseId}/teams")
    CollectionModel<TeamDTO> getTeams(@PathVariable String courseId) {
        try {
            List<TeamDTO> teams = teamService.getTeamsForCourse(courseId).stream().map(t -> ModelHelper.enrich(t, courseId)).collect(Collectors.toList());
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getTeams(courseId)).withSelfRel();
            return CollectionModel.of(teams, selfLink);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GetMapping("/{courseId}/teachers")
    CollectionModel<TeacherDTO> getTeachers(@PathVariable String courseId) {
        try {
            List<TeacherDTO> teachers = teamService.getTeachersForCourse(courseId).stream().map(ModelHelper::enrich).collect(Collectors.toList());
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getTeachers(courseId)).withSelfRel();
            return CollectionModel.of(teachers, selfLink);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @DeleteMapping({"/{courseId}"})
    void deleteCourse(@PathVariable String courseId){
        teamService.deleteCourse(courseId);
    }

    @PutMapping("/{courseId}")
    CourseDTO updateNameCourse(@RequestBody Map<String, String> map, @PathVariable String courseId){
        if (map.containsKey("name")) {
            String name = map.get("name");
            try {
                Optional<CourseDTO> courseDTOOptional = teamService.getCourse(courseId);
                if (!courseDTOOptional.isPresent())
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, courseId);
                courseDTOOptional.get().setName(name);
                if (!teamService.update(courseDTOOptional.get()))
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, courseId);
                return ModelHelper.enrich(courseDTOOptional.get());
            } catch (TeamServiceException exception) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
            } catch (ResponseStatusException exception) {
                throw exception;
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }


    @PutMapping("/{courseId}/setCourse")
    CourseDTO updateCourse(@RequestBody Map<String, String> map, @PathVariable String courseId){
        if (map.containsKey("name") && map.containsKey("min") && map.containsKey("max") && map.containsKey("enabled")) {
            String name = map.get("name");
            int min = Integer.parseInt(map.get("min"));
            int max = Integer.parseInt(map.get("max"));
            boolean enabled = Boolean.parseBoolean(map.get("enabled"));
            try {
                Optional<CourseDTO> courseDTOOptional = teamService.getCourse(courseId);
                if (!courseDTOOptional.isPresent())
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, courseId);
                courseDTOOptional.get().setName(name);
                courseDTOOptional.get().setMin(min);
                courseDTOOptional.get().setMax(max);
                courseDTOOptional.get().setEnabled(enabled);
                if (!teamService.update(courseDTOOptional.get()))
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, courseId);
                return ModelHelper.enrich(courseDTOOptional.get());
            } catch (TeamServiceException exception) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
            } catch (ResponseStatusException exception) {
                throw exception;
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

    }

    @PostMapping({"", "/"})
    CourseDTO addCourse(@RequestBody @Valid CourseDTO courseDTO) {
        try {
            if (!teamService.addCourse(courseDTO)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, courseDTO.getId());
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

    @GetMapping("/{courseId}/teams/students")
    CollectionModel<StudentDTO> getStudentsInTeams(@PathVariable String courseId) {
        try {
            List<StudentDTO> studentsInTeams = teamService.getStudentsInTeams(courseId).stream().map(ModelHelper::enrich).collect(Collectors.toList());
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getStudentsInTeams(courseId)).withSelfRel();
            return CollectionModel.of(studentsInTeams, selfLink);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GetMapping("/{courseId}/teams/availableStudents")
    CollectionModel<StudentDTO> getAvailableStudents(@PathVariable String courseId) {
        try {
            List<StudentDTO> availableStudents = teamService.getAvailableStudents(courseId).stream().map(ModelHelper::enrich).collect(Collectors.toList());
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getAvailableStudents(courseId)).withSelfRel();
            return CollectionModel.of(availableStudents, selfLink);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @PostMapping("/{courseId}/enable")
    @ResponseStatus(HttpStatus.OK)
    void enable(@PathVariable String courseId) {
        try {
            teamService.enableCourse(courseId);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @PostMapping("/{courseId}/disable")
    @ResponseStatus(HttpStatus.OK)
    void disable(@PathVariable String courseId) {
        try {
            teamService.disableCourse(courseId);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }


    // http -v POST http://localhost:8080/API/courses/ai/enrollOne studentId=265000
    @PostMapping("/{courseId}/enrollOne")
    @ResponseStatus(HttpStatus.CREATED)
    void addStudent(@RequestBody Map<String, String> map, @PathVariable String courseId) {
        if (map.containsKey("studentId")) {
            try {
                if (!teamService.addStudentToCourse(map.get("studentId"), courseId)) {
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

    @PostMapping("/{courseId}/addTeacher")
    @ResponseStatus(HttpStatus.CREATED)
    void addTeacher(@RequestBody Map<String, String> map, @PathVariable String courseId) {
        if (map.containsKey("teacherId")) {
            try {
                if (!teamService.addTeacherToCourse(map.get("teacherId"), courseId)) {
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

    @PostMapping("/{courseId}/enrollMany")
    List<Boolean> enrollStudents(@RequestParam("file") MultipartFile file, @PathVariable String courseId) {

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
                addedAndEnrolledStudents = teamService.addAndEnroll(reader, courseId);
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


    @PostMapping("/{courseId}/enrollAll")
    List<Boolean> enrollAll(@RequestParam("file") MultipartFile file, @PathVariable String courseId) {

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
                enrolledStudents = teamService.enrollAll(studentIds, courseId);
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
    @PostMapping("/{courseId}/createTeam")
    TeamDTO createTeam(@RequestBody Map<String, Object> map, @PathVariable String courseId) {
        if (map.containsKey("teamName") && map.containsKey("memberIds")) {
            try {
                String teamName = modelMapper.map(map.get("teamName"), String.class);
                if (!teamName.isEmpty() && teamName.matches("[a-zA-Z0-9]+")) {
                    Type listType = new TypeToken<List<String>>() {}.getType();
                    List<String> memberIds = modelMapper.map(map.get("memberIds"), listType);
                    if (memberIds.stream().noneMatch(id -> id == null) && memberIds.stream().allMatch(id -> id.matches("[0-9]+"))) {
                        TeamDTO team = teamService.proposeTeam(courseId, teamName, memberIds);
                        notificationService.notifyTeam(team, memberIds);
                        return ModelHelper.enrich(team, courseId);
                    } else {
                        throw new NotValidRequestException(memberIds.toString());
                    }
                } else {
                    throw new NotValidRequestException(teamName);
                }
            }/* catch (AccessDeniedException exception) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
            }*/ catch (CourseNotFoundException | StudentNotFoundException exception) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
            } catch (NotValidRequestException | IllegalArgumentException | MappingException | ConfigurationException exception) {
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


    @PostMapping("/{courseId}/createExercise")
    void createExercise(@RequestParam("image") MultipartFile file,@RequestParam Map<String, String> map, @PathVariable String courseId){
        if (map.containsKey("published") && map.containsKey("expired")) {
            try {

                Utils.checkTypeImage(file);
                System.out.println("Original Image Byte Size - " + file.getBytes().length);
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                Timestamp expired = new Timestamp(format.parse(map.get("expired")).getTime());
                Timestamp published =new Timestamp( format.parse(map.get("published")).getTime());
                exerciseService.addExerciseForCourse(courseId,published,expired,file);

            } catch (ResponseStatusException exception) {
                throw exception;
            } catch (Exception exception) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

    }

    @GetMapping("/{courseId}/exercises")
    List<ExerciseDTO> getExercises(@PathVariable String courseId){
        try {
           List<ExerciseDTO> list = exerciseService.getExercisesForCourse(courseId);
            List<ExerciseDTO> exerciseDTOS = new ArrayList<>();
            for (ExerciseDTO exerciseDTO:list) {
                exerciseDTOS.add(ModelHelper.enrich(exerciseDTO,courseId));
            }
            return exerciseDTOS;
        } catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }

    }

}
