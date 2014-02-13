/*公司公告js */
$(function(){
    $("#ut_ul_list li").live("dblclick", {"type":2}, utListItemdblclick)
    //编辑订阅
    $(".sel_subscribe_opt li").click(function(e){
        var lastChild = $(this).last().length,
            index = $(this).index(),
            length = $(this).parent("ul").find("li").length;
        if(index  == (length-1) ){
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
                    colseReset();
                },
                "width": 875,
                'height': 491,
                "resizable": false,
                 modal:true
            });
        }else{
            //后台处理的地方
        }
        $('#list1').html("");
        $("#reportPlateTree").html("");
        $("#resultList").html("");
        window.Orgtree = $('#list1').ligerTree({//公司公告来源树(机构)
            data:bulletinTree,
            checkbox:true,
            parentIcon: null,
            childIcon: null,
            getChecked:true,
            nodeDraggable:true,
            attribute:['id', 'url', 'code']
        });
        managerReportOrg = $("#list1").ligerGetTreeManager();
        managerReportOrg.collapseAll();
        $("#list1").find(".l-expandable-close").eq(0).trigger("click"); //默认展开第一级

        window.PlateTree =$("#reportPlateTree").ligerTree({//模块分类一级树
            data:plateTree,
            checkbox:true,
            parentIcon: null,
            childIcon: null,
            getChecked:true,
            attribute:['id','url','plateId'],
            onBeforeExpand:onBeforeExpand,
            onClick:onClick
        });
        managerPlateTree = $("#reportPlateTree").ligerGetTreeManager();
        managerPlateTree.collapseAll();
        $("#reportPlateTree").find(".l-expandable-close").eq(0).trigger("click"); //默认展开第一级
       // $("#list1").css({overflowX:'scroll',position:'relative'});
        //$("#reportPlateTree").css({overflowX:'scroll',position:'relative'});
        var expandedArr = [];
        function onBeforeExpand(node){
            var $dom = $("#reportPlateTree li[plateId='" + node.data.plateId + "']");
            var dom =  managerPlateTree.getNodeDom({"treedataindex":$dom.attr("treedataindex")});
            var checkClass = $(dom).find("div").eq(0).find("div").eq(1);
            if($(dom).find("div").eq(0).find("div").eq(1).hasClass("l-checkbox-checked")){
                var isChecked = true;
            }
            if (node.data.children && node.data.children.length == 0){
                $.ajax({
                    url:findPlateTreeByPlateTreeId.url(),
                    type:"post",
                    dataType:"json",
                    async:false,
                    data:{"id":node.data.plateId,"isChecked":isChecked},
                    success:function(data){
                        if(data.treedata != ""){
                            var json = eval('('+data.treeData+')');
                            $.each(expandedArr,function(i,n){                     //判断该节点是否展开过,若展开过，直接返回
                                if(n == node.data.plateId) {
                                    return false;
                                }
                            });
                            managerPlateTree.append(node.target,json);
                            expandedArr.push(node.data.plateId);

                            //板块数据宽度
                            $("#reportPlateTree").width(190);

                            selectNodeByResult();
                            // generateResultList();
                        }
                    },
                    complete:setTimeout(function(XMLHttpRequest, textStatus){
                        var $domSize = $(dom).find(".l-expandable-open").size();
                        $(dom).find(".l-expandable-open").slice(1,$domSize).trigger("click");
                        console.log( $(dom).find(".l-expandable-open").slice(1,$domSize));
                    },100)
                })
            }
        }
        function onClick(){
            $("li[type='1']").remove();
            var plateTree_val = $('#reportPlateTree').ligerTree().getChecked();
            var hasAppend=[];
            $.each(plateTree_val,function(i,n){
                var text = n.data["text"];
                var itemId = n.data["plateId"];
                //alert(itemId + "-----arrayreturn:" +$.inArray( itemId.substring(0,itemId.length-3), hasAppend ) +"----substring:" + itemId.substring(0,itemId.length-3));
                if(itemId.length == 4 || $.inArray( itemId.substring(0,itemId.length-3), hasAppend )<0){
                    $("#resultList").append("<li id=" + itemId + " type=1>"+ text +"</li>");
                    //标识已经append过了,最好的做法把append过了的子点节点从数组中清除 避免xunhuan，但要js的克隆对象方法
                }
                hasAppend.push(itemId);
                if(itemId == '1000') {
                    var $li = $("#resultList li[id='1000']");
                    $li.remove();
                }
            });
        }
        reset();
        addClickListenerToLiTag();//给右侧li标签添加click事件
    });

    $("#reset_bulletin").click(function(){
        reset();
    });

    //删除订阅条件
    $(".for_remove_item").live('click',function(event){
      /*  var $li = $("#ut_ul_list > li.cndSelected");
        if($li.size() == 0) {
            //alert("请先选择订阅条件");
            $.qicTips({message:'请先选择订阅条件', level:2, target:event.target, mleft:0, mtop:-10});
            return;
        }*/
        var $li = $(this).parent();
        var cndId = $li.attr("id").substring("ut_".length);
        new QicDialog({
            message:"是否确认删除?",
            title:"提示",
            confirm:function(){
                $.ajax({
                    url:delBulletinRoute.url(),
                    type:"post",
                    dataType:"json",
                    data:{"id":cndId},
                    success:function(data){
                        if(data.success){
                            $li.remove();
                            var $li2 = $("#options_year1_2 > li[id = "+cndId+"]");
                            console.log($li2);
                            $li2.remove();
                            $.qicTips({message:data.msg, level:1, target:'#ut_ul_list', mleft:0, mtop:-60});
                            $("#ut_ul_list > li:first").trigger("click");
                        }
                    }
                })
            }
        }).confirm();
    });

    //保存订阅条件
    $("#btn_save").click(function(){
        var orderName ;
        if($("#ut_ul_list > li >input").size()>0){
            $.qicTips({message:'请先保存订阅名称', level:2, target:'#ut_ul_list', mleft:0, mtop:-60});
            return;
        }
        if($("#ut_ul_list > li.cndSelected").size() > 0){
            var $li = $("#ut_ul_list > li.cndSelected");
            var cndId = $li.attr("id").substring("ut_".length);
            orderName = $("#ut_ul_list > li.cndSelected").text();
        }else{
            $.qicTips({message:'请先选择订阅名称', level:2, target:'#ut_ul_list', mleft:0, mtop:-60});
            return;
        }
        saveReport(orderName);
    });
    //关闭保存窗口
    $("#report_save_cancle").click(function(){
        $("#report_save").dialog("close");
    })

   /* $("#btn_cancle").click(function(){
        $(".mysubcribe_box").dialog("close");
    })*/

    //删除结果列表项
    $("#result_del").click(function(event){
        //$("#reportPlateTree").html("");
        var $li = $("#resultList > li.cndSelected");
        if($li.size() == 0) {
            $.qicTips({message:'请先选择板块', level:2, target:event.target, mleft:0, mtop:-10});
            return;
        }
        $li.remove();
        selectNodeByResult();
    });

    //根据结果列表选中板块树节点
    function selectNodeByResult(){
        var result_ids = [];
        $("#resultList li").each(function(i,n){
            result_ids.push($(this).attr("id"));
        });
        /*var $li = $("#ut_ul_list > li.cndSelected");
         var cndId = $li.attr("id").substring("ut_".length);
         var cndJson = utMap[cndId];
         var reportPlateTreeArr = cndJson.plateTree;*/

        //clearTreeItem();
        clearPlateTree();
        if($.isArray(result_ids)){
            $.each(result_ids,function(i,n){
                if(("" + n ).length > 0){ //n要有值
                    var $dom = $("#reportPlateTree li[plateId='" + n + "']");
                    if($dom.get(0)){
                        var obj = $("#" + n);
                        if(obj.attr("type") == 3){
                            obj.attr("type",1);
                        }
                    }else{
                        var flag = n.substring(0,4);//适用目前数据结构去解决（暂时性解决方案）
                        $("#reportPlateTree li[plateId='" + flag + "']").find(".l-checkbox-unchecked").eq(0).removeClass("l-checkbox-unchecked").addClass("l-checkbox-incomplete");
                    }
                    window.PlateTree.selectNode($dom);
                    var domLevel =1+ parseInt($dom.attr("outlinelevel"));
                    for(var i = domLevel;i < domLevel + 6; i ++){
                        $dom.find("li[outlinelevel="+ i +"]").each(function(i){
                            window.PlateTree.selectNode($(this));                     // 子节点选中
                        });
                    }
                }
            });
        }
    }

    $("#resultList li").live("click", function(){
        $("#resultList > li.cndSelected").removeClass("cndSelected");
        var $li = $(this);
        $li.addClass("cndSelected");
    });


    //自动完成
    function log( message ,symbol) {
        if($("#resultList").find("li[id="+symbol+"]").index() == -1)//如果不存在重复的才附加
            $("#resultList").append("<li id="+symbol+" type=2>"+message+"</li>");//type=2 : 股票数据
        $("#resultList" ).scrollTop( 0 );
    }
   /* //按enter键清除input框内容
    $("#autoComplete").live("focus", function(){
        document.onkeydown =function(e){
            if(e.keyCode == 13){
                $("#autoComplete").val("");
            }
        }
    });*/

    $("#autoComplete").autocomplete({
        source:function(request, response){
            $.ajax({
                url: findStockAutoComplete.url(),
                type:"get",
                dataType: "json",
                data: {
                    "keys": request.term
                },
                success: function( data ) {
                    var arr = [];
                    $.each(data,function(i,n){
                        arr[i] = n;
                    })
                    response( $.map( arr, function( item ) {
                        //alert(item);
                        return {
                            label:item.SYMBOL + "  " + item.SHORTNAME,
                            value:item.SHORTNAME,
                            symbol:item.SYMBOL
                        }

                    }));
                }
            });

        },
        minLength:1,
        autoFocus:true,
        select:function(event,ui){
            log( ui.item ?
                ui.item.value :
                "Nothing selected, input was " + this.value,ui.item.symbol);
        },
        open: function() {
            $( this ).removeClass( "ui-corner-all" ).addClass( "ui-corner-top" );
        },
        close: function() {
            $("#autoComplete").val("");
            $( this ).removeClass( "ui-corner-top" ).addClass( "ui-corner-all" );
        }

    });

    $(document).keydown(function(e){
        if(e.keyCode == 13 && $("#autoComplete").is(":focus")){
            if($("#autoComplete").val() == "") {
               return false;
            }
        }
    });

    //增加订阅条件
    $(".subscribe_add").live('click',function () {
        //说明有重名的,要处理重名
        var $input2 = $("#ut_ul_list > li > input");
        if($input2.size() > 0){
            //console.log("还有input啊...");
            new QicDialog({
                message:"请先保存当前订阅条件",
                title:"提示"
            }).warn();
            //$('#ut_ul_list').scrollTop($input2.get(0).offsetHeight); //滚动到当前可见的位置
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

        var $li = $("<li id='ut_-999' class='ut_li_text tooltip' mytitle='"+newName+"'  title='"+newName+"'>" + newName + "</li>");
        registToolTip();

        $li.addClass("cndSelected");
        $li.prependTo($("#ut_ul_list"));
        // $('#ut_ul_list').scrollTop($('#ut_ul_list')[0].scrollHeight+10); //滚动到底端
        addClickListenerToLiTag();
        $li.trigger("dblclick");

        //初始化交易类型和品种
        reset();
    });

    $("#btn_cancle").live('click',function(){
        reset();
    });

});


