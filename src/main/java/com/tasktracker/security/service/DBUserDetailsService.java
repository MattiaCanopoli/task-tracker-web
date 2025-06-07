package com.tasktracker.security.service;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.tasktracker.repository.UserRepo;
import com.tasktracker.security.DBUserDetails;
import com.tasktracker.security.model.User;

@Service
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
