package it.polito.ai.backend.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Token {
    @Id
    @EqualsAndHashCode.Include
    String id;
    Long teamId;
    String studentId;
    TokenStatus status;
    Timestamp expiryDate;
}
