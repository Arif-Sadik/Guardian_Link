package service;

import model.entity.SystemLog;
import repository.SystemLogRepository;

import java.util.List;

/**
 * Business logic for system log management.
 */
public class SystemLogService {

    private final SystemLogRepository repository = new SystemLogRepository();

    public List<SystemLog> getAll() {
        return repository.findAll();
    }

    public List<SystemLog> getRecent(int limit) {
        return repository.findRecent(limit);
    }

    public int getCount() {
        return repository.count();
    }

    public void save(SystemLog log) {
        repository.save(log);
    }
}
