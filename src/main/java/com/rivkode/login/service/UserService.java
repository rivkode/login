package com.rivkode.login.service;

import com.rivkode.login.domain.Users;
import com.rivkode.login.exception.AppException;
import com.rivkode.login.exception.ErrorCode;
import com.rivkode.login.repository.UserRepository;
import com.rivkode.login.utils.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    @Value("${jwt.token.secret}")
    private String key;

    private Long expireTimeMs = 1000 * 60 * 60L;

    public String join(String userName, String password) {

        // username 중복 체크
        userRepository.findByUserName(userName)
                .ifPresent(users -> {
                    throw new AppException(ErrorCode.USERNAME_DUPLICATED, userName + "는 이미 있습니다.");
                });

        // 저장
        Users users = Users.builder()
                .userName(userName)
                .password(encoder.encode(password))
                .build();

        userRepository.save(users);

        return "SUCCESS";
    }

    public String login(String userName, String password) {

        /**
         * userName 없음
         */
        Users selectedUser = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND, userName + "이 없습니다."));

        /**
         * password 틀림
         */
        log.info("selectedPw:{} pw:{}", selectedUser.getPassword(), password);
        if(!encoder.matches(password, selectedUser.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD, "패스워드를 잘못 입력 했습니다.");
        }

        /**
         * 앞에서 Exception 나지 않았으면 토큰 발행 및 반환
         */
        String token = JwtTokenUtil.createToken(selectedUser.getUserName(), key, expireTimeMs);

        return token;
    }
}
