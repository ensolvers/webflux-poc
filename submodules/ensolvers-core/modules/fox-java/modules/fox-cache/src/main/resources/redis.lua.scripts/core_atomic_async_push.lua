local push_in_chunks = function (key, args)
    local step = 7000
    for i = 1, #args, step do
        redis.call('RPUSH', key, unpack(args, i, math.min(i + step - 1, #args)))
    end
end

local key = KEYS[1]
local is_empty_key = key .. '-is-empty'

local ttlInSeconds = KEYS[2]
local is_async = KEYS[3]

if (is_async == 'true') then
    redis.call('DEL', key)
    redis.call('DEL', is_empty_key)
end;

local exists = tonumber(redis.call('EXISTS', key))
local is_empty = tonumber(redis.call('EXISTS', is_empty_key))

if (exists == 0 and is_empty == 0) then
    if (#ARGV == 0) then
        redis.call('SET', is_empty_key, 'true')
        redis.call('EXPIRE', is_empty_key, ttlInSeconds)
    else
       push_in_chunks(key, ARGV)
       redis.call('EXPIRE', key, ttlInSeconds)
    end
    return true
else
  return false
end;