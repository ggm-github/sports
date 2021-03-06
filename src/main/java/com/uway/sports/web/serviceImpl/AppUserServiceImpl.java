package com.uway.sports.web.serviceImpl;


import com.uway.sports.common.redis.SharedRedisClient;
import com.uway.sports.common.service.BaseServiceImpl;
import com.uway.sports.common.utils.JSONUtils;
import com.uway.sports.common.utils.StringUtils;
import com.uway.sports.web.dao.IAppUserDao;
import com.uway.sports.web.entity.CommConstantEntity;
import com.uway.sports.web.entity.dbentity.*;
import com.uway.sports.web.entity.excel.UserExport;
import com.uway.sports.web.entity.paraentity.NoticeTaskRequest;
import com.uway.sports.web.entity.paraentity.TimeTable;
import com.uway.sports.web.entity.paraentity.NoticeTaskResult;
import com.uway.sports.web.service.IAppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service("iAppUserService")
@Transactional
public class AppUserServiceImpl extends BaseServiceImpl implements IAppUserService {
    public static final String USER_INFO_REDIS = "user:";
    public static final String LOGINNAMELIST_INFO_REDIS = "loginNameList";
    public static final String LOGINNAME_REDIS = "loginName:";
    @Autowired
    private IAppUserDao iAppUserDao;

    @Autowired
    private SharedRedisClient sharedRedisClient;

    @Override
    public List<User> getListByUser(User user) {
        return iAppUserDao.getListByUser(user);
    }

    @Override
    public Long getListCountByUser(User user) {
        return iAppUserDao.getListCountByUser(user);
    }

    @Override
    public void updateUserById(User user) {
        iAppUserDao.updateUserById(user);
    }

    @Override
    public Long addUser(User user) {
        iAppUserDao.addUser(user);
        return user.getUserId();
    }

    @Override
    public User getUserById(Long userId) {
        return iAppUserDao.getUserById(userId);
    }

    @Override
    public User getUserInRedisById(Integer userId) {
        String userStr = sharedRedisClient.get(CommConstantEntity.USER_INFO_HEAD + userId);
        User userInfo;
        if (StringUtils.isNotEmpty(userStr)){
            userInfo = JSONUtils.fromJson(userStr,User.class);
        }else{
            userInfo = getUserById(userId.longValue());
        }
        return userInfo;
    }

    @Override
    public Long getFeedbackCount(TimeTable timeTable) {
        return iAppUserDao.getFeedbackCount(timeTable);
    }

    @Override
    public List<FeedBack> getFeedbackTable(TimeTable timeTable) {
        return iAppUserDao.getFeedbackTable(timeTable);
    }

    @Override
    public FeedBack getFeedbackById(Long id) {
        return iAppUserDao.getFeedbackById(id);
    }

    @Override
    public List<User> getBindCardListByUser(User user) {
        return iAppUserDao.getBindCardListByUser(user);
    }

    /**
     * ????????????????????????
     * @param userPost
     * @return
     */
    @Override
    public List<UserExport> exportUserList(UserPost userPost) {
        return iAppUserDao.exportUserList(userPost);
    }

    @Override
    public List<UserAttention> getUserAttentionList(UserAttention userAttention) {
        return iAppUserDao.getUserAttentionList(userAttention);
    }

    @Override
    public int updateUserAttention(Integer userId, Integer status) {
        // ???????????????status???????????????????????????????????????????????????????????????????????????
        // status ??????????????????   attenStatus ????????????
        Integer attenStatus = 1;
        if (status == 1){
            attenStatus = 0;
        }
        // ???????????????????????????
        UserAttention param = new UserAttention(userId,null,null,attenStatus);
        List<UserAttention> attenList = getUserAttentionList(param);
        if (attenList.size() > 0){
            // ???????????????????????????
            param.setStatus(status);
            iAppUserDao.updateUserAttention(param);
            // ?????????????????????redis??????
            for(UserAttention info : attenList){
                // ????????????????????????????????????
                sharedRedisClient.hdel(CommConstantEntity.USER_ATTENTION_HEAD + info.getAttentionUserId(),userId.toString());
                // ???????????????????????????????????????
                sharedRedisClient.zrem(CommConstantEntity.USER_FANS_LIST_HEAD + info.getAttentionUserId(),userId.toString());
            }
        }
        // ????????????????????????
        param.setUserId(null);
        param.setAttentionUserId(userId);
        param.setStatus(attenStatus);
        List<UserAttention> fansList = getUserAttentionList(param);
        if (fansList.size() > 0){
            // ???????????????????????????
            param.setStatus(status);
            iAppUserDao.updateUserAttention(param);
            // ?????????????????????redis??????
            for(UserAttention info : attenList){
                // ??????????????????????????????????????????
                sharedRedisClient.hdel(CommConstantEntity.USER_ATTENTION_HEAD + info.getUserId(),userId.toString());
                // ???????????????????????????????????????
                sharedRedisClient.zrem(CommConstantEntity.USER_ATTENTION_LIST_HEAD + info.getUserId(),userId.toString());
            }
        }
        // ???????????????redis????????????
        sharedRedisClient.del(CommConstantEntity.USER_ATTENTION_HEAD + userId);
        sharedRedisClient.del(CommConstantEntity.USER_ATTENTION_LIST_HEAD + userId);
        sharedRedisClient.del(CommConstantEntity.USER_FANS_LIST_HEAD + userId);
        return 0;
    }

    @Override
    public List<NoticeTaskResult> getUserAttentionForNotice(NoticeTaskRequest noticeTaskRequest) {
        return iAppUserDao.getUserAttentionForNotice(noticeTaskRequest);
    }

    @Override
    public List<NoticeTaskResult> getUserClickGoodForNotice(NoticeTaskRequest noticeTaskRequest) {
        return iAppUserDao.getUserClickGoodForNotice(noticeTaskRequest);
    }

    @Override
    public List<NoticeTaskResult> getUserMomentsForNotice(NoticeTaskRequest noticeTaskRequest) {
        return iAppUserDao.getUserMomentsForNotice(noticeTaskRequest);
    }
}

