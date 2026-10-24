package edu.dlnu.health_platform.service;

import edu.dlnu.health_platform.entity.Category;
import edu.dlnu.health_platform.vo.Result;

import java.util.List;

public interface CategoryService {

    //查询所有分类
    Result<List<Category>> list();

    //新增分类（管理员）
    Result<Void> add(Category category);

    //修改分类（管理员）
    Result<Void> update(Category category);

    //删除分类（管理员）
    Result<Void> delete(Long id);
}
