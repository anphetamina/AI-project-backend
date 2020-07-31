package it.polito.ai.backend.services.team;

public class TeacherNotFoundException extends TeamServiceNotFoundException {
    public TeacherNotFoundException(String teacherId) {
        super(String.format("teacher %s not found", teacherId));
    }
}
