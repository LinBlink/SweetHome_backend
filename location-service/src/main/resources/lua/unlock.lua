-- 将 查询key等 和 删除key 变为原子操作
if redis.call('GET', KEYS[1]) == ARGV[1] then
    return redis.call('del', KEYS[1])
else
    return 0
end