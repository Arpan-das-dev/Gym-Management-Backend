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

@Slf4j
@Service
@RequiredArgsConstructor
/*
 * this service class is responsible for member profile image management
 * and trainer assigned to the member
 */
public class MemberProfileService {

    private final AWSS3service awss3service;
    private final MemberRepository memberRepository;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /*
     * This method is responsible for  uploading
     * Member's profile image
     * it's using AWS s3 service to do so
     * @Transactional: use to ensure that
     * if any error occurs the data will be rolled back
     * After successful upload it updates the cache
     */
    @CachePut(value = "profileImageUrl", key = "'member:id:'+#id")
    @Transactional
    public String  uploadImage(String id, MultipartFile image){
        System.out.println("⌛⌛ Request received to upload Profile image Url at :"+ LocalDateTime.now().format(formatter));
        Member member = memberRepository.findById(id)
                .orElseThrow(()-> new UserNotFoundException("No member found with this id: "+id)); // if no member found
                                                                                                // then throws exception
        String url = awss3service.uploadImage(id,image);            // returns url where the image is saved
        member.setProfileImageUrl(url);                            //  set the url in the member profile
        memberRepository.save(member);
        return url;                                                 // returning the url
    }

    @Cacheable(value = "profileImageUrl", key = "'member:id:' + #id")
    public GenericResponse getProfileImageUrlByMemberId(String id) {
        long start = System.currentTimeMillis();
        System.out.println("⌛⌛ Request received to upload Profile image Url at :"+ LocalDateTime.now().format(formatter));
        log.info("Request reached to get profile image url by member:: {}",id);
        Member member = memberRepository.findById(id)
                .orElseThrow(()-> new UserNotFoundException("No member found with this id: "+id));
        log.info("Member fetched {} from db with id {}",member.getFirstName()+" "+member.getLastName(),member.getId());
        long end = System.currentTimeMillis();
        GenericResponse response = new GenericResponse(member.getProfileImageUrl());
        log.info("Completed processing in {} ms caching all data with id {}", end-start,member.getId());
        System.out.println("⌛⌛ completing process to get image url at :"+ LocalDateTime.now().format(formatter));
        return response;
    }

    /*
     * This method is responsible to delete
     * the image form profile of a member
     * then it updates the cache and
     * Transactional is used to
     * ensure that if any problem occurs the
     * data will be rolled back
     */
    @CacheEvict(value = "profileImageUrl", key = "'member:id:' + #id")
    @Transactional
    public void deleteImage(String id){
        Member member = memberRepository.findById(id)
                .orElseThrow(()-> new UserNotFoundException("NO member found with this id: "+id)); // if no member found
                                                                                                // then throws exception
        if(member.getProfileImageUrl() == null){
            throw new InvalidImageUrlException("Invalid image URL");        // throw error if no image url found
        }
        awss3service.deleteImage(member.getProfileImageUrl());
        member.setProfileImageUrl(null);    // set the url ass null after successfully delete
        memberRepository.save(member);
    }

}
