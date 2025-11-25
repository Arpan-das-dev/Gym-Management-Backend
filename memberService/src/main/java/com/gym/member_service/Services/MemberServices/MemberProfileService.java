package com.gym.member_service.Services.MemberServices;

import com.gym.member_service.Dto.NotificationDto.GenericResponse;
import com.gym.member_service.Exception.Exceptions.InvalidImageUrlException;
import com.gym.member_service.Exception.Exceptions.UserNotFoundException;
import com.gym.member_service.Model.Member;
import com.gym.member_service.Repositories.MemberRepository;
import com.gym.member_service.Services.OtherService.AWSS3service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**

 Service class responsible for managing member profile images including upload, retrieval, and deletion,

 as well as maintaining association with assigned trainers.

 <p>This service uses AWS S3 for storing profile images and a database repository to maintain
 member profile references. It supports caching of profile image URLs for performance optimization.

 <p>Transactional boundaries ensure data integrity with rollback support on failures.
 Detailed logging tracks request processing times and critical steps.

 @author Arpan Das

 @version 1.0

 @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor

public class MemberProfileService {

    private final AWSS3service awss3service;
    private final MemberRepository memberRepository;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**

     Uploads a profile image for a member.

     <p>Stores the image in AWS S3, updates the member's profile image URL in the database,
     and updates the cached URL entry. Rolls back on any failure during the transaction.

     Provides logging and prints for tracing the request and processing time.

     @param id the member's unique identifier

     @param image the multipart file containing the image to upload

     @return a GenericResponse containing the stored image URL

     @throws UserNotFoundException if no member is found with the given id
     */
    @CachePut(value = "profileImageUrl", key = "#id")
    @Transactional
    public GenericResponse uploadImage(String id, MultipartFile image) {
        System.out.println("⌛⌛ Request received to upload Profile image Url at: " + LocalDateTime.now().format(formatter));
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("No member found with this id: " + id));
        String url = awss3service.uploadImage(id, image);
        member.setProfileImageUrl(url);
        memberRepository.save(member);
        log.info("Profile image uploaded and URL saved for member id: {}", id);
        return new GenericResponse(url);
    }

    /**

     Retrieves the profile image URL for a member.

     <p>Checks the cache first; if absent, fetches from database.
     Logs processing times and key data points. Returns "empty" if no image is present.

     @param id the member's unique identifier

     @return a GenericResponse containing the profile image URL or "empty" string

     @throws UserNotFoundException if no member exists for the given id
     */
    @Cacheable(value = "profileImageUrl", key = "#id")
    public GenericResponse getProfileImageUrlByMemberId(String id) {
        long start = System.currentTimeMillis();
        System.out.println("⌛⌛ Request received to get profile image Url at: " + LocalDateTime.now().format(formatter));
        log.info("Request reached to get profile image URL by member id: {}", id);
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("No member found with this id: " + id));
        log.info("Member fetched from DB: {} {} with id {}", member.getFirstName(), member.getLastName(), member.getId());
        long end = System.currentTimeMillis();
        String url = (member.getProfileImageUrl() == null || member.getProfileImageUrl().isEmpty()) ? "empty" : member.getProfileImageUrl();
        GenericResponse response = new GenericResponse(url);
        log.info("Returning profile image URL: {}", url);
        log.info("Completed processing in {} ms, caching data with id {}", end - start, member.getId());
        System.out.println("⌛⌛ Completed process to get image URL at: " + LocalDateTime.now().format(formatter));
        return response;
    }

    /**

     Deletes a member's profile image.

     <p>Removes the image from AWS S3, clears the URL in the member profile,
     updates the cache, and rolls back changes upon failure.

     Throws exception if no image URL exists.

     @param id the member's unique identifier

     @throws UserNotFoundException if no member exists for the given id

     @throws InvalidImageUrlException if no valid image URL is associated
     */
    @CacheEvict(value = "profileImageUrl", key = "#id")
    @Transactional
    public void deleteImage(String id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("No member found with this id: " + id));
        if (member.getProfileImageUrl() == null || member.getProfileImageUrl().isBlank()) {
            throw new InvalidImageUrlException("Invalid image URL");
        }
        awss3service.deleteImage(member.getProfileImageUrl());
        member.setProfileImageUrl("");
        memberRepository.save(member);
        log.info("Deleted profile image and cleared URL for member id: {}", id);
    }
}