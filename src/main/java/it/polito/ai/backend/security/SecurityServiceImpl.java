package it.polito.ai.backend.security;

public class SecurityServiceImpl implements SecurityService {
    @Override
    public boolean isStudentAuthorized(String id) {
        return false;
    }

    @Override
    public boolean isTeacherAuthorized(String id) {
        return false;
    }

    @Override
    public boolean isEnrolled(String courseId) {
        return false;
    }

    @Override
    public boolean isPartOf(Long teamId) {
        return false;
    }

    @Override
    public boolean isTaught(String courseId) {
        return false;
    }

    @Override
    public Long getId(String email) {
        return null;
    }
}
