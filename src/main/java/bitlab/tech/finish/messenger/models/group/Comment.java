package bitlab.tech.finish.messenger.models.group;


import bitlab.tech.finish.messenger.models.User;
import jakarta.persistence.*;

@Entity
@Table(name = "t_groups")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text")
    private String text;

    @ManyToOne
    private User author;

    @ManyToOne
    private GroupPost post;

}
