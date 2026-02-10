package service;

import model.entity.Child;
import repository.ChildRepository;

import java.util.List;

/**
 * Business logic for child management.
 */
public class ChildService {

    private final ChildRepository childRepository = new ChildRepository();

    /**
     * Adds a new child (automatically approved for demo purposes).
     */
    public void addChild(Child child) {
        childRepository.save(child);
    }

    /**
     * Returns all children.
     */
    public List<Child> getAllChildren() {
        return childRepository.findAll();
    }

    /**
     * Finds a child by their database ID.
     */
    public Child getChildById(int id) {
        return childRepository.findById(id);
    }
}
