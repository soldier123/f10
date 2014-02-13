/**
 * 资讯检索
 * User: panzhiwei
 * Date: 13-3-28
 * Time: 上午11:13
 */
$(function(){
    //搜索设置
    $("#options_year1_3 li").click(function (e) {
        $("#searchType").val(2);
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
                //"autoOpen":true,
                "width":646,
                'height':530,
                "resizable":false,
                title:"搜索设置",
                modal:true
            });
            clearCndSelected();
        } else {
            //后台处理的地方
        }
    });
    clearTreeContainer();

    //判断树的节点是否是叶子节点
    function treeNodeisLeaf(treenodedata){

    }

    var bulletinClassifyTree = $('#bulletinClassifyTree').ligerTree({//公司公告来源树(机构)
        data:bulletinTree,
        checkbox:true,
        parentIcon:null,
        childIcon:null,
        getChecked:true,
        attribute:['id', 'url', 'code']
    });

    var bulletinPlateTree = $('#bulletinPlateTree').ligerTree({ //公司公告板块树
        data:plateTree,
        checkbox:true,
        parentIcon:null,
        childIcon:null,
        getChecked:true,
        attribute:['id', 'url', 'plateId'],
        onBeforeExpand:onBeforeExpand
    });

    var newsSourceTree = $("#newsSourceTree").ligerTree({ //新闻来源树
        data:newsResourceTree,
        checkbox:true,
        parentIcon:null,
        childIcon:null,
        getChecked:true,
        attribute:['id', 'url', 'code']
    });

    var newsClassifyTree = $("#newsClassifyTree").ligerTree({ //新闻分类树
        data:newsClassify,
        checkbox:true,
        parentIcon:null,
        childIcon:null,
        getChecked:true,
        attribute:['id', 'url', 'code']
    });

    var reportClassifyTree = $('#reportClassifyTree').ligerTree({ //研究报告类型树
        data:reportClassifyConfig,
        checkbox:true,
        parentIcon:null,
        childIcon:null,
        getChecked:true,
        nodeWidth:120,
        attribute:['id', 'url', 'code']
    });


    var reportOrgTree = $('#reportOrgTree').ligerTree({ //研报来源树(机构)
        data:reportOrg,
        checkbox:true,
        parentIcon:null,
        childIcon:null,
        getChecked:true,
        attribute:['id', 'url', 'data-orgId']
    });

    var reportPlateTree = $("#reportPlateTree").ligerTree({ //研报的板块树
        data:plateTree2,
        checkbox:true,
        parentIcon:null,
        childIcon:null,
        getChecked:true,
        attribute:['id', 'url', 'plateId'],
        onBeforeExpand:onBeforeExpand
    });

    //清空树的容器节点内容
    function clearTreeContainer(){
        $("#newsSourceTree").html("");
        $("#newsClassifyTree").html("");
        $('#bulletinClassifyTree').html("");
        $("#bulletinPlateTree").html("");
        $('#reportClassifyTree').html("");
        $('#reportOrgTree').html("");
        $("#reportPlateTree").html("");

    }

    var expandedArr = [];

    //异步加载公告,研报的板块树的子节点
    function onBeforeExpand(node) {
        var treeObj = null;

        var $tree = this.tree; //得到tree容器的节点的 jquery对象
        var id = $tree.attr("id");
        if("bulletinPlateTree" == id){
            treeObj = bulletinPlateTree;
        }else if("reportPlateTree" == id){
            treeObj = reportPlateTree;
        }
        var $dom = $(node.target);
        var dom = treeObj.getNodeDom({"treedataindex":$dom.attr("treedataindex")});
        if (node.data.children && node.data.children.length == 0) {
            $.ajax({
                url:findPlateTreeByPlateTreeId.url(),
                type:"post",
                dataType:"json",
                data:{"id":node.data.plateId},
                success:function (data) {
                    if (data.treedata != "") {
                        var json = eval('(' + data.treeData + ')');
                        /*$.each(expandedArr,function(i,n){                     //判断该节点是否展开过,若展开过，直接返回
                         if(n == node.data.plateId) {
                         return false;
                         }
                         });*/
                        treeObj.append(node.target, json);
                        expandedArr.push(node.data.plateId);
                    }
                }
            })

        }
    }





    //保存查询条件的时间区间
    $("#saveTime").click(function () {
        var startTime = $("#startTime").text().replace(/^(\s|\u00A0)+/,'').replace(/(\s|\u00A0)+$/,'');
        var endTime = $("#endTime").text().replace(/^(\s|\u00A0)+/,'').replace(/(\s|\u00A0)+$/,'');
        if(Date.parse(startTime) > Date.parse(endTime)) {
            $.qicTips({message:'起始时间大于结束时间,请重新输入!', level:2, target:"#saveTime", mleft:-60, mtop:-30});
            return;
        }
        $("#reportDate").val(startTime);
        $("#endDate").val(endTime);
        $(".customdate").dialog("close");
        $("#commonSearchForm").submit();
    });

    $("#cancelTime").click(function () {
        $(".customdate").dialog("close");
    })


    //重置
    /*$("#darkReset").click(function () {
        reset();
    });*/

    //删除订阅条件
    /*$("#darkDel").click(function (event) {
        var $li = $("#ut_ul_list > li.cndSelected");
        if ($li.size() == 0) {
            //alert("请先选择订阅条件");
            $.qicTips({message:'请先选择搜索条件', level:2, target:event.target, mleft:-60, mtop:-30});
            return;
        }
        var cndId = $li.attr("id").substring("ut_".length);
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
                            var $li2 = $("#options_year1_3 > li[id = " + cndId + "]");
                            $li2.remove();
                            $.qicTips({message:data.msg, level:1, target:event.target, mleft:-60, mtop:-30});
                            $("#ut_ul_list > li:first").trigger("click");
                        }
                    }
                })
            }
        }).confirm();
    });*/

    //保存订阅条件
    $("#btn_save").click(function () {
        if($("#ut_ul_list > li >input").size()>0){
            $.qicTips({message:'请先保存订阅名称', level:2, target:'#ut_ul_list', mleft:0, mtop:-60});
            return;
        }
        if($("#ut_ul_list > li.cndSelected").size() == 0) {
            $.qicTips({message:'请选择搜索条件', level:2, target:"#btn_save", mleft:-60, mtop:-30});
            return;
        }
        var orderName;
        if ($("#ut_ul_list > li.cndSelected").size() > 0) {
            var $li = $("#ut_ul_list > li.cndSelected");
            var cndId = $li.attr("id").substring("ut_".length);
            orderName = $("#ut_ul_list > li.cndSelected").text();
        }

        var $li = $("#ut_ul_list > li.cndSelected");
        var cndId = $li.attr("id").substring("ut_".length);
        editReport(orderName, cndId);

        /*new QicDialog({
            title:" 保存",
            initPromptValue:!orderName ? "" : orderName,
            label:"请输入搜索名称",
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
    });

    //取消
    $(".btn_cancle").click(function () {
        reset();
    });

    //搜索
    $(".btn_search").click(function (event) {
        $("#searchType").val(3);
        addClickListenerToLiTag();
        var $li = $("#ut_ul_list > li.cndSelected");
        if (!$li.get(0)) {
            $("#advanceId2").val("");
            fillUserOrderForm2();
            $("#commonSearchForm").submit();
        } else {
            //fillUserOrderForm2();
            var cndId = $li.attr("id").substring("ut_".length);
            $("#advanceId2").val(cndId);
            $("#commonSearchForm").submit();
        }
    });


    //新增搜索条件
    $("#subscribe_add").live('click',function () {
        //说明有重名的,要处理重名
        var $input2 = $("#ut_ul_list > li > input");
        if ($input2.size() > 0) {
            //console.log("还有input啊...");
            new QicDialog({
                message:"请先保存当前搜索条件",
                title:"提示"
            }).warn();
            $input2.get(0).focus();
            $('#ut_ul_list').scrollTop($input2.get(0).offsetHeight); //滚动到当前可见的位置
            return;
        }
        if ($("#ut_ul_list > li").size() >= 20) {
            new QicDialog({
                message:"自定义搜索上限为20个",
                title:"提示"
            }).warn();
            return;
        }

        $("#ut_ul_list > li.cndSelected").removeClass("cndSelected");//先去掉选中的

        var newNameTmp = "新搜索条件";
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

    });


    addClickListenerToLiTag();
    delSubscribe();            //添加删除事件
    $("#ut_ul_list li").live("dblclick", {"type":cndType}, utListItemdblclick);

    // 获取树的选中节点,设置到form表单上


    var newsSourceCodeArr = []; //新闻来源代码
    var newsClassifyCodeArr = []; //新闻分类代码
    var bulletinClassifyCodeArr = []; //公告分类代码
    var bulletinPlateIdArr = []; //公告板块树id
    var reportOrgIdArr = []; //研报机构id
    var reportClassifyCodeArr = []; //研报分类代码
    var reportPlateIdArr = []; //研报板块树id
    var advanceType = []; //搜索类型. 1 新闻, 2公告, 3研报

    //将选中的节点的code值加入相应数组
    function getTreeValAndFillArray() {

        var newsSourceSelectVal = newsSourceTree.getChecked();
        var newsClassifySelectVal = newsClassifyTree.getChecked();
        var bulletinClassifySelectVal = bulletinClassifyTree.getChecked();
        var bulletinPlateSelectVal = bulletinPlateTree.getChecked();
        var reportOrgSelectVal = reportOrgTree.getChecked();
        var reportClassifySelectVal = reportClassifyTree.getChecked();
        var reportPlateSelectVal = reportPlateTree.getChecked();
         //这里要先初始化,再赋值
         newsSourceCodeArr = []; //新闻来源代码
         newsClassifyCodeArr = []; //新闻分类代码
         bulletinClassifyCodeArr = []; //公告分类代码
         bulletinPlateIdArr = []; //公告板块树id
         reportOrgIdArr = []; //研报机构id
         reportClassifyCodeArr = []; //研报分类代码
         reportPlateIdArr = []; //研报板块树id
         advanceType = []; //搜索类型. 1 新闻, 2公告, 3研报

        $.each(newsSourceSelectVal, function (i, n) {
            var data = n.data["code"];
            if(data != null) {
                newsSourceCodeArr.push(data);
            }
        });
        $.each(newsClassifySelectVal, function (i, n) {
            var data = n.data["code"];
            if(data != null) {
                newsClassifyCodeArr.push(data);
            }
        });
        $.each(bulletinClassifySelectVal, function (i, n) {
            var data = n.data["code"];
            if(data != null) {
                bulletinClassifyCodeArr.push(data);
            }
        });
        $.each(bulletinPlateSelectVal, function (i, n) {
            var data = n.data["plateId"];
            if(data != null) {
                bulletinPlateIdArr.push(data);
            }
        });
        $.each(reportOrgSelectVal, function (i, n) {
            var data = n.data['data-orgId'];
            if(data != null) {
                reportOrgIdArr.push(data);
            }
        });
        $.each(reportClassifySelectVal, function (i, n) {
            var data = n.data["code"];
            if(data != null) {
                reportClassifyCodeArr.push(data);
            }
        });
        $.each(reportPlateSelectVal, function (i, n) {
            var data = n.data["plateId"] ;
            if(data != null) {
                reportPlateIdArr.push(data);
            }
        });
        if(newsSourceCodeArr.length > 0 || newsClassifyCodeArr.length > 0 ) {
            advanceType.push(1);
        }
        if(bulletinClassifyCodeArr.length > 0 || bulletinPlateIdArr > 0 ) {
            advanceType.push(2);
        }
        if(reportClassifyCodeArr.length > 0 || reportOrgIdArr.length > 0 || reportPlateIdArr.length > 0 ) {
            advanceType.push(3);
        }

    }
    function fillUserOrderForm(){
        getTreeValAndFillArray();
        //附值到 form节点上
        $("#newsSource").val(newsSourceCodeArr.join(","));
        $("#newsClassify").val(newsClassifyCodeArr.join(","));
        $("#bulletinClassify").val(bulletinClassifyCodeArr.join(","));
        $("#bulletinPlate").val(bulletinPlateIdArr.join(","));
        $("#reportClassify").val(reportClassifyCodeArr.join(","));
        $("#reportOrg").val(reportOrgIdArr.join(","));
        $("#reportPlate").val(reportPlateIdArr.join(","));
        $("#advanceType").val(advanceType.join(","));


    }
    function fillUserOrderForm2(){
        getTreeValAndFillArray();
        $("#newsSource2").val(newsSourceCodeArr.join(","));
        $("#newsClassify2").val(newsClassifyCodeArr.join(","));
        $("#bulletinClassify2").val(bulletinClassifyCodeArr.join(","));
        $("#bulletinPlate2").val(bulletinPlateIdArr.join(","));
        $("#reportClassify2").val(reportClassifyCodeArr.join(","));
        $("#reportOrg2").val(reportOrgIdArr.join(","));
        $("#reportPlate2").val(reportPlateIdArr.join(","));
        $("#advanceType2").val(advanceType.join(","));
    }

    //给条件列表(左边显示的)增加点击处理函数
    function addClickListenerToLiTag() {
        $("#ut_ul_list > li").unbind("click");
        $("#ut_ul_list > li").click(function () {
            $("#ut_ul_list > li.cndSelected").removeClass("cndSelected");
            var $li = $(this);
            $li.addClass("cndSelected");
            var cndId = $li.attr("id").substring("ut_".length);
            var cndJson = utMap[cndId];
            if (cndType == 104) {
                clearTreeItem();
                var newsSourceArr = cndJson.newsSource;
                var newsClassifyArr = cndJson.newsClassify;
                var bulletinClassifyArr = cndJson.bulletinClassify;
                var bulletinPlateTreeArr = cndJson.bulletinPlateTree;
                var reportOrgArr = cndJson.reportOrg;
                var reportClassifyArr = cndJson.reportClassify;
                var reportPlateTreeArr = cndJson.reportPlateTree;
                if ($.isArray(newsSourceArr)) {
                    $.each(newsSourceArr, function (i, n) {
                        var $dom = $("#newsSourceTree li[code='" + n + "']");
                        newsSourceTree.selectNode($dom);
                    });
                }
                if ($.isArray(newsClassifyArr)) {
                    $.each(newsClassifyArr, function (i, n) {
                        var $dom = $("#newsClassifyTree li[code=" + n + "]");
                        newsClassifyTree.selectNode($dom);
                    });
                }
                if ($.isArray(bulletinClassifyArr)) {
                    $.each(bulletinClassifyArr, function (i, n) {
                        var $dom = $("#bulletinClassifyTree li[code=" + n + "]");
                        bulletinClassifyTree.selectNode($dom);
                    });
                }
                if ($.isArray(bulletinPlateTreeArr)) {
                    $.each(bulletinPlateTreeArr, function (i, n) {
                        var $dom = $("#bulletinPlateTree li[plateId=" + n + "]");
                        bulletinPlateTree.selectNode($dom);
                        var domLevel = 1 + parseInt($dom.attr("outlinelevel"));
                        for (var i = domLevel; i < domLevel + 6; i++) {
                            $dom.find("li[outlinelevel=" + i + "]").each(function (i) {
                                bulletinPlateTree.selectNode($(this));
                            });
                        }
                    });
                }
                if ($.isArray(reportOrgArr)) {
                    $.each(reportOrgArr, function (i, n) {
                        if (("" + n ).length > 0) { //n要有值
                            var $dom = $("#reportOrgTree li[data-orgId='" + n + "']");
                            reportOrgTree.selectNode($dom);
                        }
                    });
                }
                if ($.isArray(reportClassifyArr)) {
                    $.each(reportClassifyArr, function (i, n) {
                        if (("" + n ).length > 0) {
                            var $dom = $("#reportClassifyTree li[code='" + n + "']");
                            reportClassifyTree.selectNode($dom);
                        }
                    });
                }
                if ($.isArray(reportPlateTreeArr)) {
                    $.each(reportPlateTreeArr, function (i, n) {
                        if (("" + n ).length > 0) {
                            var $dom = $("#reportPlateTree li[plateId='" + n + "']");
                            reportPlateTree.selectNode($dom);
                            var domLevel = 1 + parseInt($dom.attr("outlinelevel"));
                            for (var i = domLevel; i < domLevel + 6; i++) {
                                $dom.find("li[outlinelevel=" + i + "]").each(function (i) {
                                    reportPlateTree.selectNode($(this));
                                });
                            }
                        }
                    });
                }

            }
        });
    }

    //清空树节点
    function clearTreeItem() {
        var paramFun = function (data) {
            return false;
        };
        newsSourceTree.selectNode(paramFun);
        newsClassifyTree.selectNode(paramFun);
        bulletinClassifyTree.selectNode(paramFun);
        bulletinPlateTree.selectNode(paramFun);
        reportOrgTree.selectNode(paramFun);
        reportClassifyTree.selectNode(paramFun);
        reportPlateTree.selectNode(paramFun);
        $("div.l-checkbox-incomplete").removeClass("l-checkbox-incomplete");
    }

    //重置
    function reset() {
        clearTreeItem();
        //清掉左边选中的条件列表项
//        if ($("#ut_ul_list > li.cndSelected").size() > 0) {
//            $("#ut_ul_list > li.cndSelected").removeClass("cndSelected");
//        }
    }

    //清掉左边选中的条件列表项
    function clearCndSelected() {
        if ($("#ut_ul_list > li.cndSelected").size() > 0) {
            $("#ut_ul_list > li.cndSelected").removeClass("cndSelected");
        }
    }

    //保存订阅
    function saveReport(name) {
        if ($("#ut_ul_list > li").size() >= 20) {
            new QicDialog({
                message:"自定义订阅上限为20个",
                title:"提示"
            }).warn();
            return false;
        }
        // var name = dialog.getPromptValue();
        var isSuccess = false;
        if (!name || name == "") {
            $.qicTips({message:"输入名称为空！", level:2, target:"#promptId", mleft:0, mtop:-60});
            return false;
        }

        if ($("#ut_ul_list > li.cndSelected").size() == 0) { //增加一个订阅条件
            $.ajax({
                url:checkOrderName.url(),
                type:"get",
                dataType:"json",
                async:false,
                data:{"name":name},
                success:function (data) {
                    if (data.success) {
                        isSuccess = addReport(name);
                    } else {
                        $.qicTips({message:"您输入的名称已存在,请重新输入!", level:2, target:"#qic_dialog_confirm_btn", mleft:-60, mtop:-30});
                        isSuccess = false;
                    }
                },
                error:function () {
                    alert("出错了...");
                    isSuccess = false;
                }

            });
        } else { //编辑一个订阅条件
            var $li = $("#ut_ul_list > li.cndSelected");
            var cndId = $li.attr("id").substring("ut_".length);
            if (name == utName[cndId]) {       //判断名字是否更改，如果没改，就不用验证是否重名
                isSuccess = editReport(name, cndId);
            } else {
                $.ajax({
                    url:checkOrderName.url(),
                    type:"get",
                    dataType:"json",
                    async:false,
                    data:{"name":name},
                    success:function (data) {
                        if (data.success) {
                            isSuccess = editReport(name, cndId);
                        } else {
                            $.qicTips({message:"您输入的名称已存在,请重新输入!", level:2, target:"#qic_dialog_confirm_btn", mleft:-60, mtop:-30});
                            isSuccess = false;
                        }
                    },
                    error:function () {
                        alert("出错了...");
                        isSuccess = false;
                    }
                });
            }
        }

        return isSuccess;
    }

    //新建资讯订阅
    function addReport(name) {
        if (cndType == 104) {
            fillUserOrderForm();
        }

        var $form = $("#messageIndexForm");
        var params = $form.serializeArray();
        params.push({name: 'name', value: name});
        var isSuccess = true;
        $.ajax({
            url:addReportOrder.url(),
            type:"post",
            dataType:"json",
            async:false,
            data:params,
            success:function (data) {
                if (data.success) {
                    $.qicTips({message:data.msg, level:1, target:"#qic_dialog_confirm_btn", mleft:-60, mtop:-30});
                    var $li = $("<li id=ut_" + data.id + " class=''>" + name + "</li>");
                    $li.prependTo($("#ut_ul_list"));
                    addClickListenerToLiTag();//给左侧li标签添加click事件
                    var obj = data.id + "";
                    utMap[obj] = eval("(" + data.info + ")");
                    var $li2 = $("<li id=" + data.id + "><a style='display:block;width: 100%' href='/MessageIndexCt/AdvanceInfoSearch?advanceId=" + data.id + "'>" + name + "</a></li>");
                    $("#options_year1_3 li").eq(0).after($li2);
                    reset();
                } else {
                    $.qicTips({message:data.msg, level:2, target:"#qic_dialog_confirm_btn", mleft:-60, mtop:-30});
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

    //编辑资讯订阅
    function editReport(name, cndId) {
        if (cndType == 104) {
            fillUserOrderForm();
        }
        var isSuccess = true;
        var $form = $("#messageIndexForm");
        var params = $form.serializeArray();
        params.push({name: 'id', value: cndId});
        params.push({name: 'name', value: name});
        $.ajax({
            url:editReportOrder.url(),
            type:"post",
            dataType:"json",
            async:false,
            data:params,
            success:function (data) {
                if (data.success) {
                    $.qicTips({message:data.msg, level:1, target:"#btn_save", mleft:-60, mtop:-30});
                    addClickListenerToLiTag();
                    var obj = cndId + "";
                    utMap[obj] = eval("(" + data.info + ")");
                   // $("#ut_" + cndId).html(name+"<span class='for_remove_item'></span>");
                   //$("#" + cndId).find("a").text(name);
                    <!--遍历高级搜索列表 避免多次点击”保存按钮“时重复添加订阅名称 bug@liuhj start-->
                    var flag = false;
                    $("#options_year1_3 li").each(function(){
                        var aname = $(this).find("a").text();
                        if(aname =name){
                            flag = true;
                            return;
                        }
                    })
                    if(!flag){
                        var $li2 = $("<li id="+ cndId +"><a style='display:block;width: 100%' href='/MessageIndexCt/messageIndex?cs.advanceId=" + cndId + "&cs.searchType=2'>" + name + "</a></li>");
                    }
                    <!--遍历高级搜索列表 避免多次点击”保存按钮“时重复添加订阅名称 bug@liuhj end-->
                    //$li2.prependTo($("#options_year1_3"));
                    $("#options_year1_3 li").eq(0).after($li2);
                   //reset();
                    //isSuccess = false;
                }
                else {
                    $.qicTips({message:data.msg, level:2, target:"#btn_save", mleft:-60, mtop:-30});
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
       // var text = li.text();
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


    //修改订阅名称
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

            } else if (inputtext == $eachThis.text()) { //说明重名
                sameName = true;
                return false;
            }

            return true;
        });
        if (sameName) {
            $.qicTips({message:"名称已存在", level:2, target:'#ut_ul_list', mleft:0, mtop:-60});
            li.addClass("cndSelected");
//            li.html(inputtext);
//            li.trigger("dblclick", {"type":typeVal});
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
                        var flag = false;
                        //下面注释掉的代码的作者,你大爷的，写个注释会死啊！
                       /* $.each($("#options_year1_2 li"),function(){
                            if($(this).attr("id") == data.id) {
                                flag = true;
                            } else {
                                flag = false;
                            }

                        })
                         if(flag){
                         $("#"+cndId).children().text(inputtext);
                         }*/
                            var $li3 = $("#options_year1_3 > li[id = "+cndId+"]");//删除原来的
                            $li3.remove();
                            var $li2;
                            if(inputtext.length>4){//当长度大于4的时候截取，并弹出tooltip
                                $li2 = $("<li id="+data.id +"><a style='display:block;width: 100%' href='/MessageIndexCt/messageIndex?cs.advanceId="+data.id + "&cs.searchType=2' title='"+inputtext+"' class='tooltip'>"+inputtext.substring(0,4)+"…"+"</a></li>");
                            }else{
                                $li2 = $("<li id="+data.id+"><a style='display:block;width: 100%' href='/MessageIndexCt/messageIndex?cs.advanceId="+data.id + "&cs.searchType=2' class='tooltip'>"+inputtext+ "</a></li>");
                            }
                            $("#options_year1_3 li").eq(0).after($li2);
                            registToolTip()//注册tooltip()
                            reset();

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
            delSubscribe();
            tdNode.dblclick({"type":typeVal}, utListItemdblclick);
        }
        li.addClass("cndSelected");

    }

}); //end $(function())



function delSubscribe(){
    $(".for_remove_item").unbind("click");
    $(".for_remove_item").bind('click',function () {
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
                            var $li2 = $("#options_year1_3 > li[id = " + cndId + "]");
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

