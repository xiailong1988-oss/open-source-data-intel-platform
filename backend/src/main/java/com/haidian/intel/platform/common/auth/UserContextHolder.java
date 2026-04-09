package com.haidian.intel.platform.common.auth;

/**
 * Thread-local holder for current authenticated user.
 */
public final class UserContextHolder {

    private static final ThreadLocal<AuthenticatedUser> USER_HOLDER = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void setUser(AuthenticatedUser user) {
        USER_HOLDER.set(user);
    }

    public static AuthenticatedUser getUser() {
        return USER_HOLDER.get();
    }

    public static Long getUserIdOrDefault(long defaultValue) {
        AuthenticatedUser user = getUser();
        return user == null || user.getUserId() == null ? defaultValue : user.getUserId();
    }

    public static void clear() {
        USER_HOLDER.remove();
    }
}
