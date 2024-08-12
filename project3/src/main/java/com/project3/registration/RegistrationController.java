package com.project3.registration;

import com.project3.appuser.AppUserService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/registration")
public class RegistrationController {

    private final AppUserService appUserService;
    private final RegistrationService registrationService;

    @PostMapping
    String register(@RequestBody RegistrationRequest request){

        return registrationService.register(request);
    }

    @GetMapping(path = "confirm")
    String confirm(@RequestParam String token ){

        return registrationService.confirmToken(token);

    }
}