var symbolCache={};//缓存订阅条件的股票名
var plateTreeCache={};//缓存订阅条件的版块名
//点击订阅列表查看信息
function addClickListenerToLiTag() {
    $("#ut_ul_list > li").unbind("click");
    $("#ut_ul_list > li").click(function () {
        $("#ut_ul_list > li.cndSelected").removeClass("cndSelected");
        var $li = $(this);
        $li.addClass("cndSelected");
        var cndId = $li.attr("id").substring("ut_".length);
        var cndJson = utMap[cndId];
        clearTreeItem();
        $("#resultList").html("");
        var bulletinClassifyArr = cndJson.bulletinClassify;
        var bulletinPlateTreeArr = cndJson.plateTree;
        var symbolArr = cndJson.symbolArr;
        if ($.isArray(bulletinClassifyArr)) {
            $.each(bulletinClassifyArr, function (i, n) {
                if (("" + n ).length > 0) {
                    var $dom = $("#list1 li[code='" + n + "']");
                    window.Orgtree.selectNode($dom);
                }
            });
        }

        if($.isArray(bulletinPlateTreeArr)){
            $.each(bulletinPlateTreeArr,function(i,n){
                if(("" + n ).length > 0){
                    var $dom = $("#reportPlateTree li[plateId='" + n + "']");
                    if($dom.get(0)){
                        window.PlateTree.selectNode($dom);
                    }else{
                        var flag = n.substring(0,4);//适用目前数据结构去解决（暂时性解决方案）
                        $("#reportPlateTree li[plateId='" + flag + "']").find(".l-checkbox-unchecked").eq(0).removeClass("l-checkbox-unchecked").addClass("l-checkbox-incomplete");
                    }
                    var $fatherDom = $("#reportPlateTree li[plateId='" + n.substring(0,4) + "']");
                    $fatherDom.find('div').eq(3).addClass('l-checkbox-incomplete');
                    var $firstDom = $("#reportPlateTree li[outlinelevel='1']");
                    $firstDom.find('div').eq(2).addClass('l-checkbox-incomplete');
                    var domLevel =1+ parseInt($dom.attr("outlinelevel"));
                    $dom.find("li[outlinelevel="+domLevel +"]").each(function(i){
                        window.PlateTree.selectNode($(this));
                    }) ;
                }
            });
        }

        if(!plateTreeCache[cndId]) {
            var treeArr = "";
            for(var i = 0 ;i<bulletinPlateTreeArr.length;i++){
                treeArr += (i==bulletinPlateTreeArr.length-1?bulletinPlateTreeArr[i] : bulletinPlateTreeArr[i] + ",");
            }
            if($.isArray(bulletinPlateTreeArr)){
                $.ajax({
                    url:getPlateTreeName.url(),
                    type:"post",
                    dataType:"json",
                    data:{"reportArr":treeArr},
                    success:function(data){
                        plateTreeCache[cndId] = data;
                        appendPlateTreeToResult(data);
                    }
                });
            }
        } else {
            appendPlateTreeToResult(plateTreeCache[cndId]);
        }

        if(!symbolCache[cndId]) {
            var param = "";
            for(var i = 0 ;i<symbolArr.length;i++){
                param += (i==symbolArr.length-1?symbolArr[i] : symbolArr[i] + ",");
            }
            if($.isArray(symbolArr)){
                $.ajax({
                    url:findStockBySymbol.url(),
                    type:"post",
                    dataType:"json",
                    data:{"symbolArr":param},
                    success:function(data){
                        symbolCache[cndId] = data;
                        appendSymbolToResult(data);
                    }

                })
            }
        } else{
            appendSymbolToResult(symbolCache[cndId]);
            //delete   symbolCache[cndId];    when edit
        }
    });
}

