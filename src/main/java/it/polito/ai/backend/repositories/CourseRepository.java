package it.polito.ai.backend.repositories;

import it.polito.ai.backend.entities.Course;
import it.polito.ai.backend.entities.Student;
import it.polito.ai.backend.entities.VirtualMachineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
    @Query("SELECT s FROM Student s INNER JOIN s.teams t INNER JOIN t.course c WHERE c.name=:courseName")
    List<Student> getStudentsInTeams(String courseName);
    @Query("SELECT s FROM Student s INNER JOIN s.courses c WHERE c.name=:courseName AND s NOT IN (SELECT s FROM Student s INNER JOIN s.teams t INNER JOIN t.course c WHERE c.name=:courseName)")
    // @Query("SELECT s FROM Student s JOIN s.courses c LEFT OUTER JOIN s.teams t ON t.course.name=:courseName WHERE c.name=:courseName AND t.id IS NULL")
    List<Student> getStudentsNotInTeams(String courseName);

    /*
    select s.id, s.name, s.first_name
    from student s, course c, student_course sc
    where sc.student_id=s.id and sc.course_name=c.name and c.name='applicazioni internet' and s.id not in
      (
          select s.id
          from student s,
               course c,
               team t,
               student_course sc,
               team_student ts
          where s.id = sc.student_id
            and c.name = sc.course_name
            and ts.student_id = s.id
            and ts.team_id = t.id
            and t.course_id = c.name
            and c.name = 'applicazioni internet'
      )
     */

    @Query("select count(v) from Course c inner join c.teams t inner join t.virtualMachines v where c.name=:courseName and v.status=:status")
    int countVirtualMachinesByCourseAndStatus(String courseName, VirtualMachineStatus status);
}
