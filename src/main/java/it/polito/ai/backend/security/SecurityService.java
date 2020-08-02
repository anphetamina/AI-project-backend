package it.polito.ai.backend.security;

public interface SecurityService {
    boolean isAuthorized(String id);
    boolean isEnrolled(String courseId);
    boolean isPartOf(Long teamId);
    boolean isTaught(String courseId);
    boolean canManage(Long configurationId);
    boolean isOwnerOf(Long vmId);
    boolean canUse(Long vmId);
    boolean canConnect(Long vmId);
    boolean isHelping(Long teamId);
    boolean hasDefined(Long modelId);
    boolean canAccess(Long modelId);

    boolean canView(Long exerciseId);
    boolean canOpen(Long exerciseId);
    boolean isDone(Long exerciseId);

    boolean isAuthor(Long assignmentId);
    boolean isReview(Long assignmentId);


    String getId();
}
