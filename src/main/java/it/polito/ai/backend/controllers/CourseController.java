package it.polito.ai.backend.controllers;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.entities.SystemImage;
import it.polito.ai.backend.services.Utils;
import it.polito.ai.backend.services.exercise.*;
import it.polito.ai.backend.services.notification.NotificationService;
import it.polito.ai.backend.services.team.*;
import it.polito.ai.backend.services.vm.*;
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
import javax.validation.constraints.NotNull;
import javax.xml.ws.Response;
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
import java.util.Optional;
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
        List<CourseDTO> courses = teamService.getAllCourses().stream().map(ModelHelper::enrich).collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).all()).withSelfRel();
        return CollectionModel.of(courses, selfLink);
    }

    @GetMapping("/{courseId}")
    CourseDTO getOne(@PathVariable @NotBlank String courseId) {
        return ModelHelper.enrich(teamService.getCourse(courseId).orElseThrow(() -> new CourseNotFoundException(courseId)));
    }

    @GetMapping("/{courseId}/teams/{teamId}")
    TeamDTO getTeam(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId) {
        TeamDTO teamDTO = teamService.getTeam(courseId, teamId).orElseThrow(() -> new TeamNotFoundException(teamId.toString()));
        return ModelHelper.enrich(teamDTO, courseId);
    }

    @GetMapping("/{courseId}/enrolled")
    CollectionModel<StudentDTO> enrolledStudents(@PathVariable @NotBlank String courseId) {
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).enrolledStudents(courseId)).withSelfRel();
        List<StudentDTO> enrolledStudents = teamService.getEnrolledStudents(courseId).stream().map(ModelHelper::enrich).collect(Collectors.toList());
        return CollectionModel.of(enrolledStudents, selfLink);
    }

    @GetMapping("/{courseId}/teams/{teamId}/members")
    CollectionModel<StudentDTO> getMembers(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId) {
        List<StudentDTO> students = teamService.getMembers(courseId, teamId).stream().map(ModelHelper::enrich).collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getMembers(courseId, teamId)).withSelfRel();
        return CollectionModel.of(students, selfLink);
    }

    @GetMapping("/{courseId}/teams")
    CollectionModel<TeamDTO> getTeams(@PathVariable @NotBlank String courseId) {
        List<TeamDTO> teams = teamService.getTeamsForCourse(courseId).stream().map(t -> ModelHelper.enrich(t, courseId)).collect(Collectors.toList());
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
        // todo
        teamService.deleteCourse(courseId);
    }

    @PutMapping("/{courseId}")
    CourseDTO updateCourseName(@RequestBody Map<String, String> map, @PathVariable @NotBlank String courseId){
        if (!map.containsKey("name")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "please provide a name");
        }
        String name = map.get("name");
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "please provide a valid name");
        }
        CourseDTO courseDTO = teamService.getCourse(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        courseDTO.setName(name);
        if (!teamService.updateCourse(courseDTO))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "course name not unique");
        return ModelHelper.enrich(courseDTO);
    }


    @PutMapping("/{courseId}/setCourse")
    CourseDTO updateCourse(@RequestBody Map<String, String> map, @PathVariable @NotBlank String courseId){
        if (!map.containsKey("name") && !map.containsKey("min") && !map.containsKey("max") && !map.containsKey("enabled")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid request");
        }
        String name = map.get("name");
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "please provide a valid name");
        }
        int min = Integer.parseInt(map.get("min"));
        int max = Integer.parseInt(map.get("max"));

        // todo check: if I send enabled=random it gives me false and does not throw any exception
        boolean enabled = Boolean.parseBoolean(map.get("enabled"));
        CourseDTO courseDTO = teamService.getCourse(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        courseDTO.setName(name);
        courseDTO.setMin(min);
        courseDTO.setMax(max);
        courseDTO.setEnabled(enabled);
        if (!teamService.updateCourse(courseDTO))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "todo message");
        return ModelHelper.enrich(courseDTO);

    }

    @PostMapping({"", "/"})
    CourseDTO addCourse(@RequestBody @Valid CourseDTO courseDTO) {
        if (!teamService.addCourse(courseDTO)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("course %s already exists", courseDTO.getId()));
        }
        return ModelHelper.enrich(courseDTO);
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
    @ResponseStatus(HttpStatus.OK)
    void enable(@PathVariable @NotBlank String courseId) {
        teamService.enableCourse(courseId);
    }

    @PostMapping("/{courseId}/disable")
    @ResponseStatus(HttpStatus.OK)
    void disable(@PathVariable @NotBlank String courseId) {
        teamService.disableCourse(courseId);
    }


    // http -v POST http://localhost:8080/API/courses/ai/enrollOne studentId=265000
    @PostMapping("/{courseId}/enrollOne")
    @ResponseStatus(HttpStatus.CREATED)
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

    @PostMapping("/{courseId}/teachers")
    @ResponseStatus(HttpStatus.CREATED)
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
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "invalid file content");
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
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "invalid file content");
        }


    }

    // http -v POST http://localhost:8080/API/courses/ase/createTeam teamName=aseTeam0 memberIds:=[\"264000\",\"264001\",\"264002\",\"264004\"]
    @PostMapping("/{courseId}/teams")
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
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "invalid file content");
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

    @PostMapping("/{courseId}/model")
    @ResponseStatus(HttpStatus.CREATED)
    VirtualMachineModelDTO addVirtualMachineModel(@PathVariable @NotBlank String courseId, @RequestBody @Valid VirtualMachineModelDTO model) {
        VirtualMachineModelDTO virtualMachineModel = virtualMachineService.createVirtualMachineModel(courseId, model);
        return ModelHelper.enrich(virtualMachineModel, courseId);
    }

    @DeleteMapping("/{courseId}/model")
    void deleteVirtualMachineModel(@PathVariable @NotBlank String courseId) {
        if (!virtualMachineService.deleteVirtualMachineModel(courseId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "please turn off all the virtual machines using this model");
        }
    }

    @GetMapping("/{courseId}/model")
    VirtualMachineModelDTO getVirtualMachineModel(@PathVariable @NotBlank String courseId) {
        return ModelHelper.enrich(virtualMachineService.getVirtualMachineModelForCourse(courseId).orElseThrow(() -> new VirtualMachineModelNotDefinedException(courseId)), courseId);
    }

    @GetMapping("/{courseId}/teams/{teamId}/virtual-machines/{vmId}")
    VirtualMachineDTO getVirtualMachine(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId, @PathVariable @NotNull Long vmId) {
        VirtualMachineDTO virtualMachineDTO = virtualMachineService.getVirtualMachine(courseId, teamId, vmId).orElseThrow(() -> new VirtualMachineNotFoundException(vmId.toString()));
        return ModelHelper.enrich(virtualMachineDTO, courseId, teamId);
    }

    @GetMapping("/{courseId}/teams/{teamId}/virtual-machines/{vmId}/owners")
    CollectionModel<StudentDTO> getOwners(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId, @PathVariable @NotNull Long vmId) {
        List<StudentDTO> studentDTOList = virtualMachineService.getOwnersForVirtualMachine(courseId, teamId, vmId).stream().map(ModelHelper::enrich).collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getOwners(courseId, teamId, vmId)).withSelfRel();
        return CollectionModel.of(studentDTOList, selfLink);
    }


    @PostMapping("/{courseId}/teams/{teamId}/virtual-machines")
    @ResponseStatus(HttpStatus.CREATED)
    VirtualMachineDTO addVirtualMachine(@RequestBody @Valid VirtualMachineDTO virtualMachineDTO, @PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId) {

        /**
         * if the studentId has been provided in the request body
         * it should be validated by looking it matches with the id of the authenticated user
         */
        String studentId = "s1"; // todo to be obtained from security context
        VirtualMachineDTO virtualMachine = virtualMachineService.createVirtualMachine(courseId, teamId, studentId, virtualMachineDTO);
        return ModelHelper.enrich(virtualMachine, courseId, teamId);


    }

    @DeleteMapping("/{courseId}/teams/{teamId}/virtual-machines/{vmId}")
    void deleteVirtualMachine(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId, @PathVariable @NotNull Long vmId) {
        if (!virtualMachineService.deleteVirtualMachine(courseId, teamId, vmId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "please turn off the virtual machine before deleting it");
        }

    }

    @PutMapping("/{courseId}/teams/{teamId}/virtual-machines/{vmId}")
    VirtualMachineDTO setVirtualMachine(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId, @PathVariable @NotNull Long vmId, @RequestBody @Valid VirtualMachineDTO virtualMachineDTO) {
        VirtualMachineDTO newVirtualMachine = virtualMachineService.updateVirtualMachine(courseId, teamId, vmId, virtualMachineDTO);
        return ModelHelper.enrich(newVirtualMachine, courseId, teamId);
    }

    @PostMapping("/{courseId}/teams/{teamId}/virtual-machines/{vmId}/on")
    @ResponseStatus(HttpStatus.OK)
    void turnOn(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId, @PathVariable @NotNull Long vmId) {
        virtualMachineService.turnOnVirtualMachine(courseId, teamId, vmId);
    }

    @PostMapping("/{courseId}/teams/{teamId}/virtual-machines/{vmId}/off")
    @ResponseStatus(HttpStatus.OK)
    void turnOff(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId, @PathVariable @NotNull Long vmId) {
        virtualMachineService.turnOffVirtualMachine(courseId, teamId, vmId);
    }

    @GetMapping("/{courseId}/teams/{teamId}/virtual-machines")
    CollectionModel<VirtualMachineDTO> getVirtualMachines(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId) {
        List<VirtualMachineDTO> virtualMachineDTOList = virtualMachineService.getVirtualMachinesForTeam(courseId, teamId);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(CourseController.class).getVirtualMachines(courseId, teamId)).withSelfRel();
        return CollectionModel.of(virtualMachineDTOList, selfLink);
    }

    @PostMapping("/{courseId}/teams/{teamId}/virtual-machines/{vmId}/owners")
    @ResponseStatus(HttpStatus.OK)
    void shareOwnership(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId, @PathVariable @NotNull Long vmId, @RequestBody Map<String, String> map) {
        if (!map.containsKey("studentId")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        String studentId = map.get("studentId");

        /**
         * if studentId is null, the service will throw a StudentNotFoundException but it should be a bad request
         */
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (!virtualMachineService.addOwnerToVirtualMachine(courseId, teamId, studentId, vmId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "cannot share ownership with "+studentId);
        }
    }

    @PostMapping("/{courseId}/teams/{teamId}/configuration")
    @ResponseStatus(HttpStatus.CREATED)
    ConfigurationDTO addConfiguration(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId, @RequestBody @Valid ConfigurationDTO configurationDTO) {
        ConfigurationDTO newConfigurationDTO = virtualMachineService.createConfiguration(courseId, teamId, configurationDTO);
        return ModelHelper.enrich(newConfigurationDTO, courseId, teamId);
    }

    @GetMapping("/{courseId}/teams/{teamId}/configuration")
    ConfigurationDTO getConfiguration(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId) {
        return ModelHelper.enrich(virtualMachineService.getConfigurationForTeam(courseId, teamId).orElseThrow(() -> new ConfigurationNotDefinedException(teamId.toString())), courseId, teamId);
    }

    @PutMapping("/{courseId}/teams/{teamId}/configuration")
    ConfigurationDTO setConfiguration(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId, @RequestBody @Valid ConfigurationDTO configurationDTO) {
        ConfigurationDTO newConfigurationDTO = virtualMachineService.updateConfiguration(courseId, teamId, configurationDTO);
        return ModelHelper.enrich(newConfigurationDTO, courseId, teamId);
    }

    @GetMapping("/{courseId}/teams/{teamId}/virtual-machines/active-cpu")
    int getActiveVcpu(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId) {
        return virtualMachineService.getActiveVcpuForTeam(courseId, teamId);
    }

    @GetMapping("/{courseId}/teams/{teamId}/virtual-machines/active-disk-space")
    int getActiveDiskSpace(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId) {
        return virtualMachineService.getActiveDiskSpaceForTeam(courseId, teamId);
    }

    @GetMapping("/{courseId}/teams/{teamId}/virtual-machines/active-ram")
    int getActiveRam(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId) {
        return virtualMachineService.getActiveRAMForTeam(courseId, teamId);
    }

    @GetMapping("/{courseId}/teams/{teamId}/virtual-machines/tot")
    int getCountVirtualMachines(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId) {
        return virtualMachineService.getCountVirtualMachinesForTeam(courseId, teamId);
    }

    @GetMapping("/{courseId}/teams/{teamId}/virtual-machines/tot-on")
    int getCountActiveVirtualMachines(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId) {
        return virtualMachineService.getCountActiveVirtualMachinesForTeam(courseId, teamId);
    }

    @GetMapping("/{courseId}/teams/{teamId}/virtual-machines/resources")
    Map<String, Integer> getResources(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long teamId) {
        return virtualMachineService.getResourcesByTeam(courseId, teamId);
    }

    @GetMapping("/{courseId}/exercises/{exerciseId}")
    ExerciseDTO getExercise(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long exerciseId) {
        ExerciseDTO exerciseDTO = exerciseService.getExercise(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId.toString()));
        Optional<CourseDTO> courseDTO = teamService.getCourse(courseId);
        if(!courseDTO.isPresent())
            throw new CourseNotFoundException(courseId);
        String courseName = courseDTO.get().getName();
        return ModelHelper.enrich(exerciseDTO, courseName);
    }

    @GetMapping("/{courseId}/exercises/{exerciseId}/assignments")
    List<AssignmentDTO> getLastAssignments(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long exerciseId ){

        // todo collection model
        Optional<CourseDTO> courseDTO = teamService.getCourse(courseId);
        if(!courseDTO.isPresent())
            throw new CourseNotFoundException(exerciseId.toString());
        List<StudentDTO> students = teamService.getEnrolledStudents(courseDTO.get().getId());
        List<AssignmentDTO> lastAssignments = new ArrayList<AssignmentDTO>();
        for (StudentDTO student:students) {
            AssignmentDTO lastAssignment = exerciseService.getAssignmentsForStudent(student.getId())
                    .stream().reduce((a1,a2)-> a2).orElse(null);
            if(lastAssignment==null)
                throw  new AssignmentNotFoundException(student.getId());
            lastAssignments.add(lastAssignment);

        }
        List<AssignmentDTO> assignmentDTOS= new ArrayList<>();
        for (AssignmentDTO a:lastAssignments) {
            String studentId = exerciseService.getStudentForAssignment(a.getId()).map(StudentDTO::getId).orElseThrow( () -> new StudentNotFoundException(a.getId().toString()));
            assignmentDTOS.add(ModelHelper.enrich(a,studentId,exerciseId,courseId));

        }

        return  assignmentDTOS;
    }

    @GetMapping("/{courseId}/exercises/{exerciseId}/history")
    List<AssignmentDTO> getHistoryAssignments(@PathVariable @NotBlank String courseId,
            @PathVariable @NotNull Long exerciseId,@RequestBody Map<String,String> map ){

        // todo collection model
        if(!map.containsKey("studentId")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        String studentId1 = map.get("studentId");

        if (studentId1 == null || studentId1.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Optional<CourseDTO> courseDTO = teamService.getCourse(courseId);
        if(!courseDTO.isPresent())
            throw new CourseNotFoundException(courseId);
        List<AssignmentDTO> assignmentDTOS =
                exerciseService.getAssignmentByStudentAndExercise(map.get("studentId"),exerciseId);
        System.out.println(assignmentDTOS.size());
        List<AssignmentDTO> assignmentDTOList = new ArrayList<>();
        for (AssignmentDTO a:assignmentDTOS) {
            String studentId = exerciseService.getStudentForAssignment(a.getId()).map(StudentDTO::getId).orElseThrow( () -> new StudentNotFoundException(a.getId().toString()));
            assignmentDTOList.add(ModelHelper.enrich(a,studentId,exerciseId,courseId));

        }
        return  assignmentDTOList;

    }




    @PostMapping("/{courseId}/exercises/{exerciseId}/assignmentNull")
    void setNullAssignment(@PathVariable @NotBlank String courseId, @PathVariable @NotNull Long exerciseId){
        /*No duplicati*/
        List<AssignmentDTO> assignments = exerciseService.getAssignmentsForExercise(exerciseId);
        if(!assignments.isEmpty())
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exerciseId.toString());
        /*Per ogni studente iscritto al corso aggiungere un'elaborato con stato null*/
        Optional<CourseDTO> courseDTO = teamService.getCourse(courseId);
        if(!courseDTO.isPresent())
            throw new CourseNotFoundException(courseId);
        Optional<ExerciseDTO> exercise = exerciseService.getExercise(exerciseId);
        if(!exercise.isPresent())
            throw  new ExerciseNotFoundException(exerciseId.toString());
        List<StudentDTO> students = teamService.getEnrolledStudents(courseDTO.get().getId());
        for (StudentDTO student:students) {
            exerciseService.addAssignmentByte(
                    Utils.getNow(),
                    AssignmentStatus.NULL,
                    true,null,exercise.get().getImage(),student.getId(),exerciseId);
        }
    }

    @PostMapping("/{courseId}/exercises/{exerciseId}/assignmentRead")
    void setReadAssignment(@PathVariable @NotBlank String courseId,
            @PathVariable @NotNull Long exerciseId, @RequestBody Map<String,String> map){
        if (!map.containsKey("studentId")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        String studentId = map.get("studentId");

        if (studentId == null || studentId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Optional<CourseDTO> courseDTO = teamService.getCourse(courseId);
        if(!courseDTO.isPresent())
            throw new CourseNotFoundException(courseId);
        List<AssignmentDTO> assignments = exerciseService.getAssignmentByStudentAndExercise(map.get("studentId"),exerciseId);
        AssignmentDTO assignment = assignments.stream().reduce((a1,a2)-> a2).orElse(null);

        if(assignment==null)
            throw  new AssignmentNotFoundException(map.get("studentId"));

        Byte[] image = assignment.getImage();
        if(assignment.getStatus()==AssignmentStatus.NULL ||
                (assignment.getStatus()==AssignmentStatus.RIVSTO && assignment.isFlag()))
            exerciseService.addAssignmentByte(Utils.getNow(),
                    AssignmentStatus.LETTO,true,null,image,map.get("studentId"),exerciseId);
        else
            throw new ResponseStatusException(HttpStatus.CONFLICT, exerciseId.toString());
    }

    @PostMapping("/{courseId}/exercises/{exerciseId}/assignments")
    void submitAssignment(
            @RequestParam("image") MultipartFile file, @RequestParam Map<String, String> map, @PathVariable @NotBlank String courseId, @PathVariable @NotNull Long exerciseId){
        /*Lo studente può caricare solo una soluzione prima che il docente gli dia il permesso per rifralo*/

        if (!map.containsKey("studentId")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        String studentId = map.get("studentId");

        if (studentId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        try {
            Optional<CourseDTO> courseDTO = teamService.getCourse(courseId);
            if(!courseDTO.isPresent())
                throw new CourseNotFoundException(courseId);
            Utils.checkTypeImage(file);
            Optional<ExerciseDTO> exercise = exerciseService.getExercise(exerciseId);
            if(!exercise.isPresent())
                throw  new ExerciseNotFoundException(exerciseId.toString());

            List<AssignmentDTO> assignments = exerciseService.getAssignmentByStudentAndExercise(map.get("studentId"),exerciseId);
            AssignmentDTO assignment = assignments.stream().reduce((a1,a2)-> a2).orElse(null);
            if(assignment==null)
                throw  new AssignmentNotFoundException(map.get("studentId"));

            if(exercise.get().getExpired().after(Utils.getNow()) && assignment.isFlag() && assignment.getStatus()==AssignmentStatus.LETTO)
                exerciseService.addAssignmentByte(Utils.getNow(),AssignmentStatus.CONSEGNATO,false,null,Utils.getBytes(file),map.get("studentId"),exerciseId);
            else
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exerciseId.toString());
        } catch (TikaException | IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "invalid file content");
        }


    }

    @PostMapping("/{courseId}/exercises/{exerciseId}/assignmentReview")
    void reviewAssignment(@RequestParam("image") MultipartFile file, @RequestParam Map<String, String> map, @PathVariable @NotBlank String courseId, @PathVariable @NotNull Long exerciseId){
        /*Se il falg=false allora c'è anche il voto
         * se è true allora non c'è il voto*/
        if(!map.containsKey("flag") && !map.containsKey("studentId")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        String studentId = map.get("studentId");

        if (studentId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        try {
            Optional<CourseDTO> courseDTO = teamService.getCourse(courseId);
            if(!courseDTO.isPresent())
                throw new CourseNotFoundException(courseId);
            Utils.checkTypeImage(file);
            List<AssignmentDTO> assignments = exerciseService.getAssignmentByStudentAndExercise(map.get("studentId"),exerciseId);
            AssignmentDTO assignment = assignments.stream().reduce((a1,a2)-> a2).orElse(null);
            if(assignment==null)
                throw  new AssignmentNotFoundException(map.get("studentId"));

            boolean flag =  Boolean.parseBoolean(map.get("flag"));
            if(assignment.getStatus()==AssignmentStatus.CONSEGNATO){
                if(flag)
                    exerciseService.addAssignmentByte(Utils.getNow(),AssignmentStatus.RIVSTO,
                            flag,null,Utils.getBytes(file),map.get("studentId"),exerciseId);
                else {
                    if(map.containsKey("score")){

                        Integer score = Integer.parseInt(map.get("score"));
                        System.out.println(score);
                        exerciseService.addAssignmentByte(Utils.getNow(),AssignmentStatus.RIVSTO,
                                flag,score,Utils.getBytes(file),map.get("studentId"),exerciseId);
                    }else {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                    }
                }
            }
        } catch (TikaException | IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "invalid file content");
        }

    }

    @GetMapping("/{courseId}/exercises/{exerciseId}/assignments/{assignmentId}")
    AssignmentDTO getAssignment(@PathVariable @NotNull Long assignmentId, @PathVariable @NotBlank String courseId, @PathVariable @NotNull Long exerciseId) {
        Optional<CourseDTO> courseDTO = teamService.getCourse(courseId);
        if(!courseDTO.isPresent())
            throw  new CourseNotFoundException(courseId);
        Optional<ExerciseDTO> exerciseDTO = exerciseService.getExercise(exerciseId);
        if(!exerciseDTO.isPresent())
            throw  new ExerciseNotFoundException(exerciseId.toString());
        AssignmentDTO assignmentDTO = exerciseService.getAssignment(assignmentId).orElseThrow(() -> new AssignmentNotFoundException(assignmentId.toString()));
        String studentId = exerciseService.getStudentForAssignment(assignmentId).map(StudentDTO::getId).orElseThrow( () -> new StudentNotFoundException(assignmentId.toString()));
        return ModelHelper.enrich(assignmentDTO,studentId,exerciseId,courseId);
    }

}
