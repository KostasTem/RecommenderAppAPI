package com.unipi.cs.kt.appBackendTest.Security;

import com.unipi.cs.kt.appBackendTest.Filter.AuthenticationFilter;
import com.unipi.cs.kt.appBackendTest.Filter.AuthorizationFilter;
import com.unipi.cs.kt.appBackendTest.Services.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AppUserService appUserService;

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder,AppUserService appUserService){
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.appUserService = appUserService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(authenticationManagerBean(),secret,appUserService);
        authenticationFilter.setFilterProcessesUrl("/api/user/login");
        http.csrf().disable();

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.authorizeRequests().antMatchers("/api/user/login/**").permitAll();

        http.authorizeRequests().antMatchers("/api/user/register/**").permitAll();

        http.authorizeRequests().antMatchers("/api/user/refreshToken/**").permitAll();

        http.authorizeRequests().antMatchers("/api/user/resetPassword/**").permitAll();

        http.authorizeRequests().antMatchers("/api/user/sendResetEmail/**").permitAll();

        http.authorizeRequests().antMatchers("/api/data/test/**").permitAll();

        http.authorizeRequests().antMatchers("/api/data/insertNewUser/**").permitAll();

        http.authorizeRequests().antMatchers("/api/user/**").hasAnyAuthority("ROLE_USER");

        http.authorizeRequests().antMatchers("/api/data/**").hasAnyAuthority("ROLE_USER");

        http.authorizeRequests().anyRequest().authenticated();

        http.addFilter(authenticationFilter);

        http.addFilterBefore(new AuthorizationFilter(secret), UsernamePasswordAuthenticationFilter.class);

    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
