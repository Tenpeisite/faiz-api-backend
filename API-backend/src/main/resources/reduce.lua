-- 1.参数列表
-- 1.1 用户id
local userId = ARGV[1]
-- 1.2 接口id
--local interfaceInfoId = ARGV[2]
-- 1.3 key前缀
local prefix = KEYS[1]

-- 2. 用户对应的接口key
--local key = prefix .. userId .. ':' .. interfaceInfoId
local key = prefix .. userId

-- 3. 业务脚本
-- 3.1 判断剩余次数是否充足
if (tonumber(redis.call('get', key)) <= 0) then
    -- 剩余调用次数不足，返回1
    return 1
end
-- 3.2 剩余次数充足,自减1 decr
redis.call('decr',key)
return 0
