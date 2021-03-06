package com.classgram.backend.controller;

import com.classgram.backend.security.AuthorizationService;
import com.classgram.backend.struct.Role;
import com.classgram.backend.model.User;
import com.classgram.backend.security.user.UserComponent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

public abstract class SecureController {

    protected final UserComponent user;
    protected final AuthorizationService authorizationService;

    public SecureController(UserComponent user, AuthorizationService authorizationService) {
        this.user = user;
        this.authorizationService = authorizationService;
    }

    public ResponseEntity<?> authorize(Object...args){
        if(Objects.nonNull(args)){
            Object authObject = args[0];
            User user = this.user.getLoggedUser();
            return this.authorizationService.checkAuthorization(authObject, user);
        }
        else{
            return this.authorizationService.checkBackendLogged();
        }
    }

    public ResponseEntity<?> checkTeacherAuthorized(){
        if(this.user.getLoggedUser().isRole(Role.TEACHER)){
            return null;
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
}
