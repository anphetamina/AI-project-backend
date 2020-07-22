package it.polito.ai.backend.controllers;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.entities.SystemImage;
import it.polito.ai.backend.services.Utils;
import it.polito.ai.backend.services.exercise.ExerciseService;
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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @GetMapping("/{courseId}/teams/{teamId}")
    TeamDTO getTeam(@PathVariable String courseId, @PathVariable Long teamId) {
        try {
            TeamDTO teamDTO = teamService.getTeam(teamId).orElseThrow(() -> new TeamNotFoundException(teamId.toString()));
            return ModelHelper.enrich(teamDTO, courseId);
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

    @GetMapping("/{courseId}/teams/{teamId}/members")
    CollectionModel<StudentDTO> getMembers(@PathVariable String courseId, @PathVariable Long teamId) {
        try {
            List<StudentDTO> students = teamService.getMembers(teamId).stream().map(ModelHelper::enrich).collect(Collectors.toList());
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getMembers(courseId, teamId)).withSelfRel();
            return CollectionModel.of(students, selfLink);
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
        // todo
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

    @GetMapping("/{courseId}/teams/available-students")
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

    @PostMapping("/{courseId}/teachers")
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
    @PostMapping("/{courseId}/teams")
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


    @PostMapping("/{courseId}/createExercise")
    void createExercise(@RequestParam("image") MultipartFile file,@RequestParam Map<String, String> map, @PathVariable String courseId){
        if (map.containsKey("expired")) {
            try {

                Utils.checkTypeImage(file);
                System.out.println("Original Image Byte Size - " + file.getBytes().length);
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                Timestamp expired = new Timestamp(format.parse(map.get("expired")).getTime());
                Timestamp published = Utils.getNow();
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

    @PostMapping("/{courseId}/model")
    @ResponseStatus(HttpStatus.CREATED)
    VirtualMachineModelDTO addVirtualMachineModel(@PathVariable @NotBlank String courseId, @NotNull Map<String, Object> map) {

        if (!map.containsKey("systemImage")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } else {
            try {
                SystemImage os = modelMapper.map(map.get("systemImage"), SystemImage.class);
                VirtualMachineModelDTO virtualMachineModel = virtualMachineService.createVirtualMachineModel(courseId, os);
                return ModelHelper.enrich(virtualMachineModel, courseId);
            } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

    }

    @DeleteMapping("/{courseId}/model")
    void deleteVirtualMachineModel(@PathVariable @NotBlank String courseId) {
        try {
            if (!virtualMachineService.deleteVirtualMachineModel(courseId)) {
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

    @GetMapping("/{courseId}/model")
    VirtualMachineModelDTO getVirtualMachineModel(@PathVariable @NotBlank String courseId) {
        try {
            return ModelHelper.enrich(virtualMachineService.getVirtualMachineModelForCourse(courseId).orElseThrow(() -> new VirtualMachineModelNotDefinedException(courseId)), courseId);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{courseId}/teams/{teamId}/virtual-machines/{vmId}")
    VirtualMachineDTO getVirtualMachine(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId, @PathVariable @NotNull Long vmId) {
        try {
            VirtualMachineDTO virtualMachineDTO = virtualMachineService.getVirtualMachine(courseId, teamId, vmId).orElseThrow(() -> new VirtualMachineNotFoundException(vmId.toString()));
            return ModelHelper.enrich(virtualMachineDTO, courseId, teamId);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{courseId}/teams/{teamId}/virtual-machines/{vmId}/owners")
    CollectionModel<StudentDTO> getOwners(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId, @PathVariable @NotNull Long vmId) {
        try {
            List<StudentDTO> studentDTOList = virtualMachineService.getOwnersForVirtualMachine(courseId, teamId, vmId).stream().map(ModelHelper::enrich).collect(Collectors.toList());
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getOwners(courseId, teamId, vmId)).withSelfRel();
            return CollectionModel.of(studentDTOList, selfLink);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/{courseId}/teams/{teamId}/virtual-machines")
    @ResponseStatus(HttpStatus.CREATED)
    VirtualMachineDTO addVirtualMachine(@RequestBody @NotNull Map<String, Object> map, @PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId) {

        if (!(map.containsKey("studentId") && map.containsKey("numVcpu") && map.containsKey("diskSpace") && map.containsKey("ram"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid number of parameters");
        } else {

            try {

                String studentId = modelMapper.map(map.get("studentId"), String.class);
                int numVcpu = modelMapper.map(map.get("numVcpu"), int.class);
                int diskSpace = modelMapper.map(map.get("diskSpace"), int.class);
                int ram = modelMapper.map(map.get("ram"), int.class);

                /**
                 * parameters validation
                 */

                if (studentId.isEmpty() || studentId.trim().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid student id" + studentId);
                }
                if (teamId == null || teamId <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid team id" + teamId);
                }
                if (numVcpu <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid num vcpu" + numVcpu);
                }
                if (diskSpace <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid disk space" + diskSpace);
                }
                if (ram <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ram" + ram);
                }

                VirtualMachineDTO virtualMachine = virtualMachineService.createVirtualMachine(courseId, teamId, studentId, numVcpu, diskSpace, ram);
                return ModelHelper.enrich(virtualMachine, courseId, teamId);
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


    }

    @DeleteMapping("/{courseId}/teams/{teamId}/virtual-machines/{vmId}")
    void deleteVirtualMachine(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId, @PathVariable @NotNull Long vmId) {
        try {
            if (!virtualMachineService.deleteVirtualMachine(courseId, teamId, vmId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT);
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

    @PutMapping("/{courseId}/teams/{teamId}/virtual-machines/{vmId}")
    VirtualMachineDTO setVirtualMachine(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId, @PathVariable @NotNull Long vmId, @RequestBody @Valid VirtualMachineDTO virtualMachineDTO) {
        try {
            VirtualMachineDTO newVirtualMachine = virtualMachineService.updateVirtualMachine(courseId, teamId, vmId, virtualMachineDTO);
            return ModelHelper.enrich(newVirtualMachine, courseId, teamId);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{courseId}/teams/{teamId}/virtual-machines/{vmId}/on")
    @ResponseStatus(HttpStatus.OK)
    void turnOn(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId, @PathVariable @NotNull Long vmId) {
        try {
            virtualMachineService.turnOnVirtualMachine(courseId, teamId, vmId);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{courseId}/teams/{teamId}/virtual-machines/{vmId}/off")
    @ResponseStatus(HttpStatus.OK)
    void turnOff(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId, @PathVariable @NotNull Long vmId) {
        try {
            virtualMachineService.turnOffVirtualMachine(courseId, teamId, vmId);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{courseId}/teams/{teamId}/virtual-machines")
    CollectionModel<VirtualMachineDTO> getVirtualMachines(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId) {
        try {
            List<VirtualMachineDTO> virtualMachineDTOList = virtualMachineService.getVirtualMachinesForTeam(courseId, teamId);
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getVirtualMachines(courseId, teamId)).withSelfRel();
            return CollectionModel.of(virtualMachineDTOList, selfLink);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{courseId}/teams/{teamId}/virtual-machines/{vmId}/owners")
    @ResponseStatus(HttpStatus.OK)
    void shareOwnership(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId, @PathVariable @NotNull Long vmId, @RequestBody @NotBlank String studentId) {
        try {
            if (!virtualMachineService.addOwnerToVirtualMachine(courseId, teamId, studentId, vmId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot share ownership with "+studentId);
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

    @PostMapping("/{courseId}/teams/{teamId}/configuration")
    @ResponseStatus(HttpStatus.CREATED)
    ConfigurationDTO addConfiguration(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId, @RequestBody @NotNull Map<String, Object> map) {
        if (!(map.containsKey("min_vcpu") && map.containsKey("max_vcpu") && map.containsKey("min_disk_space") && map.containsKey("max_disk_space") && map.containsKey("min_ram") && map.containsKey("max_ram") && map.containsKey("max_on") && map.containsKey("tot"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } else {

            try {

                int min_vcpu = modelMapper.map(map.get("min_vcpu"), int.class);
                int max_vcpu = modelMapper.map(map.get("max_vcpu"), int.class);
                int min_disk_space = modelMapper.map(map.get("min_disk_space"), int.class);
                int max_disk_space = modelMapper.map(map.get("max_disk_space"), int.class);
                int min_ram = modelMapper.map(map.get("min_ram"), int.class);
                int max_ram = modelMapper.map(map.get("max_ram"), int.class);
                int max_on = modelMapper.map(map.get("max_on"), int.class);
                int tot = modelMapper.map(map.get("tot"), int.class);

                if (min_vcpu <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid min vcpu number " + min_vcpu);
                }
                if (max_vcpu <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid max vcpu number " + max_vcpu);
                }
                if (min_disk_space <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid min disk space number " + min_disk_space);
                }
                if (max_disk_space <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid max disk space number " + max_disk_space);
                }
                if (min_ram <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid min ram number " + min_ram);
                }
                if (max_ram <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid max ram number " + max_ram);
                }
                if (max_on <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid max on number " + max_on);
                }
                if (tot <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid tot number " + tot);
                }

                ConfigurationDTO configurationDTO = virtualMachineService.createConfiguration(courseId, teamId, min_vcpu, max_vcpu, min_disk_space, max_disk_space, min_ram, max_ram, max_on, tot);
                return ModelHelper.enrich(configurationDTO, courseId, teamId);
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

    }

    @GetMapping("/{courseId}/teams/{teamId}/configuration")
    ConfigurationDTO getConfiguration(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId) {
        try {
            return ModelHelper.enrich(virtualMachineService.getConfigurationForTeam(courseId, teamId).orElseThrow(() -> new ConfigurationNotDefinedException(teamId.toString())), courseId, teamId);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{courseId}/teams/{teamId}/configuration")
    ConfigurationDTO setConfiguration(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId, @RequestBody @Valid ConfigurationDTO configurationDTO) {
        try {
            ConfigurationDTO newConfigurationDTO = virtualMachineService.updateConfiguration(courseId, teamId, configurationDTO);
            return ModelHelper.enrich(newConfigurationDTO, courseId, teamId);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{courseId}/teams/{teamId}/virtual-machines/active-cpu")
    int getActiveVcpu(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId) {
        try {
            return virtualMachineService.getActiveVcpuForTeam(courseId, teamId);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{courseId}/teams/{teamId}/virtual-machines/active-disk-space")
    int getActiveDiskSpace(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId) {
        try {
            return virtualMachineService.getActiveDiskSpaceForTeam(courseId, teamId);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{courseId}/teams/{teamId}/virtual-machines/active-ram")
    int getActiveRam(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId) {
        try {
            return virtualMachineService.getActiveRAMForTeam(courseId, teamId);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{courseId}/teams/{teamId}/virtual-machines/tot")
    int getCountVirtualMachines(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId) {
        try {
            return virtualMachineService.getCountVirtualMachinesForTeam(courseId, teamId);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{courseId}/teams/{teamId}/virtual-machines/tot-on")
    int getCountActiveVirtualMachines(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId) {
        try {
            return virtualMachineService.getCountActiveVirtualMachinesForTeam(courseId, teamId);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{courseId}/teams/{teamId}/virtual-machines/resources")
    Map<String, Integer> getResources(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId) {
        try {
            return virtualMachineService.getResourcesByTeam(courseId, teamId);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
