package com.gym.member_service.Services.MemberServices;

import com.gym.member_service.Exception.Exceptions.InvalidImageUrlException;
import com.gym.member_service.Exception.Exceptions.UserNotFoundException;
import com.gym.member_service.Model.Member;
import com.gym.member_service.Repositories.MemberRepository;
import com.gym.member_service.Services.OtherService.AWSS3service;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
/*
 * this service class is responsible for member profile image management
 * and trainer assigned to the member
 */
public class MemberProfileService {

    private final AWSS3service awss3service;
    private final MemberRepository memberRepository;

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
        Member member = memberRepository.findById(id)
                .orElseThrow(()-> new UserNotFoundException("NO member found with this id: "+id)); // if no member found
                                                                                                // then throws exception
        String url = awss3service.uploadImage(id,image);            // returns url where the image is saved
        member.setProfileImageUrl(url);                            //  set the url in the member profile
        memberRepository.save(member);
        return url;                                                 // returning the url
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
