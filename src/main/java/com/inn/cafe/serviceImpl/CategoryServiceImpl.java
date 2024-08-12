package com.inn.cafe.serviceImpl;

import com.google.common.base.Strings;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Category;
import com.inn.cafe.constants.CafeConstants;
import com.inn.cafe.dao.CategoryDao;
import com.inn.cafe.exceptions.BadRequestException;
import com.inn.cafe.exceptions.ImageParsingException;
import com.inn.cafe.exceptions.UnauthorizedException;
import com.inn.cafe.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public Category addNewCategory(String name, MultipartFile image) {
        try {
            if (jwtFilter.isAdmin()) {
                if (!StringUtils.isEmpty(name)) {
                    if (categoryDao.isPresent(name)) {
                        throw new BadRequestException("Category with name " + name + " already exists.");
                    }
                    return categoryDao.save(getCategory(name, image));
                } else {
                    throw new BadRequestException(CafeConstants.INVALID_PAYLOAD);
                }
            } else {
                throw new UnauthorizedException(CafeConstants.UNAUTHORISED_ACCESS);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public List<Category> getAllCategories(String filterValue) {
        try {
            if (!Strings.isNullOrEmpty(filterValue) && filterValue.equalsIgnoreCase("true")) {
                log.info("Inside if");
                return categoryDao.getAllCategory(filterValue);

            }
            return categoryDao.findAll();

        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public Category updateCategory(int id, String name, MultipartFile image) {
        try {
            if (jwtFilter.isAdmin()) {
                if (!StringUtils.isEmpty(name)) {
                    Optional<Category> optional = categoryDao.findById(id);
                    if (optional.isPresent()) {
                        Category category = getCategory(name, image);
                        category.setId(id);
                        if (category.getImage() == null) {
                            category.setImage(optional.get().getImage());
                        }
                        return categoryDao.save(category);
                    } else {
                        throw new BadRequestException("Category id does not exist");
                    }
                }
                throw new BadRequestException(CafeConstants.INVALID_PAYLOAD);
            } else {
                throw new UnauthorizedException(CafeConstants.UNAUTHORISED_ACCESS);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    private Category getCategory(String name, MultipartFile image) {
        Category category = new Category();
        category.setName(name);
        if (image != null && !image.isEmpty()) {
            try {
                category.setImage(image.getBytes());
            } catch (IOException e) {
                throw new ImageParsingException("Some error occurred while processing image");
            }
        }
        return category;
    }

}
