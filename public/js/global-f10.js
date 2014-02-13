/*
 *func: NT F10中间页面js
 *author:jinwei
 *email: awayInner@gmail.com
 *date: 2012-08-17
 */
_gIsProd =true;
if(_gIsProd){
    //屏蔽右键
   document.oncontextmenu=function(e){return false;}
    document.onselectstart=function(){return false;}
    //屏蔽F12 F11键
    document.onkeydown =function(e){

        if(e.keyCode == 123 || e.keyCode == 122 || e.keyCode == 8  || e.keyCode==78 || e.keyCode==17 ){
            return false;
        }
    }
}

//屏蔽ctrl和单击事件同时存在的情况
;(function(){
    var bTrue1 = false,
        bTrue2 = false;
    $(document).live({
        "click": function(e){
            bTrue1 = true;
            if(bTrue1 == true && bTrue2 == true){
                return false;
            }
        },
        "keydown": function(e){
            if(e.keyCode == 17|| e.keyCode ==16){
                bTrue2 = true;
            }
        },
        "keyup": function(e){
            if(e.keyCode == 17||e.keyCode ==16){
                bTrue2 = false;
            }
        }
    });

})();



$(".stock_code, .ut_li_text").live("focus", function(){
    document.onkeydown =function(e){
        if(e.keyCode == 8 || e.keyCode==17){
            return true;
        }
    }
});

$(".stock_code, .ut_li_text").live("blur", function(){
    document.onkeydown =function(e){
        if( e.keyCode == 8 || e.keyCode==17){
            return false;
        }
    }
});

$("#ut_-999").live("focus", function(){
    document.onkeydown =function(e){
        if(e.keyCode == 8){
            return true;
        }
    }

});
$("#ut_-999").live("blur", function(){
    document.onkeydown =function(e){
        if( e.keyCode == 8){
            return false;
        }
    }
});


//监控 字母、数字和ESC键 屏蔽中文输入
if (typeof _openKeyBrowser !== 'undefined' &&  _openKeyBrowser) {
    document.documentElement.addEventListener("keydown", function (e) {
        var keyCode = e.keyCode;
        if (keyCode == 27 || keyCode >= 48 && keyCode <= 57 || keyCode >= 65 && keyCode <= 90 || keyCode >= 96 && keyCode <= 105) {
            if(typeof(qicScriptContext)!= 'undefined') qicScriptContext.onBrowerKeyDown(keyCode);
        }
    }, false);
}

/*1.0
 *左边子菜单显示隐藏
 */
//document.oncontextmenu=function(e){return false;}

function _id(id){
    return document.getElementById(id);
}
(function(){
    var folder = _id("folder");//点击项
    var subMenu = _id("subMenu");//左边导航菜单
    var mainWrap = _id("mainWrap");//优先加载的id

    if(folder && typeof folder !== 'undefined'){
        folder.onclick = function(){
            var _thisClass = this.children[0].className,
                hideTips = 'folder_tips_hide',
                showTips = 'folder_tips_show'
            if(_thisClass == hideTips){
                this.children[0].className = showTips;
                folder.children[0].cssText = 'background:url(images/folder_tips_222show.jpg) no-repeat -9px 0;';
                subMenu.getElementsByTagName("ul")[0].style.display='none'
                subMenu.style.cssText = 'width:8px;';
                this.style.cssText = 'left:0px;'
                mainWrap.style.marginLeft = '25px';
                folder.title = '展开菜单';
            }else{
                this.children[0].className = hideTips;
                subMenu.getElementsByTagName("ul")[0].style.display ='block'
                subMenu.style.cssText = 'width:122px;';
                this.style.cssText = 'left:124px;'
                mainWrap.style.marginLeft = '147px';
                folder.title = '折叠菜单';
            }
            //console.log(this.className);
        }
    }

})();


/*folder.onmouseover = function(){
	this.children[0].style.cssText = 'background:url(images/folder_hide_2.jpg)'
}
folder.onmouseout = function(){
	this.children[0].style.cssText = 'background:url(images/folder_hide.jpg)'
}*/


/*2.0
 *func: 通过类名取得元素引用
 */
