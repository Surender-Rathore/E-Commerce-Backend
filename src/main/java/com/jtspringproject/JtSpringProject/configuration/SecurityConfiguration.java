package com.jtspringproject.JtSpringProject.configuration;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.jtspringproject.JtSpringProject.models.User;
import com.jtspringproject.JtSpringProject.services.userService;

@Configuration
public class SecurityConfiguration {

	private final userService userService;

	public SecurityConfiguration(userService userService) {
		this.userService = userService;
	}

	@Configuration
	@Order(1)
	public static class AdminConfigurationAdapter {

		@Bean
		SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
			http.antMatcher("/admin/**")
					.csrf(csrf -> csrf.disable())
					.cors(cors -> {})
					.authorizeHttpRequests(requests -> requests
							.requestMatchers(new AntPathRequestMatcher("/admin/login")).permitAll()
							.requestMatchers(new AntPathRequestMatcher("/admin/**")).hasRole("ADMIN"))
					.formLogin(login -> login
							.loginPage("/admin/login")
							.loginProcessingUrl("/admin/loginvalidate")
							.successHandler((request, response, authentication) -> {
								response.setStatus(HttpServletResponse.SC_OK);
								response.setContentType("application/json");
								response.getWriter().write("{\"message\":\"Admin login successful\"}");
							})
							.failureHandler((request, response, exception) -> {
								response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
								response.setContentType("application/json");
								response.getWriter().write("{\"message\":\"Invalid admin credentials\"}");
							}))
					.logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/admin/logout", "GET"))
							.logoutSuccessUrl("/admin/login")
							.deleteCookies("JSESSIONID"))
					.exceptionHandling(exception -> exception
							.accessDeniedPage("/403"));
			return http.build();
		}
	}

	@Configuration
	@Order(2)
	public static class UserConfigurationAdapter {

		@Bean
		SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {

			http.csrf(csrf -> csrf.disable())
					.cors(cors -> {})
					.authorizeHttpRequests(requests -> requests
							.antMatchers("/login", "/register", "/newuserregister", "/api/currentuser").permitAll()
							.antMatchers("/**").hasRole("USER"))
					.formLogin(login -> login
							.loginPage("/login")
							.usernameParameter("email")
							.loginProcessingUrl("/userloginvalidate")
							.successHandler((request, response, authentication) -> {
								response.setStatus(HttpServletResponse.SC_OK);
								response.setContentType("application/json");
								response.getWriter().write("{\"message\":\"Login successful\"}");
							})
							.failureHandler((request, response, exception) -> {
								response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
								response.setContentType("application/json");
								response.getWriter().write("{\"message\":\"Invalid username or password\"}");
							}))
					.logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
							.logoutSuccessUrl("/login")
							.deleteCookies("JSESSIONID"))
					.exceptionHandling(exception -> exception
							.accessDeniedPage("/403"));
			return http.build();
		}
	}

	@Bean
	UserDetailsService userDetailsService() {
		return usernameOrEmail -> {
			User user = userService.getUserByUsername(usernameOrEmail);
			if (user == null) {
				user = userService.getUserByEmail(usernameOrEmail);
			}
			if (user == null) {
				throw new UsernameNotFoundException("User not found: " + usernameOrEmail);
			}
			String role = "ROLE_ADMIN".equals(user.getRole()) ? "ADMIN" : "USER";

			return org.springframework.security.core.userdetails.User
					.withUsername(user.getUsername())
					.password(user.getPassword())
					.roles(role)
					.build();
		};
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:5174"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}