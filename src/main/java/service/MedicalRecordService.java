package service;

import model.entity.MedicalRecord;
import repository.MedicalRecordRepository;

import java.util.List;

/**
 * Business logic for medical records.
 */
public class MedicalRecordService {

    private final MedicalRecordRepository repository = new MedicalRecordRepository();

    /**
     * Returns all medical records for a given child.
     */
    public List<MedicalRecord> getRecordsByChildId(int childId) {
        return repository.findByChildId(childId);
    }

    /**
     * Saves a new medical record.
     */
    public void addRecord(MedicalRecord record) {
        repository.save(record);
    }
}
