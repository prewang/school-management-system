package com.school.security;

import com.school.entity.SysUser;
import io.jsonwebtoken.Claims;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    /** 从数据库实体构建（登录时使用）*/
    public UserPrincipal(SysUser user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPasswordHash();
        this.enabled = Integer.valueOf(1).equals(user.getStatus());
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    /**
     * 从 JWT Claims 构建（Filter 请求认证时使用，无需查库）。
     * status 不在 Token 中，设为 true；账号禁用的实时生效依赖 Access Token 2h 短期设计。
     */
    public UserPrincipal(Claims claims) {
        this.id = claims.get("userId", Long.class);
        this.username = claims.getSubject();
        this.password = null;
        this.enabled = true;
        this.authorities = List.of(new SimpleGrantedAuthority(
                "ROLE_" + claims.get("role", String.class)));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }
}
