/*新闻机构js */
$(function(){
    $("#ut_ul_list li").live("dblclick", {"type":2}, utListItemdblclick)
    //编辑订阅
    $(".sel_subscribe_opt li").click(function(e){
        var lastChild = $(this).last().length,
            index = $(this).index(),
            length = $(this).parent("ul").find("li").length;
        if(index  == (length-1) ){
           // doClean();
            $(".mysubcribe_box").dialog({
                beforeClose: function(){
                    if($("#ut_ul_list > li >input").size()>0){
                        return false;
                    }else{
                        return true;
                    }
                },
                "autoOpen": true,
                close:function(){
                    doClean();
                },
                "width": 668,
                "height": 335,
                "resizable": false,
                 "modal":true
            });
        }else{
            //后台处理的地方
        }

    });
    //删除订阅条件
    $(".for_remove_item").live('click',function(event){
        var $li = $("#ut_ul_list > li.cndSelected");
        if($li.size() == 0) {
            $.qicTips({message:'请先选择订阅条件', level:2, target:'#ut_ul_list', mleft:0, mtop:-10});
            return;
        }
        var cndId = $li.attr("id").substring("ut_".length);
        new QicDialog({
            message:"是否确认删除?",
            title:"提示",
            confirm:function(){
                $.ajax({
                    url:delCondRoute.url(),
                    type:"post",
                    dataType:"json",
                    data:{"id":cndId},
                    success:function(data){
                        if(data.success){
                            $li.remove();
                            var $li2 = $("#options_year1_2 > li[id = utt_"+cndId+"]");
                            $li2.remove();
                            $.qicTips({message:data.msg, level:1, target:'#ut_ul_list', mleft:0, mtop:-60});
                            $("#ut_ul_list > li:first").trigger("click");
                        }
                    }
                })
            }
        }).confirm();
    });

//点击保存
    $("#savebtn").click(function(){
        var orderName ;
        if($("#ut_ul_list > li.cndSelected").size() == 0) {
            $.qicTips({message:'请选择订阅名称', level:2, target:"#savebtn", mleft:-60, mtop:-30});
            return;
        }
        if($("#ut_ul_list > li >input").size()>0){
            $.qicTips({message:'请先保存订阅名称', level:2, target:'#ut_ul_list', mleft:0, mtop:-60});
            return;
        }
        if($("#ut_ul_list > li.cndSelected").size() > 0){
            var $li = $("#ut_ul_list > li.cndSelected");
            var cndId = $li.attr("id").substring("ut_".length);
            orderName = $("#ut_ul_list > li.cndSelected").text();
        }

        saveNews(orderName);
    })

    addClickListenerToLiTag1();
});

function addClickListenerToLiTag1(){
    $("#ut_ul_list > li").click(function(){
        $("#ut_ul_list > li.cndSelected").removeClass("cndSelected");
        var $li = $(this);
        $li.addClass("cndSelected");
        var cndId = $li.attr("id").substring("ut_".length);
        var cndJson = utMap[cndId];
        var source = cndJson["source"];
        var newsClass = cndJson["newsClass"];
        $("input[type=checkbox]").attr("checked",false);
        if(source!=undefined){
            for(var i =0 ;i<source.length;i++){
                $("input[type=checkbox][value="+source[i]+"]").attr("checked",true);
            }
        }
        if(newsClass!=undefined ){
            for(var m =0 ;m<newsClass.length;m++){
                $("input[type=checkbox][value="+newsClass[m]+"]").attr("checked",true);
            }
        }

    });
}

//清空选中的复选框
function reset1(){
    $("input[type=checkbox]").attr("checked",false);
}

//重置
function doClean(){
    if($("#ut_ul_list > li.cndSelected").size()>0){
     $("#ut_ul_list > li.cndSelected").removeClass("cndSelected");
     }
    $("input[type=checkbox]").attr("checked",false);
}

