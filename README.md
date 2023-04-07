# SpringSecurity

springSecurity를 활용한 회원가입 및 로그인 기능입니다.

멋사 

- [Kim Kyeongrok님의 회원가입강의](https://www.youtube.com/watch?v=c6tLfLd9AVw)
- [Kim Kyeongrok님의 로그인강의](https://www.youtube.com/watch?v=f_c11jdjjVI)

를 참고하였습니다.

<br>

## 회원가입(join) 만들기 A to Z

1. Post /api/v1/users/join 만들기
2. 회원가입 성공 Controller Test
3. 회원가입 실패 -userName중복 Controller Test
4. UserJoinRequest userName, password 받기
5. UserService.join() 중복 check 해서 ifPresent면 RuntimeException 리턴
6. @RestControllerAdvice 선언
7. @ExceptionHandler로 RuntimeException 받기
8. CustomException
9. ErrorCode 선언
10. 중복 check exception 수정

<br>


## SpringSecurity 로그인 만들기

1. 테스트코드 작성
2. Service 구현
3. 토큰 발행 기능 구현


## 회원가입(join) 만들기 A to Z


<br>


### Post /api/v1/users/join 만들기
Controller를 기준으로 작성하면서 클래스를 create 해 나간다.

userName, password를 UserJoinRequest dto를 통해 받아온다.

받아온 데이터를 userService에 join을 하여 DB에 저장하도록 한다.

반환타입으로 ResponseEntity<String> 를 사용하여 ResponseEntity.ok().body("회원가입에 성공하였습니다.") 라는 String 타입의 메세지를 
담아 보내도록 한다. 


<br>


### /controller

@RequestBody를 사용하여 UserJoinRequest dto를 받아오고

에서 반환타입을 ResponseEntity<String>으로 하여 성공시 200 과 함께 String으로 "회원가입이 성공하였습니다."를 넘겨준다.

Join 로직을 수행할 UserService를 만들어야 하므로 Controller에서 Service 선언 후 -> create

`private final` 선언후 @RequiredArgsConstructor 어노테이션을 적용해주면 `private final`이 붙은 변수는 의존성을 자동 주입해준다.

<br>


### /service

join 기능을 만든다.

users 중복 check을 하기 위해 인자로 userName, password를 받아오고 성공한다면 return값으로 "SUCCESS"를 넘겨준다.

중복 check을 하려면 DB에 갔다와야 하므로 Service 내에 UserRepository를 선언 후 create

이 부분도 interface로 만든 후 `private final` @RequiredArgsConstructor 적용

join 메소드에 UserRepository의 findByUserName메소드를 호출한다.

// 중복 체크

.ifPresent를 통해 userName이 만약 이미 존재한다면
`throw new RuntimeException` 을 통해
"이미 존재합니다."라는 String 값을 리턴해주도록 합니다.

// 저장

userRepository에서 save를 만들어줍니다.

User 객체를 Builder를 통해 만들어줍니다.

```java
User users = User.builder()
        .userName(userName)
        .password(password)
        .build();

        userRepository.save(users);
```


<br>



### /repository

UserRepository 를 만들어주었으므로 JPARepository를 상속(extends)할때 User를 타입으로 받아온다.

User를 타입으로 받아온다는 것은 User가 엔티티라는 것

domain 폴더에 User 엔티티 -> create

UserRepository 인터페이스어 findByUserName 메소드를 만들고 Optional<User> 를 반환타입으로 선언하여 userName이 없으면 Optional안에 
값이 들어오지 않고 있으면 들어오게 한다.



<br>


### /domain

User 엔티티에 속성값으로 id, userName, password를 넣어주었다.

어노테이션

@NoArgsConstructor : 파라미터가 없는 기본 [생성자](https://velog.io/@rivkode/%EC%83%9D%EC%84%B1%EC%9E%90-Constructor) 를 생성
@AllArgsConstructor : 모든 필드 값을 파라미터로 받는 생성자를 만듦
@RequiredArgsConstructor : final이나 @NonNull인 필드 값만 파라미터로 받는 생성자를 만듦

@Builder

인스턴스 생성을 할때 속성타입이 동일하고 순서가 바뀐다면 ? lombok이 변화를 알아체지 못하며, 심각한 비즈니스 로직 에러를 발생시킬 수 있다.

이 때문에 @AllArgsConstructor와 @RequiredArgsConstructor 사용을 하지 않는다고 하면 우리에겐 Builder 어노테이션이 있다.

이 방법은 파라미터 순서가 아닌 이름으로 값을 설정하기 때문에 아래와 같이 리팩토링에 유연하게 대응이 가능하다.

```java
public class Order {
    private int cancelAmount;
    private int orderAmount;
    
    @Builder
    private Order(int cancelAmount, int orderAmount) {
        this.cancelAmount = cancelAmount;
        this.orderAmount = orderAmount;
    }
}


// field 순서를 변경해도 에러가 없다.
Order order = Order.builder().orderAmount(5).cancelAmount(4).build();
```

<br>


### /회원가입 테스트

```java
    @Test
    @DisplayName("회원가입 성공")
    void join() throws Exception {
        String userName = "jonghun";
        String password = "1234";


        mockMvc.perform(post("/api/v1/users/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new UserJoinRequest(userName, password))))
                .andDo(print())
                .andExpect(status().isOk());
    }
```

<br>


### /RestControllerAdvice에서 Exception 처리

회원 가입시
![image](https://user-images.githubusercontent.com/109144975/220839807-9aa4545b-4533-4409-a5f1-fdb0ced85bce.png)

DB 저장


![image](https://user-images.githubusercontent.com/109144975/220842443-80f28c22-ba53-499d-a343-4cde63762c5b.png)

[에러] 동일한 이름으로 가입 시

`(userName)는 이미 있습니다.`

라는 메세지를 포함하여 리턴하도록 하였습니다.

![image](https://user-images.githubusercontent.com/109144975/220849377-c737c2ac-42ec-4b0c-a54e-05f75c0472bf.png)

UserService join -> 회원가입의 경우 성공, 실패 두가지 경우가 있음

- 성공일 경우 userRepository 에서 save를 통해 저장이 되며 "SUCCESS"가 반환이 되고

- 실패일 경우 에러가 findByUserName 중복 이름일 경우 이므로 RumtimeException 이 발생되면

- ExceptionManager에 @RestControllerAdvice 어노테이션이 달린 @ExceptionHandler의 RuntimeException의 메소드로 이동하게 되어

- status는 HttpStatus.CONFLICT가 되며 body에는 e.getMessage()를 통해 에러 메세지를 담게 된다.

<br>


### /CustomException - AppException

exception 패키지에 만들고 싶은 Exception 클래스를 생성한 후 RuntimeException을 상속한다.

어노테이션으로 @AllArgsConstructor, @Getter을 사용한다.

속성으로는 ErrorCode를 enum으로 만들어 주고 에러 메세지를 받아와야 하므로 message를 만들어 준다.

enum을 생성하고 enum 속성으로 HttpStatus, message를 만들어 준 후 USERNAME_DUPLICATED(HttpStatus.CONFLICT, "메세지")를 만들어 준다.

- ExceptionManager에 가서 방금 만들어준 AppException을 아래와 같이 적용해준다.

```java
@RestControllerAdvice
public class ExceptionManager {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> appExceptionHandler(AppException e) {
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(e.getErrorCode().name() + " " + e.getMessage());
    }
```
만들어준 어노테이션을 적용한 에러메세지 이다.

어노테이션을 통해 에러메세지에 USERNAME_DUPLICATED가 함께 나오도록 하였다.

![image](https://user-images.githubusercontent.com/109144975/220851606-eb5c4d2d-1b04-4b66-ae7c-f32102f703c4.png)

<br>


### /Password Encoding해서 저장하기

springSecurity 추가 후 해당 url로 Post 요청시 거절됨

![image](https://user-images.githubusercontent.com/109144975/220853229-32768c1c-4020-40eb-bb3a-06c4be02fcc9.png)


SecurityConfig 클래스 추가 후 성공
![image](https://user-images.githubusercontent.com/109144975/220854312-be377135-4afc-4738-b7b1-badde9c912c3.png)

하지만 문제가 있음

회원이 보내준대로 DB에 아이디, 비밀번호가 바로 저장되므로 DB를 볼 수 있다면 모든 회원정보를 털어갈 수 있음

따라서 회원이 보내준 DB를 `encoder.encode(password)`를 통해 encoding하여 DB에 저장함

![image](https://user-images.githubusercontent.com/109144975/220855165-7506580b-4354-4291-8123-dd8371524db8.png)

![image](https://user-images.githubusercontent.com/109144975/220855424-fd85605a-765f-402f-ae7f-987d34ec3562.png)

<br>


## SpringSecurity 로그인 만들기

<br>


### 테스트코드 작성

Spring Security Test 라이브러리 import
`testImplementation group: 'org.springframework.security', name: 'spring-security-test', version: '5.8.0'`

- 성공 -> Success
- 실패 - id 없음 -> Not Found
- 실패 - 잘못된 password 입력 -> Unauthorized

controller에서 Service를 호출하므로 mock을 넣어줌

with(csrf()) 를 넣어줘야 회원가입, 로그인이 성공함
- 이유는? 더 찾아보자

<br>


### 로그인 Service 구현

service에서 login 메소드를 생성

login 메소드는 3가지 기능

- userName 없음
- password 틀림
- 앞의 내용이 다 통과될 경우 토큰 발행 및 반환

userName 없음 기능

userName 없음은 userRepository 에서 findByUserName 메소드로 검색 후 없으면 커스텀 어노테이션인
AppException 에서 **예외처리**를 ErrorCode USERNAME_NOT_FOUND을 반환하도록 한다. (enum 값 추가)

password 틀림은 `private final BCryptPasswordEncoder encoder` 로 선언한 encoder.mathes에서 password를 넣은 뒤 해당 값을 가져온 다음
실제 password와 같은지를 확인 후 같다면 아래 줄을 실행 틀리다면 AppExeption에서  **예외처리**를 ErrorCode INVALID_EXCEPTION를 반환하도록 한다.
(enum 값 추가)

<br>


### 로그인 토큰 발행

- JWT 라이브러리 import - `implementation group: 'io.jsonwebtoken', name: 'jjwt', version: '0.9.1'`
- jwt.token.secret = "secretKey"
- JWT_TOKEN_SECRET = "real_secret_key"

utils 패키지에 JwtTokenUtil 클래스 생성

해당 클래스에서 
createToken 메소드에서 인자로 (String userName, String key, long expireTimeMs)를 받아오고

만든날짜, 끝나는 날짜, keu를 가지고 만든 jwt 토큰을 compact하여 리턴합니다.

`.signWith(SignatureAlgorithm.HS256, key)`  [key를 가지고 해당 ES256 알고리즘으로 jwt를 만들겠다는 것]

리턴된 token을 로그인시 사용합니다.



jonghun10 아이디로 회원가입 후
![image](https://user-images.githubusercontent.com/109144975/220872394-1933e8f7-53a2-4de3-b63f-cfc67a10f4ce.png)

- jonghun10으로 로그인 시도
- 시도 결과 jwt 토큰 발행된 것 확인

![image](https://user-images.githubusercontent.com/109144975/220874931-2e9f5cb8-c167-4a3f-9d9e-a0dc175cef4c.png)

`log.info("selectedPw:{} pw:{}", selectedUser.getPassword(), password);`
`if(!encoder.matches(password, selectedUser.getPassword()))` 위부분
- 위 selectedUser.getPassword()를 통해 가져온 비밀번호와 password log를 통해 

![image](https://user-images.githubusercontent.com/109144975/220902032-913f02f5-710f-48ce-8b59-597943979e42.png)

