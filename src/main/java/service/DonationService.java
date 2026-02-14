package service;

import model.entity.Donation;
import repository.DonationRepository;

import java.util.List;

/**
 * Business logic for donation management.
 */
public class DonationService {

    private final DonationRepository repository = new DonationRepository();

    public List<Donation> getAll() {
        return repository.findAll();
    }

    public List<Donation> getByDonorId(int donorId) {
        return repository.findByDonorId(donorId);
    }

    public List<Donation> getByChildId(int childId) {
        return repository.findByChildId(childId);
    }

    public double getTotalByDonorId(int donorId) {
        return repository.getTotalByDonorId(donorId);
    }

    public double getTotalByChildId(int childId) {
        return repository.getTotalByChildId(childId);
    }

    public int countChildrenByDonorId(int donorId) {
        return repository.countChildrenByDonorId(donorId);
    }

    public boolean save(Donation donation) {
        return repository.save(donation);
    }
}