//原生js取得class类名，直接传递名称即可
function getByClass(className, context, tagName){
	var context = context || document;
	//如果浏览器支持getElementsByClassName方法，直接返回
	if(context.getElementsByClassName){
		return document.getElementsByClassName(className);
	}

	var nodes = context.getElementsByTagName('*');
	var classArr = [];
	for(var i=0; i<nodes.length; i++){
		if(hasClass(nodes[i], className)){
			 //把符合的添加到数组中
			classArr.push(nodes[i]);
		}
	}
	return classArr;
}
//判断有没有传过来class类名
function hasClass(node, className){
	 //类名是空隔分开的
	var name = node.className.split(/\s/);
	for(var i=0; i<name.length; i++){
		if(name[i] == className){
			//alert(name[i])
			return true;
		}
	}
	return false;
}

/*3.0
 *参数tbl_common:表格类名，隔行换色背景。及鼠标滑过的背景色
 */
function tableBgFunc(tableClass, trColor){
	var tableClass = tableClass || "tbl_common",
		trColor = trColor || '#4b4b4b'
	var tblCommonTable = getByClass(tableClass);//得到class名的引用
	for(var j=0; j<tblCommonTable.length; j++){//隔行换色背景
		var tblCommon = tblCommonTable[j].getElementsByTagName("tr");
		for(var i=0; i<tblCommon.length; i++){
			if(i % 2 != 0){
				tblCommon[i].className = 'tbl_bg';
			}
			tblCommon[i].onmouseover = function(){
				this.style.backgroundColor = trColor;
			};
			tblCommon[i].onmouseout = function(){
				this.style.backgroundColor = '';
			};
		}
	}
}

/*3.1
 *参数tbl_common:表格类名，隔行换色背景。及鼠标滑过的背景色
 */
function tableBgFunc_2(tableClass, trColor){
	var tableClass = tableClass || "tbl_common",
		trColor = trColor || '#4b4b4b'
	var tblCommonTable = getByClass(tableClass);//得到class名的引用
	for(var j=0; j<tblCommonTable.length; j++){//隔行换色背景
		var tblCommon = tblCommonTable[j].getElementsByTagName("tr");
		for(var i=0; i<tblCommon.length; i++){

			tblCommon[i].onmouseover = function(){
				this.style.backgroundColor = trColor;
			};
			tblCommon[i].onmouseout = function(){
				this.style.backgroundColor = '';
			};
		}
	}
}

/*4.0
 *原生js tab切换
 *func: tabMenu("menuTab", "subCont", "display");
 *参数tabId：鼠标点击要切换的ul的id, subCont：要切换的内容ul的id, display:焦点class
 *一个页面多次调用
 *author: jinwei
 *email: awayInner@gmail.com
 *document.write("星期" + ['一','二','三','四','五','六','日'][new Date().getDay()]);
 */

function _id(id){
	return document.getElementById(id);
}

function tabMenu(tabId, contId, display){
    var callBackObj = arguments[3] || {}; //自己加的回调对象
	var subCont = _gid(contId); //内容父id
	var subContLi = subCont.children;
	//var subContLi = subCont.getElementsByTagName("li");
	var mentTab = _gid(tabId); //切换菜单父id
	var tabLi = mentTab.children;
	//var tabLi = mentTab.getElementsByTagName("li");
	var arr=[];



	//获取id的引用
	function _gid(id){
		return document.getElementById(id);
	}
	//遍历tab的 li
	for(var i=0; i<tabLi.length; i++){
		arr.push(tabLi[i])
		tabLi[i].onclick = function(){
			clearClass(tabLi); //清除菜单tab下的li class
			clearClass(subContLi); //清除内容下的li class
			this.className = display;

			//alert($("li").index(this)); //1.0 jQ 获取当前索引值
			//var curIndex = arr.index(this); //2.0 原生js获取当前索引值

			for(var j=0,len=arr.length; j<len; j++){
				if(arr[j] == this){
					curIndex = j;
					break;
				}
			}
			subContLi[curIndex].className = display;

            //用于处理回调方法. 回调的格式为 {"序号":函数}. 如 {"2":func2}, 则表示点击第3个tab时, 回调func2函数. 传为window作为this对象
            var callBackFun = callBackObj[curIndex + ""];
            if(callBackFun && jQuery.isFunction(callBackFun)){ //判断是否有回调对象
                callBackFun.call(window);
            }
		};

	}

	//清除所有同胞节点的class
	function clearClass(curenLi){
		for(var j=0; j<curenLi.length; j++){
			curenLi[j].className = '';
		}
	}

	 //为Array对象 添加新方法index() 专门获取当前点击索引值
/*		Array.prototype.index = function (vItem){ //获取当前索引值
		for(var i=0,l=this.length;l>i;i++){
			if(vItem == this[i]){
				return i;
				break;
			}
		}
		return -1;
	}*/

}

