package net.peihuan.newblog.handler;

import lombok.extern.slf4j.Slf4j;
import net.peihuan.newblog.bean.enums.ResultEnum;
import net.peihuan.newblog.bean.vo.RestResult;
import net.peihuan.newblog.exception.BaseException;
import net.peihuan.newblog.exception.ForbiddenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Objects;


@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(value = BaseException.class)
    public RestResult badRequestException(BaseException e) {
        return RestResult.error(e.getCode(), e.getMsg());
    }


    @ExceptionHandler({Exception.class, Throwable.class})
    public RestResult handlerException(Exception e) {
        log.error("服务器未知异常", e);
        return RestResult.error(ResultEnum.SYS_ERROR);
    }

    @ExceptionHandler({ForbiddenException.class})
    public RestResult handlerException(ForbiddenException e) {
        return RestResult.error(ResultEnum.FORBIDDEN);
    }


    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public RestResult handlerException(MethodArgumentTypeMismatchException e) {
        return RestResult.error(404, "404 not found");
    }


    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public RestResult messageNotReadableException(HttpMessageNotReadableException e) {
        log.error(e.getMessage(), e);
        return RestResult.error(ResultEnum.PARAMS_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public RestResult handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);
        String[] str = Objects.requireNonNull(e.getBindingResult().getAllErrors().get(0).getCodes())[1].split("\\.");
        return RestResult.error(ResultEnum.PARAMS_ERROR, str[1] + ":" + e.getBindingResult()
                .getAllErrors().get(0).getDefaultMessage());
    }


    @ExceptionHandler(BindException.class)
    public RestResult handleMethodArgumentNotValidException(BindException e) {
        log.error(e.getMessage(), e);
        String[] str = Objects.requireNonNull(e.getBindingResult().getAllErrors().get(0).getCodes())[1].split("\\.");
        return RestResult.error(ResultEnum.PARAMS_ERROR, str[1] + ":" + e.getBindingResult()
                .getAllErrors().get(0).getDefaultMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public RestResult handleMethodArgumentNotValidException(HttpRequestMethodNotSupportedException e) {
        log.info("http方法不支持 e={}", e.getMethod());
        return RestResult.error(ResultEnum.HTTP_METHOD_NOT_SUPPORT);
    }

}
