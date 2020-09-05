package it.polito.ai.backend.entities;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Assignment {
    @Id
    @GeneratedValue
    Long id;
    Timestamp published;
    Timestamp expired;
    @Lob
    private byte[] image;
    @ManyToOne
    @JoinColumn(name = "course_id")
    Course course;

    public void setCourse(Course course) {
        if (this.course != null) {
            this.course.getAssignments().remove(this);
        }
        this.course = course;
        if (course != null) {
            course.getAssignments().add(this);
        }
    }

    @OneToMany(mappedBy = "assignment")
    private List<Paper> papers =new ArrayList<Paper>();
    public void addPaper(Paper paper) {
        paper.assignment =this;
        papers.add(paper);

    }

    public void removePaper(Paper paper) {
        papers.remove(paper);
        paper.assignment = null;
    }
}
