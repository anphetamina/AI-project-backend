package it.polito.ai.backend.entities;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Course {
    @Id
    @EqualsAndHashCode.Include
    String id;
    String name;
    int min;
    int max;
    boolean enabled;

    @ManyToMany(mappedBy = "courses", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    List<Student> students = new ArrayList<>();

    public void addStudent(Student student) {
        students.add(student);
        student.courses.add(this);
    }

    @ManyToMany(mappedBy = "courses", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    List<Teacher> teachers = new ArrayList<>();

    public void addTeacher(Teacher teacher) {
        teachers.add(teacher);
        teacher.courses.add(this);
    }

    @OneToMany(mappedBy = "course", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    List<Team> teams = new ArrayList<>();

    public void addTeam(Team team) {
        team.course = this;
        teams.add(team);
    }

    public void removeTeam(Team team) {
        if (team.course != null) {
            team.course = null;
        }
        teams.remove(team);
    }

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @JoinColumn(name = "vm_model")
    VirtualMachineModel virtualMachineModel;

    public void setVirtualMachineModel(VirtualMachineModel virtualMachineModel) {
        if (this.virtualMachineModel != null) {
            this.virtualMachineModel.course = null;
        }
        this.virtualMachineModel = virtualMachineModel;
        if (virtualMachineModel != null) {
            virtualMachineModel.course = this;
        }
    }


    @OneToMany(mappedBy = "course")
    private List<Exercise> exercises =new ArrayList<Exercise>();
    public void addExercise(Exercise exercise) {
        exercise.course = this;
        exercises.add(exercise);
    }
}
