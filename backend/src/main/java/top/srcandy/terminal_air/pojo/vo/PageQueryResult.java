package top.srcandy.terminal_air.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PageQueryResult<T> {
    private int current;
    private int pageSize;
    private long total;
    private T result;
}
