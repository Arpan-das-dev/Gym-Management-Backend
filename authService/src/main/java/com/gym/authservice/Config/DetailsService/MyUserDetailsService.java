//package com.gym.authservice.Config.DetailsService;
//
//import com.gym.authservice.Entity.SignedUps;
//import com.gym.authservice.Repository.SignedUpsRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//import java.util.Collection;
//import java.util.List;
//
//@RequiredArgsConstructor
//@Service
//public class MyUserDetailsService implements UserDetailsService {
//    private final SignedUpsRepository signedUpsRepository;
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        SignedUps signedUps = signedUpsRepository.findByEmail(username)
//                .orElseThrow(()->new UsernameNotFoundException("no user found with this email id"));
//
//        boolean enabled = signedUps.isEmailVerified() && signedUps.isPhoneVerified() && signedUps.isApproved();
//        return new User(
//                signedUps.getEmail(),
//                signedUps.getPassword(),
//                enabled,
//                true,
//                true,
//                true,
//                getAuthorities(signedUps.getRole().name())
//        );
//    }
//    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
//        return List.of(new SimpleGrantedAuthority("ROLE_"+ role));
//    }
//}