/*5.0
 *动态加载样式
 */
function loadStyleString(css){
	var style = document.createElement("style");
		style.type = "text/css";
	var string = document.createTextNode(css);
	try {
		style.appendChild(string);
	}catch(ex){
		style.styleSheet.cssText = css;
	}
	var head = document.getElementsByTagName("head")[0];
	head.appendChild(style);
}


/**
 * 来自: http://gwbasic.iteye.com/blog/161249
 * 格式化数字显示方式
 * 用法
 * formatNumber(12345.999,'#,##0.00');
 * formatNumber(12345.999,'#,##0.##');
 * formatNumber(123,'000000');
 * @param num
 * @param pattern
 */
function formatNumber(num,pattern){
  if(num === undefined || num == null){
    return "--";
  }

  var strarr = num?num.toString().split('.'):['0'];
  var fmtarr = pattern?pattern.split('.'):[''];
  var retstr='';

  // 整数部分
  var str = strarr[0];
  var fmt = fmtarr[0];
  var i = str.length-1;
  var comma = false;
  for(var f=fmt.length-1;f>=0;f--){
    switch(fmt.substr(f,1)){
      case '#':
        if(i>=0 ) retstr = str.substr(i--,1) + retstr;
        break;
      case '0':
        if(i>=0) retstr = str.substr(i--,1) + retstr;
        else retstr = '0' + retstr;
        break;
      case ',':
        comma = true;
        retstr=','+retstr;
        break;
    }
  }
  if(i>=0){
    if(comma){
      var l = str.length;
      for(;i>=0;i--){
        retstr = str.substr(i,1) + retstr;
        if(i>0 && ((l-i)%3)==0) retstr = ',' + retstr;
      }
    }
    else retstr = str.substr(0,i+1) + retstr;
  }

  retstr = retstr+'.';
  // 处理小数部分
  str=strarr.length>1?strarr[1]:'';
  fmt=fmtarr.length>1?fmtarr[1]:'';
  i=0;
  for(var f=0;f<fmt.length;f++){
    switch(fmt.substr(f,1)){
      case '#':
        if(i<str.length) retstr+=str.substr(i++,1);
        break;
      case '0':
        if(i<str.length) retstr+= str.substr(i++,1);
        else retstr+='0';
        break;
    }
  }
  return retstr.replace(/^,+/,'').replace(/\.$/,'');
}

//highChart图例点击处理程序. 图例只剩下最后一个时, 点击不能把节点隐藏
function highChartLegendItemClick() {
    var curItem = this;
    if (!curItem.visible) { //当前点是不显示的. 则表示由不显示到显示, 直接返回true
        return true;
    }
    var items = curItem.chart.legend.allItems
    var totalLen = items.length;
    var showItemLength = 0;
    $.each(items, function (i, item) {
        if (item.visible) {
            showItemLength++;
        }
    });
    if (showItemLength <= 1) { //只剩下最后一个显示时, 不处理, 返回false
        return false;
    } else {
        return true;
    }
}

//highChar pie图例点击, 这个跟别的有点不一样
function highChartPicLegendItemClick() {
    var curItem = this;
    if (!curItem.visible) { //当前点是不显示的. 则表示由不显示到显示, 直接返回true
        return true;
    }
    var items = curItem.series.chart.legend.allItems
    var totalLen = items.length;
    var showItemLength = 0;
    $.each(items, function (i, item) {
        if (item.visible) {
            showItemLength++;
        }
    });
    if (showItemLength <= 1) { //只剩下最后一个显示时, 不处理, 返回false
        return false;
    } else {
        return true;
    }
}

