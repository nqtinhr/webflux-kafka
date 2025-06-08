package com.syshero.profileservice.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.syshero.profileservice.data.Profile;

import reactor.core.publisher.Mono;

public interface ProfileRepository extends ReactiveCrudRepository<Profile, Long> {
    Mono<Profile> findByEmail(String email);
}
