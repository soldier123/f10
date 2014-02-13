package dto;

import play.data.binding.As;

/**
 * 今日头条,订阅条件 dto
 * User: liangbing
 * Date: 13-1-29
 * Time: 下午3:59
 */
public class ToDayNewsSearchCnd {

    @As(",")
    public String[] source;
    @As(",")
    public String[] newsClass;
    public ToDayNewsSearchCnd(){
        this(null,null);
    }
    public ToDayNewsSearchCnd(String[] source,String[] newsClass){
        this.source = source;
        this.newsClass = newsClass;
    }

}
