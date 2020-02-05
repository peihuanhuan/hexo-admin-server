package net.peihuan.newblog.bean.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String title;

    private String publishedTitle;

    private String content;

    private List categories;

    private List tags;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime updateTime;

    private Boolean publish;
}
