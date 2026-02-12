package service;

import repository.RolePermissionsRepository;

import java.util.Map;

/**
 * Business logic for role permissions management.
 */
public class RolePermissionsService {

    private final RolePermissionsRepository repository = new RolePermissionsRepository();

    /**
     * Returns all role permissions.
     */
    public Map<String, String> getAllPermissions() {
        return repository.findAll();
    }

    /**
     * Updates permissions for a specific role.
     * 
     * @return true if updated successfully, false otherwise
     */
    public boolean updatePermissions(String roleName, String permissions) {
        return repository.updatePermissions(roleName, permissions);
    }

    /**
     * Gets permissions for a specific role.
     */
    public String getPermissions(String roleName) {
        return repository.getPermissions(roleName);
    }
}
