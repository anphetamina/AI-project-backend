package it.polito.ai.backend.security;

public interface SecurityService {
    boolean isAuthorized(String id);
    boolean isEnrolled(String courseId);
    boolean isPartOf(Long teamId);
    boolean isTaught(String courseId);
    String getId();
}
