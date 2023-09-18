-- auto-generated definition
create table invoke_count_order
(
    id           bigint auto_increment comment '主键'
        primary key,
    out_trade_no varchar(256)                        not null comment '订单号',
    total_amount double                              not null comment '实际应支付的交易金额',
    pay_amount   double                              not null comment '实际支付的金额',
    interface_id bigint                              not null comment '购买接口ID',
    user_id      bigint                              not null comment '用户ID',
    invoke_count int                                 not null comment '购买的接口调用次数',
    status       int       default 0                 not null comment '交易状态，0-待支付，1-已完成，2-取消',
    create_time  timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  timestamp default CURRENT_TIMESTAMP not null comment '更新时间',
    is_delete    int       default 0                 not null comment '逻辑删除，0-正常，1-删除',
    constraint out_trade_node_unique
        unique (out_trade_no)
)
    comment '接口调用次数购买记录订单';

