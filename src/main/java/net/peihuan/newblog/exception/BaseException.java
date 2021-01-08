package net.peihuan.newblog.exception;

import lombok.Getter;
import lombok.Setter;
import net.peihuan.newblog.bean.enums.ResultEnum;

@Getter
@Setter
public class BaseException extends RuntimeException {

    private static final long serialVersionUID = -3167551783151379425L;

    private Integer code;

    private String msg;

    public BaseException(ResultEnum resultEnum) {
        super(resultEnum.getMsg());
        this.msg = resultEnum.getMsg();
        this.code = resultEnum.getCode();
    }


    public BaseException(ResultEnum resultEnum, String msg) {
        super(resultEnum.getMsg() + " " + msg);
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getMsg() + " " + msg;
    }

}
