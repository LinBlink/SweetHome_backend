if redis.call('GET', KEYS[1]) == ARGV[1] then
    return redis.call('del', KEYS[1])
else
    return 0
end