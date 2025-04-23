package top.srcandy.terminal_air.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PageQueryResultVo<T> {
    private int dataTotal;

    private int page;

    private int pageSize;

    private int pageCount;

    private List<T> result;
}