//把缓存放入结果列表
function appendSymbolToResult(data){
    for(var i=0 ;i<data.length ;i++){
        var obj = data[i];
        $("#resultList").append("<li id=" + data[i].symbol+ " type=2 >"+ data[i].shortname +"</li>");
    }
}

function appendPlateTreeToResult(data){
    var plateTree_val = $('#reportPlateTree').ligerTree().getChecked();
    for(var i = 0; i < data.length; i++){
        var id = data[i].id;
        var flag = false;
        var item = "";
        $.each(plateTree_val,function(i,n){
            item = n.data["plateId"];
            if(id == item){
                flag = true;
            }
        })
        if(flag == true){
            $("#resultList").append("<li id=" + data[i].id + " type=1 >"+ data[i].text +"</li>");
        } else{
            $("#resultList").append("<li id=" + data[i].id + " type=3 >"+ data[i].text +"</li>");
        }
    }
}


//得到选中数节点的值，并存入订阅表单的隐藏域中
function getTreeVal() {
    var bulletin_tree_val = $('#list1').ligerTree().getChecked();
    var plateTree_val = $('#reportPlateTree').ligerTree().getChecked();
    var bulletin_tree_ids = [];
    var result_ids = [];
    var symbol_ids = [];
    $.each(bulletin_tree_val,function(i,n){
        var itemData = n.data["code"];
        bulletin_tree_ids.push(itemData);
    });

    $("#resultList li").each(function(i,n){
        if($(this).attr("type")=="2"){
            symbol_ids.push($(this).attr("id"));
        }
        else{
            result_ids.push($(this).attr("id"));
        }

    });
    $("#bulletin_tree").val(bulletin_tree_ids.join(','));
    $("#plateTree").val(result_ids.join(','));
    $("#symbolList").val(symbol_ids.join(','));
}

