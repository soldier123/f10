/*研报机构js */
$(function () {
    //编辑订阅
    $(".sel_subscribe_opt li").click(function (e) {
        var lastChild = $(this).last().length,
            index = $(this).index(),
            length = $(this).parent("ul").find("li").length;
        if (index == (length - 1)) {
            $(".mysubcribe_box").dialog({
                beforeClose: function(){
                    if($("#ut_ul_list > li >input").size()>0){
                        return false;
                    }else{
                        return true;
                    }
                },
                "autoOpen":true,
                "width":1005,
                'height':491,
                "resizable":false,
                modal:true
            });
            clearCndSelected();
        } else {

        }
        //window.Orgtree.clear();
        //window.Classifytree.clear();
        $('#reportOrgTree').html("");                 //树清空
        $("#reportClassifyTree").html("");
        $("#reportPlateTree").html("");
        $("#resultList").html("");
        window.Orgtree = $('#reportOrgTree').ligerTree({          //研报来源树(机构)
            data:reportOrg,
            checkbox:true,
            parentIcon:null,
            childIcon:null,
            getChecked:true,
            nodeWidth:110,
            attribute:['id', 'url', 'data-orgId']
        });
        managerReportOrg = $("#reportOrgTree").ligerGetTreeManager();
        managerReportOrg.collapseAll();
        $("#reportOrgTree").find(".l-expandable-close").eq(0).trigger("click"); //默认展开第一级

        window.Classifytree = $('#reportClassifyTree').ligerTree({                  //报告类型树
            data:reportClassifyConfig,
            checkbox:true,
            parentIcon:null,
            childIcon:null,
            getChecked:true,
            nodeWidth:110,
            attribute:['id', 'url', 'code']
        });
        managerClassifyTree = $("#reportClassifyTree").ligerGetTreeManager();
        managerClassifyTree.collapseAll();
        $("#reportClassifyTree").find(".l-expandable-close").eq(0).trigger("click"); //默认展开第一级

        window.PlateTree = $("#reportPlateTree").ligerTree({
            data:plateTree,
            checkbox:true,
            parentIcon:null,
            childIcon:null,
            nodeWidth:90,
            getChecked:true,
            //idFieldName :'id',
            //parentIDFieldName :'pid',
            attribute:['id', 'url', 'plateId'],
            onBeforeExpand:onBeforeExpand,
            onCheck:onCheck
        });
        managerPlateTree = $("#reportPlateTree").ligerGetTreeManager();
        managerPlateTree.collapseAll();
        $("#reportPlateTree").find(".l-expandable-close").eq(0).trigger("click"); //默认展开第一级

        var expandedArr = [];

        function onBeforeExpand(node) {
            var $dom = $("#reportPlateTree li[plateId='" + node.data.plateId + "']");
            var dom = managerPlateTree.getNodeDom({"treedataindex":$dom.attr("treedataindex")});
            /*var checkClass = $(dom).find("div").eq(0).find("div").eq(1);
             if($(dom).find("div").eq(0).find("span").prev("div").attr("class")=="l-box l-checkbox l-checkbox-checked"){
             var isChecked = true;
             }*/
            if (node.data.children && node.data.children.length == 0) {
                $.ajax({
                    url:findPlateTreeByPlateTreeId.url(),
                    type:"post",
                    dataType:"json",
                    data:{"id":node.data.plateId},
                    success:function (data) {
                        if (data.treedata != "") {
                            var json = eval('(' + data.treeData + ')');
                            $.each(expandedArr, function (i, n) {                     //判断该节点是否展开过,若展开过，直接返回
                                if (n == node.data.plateId) {
                                    return false;
                                }
                            });
                            managerPlateTree.append(node.target, json);
                            expandedArr.push(node.data.plateId);

                            //板块数据宽度
                            $("#reportPlateTree").width(190);

                            selectNodeByResult();
                            // generateResultList();
                        }
                    }
                })

            }
        }

        function onCheck(node, checked) {
            window.setTimeout(function () {
                generateResultList();
            }, 200);
        }

        //$("#ut_ul_list > li:first").trigger("click");   //默认选中第一个
        reset();
        addClickListenerToLiTag();//给左侧li标签添加click事件
        delSubscribe();            //添加删除事件
    });

    $(".dark_s_reset").click(function () {
        reset();
    });
    function generateResultList() {
        $("#resultList li[type='1']").remove();

        var plateTree_val = $('#reportPlateTree').ligerTree().getChecked();
        var hasAppend = [];

        $.each(plateTree_val, function (i, n) {
            var text = n.data["text"];
            var itemId = n.data["plateId"];
            //alert(itemId + "-----arrayreturn:" +$.inArray( itemId.substring(0,itemId.length-3), hasAppend ) +"----substring:" + itemId.substring(0,itemId.length-3));
            if (itemId.length == 4 || $.inArray(itemId.substring(0, itemId.length - 3), hasAppend) < 0) {
                $("#resultList").append("<li id=" + itemId + " type=1>" + text + "</li>");                      //type=1 : 板块数据
                //标识已经append过了,最好的做法把append过了的子点节点从数组中清除 避免xunhuan，但要js的克隆对象方法
            }
            hasAppend.push(itemId);
            if(itemId == '1000') {
                var $li = $("#resultList li[id='1000']");
                $li.remove();
            }
        });
    }




    //保存弹出窗口
    $("#btn_save").click(function () {
        if($("#ut_ul_list > li >input").size()>0){
            $.qicTips({message:'请先保存订阅名称', level:2, target:'#ut_ul_list', mleft:0, mtop:-60});
            return;
        }
        if($("#ut_ul_list > li.cndSelected").size() == 0) {
            $.qicTips({message:'请选择订阅名称', level:2, target:"#btn_save", mleft:-60, mtop:-30});
            return;
        }
        var orderName;
        if ($("#ut_ul_list > li.cndSelected").size() > 0) {
            var $li = $("#ut_ul_list > li.cndSelected");
            var cndId = $li.attr("id").substring("ut_".length);
            orderName = $("#ut_ul_list > li.cndSelected").text();
        }
        saveReport(orderName);
        /*new QicDialog({
         title:" 保存",
         initPromptValue:!orderName ? "" : orderName,
         label:"请输入订阅名称",
         autoClose:false,
         confirm:function (value, dialog) {
         var isSuccess = saveReport(value);
         if (isSuccess != false) {
         dialog.destroy();
         }
         },
         cancel:function () {

         }
         }).prompt();*/
    })
    //关闭保存窗口
    $("#report_save_cancle").click(function () {
        $("#report_save").dialog("close");
    })

    $("#btn_cancle").click(function () {
        reset();
    })

    //删除结果列表项
    $("#result_del").click(function (event) {
        //$("#reportPlateTree").html("");
        var $li = $("#resultList > li.cndSelected");
        if ($li.size() == 0) {
            $.qicTips({message:'请选择板块', level:2, target:event.target, mleft:-60, mtop:-30});
            return;
        }
        $li.remove();
        selectNodeByResult();

    })
    //根据结果列表选中板块树节点
    function selectNodeByResult() {
        var result_ids = [];
        $("#resultList li").each(function (i, n) {
            result_ids.push($(this).attr("id"));
        });
        /*var $li = $("#ut_ul_list > li.cndSelected");
         var cndId = $li.attr("id").substring("ut_".length);
         var cndJson = utMap[cndId];
         var reportPlateTreeArr = cndJson.plateTree;*/

        //clearTreeItem();
        clearPlateTree();
        if ($.isArray(result_ids)) {
            $.each(result_ids, function (i, n) {
                if (("" + n ).length > 0) { //n要有值
                    for (var i = 4; i < n.length; i++) {
                        $("#reportPlateTree li[plateId='" + n.substring(0,4+(i-4)*3) + "']").find('div').eq(i-1).addClass('l-checkbox-incomplete');
                    }
                    var $dom = $("#reportPlateTree li[plateId='" + n + "']");
                    window.PlateTree.selectNode($dom);
                    if ($dom.get(0)) {
                        var obj = $("#" + n);
                        if (obj.attr("type") == 3) {
                            obj.attr("type", 1);
                        }
                    }
                    //var $fatherDom = $("#reportPlateTree li[plateId='" + n.substring(0,4) + "']");
                    //$fatherDom.find('div').eq(3).addClass('l-checkbox-incomplete');
                    var domLevel = 1 + parseInt($dom.attr("outlinelevel"));
                    for (var i = domLevel; i < domLevel + 6; i++) {
                        $dom.find("li[outlinelevel=" + i + "]").each(function (i) {
                            window.PlateTree.selectNode($(this));                     // 子节点选中
                        });
                    }
                }
            });
        }
    }

    $("#resultList li").live("click", function () {
        $("#resultList > li.cndSelected").removeClass("cndSelected");
        var $li = $(this);
        $li.addClass("cndSelected");
    });

    //自动完成
    function log(message, symbol) {
        //$( "<div>" ).text( message ).prependTo( "#resultList" );
        var flag = false;
        $("#resultList li").each(function (i, n) {
            if ($(this).attr("id") == symbol) {
                flag = true;
            }
        });
        if (flag == false) {
            $("#resultList").append("<li id=" + symbol + " type=2>" + message + "</li>");          //type=2 : 股票数据
            $("#resultList").scrollTop(0);
        }
    }

    $("#autoComplete").autocomplete({
        source:function (request, response) {
            $.ajax({
                url:findStockAutoComplete.url(),
                type:"get",
                dataType:"json",
                data:{
                    "name_startsWith":request.term
                },
                success:function (data) {
                    var arr = [];
                    $.each(data, function (i, n) {
                        arr[i] = n;
                    })
                    response($.map(arr, function (item) {
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
        select:function (event, ui) {
            log(ui.item ?
                ui.item.value :
                "Nothing selected, input was " + this.value, ui.item.symbol);
        },
        open:function () {
            $(this).removeClass("ui-corner-all").addClass("ui-corner-top");
        },
        close:function () {
            $("#autoComplete").val("");
            $(this).removeClass("ui-corner-top").addClass("ui-corner-all");
        }

    });

    $(document).keydown(function(e){
        if(e.keyCode == 13 && $("#autoComplete").is(":focus")){
           return false;
        }
    });

 /*   //按enter键清除input框内容
    $("#autoComplete").live("focus", function(){
        document.onkeydown =function(e){
            if(e.keyCode == 13){
                $("#autoComplete").val("");
            }
        }
    });*/

    $("#ut_ul_list li").live("dblclick", {"type":cndType}, utListItemdblclick);
    //新增订阅
    $("#subscribe_add").click(function () {
        //说明有重名的,要处理重名
        var $input2 = $("#ut_ul_list > li > input");
        if ($input2.size() > 0) {
            //console.log("还有input啊...");
            new QicDialog({
                message:"请先保存订阅",
                title:"提示"
            }).warn();
            $('#ut_ul_list').scrollTop($input2.get(0).offsetHeight); //滚动到当前可见的位置
            $input2.get(0).focus();
            return;
        }
        if ($("#ut_ul_list > li").size() >= 20) {
            new QicDialog({
                message:"此功能只能保存20个订阅",
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
            tmpNameIntArr.sort(function (o1, o2) {
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
        //var $li2 = $("<li id='ut_-999'><a style='display:block;width: 100%' href='/reportct/reportinfo?sp.id=-999'>" + newName + "</a></li>");

        addClickListenerToLiTag();
        $li.trigger("dblclick");
        reset();
    });

});
var symbolCache = {};//缓存订阅条件的股票名
var plateTreeCache = {};//缓存订阅条件的版块名
function addClickListenerToLiTag() {       //点击订阅列表查看信息
    $("#ut_ul_list > li").unbind("click");
    $("#ut_ul_list > li").click(function () {
        $("#ut_ul_list > li.cndSelected").removeClass("cndSelected");
        var $li = $(this);
        $li.addClass("cndSelected");
        var cndId = $li.attr("id").substring("ut_".length);
        var cndJson = utMap[cndId];
        if (cndType == 103) {
            clearTreeItem();
            $("#resultList").html("");
            var reportOrgArr = cndJson.reportOrg;
            var reportClassifyArr = cndJson.reportClassify;
            var reportPlateTreeArr = cndJson.plateTree;
            var symbolArr = cndJson.symbolArr;
            if ($.isArray(reportOrgArr)) {
                $.each(reportOrgArr, function (i, n) {
                    if (("" + n ).length > 0) { //n要有值
                        var $dom = $("#reportOrgTree li[data-orgId='" + n + "']");
                        window.Orgtree.selectNode($dom);
                    }
                });
            }
            if ($.isArray(reportClassifyArr)) {
                $.each(reportClassifyArr, function (i, n) {
                    if (("" + n ).length > 0) {
                        var $dom = $("#reportClassifyTree li[code='" + n + "']");
                        window.Classifytree.selectNode($dom);
                    }
                });
            }
            if ($.isArray(reportPlateTreeArr)) {
                $.each(reportPlateTreeArr, function (i, n) {
                    if (("" + n ).length > 0) {
                        var $dom = $("#reportPlateTree li[plateId='" + n + "']");
                        window.PlateTree.selectNode($dom);
                        var $fatherDom = $("#reportPlateTree li[plateId='" + n.substring(0,4) + "']");
                        $fatherDom.find('div').eq(3).addClass('l-checkbox-incomplete');
                        var $firstDom = $("#reportPlateTree li[outlinelevel='1']");
                        $firstDom.find('div').eq(2).addClass('l-checkbox-incomplete');
                        var domLevel = 1 + parseInt($dom.attr("outlinelevel"));
                        for (var i = domLevel; i < domLevel + 6; i++) {
                            $dom.find("li[outlinelevel=" + i + "]").each(function (i) {
                                window.PlateTree.selectNode($(this));
                            });
                        }
                        //var dom =  managerPlateTree.getNodeDom({"treedataindex":$dom.attr("treedataindex")});
                        //$("#resultList").append("<li id=" + n + " type=1 >"+ $(dom).find("div").eq(0).find("span").text() +"</li>");

                    }
                });
            }
            if ($.isArray(reportPlateTreeArr)) {
                if (!plateTreeCache[cndId]) {
                    var treeArr = "";
                    for (var i = 0; i < reportPlateTreeArr.length; i++) {
                        treeArr += (i == reportPlateTreeArr.length - 1 ? reportPlateTreeArr[i] : reportPlateTreeArr[i] + ",");
                    }

                    $.ajax({
                        url:getPlateTreeName.url(),
                        type:"post",
                        dataType:"json",
                        data:{"reportPlateTreeArr":treeArr},
                        success:function (data) {
                            plateTreeCache[cndId] = data;
                            appendPlateTreeToResult(data);
                        }
                    });

                } else {
                    appendPlateTreeToResult(plateTreeCache[cndId]);
                }
            }
            if ($.isArray(symbolArr)) {
                if (!symbolCache[cndId]) {
                    var param = "";
                    for (var i = 0; i < symbolArr.length; i++) {
                        param += (i == symbolArr.length - 1 ? symbolArr[i] : symbolArr[i] + ",");
                    }

                    $.ajax({
                        url:findStockBySymbol.url(),
                        type:"post",
                        dataType:"json",
                        data:{"symbolArr":param},
                        success:function (data) {
                            symbolCache[cndId] = data;
                            appendSymbolToResult(data);
                        }

                    });

                } else {
                    appendSymbolToResult(symbolCache[cndId]);
                    //delete   symbolCache[cndId];    when edit
                }
            }
        }
    });
}
function appendSymbolToResult(data) {
    for (var i = 0; i < data.length; i++) {
        var obj = data[i];
        $("#resultList").append("<li id=" + data[i].symbol + " type=2 >" + data[i].shortname + "</li>");
    }
}
function appendPlateTreeToResult(data) {
    var plateTree_val = $('#reportPlateTree').ligerTree().getChecked();
    for (var i = 0; i < data.length; i++) {
        var id = data[i].id;
        var flag = false;
        var item = "";
        $.each(plateTree_val, function (i, n) {
            item = n.data["plateId"];
            if (id == item) {
                flag = true;
            }
        })
        if (flag == true) {
            $("#resultList").append("<li id=" + data[i].id + " type=1 >" + data[i].text + "</li>");
        } else {
            $("#resultList").append("<li id=" + data[i].id + " type=3 >" + data[i].text + "</li>");
        }
    }
}

//得到选中树节点的值，并存入订阅表单的隐藏域中
function getTreeVal() {
    var OrgTree_val = $('#reportOrgTree').ligerTree().getChecked();
    var Classify_val = $('#reportClassifyTree').ligerTree().getChecked();
    var OrgTree_ids = [];
    var classifyTree_ids = [];
    $.each(OrgTree_val, function (i, n) {
        var itemData = n.data["data-orgId"];
        if(itemData != null){
            OrgTree_ids.push(itemData);
        }
    });
    $.each(Classify_val, function (i, n) {
        var itemData2 = n.data["code"];
        if(itemData2 != null) {
            classifyTree_ids.push(itemData2);
        }
    });

    var result_ids = [];
    var symbol_ids = [];
    $("#resultList li").each(function (i, n) {
        if ($(this).attr("type") == "1" || $(this).attr("type") == "3") {
            result_ids.push($(this).attr("id"));
        }
        else {
            symbol_ids.push($(this).attr("id"));
        }

    });

    $("#OrgTree").val(OrgTree_ids.join(','));
    $("#classifyTree").val(classifyTree_ids.join(','));
    $("#plateTree").val(result_ids.join(','));
    $("#symbolList").val(symbol_ids.join(','));

}

//清空节点
function clearTreeItem() {
    var parm = function (data) {
        return false;
    };
    window.Orgtree.selectNode(parm);
    window.Classifytree.selectNode(parm);
    window.PlateTree.selectNode(parm);
    $("div.l-checkbox-incomplete").removeClass("l-checkbox-incomplete");
}

//清空板块树
function clearPlateTree() {
    var parm = function (data) {
        return false;
    };
    window.PlateTree.selectNode(parm);
    $("#reportPlateTree").find(".l-checkbox-incomplete").removeClass("l-checkbox-incomplete");
}
//重置
function reset() {
    clearTreeItem();
//    if($("#ut_ul_list > li.cndSelected").size()>0){
//        $("#ut_ul_list > li.cndSelected").removeClass("cndSelected");
//    }
    $("#resultList").html("");
}

//清掉左边选中的条件列表项
function clearCndSelected() {
    if ($("#ut_ul_list > li.cndSelected").size() > 0) {
        $("#ut_ul_list > li.cndSelected").removeClass("cndSelected");
    }
}

//删除订阅条件
function delSubscribe(){
    $(".for_remove_item").live('click',function () {
        var $li = $(this).parent();
        /*if ($li.size() == 0) {
         //alert("请先选择订阅条件");
         $.qicTips({message:'请先选择订阅条件', level:2, target:event.target, mleft:-60, mtop:-30});
         return;
         }*/
        //alert($(this).parent().attr("id"));
        var cndId = $(this).parent().attr("id").substring("ut_".length);
        new QicDialog({
            message:"是否确认删除?",
            title:"提示",
            confirm:function () {
                $.ajax({
                    url:delCondRoute.url(),
                    type:"post",
                    dataType:"json",
                    data:{"id":cndId},
                    success:function (data) {
                        if (data.success) {
                            $li.remove();
                            var $li2 = $("#options_year1_2 > li[id = " + cndId + "]");
                            $li2.remove();
                            $.qicTips({message:data.msg, level:1, target:"#ut_ul_list", mleft:0, mtop:-60});
                            $("#ut_ul_list > li:first").trigger("click");
                        }
                    }
                })
            }
        }).confirm();
    });
}


//保存研报订阅    addReport():保存订阅  editReport():编辑订阅
function saveReport(name) {
    //保存订阅
    //alert($("#ut_ul_list > li").size());
    /*if (checked(name)) {
     return false;
     }*/
    if ($("#ut_ul_list > li").size() > 20) {
        new QicDialog({
            message:"自定义订阅上限为20个",
            title:"提示"
        }).warn();
        return false;
    }
    // var name = dialog.getPromptValue();
    var isSuccess = false;
    /*if (!name || name == "") {
     $.qicTips({message:"输入名称为空！", level:2, target:"#promptId", mleft:0, mtop:-60});
     return false;
     }*/

    /*if ($("#ut_ul_list > li.cndSelected").size() == 0) {
     $.ajax({
     url:checkOrderName.url(),
     type:"get",
     dataType:"json",
     async:false,
     data:{"name":name},
     success:function (data) {
     if (data.success) {
     //getTreeVal();
     //alert("ok");
     isSuccess = addReport(name);
     }
     else {
     $.qicTips({message:"您输入的名称已存在,请重新输入!", level:2, target:"#qic_dialog_confirm_btn", mleft:0, mtop:-60});
     */
    /*new QicDialog({
     message:"您输入的名称已存在,请重新输入!",
     title:"提示"
     }).warn();*/
    /*
     isSuccess = false;
     }
     },
     error:function () {
     alert("出错了...");
     isSuccess = false;
     }

     });
     }
     else {*/
    var $li = $("#ut_ul_list > li.cndSelected");
    var cndId = $li.attr("id").substring("ut_".length);
    delete  symbolCache[cndId];  //删除缓存订阅条件的股票名
    delete  plateTreeCache[cndId]; //删除缓存订阅条件的版块名
    editReport(name, cndId);
    /*if (name == utName[cndId]) {       //判断名字是否更改，如果没改，就不用验证是否重名
        isSuccess = editReport(name, cndId);
    }
    else {
        $.ajax({
            url:checkOrderName.url(),
            type:"get",
            dataType:"json",
            async:false,
            data:{"name":name},
            success:function (data) {
                if (data.success) {
                    isSuccess = editReport(name, cndId);
                }
                else {
                    $.qicTips({message:"您输入的名称已存在,请重新输入!", level:2, target:"#btn_save", mleft:0, mtop:-60});
                    *//*new QicDialog({
                     message:"您输入的名称已存在,请重新输入!",
                     title:"提示"
                     }).warn();*//*
                    isSuccess = false;
                }
            },
            error:function () {
                alert("出错了...");
                isSuccess = false;
            }
        });
    }
*/

    //}
    return isSuccess;
}

//新建研报订阅
function addReport(name) {
    if (cndType == 103) {
        getTreeVal();
    }


    var $form = $("#ReportOrderForm");
    var isSuccess = true;
    var params = $form.serializeArray();
    params.push({name: 'name', value: name});
    $.ajax({
        url:addReportOrder.url(),
        type:"post",
        dataType:"json",
        async:false,
        data:params,
        success:function (data) {
            if (data.success) {
                $.qicTips({message:data.msg, level:1, target:"#btn_save", mleft:0, mtop:-60});
                var $li = $("<li id=ut_" + data.id + " class=''>" + name + "</li>");
                $li.prependTo($("#ut_ul_list"));
                addClickListenerToLiTag();//给右侧li标签添加click事件
                var obj = data.id + "";
                utMap[obj] = eval("(" + data.info + ")");

                var $li3 = $("#options_year1_2 > li[id = "+data.id+"]");//删除原来的
                $li3.remove();
                var $li2;
                if(name.length>4){//当长度大于4的时候截取，并弹出tooltip
                    $li2 = $("<li id="+ data.id +"><a style='display:block;width: 100%' href='/reportct/reportinfo?sp.id=" + data.id + "' title='"+name+"' class='tooltip'>"+name.substring(0,4)+"…"+"</a></li>");
                }else{
                    $li2 = $("<li id="+ data.id +"><a style='display:block;width: 100%' href='/reportct/reportinfo?sp.id=" + data.id + "' class='tooltip'>"+name+ "</a></li>");
                }
                $li2.prependTo($("#options_year1_2"));
                //isSuccess = false;
                registToolTip()//注册tooltip()
                reset();
            }
            else {
                $.qicTips({message:data.msg, level:2, target:"#btn_save", mleft:0, mtop:-60});
                isSuccess = false;
            }
        },
        error:function () {
            alert("出错了...");
            isSuccess = false;
        }

    });
    return isSuccess;
}

//编辑研报订阅
function editReport(name, cndId) {
    if (cndType == 103) {
        getTreeVal();
    }
    var isSuccess = true;
    var $form = $("#ReportOrderForm");
    var params = $form.serializeArray();
    params.push({name: 'name', value: name});
    params.push({name: 'id', value: cndId});
    $.ajax({
        url:editReportOrder.url(),
        type:"post",
        dataType:"json",
        async:false,
        data:params,
        success:function (data) {
            if (data.success) {
                $.qicTips({message:data.msg, level:1, target:"#btn_save", mleft:0, mtop:-60});
                addClickListenerToLiTag();
                var obj = cndId + "";
                utMap[obj] = eval("(" + data.info + ")");
                //$("#ut_" + cndId).html(name + "<span class='for_remove_item'></span>");
                //$("#" + cndId).find("a").text(name);
                //reset(); //保存后不用清空树。
                //isSuccess = false;
            }
            else {
                $.qicTips({message:data.msg, level:2, target:"#btn_save", mleft:0, mtop:-60});
                isSuccess = false;
            }
        },
        error:function () {
            alert("出错了...");
            isSuccess = false;
        }

    });
    return isSuccess;
}


//搜索条件列表项双击事件处理
function utListItemdblclick(e) {
    if($("#ut_ul_list > li >input").size()>0){
        return;
    }
    var li = $(this);
    if (!li.hasClass("cndSelected")) { //如果当前不选中的话, 直接跳过
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
    if (inputtext.length == 0) {
        $.qicTips({message:"名称不能为空", level:2, target:'#ut_ul_list', mleft:0, mtop:-60});
        input.focus();
        return;
    } else if (inputtext.length > 20) {
        $.qicTips({message:"名称过长", level:2, target:'#ut_ul_list', mleft:0, mtop:-60});
        input.focus();
        return;
    }
    if(checked(inputtext)){
        input.focus();
        return;
    }
    var sameName = false;
    $("#ut_ul_list > li").each(function (i) {
        var $eachThis = $(this);
        if ($eachThis == li) { //当前节点

        }else if((inputtext+'<span class="for_remove_item"></span>').trim() == ($eachThis.html()).trim()){ //说明重名
            sameName = true;
            return false;
        }

        return true;
    });
    if (sameName) {
        $.qicTips({message:"名称已存在", level:2, target:'#ut_ul_list', mleft:0, mtop:-60});
        li.addClass("cndSelected");
//        li.html(inputtext);
//        li.trigger("dblclick", {"type":typeVal});
        return;
    }

    if (cndId == -999 || (inputtext.length > 0 && text != inputtext)) { //新加节点 或者 有修改
        $.ajax({
            url:renameCondRoute.url({id:cndId, name:inputtext}),
            type:renameCondRoute.method,
            data:{"type":typeVal},
            dataType:"json",
            success:function (data) {
                var liNode = input.parent();
                //暂时没有找到双击的时候文本框里尾部出现"..."的情况，就这样先处理下
                inputtext = replaceErrorCharacter(inputtext)//global-f10中的方法
                if (data.op) {
                    if(inputtext.length>7){
                        liNode.html(inputtext.substring(0,7)+'…<span class="for_remove_item"></span>');
                    }else{
                        liNode.html(inputtext.trim()+'<span class="for_remove_item"></span>');
                    }
                    li.attr("mytitle",inputtext);
                    if(inputtext.length>7){
                        li.attr("title",inputtext);
                    }else{
                        li.attr("title",null);
                    }
                    delSubscribe();
                    var flag;
                    $.each($("#options_year1_2 li"),function(){
                        if($(this).attr("id") == data.id) {
                            flag = true;
                        } else {
                            flag = false;
                        }
                    })
                    if(flag){
                        $("#"+cndId).children().text(inputtext);
                    } else{

                    }
                    var $li3 = $("#options_year1_2 > li[id = "+cndId+"]");//删除原来的
                    $li3.remove();
                    var $li2;
                    if(inputtext.length>4){//当长度大于4的时候截取，并弹出tooltip
                        $li2 = $("<li id="+ data.id +"><a style='display:block;width: 100%' href='/reportct/reportinfo?sp.id="+ data.id +"' title='"+inputtext+"' class='tooltip'>" +inputtext.substring(0,4)+"…"+"</a></li>");
                    }else{
                        $li2 = $("<li id="+ data.id +"><a style='display:block;width: 100%' href='/reportct/reportinfo?sp.id="+ data.id +"' class='tooltip'>"+inputtext+"</a></li>");
                    }
                    $li2.prependTo($("#options_year1_2"));
                    registToolTip()//注册tooltip()

                    if (data.id) { //新加节点
                        liNode.attr("id", "ut_" + data.id);
                        utMap[data.id] = data.utInfo;
                        //新加的,则要清空右边的选项
                        // resetBtn_onclick();
                    }
                    $.qicTips({message:data.msg, level:1, target:'#ut_ul_list', mleft:0, mtop:-60});
                    //$(".ser_keep").toggleClass("ser_keep");

                    var event = $(".ser_keep");
                    if ($("#ut_ul_list li").size() == 1) {
                        event.removeClass("dark_keep");
                        event.addClass("ser_keep");
                    }

                } else {
                    if (data.sameName) { //是否重名错误
                        liNode.html(inputtext);
                        liNode.trigger("dblclick", {"type":typeVal});
                    } else {
                        liNode.html(text);
                    }
                    $.qicTips({message:data.msg, level:2, target:'#ut_ul_list', mleft:0, mtop:-60});
                }
                liNode.dblclick({"type":typeVal}, utListItemdblclick);
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



