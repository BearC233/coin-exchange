package com.bearc.service.impl;

import com.bearc.constant.Login;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailServiceImpl implements UserDetailsService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String loginType = requestAttributes.getRequest().getParameter("login_type");
        if(StringUtils.isEmpty(loginType)){
            throw new AuthenticationServiceException("登录方式不能为空");
        }
        UserDetails userDetails;
        try{
            String grantType = requestAttributes.getRequest().getParameter("grant_type");//由于把用户名临时改为id生成jwt令牌,使用refresh_token时要纠正回来
            if(Login.REFRESH_TOKEN.equals(grantType.toUpperCase())){
                name = adjustName(name,loginType);
            }
            switch (loginType){
                case Login.MEMBER_TYPE:
                    userDetails = loadMemberByUsername(name);
                    break;
                case Login.ADMIN_TYPE:
                    userDetails = loadAdminByUsername(name);
                    break;
                default:
                    throw new AuthenticationServiceException("暂不支持的登录方式:"+loginType);
            }
        }
        catch (IncorrectResultSizeDataAccessException e){
            throw new UsernameNotFoundException("用户"+name+"不存在");
        }

        return userDetails;


    }

    private String adjustName(String name, String loginType) {
        if(Login.ADMIN_TYPE.equals(loginType)){
            return jdbcTemplate.queryForObject(Login.QUERY_ADMIN_USER_WITH_ID, String.class, name);
        }
        if(Login.MEMBER_TYPE.equals(loginType)){
            return jdbcTemplate.queryForObject(Login.QUERY_MEMBER_USER_WITH_ID, String.class, name);
        }
        return name;
    }

    private UserDetails loadMemberByUsername(String name) {
        return jdbcTemplate.queryForObject(Login.QUERY_MEMBER_SQL, new RowMapper<User>(){
            @Override
            public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                if(rs.wasNull()){
                    throw new UsernameNotFoundException("用户不存在");
                }
                long id = rs.getLong("id");
                String password = rs.getString("password");
                int status = rs.getInt("status");
                return new User(String.valueOf(id), password, status == 1, true, true, true, Arrays.asList(new SimpleGrantedAuthority("ROLE_MEMBER")));
            }
        },name,name);

    }

    private Collection<? extends GrantedAuthority> getAdminPermissions(long id) {
        //1根据id查询什么身份
        String roleCode = jdbcTemplate.queryForObject(Login.QUERY_ROLE_CODE_SQL, String.class, id);
        List<String> permissions = null;
        //2如果是管理员
        if(roleCode.equals(Login.ADMIN_ROLE_CODE)){
            permissions = jdbcTemplate.queryForList(Login.QUERY_ALL_PERMISSIONS,  String.class);
        }
        else{//普通用户,则角色->权限
            permissions = jdbcTemplate.queryForList(Login.QUERY_PERMISSION_SQL,  String.class, id);
        }
        if(permissions.isEmpty()||permissions==null){
            return Collections.EMPTY_SET;
        }
        return permissions.stream().distinct()//去重
                .map(permission -> new SimpleGrantedAuthority(permission)).collect(Collectors.toSet());
    }

    private UserDetails loadAdminByUsername(String name) {
        //1.使用用户名查询用户
        return jdbcTemplate.queryForObject(Login.QUERY_ADMIN_SQL, new RowMapper<User>(){
            @Override
            public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                if(rs.wasNull()){
                    throw new UsernameNotFoundException("用户不存在");
                }
                long id = rs.getLong("id");
                String password = rs.getString("password");
                int status = rs.getInt("status");
                //2.查询用户权限
                //3.封装用户信息
                return new User(String.valueOf(id), password, status == 1, true, true, true, getAdminPermissions(id));
            }
        },name);
    }
}
