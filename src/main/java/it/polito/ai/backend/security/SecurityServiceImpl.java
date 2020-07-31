package it.polito.ai.backend.security;

import it.polito.ai.backend.entities.User;
import it.polito.ai.backend.repositories.CourseRepository;
import it.polito.ai.backend.repositories.TeacherRepository;
import it.polito.ai.backend.repositories.TeamRepository;
import it.polito.ai.backend.services.team.CourseNotFoundException;
import it.polito.ai.backend.services.team.TeamNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class SecurityServiceImpl implements SecurityService {

    @Autowired
    CourseRepository courseRepository;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    TeacherRepository teacherRepository;

    @Override
    public boolean isAuthorized(String id) {
        String userId = this.getId();
        if (userId != null) {
            return userId.equalsIgnoreCase(id);
        }
        return false;
    }

    @Override
    public boolean isEnrolled(String courseId) {
        String userId = this.getId();
        if (userId != null) {
            return courseRepository.findById(courseId)
                    .orElseThrow(() -> new CourseNotFoundException(courseId))
                    .getStudents()
                    .stream()
                    .anyMatch(s -> s.getId().equalsIgnoreCase(userId));
        }
        return false;
    }

    @Override
    public boolean isPartOf(Long teamId) {
        String userId = this.getId();
        if (userId != null) {
            return teamRepository.findById(teamId)
                    .orElseThrow(() -> new TeamNotFoundException(teamId.toString()))
                    .getMembers()
                    .stream()
                    .anyMatch(s -> s.getId().equalsIgnoreCase(userId));
        }
        return false;
    }

    @Override
    public boolean isTaught(String courseId) {
        String userId = this.getId();
        if (userId != null) {
            return courseRepository.findById(courseId)
                    .orElseThrow(() -> new CourseNotFoundException(courseId))
                    .getTeachers()
                    .stream()
                    .anyMatch(s -> s.getId().equalsIgnoreCase(userId));
        }
        return false;
    }

    @Override
    public String getId() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            String username = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
            int index = username.indexOf('@');
            return username.substring(0, index);
        }
        return null;
    }
}
