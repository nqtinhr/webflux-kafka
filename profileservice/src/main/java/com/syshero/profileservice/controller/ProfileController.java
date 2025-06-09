package com.syshero.profileservice.controller;

import java.io.InputStream;


import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.syshero.commonservice.utils.CommonFunction;
import com.syshero.profileservice.model.ProfileDTO;
import com.syshero.profileservice.service.ProfileService;
import com.syshero.profileservice.utils.Constant;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/profiles")
public class ProfileController {
    @Autowired
    ProfileService profileService;
    Gson gson = new Gson();

    @GetMapping
    public ResponseEntity<Flux<ProfileDTO>> getAllProfile() {
        return ResponseEntity.ok(profileService.getAllProfile());
    }

    @GetMapping(value = "/checkDuplicate/{email}")
    public ResponseEntity<Mono<Boolean>> checkDuplicate(@PathVariable String email) {
        return ResponseEntity.ok(profileService.checkDuplicate(email));
    }

    @PostMapping
    public ResponseEntity<Mono<ProfileDTO>> createNewProfile(@RequestBody String requestStr) {
        // Truyền đường dẫn stream
        InputStream inputStream = ProfileController.class.getClassLoader()
                .getResourceAsStream(Constant.JSON_REQ_CREATE_PROFILE);
        CommonFunction.jsonValidate(inputStream, requestStr);
        // Gson: Chuyển đổi chuỗi JSON sang đối tượng ProfileDTO
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.createNewProfile(gson.fromJson(requestStr, ProfileDTO.class)));
    }
}
