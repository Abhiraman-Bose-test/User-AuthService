package com.pm.userauthservice.services;

import com.pm.userauthservice.dtos.AuthResponse;
import com.pm.userauthservice.exceptions.PasswordMismatchException;
import com.pm.userauthservice.exceptions.UserAlreadyExistException;
import com.pm.userauthservice.exceptions.UserNotRegisteredException;
import com.pm.userauthservice.models.User;
import org.antlr.v4.runtime.misc.Pair;

public interface IAuthService {
    User signup(String name,String email, String password) throws UserAlreadyExistException;
    AuthResponse login(String email, String password) throws UserNotRegisteredException, PasswordMismatchException;
    Boolean validateToken(String token, Long userId);
}
