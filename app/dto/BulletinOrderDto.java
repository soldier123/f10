package dto;

import play.data.binding.As;

/**
 * des: 公告订阅条件
 * User: weiguili(li5220008@163.com)
 * Date: 13-2-21
 * Time: 下午3:18
 */
public class BulletinOrderDto {
    //报告类型
    @As(",")
    public String[] bulletinClassify;
    //板块树
    @As(",")
    public String[] plateTree;
    //证券代码
    @As(",")
    public String[] symbolArr;
}
