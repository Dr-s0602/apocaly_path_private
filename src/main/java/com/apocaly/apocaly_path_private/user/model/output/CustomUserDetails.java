package com.apocaly.apocaly_path_private.user.model.output;

import com.apocaly.apocaly_path_private.user.model.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // DB의 is_admin 컬럼 값에 따라 권한을 설정합니다.
        // 0이면 ROLE_USER 권한을, 1이면 ROLE_ADMIN 권한을 부여합니다.
        if (this.user.getIsAdmin()) { // isAdmin 메소드는 해당 필드가 1인지를 확인하는 메소드라고 가정합니다.
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return authorities;
    }

    @Override
    public String getPassword() {

        return user.getPassword();
    }

    @Override
    public String getUsername() {

        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        // 계정이 만료되지 않았는지 체크합니다. 여기서는 만료 개념이 없으므로 항상 true를 반환합니다.
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // is_delete가 0이면 계정이 잠겨있지 않은 것으로 간주합니다. 1이면 삭제 신청이 되어 잠금 상태로 볼 수 있습니다.
        return !this.user.getIsDelete();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 크리덴셜(비밀번호 등)이 만료되지 않았는지 체크합니다. 여기서는 만료 개념이 없으므로 항상 true를 반환합니다.
        return true;
    }

    @Override
    public boolean isEnabled() {
        // is_activated가 1이면 계정이 활성화 상태입니다.
        return !this.user.getIsActivated();
    }
}