//保存订阅
function saveNews(name){
        var $li = $("#ut_ul_list > li.cndSelected");
        var cndId = $li.attr("id").substring("ut_".length);
        var params = $("#newsForm").serializeArray();
        params.push({name: 'name', value: name});
        params.push({name: 'id', value: cndId});
        params.push({name: 'updateName', value: 0});
            $.ajax({
                url:editNewsOrder.url(),
                type:"post",
                dataType:"json",
                async : false,
                data:params,
                success:function(data){
                        $.qicTips({message:data.msg, level:1, target:"#ut_ul_list", mleft:0, mtop:-60});
                        var obj = cndId+"";
                        utMap[obj] = eval("("+data.info+")");
                       // $("#ut_"+cndId).text(name);
                },
                error:function(){
                    alert("出错了...");
                    isSuccess = false;
                }
            });
}


//增加订阅条件
$(".subscribe_add").live('click',function () {
    //说明有重名的,要处理重名
    var $input2 = $("#ut_ul_list > li > input");
    if($input2.size() > 0){
        new QicDialog({
            message:"请先保存当前订阅名称",
            title:"提示"
        }).warn();
        $input2.get(0).focus();
        return ;
    }
    if ($("#ut_ul_list > li").size() >= 20) {
        new QicDialog({
            message:"订阅条件上限为20个",
            title:"提示"
        }).warn();
        return;
    }

    $("#ut_ul_list > li.cndSelected").removeClass("cndSelected");//先去掉选中的

    var newNameTmp = "我的订阅";
    var newName = "";
    var tmpNameIntArr = []; //用于保存 newNameTmp 开头的接下来的整数值
    $("#ut_ul_list > li").each(function () {
        var itemHtml = $(this).html();
        if (itemHtml.indexOf(newNameTmp) > -1) {
            var idx = parseInt(itemHtml.substring(newNameTmp.length));//后续数字
            if (isNaN(idx)) {
                tmpNameIntArr.push(0);
            } else {
                tmpNameIntArr.push(idx);
            }
        }
    });
    //排序取出最大的, 作为名称
    if (tmpNameIntArr.length > 0) {
        tmpNameIntArr.sort(function(o1, o2){
            return o1 - o2;
        });
        newName = newNameTmp + (tmpNameIntArr[tmpNameIntArr.length - 1] + 1);
    } else {
        newName = newNameTmp + "1";
    }
    var $li = $("<li id='ut_-999' class='ut_li_text tooltip' mytitle='"+newName+"' title='"+newName+"'>" + newName + "</li>");

    registToolTip();
    $li.addClass("cndSelected");
    $li.prependTo($("#ut_ul_list"));
    // $('#ut_ul_list').scrollTop($('#ut_ul_list')[0].scrollHeight+10); //滚动到底端
    $li.trigger("dblclick");

    //初始化交易类型和品种
    reset1();
});

//搜索条件列表项双击事件处理
function utListItemdblclick(e) {
    if($("#ut_ul_list > li >input").size()>0){
        return;
    }
    var li = $(this);
    if(! li.hasClass("cndSelected")){ //如果当前不选中的话, 直接跳过
        return;
    }
    var typeVal = 2;
    li.removeClass("cndSelected");
    var cndId = li.attr("id").substring("ut_".length); //模板id
    //var text = li.text();
    var text = li.attr("mytitle");
    li.html("");
    var input = $("<input type='text' onmouseout='registToolTip()' onmouseover='cancelToolTip()' id='inputId' maxlength='20' onkeyup='f_Check()'/>");
    //暂时没有找到双击的时候文本框里尾部出现"..."的情况，就这样先处理下
    text = replaceErrorCharacter(text)//global-f10中的方法
    input.attr("value", text);
    input.blur(function (event) {
        changeName(li,typeVal,cndId,text,input);
    });

    input.keyup(function(event) {
        if (event.keyCode == "13") {//keyCode=13是回车键
            changeName(li,typeVal,cndId,text,input);
        }
    });

    //把文本框加到元素中去
    li.append(input);

    //让文本狂中的文字被高亮选中
    //需要将jquery的对象转换为dom对象
    var inputdom = input.get(0);
    inputdom.select();
    //inputdom.focus();
    //6.清除元素上注册的点击事件
    //li.unbind("dblclick");
}

$("#btn_cancle").live('click',function(){
    reset1();

});

