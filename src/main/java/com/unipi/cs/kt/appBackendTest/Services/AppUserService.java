package com.unipi.cs.kt.appBackendTest.Services;

import com.unipi.cs.kt.appBackendTest.DataClasses.AppUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AppUserService {
    AppUser saveUser(AppUser appUser,boolean encode);
    AppUser getUser(String username);
    AppUser getUserByEmail(String email);
    AppUser getUserByID(Long id);
    List<AppUser> getUsers();
}
