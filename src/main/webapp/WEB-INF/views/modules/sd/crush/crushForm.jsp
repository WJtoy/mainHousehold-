<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>突击单</title>
	<meta name="description" content="新开或修改突击单">
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<meta name="decorator" content="default"/>
	<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
	<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
	<script src="${ctxStatic}/sd/Order.js?_v=${OrderJsVersion}" type="text/javascript"></script>
	<script type="text/javascript">
        var parentIndex = getCookie('layer.parent.id');
		var this_index = top.layer.index;
        var index = parent.layer.getFrameIndex(window.name); //获取窗口索引
        var clickTag = 0;
		$(document).ready(function() {
            $('a[data-toggle=tooltip]').darkTooltip();

			$("#inputForm").validate({
				submitHandler: function(form){
				    var $btnSubmit = $("#btnSubmit");
                    layer.confirm('突击单提交后，<font color="red">不允许修改</font>。确定要提交突击单吗?', {icon: 3, title:'系统确认'}, function(index){
                        layer.close(index);//关闭本身
						//console.log("date:" + new Date().getTime() + " ,clickTag:" + clickTag);
                        if(clickTag == 1){
                            return false;
                        }
                        clickTag = 1;
                        $btnSubmit.attr('disabled', 'disabled');
                        // do something
                        var loadingIndex;
                        var ajaxSuccess = 0;
                        var action = $("#action").val();
                        $.ajax({
                            async: false,
                            cache: false,
                            type: "POST",
                            url: "${ctx}/sd/order/crush/save?"+ (new Date()).getTime(),
                            data:$(form).serialize(),
                            beforeSend: function () {
                                loadingIndex = layer.msg('正在提交，请稍等...', {
                                    icon: 16,
                                    time: 0,//不定时关闭
                                    shade: 0.3
                                });
                            },
                            complete: function () {
                                if(loadingIndex) {
                                    layer.close(loadingIndex);
                                }
                                //console.log("complete:" + new Date().getTime());
                                if(ajaxSuccess == 0) {
                                    setTimeout(function () {
                                        clickTag = 0;
                                        $btnSubmit.removeAttr('disabled');
                                    }, 2000);
                                }
                            },
                            success: function (data) {
                                if(ajaxLogout(data)){
                                    return false;
                                }
                                if(data && data.success == true) {
                                    if(action=='0') {
                                        ajaxSuccess = 1;
                                        //回调父窗口方法
                                        setTimeout(function() {
                                            //var layero = $("#layui-layer" + parentIndex,top.document);
                                            //var iframeWin = top[layero.find('iframe')[0]['name']];
                                            //iframeWin.updateService();
                                            var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                                            if(iframe != undefined){
                                                iframe.repage();
                                            }
                                            //top.layer.close(this_index);//关闭本窗口
                                            parent.layer.close(index);
                                            return false;
                                        }, 300);
                                        layerMsg('提交成功');
                                        return false;
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
                                ajaxLogout(e.responseText,null,"保存错误，请重试!");
                            }
                        });
                    },function(index){
                        //cancel
                        // $btnSubmit.removeAttr('disabled');
                        clickTag =0;
                    });
                    return false;
				},
				errorContainer: "#messageBox"
			});
			
		});

        $(document).on("click", "#btnTempSave", function () {
            var self = this;
            if(clickTag == 1){
                return false;
            }
            clickTag = 1;
            $(self).attr('disabled', 'disabled');
            var loadingIndex;
            $.ajax({
                type: "POST",
                url: "${ctx}/sd/order/crush/tempCreateSave?"+ (new Date()).getTime(),
                data:$("#inputForm").serialize(),
                beforeSend: function () {
                    loadingIndex = top.layer.msg('正在暂存，请稍等...', {
                        icon: 16,
                        time: 0,
                        shade: 0.3
                    });
                },
                complete: function () {
                    if(loadingIndex) {
                        top.layer.close(loadingIndex);
                    }
                    $(self).removeAttr('disabled');
                    clickTag = 0;
                },
                success: function (data) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if(data && data.success == true) {
                        layerMsg('暂存成功');

                        setTimeout(function() {
                            //var layero = $("#layui-layer" + parentIndex,top.document);
                            //var iframeWin = top[layero.find('iframe')[0]['name']];
                            //iframeWin.updateService();
                            //top.layer.close(this_index);//关闭本窗口
                            var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                            if(iframe != undefined){
                                iframe.repage();
                            }
                            //top.layer.close(this_index);//关闭本窗口
                            parent.layer.close(index);
                            return false;
                        }, 300);
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
                    ajaxLogout(e.responseText,null,"保存错误，请重试!");
                }
            });
        });

		function closeme(){
			//top.layer.close(this_index);
            layer.confirm(
                '取消后，填写的单据内容不保存，<br/>确定取消保存并关闭窗口吗？'
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
	</script>
	<style type="text/css">
		.form-horizontal{margin-top:5px;}
		.form-horizontal .control-label {width: 100px;}
		.form-horizontal .controls {margin-left: 120px;}
        #contentTable td,#contentTable th {text-align: center; vertical-align: middle;}
        .form-actions {margin-top: 0px;margin-bottom: 0px;padding: 8px 20px 8px;}
	</style>
</head>
<body>
	<form:form id="inputForm" modelAttribute="orderCrush" action="${ctx}/sd/order/crush/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="action"/>
		<form:hidden path="quarter"/>
		<form:hidden path="crushNo" />
		<form:hidden path="status" />
		<form:hidden path="orderId" />
		<form:hidden path="area.id"/>
		<form:hidden path="customer.id"/>
		<form:hidden path="productCategoryId"/>
		<form:hidden path="orderServiceType"/>
		<sys:message content="${message}"/>
	<fieldset>
		<legend>
				<p class="text-right" style="margin-right: 10px;<c:if test="${empty orderCrush.crushNo}">margin-right: 115px;</c:if>">No. ${orderCrush.crushNo}</p>
		</legend>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">工单号:</label>
					<div class="controls">
						<form:input path="orderNo"  htmlEscape="false" cssClass="input-block-level required" readonly="true"/>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">客户:</label>
					<div class="controls">
						<form:hidden path="customer.id"></form:hidden>
						<form:input path="customer.name"  htmlEscape="false" cssClass="input-block-level" readonly="true"/>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">用户:</label>
					<div class="controls">
						<form:input path="userName"  htmlEscape="false" readonly="true" cssClass="input-block-level required" />
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">电话:</label>
					<div class="controls">
						<form:input path="userPhone"  htmlEscape="false" readonly="true" cssClass="input-block-level required"  />
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">用户地址:</label>
					<div class="controls">
						<form:input path="userAddress"  htmlEscape="false" readonly="true" cssClass="input-block-level required" cssStyle="width: 95%" />
						<a style="padding-left: 10px;" href="javascript:void(0);" onclick="Order.locateAddr('${orderCrush.userAddress}')">
							<img border="0" style="height: 21px;width: 19px;" src="${ctxStatic}/images/maps_96px.png" title="查看地图" />
						</a>
					</div>
				</div>
			</div>

		</div>
		<div id="divGrid" style="overflow-x:auto;max-height: 400px;margin-bottom: 20px;border: #f5f5f5 1px solid">
			<table id="contentTable" class="datatable table table-bordered table-condensed table-hover" >
				<thead>
				<tr>
					<th width="45">序号</th>
					<th width="180">网点</th>
					<th width="100">主帐号</th>
					<th width="70">手机接单</th>
					<th width="120">手机</th>
					<th width="250">详细地址</th>
					<th width="80">完成单数</th>
					<th width="180">网点备注</th>
					<th width="200">派单备注</th>
					<th width="150">派单备注操作</th>
				</tr>
				</thead>
				<tbody>
				<c:set var="index" value="0" />
				<c:forEach items="${orderCrush.servicePoints}" var="servicepoint" >
					<c:set var="i" value="${i+1}" />
					<tr>
						<td>${i}</td>
						<td>
							<a href="javascript:void(0);">
									${servicepoint.servicePointNo}<br/>
											${servicepoint.name}
							</a>
						</td>
						<td>${servicepoint.primary.name}</td>
						<td>${servicepoint.primary.appFlag eq 1?'是':'否'}</td>
						<td>${servicepoint.contactInfo1}
							<c:if test="${!empty servicepoint.contactInfo1}">
								<button id="btnCall" class="btn btn-success" type="button" onclick="javascript:StartDial('${servicepoint.contactInfo1}',false)"><i class="icon-phone-sign icon-white"></i>  拨号</button>
							</c:if>
						</td>
						<td>
							<a style="padding-left: 10px;" href="javascript:void(0);" onclick="addressRoute('${servicepoint.address}','${orderCrush.userAddress}')">
								<img border="0" style="height: 21px;width: 19px;" src="${ctxStatic}/images/drivingRoute.png" title="查看地图" />
							</a>
							${servicepoint.address}
						</td>
						<td><span class="label label-success">${servicepoint.orderCount}</span></td>
						<td>
							<a href="javascript:void(0);" title="${servicepoint.remarks}">${fns:abbr(servicepoint.remarks,35)}</a>
						</td>
						<td>
                            <textarea id="planRemark_${servicepoint.id}"  class="input" rows="2" maxlength="98" style="margin-bottom: 5px;border: 1px solid #ccc;padding: 4px 6px;border-radius: 5px;resize: vertical;max-height: 300px;overflow:hidden">${servicepoint.planRemark}</textarea>
                        </td>
						<td style="border-left-style: none">
                            <button id="btnSavePlanRemark" class="btn btn-mini" data-serveicepointid="${servicepoint.id}" type="button" onclick="saveServicePointPlanRemark('${servicepoint.id}',this);">
                                保存
                            </button>
                            <button id="btnShowPlanRemarkList" style="margin-left: 2px;" class="btn btn-mini" type="button" onclick="viewPlanRemarkList('${servicepoint.id}','${servicepoint.servicePointNo}','${servicepoint.name}');">
                                历史
                            </button>
                        </td>
					</tr>
					<c:set var="index" value="${index+1}" />
				</c:forEach>
				</tbody>
			</table>
		</div>
		<div class="row-fluid">
			<div class="span12">
				<div class="control-group">
					<label class="control-label">发起说明:</label>
					<div class="controls">
						<form:textarea path="createRemark" htmlEscape="false" rows="4" maxlength="190" class="input-block-level" cssStyle="resize: vertical;max-height: 300px;" />
					</div>
				</div>
			</div>
		</div>
		<div class="form-actions">
			<c:if test="${canAction eq true }">
				<shiro:hasPermission name="sd:orderCrush:edit">
					<button id="btnTempSave" class="btn btn-info" type="button" value="暂存" >
						<i class="icon-hand-up"></i> 暂 存
					</button>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<button id="btnSubmit" class="btn btn-primary" type="submit" value="提交">
					<i class="icon-save"></i> 提 交
				</button>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				</shiro:hasPermission>
			</c:if>
			<button id="btnCancel" class="btn" type="button" value="取 消"  onclick="closeme();">
				<i class="icon-remove"></i> 取 消
			</button>
		</div>
	</fieldset>
	</form:form>
	<object id="plugin0" type="application/x-nyteleactivex" width="0" height="0">
		<param name="onload" value="pluginLoaded"/>
		<param name="install-url" value="${ctxPlugin}/npNYTeleActiveX.dll"/>
	</object>
	<script type="text/javascript">
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
	</script>
	<script type="text/javascript">
		//保存新的网店备注
		function saveServicePointPlanRemark(servicePointId,btn) {
			if(btn.disabled==true){return fasle;}
			btn.disabled=true;
			var id= btn.dataset.serveicepointid;
			var inputId="#planRemark_"+id;
			var planRemark=$(inputId).val();
			if (!planRemark){
				//layerError("请先输入备注信息");
				layerMsg('请先输入备注信息');
				btn.disabled=false;
				$(inputId).focus();
				return false;
			}
			if (servicePointId!=null && planRemark!=null){
				$.ajax({
					cache: false,
					type: "POST",
					url:"${ctx}/md/servicepoint/ajax/savePlanRemark?servicePointId=" + servicePointId + "&planRemark=" + (planRemark || ''),
					dataType: 'json',
					success: function (data) {
						btn.disabled=false;
						if(ajaxLogout(data)){
							return false;
						}
						if (data.success ) {
							layerMsg('保存成功');

						}else{
							layerError(data.message);
						}
					},
					error: function (e) {
						ajaxLogout(e.responseText,null,"保存派单备注错误，请重试!");
						btn.disabled=false;
					}
				});

			}else {
				layerError("获取保存内容错误","错误提示");
				return false;
			}

		}
		//查看网店备注历史列表
		function viewPlanRemarkList(servicePointId,servicePointNo,servicePointName) {
			var planIndex = top.layer.open({
				type: 2,
				id:'layer_planRemarkList_view',
				zIndex:19891016,
				title:'网点备注',
				content: "${ctx}/md/servicepoint/viewPlanRemarkList?servicePointId=" + (servicePointId || '')+"&servicePointNo="+ (servicePointNo || '')+"&servicePointName="+ (servicePointName || ''),
				// area: ['980px', '640px'],
				area: ['1255px', (screen.height/2)+'px'],
				shade: 0.3,
				shadeClose: true,
				maxmin: false,
				success: function(layero,index){
				},
				end:function(){
				}
			});
		}

	</script>

</body>
</html>