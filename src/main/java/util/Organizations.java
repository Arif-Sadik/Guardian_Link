package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for managing organizations in the Guardian Link system.
 * Contains all available organizations that caregivers can be assigned to.
 */
public class Organizations {

    // List of all available organizations
    private static final List<String> ORGANIZATIONS = new ArrayList<>(Arrays.asList(
        "Bright Future Foundation",
        "Children Care Initiative",
        "Community Support Network",
        "Hope for Tomorrow",
        "Children's Welfare Association",
        "Youth Development Program",
        "Safe Haven Shelter",
        "Learning & Growth Center",
        "Family Empowerment Initiative",
        "Child Advocacy Network",
        "Rural Education Project",
        "Urban Children's Mission",
        "Health & Wellness Foundation",
        "Special Needs Support Center",
        "Foster Care Alliance",
        "Peace & Harmony Organization",
        "New Beginnings Care Home",
        "Inclusive Development Program",
        "Sunshine Outreach Foundation",
        "Tomorrow's Leaders Initiative"
    ));

    /**
     * Get all organizations
     */
    public static List<String> getAll() {
        return new ArrayList<>(ORGANIZATIONS);
    }

    /**
     * Get organization by index
     */
    public static String getByIndex(int index) {
        if (index >= 0 && index < ORGANIZATIONS.size()) {
            return ORGANIZATIONS.get(index);
        }
        return null;
    }

    /**
     * Check if organization exists
     */
    public static boolean exists(String organization) {
        return ORGANIZATIONS.contains(organization);
    }

    /**
     * Get total count of organizations
     */
    public static int getCount() {
        return ORGANIZATIONS.size();
    }

    /**
     * Add a new organization (if not already exists)
     */
    public static boolean addOrganization(String organization) {
        if (!exists(organization)) {
            ORGANIZATIONS.add(organization);
            return true;
        }
        return false;
    }
}
