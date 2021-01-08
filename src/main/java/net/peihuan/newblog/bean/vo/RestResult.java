package net.peihuan.newblog.bean.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.peihuan.newblog.bean.enums.ResultEnum;

import java.util.HashMap;
import java.util.Map;

@ToString
@Setter
@Getter
public class RestResult {

    private Integer code;

    private String msg;

    private Object data;
    

    public static RestResult success(Object object) {
        RestResult resultVO = new RestResult();
        resultVO.setData(object);
        resultVO.setCode(ResultEnum.SUCCESS.getCode());
        resultVO.setMsg(ResultEnum.SUCCESS.getMsg());
        return resultVO;
    }

    public static RestResult success() {
        return success(null);
    }

    public static RestResult success(String key, Object value) {
        Map<String, Object> data = new HashMap<>();
        data.put(key, value);
        return success(data);
    }


    public static  RestResult error(ResultEnum resultEnum) {
        RestResult resultVO = new RestResult();
        resultVO.setCode(resultEnum.getCode());
        resultVO.setMsg(resultEnum.getMsg());
        return resultVO;
    }

    public static  RestResult error(ResultEnum resultEnum, String msg) {
        RestResult resultVO = new RestResult();
        resultVO.setCode(resultEnum.getCode());
        resultVO.setMsg(resultEnum.getMsg() + ": " + msg);
        return resultVO;
    }

    public static  RestResult error(Integer code, String msg) {
        RestResult resultVO = new RestResult();
        resultVO.setCode(code);
        resultVO.setMsg(msg);
        return resultVO;
    }
    
}
