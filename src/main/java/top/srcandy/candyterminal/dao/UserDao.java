package top.srcandy.candyterminal.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.srcandy.candyterminal.bean.dto.LoginDTO;
import top.srcandy.candyterminal.bean.dto.RegisterDTO;
import top.srcandy.candyterminal.model.User;
import top.srcandy.candyterminal.request.RegisterRequest;
import top.srcandy.candyterminal.request.UpdateProfileRequest;

@Mapper
public interface UserDao {
    User selectByUserName(@Param("username") String username);

    void update(User updateProfileRequest);

    int insert(LoginDTO user);

    User selectByUserPhone(@Param("phone") String phone);

    int insertSelective(RegisterDTO request);
}
