<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>突击单</title>
	<meta name="description" content="完成突击单">
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
	<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<%@ include file="/WEB-INF/views/modules/sd/crush/tpl/crushServicePointList.html" %>
	<script type="text/javascript">
        var this_index = top.layer.index;
        <%String layerIndex = request.getParameter("layerIndex");%>
        var layerIndex = '<%=layerIndex==null?"":layerIndex %>';
        if(layerIndex!=null && layerIndex!=""){
            this_index = layerIndex;
		}
        var parentLayerIndex = parent.layer.getFrameIndex(window.name);
		$(document).ready(function() {
            $('a[data-toggle=tooltip]').darkTooltip();
            $("#inputForm").validate({
                submitHandler: function(form){
                    var $btnSubmit = $("#btnSubmit");
                    if ($btnSubmit.prop("disabled") == true) {
                        event.preventDefault();
                        return false;
                    }
                    $btnSubmit.prop("disabled", true);
                    layer.confirm(
                        '确定要取消突击，取消后该工单将不再开发网点！'
                        ,{icon: 3, title:'系统确认',zIndex:19891015,success: function(layro, index) {
                                $(document).on('keydown', layro, function(e) {
                                    if (e.keyCode == 13) {
                                        layro.find('a.layui-layer-btn0').trigger('click');
                                        layer.close(index);//关闭本身
                                    }else if(e.keyCode == 27){
                                        $("#btnSubmit").removeAttr('disabled');
                                        layer.close(index);//关闭本身
                                    }
                                })
                            }
                        }
                        ,function(index) {
                            layer.close(index);//关闭本身
                            // do something
                            var loadingIndex = top.layer.msg('正在提交，请稍等...', {
                                icon: 16,
                                time: 0,//不定时关闭
                                shade: 0.3
                            });
                            var action = $("#action").val();
                            $.ajax({
                                type: "POST",
                                url: "${ctx}/sd/order/crush/saveClose?"+ (new Date()).getTime(),
                                data:$(form).serialize(),
                                success: function (data) {
                                    top.layer.close(loadingIndex);
                                    $btnSubmit.removeAttr('disabled');
                                    if(ajaxLogout(data)){
                                        return false;
                                    }
                                    if(data && data.success == true) {
                                        if(action=='0') {
                                            top.layer.close(this_index);
                                            layerMsg('提交成功');

                                            var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                                            if (iframe != undefined) {
                                                iframe.repage();
                                            }
                                        }else{
                                            return false;
                                        }
                                    }
                                    else if( data && data.message){
                                        layerError(data.message,"错误提示");
                                    }
                                    else {
                                        layerError("保存错误", "错误提示");
                                    }
                                    return false;
                                },
                                error: function (e) {
                                    top.layer.close(loadingIndex);
                                    $btnSubmit.removeAttr('disabled');
                                    ajaxLogout(e.responseText,null,"保存错误，请重试!");
                                }
                            });
                            return false;
                        }
                        ,function(index) {
                            $btnSubmit.removeAttr('disabled');
                        });
                    return false;
                },
                errorContainer: "#messageBox"
            });
		});
		//派单成功之后的回调方法
        function updateService(){
            top.layer.close(this_index);
            var iframe = getActiveTabIframe();//定义在jeesite.min.js中
            if (iframe != undefined) {
                iframe.repage();
            }
        }
        function tempSave(id,quarter){
            if ($("#btnTempSave").prop("disabled") == true) {
                event.preventDefault();
                return false;
            }
            $("#btnTempSave").prop("disabled", true);
            var loadingIndex = top.layer.msg('正在暂存，请稍等...', {
                icon: 16,
                time: 0,//不定时关闭
                shade: 0.3
            });
            $.ajax({
                type: "POST",
                url: "${ctx}/sd/order/crush/tempSave?"+ (new Date()).getTime(),
                data:$("#inputForm").serialize(),
                success: function (data) {
                    top.layer.close(loadingIndex);
                    $('#btnTempSave').removeAttr('disabled');
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if(data && data.success == true) {
                        layerMsg('暂存成功');
                        top.layer.close(this_index);
                    }
                    else if( data && data.message){
                        layerError(data.message,"错误提示");
                    }
                    else {
                        layerError("保存错误", "错误提示");
                    }
                    return false;
                },
                error: function (e) {
                    top.layer.close(loadingIndex);
                    $('#btnTempSave').removeAttr('disabled');
                    ajaxLogout(e.responseText,null,"保存错误，请重试!");
                }
            });
        }
		function closeme(){
			//top.layer.close(this_index);
            layer.confirm(
                '关闭后，填写的单据内容不保存，<br/>确定取消保存并关闭窗口吗？'
                ,{icon: 3, title:'系统确认',success: function(layro, index) {
                        $(document).on('keydown', layro, function(e) {
                            if (e.keyCode == 13) {
                                layro.find('a.layui-layer-btn0').trigger('click')
                            }else if(e.keyCode == 27){
                                layer.close(index);//关闭本身
                            }
                        })
                    }
                }
                ,function(index) {
                    layer.close(index);//关闭本身
                    top.layer.close(this_index);
                }
                ,function(index) {});
            return false;
		};

        function showHistoryPlanList(areaLevel){
            //areaLevel 0-省 1-市 2-区 3-街道
            var areaId = $("[id='area.id']").val();
            var areaName = $("[id='area.name']").val();
            var subAreaId = $("[id='subArea.id']").val();
            var subAreaName = $("[id='subArea.name']").val();
            var parameters = "areaLevel=" + areaLevel;
            if(areaLevel === 2){
                parameters = parameters +  "&area.id="+ areaId + "&area.name=" + areaName + "&area.parent.id=0";
            }else if(areaLevel === 3){
                parameters = parameters +  "&area.id="+ subAreaId + "&area.name=" + subAreaName + "&area.parent.id=" + areaId;// + "&area.parent.name=" + areaName;
            }
            var userAddress = $("#userAddress").val();
            parameters = parameters + "&userAddress=" + userAddress;
            var h = $(top.window).height();
            var w = $(top.window).width();
            top.layer.open({
				type: 2,
				id:'layer_planlist',
				zIndex: 19891015,
				title: (areaLevel === 2?'['+areaName+']':'') + '历史派单记录',
				content: "${ctx}/sd/order/crush/historyPlanList?" + parameters,
				shade: 0.3,
				shadeClose: true,
				// area:['1400px','800px'],
                area:[(w-40)+'px',(h-40)+'px'],
				maxmin: false,
				success: function(layero,index){
				},
				end:function(){
				}
			});
		}

		function rushPlan(id,no,quarter,crushPlanFlag) {
			var planIndex = top.layer.open({
				type: 2,
				id:'layer_plan',
				zIndex: 19891015,
				title:'派单 ['+no+']',
				content: "${ctx}/sd/order/crush/rushPlan?orderId="+ id +'&quarter=' + (quarter || '') + "&parentIndex=" + (parentLayerIndex || '') + "&crushPlanFlag="+(crushPlanFlag || 0),
				area: ['980px', '580px'],
				shade: 0.3,
				maxmin: true,
				success: function(layero,index){
				},
				end:function(){
				}
			})
        }

	</script>
	<script type="text/javascript">

        $(document).ready(function() {
            var screen = getOpenDialogWidthAndHeight();
            var height = screen.height-43-60;
            $("#leftDiv").css("height",height+"px");
            $("#rightDiv").css("height",height+"px");

            var items = ${fns:toJson(orderCrush.servicePoints)};
            var model = {
                routeAddre:$("#routeAddre").val(),
                items: items
            };
            loadServicePoint(model)
		});
		
        function loadServicePoint(model) {
            if(model && model.items && model.items.length>0){
                var tmpl = document.getElementById('tpl-crushServicePoint').innerHTML;
                var doTtmpl = doT.template(tmpl);
                var html = doTtmpl(model);
                $("#tabItems").html(html);
                $('a[data-toggle=tooltip]',"#tabItems").darkTooltip();
			}else{
                var tmpl = document.getElementById('tpl-crushServicePoint').innerHTML;
                var doTtmpl = doT.template(tmpl);
				model = {
                };
                var html = doTtmpl(model);
                $("#tabItems").html(html);
			}
        }
        
        function findCrushServicePointList(degree) {
            $("#navtabs").find("li").removeClass('active');
            $("#litab"+degree).addClass('active');
			var orderId = $("#orderId").val();
			var quarter = $("#quarter").val();
            var areaId = $("[id='area.id']").val();
            var loadingIndex = layerLoading('正在查询，请稍等...');
            $.ajax({
                type: "POST",
                url: "${ctx}/sd/order/crush/findCrushServicePointList",
                data:{orderId:orderId,quarter:quarter,areaId:areaId,degree:degree},
                success: function (data) {
                    top.layer.close(loadingIndex);
                    $('#btnTempSave').removeAttr('disabled');
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if(data && data.success == true) {
                        /*layerMsg('暂存成功');
                        top.layer.close(this_index);*/
						var model = {
							routeAddre:$("#routeAddre").val(),
							items: data.data
						};
                        loadServicePoint(model);
                    }
                    else if( data && data.success == false){
                        layerError(data.message,"错误提示");
                    }
                    else {
                        layerError("读取网点错误错误", "错误提示");
                    }
                    return false;
                },
                error: function (e) {
                    top.layer.close(loadingIndex);
                    //$('#btnTempSave').removeAttr('disabled');
                    ajaxLogout(e.responseText,null,"保存错误，请重试!");
                }
            });
        }
        
        <!-- 拨号插件 -->
        function plugin0() {
            return document.getElementById('plugin0');
        }
        plugin = plugin0;
        function addEvent(obj, name, func) {
            if (window.addEventListener) {
                obj.addEventListener(name, func, false);
            } else {
                obj.attachEvent("on" + name, func);
            }
        }

        function load() {
            addEvent(plugin(), 'OnDeviceConnect', function (number) {
                alert('设备连接事件：' + number);
            });
            addEvent(plugin(), 'OnDeviceDisconnect', function (number) {
                alert('设备断开事件：' + number);
            });
            addEvent(plugin(), 'OnCallOut', function (teleNo) {
                alert('呼叫事件：' + teleNo);
            });
            addEvent(plugin(), 'OnCallIn', function (teleNo) {
                alert('来电事件：' + teleNo);
            });
            addEvent(plugin(), 'OnHangUp', function (teleNo) {
                alert('挂起事件：' + teleNo);
            });
            addEvent(plugin(), 'OnAnswer', function (teleNo) {
                alert('应答事件：' + teleNo);
            });
        }
        function pluginLoaded() {
            //alert("Plugin loaded!");
        }

        //摘机
        function OffHookCtrl() {
            if (!plugin().OffHookCtrl())
                alert("OffHookCtrl Fail");
        }

        //拔号
        function StartDial(teleNo, bRecord) {
            if(Utils.isEmpty(teleNo)){
                return false;
            }
            if( !plugin().StartDial("00" + teleNo, bRecord) )
                alert("StartDial Fail");
        }

        //挂机或挂断
        function HangUpCtrl() {
            if (!plugin().HangUpCtrl())
                alert("HangUpCtrl Fail");
        }
        //上传录音
        function HangUpCtrl() {
            alert(plugin().UploadRecord());
        }
        function testEvent() {
            plugin().testEvent();
        }

        function pluginValid() {
            if (plugin().valid) {
                alert(plugin().echo("This plugin seems to be working!"));
            } else {
                alert("Plugin is not working :(");
            }
        }

        function addressRoute(fromAddr, toAddr) {
            var toAddress = encodeURI(toAddr);
            var fromAddress = encodeURI(fromAddr);

            var locateAddress = top.layer.open({
                type: 2,
                id:'layer_location_address',
                zIndex:19891015,
                title:'路径规划',
                content: "${ctx}/md/servicepoint/addressRoute?fromAddr=" + (fromAddress || '') +"&toAddr="+(toAddress|| ''),
                area: ['1255px', screen.height-200+'px'],
                shade: 0.3,
                shadeClose:true,
                maxmin: false,
                success: function(layero,index){
                }
            });
        }
        
        function updateAddress(orderId,quarter,id) {
            var updateAddress = top.layer.open({
                type: 2,
                id:'layer_updateAddress',
                zIndex:19891015,
                title:'修改',
                content: "${ctx}/sd/order/crush/updateAddressForm?orderId=" + (orderId || 0) +"&quarter="+(quarter|| '') +"&id=" + id +"&parentLayerIndex=" + parentLayerIndex,
                area: ['936px', '450px'],
                shade: 0.3,
                shadeClose:true,
                maxmin: false,
                success: function(layero,index){
                }
            });
        }

        function reload() {
            var id = $("#id").val();
            var quarter = $("#quarter").val();
            window.location.href = "${ctx}/sd/order/crush/close?id=" + (id || '') + "&quarter=" + quarter+"#layerIndex="+this_index;
        }

        function locateServiceAddress(serviceAddress) {
            var address = encodeURI(serviceAddress);
            var screen = getOpenDialogWidthAndHeight();
            var locateAddress = top.layer.open({
                type: 2,
                id:'layer_location_serviceAddress',
                zIndex:19891015,
                title:'地址定位',
                content:"${ctx}/sd/order/kefuOrderList/locateAddress?address=" + (address || ''),
                area: ['1255px', screen.height-200+'px'],
                shade: 0.3,
                shadeClose:true,
                maxmin: false,
                success: function(layero,index){
                }
            });
        }
        
	</script>
	<style type="text/css">
		legend span {
			border-bottom: #0096DA 4px solid;
			padding-bottom: 6px;}
		.form-horizontal{margin-top:5px;}
		.form-horizontal .control-label {width: 100px;}
		.form-horizontal .controls {margin-left: 100px;}
		#contentTable td,#contentTable th {text-align: center; vertical-align: middle;}
		.form-actions {margin-top: 0px;margin-bottom: 0px;padding: 8px 20px 8px;}
		.fromInput {
			border:1px solid #ccc;padding:3px 6px;color:#555;border-radius:4px;
		}
		.cance_btn{height: 30px;border-radius: 4px;color: white;border: 1px solid rgba(255, 255, 255, 0);background-color: rgba(255, 149, 2, 1)}
		.breadcrumb{
			padding: 5px 7px;
		}
	</style>
