package cloud.hytora.remote.adapter;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class LoginCheckResult {


    public static LoginCheckResult allowed() {
        return new LoginCheckResult(true, null);
    }

    public static LoginCheckResult denied(String reason) {
        return new LoginCheckResult(false, reason);
    }

    private final boolean state;
    private final String reason;




    public boolean isLoginAllowed() {
        return state;
    }

    public boolean isLoginDenied() {
        return !isLoginAllowed();
    }
}
