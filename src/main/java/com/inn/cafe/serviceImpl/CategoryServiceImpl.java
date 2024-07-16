package com.inn.cafe.serviceImpl;

import com.google.common.base.Strings;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Category;
import com.inn.cafe.constants.CafeConstants;
import com.inn.cafe.dao.CategoryDao;
import com.inn.cafe.exceptions.BadRequestException;
import com.inn.cafe.exceptions.UnauthorizedException;
import com.inn.cafe.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public Category addNewCategory(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                if (validateCategoryMap(requestMap, false)) {
//                    log.info("Inside ");
                    if (categoryDao.isPresent(requestMap.get("name"))) {
                        throw new BadRequestException("Category with name " + requestMap.get("name") + " already exists.");
                    }
                    return categoryDao.save(getCategoryFromMap(requestMap, false));
                }else {
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
            if(!Strings.isNullOrEmpty(filterValue) && filterValue.equalsIgnoreCase("true")){
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
    public Category updateCategory(Map<String, String> requestMap) {
        try{
            if(jwtFilter.isAdmin()){
                if(validateCategoryMap(requestMap,true)){
                    Optional optional=categoryDao.findById(Integer.parseInt(requestMap.get("id")));
                    if(!optional.isEmpty()){
                        return categoryDao.save((getCategoryFromMap(requestMap,true)));
                    }
                    else{
                        throw new BadRequestException("Category id does not exist");
                    }
                }
                throw new BadRequestException(CafeConstants.INVALID_PAYLOAD);
            }else{
                throw new UnauthorizedException(CafeConstants.UNAUTHORISED_ACCESS);
            }
        }catch (Exception ex){
            ex.printStackTrace();
            throw ex;
        }
    }

    private boolean validateCategoryMap(Map<String, String> requestMap, boolean validateId) {
        if (requestMap.containsKey("name")) {
            if (requestMap.containsKey("id") && validateId) {
                return true;
            } else if (!validateId) {
                return true;
            }
        }
        return false;

    }

    private Category getCategoryFromMap(Map<String, String> requestMap, Boolean isAdd) {
        Category category = new Category();
        if (isAdd) {
            category.setId(Integer.parseInt(requestMap.get("id")));
        }
        category.setName(requestMap.get("name"));
        return category;
    }

}