</head>
<body>
	<c:choose>
		<c:when test="${orderCrush.subArea != null && orderCrush.subArea.id != null && orderCrush.subArea.id >0}">
			<input type="hidden" id="routeAddre" value="${orderCrush.province.name}${orderCrush.city.name}${orderCrush.area.name}${orderCrush.subArea.name}${orderCrush.userAddress}">
		</c:when>
		<c:otherwise>
			<input type="hidden" id="routeAddre" value="${orderCrush.province.name}${orderCrush.city.name}${orderCrush.area.name}${orderCrush.userAddress}">
		</c:otherwise>
	</c:choose>
	<form:form id="inputForm" modelAttribute="orderCrush" action="${ctx}/sd/order/crush/saveClose" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="orderId"/>
		<form:hidden path="action"/>
		<form:hidden path="quarter"/>
		<form:hidden path="area.id"/>
        <input type="hidden" id="area.name" name="areaName" value="${orderCrush.area.fullName}" />
        <form:hidden path="subArea.id"/>
        <input type="hidden" id="subArea.name" name="subAreaName" value="${orderCrush.subArea.fullName}" />
        <form:hidden path="userAddress"/>
		<sys:message content="${message}"/>
		<div style="width: 35%;border-right: solid 1px #EEEEEE;float: left" id="leftDiv">
			<div style="width: 90%;height: auto;background-color: #F6F6F6;margin-top: 24px">
				<div class="row-fluid">
					<div class="span12" style="margin-top: 16px">
						<label class="control-label" style="padding-top: 0px">工单单号：</label>
						<div class="controls">
							<a href="javascript:void(0);" onclick="Order.showKefuOrderDetail('${orderCrush.orderId}','${orderCrush.quarter}',1);">${orderCrush.orderNo}</a>
						</div>
					</div>
				</div>
				<div class="row-fluid">
					<div class="span12">
						<label class="control-label" style="padding-top: 0px">突击单号：</label>
						<div class="controls">
							<p>${orderCrush.crushNo}</p>
						</div>
					</div>
				</div>
				<div class="row-fluid">
					<div class="span12">
						<label class="control-label" style="padding-top: 0px">用户地址：</label>
						<div class="controls">
							<p>
							  ${orderCrush.province.name}${orderCrush.city.name}/<a href="javascript:;" onclick="showHistoryPlanList(2);">${orderCrush.area.name}</a>
							  <c:if test="${orderCrush.subArea != null && orderCrush.subArea.id != null && orderCrush.subArea.id >0}">
								<span class="divider">/</span>
								<a href="javascript:;" onclick="showHistoryPlanList(3);">${orderCrush.subArea.name}</a>
							  </c:if>
							  /${orderCrush.userAddress}
							  <c:choose>
								  <c:when test="${orderCrush.subArea != null && orderCrush.subArea.id != null && orderCrush.subArea.id >0}">
									  <a style="padding-left: 10px;" href="javascript:void(0);" onclick="locateServiceAddress('${orderCrush.province.name}${orderCrush.city.name}${orderCrush.area.name}${orderCrush.subArea.name}${orderCrush.userAddress}')">
										  <img border="0" style="height: 21px;width: 19px;" src="${ctxStatic}/images/maps_96px.png" title="查看地图" />
									  </a>
								  </c:when>
								  <c:otherwise>
									  <a style="padding-left: 10px;" href="javascript:void(0);" onclick="locateServiceAddress('${orderCrush.province.name}${orderCrush.city.name}${orderCrush.area.name}${orderCrush.userAddress}')">
										  <img border="0" style="height: 21px;width: 19px;" src="${ctxStatic}/images/maps_96px.png" title="查看地图" />
									  </a>
								  </c:otherwise>
							  </c:choose>
							</p>
						</div>
					</div>
				</div>
				<div class="row-fluid" style="padding-bottom: 20px">
					<div class="span12">
						<label class="control-label" style="padding-top: 0px"></label>
						<div class="controls">
							<button class="btn btn-primary" type="button" onclick="updateAddress('${orderCrush.orderId}','${orderCrush.quarter}','${orderCrush.id}')">
								<i class="icon-edit"></i> 修改地址
							</button>
						</div>
					</div>
			    </div>
			</div>
			<legend style="margin-top: 15px;width: 90%"><span>发起</span></legend>
			<div style="width: 90%;height: auto;background-color: #F6F6F6">
				<div class="row-fluid">
					<div class="span12" style="margin-top: 16px">
						<label class="control-label" style="padding-top: 0px">发起人：</label>
						<div class="controls">
							<p>${orderCrush.createBy.name}</p>
						</div>
					</div>
				</div>
				<div class="row-fluid">
					<div class="span12">
						<label class="control-label" style="padding-top: 0px">发起时间：</label>
						<div class="controls">
							<p>${fns:formatDate(orderCrush.createDate,'yyyy-MM-dd HH:mm')}</p>
						</div>
					</div>
				</div>
				<div class="row-fluid" style="padding-bottom: 10px">
					<div class="span12">
						<label class="control-label" style="padding-top: 0px">发起内容：</label>
						<div class="controls">
							<p>${orderCrush.createRemark}</p>
						</div>
					</div>
				</div>
			</div>
			<legend style="margin-top: 15px;width: 90%"><span>处理</span></legend>
			<div style="width: 90%;height: auto">
				<div class="row-fluid">
					<div class="span12">
						<div class="control-group">
							<label class="control-label">处理内容：</label>
							<div class="controls">
								<form:textarea path="closeRemark" htmlEscape="false" rows="10" maxlength="496" class="input-block-level" cssStyle="resize: vertical;max-height: 300px;"/>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div style="float: right;width: 60%;margin-right: 20px;overflow-x:auto;" id="rightDiv">
			<ul id="navtabs" class="nav nav-tabs">
				<c:forEach items="${fns:getDictListFromMS('degreeType')}" var="dict">
					<c:choose>
						<c:when test="${dict.value == degreeType}">
							<li class="active" id="litab${dict.value}"><a href="javascript:findCrushServicePointList('${dict.value}');" style="width: 70px;text-align: center" title="${dict.label}">${dict.label}</a></li>
						</c:when>
						<c:otherwise>
							<li id="litab${dict.value}"><a href="javascript:findCrushServicePointList(${dict.value})" style="width: 70px;text-align: center" title="${dict.label}">${dict.label}</a></li>
						</c:otherwise>
					</c:choose>
				</c:forEach>
			</ul>
			<div class="tab-pane active" id="tabItems" style="margin-top: 24px;"></div>
		</div>
	</form:form>
   <%-- <div style="height: 60px;width: 100%"></div>--%>
    <div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
        <hr style="margin: 0px;border: 1px solid rgba(238, 238, 238, 1)"/>
        <shiro:hasPermission name="sd:orderCrush:edit">
            <div style="float: right;margin-top: 10px;margin-right: 20px">
                <button id="btnTempSave" class="btn btn-primary" type="button" value="暂存" style="margin-right: 5px;width: 96px;height: 40px" onclick="tempSave('${orderCrush.id}','${orderCrush.quarter}');">
                    暂 存
                </button>
                <button id="btnPlanOrder" class="btn btn-primary" type="button" style="margin-right: 5px;width: 96px;height: 40px" onclick="rushPlan('${orderCrush.orderId}','${orderCrush.orderNo}','${orderCrush.quarter}',1)" value="派单">
                    派 单
                </button>
				<button id="btnSubmit" class="cance_btn" type="button" style="margin-right: 5px;width: 96px;height: 40px" onclick="$('#inputForm').submit()" value="取 消">
					取消突击
				</button>
                <button id="btnCancel" class="btn" type="button" value="关 闭"  onclick="closeme();" style="width: 96px;height: 40px">
                    关 闭
                </button>
            </div>
        </shiro:hasPermission>
    </div>
	<object id="plugin0" type="application/x-nyteleactivex" width="0" height="0">
		<param name="onload" value="pluginLoaded"/>
		<param name="install-url" value="${ctxPlugin}/npNYTeleActiveX.dll"/>
	</object>
</body>
</html>