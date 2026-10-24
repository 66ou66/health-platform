package edu.dlnu.health_platform.dal;

import edu.dlnu.health_platform.entity.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * service_categories 表 Mapper（注解 SQL 风格）
 */
@Mapper
public interface CategoryMapper {

    //分类列表（按排序值升序）
    @Select("SELECT id, category_name, icon, sort FROM service_categories ORDER BY sort, id")
    List<Category> findAll();

    //根据ID查询分类
    @Select("SELECT id, category_name, icon, sort FROM service_categories WHERE id = #{id}")
    Category findById(@Param("id") Long id);

    //新增分类，返回受影响行数，主键回填
    @Insert("INSERT INTO service_categories (category_name, icon, sort) VALUES (#{categoryName}, #{icon}, #{sort})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    //修改分类
    @Update("UPDATE service_categories SET category_name = #{categoryName}, icon = #{icon}, sort = #{sort} WHERE id = #{id}")
    int update(Category category);

    //删除分类
    @Delete("DELETE FROM service_categories WHERE id = #{id}")
    int delete(@Param("id") Long id);
}
