-- KEY[1] 红包分配好金额的list的key
-- KEY[2] 抢到红包的用户的hash的key
-- KEY[3] 抢红包的入库消息队列key
-- ARGV[1] 放哪个用户在抢userId
-- ARGV[2] 放红包id redpacketId

-- 判断有没有抢过。返回1有，0没有
if redis.call('HEXISTS', KEYS[2], ARGV[1]) == 1 then
    return -1;  -- 返回-1表示抢过了
else
    -- 没有抢过，直接抢，拿到抢到的金额
    local amountGrabbed = redis.call('LPOP', KEYS[1])
    -- 注意一个坑！LPOP默认返回的是字符串
    if amountGrabbed == false then
        return 0; -- 返回0表示抢光了
    end

    -- 加入抢到红包的记录到hash中。键是用户id，值是抢到的金额
    redis.call('HSET', KEYS[2], ARGV[1], amountGrabbed);

    -- 将抢红包记录到消息队列，消费者消费时入库
    redis.call('XADD', KEYS[3], '*',
            'redpacketId', ARGV[2],
            'userId', ARGV[1],
            'amount', amountGrabbed
    )

    return tonumber(amountGrabbed); -- 返回具体抢到的数值

end