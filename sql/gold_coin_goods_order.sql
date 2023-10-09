-- auto-generated definition
create table gold_coin_goods_order
(
    id           bigint auto_increment comment '主键ID'
        primary key,
    user_id      bigint                                  not null comment '购买者ID',
    out_trade_no varchar(256)                            not null comment '订单号',
    name         varchar(256)                            not null comment '订单名称',
    description  varchar(1024) default ''                not null comment '订单描述',
    number       int                                     not null comment '金币数量',
    total_amount double        default 0                 not null comment '实际应支付的交易金额（元）',
    pay_amount   double        default 0                 null comment '实际支付的交易金额',
    status       int           default 0                 not null comment '订单状态，0-待支付，1-已完成，2-取消',
    create_time  timestamp     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  timestamp     default CURRENT_TIMESTAMP not null comment '更新时间',
    is_delete    int           default 0                 not null comment '逻辑删除，0-正常，1-删除'
)
    comment '订单表';

