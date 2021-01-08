package net.peihuan.newblog.bean.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Article {
    @TableId
    private Long id;

    private String title;

    /**
     * 现在已发布的标题
     */
    private String publishedTitle;

    private String content;

    private String categories;

    private String tags;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Boolean publish;
    @TableLogic
    private Boolean deleted;
}
