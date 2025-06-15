package com.syshero.profileservice.service;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.syshero.commonservice.common.CommonException;
import com.syshero.commonservice.utils.Constant;
import com.syshero.profileservice.event.EventProducer;
import com.syshero.profileservice.model.ProfileDTO;
import com.syshero.profileservice.repository.ProfileRepository;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ProfileService {
    Gson gson = new Gson();
    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    EventProducer eventProducer;

    public Flux<ProfileDTO> getAllProfile() {
        log.info("Fetching all profiles");
        return profileRepository.findAll()
                .map(profile -> ProfileDTO.entityToDto(profile))
                .switchIfEmpty(Mono.error(new CommonException("PF01", "Empty profile list !",
                        HttpStatus.NOT_FOUND)));
    }

    public Mono<Boolean> checkDuplicate(String email) {
        return profileRepository.findByEmail(email)
                .flatMap(profile -> Mono.just(true))
                .switchIfEmpty(Mono.just(false));
    }

    public Mono<ProfileDTO> createNewProfile(ProfileDTO profileDTO) {
        return checkDuplicate(profileDTO.getEmail())
                .flatMap(aBoolean -> {
                    if (Boolean.TRUE.equals(aBoolean)) {
                        return Mono.error(new CommonException("PF02", "Duplicate profile !", HttpStatus.BAD_REQUEST));
                    } else {
                        profileDTO.setStatus(Constant.STATUS_PROFILE_PENDING);
                        return createProfile(profileDTO);
                    }
                });
    }

    public Mono<ProfileDTO> createProfile(ProfileDTO profileDTO) {
        return Mono.just(profileDTO)
                .map(ProfileDTO::dtoToEntity)
                .flatMap(profile -> profileRepository.save(profile))
                .map(ProfileDTO::entityToDto)
                .doOnError(throwable -> log.error(throwable.getMessage()))
                .doOnSuccess(dto -> {
                    // Nếu trạng thái là PENDING, khởi tạo số dư ban đầu và gửi sự kiện
                    if (Objects.equals(dto.getStatus(), Constant.STATUS_PROFILE_PENDING)) {
                        dto.setInitialBalance(profileDTO.getInitialBalance());
                        eventProducer.send(Constant.PROFILE_ONBOARDING_TOPIC,
                                gson.toJson(dto)).subscribe();
                    }
                });
    }

    public Mono<ProfileDTO> updateStatusProfile(ProfileDTO profileDTO) {
        return getDetailProfileByEmail(profileDTO.getEmail())
                .map(ProfileDTO::dtoToEntity)
                .flatMap(profile -> {
                    profile.setStatus(profileDTO.getStatus());
                    return profileRepository.save(profile);
                })
                .map(ProfileDTO::entityToDto)
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    public Mono<ProfileDTO> getDetailProfileByEmail(String email) {
        return profileRepository.findByEmail(email)
                .map(ProfileDTO::entityToDto)
                .switchIfEmpty(Mono.error(new CommonException("PF03", "Profile not found", HttpStatus.NOT_FOUND)));
    }
}
