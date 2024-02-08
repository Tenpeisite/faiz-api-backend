package com.zhj.project.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhj.common.constant.EmailConstant;
import com.zhj.common.constant.RedisConstant;
import com.zhj.common.constant.UserConstant;
import com.zhj.common.model.dto.user.*;
import com.zhj.common.model.entity.User;
import com.zhj.common.model.enums.UserAccountStatusEnum;
import com.zhj.common.model.vo.UserVO;
import com.zhj.common.utils.ErrorCode;
import com.zhj.project.exception.BusinessException;
import com.zhj.project.mapper.UserMapper;
import com.zhj.project.service.UserService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.zhj.common.constant.UserConstant.ADMIN_ROLE;
import static com.zhj.common.constant.UserConstant.USER_LOGIN_STATE;


/**
 * 用户服务实现类
 *
 * @author zhj
 */
@Service
@Slf4j
@Data
@ConfigurationProperties(prefix = "wx.open")
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {


    private String appId;

    private String secret;

    private String redirectUrl;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 盐值，混淆密码
     */
    //private static final String SALT = "yupi";
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String userName = userRegisterRequest.getUserName();
        String invitationCode = userRegisterRequest.getInvitationCode();
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userName.length() > 40) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称过长");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 6 || checkPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = userMapper.selectCount(queryWrapper);
            //Long count = lambdaQuery().eq(User::getUserAccount, userAccount).count();
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword).getBytes());
            String accessKey = generateUniqueKey(userAccount, RandomUtil.randomNumbers(5));
            String secretKey = generateUniqueKey(userAccount, RandomUtil.randomNumbers(8));

            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setUserName(userName);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            user.setInvitationCode(generateRandomString(8));

            if (StringUtils.isNotBlank(invitationCode)) {
                //查询邀请用户
                User invitUser = lambdaQuery().eq(User::getInvitationCode, invitationCode).one();

                if (invitUser != null) {
                    user.setBalance(100);
                    invitUser.setBalance(invitUser.getBalance() + 100);
                    updateById(invitUser);
                } else {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "该邀请码无效");
                }
            }
            //保存数据
            boolean saveResult = this.save(user);

            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }


    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        //用户被封禁
        if (user.getStatus().equals(UserAccountStatusEnum.BAN.getValue())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户账号封禁中");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return user;
    }

    @Override
    public boolean addWalletBalance(Long userId, Integer addPoints) {
        LambdaUpdateWrapper<User> userLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userLambdaUpdateWrapper.eq(User::getId, userId);
        userLambdaUpdateWrapper.setSql("balance = balance + " + addPoints);
        return this.update(userLambdaUpdateWrapper);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public UserVO getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (user.getStatus().equals(1)) {
            throw new BusinessException(ErrorCode.PROHIBITED, "账号已封禁");
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && ADMIN_ROLE.equals(user.getUserRole());
    }

    @Override
    public User isTourist(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        return currentUser == null || currentUser.getId() == null ? null : currentUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public boolean updateKey(Long id) {
        User user = getById(id);
        String accessKey = generateUniqueKey(user.getUserAccount(), RandomUtil.randomNumbers(5));
        String secretKey = generateUniqueKey(user.getUserAccount(), RandomUtil.randomNumbers(8));
        user.setAccessKey(accessKey);
        user.setSecretKey(secretKey);
        return updateById(user);
    }

    public static String generateUniqueKey(String userAccount, String random) {
        UUID uuid = UUID.randomUUID();
        String combinedString = UserConstant.SALT + userAccount + random + uuid.toString();
        // 使用MD5哈希算法生成Key
        String key = DigestUtil.md5Hex(combinedString);
        return key;
    }

    //远程调用微信拿令牌，拿到令牌查询用户信息，将用户信息写入数据库
    @Override
    public User wxAuth(String code) {
        //拿授权码获取access_token
        Map<String, String> access_token = getAccess_token(code);
        log.debug("access_token:{}", access_token);
        String token = access_token.get("access_token");
        String openid = access_token.get("openid");
        //通过令牌，获取用户信息
        Map<String, String> userInfo = getUserInfo(token, openid);
        //判断是否是第一次登录,是则注册，不是则直接返回
        UserServiceImpl proxy = (UserServiceImpl) AopContext.currentProxy();
        User user = proxy.addUser(userInfo);
        return user;
    }

    @Override
    public UserVO userBindEmail(UserBindEmailRequest userBindEmailRequest, HttpServletRequest request) {
        String emailAccount = userBindEmailRequest.getEmailAccount();
        String captcha = userBindEmailRequest.getCaptcha();

        //校验邮箱格式
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(emailPattern, emailAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不合法的邮箱地址！");
        }
        //校验用户名和验证码
        if (StringUtils.isBlank(captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码不能为空");
        }
        //校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(EmailConstant.CAPTCHA_CACHE_KEY + emailAccount);
        if (StringUtils.isBlank(cacheCode)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码已过期");
        }
        if (!captcha.equals(cacheCode)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码不匹配");
        }

        //查询该邮箱是否已经绑定
        Long count = lambdaQuery().eq(User::getEmail, emailAccount).or().eq(User::getUserAccount, emailAccount).count();
        if (count > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "此邮箱已被绑定,请更换新的邮箱！");
        }
        UserVO userVO = getLoginUser(request);
        userVO.setEmail(emailAccount);
        User user = new User();
        user.setId(userVO.getId());
        user.setEmail(emailAccount);
        updateById(user);
        return userVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long userEmailRegister(UserEmailRegisterRequest userEmailRegisterRequest) {
        String emailAccount = userEmailRegisterRequest.getEmailAccount();
        String captcha = userEmailRegisterRequest.getCaptcha();
        String userName = userEmailRegisterRequest.getUserName();
        String invitationCode = userEmailRegisterRequest.getInvitationCode();

        //校验邮箱格式
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(emailPattern, emailAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不合法的邮箱地址！");
        }
        //校验用户名和验证码
        if (StringUtils.isAnyBlank(userName, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        //校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(EmailConstant.CAPTCHA_CACHE_KEY + emailAccount);
        if (StringUtils.isBlank(cacheCode)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码已过期");
        }
        //验证码不匹配
        if (!captcha.equals(cacheCode)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码不匹配");
        }

        synchronized (emailAccount.intern()) {
            Long count = lambdaQuery().eq(User::getEmail, emailAccount).or().eq(User::getEmail, emailAccount).count();
            if (count > 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "该邮箱已被绑定");
            }

            String accessKey = generateUniqueKey(emailAccount, RandomUtil.randomNumbers(5));
            String secretKey = generateUniqueKey(emailAccount, RandomUtil.randomNumbers(8));

            User user = new User();
            user.setUserName(userName);
            user.setEmail(emailAccount);
            user.setUserAccount(emailAccount);
            user.setInvitationCode(generateRandomString(6));
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);

            boolean flag1 = false;
            if (StringUtils.isNotBlank(invitationCode)) {
                //查询邀请用户
                User invitUser = lambdaQuery().eq(User::getInvitationCode, invitationCode).one();

                if (invitUser != null) {
                    user.setBalance(100);
                    invitUser.setBalance(invitUser.getBalance() + 100);
                    flag1 = updateById(invitUser);
                } else {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "该邀请码无效");
                }
            }

            boolean flag2 = save(user);
            if (!(flag1 && flag2)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
            }
            return user.getId();
        }
    }

    @Override
    public UserVO userUnBindEmail(UserUnBindEmailRequest userUnBindEmailRequest, HttpServletRequest request) {
        String emailAccount = userUnBindEmailRequest.getEmailAccount();
        String captcha = userUnBindEmailRequest.getCaptcha();

        //校验邮箱格式
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(emailPattern, emailAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不合法的邮箱地址！");
        }
        //校验用户名和验证码
        if (StringUtils.isBlank(captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码不能为空");
        }
        //校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(EmailConstant.CAPTCHA_CACHE_KEY + emailAccount);
        if (StringUtils.isBlank(cacheCode)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码已过期");
        }
        if (!captcha.equals(cacheCode)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码不匹配");
        }

        UserVO loginUser = getLoginUser(request);
        User user = new User();
        user.setId(loginUser.getId());
        user.setEmail("");
        boolean flag = updateById(user);
        if (!flag) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "邮箱解绑失败,请稍后再试！");
        }
        loginUser.setEmail(null);
        return loginUser;
    }

    @Override
    public UserVO userEmailLogin(UserEmailLoginRequest userEmailLoginRequest, HttpServletRequest request) {
        String emailAccount = userEmailLoginRequest.getEmailAccount();
        String captcha = userEmailLoginRequest.getCaptcha();

        //校验邮箱格式
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!Pattern.matches(emailPattern, emailAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不合法的邮箱地址！");
        }
        //校验用户名和验证码
        if (StringUtils.isBlank(captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码不能为空");
        }
        //校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(EmailConstant.CAPTCHA_CACHE_KEY + emailAccount);
        if (StringUtils.isBlank(cacheCode)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码已过期");
        }
        if (!captcha.equals(cacheCode)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码不匹配");
        }

        User user = lambdaQuery().eq(User::getUserAccount, emailAccount).one();
        if (user == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户不存在");
        }

        //用户被封禁
        if (user.getStatus().equals(UserAccountStatusEnum.BAN.getValue())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户账号封禁中");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Transactional
    public User addUser(Map<String, String> userInfo) {
        String unionid = userInfo.get("unionid");
        User user = lambdaQuery().eq(StringUtils.isNotBlank(unionid), User::getUserAccount, unionid).one();
        if (user != null) {
            return user;
        }
        // 创建用户
        user = new User();
        // 账号
        user.setUserAccount(unionid);
        // 密码
        String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + "123456789").getBytes());
        user.setUserPassword(encryptPassword);
        // 昵称
        String nickname = userInfo.get("nickname");
        user.setUserName(nickname);
        // 头像
        String img = userInfo.get("headimgurl");
        user.setUserAvatar(img);
        userMapper.insert(user);
        return user;
    }

    /***
     * @description 携带授权码申请令牌
     * https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
     * @param code 授权码
     * @return
     * {
     * "access_token":"ACCESS_TOKEN",
     * "expires_in":7200,
     * "refresh_token":"REFRESH_TOKEN",
     * "openid":"OPENID",
     * "scope":"SCOPE",
     * "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
     * }
     * @author zhj
     */
    private Map<String, String> getAccess_token(String code) {
        String url_template = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        //请求路径
        String url = String.format(url_template, appId, secret, code);
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, null, String.class);
        //获取响应结果
        String result = exchange.getBody();
        //将结果转成map
        Map<String, String> map = JSON.parseObject(result, Map.class);
        return map;
    }


    /***
     * @param access_token
     * @param openid
     * http请求方式: GET
     * https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID
     *
     * 返回结果
     * {
     * "openid":"OPENID",
     * "nickname":"NICKNAME",
     * "sex":1,
     * "province":"PROVINCE",
     * "city":"CITY",
     * "country":"COUNTRY",
     * "headimgurl": "https://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0",
     * "privilege":[
     * "PRIVILEGE1",
     * "PRIVILEGE2"
     * ],
     * "unionid": " o6_bmasdasdsad6_2sgVt7hMZOPfL"
     *
     * }
     * @author 朱焕杰
     */
    private Map<String, String> getUserInfo(String access_token, String openid) {
        String url_template = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        String url = String.format(url_template, access_token, openid);
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        String result = new String(exchange.getBody().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        Map<String, String> map = JSON.parseObject(result, Map.class);
        return map;
    }


    /**
     * 生成随机字符串
     *
     * @param length 长
     * @return {@link String}
     */
    public String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            char randomChar = characters.charAt(index);
            sb.append(randomChar);
        }
        return sb.toString();
    }

}




