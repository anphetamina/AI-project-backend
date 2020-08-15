package it.polito.ai.backend.services.notification;

import it.polito.ai.backend.entities.*;
import it.polito.ai.backend.repositories.*;
import it.polito.ai.backend.security.CustomUserDetailsService;
import it.polito.ai.backend.services.Utils;
import it.polito.ai.backend.dtos.PaperStatus;
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
    PaperRepository paperRepository;
    @Autowired
    AssignmentRepository exerciseRepository;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    TeamService teamService;
    @Autowired
    JwtBlackListRepository jwtBlackListRepository;
    @Autowired
    ConfirmationTokenRepository confirmationTokenRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CustomUserDetailsService userService;

    /*
    * every day at 4am
    * */
    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanTokensAndProposeTeam() {
        List<Token> tokens = tokenRepository.findAllByExpiryDateBefore(Utils.getNow());
        System.out.println("Found "+tokens.size()+" tokens to be removed");
        tokens.forEach(token -> {
                    if(!token.getStatus().equals(TokenStatus.ACCEPT))
                        teamService.evictTeam(token.getTeamId()); });
        tokenRepository.deleteAll(tokens);
        System.out.println("Token repository cleaned and alla proposes team");
    }

    /*
     * every day at 4:00am
     * */
    @Scheduled(cron = "0 00 4 * * ?")
    public void expiredAssignment() {
        System.out.println("Conrtollo la scadenza condegne");
        /*Consegne scaduta*/
        List<Assignment> assignments = exerciseRepository.findByExpiredBefore(Utils.getNow());
        System.out.println(assignments.size());
        HashSet<Student> students = new HashSet<Student>();
        List<Paper> papers = new ArrayList<Paper>();
        /*Per ogni consegna scaduta trovo gli studenti iscritti a corse a di cui fa parte la consega*/
        for (Assignment assignment : assignments) {
            Optional<Course> course = courseRepository.findById(assignment.getCourse().getId());
            if(course.isPresent())
                students.addAll(course.get().getStudents());

        }
        System.out.println(students.size());
        /*Per ogni consegna cerco l'ultimo elaborato dello studente*/
        if(!students.isEmpty()){
            for(Assignment assignment : assignments){
                for (Student s:students) {
                    papers.add( paperRepository.findByStudentAndAssignment(s, assignment)
                            .stream()
                            .sorted(Comparator.comparing(Paper::getPublished, Timestamp::compareTo))
                    .reduce((a1,a2)->a2).orElse(null));
                 }
            }
        }
        System.out.println(papers.size());
        for (Paper a: papers) {
            /*Se l'elaborato non ha stato consegnato e ha flag true(può essere caricato)
             allora lo carico con stato consegnato e falg false*/
            if(a!=null && a.getStatus()!= PaperStatus.CONSEGNATO && a.isFlag()){
                Paper ac = new Paper();
                ac.setImage(a.getImage());
                ac.setStudent(a.getStudent());
                ac.setStatus(PaperStatus.CONSEGNATO);
                ac.setPublished(Utils.getNow());
                ac.setAssignment(a.getAssignment());
                ac.setScore(null);
                ac.setFlag(false);
                paperRepository.save(ac);
            }
        }
    }

    /*
     * every day at 4:00am
     * */
    @Scheduled(cron = "0 00 04 * * ?")
    public void clearConfirmationToken() {
        System.out.println("Remove all user not confirmed");
        List<ConfirmationToken> listToken = confirmationTokenRepository.findAllByExpiryDateBefore(Utils.getNow());
        System.out.println(listToken.size());
        listToken.forEach(t -> {

                userService.deleteUser(t.getUsername());
        });

        confirmationTokenRepository.deleteAll(listToken);

    }

    /*
     * every day at 4:00am
     * */
    @Scheduled(cron = "0 00 04 * * ?")
    public void clearBlackList() {
        jwtBlackListRepository.deleteAll();
        System.out.println("JwtTokenBlackList is clear");

    }


}
