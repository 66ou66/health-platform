package edu.dlnu.health_platform.service.impl;

import edu.dlnu.health_platform.dal.CategoryMapper;
import edu.dlnu.health_platform.entity.Category;
import edu.dlnu.health_platform.service.CategoryService;
import edu.dlnu.health_platform.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public Result<List<Category>> list() {
        return Result.success(categoryMapper.findAll());
    }

    @Override
    public Result<Void> add(Category category) {
        if (category == null || category.getCategoryName() == null || category.getCategoryName().isBlank()) {
            return Result.fail("分类名称不能为空");
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        categoryMapper.insert(category);
        return Result.success();
    }

    @Override
    public Result<Void> update(Category category) {
        if (category == null || category.getId() == null) {
            return Result.fail("分类ID不能为空");
        }
        if (categoryMapper.findById(category.getId()) == null) {
            return Result.fail("分类不存在");
        }
        if (category.getCategoryName() == null || category.getCategoryName().isBlank()) {
            return Result.fail("分类名称不能为空");
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        categoryMapper.update(category);
        return Result.success();
    }

    @Override
    public Result<Void> delete(Long id) {
        if (categoryMapper.findById(id) == null) {
            return Result.fail("分类不存在");
        }
        categoryMapper.delete(id);
        return Result.success();
    }
}
