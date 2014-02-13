package dto;

/**
 * 我的订阅 内容保存
 * User: liangbing
 * Date: 13-1-19
 * Time: 下午1:48
 */

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 用户自定义模板
 * User: 梁兵
 * Date: 12-11-7
 * Time: 上午10:55
 */

public class UserTemplate extends Model {

    public Long id;//模板ID

    public String name;

    public Integer type; //101.今日头条

    public String content; //保存的内容,只提供存储,里面的内容自定义

    public Long uid;//用户id

    @Override
    public String toString() {
        return "UserTemplate{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", content='" + content + '\'' +
                '}';
    }
}

