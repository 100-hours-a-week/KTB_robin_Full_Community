package ktb3.fullstack.week4.domain.users;

public enum Role {
    USER, ADMIN;

    String toString(Role role) {
        return String.valueOf(role);
    }
}
