package com.example.todosapp.Utils;

public class FirebaseConstants {
    public static final int timeOut = 5000;
    public static class ERROR {
        public static String USER_NOT_EXIST = "There is no user record corresponding to this identifier. The user may have been deleted.";
        public static String PASSWORD_INVALID = "The password is invalid or the user does not have a password.";
    }

    public static class REFERENCES {
        public static final String ID = "id";
        public static String USERS = "USERS";
        public static String TAGS = "tags";
        public static String TASKS = "tasks";
        public static String COMPLETE ="complete";
        public static String COMPLETED_DATE ="completedDate";
    }
}
