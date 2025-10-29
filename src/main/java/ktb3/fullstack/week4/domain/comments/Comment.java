package ktb3.fullstack.week4.domain.comments;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import ktb3.fullstack.week4.domain.BaseModifiedTimeEntity;
import ktb3.fullstack.week4.domain.SoftDeleteBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment extends BaseModifiedTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    private long postId;

    private long authorId;

    private String content;

//    private boolean isDeleted;

}