//清空节点
function clearTreeItem(){
    var parm = function (data){
        return false;
    };
    window.Orgtree.selectNode(parm);
    window.PlateTree.selectNode(parm);
    $("div.l-checkbox-incomplete").removeClass("l-checkbox-incomplete");
}

//清空板块树
function clearPlateTree(){
    var parm = function (data){
        return false;
    };
    window.PlateTree.selectNode(parm);
    $("#reportPlateTree").find(".l-checkbox-incomplete").removeClass("l-checkbox-incomplete");
}

//重置
function reset(){
    clearTreeItem();
    //if($("#ut_ul_list > li.cndSelected").size()>0){
      //  $("#ut_ul_list > li.cndSelected").removeClass("cndSelected");
   // }
    $("#resultList").html("");
}

//关闭重置
function colseReset(){
    clearTreeItem();
    if($("#ut_ul_list > li.cndSelected").size()>0){
      $("#ut_ul_list > li.cndSelected").removeClass("cndSelected");
     }
    $("#resultList").html("");
}

//保存公司公告订阅
function saveReport(name) {
        var $li = $("#ut_ul_list > li.cndSelected");
        var cndId = $li.attr("id").substring("ut_".length);
        delete  symbolCache[cndId];  //删除缓存订阅条件的股票名
        delete  plateTreeCache[cndId]; //删除缓存订阅条件的版块名
        editReport(name,cndId);
}


