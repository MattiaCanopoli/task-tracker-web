package com.tasktracker.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
	 *
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
	 *
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

	public boolean isStatusValid(String statusName) {
		List<Status> status = statusRepo.findAll();
		Set<String> statusNames = new HashSet<>();

		for (Status s : status) {
			statusNames.add(s.getStatusName());
		}

		return statusNames.contains(statusName);
	}

}
