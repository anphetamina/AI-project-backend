package it.polito.ai.backend.security;

public interface SecurityService {
    boolean isStudentAuthorized(String id);
    boolean isTeacherAuthorized(String id);
    boolean isEnrolled(String courseId);
    boolean isPartOf(Long teamId);
    boolean isTaught(String courseId);
    Long getId(String email);
}
