package net.peihuan.newblog.bean.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

@Data
public class LoginForm {

    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
