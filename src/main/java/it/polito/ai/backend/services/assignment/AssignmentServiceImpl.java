package it.polito.ai.backend.services.assignment;

import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.entities.*;
import it.polito.ai.backend.repositories.*;
import it.polito.ai.backend.services.team.CourseNotEnabledException;
import it.polito.ai.backend.services.team.CourseNotFoundException;
import it.polito.ai.backend.services.team.StudentNotEnrolledException;
import it.polito.ai.backend.services.team.StudentNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import it.polito.ai.backend.services.Utils;


@Service
@Transactional
public class AssignmentServiceImpl implements AssignmentService {
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    AssignmentRepository assignmentRepository;
    @Autowired
    StudentRepository studentRepository;
    @Autowired
    TeacherRepository teacherRepository;
    @Autowired
    PaperRepository paperRepository;
    @Autowired
    ModelMapper modelMapper;


    @Override
    @PreAuthorize("hasRole('TEACHER') and @securityServiceImpl.isTaught(#courseId)")
    public AssignmentDTO addAssignmentForCourse(String courseId, Timestamp published, Timestamp expired, MultipartFile file) throws IOException {
        Optional<Course> course = courseRepository.findById(courseId);

            if (!course.isPresent()) {
                throw new CourseNotFoundException("Course not fount with id:"+courseId);

            } else if (!course.get().isEnabled()) {
                throw new CourseNotEnabledException("Course with id: "+courseId+" is not enable");
            }
            if(expired.before(Utils.getNow()))
                throw new AssignmentServiceException("Invalid expired time");

            Assignment assignment = new Assignment();
            assignment.setPublished(published);
            assignment.setExpired(expired);
            assignment.setCourse(course.get());
            assignment.setImage(Utils.getBytes(file));
            assignmentRepository.save(assignment);
            return modelMapper.map(assignment,AssignmentDTO.class);

    }

    @Override
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.canOpen(#assignmentId)) or (hasRole('STUDENT') and @securityServiceImpl.canView(#assignmentId) " +
            "and @securityServiceImpl.isDone(#assignmentId) and @securityServiceImpl.isAuthorized(#studentId))")
    public List<PaperDTO> getPaperByStudentAndAssignment(String studentId, Long assignmentId) {
        Optional<Student> student = studentRepository.findById(studentId);
        if(!student.isPresent())
            throw new StudentNotFoundException("Student not found with id: "+studentId);
        Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);
        if(!assignment.isPresent())
            throw new AssignmentNotFoundException("assignment not found for id : "+assignmentId.toString());

