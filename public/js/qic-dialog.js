/**
 * Created with IntelliJ IDEA.
 * User: liujl
 * Date: 13-1-21
 * Time: 下午6:16
 * To change this template use File | Settings | File Templates.
 */

var QIC_DIALOG_TEMPLATE = "<div style='text-align: center;' id='qic_dialog_001' title='提示'>"
    + "<div id='qic_dialog_bg_image'><span  id='qic_dialog_001_content'></span></div>"
    +"<div style='display:inline-block; z-index:10000;margin:30px 10px 10px 150px;'>"
    +"<nobr><input id='qic_dialog_confirm_btn'type='button'  class='dialog_common_blue' style='background: -moz-linear-gradient(center top , #F0C633 0%, #A67A3A 50%, #A67A3A 100%) repeat scroll 0 0 transparent;' value='确定'>"
    + "<input  id='qic_dialog_cancel_btn' type='button'   class='dialog_common_blue dialog_common_cancle' style='background: -moz-linear-gradient(center top , #F0C633 0%, #A67A3A 50%, #A67A3A 100%) repeat scroll 0 0 transparent;' value='取消'></nobr>"
    +"</div>"
    +" </div>" ;

function QicDialog(config){
    $("#qic_dialog_001").remove();
    this.message  = config.message ? config.message : "你确定要这样做吗？";
    this.title = config.title ? config.title : "提示";
    this.confirmAction = config.confirm;
    this.cancelAction = config.cancel;
    this.target = config.target;
    this.height = config.height ? "130" : config.height;
    this.width = config.width ? "auto" : config.width;
    this.confirmName = config.confirmName ? config.confirmName : "确定";
    this.cancelName = config.cancelName ? config.cancelName : "取消";
    this.modal = config.modal ?  config.modal : true;
    this.initPromptValue = config.initPromptValue ?  config.initPromptValue :"";
    this.label = config.label ?   config.label :"please input";
    this.isPrompt = false,
    this.autoClose = (config.autoClose != undefined) ?  config.autoClose :true;
    $("body").append(QIC_DIALOG_TEMPLATE);
    this.setTitle(this.title)
};
QicDialog.prototype.setTitle = function(title){
    this.title =title;
    $("#qic_dialog_001").attr("title",this.title);
};
QicDialog.prototype.setMessage = function(message){
    this.message =message;
    $("#qic_dialog_001_content").html(this.message);

};
QicDialog.prototype.getTitle = function (){return this.title ; };

