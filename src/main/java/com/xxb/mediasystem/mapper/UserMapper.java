package com.xxb.mediasystem.mapper;




import com.xxb.mediasystem.model.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    User selectByPrimaryKey(int userid);

    int addUser(User user);

    int editUser(User user);
    User findUserByToken(String token);
    List<User> getAllUsers();
    List<User> getUsersByType(Integer type);
}