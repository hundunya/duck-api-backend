-- auto-generated definition
create table user_interface
(
    id           bigint auto_increment comment '主键'
        primary key,
    user_id      bigint                              not null comment '用户ID',
    interface_id bigint                              not null comment '接口ID',
    total_num    int       default 0                 not null comment '接口总调用次数',
    left_num     int       default 0                 not null comment '接口剩余调用次数',
    status       int       default 0                 not null comment '状态(0-正常，1-关闭)',
    create_time  timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete    tinyint   default 0                 not null comment '逻辑删除(0-正常，1-删除)'
)
    comment '接口信息表';

