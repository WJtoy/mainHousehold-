<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>安维管理</title>
	<meta name="decorator" content="default" />
	<%@include file="/WEB-INF/views/include/dialog.jsp"%>
	<link href="${ctxStatic}/jquery.darktooltip/darktooltip.min.css" type="text/css" rel="stylesheet" />
	<script src="${ctxStatic}/jquery.darktooltip/jquery.darktooltip.min.js" type="text/javascript"></script>
	<script src="${ctxStatic}/jquery-honeySwitch/honeySwitch.js" type="text/javascript"></script>
	<link href="${ctxStatic}/jquery-honeySwitch/honeySwitch.css" rel="stylesheet"/>
	<script src="${ctxStatic}/js/ajaxfileupload.js"></script>
	<%--<script src="${ctxStatic}/js/fixtable.js" type="text/javascript"></script>--%>
	<style type="text/css">
	.sort {color: #0663A2;cursor: pointer;}
	.form-horizontal .control-label {width: 70px;}
	.form-horizontal .controls { margin-left: 80px;}
	.form-search .ul-form li label {width: auto;}
	.messageNew{
		left: 240px;
		top: 136px;
		width: 100%;
		height: 32px;
		line-height: 17px;
		border-radius: 2px;
		background-color: rgba(255, 247, 214, 100);
		color: rgba(81, 90, 110, 100);
		font-size: 12px;
		text-align: left;
		font-family: Roboto;
		border: 1px solid rgba(255, 203, 1, 100);
	}
	</style>
	<%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
	<script type="text/javascript">
		top.layer.closeAll();
	//    $(document).ready(function () {
	//        var w = $(window).width();
	//        FixTable("contentTable", 2, w, "100%");
	//    });
		$(document).ready(function() {
			 $('a[data-toggle=tooltip]').darkTooltip();
			 $('a[data-toggle=tooltipeast]').darkTooltip({gravity:'east'});
		});

		function resetPassword(id){
			if (id == undefined || id=='' || id == '0') {
				layerError('参数错误', '错误提示',true);
				return false;
			}

			top.layer.confirm("重置密码后，师傅需要重新登录，<br/>确定要<font color='blue'>重置</font>密码吗?", {icon: 3, title:'系统确认'}, function(index){
				top.layer.close(index);//关闭本身
				// do something
				var data = {id:id};
				$.ajax({
					cache: false,
					type: "POST",
					url: "${ctx}/md/engineer/resetPassword",
					data: data,
					success: function (data) {
						if (data.success){
							layerMsg("密码已重置成功。<br/>新的密码就是您的手机号<font color='#ff4500'>后六位</font>。",true)
						}
						else{
							layerError(data.message,"错误提示",true);
						}
					},
					error: function (xhr, ajaxOptions, thrownError) {
						layerError(thrownError,"错误提示",true);
					}
				});
			},function(index){
				//cancel
			});
			return false;
		}

		function pointSelect_callback(data){
			$("[id^='servicePoint.servicePointNo']").val(data.servicePointNo);
		}

		function upgrade(id)
		{
			top.$.jBox.open("iframe:" + "${ctx}/md/engineer/upgrade?id=" + id, "升级网点", 800, 600,
				{
					top : '10px',
					buttons : {},
					loaded : function(h)
					{
						$(".jbox-content", h).css("overflow-y","hidden");
						$("#jbox-iframe", h).prop("height", "98%");
					}
				});
		}

        function editEngineer(type,id) {
            var h = $(top.window).height();
            var w = $(top.window).width();
            var text = "添加师傅";
            var url = "${ctx}/md/engineer/form";
            var area = ['900px',(h-250)+'px'];
            if(type == 2){
                text = "修改师傅";
                url = "${ctx}/md/engineer/form?id=" + id;
                area = ['900px',(h-180)+'px'];
            }
            top.layer.open({
                type: 2,
                id:"engineer",
                zIndex:19891015,
                title:text,
                content: url,
                area: area,
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }

		function enableUser(id,name,contactInfo,obj){
			var title;
			var delFlag = $("#delFlag_"+id+"").val();
			var url;
			if(delFlag == 0){
				title = "停用";
				delFlag = 1;
				url = "${ctx}/md/engineer/delete?id="+id;
			}else {
				delFlag = 0;
				title = "启用";
				url = "${ctx}/md/engineer/enable?id="+id +"&contactInfo=" + contactInfo;
			}

			layer.confirm(
					'确认要'+ title+'<label style="color:blue">'+name +'</label>吗？',
					{
						btn: ['确定','取消'], //按钮
						title:'提示',
						cancel: function(index, layero){
							// 右上角叉
							if ($(obj).attr("class") == 'switch-off') {
								honeySwitch.showOn(obj);
							} else {
								honeySwitch.showOff(obj);
							}
						}
					}, function(index){
						layer.close(index);//关闭本身
						var loadingIndex = top.layer.msg('正在'+ title +'，请稍等...', {
							icon: 16,
							time: 0,//不定时关闭
							shade: 0.3
						});
						$.ajax({
							url: url,
							success:function (data) {
								// 提交后的回调函数
								if(loadingIndex) {
									setTimeout(function () {
										layer.close(loadingIndex);
									}, 2000);
								}
								if (data.success) {
									layerMsg(data.message);

									// 停用
									if (delFlag == 1) {
										$("#delFlag_"+id+"").val(1);
										$("#tr_"+id+" .noStop").css("color","red");
										$("#tr_"+id+" .noStop").attr("class", "stop");

									} else {
										$("#delFlag_"+id+"").val(0);
										$("#tr_"+id+" .stop").css("color","");
										$("#tr_"+id+" .stop").attr("class", "noStop");
									}

								} else {
									layerError(title + "失败:" + data.message, "错误提示");
									// 取消操作
									if ($(obj).attr("class") == 'switch-off') {
										honeySwitch.showOn(obj);
									} else {
										honeySwitch.showOff(obj);
									}
								}
								return false;
							},
							error: function (data) {
                                layer.close(loadingIndex);
								ajaxLogout(data,null,"数据保存错误，请重试!");
								if ($(obj).attr("class") == 'switch-off') {
									honeySwitch.showOn(obj);
								} else {
									honeySwitch.showOff(obj);
								}
							},
						});
						return false;
					}, function(){
						// 取消操作
						if ($(obj).attr("class") == 'switch-off') {
							honeySwitch.showOn(obj);
						} else {
							honeySwitch.showOff(obj);
						}
					});
		}
		</script>
</head>
<body>
	<ul class="nav nav-tabs">
		<li class="active"><a href="javascript:;">师傅</a></li>
		<%--<shiro:hasPermission name="md:engineer:edit">--%>
			<%--<li><a href="${ctx}/md/engineer/form?servicePoint.id=${engineer.servicePoint.id}">网点师傅添加</a></li>--%>
		<%--</shiro:hasPermission>--%>
	</ul>
	<c:set var="currentuser" value="${fns:getUser() }" />
	<c:set var="isSystemUser" value="${currentuser.isSystemUser()}" />
	<sys:message content="${message}" type="loading" />
<%--	<div class="messageNew">--%>
<%--		<div style="margin-left: 10px;margin-top: 5px">${message}</div>--%>
<%--		<button data-dismiss="alert" class="close">×</button>${fn:replace(content,';','.')}--%>
<%--	</div>--%>
	<form:form id="searchForm" modelAttribute="engineer" action="${ctx}/md/engineer" method="POST" class="breadcrumb form-search">
		<form:hidden path="firstSearch" />
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}" />
		<input id="orderBy" name="orderBy" type="hidden" value="${engineer.orderBy}" />
		<ul class="ul-form">
			<li>
				<label>网点编号：</label>
				<c:choose>
					<c:when test="${isSystemUser}">
						<form:input path="servicePoint.servicePointNo" readonly="false" htmlEscape="false" class="input-small" maxlength="20" cssStyle="width: 186px"/>
					</c:when>
					<c:otherwise>
						<form:input path="servicePoint.servicePointNo" readonly="true" htmlEscape="false" class="input-small" maxlength="20" cssStyle="width: 186px"/>
					</c:otherwise>
				</c:choose>

			</li>
			<li>
				<label>网点名称：</label>
				<c:choose>
					<c:when test="${isSystemUser}">
						<form:input path="servicePoint.name" htmlEscape="false" maxlength="30" class="input-small" cssStyle="width: 266px"/>
						<%--<md:pointselectlayer id="servicePoint" name="servicePoint.id" value="${engineer.servicePoint.id}" labelName="servicePointNo.name" labelValue="${engineer.servicePoint.name}"--%>
											 <%--width="1200" height="780" callbackmethod="pointSelect_callback" title="选择安维网点" areaId="" hidePhone="1" cssClass="required"/>--%>
					</c:when>
					<c:otherwise>
						<form:input path="servicePoint.name" readonly="true" htmlEscape="false" class="input-small" cssStyle="width: 266px"/>
						<form:hidden path="servicePoint.id" />
					</c:otherwise>
				</c:choose>
			</li>
			<li>
				<label>师傅名称：</label>
				<form:input path="name" htmlEscape="false" maxlength="20" class="input-mini" style="width: 186px"/>
			</li>
			<li>
				<label>联系电话：</label>
				<form:input path="contactInfo" htmlEscape="false" maxlength="20" class="input-small" cssStyle="width: 186px"/>
			</li>
			<li>
				<label>帐号类型：</label>
				<form:select path="masterFlag" class="input-small" cssStyle="width: 200px">
					<form:option value="-1">所有</form:option>
					<form:option value="1">主帐号</form:option>
					<form:option value="0">子帐号</form:option>
				</form:select>
			</li>

		</ul>
		<ul class="ul-form" style="margin-top: 10px">
			<li>
				<label>手机接单：</label>
				<form:select path="appFlag" class="input-small" cssStyle="width: 200px">
					<form:option value="-1">所有</form:option>
					<form:option value="1">是</form:option>
					<form:option value="0">否</form:option>
				</form:select>
			</li>
			<shiro:hasPermission name="md:engineer:view">
				<li class="btns"><input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" /></li>
				<li class="clearfix"></li>
			</shiro:hasPermission>
		</ul>
	</form:form>

	<shiro:hasPermission name="md:engineer:edit">
		<button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 120px;height: 30px" onclick="editEngineer(1,null)">
			<i class="icon-plus-sign"></i>&nbsp;添加师傅
		</button>
	</shiro:hasPermission>
<shiro:hasPermission name="md:engineer:view">
	<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover table-hover">
		<thead>
			<tr>
				<th width="55">序号</th>
				<th width="100">网点编号</th>
				<th width="140">网点名称</th>
				<th width="100">师傅名称</th>
				<th width="80">联系电话</th>
				<th width="160">师傅地址</th>
				<th width="160">收货地址</th>
<%--				<th width="30">QQ</th>--%>
				<th width="40">等级</th>
				<th width="50">派单量</th>
				<th width="40">评分</th>
				<th width="45">帐号类型</th>
				<th width="60">手机接单</th>
				<th width="100">服务区域</th>
				<th width="90">启用</th>
				<th width="160">操作</th>
				<%--<shiro:hasPermission name="md:tmall:servicepoint"> &lt;%&ndash; B2B &ndash;%&gt;--%>
					<%--<th>B2B管理</th>--%>
				<%--</shiro:hasPermission>--%>
			</tr>
		</thead>
		<tbody>
			<c:forEach items="${page.list}" var="model">
				<c:set var="index" value="${index+1}" />
				<tr id="tr_${model.id}">
					<td>${index+(page.pageNo-1)*page.pageSize}</td>
					<td>${model.servicePoint.servicePointNo}</td>
					<td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${model.servicePoint.name}">${fns:abbr(model.servicePoint.name,25)}</a></td>
					<td>${model.name}</td>
					<td>${model.contactInfo}</td>
					<td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${model.address}">${fns:abbr(model.address,30)}</a></td>
					<td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${model.engineerAddress.address}">${fns:abbr(model.engineerAddress.address,25)}</a></td>
<%--					<td>${model.qq}</td>--%>
					<td>${model.level.label}</td>
					<td>${model.planCount}</td>
					<td>${model.grade}</td>
					<td>${model.masterFlag eq 1?'主帐号':'子帐号'}</td>
					<td style="color: <c:out value="${model.appFlag == 1 ?'':'#F54142'}"/>">${model.appFlag == 1 ? '是' : '否'}</td>
					<td><a href="javascript:" data-toggle="tooltip"  data-tooltip="${model.areas}">${fns:abbr(model.areas,20)}</a></td>
					<td>
					<shiro:hasPermission name="md:engineer:stop">
						<c:choose>
							<c:when test="${model.delFlag==0}">
<%--								<a href="${ctx}/md/engineer/delete?id=${model.id}&pointId=${model.servicePoint.id}" onclick="return layerConfirmx('确认要停用该安维人员吗？', this.href)">停用</a>--%>
								<span class="switch-on"  style="zoom: 0.7"  onclick="enableUser('${model.id}','${model.name}','${model.contactInfo}',this)"></span>
							</c:when>
							<c:otherwise>
<%--								<a href="${ctx}/md/engineer/enable?id=${model.id}&pointId=${model.servicePoint.id}" onclick="return layerConfirmx('确认要启用该安维人员吗？', this.href)">启用</a>--%>
								<span class="switch-off"  style="zoom: 0.7"  onclick="enableUser('${model.id}','${model.name}','${model.contactInfo}',this)"></span>
							</c:otherwise>
						</c:choose>
						<input type="hidden" value="${model.delFlag}" name="delFlag" id="delFlag_${model.id}">
					</shiro:hasPermission>
					</td>
					<td>
						<shiro:hasPermission name="md:engineer:edit">
							<%--<a href="${ctx}/md/engineer/form?id=${model.id}">修改</a>--%>
							<a href="javascript:editEngineer(2,'${model.id}')">修改</a>
						</shiro:hasPermission>
						<shiro:hasPermission name="md:engineer:resetpassword">
							<a href="javascript:;" onclick="resetPassword('${model.id}');">重置密码</a>
						</shiro:hasPermission>
						<shiro:hasPermission name="md:engineer:upgrade">
							<a href="javascript:;" onclick="upgrade(${model.id});" title="升级为网点">升级</a>
						</shiro:hasPermission>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
</shiro:hasPermission>
</body>
<script class="removedscript" type="text/javascript">
	$(document).ready(function() {
		$("th").css({"text-align":"center","vertical-align":"middle"});
		$("td").css({"text-align":"center","vertical-align":"middle"});

	});
</script>
</html>
