package com.tasktraker.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tasktraker.model.Status;
import com.tasktraker.repository.StatusRepo;

@Service
public class StatusService {

	private final StatusRepo statusRepo;

	public StatusService(StatusRepo statusRepo) {
		this.statusRepo = statusRepo;
	}

	public Status findStatusByName(String statusName) {
		Optional<Status> status = statusRepo.findByStatusName(statusName);

		if (!status.isPresent()) {
			return null;
		}

		return status.get();
	}

}
