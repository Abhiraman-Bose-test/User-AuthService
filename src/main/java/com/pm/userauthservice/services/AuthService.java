package com.pm.userauthservice.services;

import com.pm.userauthservice.dtos.AuthResponse;
import com.pm.userauthservice.exceptions.PasswordMismatchException;
import com.pm.userauthservice.exceptions.UserAlreadyExistException;
import com.pm.userauthservice.exceptions.UserNotRegisteredException;
import com.pm.userauthservice.models.Session;
import com.pm.userauthservice.models.Status;
import com.pm.userauthservice.models.User;
import com.pm.userauthservice.repos.SessionRepo;
import com.pm.userauthservice.repos.UserRepo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

@Service
public class AuthService implements IAuthService{

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SessionRepo sessionRepo;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private SecretKey secretKey;


    @Override
    public User signup(String name, String email, String password) throws UserAlreadyExistException{
        Optional<User> userOptional = userRepo.findByEmail(email);
        if(userOptional.isPresent()){
            throw new UserAlreadyExistException("User already exist");
        }
        User user =new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setCreatedAt(new Date());
        user.setLastUpdatedAt(new Date());
        userRepo.save(user);
        return user;
    }

    @Override
    public AuthResponse login(String email, String password) throws UserNotRegisteredException,PasswordMismatchException {
        Optional<User> userOptional = userRepo.findByEmail(email);
        if(userOptional.isEmpty()){
            throw new UserNotRegisteredException("User not registered");
        }
        String storedPassword = userOptional.get().getPassword();
        if(!bCryptPasswordEncoder.matches(password,storedPassword)){
            throw new PasswordMismatchException("Please add correct Password");
        }
//        Map<String,Object> payload=new HashMap<>();
//        Long nowInMillis =System.currentTimeMillis();
//        payload.put("iat",nowInMillis);
//        payload.put("exp",nowInMillis+1000*60*60*24);
//        payload.put("userId", userOptional.get().getId());
//        payload.put("iss","scaler");
//        payload.put("scope",userOptional.get().getRoles());
        String token = Jwts.builder().subject(userOptional.get().getEmail()).claim("userId",userOptional.get().getId()).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis()+1000*60*60*24)).claim("name",userOptional.get().getName()).signWith(secretKey).compact();


        Session session = new Session();
        session.setToken(token);
        session.setUser(userOptional.get());
        session.setStatus(Status.ACTIVE);
        session.setCreatedAt(new Date());
        session.setLastUpdatedAt(new Date());
        sessionRepo.save(session);

        return new AuthResponse(token,userOptional.get().getId(),userOptional.get().getName(),userOptional.get().getEmail());
    }

    @Override
    public Boolean validateToken(String token, Long userId) {
        Optional<Session> optionalSession =sessionRepo.findByTokenAndUser_Id(token,userId);
        if(optionalSession.isEmpty()){
            return false;
        }
        String persistedToken = optionalSession.get().getToken();
        JwtParser jwtParser = Jwts.parser().verifyWith(secretKey).build();
        Claims claims = jwtParser.parseSignedClaims(token).getPayload();
        Long tokenExpiry =(Long) claims.get("exp");
        Long currentTime = System.currentTimeMillis();

        System.out.println(tokenExpiry);
        System.out.println(currentTime);
        if(currentTime>tokenExpiry){
            Session session = optionalSession.get();
            session.setStatus(Status.INACTIVE);
            sessionRepo.save(session);
            return false;
        }
        return true;
    }
}
