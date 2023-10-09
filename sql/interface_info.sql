-- auto-generated definition
create table interface_info
(
    id              bigint auto_increment comment '主键'
        primary key,
    name            varchar(256) default '测试接口'         not null comment '接口名称',
    description     varchar(256) default '这是一个测试接口' not null comment '接口描述',
    url             varchar(512) default '/api/test'        not null comment '接口地址',
    method          varchar(256) default 'GET'              not null comment '请求类型',
    price           int                                     not null comment '接口调用一次的消耗的金币数量',
    request_header  varchar(512) default '[]'               not null comment '请求头',
    response_header varchar(512) default '[]'               not null comment '响应头',
    request_param   varchar(512) default '[]'               not null comment '请求参数',
    response_param  varchar(512) default '[]'               not null comment '响应参数',
    total_num       bigint       default 0                  not null comment '接口总调用次数',
    status          int          default 0                  not null comment '接口状态(0-关闭，1-关闭)',
    create_user     bigint                                  not null comment '接口创建者ID',
    create_time     timestamp    default CURRENT_TIMESTAMP  not null comment '创建时间',
    update_time     timestamp    default CURRENT_TIMESTAMP  not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete       tinyint      default 0                  not null comment '逻辑删除(0-正常，1-删除)'
)
    comment '接口信息表';

