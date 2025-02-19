package com.shouq.project.Security.security;

import java.util.Arrays;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizationSuccessHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.shouq.project.Security.jwt.AuthEntryPointJwt;
import com.shouq.project.Security.jwt.AuthTokenFilter;
import com.shouq.project.Security.security.services.UserDetailsServiceImpl;



@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {
  @Autowired
  UserDetailsServiceImpl userDetailsService;

  @Autowired
  private AuthEntryPointJwt unauthorizedHandler;

  // @Autowired
  // private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

  @Autowired
  private AuthenticationSuccessHandler oAuth2LoginSuccessHandler;
  
  @Value("${frontend.url}")
  private String frontendUrl;
  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());

    return authProvider;
  }

  ////بشوف صلاحيات المستخدم
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }



    @Bean
  public FilterRegistrationBean<CorsFilter> corsFilterRegistrationBean() {
    List<String> all = Arrays.asList("*");

    CorsConfiguration corsConfiguration = new CorsConfiguration();
    corsConfiguration.setAllowedOrigins(Arrays.asList("http://localhost:3000","http://localhost:3000/Homeee"));
    corsConfiguration.setAllowedMethods(all);
    corsConfiguration.setAllowedHeaders(all);
    corsConfiguration.setAllowCredentials(true); // Allow credentials

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfiguration);

    CorsFilter corsFilter = new CorsFilter(source);
    FilterRegistrationBean<CorsFilter> filterRegistrationBean = new FilterRegistrationBean<>(corsFilter);
    filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);

    return filterRegistrationBean;
  }
  
 @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http.csrf(csrf -> csrf.disable())
        .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth ->
            auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/user").permitAll()
            .requestMatchers(HttpMethod.PUT,"/users/**").authenticated()
            .requestMatchers("/swagger-ui.html").permitAll()
            .requestMatchers("/swagger-ui/**").permitAll()
            .requestMatchers("/api-docs/**").permitAll()
            .anyRequest().permitAll())
          //   .oauth2Login(oath2 -> {
          //     oath2.loginPage("/login").permitAll();
          //     oath2.successHandler(oAuth2LoginSuccessHandler);
          // })
        // .oauth2Login(oauth2 -> oauth2.successHandler(customAuthenticationSuccessHandler))
        // .oauth2Client(Customizer.withDefaults())
        ;

      http.authenticationProvider(authenticationProvider());


      http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

      return http.build();
  }
    // @Bean
    // SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    //     return http
    //             .csrf(AbstractHttpConfigurer::disable)
    //             .cors(cors -> cors.configurationSource(corsConfigurationSource()))
    //             .authorizeHttpRequests(auth -> {
    //                 auth.anyRequest().authenticated();
    //             })
    //             .oauth2Login(oath2 -> {
    //                 oath2.loginPage("/login").permitAll();
    //                 oath2.successHandler(oAuth2LoginSuccessHandler);
    //             })
    //             .build();
    // }


    //   @Bean
    // public CorsConfigurationSource CorsConfigurationSource() {
    //     CorsConfiguration configuration = new CorsConfiguration();
    //     configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); // Allow requests from port 3000
    //     configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    //     configuration.setAllowedHeaders(Arrays.asList("*"));
    //     configuration.setAllowCredentials(true); // Allow credentials

    //     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //     source.registerCorsConfiguration("/**", configuration);
    //     return source;
    // }
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl));
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", configuration);
        return urlBasedCorsConfigurationSource;
    }
    

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}