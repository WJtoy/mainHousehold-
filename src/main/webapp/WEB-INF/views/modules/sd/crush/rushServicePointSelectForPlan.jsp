<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>选择安维网点For派单</title>
	<meta name="decorator" content="default" />
	<style type="text/css">
		.pagination {  margin: 10px 0;  }
		.td {  word-break: break-all;  }
		.label-fullPay{background-color: #0096DA;}
		.label-notFullPay{background-color: #FF9500;}
		.label-notPay{background-color: #F64344;}
	</style>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<script type="text/javascript">
		var data = null;
		var index = top.layer.index;

		function setData(i){
			/*data = items[i];
			var layerIndex = $("#layerIndex").val() || index;
            top.$("#layui-layer"+layerIndex).find(".layui-layer-btn0").trigger("click");*/
            data = items[i];
            var servicePointId = data.id;
            var capacity = $("#capacity_"+servicePointId).text();
            var unfinishedCount = $("#unfinished_count_"+servicePointId).text();
            if(parseInt(unfinishedCount)>=parseInt(capacity)){
                layerError("该网点的未完工数量已经大于或等于网点的工单容量，请选择其他网点","提示");
                return false
            }
            var layerIndex = $("#layerIndex").val() || index;
            var parentIndex = $("#parentLayerIndex").val();
            if(parentIndex && parentIndex != undefined && parentIndex != ''){
                var layero = $("#layui-layer" + parentIndex,top.document);
                var iframeWin = top[layero.find('iframe')[0]['name']];
                iframeWin.setServicePoint(data);
                top.layer.close(layerIndex)
            }
		}
		var items =[
				<c:forEach items="${page.list}" var="point">{id:"${point.id}",servicePointNo:"${point.servicePointNo}",name:"${point.name}",primary:{id:"${point.primary.id}",name:"${point.primary.name}"}, phone:"${point.contactInfo1 }",grade:${point.grade},appFlag:${point.primary.appFlag},paymentType:{label:"${point.finance.paymentType.label}",value:${point.finance.paymentType.value}}}, </c:forEach>
			];
	</script>
	<script type="text/javascript">

        $(document).ready(function(){
            var degree = $("#degree").val();
        });
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
                $(inputId).focus()
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
			   layerError("获取保存类容错误","错误提示");
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

		function showHistoryPlanList(areaLevel){
			//areaLevel 0-省 1-市 2-区 3-街道
			var areaId = $("[id='area.id']").val();
			var areaName = $("[id='area.fullName']").val();
			var subAreaId = $("[id='subArea.id']").val();
			var subAreaName = $("[id='subArea.fullName']").val();
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
            // var zindex = Math.max(layer.zIndex||19891014,top.layer.zIndex||19891014,19891015);
			top.layer.open({
				type: 2,
				id:'layer_planlist',
				zIndex: 19891018,
				title: (areaLevel === 2?'['+areaName+']':'') + '历史派单记录',
				content: "${ctx}/sd/order/kefuOrderList/historyPlanList?" + parameters,
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

		function addressRoute(fromAddr, toAddr) {
            var toAddress = encodeURI("${servicePoint.address}");
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

		//跳到配置服务区域页面
		function showServicePointStation(servicePointId,areaId) {
			 top.layer.open({
                type: 2,
                id:'layer_servicePointStation',
                zIndex:19891015,
                title:'服务区域',
                content: "${ctx}/md/servicepointstation/showServicePointStationByAreaId?servicePointId=" + servicePointId +"&areaId="+ areaId,
                area: ['980px', screen.height-400+'px'],
                shade: 0.3,
                shadeClose:true,
                maxmin: false,
                success: function(layero,index){
                }
            });
        }

        // 网点下产品选择页面
        function showServicePointProduct(servicePointId) {
            top.layer.open({
                type: 2,
                id:'layer_servicePointProduct',
                zIndex:19891015,
                title:'产品',
                content: "${ctx}/md/servicePointProduct/showServicePointProductByPointId?servicePointId=" + servicePointId,
                area: ['600px', screen.height-520+'px'],
                shade: 0.3,
                shadeClose:true,
                maxmin: false,
                success: function(layero,index){
                }
            });
        }

        function findServicePoint(degree) {
           $("#degree").val(degree);
           $("#searchForm").submit();
        }

        function showServicePointAdd(areaId,productCategoryId,address,subAreaId) {
		    var layerIndex = $("#layerIndex").val();
            var parentLayerIndex = $("#parentLayerIndex").val();
            window.location.href="${ctx}/sd/order/crush/rushAddServicePointForPlanForm?dialogType=layer&area.id=" +areaId + '&productCategoryId='+ productCategoryId +"&address=" + encodeURI(address) +"&layerIndex=" + layerIndex + "&parentLayerIndex=" +parentLayerIndex +"&subArea.id=" + subAreaId;
        }
        function unableServicePoint(areaId,productCategoryId,address,subAreaId) {
            var layerIndex = $("#layerIndex").val();
            var parentLayerIndex = $("#parentLayerIndex").val();
            window.location.href="${ctx}/sd/order/crush/rushUnableSelectForPlan?dialogType=layer&area.id=" +areaId + '&productCategoryId='+ productCategoryId +"&address=" + encodeURI(address) +"&layerIndex=" + layerIndex + "&parentLayerIndex=" +parentLayerIndex +"&subArea.id=" + subAreaId;
        }

        function cancel() {
            var layerIndex = $("#layerIndex").val() || index;
            top.layer.close(layerIndex)
        }

	</script>
</head>

<body>
	<ul id="navtabs" class="nav nav-tabs">
		<%--<li class="active" id="trtLi"><a href="javascript:findConnomServicePoint();" style="width: 70px;text-align: center" title="常用网点">常用网点</a></li>
		<li id="commonLi"><a href="javascript:findTryServicePoint()" style="width: 70px;text-align: center" title="试单网点">试单网点</a></li>--%>
		<c:forEach items="${fns:getDictListFromMS('degreeType')}" var="dict">
			<c:choose>
				<c:when test="${dict.value == servicePoint.degree}">
					<li class="active"><a href="javascript:findServicePoint('${dict.value}');" style="width: 70px;text-align: center" title="${dict.label}">${dict.label}</a></li>
				</c:when>
				<c:otherwise>
					<li><a href="javascript:findServicePoint('${dict.value}');" style="width: 70px;text-align: center" title="${dict.label}">${dict.label}</a></li>
				</c:otherwise>
			</c:choose>
		</c:forEach>
		<li><a href="javascript:unableServicePoint('${servicePoint.area.id}','${servicePoint.productCategoryId}','${servicePoint.address}','${servicePoint.subArea.id}')">网点找回</a></li>
		<li><a href="javascript:showServicePointAdd('${servicePoint.area.id}','${servicePoint.productCategoryId}','${servicePoint.address}','${servicePoint.subArea.id}')">添加网点</a></li>
	</ul>
	<c:set var="currentuser" value="${fns:getUser()}" />
	<div style="margin-left:3px;margin-right:3px;">
		<form:form id="searchForm" modelAttribute="servicePoint"  action="${ctx}/sd/order/crush/rushSelectForPlan" method="post" class="breadcrumb form-inline">
			<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
			<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
			<input id="layerIndex" name="layerIndex" type="hidden" value="${servicePoint.layerIndex}"/>
			<input id="parentLayerIndex" name="parentLayerIndex" type="hidden" value="${servicePoint.parentLayerIndex}"/>
			<input id="canRush" name="canRush" type="hidden" value="${servicePoint.canRush}"/>
			<form:hidden path="degree"/>
			<form:hidden path="area.id"/>
			<form:hidden path="area.fullName"/>
			<form:hidden path="subArea.id"/>
			<form:hidden path="subArea.fullName"/>
			<form:hidden path="productCategoryId"/>
			<form:hidden path="address"/>
			<div>
				<label>工单区域：</label>
				<ul class="breadcrumb" style=" display: inline-block;background-color: white;">
					<li class="active">${servicePoint.city.parent.name} <span class="divider">/</span></li>
					<li class="active">${servicePoint.city.name} <span class="divider">/</span></li>
					<li><a href="javascript:;" onclick="showHistoryPlanList(2);">${servicePoint.area.name}</a> </li>
					<c:if test="${servicePoint.subArea != null && servicePoint.subArea.id != null && servicePoint.subArea.id >0}">
						<li><span class="divider">/</span><a href="javascript:;" onclick="showHistoryPlanList(3);">${servicePoint.subArea.name}</a></li>
					</c:if>
					<c:choose>
						<c:when test="${servicePoint.subArea != null && servicePoint.subArea.id != null && servicePoint.subArea.id >0}">
							<a style="padding-left: 10px;" href="javascript:void(0);" onclick="addressRoute('${servicePoint.city.parent.name}${servicePoint.city.name}${servicePoint.area.name}${servicePoint.subArea.name}', '${servicePoint.address}')">
								<img border="0" style="height: 21px;width: 19px;" src="${ctxStatic}/images/drivingRoute.png" title="查看工单地址" />
							</a>
						</c:when>
						<c:otherwise>
							<a style="padding-left: 10px;" href="javascript:void(0);" onclick="addressRoute('${servicePoint.city.parent.name}${servicePoint.city.name}${servicePoint.area.name}', '${servicePoint.address}')">
								<img border="0" style="height: 21px;width: 19px;" src="${ctxStatic}/images/drivingRoute.png" title="查看工单地址" />
							</a>
						</c:otherwise>
					</c:choose>
				</ul>
				&nbsp;&nbsp;
				<label>详细地址：</label>
				<span style="background-color: white;padding: 11px 15px;border-radius: 4px">${servicePoint.address}</span>
			</div>
			<div style="margin-top: 8px">
				<label>网点编号：</label>
				<form:input path="servicePointNo" maxlength="50" class="input-mini" cssStyle="width: 200px;"/>
				&nbsp;&nbsp;<label>网点名称：</label>
				<form:input path="name" maxlength="50" class="input-mini" cssStyle="width: 200px;"/>
				&nbsp;&nbsp;<label>网点电话：</label>
				<form:input path="contactInfo1" maxlength="20" class="input-mini digits" cssStyle="width: 100px;"/>
				&nbsp;&nbsp; <label>结算方式：</label>
				<select id="finance.paymentType" name="finance.paymentType" class="input-small" style="width:100px;">
					<option value=""
						<c:out value="${(servicePoint.finance==null || servicePoint.finance.paymentType == null || !empty servicePoint.finance.paymentType.value)?'selected=selected':''}" />>所有</option>
					<c:forEach items="${fns:getDictListFromMS('PaymentType')}" var="dict"><%--切换为微服务--%>
						<option value="${dict.value}"
							<c:out value="${(servicePoint.finance.paymentType.value eq dict.value)?'selected=selected':''}" />>${dict.label}</option>
					</c:forEach>
				</select> &nbsp;
					<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
			</div>
		</form:form>
		<sys:message content="${message}" />
		<c:set var="isEngineer" value="${currentuser.isEngineer()}"/>
		<table id="contentTable" class="datatable table table-bordered table-condensed table-hover" >
			<thead>
				<tr>
					<th width="45">序号</th>
					<th width="180">网点</th>
					<th width="100">主帐号</th>
					<th width="80">手机接单</th>
					<th width="120">手机</th>
					<th width="250">详细地址</th>
					<th width="80">派单数</th>
					<th width="80">完成单数</th>
					<th width="80">违约单数</th>
					<th width="55">评价分数</th>
					<th width="40">工单容量</th>
					<th width="40">未完工单数</th>
					<th width="55">结算方式</th>
                   <c:if test="${servicePoint.degree!=30}"> <%--返现网点不需要显示--%>
					<th width="45">质保等级<br/>已缴金额(元)</th>
                    </c:if>
					<th width="400">派单备注</th>
                    <th width="100">操作</th>
					<th width="180">备注</th>
				</tr>
			</thead>
			<tbody>
				<c:set var="index" value="0" />
				<c:forEach items="${page.list}" var="servicepoint">
					<c:set var="i" value="${i+1}" />
					<tr>
						<td>${i+(page.pageNo-1)*page.pageSize}</td>
						<td>
							<a href="javascript:void(0);" onclick="javascript:setData(${index});">
								${servicepoint.servicePointNo}<br/>${servicepoint.name}
									<c:if test="${servicepoint.primary.appLoged eq 1}">&nbsp;<i class="icon-mobile-phone" style="font-size: 17px;" title="该用户有手机登陆过APP" ></i></c:if>
						</a>
						</td>
						<td>${servicepoint.primary.name}</td>
						<td>${servicepoint.primary.appFlag eq 1?'是':'否'}</td>
						<td>${servicepoint.contactInfo1}
							<c:if test="${!empty servicepoint.contactInfo1}">
							<button id="btnCall" class="btn btn-success" type="button" onclick="javascript:StartDial(${servicepoint.contactInfo1},false)"><i class="icon-phone-sign icon-white"></i>  拨号</button>
							</c:if>
						</td>
						<td>
							<a style="padding-left: 10px;" href="javascript:void(0);" onclick="addressRoute('${servicepoint.address}', '')">
								<img border="0" style="height: 21px;width: 19px;" src="${ctxStatic}/images/drivingRoute.png" title="查看路径" />
							</a> ${servicepoint.address}
						</td>
						<td><span class="label label-info">${servicepoint.planCount}</span>
						</td>
						<td><span class="label label-success">${servicepoint.orderCount}</span>
						</td>
						<td><span class="label label-important">${servicepoint.breakCount}</span>
						</td>
						<td>${servicepoint.grade}</td>
						<td id="capacity_${servicepoint.id}">${servicepoint.capacity}</td>
						<td id="unfinished_count_${servicepoint.id}">${servicepoint.unfinishedOrderCount}</td>
						<td>${servicepoint.finance.paymentType.label}<c:if test="${servicepoint.finance.invoiceFlag == 1}"><br/><span class="label status_Canceled">开票</span></c:if></td>
                        <c:if test="${servicePoint.degree!=30}">
						<td>
							<c:choose>
								<c:when test="${servicepoint.mdDepositLevel!=null and servicepoint.mdDepositLevel.id>0}">
									${servicepoint.mdDepositLevel.name}<br/>${servicepoint.finance.deposit}<br/>
									<c:choose>
										<c:when test="${servicepoint.finance.deposit>=servicepoint.deposit}"><span class="label label-fullPay">已缴满</span></c:when>
										<c:when test="${servicepoint.finance.deposit > 0}"><span class="label label-notFullPay">未缴满</span></c:when>
										<c:otherwise><span class="label label-notPay">未缴费</span></c:otherwise>
									</c:choose>
								</c:when>
								<c:otherwise>
									<p style="color: red">无</p>
								</c:otherwise>
							</c:choose>
						</td>
                        </c:if>
						<td>
							<div class="planText" style="width: 100px;float: left;">
								<textarea id="planRemark_${servicepoint.id}"  class="input" rows="2" maxlength="98" style="width: 130px;margin-bottom: 5px;border: 1px solid #ccc;padding: 4px 6px;border-radius: 5px;resize: vertical;max-height: 300px;overflow:hidden">${servicepoint.planRemark}</textarea>
							</div>
							<div class="planBtn" style="float: right;width: 50px;">
								<button id="btnSavePlanRemark" class="btn btn-small" style="margin-bottom: 5px;height: 25px;" data-serveicepointid="${servicepoint.id}" type="button" onclick="saveServicePointPlanRemark('${servicepoint.id}',this);">
									保存
								</button>
								<button id="btnShowPlanRemarkList" style="margin-left: 2px;height: 25px;" class="btn btn-small" type="button" onclick="viewPlanRemarkList('${servicepoint.id}','${servicepoint.servicePointNo}','${servicepoint.name}');">
									历史
								</button>
							</div>
						</td>
                        <td style="border-left-style: none">

							<c:if test="${servicePoint.subArea == null || servicePoint.subArea.id == null || servicePoint.subArea.id <=0}">
								<button id="btnShowServicePointStation" style="margin-bottom: 5px;height: 25px;" class="btn btn-small" type="button" onclick="showServicePointStation('${servicepoint.id}',${servicePoint.area.id});">
									区域
								</button>
							</c:if>
							<button id="btnShowServicePointProduct" style="margin-left: 2px;height: 25px;" class="btn btn-small" type="button" onclick="showServicePointProduct('${servicepoint.id}')">
								产品
							</button>
                        </td>
						<td>
							<c:if test="${isEngineer == false}">
								<a href="javascript:void(0);" title="${servicepoint.remarks}">${fns:abbr(servicepoint.remarks,35)}</a>
							</c:if>
						</td>
					</tr>
					<c:set var="index" value="${index+1}" />
				</c:forEach>
			</tbody>
		</table>
		<div class="pagination">${page}</div>
	</div>
	<div style="height: 60px;width: 100%"></div>
	<div style="position: fixed;bottom: 0; width: 100%;height: 60px;background-color: white">
		<hr style="margin: 0px;border: 1px solid rgba(238, 238, 238, 1)"/>
		<div style="float: right;margin-top: 10px;margin-right: 20px">
			<input id="btnCancel" class="btn" type="button" value="关  闭" style="width: 96px;height: 40px" onclick="cancel()"/>
		</div>
	</div>
	<object id="plugin0" type="application/x-nyteleactivex" width="0" height="0">
			    <param name="onload" value="pluginLoaded" />
				<param name="install-url" value="${ctxPlugin}/npNYTeleActiveX.dll" />
			</object>
	<script type="text/javascript">
        function plugin0()
        {
            return document.getElementById('plugin0');
        }
        plugin = plugin0;
        function addEvent(obj, name, func)
        {
            if (window.addEventListener) {
                obj.addEventListener(name, func, false);
            } else {
                obj.attachEvent("on"+name, func);
            }
        }

        function load()
        {
            addEvent(plugin(), 'OnDeviceConnect', function(number){
                alert('设备连接事件：'+number);
            });
            addEvent(plugin(), 'OnDeviceDisconnect', function(number){
                alert('设备断开事件：'+number);
            });
            addEvent(plugin(), 'OnCallOut', function(teleNo){
                alert('呼叫事件：'+teleNo);
            });
            addEvent(plugin(), 'OnCallIn', function(teleNo){
                alert('来电事件：'+teleNo);
            });
            addEvent(plugin(), 'OnHangUp', function(teleNo){
                alert('挂起事件：'+teleNo);
            });
            addEvent(plugin(), 'OnAnswer', function(teleNo){
                alert('应答事件：'+teleNo);
            });
        }
        function pluginLoaded() {
            //alert("Plugin loaded!");
        }

        //摘机
        function OffHookCtrl()
        {
            if( !plugin().OffHookCtrl() )
                alert("OffHookCtrl Fail");
        }

        //拔号
        function StartDial(teleNo, bRecord)
        {
            teleNo="00"+teleNo;
            alert(teleNo);
            if( !plugin().StartDial(teleNo, bRecord) )
                alert("StartDial Fail");
        }

        //挂机或挂断
        function HangUpCtrl()
        {
            if( !plugin().HangUpCtrl() )
                alert("HangUpCtrl Fail");
        }
        //上传录音
        function HangUpCtrl()
        {
            alert(plugin().UploadRecord());
        }
        function testEvent()
        {
            plugin().testEvent();
        }

        function pluginValid()
        {
            if(plugin().valid){
                alert(plugin().echo("This plugin seems to be working!"));
            } else {
                alert("Plugin is not working :(");
            }
        }
	</script>
	<script type="text/javascript" language="javascript" class="init">
	$(document).ready(function() {
		$("td,th").css({"text-align":"center","vertical-align":"middle"});
        if(Utils.isEmpty($("#layerIndex").val())){
            $("#layerIndex").val(index);
        }
	});
	</script>
</body>
</html>
