package io.github.guseoh.board.member.web;

import io.github.guseoh.board.member.MemberService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService service;

    public MemberController(MemberService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberRequest.Create request) {
        MemberResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/members/" + response.id())).body(response);
    }

    @GetMapping("/me")
    public MemberResponse me(@AuthenticationPrincipal UserDetails user) {
        return service.me(user.getUsername());
    }

    @PutMapping("/me")
    public MemberResponse update(@AuthenticationPrincipal UserDetails user,
                                 @Valid @RequestBody MemberRequest.Update request) {
        return service.update(user.getUsername(), request);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails user) {
        service.delete(user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
