package it.polito.ai.backend.services.notification;

import it.polito.ai.backend.dtos.StudentDTO;
import it.polito.ai.backend.entities.*;
import it.polito.ai.backend.repositories.AssignmentRepository;
import it.polito.ai.backend.repositories.CourseRepository;
import it.polito.ai.backend.repositories.ExerciseRepository;
import it.polito.ai.backend.repositories.TokenRepository;
import it.polito.ai.backend.services.Utils;
import it.polito.ai.backend.services.exercise.AssignmentStatus;
import it.polito.ai.backend.services.exercise.ExerciseService;
import it.polito.ai.backend.services.team.CourseNotFoundException;
import it.polito.ai.backend.services.team.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;

@Component
@Transactional
@EnableScheduling
public class ScheduledTasks {

    @Autowired
    TokenRepository tokenRepository;
    @Autowired
    AssignmentRepository assignmentRepository;
    @Autowired
    ExerciseRepository exerciseRepository;
    @Autowired
    CourseRepository courseRepository;

    /*
    * every day at 4am
    * */
    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanTokens() {
        List<Token> tokens = tokenRepository.findAllByExpiryDateBefore(Utils.getNow());
        System.out.println("Found "+tokens.size()+" tokens to be removed");
        tokenRepository.deleteAll(tokens);
        System.out.println("Token repository cleaned");
    }

    /*
     * every day at 4:30am
     * */
    @Scheduled(cron = "0 30 4 * * ?")
    public void expiredAssignment() {
        System.out.println("Conrtollo la scadenza condegne");
        /*Consegne scaduta*/
        List<Exercise> exercises = exerciseRepository.findByExpiredBefore(Utils.getNow());
        System.out.println(exercises.size());
        HashSet<Student> students = new HashSet<Student>();
        List<Assignment> assignments = new ArrayList<Assignment>();
        /*Per ogni consegna scaduta trovo gli studenti iscritti a corse a di cui fa parte la consega*/
        for (Exercise exercise:exercises) {
            Optional<Course> course = courseRepository.findById(exercise.getCourse().getId());
            if(course.isPresent())
                students.addAll(course.get().getStudents());

        }
        System.out.println(students.size());
        /*Per ogni consegna cerco l'ultimo elaborato dello studente*/
        if(!students.isEmpty()){
            for(Exercise exercise:exercises){
                for (Student s:students) {
                    assignments.add( assignmentRepository.findByStudentAndAndExercise(s,exercise)
                            .stream()
                            .sorted(Comparator.comparing(Assignment::getPublished, Timestamp::compareTo))
                    .reduce((a1,a2)->a2).orElse(null));
                 }
            }
        }
        System.out.println(assignments.size());
        for (Assignment a:assignments) {
            /*Se l'elaborato non ha stato consegnato e ha flag true(può essere caricato)
             allora lo carico con stato consegnato e falg false*/
            if(a!=null && a.getStatus()!=AssignmentStatus.CONSEGNATO && a.isFlag()){
                Assignment ac = new Assignment();
                ac.setImage(a.getImage());
                ac.setStudent(a.getStudent());
                ac.setStatus(AssignmentStatus.CONSEGNATO);
                ac.setPublished(Utils.getNow());
                ac.setExercise(a.getExercise());
                ac.setScore(null);
                ac.setFlag(false);
                assignmentRepository.save(ac);
            }
        }
    }

}
