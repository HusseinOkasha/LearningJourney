package com.project3.registration;

import com.project3.appuser.AppUserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/registration")
public class RegistrationController {

    private final AppUserService appUserService;
    private final RegistrationService registrationService;

    @PostMapping
    String register(@RequestBody RegistrationRequest request){
        return registrationService.register(request);
    }
}
