package com.tasktracker.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tasktracker.model.Status;
import com.tasktracker.repository.StatusRepo;

@Service
public class StatusService {

	private final StatusRepo statusRepo;

	public StatusService(StatusRepo statusRepo) {
		this.statusRepo = statusRepo;
	}

	/**
	 * Retrieve a status object by its name.
	 * @param statusName
	 * @return a Status object
	 * @return null if there is no Status object with such name
	 */
	public Status findStatusByName(String statusName) {
		Optional<Status> status = statusRepo.findByStatusName(statusName);

		if (!status.isPresent()) {
			return null;
		}

		return status.get();
	}
	
	/**
	 * Retrieves a status by its ID
	 * @param statusId
	 * @return a Status object
	 * @return null if there is no Status object with such ID
	 */
	public Status findStatusById(int statusId) {
		Optional<Status> status = statusRepo.findById(statusId);

		if (!status.isPresent()) {
			return null;
		}

		return status.get();
	}

}
