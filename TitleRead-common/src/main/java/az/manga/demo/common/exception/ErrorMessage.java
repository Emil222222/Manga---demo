package az.manga.demo.common.exception;

public final class ErrorMessage {

    private ErrorMessage() {}

    public static final String PASSWORDS_DO_NOT_MATCH = "Passwords do not match";
    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String TOKEN_EXPIRED = "Token has expired";
    public static final String TOKEN_INVALID = "Token is invalid";
    public static final String USER_ROLE_NOT_FOUND = "User role not found";

    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_ALREADY_EXISTS = "User with this email already exists";
    public static final String ACCOUNT_DEACTIVATED = "Account deactivated";

    public static final String MANGA_NOT_FOUND = "Manga not found";

    public static final String CHAPTER_NOT_FOUND = "Chapter not found";
    public static final String CHAPTER_ALREADY_EXISTS = "Chapter with this number already exists";

    public static final String PAGE_NOT_FOUND = "Page not found";

    public static final String GENRE_ALREADY_EXISTS = "Genre with this slug already exists";
    public static final String GENRE_NOT_FOUND = "Genre not found";

    public static final String TAG_NOT_FOUND ="Tag not found";
    public static final String TAG_ALREADY_EXISTS = "Tag with this name already exists";

    public static final String COMMENT_NOT_FOUND = "Comment not found";

    public static final String ALREADY_IN_FAVORITES ="Manga already in favorites";
    public static final String FAVORITE_NOT_FOUND ="Manga not favorite";

    public static final String HISTORY_NOT_FOUND ="History not found";

    public static final String ACCESS_DENIED = "Access denied";
    public static final String UNAUTHORIZED = "Unauthorized";

    public static final String SOMETHING_WENT_WRONG = "Something went wrong";
}

