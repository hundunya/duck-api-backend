-- auto-generated definition
create table gold_coin_goods
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    name        varchar(50)                         not null comment '商品名称',
    description varchar(256)                        not null comment '商品描述',
    number      int       default 0                 not null comment '金币数量',
    price       double    default 0                 not null comment '商品价格，默认为0',
    create_user bigint                              not null comment '创建者',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp default CURRENT_TIMESTAMP not null comment '更新时间',
    is_delete   int       default 0                 not null comment '逻辑删除，0-正常，1-删除'
)
    comment '金币商城';

