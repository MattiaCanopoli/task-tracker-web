package com.tasktracker.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.tasktracker.security.model.Role;
import com.tasktracker.security.model.User;

public class DBUserDetails implements UserDetails {
	
	private final String username;
	private  final String password;
	private final long id;
	private final Set<GrantedAuthority> authorities;
	
	public DBUserDetails(User user) {
		this.username = user.getUsername();
		this.password = user.getPassword();
		this.id=user.getId();
		this.authorities = new HashSet<GrantedAuthority>();
		for (Role role : user.getRoles()) {
			this.authorities.add(new SimpleGrantedAuthority(role.getName()));
		}
		
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

}
