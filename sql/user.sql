-- auto-generated definition
create table user
(
    id                bigint auto_increment comment 'id'
        primary key,
    user_account      varchar(256)                           not null comment '账号',
    user_password     varchar(512)                           not null comment '密码',
    user_name         varchar(256)                           null comment '用户昵称',
    user_avatar       varchar(1024)                          null comment '用户头像',
    signature         varchar(100) default ''                not null comment '签名',
    user_role         varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    gold_coin_balance int          default 100               not null comment '金币余额',
    access_key        varchar(256)                           null comment 'API账号',
    secret_key        varchar(256)                           null comment 'API密钥',
    create_time       timestamp    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time       timestamp    default CURRENT_TIMESTAMP not null comment '更新时间',
    is_delete         tinyint      default 0                 not null comment '是否删除',
    constraint access_key
        unique (access_key)
)
    comment '用户' collate = utf8mb4_unicode_ci;

