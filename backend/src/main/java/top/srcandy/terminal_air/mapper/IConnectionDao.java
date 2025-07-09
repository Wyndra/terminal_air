package top.srcandy.terminal_air.mapper;


import org.apache.ibatis.annotations.Mapper;
import top.srcandy.terminal_air.pojo.dto.ConnectionDto;
import top.srcandy.terminal_air.pojo.model.Connection;
import top.srcandy.terminal_air.pojo.model.Credential;

import java.util.List;

@Mapper
public interface IConnectionDao {
    List<ConnectionDto> selectCredentialsByUserId(Long userId);
}
