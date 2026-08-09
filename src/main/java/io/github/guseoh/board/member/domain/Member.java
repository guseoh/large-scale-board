package io.github.guseoh.board.member.domain;

import io.github.guseoh.board.global.entity.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "members")
public class Member extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;

    protected Member() { }
    public Member(String username, String email, String password) {
        this.username = username; this.email = email; this.password = password;
    }
    public void update(String email) { this.email = email; }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
