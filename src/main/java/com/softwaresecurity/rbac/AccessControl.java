
package com.softwaresecurity.rbac;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AccessControl {

    private Map<String, Set<String>> rolePermissions = new HashMap<>();

    public AccessControl() {
        addPermission("Admin", "Create");
        addPermission("Admin", "View");
        addPermission("Admin", "Update");
        addPermission("Admin", "Delete");

        addPermission("Manager", "View");
        addPermission("Manager", "Update");

        addPermission("Customer", "View");
    }

    public void addPermission(String role, String permission) {
        rolePermissions.putIfAbsent(role, new HashSet<>());
        rolePermissions.get(role).add(permission);
    }

    public void removePermission(String role, String permission) {
        if (rolePermissions.containsKey(role)) {
            rolePermissions.get(role).remove(permission);
        }
    }

    public boolean canAccess(String role, String action) {
        if (!rolePermissions.containsKey(role)) {
            return false;
        }

        return rolePermissions.get(role).contains(action);
    }

    public Set<String> getPermissions(String role) {
        if (rolePermissions.containsKey(role)) {
            return new HashSet<>(rolePermissions.get(role));
        }

        return new HashSet<>();
    }
}