        return paperRepository.findByStudentAndAssignment(student.get(),assignment.get())
                .stream()
                .sorted(Comparator.comparing(Paper::getPublished,Timestamp::compareTo))
                .map(a -> modelMapper.map(a, PaperDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasRole('TEACHER') and @securityServiceImpl.canOpen(#assignmentId)")
    public List<PaperDTO> getLastPapers(Long assignmentId) {
        Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);
        if(!assignment.isPresent())
            throw  new AssignmentNotFoundException(assignmentId.toString());
        Course course = assignment.get().getCourse();
        List<Student> students = course.getStudents();
        List<Paper> lastPapers = new ArrayList<>();

        for (Student student:students) {
            Paper lastPaper = paperRepository.findByStudentAndAssignment(student,assignment.get())
                    .stream()
                    .sorted(Comparator.comparing(Paper::getPublished,Timestamp::compareTo))
                    .reduce((a1,a2)-> a2).orElse(null);
            if(lastPaper != null)
                lastPapers.add(lastPaper);
        }


        return lastPapers.stream()
                .map(a -> modelMapper.map(a, PaperDTO.class))
                .collect(Collectors.toList());

    }


    @Override
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.canOpen(#assignmentId)) or (hasRole('STUDENT') and @securityServiceImpl.canView(#assignmentId))")
    public Optional<AssignmentDTO> getAssignment(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .map(e -> modelMapper.map(e, AssignmentDTO.class));
    }

    @Override
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.isReview(#paperId)) or (hasRole('STUDENT') and @securityServiceImpl.isAuthor(#paperId))")
    public Optional<PaperDTO> getPaper(Long paperId) {
        return paperRepository.findById(paperId)
                .map(a -> modelMapper.map(a, PaperDTO.class));

    }

    @Override
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.isReview(#paperId)) or (hasRole('STUDENT') and @securityServiceImpl.isAuthor(#paperId))")
    public Optional<AssignmentDTO> getAssignmentForPaper(Long paperId) {
        return paperRepository.findById(paperId)
                .map(a -> modelMapper.map(a.getAssignment(), AssignmentDTO.class));

    }


    @Override
    @PreAuthorize ("(hasRole('TEACHER') and @securityServiceImpl.isTaught(#courseId)) or (hasRole('STUDENT') and @securityServiceImpl.isEnrolled(#courseId))")
    public List<AssignmentDTO> getAssignmentsForCourse(String courseId) {
        Optional<Course> course = courseRepository.findById(courseId);
        if(!course.isPresent()){
            throw new CourseNotFoundException("Course not found with id: "+courseId);
        }
        return course.get()
                .getAssignments().stream()
                .map(e -> modelMapper.map(e, AssignmentDTO.class ))
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasRole('STUDENT') and @securityServiceImpl.canView(#assignmentId)")
    public Optional<CourseDTO> getCourse(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .map(e -> modelMapper.map(e.getCourse(), CourseDTO.class));
    }


    @Override
    @PreAuthorize("hasRole('TEACHER') and @securityServiceImpl.canOpen(#paperId)")
    public void setPapersNullForAssignment(Long paperId) {
        Optional<Assignment> exercise = assignmentRepository.findById(paperId);
        if(!exercise.isPresent())
            throw  new AssignmentNotFoundException(paperId.toString());
        // For each student enrolled to the course add an paper with state null
        /* There must be no others papers*/
       List<Student> students= exercise.get().getCourse().getStudents();
        for (Student student:students) {
            List<Paper> paper = paperRepository.findByStudentAndAssignment(student,exercise.get());
            if(paper.isEmpty()){
                addPaperByte(
                        Utils.getNow(),
                        PaperStatus.NULL,
                        true,null,exercise.get().getImage(),student.getId(),paperId);
            }
        }

    }



    @Override
    @PreAuthorize("hasRole('STUDENT') and @securityServiceImpl.canView(#assignmentId) and @securityServiceImpl.isDone(#assignmentId) and @securityServiceImpl.isAuthorized(#studentId)")
    public boolean setPapersReadForStudentAndAssignment(Long assignmentId, String studentId) {
        Optional<Student> student = studentRepository.findById(studentId);
        if(!student.isPresent())
            throw new StudentNotFoundException("Student not found with id:"+ studentId);
        Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);
        if(!assignment.isPresent())
            throw new AssignmentNotFoundException("assignment not found with id:"+assignmentId.toString());
        Paper paper = paperRepository.findByStudentAndAssignment(student.get(),assignment.get())
                .stream()
                .sorted(Comparator.comparing(Paper::getPublished,Timestamp::compareTo))
                .reduce((a1,a2)-> a2).orElse(null);

        if(paper ==null)
            throw  new PaperNotFoundException(studentId);

        byte[] image = paper.getImage();
        if(paper.getStatus()== PaperStatus.NULL ||
                (paper.getStatus()== PaperStatus.REVISED && paper.isFlag())) {
           addPaperByte(Utils.getNow(), PaperStatus.READ,true,null,image,studentId,assignmentId);
            return  true;
        }


        else return false;
    }

    @Override
    @PreAuthorize("hasRole('STUDENT') and @securityServiceImpl.canView(#assignmentId) and @securityServiceImpl.isDone(#assignmentId) and @securityServiceImpl.isAuthorized(#studentId)")
    public boolean checkPaper(Long assignmentId, String studentId){

        /*The student can only upload a solution before the teacher gives him permission to do it again */
        Optional<Student> student = studentRepository.findById(studentId);
        if(!student.isPresent())
            throw new StudentNotFoundException("Student not found with id:"+studentId);
        Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);
        if(!assignment.isPresent())
            throw new AssignmentNotFoundException("assignment not found with id:"+assignmentId.toString());
        Paper paper = paperRepository.findByStudentAndAssignment(student.get(),assignment.get())
                .stream()
                .sorted(Comparator.comparing(Paper::getPublished,Timestamp::compareTo))
                .reduce((a1,a2)-> a2).orElse(null);
        if(paper ==null)
            throw  new PaperNotFoundException(studentId);
        return assignment.get().getExpired().after(Utils.getNow()) && paper.isFlag() && paper.getStatus() == PaperStatus.READ;

    }

    @Override
    public Optional<StudentDTO> getStudentForPaper(Long assignmentId) {
        return paperRepository.findById(assignmentId)
               .map(a -> modelMapper.map(a.getStudent(), StudentDTO.class));
    }





    @Override
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.canOpen(#assignmentId)) or (hasRole('STUDENT') and @securityServiceImpl.canView(#assignmentId) and @securityServiceImpl.isDone(#assignmentId) and @securityServiceImpl.isAuthorized(#studentId))")
    public PaperDTO addPaperByte(Timestamp published, PaperStatus state, boolean flag, String score, byte[] image, String studentId, Long assignmentId) {
        Optional<Student> student = studentRepository.findById(studentId);
        if(!student.isPresent())
            throw  new StudentNotFoundException(studentId);
        Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);
        if(!assignment.isPresent())
            throw  new AssignmentNotFoundException(assignmentId.toString());

        if(!student.get().getCourses().contains(assignment.get().getCourse()))
            throw  new StudentNotEnrolledException(studentId);

        Paper paper = new Paper();
        paper.setScore(score);
        paper.setFlag(flag);
        paper.setStatus(state);
        paper.setAssignment(assignment.get());
        paper.setPublished(published);
        paper.setStudent(student.get());
        paper.setImage(image);
        paperRepository.save(paper);
        return modelMapper.map(paper, PaperDTO.class);

    }




}

