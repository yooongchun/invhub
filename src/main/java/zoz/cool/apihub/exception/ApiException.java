package zoz.cool.apihub.exception;

import lombok.Getter;
import zoz.cool.apihub.enums.HttpCode;

@Getter
public class ApiException extends RuntimeException {
    private HttpCode errorCode;

    public ApiException(HttpCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ApiException(String message) {
        super(message);
    }


    public ApiException(HttpCode errCode, String message) {
        super(message);
        this.errorCode = errCode;
    }

    public ApiException(Throwable cause) {
        super(cause);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

}
