package com.tasktracker.security.service;


import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.tasktracker.dto.DTOUser;
import com.tasktracker.exception.InvalidPasswordLengthException;
import com.tasktracker.exception.UserAlreadyExistsException;
import com.tasktracker.repository.UserRepo;
import com.tasktracker.security.model.User;

@Service
public class UserService {


	private final UserRepo uRepo;
	private final RoleService rService;

	public UserService(UserRepo uRepo, RoleService rService) {
		this.uRepo = uRepo;
		this.rService = rService;
	}

	public long getIdByUsername(String username) {

		User user = this.getByUsername(username);

		return user.getId();

	}

	public User getByUsername(String username) {
		Optional<User> user = uRepo.findByUsername(username);
		if (!user.isPresent()) {
			throw new UsernameNotFoundException(
					"Username" + username + " not found");
		}

		return user.get();

	}
	
	public User saveUser(DTOUser dto) {
		
		if (uRepo.findByUsername(dto.getUsername()).isPresent()) {
			throw new UserAlreadyExistsException("User: \""+dto.getUsername() +"\" already exists");
		}
		
		if (uRepo.findByEmail(dto.getEmail()).isPresent()) {
			throw new UserAlreadyExistsException("Email: \""+dto.getEmail() +"\" already exists");
		}
		
		if (dto.getPassword().length()<6) {
			throw new InvalidPasswordLengthException("Choosen password is too short. Password should be at least 6 characters long");
		}
		
		if (dto.getPassword().length()>24) {
			throw new InvalidPasswordLengthException("Choosen password is too long. Password should be at most 24 characters long");
		}
		
		User user = new User();
		user.setUsername(dto.getUsername());
		user.setEmail(dto.getEmail());
		
		BCryptPasswordEncoder encoder =  new BCryptPasswordEncoder(12);
		
		String password = encoder.encode(dto.getPassword());
		user.setPassword(password);
		
		user.setRoles(Set.of(rService.getByName("USER")));
		
		uRepo.save(user);
		
		return user;
			
	}
	
	public List<User> getAllUsers(){
		return uRepo.findAll();
	}
}
