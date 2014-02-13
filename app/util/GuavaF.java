package util;

import com.google.common.base.Function;
import com.google.common.base.Joiner;

/**
 * 这里包含一些guava提供的函数式编程的方法或者是一些常用的工具方法
 * User: wenzhihong
 * Date: 13-9-13
 * Time: 上午8:09
 */
public class GuavaF {
    /**
     * 逗号连接
     */
    public static final Joiner commaJoin = Joiner.on(',').skipNulls();

    /**
     * or 连接
     */
    public static final Joiner orJoin = Joiner.on(" or ").skipNulls();

    /**
     * sql给字符类型加上 ''
     */
    public static final Function<String, String> sqlSingleQuote = new Function<String, String>() {
        @Override
        public String apply(String input) {
            return " '" + input + "' ";
        }
    };

}
