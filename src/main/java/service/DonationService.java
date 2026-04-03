package service;

import model.entity.Donation;
import model.entity.Child;
import repository.DonationRepository;
import repository.ChildRepository;

import java.util.List;

/**
 * Business logic for donation management.
 */
public class DonationService {

    private final DonationRepository repository = new DonationRepository();
    private final ChildRepository childRepository = new ChildRepository();

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
        boolean saved = repository.save(donation);
        // Automatically set donor as sponsor of the child
        if (saved && donation.getDonorId() > 0 && donation.getChildId() > 0) {
            Child child = childRepository.findById(donation.getChildId());
            if (child != null) {
                child.setSponsorId(donation.getDonorId());
                childRepository.updateChild(child);
            }
        }
        return saved;
    }
}
