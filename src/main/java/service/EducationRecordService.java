package service;

import model.entity.EducationRecord;
import repository.EducationRecordRepository;

import java.util.List;

/**
 * Business logic for education records.
 */
public class EducationRecordService {

    private final EducationRecordRepository repository = new EducationRecordRepository();

    /**
     * Returns all education records for a given child.
     */
    public List<EducationRecord> getRecordsByChildId(int childId) {
        return repository.findByChildId(childId);
    }

    /**
     * Saves a new education record.
     */
    public void addRecord(EducationRecord record) {
        repository.save(record);
    }
}
