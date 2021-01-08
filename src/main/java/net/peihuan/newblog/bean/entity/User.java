package net.peihuan.newblog.bean.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;


@Data
public class User {
    @TableId
    private Long id;
    private String username;
    private String password;
}
