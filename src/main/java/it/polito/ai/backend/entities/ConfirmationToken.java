package it.polito.ai.backend.entities;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmationToken {
    @Id
    @EqualsAndHashCode.Include
    String id;
    String username;
    Timestamp expiryDate;
}