QicDialog.prototype.getPromptValue = function (){
    return $("#promptId").val()  ;
};
QicDialog.prototype.setTarget = function (target){
    if(target){
        this.target  = target;
    }else{
        this.target = QIC_DIALOG_TEMPLATE;
    }
};
QicDialog.prototype.getTarget = function (target){
    return this.target = target ;
};
QicDialog.prototype.setConfirmAction= function (confirmAction){
    var obj = this;
    if(confirmAction){
        this.confirmAction = confirmAction;
        $("#qic_dialog_confirm_btn").click(function(){
            if(obj.isPrompt){
              confirmAction(obj.getPromptValue(),obj);
            }else{
              confirmAction();
            }
            if(obj.autoClose){
              obj.destroy();
            }
        });
    }else{
        $("#qic_dialog_confirm_btn").click(function(){//默认执行
            $("#qic_dialog_001").dialog("close");
            obj.destroy();
        });
    }
};
QicDialog.prototype.setCancleAction = function (cancleAction){
    var obj = this;
    if(cancleAction){
        this.cancleAction = cancleAction;
        $("#qic_dialog_cancel_btn").click(function(){
            cancleAction();
            obj.destroy();
        });
    }else{
        $("#qic_dialog_cancel_btn").click(function(){//默认执行
            obj.destroy();
        });
    }
};
//提示信息
QicDialog.prototype.alert = function(){
    var obj = this;
    $("#qic_dialog_001").dialog({
        resizable: false,
        height:obj.height,
        width:obj.width,
        modal: true
    });
    $("#qic_dialog_cancel_btn").css("display","none");
    $("#qic_dialog_confirm_btn").css("margin-left","50px");
    this.init();
    $("#qic_dialog_001_content").addClass("dialog_image_success");
}
//出错担示信息 带红色提示图标
QicDialog.prototype.error = function(){
    var obj = this;
    $("#qic_dialog_001").dialog({
        resizable: false,
        height:obj.height,
        width:obj.width,
        modal: true
    });
    $("#qic_dialog_cancel_btn").css("display","none");
    $("#qic_dialog_confirm_btn").css("margin-left","50px");
    this.init();
    $("#qic_dialog_001_content").addClass("dialog_image_error");
}
//警告提示  但黄色提示图标
QicDialog.prototype.warn = function(){
    var obj = this;
    $("#qic_dialog_001").dialog({
        resizable: false,
        height:obj.height,
        width:obj.width,
        modal: true
    });
    $("#qic_dialog_cancel_btn").css("display","none");
    $("#qic_dialog_confirm_btn").css("margin-left","50px");
    this.init();
    $("#qic_dialog_001_content").addClass("dialog_image_warn");
}
QicDialog.prototype.confirm = function(){
    var obj = this;
    $("#qic_dialog_001").dialog({
        resizable: false,
        height:obj.height,
        width:obj.width,
        modal: true
    });
    $("#qic_dialog_cancel_btn").css("display","");
    $("#qic_dialog_confirm_btn").css("margin-left","0px");
    this.init();
    $("#qic_dialog_001_content").addClass("dialog_image_notice");
}

QicDialog.prototype.prompt = function(){
    var obj = this;
    $("#qic_dialog_001").dialog({
        resizable: false,
        height:obj.height,
        width:obj.width,
        modal: true
    });
    $("#qic_dialog_cancel_btn").css("display","");
    $("#qic_dialog_confirm_btn").css("margin-left","0px");
    this.setMessage(obj.label+"<input maxlength='10'  value='"+obj.initPromptValue+"' type='text' id='promptId'/>");
    this.isPrompt = true;
    this.init();
    //$("#qic_dialog_001_content").addClass("dialog_image_notice");
}

function checked(name){
    var reg = new RegExp("^([\u4E00-\uFA29]|[\uE7C7-\uE7F3]|[a-zA-Z0-9])*$");
    if(!reg.test(name)){
        $.qicTips({message:"订阅名称不能有特殊字符", level:2, target:"#ut_ul_list", mleft:0, mtop:0});
        return true ;
    }
}


QicDialog.prototype.init = function(){
    var obj = this;
    var confirmName = obj.confirmName;
    var cancelName = obj.cancelName;
    $("#qic_dialog_001_content").removeClass();
    // $("#qic_dialog_001_content").addClass("dialog_image")
    $("#qic_dialog_cancel_btn").attr("value",cancelName);
    $("#qic_dialog_confirm_btn").attr("value",confirmName);
    this.setConfirmAction(this.confirmAction)
    this.setCancleAction(this.cancelAction);
    this.setMessage(this.message);
    this.setTitle(this.title);
    $("#qic_dialog_confirm_btn").css("margin-left","0px");
}
//销毁
QicDialog.prototype.destroy = function(){
    // $("#qic_dialog_001").dialog("close");
    $("#qic_dialog_001").remove();

}


<!-- 资讯中 基本搜索关键字屏蔽特殊字符 start-->
$("#codeBtn").live('click',function(){
    var name = $('.stock_code').val();
    var reg = new RegExp("^([\u4E00-\uFA29]|[\uE7C7-\uE7F3]|[a-zA-Z0-9])*$");
    if(!reg.test(name)){
    $.qicTips({message:"请输入中文字母或数字,不能包含空格", level:2, target:".stock_code", mleft:0, mtop:-20});
return false ;
}
$("#infoForm")[0].submit();

});
<!-- 基本搜索关键字屏蔽特殊字符 end-->

