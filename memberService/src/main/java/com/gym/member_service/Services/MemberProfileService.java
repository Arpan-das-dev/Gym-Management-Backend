package com.gym.member_service.Services;

import com.gym.member_service.Exception.Exceptions.UserNotFoundException;
import com.gym.member_service.Model.Member;
import com.gym.member_service.Repositories.MemberRepository;
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

    @CachePut(value = "profileImageUrl", key = "'member:id:'+#id")
    @Transactional
    public String  uploadImage(String id, MultipartFile image){
        Member member = memberRepository.findById(id)
                .orElseThrow(()-> new UserNotFoundException("NO member found with this id: "+id));
        String url = awss3service.uploadImage(id,image);
        member.setProfileImageUrl(url);
        memberRepository.save(member);
        return url;
    }

    @CacheEvict(value = "profileImageUrl", key = "'member:id:' + #id")
    @Transactional
    public void deleteImage(String id){
        Member member = memberRepository.findById(id)
                .orElseThrow(()-> new UserNotFoundException("NO member found with this id: "+id));
        if(member.getProfileImageUrl() == null){
            throw new IllegalArgumentException("Invalid image URL");
        }
        awss3service.deleteImage(member.getProfileImageUrl());
        member.setProfileImageUrl(null);
        memberRepository.save(member);
    }



}