//编辑研报订阅
function editReport(name,cndId){
    getTreeVal();
    var isSuccess = true;
    var $form = $("#BulletinOrderForm");
    var params = $form.serializeArray();
    params.push({name: 'name', value: name});
    params.push({name: 'id', value: cndId});
    params.push({name: 'iFlag', value: 0});
    $.ajax({
        url:editBulletinOrderUrl.url(),
        type:"post",
        dataType:"json",
        async : false,
        data: params,
        success:function(data){
            if(data.success){
                addClickListenerToLiTag();
                var obj = cndId+"";
                utMap[obj] = eval("("+data.info+")");
//                $("#ut_"+cndId).text(name);
//                $("#"+ cndId).find("a").text(name);
                $.qicTips({message:data.msg, level:1, target:'.cndSelected', mleft:0, mtop:-60});
            }
            else{
                isSuccess = false;
                $.qicTips({message:data.msg, level:2, target:'#ut_ul_list', mleft:0, mtop:-60});
            }
        },
        error:function(){
            alert("出错了...");
            isSuccess = false;
        }

    });
    return isSuccess;
}




//搜索条件列表项双击事件处理
function utListItemdblclick(e) {
    var li = $(this);
    if($("#ut_ul_list > li >input").size()>0){
        //$.qicTips({message:'请先保存订阅名称', level:2, target:'#ut_ul_list', mleft:0, mtop:-60});
        return;
    }
    if(! li.hasClass("cndSelected")){ //如果当前不选中的话, 直接跳过
        return;
    }
    var typeVal = e.data.type;
    var li = $(this);
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
        return ;
    }

    var sameName = false;
    $("#ut_ul_list > li").each(function(i){
        var $eachThis = $(this);
        //console.log(($eachThis.html()).trim());
        //console.log("-------------"+inputtext+'<span class="for_remove_item"></span>');
        //  console.log($eachThis.html());
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
        var $form = $("#BulletinOrderForm");
        var params = $form.serializeArray();
        params.push({name: 'name', value: inputtext});
        params.push({name: 'cid', value: cndId});
        $.ajax({
            url:addBulletinOrderUrl.url(),
            type:"post",
            dataType:"json",
            async : false,
            data: params,
            success:function(data){
                if(data.success){
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
                    if(inputtext.length>7){
                        li.attr("title",inputtext);
                    }else{
                        li.attr("title",null);
                    }
                    li.attr("mytitle",inputtext);
                    addClickListenerToLiTag();//给右侧li标签添加click事件
                    var obj =data.id+"" ;
                    utMap[obj] = eval("("+data.info+")");
                    var $li3 = $("#options_year1_2 > li[id = "+cndId+"]");//删除原来的
                    $li3.remove();


                    var $li2;
                    if(inputtext.length>4){//当长度大于4的时候截取，并弹出tooltip
                        $li2 = $("<li id="+ data.id +"><a style='display:block;width: 100%' href='/bulletinCt/orderBulletinInfo?orderId="+data.id +"' title='"+inputtext+"' class='tooltip'>"+inputtext.substring(0,4)+"…"+"</a></li>");
                    }else{
                        $li2 = $("<li id="+ data.id +"><a style='display:block;width: 100%' href='/bulletinCt/orderBulletinInfo?orderId="+data.id +"' class='tooltip'>"+inputtext+"</a></li>");
                    }
                    $li2.prependTo($("#options_year1_2"));
                    registToolTip()//注册tooltip()
                    $.qicTips({message:data.msg, level:1, target:"#ut_ul_list", mleft:0, mtop:-60});
                }
                else{
                    $.qicTips({message:data.msg, level:2, target:"#ut_ul_list", mleft:0, mtop:-60});
                    isSuccess = false;
                }
                liNode.dblclick({"type":typeVal}, utListItemdblclick);
            },
            error:function(){
                alert("出错了...");
                isSuccess = false;
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