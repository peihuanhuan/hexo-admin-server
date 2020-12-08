create database blog;
create table blog.article (
         id bigint auto_increment primary key,
         title varchar(64),
         published_title varchar(64),
         content text ,
         categories varchar(255) default '',
         tags varchar(255) default '' ,
         create_time datetime default current_timestamp ,
         update_time datetime default current_timestamp ,
         publish tinyint default 0 ,
         deleted tinyint default 0
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;


create table blog.user (
         id bigint auto_increment primary key,
         username varchar(64) unique,
         password varchar(255),
         deleted tinyint default 0
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

insert into blog.user values(1, 'admin', md5('123456'), 0);

--  如果需要首页
insert into article values (1,"index","index","","","","2100-10-10 00:00:00",now(),1,0);