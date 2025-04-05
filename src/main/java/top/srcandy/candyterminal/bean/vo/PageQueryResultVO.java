package top.srcandy.candyterminal.bean.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PageQueryResultVO<T> {
    private int dataTotal;

    private int page;

    private int pageSize;

    private int pageCount;

    private List<T> result;
}
