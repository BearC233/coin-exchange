package com.bearc.constant;

public class Login {
    // 管理员
    public static final String ADMIN_TYPE = "admin_type";
    public static final String ADMIN_ROLE_CODE = "ROLE_ADMIN";
    //  会员
    public static final String MEMBER_TYPE = "member_type";
    public static final String MEMBER_ROLE_CODE = "ROLE_MEMBER";
    //查询会员的sql语句
    public static final String QUERY_ADMIN_SQL =
            "SELECT `id` ,`username`, `password`, `status` FROM sys_user WHERE username = ? ";
    //查询角色的sql语句
    public static final String QUERY_ROLE_CODE_SQL =
            "SELECT `code` FROM sys_role LEFT JOIN sys_user_role ON sys_role.id = sys_user_role.role_id WHERE sys_user_role.user_id= ?";
    //用户为管理员,可以查询所有权限
    public static final String QUERY_ALL_PERMISSIONS =
            "SELECT `name` FROM sys_privilege";
    //用户为会员,只能通过身份查询自己的权限
    public static final String QUERY_PERMISSION_SQL =
            "SELECT * FROM sys_privilege LEFT JOIN sys_role_privilege ON sys_role_privilege.privilege_id = sys_privilege.id LEFT JOIN sys_user_role  ON sys_role_privilege.role_id = sys_user_role.role_id WHERE sys_user_role.user_id = ?";
    //查询会员的sql语句
    public static final String QUERY_MEMBER_SQL =
            "SELECT `id`,`password`, `status` FROM `user` WHERE mobile = ? or email = ? ";
    public static  final  String REFRESH_TOKEN = "REFRESH_TOKEN" ;
    /**
     * 使用用户的id 查询用户名称
     */
    public static  final  String QUERY_ADMIN_USER_WITH_ID = "SELECT `username` FROM sys_user where id = ?" ;

    /**
     * 使用用户的id 查询用户名称
     */
    public static  final  String QUERY_MEMBER_USER_WITH_ID = "SELECT `mobile` FROM user where id = ?" ;

}
