package edu.dlnu.health_platform.dal;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.dlnu.health_platform.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * users 表 Mapper（注解 SQL 风格）
 */
@Mapper
public interface UserMapper {

    //根据用户名查询用户（登录、查重用）
    @Select("SELECT * FROM users WHERE username = #{userName}")
    User findByUserName(@Param("userName") String userName);

    //注册新用户，返回受影响行数，主键回填
    @Insert("INSERT INTO users (username, password, real_name, phone, role, status) " +
            "VALUES (#{username}, #{password}, #{realName}, #{phone}, #{role}, 1)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    //根据ID查询用户
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(@Param("id") Long id);

    //用户列表（管理员，分页；role 为空时查全部角色）
    @Select("<script>SELECT id, username, real_name, phone, role, avatar, status, created_at FROM users " +
            "<if test='role != null'>WHERE role = #{role}</if> ORDER BY id</script>")
    IPage<User> findAll(Page<User> page, @Param("role") Integer role);

    //冻结/解冻用户（1正常 0冻结）
    @Update("UPDATE users SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    //用户总数（看板统计）
    @Select("SELECT COUNT(*) FROM users")
    long countAll();

    //按角色统计用户数（看板统计）
    @Select("SELECT COUNT(*) FROM users WHERE role = #{role}")
    long countByRole(@Param("role") Integer role);
}
