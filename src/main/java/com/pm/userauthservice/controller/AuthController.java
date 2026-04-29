package com.pm.userauthservice.controller;

import com.pm.userauthservice.dtos.*;
import com.pm.userauthservice.exceptions.PasswordMismatchException;
import com.pm.userauthservice.exceptions.UnauthorizedException;
import com.pm.userauthservice.exceptions.UserAlreadyExistException;
import com.pm.userauthservice.exceptions.UserNotRegisteredException;
import com.pm.userauthservice.models.User;
import com.pm.userauthservice.services.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private IAuthService authService;


    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest){
        try{
            User user =authService.signup(signupRequest.getName(),signupRequest.getEmail(),signupRequest.getPassword());
            return new ResponseEntity<>(from(user), HttpStatus.CREATED);
        }catch (UserAlreadyExistException e){
            return ResponseEntity.badRequest().body("User already exists");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){
        try{
            AuthResponse response = authService.login(loginRequest.getEmail(),loginRequest.getPassword());
//            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
//            headers.add(HttpHeaders.SET_COOKIE,response.b);
            return  ResponseEntity.ok(response);
        }catch(UserNotRegisteredException exception){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not registered");
        }catch(PasswordMismatchException exception){
            return ResponseEntity.badRequest().body("Incorrect Password");
        }
    }

    @PostMapping("/validateToken")
    public ResponseEntity<Boolean> validateToken(@RequestBody ValidateTokenDto validateTokenDto) throws UnauthorizedException {
        Boolean result =authService.validateToken(validateTokenDto.getToken(),validateTokenDto.getUserId());
        if(!result){
            throw new UnauthorizedException("Please login again, Inconvenience regretted");
        }
        return ResponseEntity.ok(true);
    }
    public UserDto from(User user){
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        return userDto;
    }
}
