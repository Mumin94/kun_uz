package dasturlash.uz.entity;

import dasturlash.uz.enums.EmailType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "email_history")
public class EmailHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(unique = true)
    private String token;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column
    private LocalDateTime expiresAt;

    @Column
    private LocalDateTime usedDate;

    @Column(nullable = false)
    private boolean sent = false;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EmailType type;
}
