package ktb3.fullstack.week4.domain.likes;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import ktb3.fullstack.week4.domain.SoftDeletetionEntity;

@Entity
public class Like extends SoftDeletetionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private boolean isLiked;
}
