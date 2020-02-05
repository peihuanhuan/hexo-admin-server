package net.peihuan.newblog.exception;

/**
 * @author earthchen
 * @date 2019/12/4
 **/

public class ForbiddenException extends RuntimeException {

    private static final long serialVersionUID = -5908295826336735304L;

    public ForbiddenException(String msg) {
        super(msg);
    }
}
