/* 
 *func: NT市场资讯
 *author:jinwei
 *email: awayInner@gmail.com
 *date: 2012 january 14
 */

;(function ($) {
    jQuery.extend({
        /**
         * obj的属性有的: message 信息(文字).
         * level 级别(1. 正确的 2.错误的, 3. 信息)
         * target  jquery的选择器(字符).
         * time 时间. 默认2000.
         * mleft  提示框离你的target对象左边多少距离
         * mtop   提示框离你的target对象top多少距离
         */
        qicTips:function (options) {//操作提示方法
            var defaults = {
                message:"操作成功",
                level:1,
                target:'body',
                time:2000,
                mleft:-60,
                mtop: -30
            };

            var options = $.extend(defaults, options); //扩展属性

            var imgSrc1 = _gF10.ctx + "/public/images/operate_sprites_1.png";
            var imgSrc2 = _gF10.ctx + "/public/images/operate_sprites_2.png";
            var imgSrc3 = _gF10.ctx + "/public/images/operate_sprites_3.png";
            if ($(".operate_tips").length == 0) {
                var html = '<div class="operate_tips" style="position:absolute;left:500px; z-index:9000;top:100px;  color:#65a5df; height:27px;"><div class="operate_tips_1" style="height:29px;line-height:29px; float:left; position:relative; z-index:50; padding-bottom:1px;background:url('
                    + imgSrc1
                    + ') no-repeat 0px -0px; width:71px;">&nbsp;</div><div class="operate_tips_2" style="height:29px;line-height:29px; float:left; position:relative; z-index:50; padding-bottom:1px;background:url('
                    + imgSrc2
                    + ') repeat-x 0px -0px; z-index:49;position:relative; left:-46px;text-align:center; min-width:42px;max-width:300px;overflow:hidden;white-space:nowrap;">操作成功</div><div class="operate_tips_3" style="height:29px;line-height:29px; float:left; position:relative; z-index:50; padding-bottom:1px;background:url('
                    + imgSrc3
                    + ') no-repeat 0px -0px;position:relative; left:-46px;width:6px;">&nbsp;</div></div>';

                $("body").append(html);
            }

            var operTips = $(".operate_tips");
            //取得包含框的子元素(注：children()只考虑子元素而不考虑所有后代元素
            var operTipsChildren = operTips.children("div");
            var operTipsMidTxt = $(".operate_tips_2");
            var time = options.time; //停留时间
            var clientX = 0;
            var clientY = 0;
            var $body = $("body");
            var $target = $(options.target);
            if (options.target == 'body') {
                clientX = $body.clientWidth / 2;
            } else {
                clientX = $target.offset().left + (options.mleft);
            }
            if (options.target == 'body') {
                clientY = $body[0].clientHeight / 2;
            } else {
                clientY = $target.offset().top + (options.mtop);
            }

            switch (options.level) {
                case 1:
                    operTipsChildren.css({"background-position":'0px -0px'});
                    operTipsMidTxt.html(options.message);
                    break;
                case 2:
                    operTipsChildren.css({"background-position":'0px -31px', "color":'#db1f10'});
                    operTipsMidTxt.html(options.message);
                    break;
                case 3:
                    operTipsChildren.css({"background-position":'0px -61px'});
                    operTipsMidTxt.html(options.message);
                    break;
                default:
                    break
            }

            operTips.css({
                "position":"absolute",
                "top":clientY,
                "left":clientX,
                "z-index":90000,
                'display':'none',
                "background-position":'0px 0px',
                'color':'#65a5df'
            });
            operTips.stop(false, true).show('fast');
            window.setTimeout(function () {
                operTips.stop(false, true).hide('fast');
            }, time);
        }
    });
})(jQuery);

/*1.0
 *左边子菜单显示隐藏
 */


(function(){
	//折叠
	window.addEventListener("load", function(){
		function selector(name){
			return document.querySelectorAll(name);	
		};
		
		var folder = $("div.folder");
			siderNews = $(".sider_news"),
			barWidth = siderNews.width(),
			mainWrap = $(".main_wrap");
			wrapLeft = mainWrap.css("margin-left"),
			folderSc = $(".folder"),
			sLeft = folderSc.css("left");
			
		//点击
		folder.click(function(){
			var _thisClass = this.children[0].className,
				slideNews = selector(".sider_news")[0],
				main_wrap = selector(".main_wrap")[0],
				folderShow = selector(".folder_tips_hide")[0],
				hideTips = 'folder_tips_hide',
				showTips = 'folder_tips_show';
			
			
			if(_thisClass == hideTips){
				mainWrap.animate({"margin-left": 30});
				siderNews.animate({"width": 0, "overflow": "hidden"});
				this.children[0].className = showTips;
				siderNews.animate({"overflow": "hidden"});
				folderSc.animate({"left": 0});
                $(".bl_news_hide").hide();
			}else{
				mainWrap.animate({"margin-left": wrapLeft});
				siderNews.stop(false, true).animate({"width": barWidth});
				this.children[0].className = hideTips;
				folderSc.stop(false, true).animate({"left": sLeft});
                $(".bl_news_hide").show();
			}
			
		});	
	}, false)
})();