$(function(){
    $("table.hhmore").each(function(){
        var showRowCount = 7; //显示的行数

        var $this = $(this);
        var tableId = $this.attr("id");
        $("tbody > tr:gt(" + showRowCount + ")", $this).hide();

        var tdCount = $("thead > tr > th", $this).size();
        if($("tfoot", $this).size() == 0 && $("tbody > tr", $this).size() > showRowCount + 1){
            var $tipRow = $("<tfoot><tr><td class='noshow' colspan='" + tdCount + "'>更多内容</td></tr></tfoot>");
            $this.append($tipRow);
            //$tipRow.appendTo($this);
            $("tfoot > tr > td", $this).click(function(){
                var $td = $(this);
                if($td.hasClass('noshow')){ //展开
                    $("tbody > tr:gt(" + showRowCount + ")", $this).fadeIn(200);
                    $td.removeClass('noshow').addClass('showed').text("收起");
                }else{
                    $("tbody > tr:gt(" + showRowCount + ")", $this).fadeOut(200);
                    $td.removeClass('showed').addClass('noshow').text("更多内容");
                }
            });
        }
    });
});
function exportFile(url){
   // top.location.href = url;
        if(url.indexOf("?") == -1){
            url = url+"?_timestamp="+new Date().valueOf();
        }else{
            url = url+"&_timestamp="+new Date().valueOf();
        }
        var iframe = document.getElementById("uploadIframe");
        if (iframe) {
            document.body.removeChild(iframe);
        }
        var loadingDiv = document.getElementById("loading");
        if(loadingDiv != null) loadingDiv.style.display = ''; //容错处理
        var iframe = document.createElement("iframe");
        setTimeout(function(){
            document.getElementById("loading").style.display = 'none'
        },6000);
        if($.blockUI){//容错处理
            $.blockUI({
                message: "正在导出,请稍后...",
                // $.blockUI.defaults.css = {};
                css: {
                    padding:        0,
                    margin:         0,
                    width:          '15%',
                    top:            '50%',
                    left:           '50%',
                    textAlign:      'center',
                    color:          '#000',
                    border:         '1px solid #aaa',
                    backgroundColor:'#fff',
                    '-webkit-border-radius': '5px',
                    '-moz-border-radius': '5px',
                    //opacity: .9,
                    cursor:         'wait'
                },
                // styles for the overlay
                overlayCSS:  {
                    backgroundColor: '#000',
                    opacity:         .5,
                    cursor:          'wait',
                    zIndex:10000
                },
                timeout: 2000
            });
            //setTimeout($.unblockUI, 2000);
        }
        iframe.id = "uploadIframe";
        iframe.src = url;
        iframe.style.display = 'none'
        if (iframe.attachEvent){
            iframe.attachEvent("onload", function(){
                loadingDiv.style.display = 'none';
            });
        } else {
            iframe.onreadystatechange  = function(){
                loadingDiv.style.display = 'none';
            };
        }
        document.body.appendChild(iframe);

}

//用户输入特殊字符时自动删除
function f_Check(){
    var e=window.event;
    var code = e.keyCode;
    if(code==37 || code ==38 || code ==39  || code ==40  ){
        return
    }
    var inputValue = $("#inputId").val();
    var reg = /^([\u4E00-\uFA29]|[\uE7C7-\uE7F3]|[a-zA-Z0-9])*$/;
    var result="";
    var hasErrorCharacter = false;
    for(var i=0;i<inputValue.length;i++){
        if(reg.test(inputValue[i])){
            result+=inputValue[i];
        }else{
            hasErrorCharacter=true;
        }
    }
    if(hasErrorCharacter){//当检测到有非法字符的时候才替换
         $("#inputId").val(result);
    }
}

function cancelToolTip(){
    $( ".tooltip" ).tooltip( "destroy" );
}

function replaceErrorCharacter(srcValue){
    if(!srcValue.length>0){
        return srcValue;
    }
    var result="";
    var reg = /^([\u4E00-\uFA29]|[\uE7C7-\uE7F3]|[a-zA-Z0-9])*$/;
    for(var i=0;i<srcValue.length;i++){
        if(reg.test(srcValue[i])){
            result+=srcValue[i];
        }
    }
    return result;
}

$("body").on("click", ".more, .left_more, #moreList", function(){
    $("body").css("overflow", "hidden");
});

$("body").on("click", ".ui-corner-all", function(){
    $("body").css("overflow", "inherit");
});