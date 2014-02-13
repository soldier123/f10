package dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 板块树dto
 * User: panzhiwei
 * Date: 13-2-1
 * Time: 上午10:31
 * To change this template use File | Settings | File Templates.
 */
public class PlateTreeDto {
    public Long id;
    public Long pid;
    @SerializedName(value="text")
    public String name;
    public String code;
    public List<PlateTreeDto> subs;

}