//2.0市场资讯
var marketNews = {
		//2.1客户端高度实时变化
		contentResize: function(selector, redHigh){
			//内容高度
			var clientHeight = document.documentElement.clientHeight,
				detail = document.querySelector(selector);
				detail.style.height =  clientHeight - redHigh + "px"; 
			
			window.onresize = function(){
				var clientHeight = document.documentElement.clientHeight;
				detail.style.height =  clientHeight - redHigh + "px"; 
			}
	
		},
		
		//2.2我的订阅、高级搜索
		mySubscribe: function(){
			$("body").click(function(e){
					var target = e.target;
					var className = target.className,
						aClassName;
					//提取class	
					if( /(sel_subscribe)/.test(className) ){
						aClassName = RegExp['$1']
					}
					
					if(aClassName == 'sel_subscribe'){
						$(target).siblings(".sel_subscribe_opt").slideToggle(100);	
					}else{
						$(".sel_subscribe_opt").slideUp(100);
					}
			});
			
		},
		
		//2.3动态加载样式
		loadStyleString: function(){
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
			
		},
		
		//2.4子菜单
		subMenu: function(){
			$(".for_folder").toggle(function(){
				$(this).find(".sp_tip").toggleClass("sp_tip_2");
				$(this).siblings(".submenu_mn").slideToggle()

			}, function(){
				$(this).find(".sp_tip").toggleClass("sp_tip_2");
				$(this).siblings(".submenu_mn").slideToggle();
			});
			
		},

		//2.5模仿select
		downBox: function(clickId, downShow, hiddenValueId, hiddenNameId){
		
			//function downBox(clickId, downShow, hiddenValueId, hiddenNameId){
				$('body').click(function(event){
					var cID = '#' + clickId; //给ID加一个#,以便JQ选中
					var dList = downShow + ' '+'li'; //点击选项列表
					
					if(event.target.id == clickId){
						//控制再一次点击 当前显示项关闭
						$(downShow).stop(false,true).slideUp("fast");
						$(""+downShow+":hidden").stop(false,true).slideDown("fast");
						
						  $(dList).click(function(){
							  $(cID).text(''); //清空点击ID的值
							  var txt = $(this).text();//获得当前点击列表项的值
							  
							  var hiddenValue = $(this).attr("data-value");

								  
							  // $("#reportDate").val(hiddenValue)
							  
							  if($(this).is(".open_selected")){
								  $(cID).html(txt).css("color","#7D7D7D"); //设置选择的值
								  
								  $("#" + hiddenValueId).val(hiddenValue);
								  $("#" + hiddenNameId).val(txt);
								  
							  }else{
								  $(cID).html(txt).css("color","#000"); //设置选择的值
								  
								   $("#" + hiddenValueId).val(hiddenValue);
								  $("#" + hiddenNameId).val(txt);
							  }
						  });
							  
					 }else{
						 //downShow.slideUp();
						 $(downShow).slideUp("fast").hide(); //隐藏下拉框
					 }
			
				});
				
		 },
		 
		 //2.6 jQ tab切换
		 tabClick: function(){
			$(".tab_li li").click(function(){
				var index = $(this).index();
				$(".tab_li").children("li").removeClass().eq(index).addClass('display');
				$(".sub_content").children("li").removeClass().eq(index).addClass('display')
			});
			 
		 }

	
};

function  autoSection(source){
    var result = "";
    if(!source || source == ""){
        return result;
    }else{
        var paragraphs = source.split(/\s\s+/);
        if(paragraphs.length>1){
            for(var i = 0;i<paragraphs.length ;i++){
                paragraphs[i] = "<br>&nbsp;&nbsp;&nbsp;&nbsp;" + paragraphs[i] ;
            }
        }
        return paragraphs.join("");
        //return paragraphs = "&nbsp;&nbsp;&nbsp;&nbsp;" + paragraphs;

    }
}

function jumpToQuote(code) { //跳转到K线图
    if (code.substr(0, 1) == "6" || code.substr(0, 1) == "9") {
        qicScriptContext.jumpToQuoteChart(1, code);
    }else if(code.substr(0, 1) == "2"){
        qicScriptContext.jumpToQuoteChart(2, code);
    }
    else {
        qicScriptContext.jumpToQuoteChart(2, code);
    }
}



