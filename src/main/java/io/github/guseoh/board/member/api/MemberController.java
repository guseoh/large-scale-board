package io.github.guseoh.board.member.api;

import io.github.guseoh.board.member.application.MemberSignUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

	private final MemberSignUpService signUpService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public MemberResponse signUp(@Valid @RequestBody SignUpRequest request) {
		return MemberResponse.from(
				signUpService.signUp(request.email(), request.password(), request.nickname()));
	}
}
