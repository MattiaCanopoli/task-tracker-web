package com.tasktracker.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.tasktracker.exception.StatusNotFoundException;
import com.tasktracker.model.Status;
import com.tasktracker.repository.StatusRepo;

/**
 * Service layer responsible for handling business logic related to {@link Status} entities.
 * <p>
 * This Spring-managed service provides methods to retrieve and validate status records
 * from the underlying database via the {@link StatusRepo} repository.
 * <p>
 * It is automatically instantiated and injected where needed by Spring's dependency injection container.
 */
@Service
public class StatusService {

	private final StatusRepo statusRepo;
	
	
    /**
     * Constructs a new instance of {@code StatusService} with the provided repository.
     * <p>
     * This constructor is typically invoked by the Spring container through
     * constructor-based dependency injection.
     *
     * @param statusRepo the repository used to access {@link Status} entities; must not be {@code null}
     */
	public StatusService(StatusRepo statusRepo) {
		this.statusRepo = statusRepo;
	}

	/**
	 * Retrieves a {@link Status} object by its name.
	 *
	 * @param statusName the name of the status to be retrieved
	 * @return the {@link Status} object matching the given name, or {@code null} if not found
	 */
	public Status findStatusByName(String statusName) throws StatusNotFoundException {
		Optional<Status> status = statusRepo.findByStatusName(statusName.toUpperCase());

		if (!status.isPresent()) {
			throw new StatusNotFoundException("Status " + statusName + " cannot be found");
		}

		return status.get();
	}

	/**
	 * Retrieves a {@link Status} object by its unique identifier.
	 *
	 * @param statusId the ID of the status to be retrieved
	 * @return the {@link Status} object with the given ID, or {@code null} if not found
	 */
	public Status findStatusById(int statusId) {
		Optional<Status> status = statusRepo.findById(statusId);

		if (!status.isPresent()) {
			throw new StatusNotFoundException("Status with ID '" + statusId + "' cannot be found");
		}

		return status.get();
	}

	/**
	 * Checks whether a given status name exists among the persisted {@link Status} entities.
	 *
	 * @param statusName the name of the status to validate
	 * @return {@code true} if the status name exists, {@code false} otherwise
	 */
	public boolean isStatusValid(String statusName) {
		List<Status> status = statusRepo.findAll();
		Set<String> statusNames = new HashSet<>();

		for (Status s : status) {
			statusNames.add(s.getStatusName());
		}

		return statusNames.contains(statusName.toUpperCase());
	}

}
