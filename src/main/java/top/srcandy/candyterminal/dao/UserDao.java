package top.srcandy.candyterminal.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.srcandy.candyterminal.dto.LoginDTO;
import top.srcandy.candyterminal.model.User;


@Mapper
public interface UserDao {
    User selectByUserName(@Param("username") String username);

    int insert(LoginDTO user);
}
