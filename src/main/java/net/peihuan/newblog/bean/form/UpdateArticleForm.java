package net.peihuan.newblog.bean.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

@Data
public class UpdateArticleForm {
    @NotNull
    private Long id;
    @NotBlank
    @Pattern(regexp = "^[^+-./ @#$%*()\\[\\]][^/ @#$%*()\\[\\]]{0,254}$")
    private String title;
    private String content;
    private List<String> categories;
    private List<String> tags;
    private Boolean publish;
}