function changeName(li,typeVal,cndId,text,input){
    li.addClass("cndSelected");
    var inputtext = $.trim(input.val());
    if(inputtext.length == 0){
        $.qicTips({message:"名称不能为空", level:2, target:'#ut_ul_list', mleft:0, mtop:-60});
        input.focus();
        return ;
    }else if(inputtext.length > 20){
        $.qicTips({message:"名称过长", level:2, target:'#ut_ul_list', mleft:0, mtop:-60});
        input.focus();
        return ;
    }
    if(checked(inputtext)){
        input.focus();
        return;
    }
    var sameName = false;
    $("#ut_ul_list > li").each(function(i){
        var $eachThis = $(this);
        if($eachThis == li){ //当前节点

        }else if((inputtext+'<span class="for_remove_item"></span>').trim() == ($eachThis.html()).trim()){ //说明重名
            sameName = true;
            return false;
        }

        return true;
    });
    if(sameName){
        $.qicTips({message:"名称已存在", level:2, target:'#ut_ul_list', mleft:0, mtop:-60});
        li.addClass("cndSelected");
        // li.html(inputtext);
        // li.trigger("dblclick", {"type":typeVal});
        return ;
    }

    if (cndId == -999 || (inputtext.length > 0 && text != inputtext) ) { //新加节点 或者 有修改
        var params = $("#newsForm").serializeArray();
        params.push({name: 'name', value: inputtext});
        params.push({name: 'cid', value: cndId});
        $.ajax({
            url:addNewOrder.url(),
            type:"get",
            dataType:"json",
            async : false,
            data:params,
            success:function (data) {
                if (data.success) {
                    var liNode = input.parent();
                    //暂时没有找到双击的时候文本框里尾部出现"..."的情况，就这样先处理下
                    inputtext = replaceErrorCharacter(inputtext)//global-f10中的方法
                    if(inputtext.length>7){
                        liNode.html(inputtext.substring(0,7)+'…<span class="for_remove_item"></span>');
                    }else{
                        liNode.html(inputtext.trim()+'<span class="for_remove_item"></span>');
                    }
                    liNode.attr("id", "ut_" + data.id);
                    liNode.css("cursor", "pointer");
                    li.attr("mytitle",inputtext);
                    if(inputtext.length>7){
                        li.attr("title",inputtext);
                    }else{
                        li.attr("title",null);
                    }



                    $.qicTips({message:data.msg, level:1, target:"#ut_ul_list", mleft:0, mtop:-60});
                    var obj =data.id+"" ;
                    utMap[obj] = eval("("+data.info+")");
                    var obj =data.id+"" ;
                    var cndJson = utMap[obj];
                    var $li3 = $("#options_year1_2 > li[id = utt_"+cndId+"]");//删除原来的
                    $li3.remove();
                    var $li2;
                    if(inputtext.length>4){//当长度大于4的时候截取，并弹出tooltip
                        $li2 = $("<li id=utt_"+ data.id +"><a style='display:block;width: 100%' href='/newsCt/orderNews?id="+data.id +"' title='"+inputtext+"' class='tooltip'>"+inputtext.substring(0,4)+"…"+"</a></li>");
                    }else{
                        $li2 = $("<li id=utt_"+ data.id +"><a style='display:block;width: 100%' href='/newsCt/orderNews?id="+data.id +"' class='tooltip'>"+inputtext+ "</a></li>");
                    }
                    $li2.prependTo($("#options_year1_2"));

                    reset1();
                    addClickListenerToLiTag1();
                    registToolTip()//注册tooltip()
                }
                else {
                    $.qicTips({message:"您输入的名称已存在,请重新输入!", level:2, target:"#ut_ul_list", mleft:0, mtop:-60});
                }
            },
            error:function(){
                alert("出错了...");
            }

        });
    } else { //还原回去
        var tdNode = input.parent();
        if(inputtext.length>7){
            tdNode.html(inputtext.substring(0,7)+'…<span class="for_remove_item"></span>');
        }else{
            tdNode.html(inputtext.trim()+'<span class="for_remove_item"></span>');
        }
        tdNode.dblclick({"type":typeVal}, utListItemdblclick);
    }

    li.addClass("cndSelected");
}