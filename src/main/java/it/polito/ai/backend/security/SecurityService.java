package it.polito.ai.backend.security;

public interface SecurityService {
    boolean isStudentAuthorized(String id);
    boolean isTeacherAuthorized(String id);
    boolean isEnrolled(String courseName);
    boolean isPartOf(Long teamId);
    boolean isTaught(String courseName);
    Long getId(String email);
}
