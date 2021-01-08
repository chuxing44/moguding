package com.qianmua.service.impl;

import com.qianmua.dao.LoginMapper;
import com.qianmua.dao.SinginMapper;
import com.qianmua.pojo.Login;
import com.qianmua.pojo.Singin;
import com.qianmua.pojo.vo.LoginVo;
import com.qianmua.sign.in.SignServer;
import com.qianmua.service.Userservice;
import com.qianmua.util.PublicUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserserviceImpl implements Userservice {

    @Autowired
    private LoginMapper loginMapper;

    @Autowired
    private SinginMapper singinMapper;

    @Autowired
    private SignServer signServer;

    @Override
    public List<Login> getLoginInfo() {

        List<Login> logins = loginMapper.selectAll();
        if (logins == null)
            throw new ArrayIndexOutOfBoundsException();
        return logins;
    }

    /**
     * 这里结构很乱
     * 不太好重构
     * @param login userInfo
     */
    @Override
    public void addUser(Login login) {
        // get planId
        LoginVo loginvo = new LoginVo();
        BeanUtils.copyProperties(login,loginvo);
        loginvo.setLoginType(login.getLogintype());

        String plan = signServer.getPlan(loginvo);

        // save to login table
        String replace = PublicUtils.genUUID();
        login.setId(replace);
        loginMapper.insert(login);

        // save to sign table
        Singin singins = login.getSingins();
        String uuid = PublicUtils.genUUID();
        singins.setId(uuid);
        singins.setPlanId(plan);
        singins.setLoginId(replace);
        singinMapper.insert(singins);

    }


}
