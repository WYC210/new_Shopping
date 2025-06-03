package com.wyc.service.impl;

import com.wyc.domain.dto.LoginDTO;
import com.wyc.domain.po.Users;
import com.wyc.domain.vo.BrowsingRecordVO;
import com.wyc.domain.vo.PasswordUpdateVO;
import com.wyc.domain.vo.UserCouponVO;
import com.wyc.domain.vo.UserDetailsVO;
import com.wyc.domain.vo.UserProfileUpdateVO;
import com.wyc.domain.vo.UserRegisterVO;
import com.wyc.exception.ServiceException;
import com.wyc.mapper.UserCouponMapper;
import com.wyc.mapper.UsersMapper;
import com.wyc.mapper.VisitorBrowsingRecordMapper;
import com.wyc.mapper.VisitorMapper;
import com.wyc.security.SecurityUserDetails;
import com.wyc.service.IUserService;
import com.wyc.utils.JwtTokenUtil;
import com.wyc.utils.R;
import com.wyc.utils.RedisCache;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements IUserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Autowired
    private VisitorBrowsingRecordMapper browsingRecordMapper;

    @Autowired
    private VisitorMapper visitorMapper;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisCache redisCache;

    @Value("${jwt.tokenHeader}")
    private String tokenHeader;

    @Value("${jwt.tokenPrefix}")
    private String tokenPrefix;

    @Override
    @Transactional
    public void register(UserRegisterVO registerVO) {
        logger.info("开始注册用户: username={}, email={}, phone={}",
                registerVO.getUsername(), registerVO.getEmail(), registerVO.getPhone());

        // 验证用户名是否已存在
        if (usersMapper.selectByUsername(registerVO.getUsername()) != null) {
            logger.error("用户名已存在: {}", registerVO.getUsername());
            throw new ServiceException("用户名已存在: " + registerVO.getUsername());
        }

        // 验证邮箱是否已存在
        if (usersMapper.selectByEmail(registerVO.getEmail()) != null) {
            logger.error("邮箱已被注册: {}", registerVO.getEmail());
            throw new ServiceException("邮箱已被注册: " + registerVO.getEmail());
        }

        // 验证手机号是否已存在
        if (usersMapper.selectByPhone(registerVO.getPhone()) != null) {
            logger.error("手机号已被注册: {}", registerVO.getPhone());
            throw new ServiceException("手机号已被注册: " + registerVO.getPhone());
        }

        // 创建新用户
        Users user = new Users();
        user.setUsername(registerVO.getUsername());
        String encodedPassword = passwordEncoder.encode(registerVO.getPassword());
        logger.info("密码加密结果: {}", encodedPassword);
        user.setPasswordHash(encodedPassword);
        user.setEmail(registerVO.getEmail());
        user.setPhone(registerVO.getPhone());
        user.setBalance(new java.math.BigDecimal("0.00"));
        user.setIsDeleted(Boolean.FALSE);
        user.setUuid(UUID.randomUUID().toString());

        usersMapper.insert(user);
        logger.info("用户注册成功: userId={}, username={}", user.getUserId(), user.getUsername());
    }

    @Override
    public String login(LoginDTO loginDTO) {
        logger.info("开始登录: username={}", loginDTO.getUsername());

        try {
            // 用户认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 从认证对象中获取用户信息
            SecurityUserDetails userDetails = (SecurityUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getUserId();
            logger.info("用户认证成功: userId={}, username={}", userId, userDetails.getUsername());

            // 生成token
            String token = jwtTokenUtil.generateToken(userDetails);
            logger.info("生成token成功: userId={}", userId);

            // 登录成功后同步游客浏览记录
            if (loginDTO.getFingerprint() != null) {
                logger.info("同步游客浏览记录: userId={}, fingerprint={}", userId, loginDTO.getFingerprint());
                syncBrowsingRecordsOnLogin(userId, loginDTO.getFingerprint());
            }

            return token;
        } catch (Exception e) {
            logger.error("登录失败: username={}, error={}", loginDTO.getUsername(), e.getMessage());
            throw e;
        }
    }

    @Override
    public void logout(HttpServletRequest request) {
        String token = request.getHeader(tokenHeader);
        if (StringUtils.hasText(token) && token.startsWith(tokenPrefix)) {
            token = token.substring(tokenPrefix.length());
            // 将token加入黑名单
            redisCache.setCacheObject("token_blacklist:" + token, true);
        }
        SecurityContextHolder.clearContext();
    }

    @Override
    public R getUserDetails(Long userId) {
        Users user = usersMapper.selectByIdIncludeDeleted(userId);
        System.out.println("哈哈哈" + user + userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new ServiceException("用户已被删除");
        }

        UserDetailsVO detailsVO = new UserDetailsVO();
        BeanUtils.copyProperties(user, detailsVO);
        return R.ok(detailsVO);
    }

    @Override
    @Transactional
    public void updateBalance(Long userId, Double amount) {
        Users user = usersMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        // 检查余额是否足够（如果是减少余额）
        if (amount < 0
                && user.getBalance().add(new java.math.BigDecimal(amount)).compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new ServiceException("余额不足");
        }

        usersMapper.updateBalance(userId, amount);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, PasswordUpdateVO passwordUpdateVO) {
        Users user = usersMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        // 验证旧密码
        String oldPassword = usersMapper.getPassword(userId);
        if (!passwordEncoder.matches(passwordUpdateVO.getOldPassword(), oldPassword)) {
            throw new ServiceException("旧密码错误");
        }

        // 更新密码
        String newPasswordHash = passwordEncoder.encode(passwordUpdateVO.getNewPassword());
        usersMapper.updatePassword(userId, newPasswordHash);
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, UserProfileUpdateVO profileUpdateVO) {
        Users user = usersMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        // 验证邮箱是否已被其他用户使用
        if (StringUtils.hasText(profileUpdateVO.getEmail())) {
            Users existingUser = usersMapper.selectByEmail(profileUpdateVO.getEmail());
            if (existingUser != null && !existingUser.getUserId().equals(userId)) {
                throw new ServiceException("邮箱已被其他用户使用");
            }
        }

        // 验证手机号是否已被其他用户使用
        if (StringUtils.hasText(profileUpdateVO.getPhone())) {
            Users existingUser = usersMapper.selectByPhone(profileUpdateVO.getPhone());
            if (existingUser != null && !existingUser.getUserId().equals(userId)) {
                throw new ServiceException("手机号已被其他用户使用");
            }
        }

        usersMapper.updateProfile(userId, profileUpdateVO);
    }

    @Override
    public List<UserCouponVO> getUserCoupons(Long userId, String status) {
        return userCouponMapper.getUserCoupons(userId, status);
    }

    @Override
    public List<BrowsingRecordVO> getBrowsingHistory(Long userId) {
        return browsingRecordMapper.getBrowsingHistory(userId);
    }

    @Override
    public void recordBrowsing(String fingerprint, Long productId, String productName) {
        // 未登录用户，记录到游客缓存
        recordVisitorBrowsing(fingerprint, productId, productName);
    }

    /**
     * 登录后同步游客浏览记录到用户浏览记录表，并清理缓存
     */
    public void syncBrowsingRecordsOnLogin(Long userId, String fingerprint) {
        String key = "visitor:browsing:" + fingerprint;
        List<BrowsingRecordVO> records = redisCache.getCacheList(key);
        if (records != null && !records.isEmpty()) {
            for (BrowsingRecordVO record : records) {
                // 将LocalDateTime转为java.util.Date
                java.util.Date viewedAt = record.getViewedAt() == null ? null
                        : java.sql.Timestamp.valueOf(record.getViewedAt());
                browsingRecordMapper.insertUserBrowsingRecord(userId, record.getProductId(), record.getProductName(),
                        viewedAt);
            }
            redisCache.deleteObject(key);
        }
        // 关联Visitor表
        visitorMapper.bindUserIdToFingerprint(fingerprint, userId);
    }

    /**
     * 游客记录浏览，缓存三天
     */
    public void recordVisitorBrowsing(String fingerprint, Long productId, String productName) {
        String key = "visitor:browsing:" + fingerprint;
        BrowsingRecordVO record = new BrowsingRecordVO(productId, productName, new java.util.Date());
        redisCache.addToList(key, record);
        redisCache.expire(key, 3, java.util.concurrent.TimeUnit.DAYS);
    }
}
