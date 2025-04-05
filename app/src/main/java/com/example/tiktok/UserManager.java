package com.example.tiktok;

/**
 * Singleton class to manage user information globally across the application
 */
public class UserManager {
    private static UserManager instance;
    private User currentUser;

    // Private constructor to prevent instantiation
    private UserManager() {
    }

    // Get the singleton instance
    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    // Set the current user
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    // Get the current user
    public User getCurrentUser() {
        return currentUser;
    }

    // Clear user data (for logout)
    public void clearUserData() {
        currentUser = null;
    }
}

