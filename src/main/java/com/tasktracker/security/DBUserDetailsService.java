package com.tasktracker.security;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.tasktracker.repository.UserRepo;
import com.tasktracker.security.model.User;

public class DBUserDetailsService implements UserDetailsService {
	
	private final UserRepo userRepo;
	
	public DBUserDetailsService(UserRepo userRepo) {
		this.userRepo=userRepo;
	}

	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		Optional<User> user = userRepo.findByUsername(username);
		
		if (!user.isPresent()) {
			throw new UsernameNotFoundException("User "+username+" not found");
		}
		
		return new DBUserDetails(user.get());
	}

}
