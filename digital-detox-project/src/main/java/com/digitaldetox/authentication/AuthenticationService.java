package com.digitaldetox.authentication;

import com.digitaldetox.dto.auth.AuthenticationRequestDTO;
import com.digitaldetox.dto.auth.AuthenticationResponseDTO;
import com.digitaldetox.model.User;
import com.digitaldetox.repository.CoachProfileRepository;
import com.digitaldetox.repository.MemberProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MemberProfileRepository memberProfileRepository;
    private final CoachProfileRepository coachProfileRepository;

    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password()));
        User user = (User) authentication.getPrincipal();
        String token = jwtService.generateToken(authentication.getName(), user.getRole().getName());
        return new AuthenticationResponseDTO(token, user.getRole().getName(), resolveDisplayName(user));
    }

    private String resolveDisplayName(User user) {
        return memberProfileRepository.findByUserUsernameAndDeletedFalse(user.getUsername())
                .map(profile -> profile.getDisplayName())
                .orElseGet(() -> coachProfileRepository.findByUserUsernameAndDeletedFalse(user.getUsername())
                        .map(profile -> profile.getDisplayName())
                        .orElse(user.getUsername()));
    }
}
