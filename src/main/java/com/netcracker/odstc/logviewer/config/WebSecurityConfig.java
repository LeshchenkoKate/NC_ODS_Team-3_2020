package com.netcracker.odstc.logviewer.config;

import com.netcracker.odstc.logviewer.security.UserDetailsServiceImpl;
import com.netcracker.odstc.logviewer.security.jwt.JwtAuthenticationFilter;
import com.netcracker.odstc.logviewer.security.jwt.JwtAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String GENERAL_ENDPOINT = "/*";
    private static final String LOGIN_ENDPOINT = "/login";
    private static final String USER_RESET_ENDPOINT = "/api/user/resetPassword**";
    private static final String USER_CHANGE_PASSWORD_ENDPOINT = "/api/user/changePassword**";
    private static final String USER_UPDATE_PASSWORD_ENDPOINT = "/api/user/updatePassword**";
    private static final String ADMIN_ENDPOINT = "/api/user/*";

    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    public WebSecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
    }

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable()
                .authorizeRequests()
                .antMatchers(LOGIN_ENDPOINT,GENERAL_ENDPOINT, USER_RESET_ENDPOINT, USER_CHANGE_PASSWORD_ENDPOINT,USER_UPDATE_PASSWORD_ENDPOINT).permitAll()
                .antMatchers(ADMIN_ENDPOINT).access("hasAuthority('ADMIN')")
                .anyRequest().authenticated()
                .and()
                .addFilter(new JwtAuthenticationFilter(authenticationManager()))
                .addFilter(new JwtAuthorizationFilter(authenticationManager()))
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}
