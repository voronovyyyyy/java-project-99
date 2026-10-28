package hexlet.code.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "labels")
public class Label extends BaseEntity {
    @Column(nullable = false, unique = true, length = 1000)
    private String name;
}
