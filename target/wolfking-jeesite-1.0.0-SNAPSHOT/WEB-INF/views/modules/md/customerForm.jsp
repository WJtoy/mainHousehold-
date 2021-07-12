<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
<title>客戶管理</title>
<meta name="decorator" content="default" />
<link href="${ctxStatic}/jquery-upload-file/css/uploadfile.min.css" rel="stylesheet">
<%@include file="/WEB-INF/views/include/treeview.jsp"%>
<script src="${ctxStatic}/js/ajaxfileupload.js"></script>
<style type="text/css">
.imgfile {
	max-width: 300px;
	max-height: 300px;
}

.logo_image {
	height: 150px;
	width: 150px;
}
</style>

	<script type="text/javascript">
		$(document).ready(function() {

			$("#btnApprove").click(function(){
				top.$.jBox.confirm("确认保存并通过？","系统提示",function(v,h,f){
					if(v=="ok"){
						$("#inputForm").attr("action","${ctx}/md/customer/saveAndApprove");
						$("#inputForm").submit();
					}
				},{buttonsFocus:1});
				top.$('.jbox-body .jbox-icon').css('top','55px');
			});
			$("#btnSubmit").click(function(){
				$btnSubmit = $("#btnSubmit");
				if($btnSubmit.prop("disabled") == true){
					return false;
				}
				$btnSubmit.attr("disabled", "disabled");
				$("#inputForm").attr("action","${ctx}/md/customer/save");
				$("#inputForm").submit();
				$btnSubmit.removeAttr('disabled');
			});

			$("#btnUpdateSales").click(function(){
                 top.layer.open({
                    type: 2,
                    id:'layer_salesUpdate',
                    zIndex:19891015,
                    title:'业务员变更',
                    content:'${ctx}/md/customer/salesChangeForm?id='+($("#id").val() ||''),
                    shade: 0.3,
                    shadeClose: true,
                    area: ['550px', '350px'],
                    maxmin: false,
                    success: function(layero){},
                    end: function(){}
                });
			});

			$("#btnUpdateMerchandiser").click(function(){
				top.layer.open({
					type: 2,
					id:'layer_merchandiserUpdate',
					zIndex:19891016,
					title:'跟单员变更',
					content:'${ctx}/md/customer/merchandiserChangeForm?id='+($("#id").val() ||''),
					shade: 0.3,
					shadeClose: true,
					area: ['550px', '350px'],
					maxmin: false,
					success: function(layero){},
					end: function(){}
				});
			});

			$("#code").focus();
			$("#inputForm").validate({
				rules: {
					code: {remote: "${ctx}/md/customer/checkLoginName?oldLoginName=" + encodeURIComponent('${customer.code}')},
					'finance.credit': { min: 0, max : 10000000},
                    'finance.deposit': { min: 0, max : 1000000},
				},
				messages: {
					code: {remote: "客户已存在"},
                    'finance.credit': { min: "信用额度不能低于0.",max : "信用额度不能超过一千万."},
                    'finance.deposit': { min: "押金不能低于0.",max : "押金不能超过一百万."}
				},
				submitHandler: function(form){
				    //check mobile
					var id = $("#id").val();
					var masterphone = $("#masterPhone").val();//修改前负责人手机号
					var phone = $("#phone").val();
                    var data = {id: id||'0',phone:phone};
                    var checkPhone = true;
                    if(!Utils.isEmpty(id) && masterphone == phone){
                        checkPhone = false;
                    }
                    if(checkPhone == true) {
                        $.ajax(
                            {
                                cache: false,
                                type: "POST",
                                url: "${ctx}/md/customer/account/checkMasterPhone",
                                data: data,
                                success: function (data) {
                                    if(ajaxLogout(data)){
                                        return false;
                                    }
                                    //手机号已注册
                                    if (data.success==false) {
                                        layerAlert(data.message, "系统提示");
                                        return;
                                    }else{
                                        submitForm(form);
									}
                                },
                                error: function (e) {
                                    ajaxLogout(e.responseText,null,"检查负责人手机号是否注册错误，请重试!");
                                    //layerError("检查负责人手机号是否注册失败:" + e, "错误提示");
                                    return;
                                }
                            });
                    }else{
                        submitForm(form);
                    }

				},
				errorContainer: "#messageBox",
				errorPlacement: function(error, element) {
				    top.$.jBox.closeTip();
					$("#messageBox").text("输入有误，请先更正。");
					if (element.is(":checkbox")||element.is(":radio")||element.parent().is(".input-append")){
						error.appendTo(element.parent().parent());
					} else {
						error.insertAfter(element);
					}
				}
			});

			function submitForm(form){
                var ids = [], nodes = ptree.getCheckedNodes(true);
                for(var i=0; i<nodes.length; i++) {
                    if(nodes[i].level >0){
                        ids.push(nodes[i].id);
                    }
                }
                if(ids.length==0){
                    top.$.jBox.info("请选择客户负责的产品","信息提示");
                    return false;
                }
                $("#productIds").val(ids);
				layerLoading('正在提交，请稍等...',true);
                form.submit();
            }

			// 产品
			setting = {
				check: {
					enable: true,
					nocheckInherit: true
				},
				data: {
					simpleData: {
						enable: true
					}
				}
			};
			var zNodes = [
				<c:forEach items="${fns:getProductCategories()}" var="cat">
				<c:if test="${cat.name ne '烟机'}">
				{id: 'p_${cat.id}', pId: '0', name: "${cat.name}"},
				</c:if>
				</c:forEach>
					<c:forEach items="${fns:getProducts()}" var="prod">{
					id: '${prod.id}',
					pId: 'p_${prod.category.id}',
					name: "${prod.name}"
				},
				</c:forEach>];
			// 初始化树结构
			var ptree = $.fn.zTree.init($("#productTree"), setting, zNodes);


			// 默认选择节点
			var ids = "${productIds}".split(",");
			for(var i=0; i<ids.length; i++) {
				var node = ptree.getNodeByParam("id", ids[i]);
				try{ptree.checkNode(node, true, true,false);}catch(e){}
			}
			// 默认展开全部节点
			ptree.expandAll(true);



			// Logo
			if($("#logo").val().length>0)
			{
				$("#logo_image").attr("src","${ctxUpload}/"+$("#logo").val()+"?t="+Math.random());
			}
			$("#buttonUploadLogo").click(function() {
				uploadfile($("#logo"),$("#logo_image"), "fileToUploadlogo");
				return false;
			});

			//show the attachment image when it is update
			if($("#attachment1").val().length>0)
			{
				$("#attachment1_image").attr("src","${ctxUpload}/"+$("#attachment1").val()+"?t="+Math.random());
			}
			if($("#attachment2").val().length>0)
			{
				$("#attachment2_image").attr("src","${ctxUpload}/"+$("#attachment2").val()+"?t="+Math.random());
			}
			if($("#attachment3").val().length>0)
			{
				$("#attachment3_image").attr("src","${ctxUpload}/"+$("#attachment3").val()+"?t="+Math.random());
			}
			if($("#attachment4").val().length>0)
			{
				$("#attachment4_image").attr("src","${ctxUpload}/"+$("#attachment4").val()+"?t="+Math.random());
			}


			$("#buttonUpload1").click(function() {
				uploadfile($("#attachment1"),$("#attachment1_image"), "fileToUpload1");
				return false;
			});
			$("#buttonUpload2").click(function() {
				uploadfile($("#attachment2"),$("#attachment2_image"), "fileToUpload2");
				return false;
			});
			$("#buttonUpload3").click(function() {
				uploadfile($("#attachment3"),$("#attachment3_image"), "fileToUpload3");
				return false;
			});
			$("#buttonUpload4").click(function() {
				uploadfile($("#attachment4"),$("#attachment4_image"), "fileToUpload4");
				return false;
			});

		});

		function uploadfile($obj1,$obj1_image, obj2) {
			var data = {
				fileName : $obj1.val()
			};
			$.ajaxFileUpload({
				url : '${pageContext.request.contextPath}/servlet/Upload?fileName=' + $obj1.val(),//处理图片脚本
				secureuri : false,
				data : data,
				fileElementId : obj2,//file控件id
				dataType : 'json',
				success : function(data, status) {
                    if(ajaxLogout(data)){
                        return false;
                    }
					$obj1.val(data.fileName);
					$obj1_image.show();
					$obj1_image.attr("src","${ctxUpload}/"+data.fileName+"?t="+Math.random());
				},
				error : function(e) {
					//alert(e);
                    ajaxLogout(e.responseText,null,"上传失败，请重试!");
				}
			});
		}

        //refresh this page
        function repage(id, name) {
            $("#sales\\.name").val(name);
            $("#sales\\.id").val(id);
        }

		function reMerchandiserPage(id, name) {
			$("#merchandiser\\.name").val(name);
			$("#merchandiser\\.id").val(id);
		}
	</script>
</head>
<body>
	<ul class="nav nav-tabs">
		<shiro:hasPermission name="md:customer:view">
		<li><a href="${ctx}/md/customer">客戶列表</a></li>
		</shiro:hasPermission>
		<li class="active">
			<a href="javascript:;">客戶<shiro:hasPermission name="md:customer:edit">${not empty customer.id?'修改':'添加'}</shiro:hasPermission>
			<shiro:lacksPermission name="md:customer:edit">查看</shiro:lacksPermission>
		</a>
		</li>
		<%--<shiro:hasPermission name="md:customer:edit">--%>
			<%--<li><a href="${ctx}/md/customer/approvelist">客户审核</a>--%>
			<%--</li>--%>
		<%--</shiro:hasPermission>--%>
	</ul>
	<br />
	<c:set var="currentuser" value="${fns:getUser() }" />
	<form:form id="inputForm" modelAttribute="customer" action="${ctx}/md/customer/save" method="post" class="form-horizontal">
		<form:hidden path="id" />
		<form:hidden path="finance.balance" />
		<form:hidden path="materialFlag"/>
		<form:hidden path="errorFlag"/>
		<form:hidden path="vip"/>
		<input type="hidden" id="masterPhone" value="${customer.phone}" />
		<sys:message content="${message}" />
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">Logo:</label>
					<div class="controls">
						<img id="logo_image" class="logo_image" alt="" />
						<input name="logo.filePath" id="logo" type="hidden" value="${customer.logo.filePath}" htmlEscape="false" />
						<form:hidden path="logo.id" />
						<input id="fileToUploadlogo" type="file" size="20" name="fileToUploadlogo" class="input">
						<button id="buttonUploadLogo" type="button" class="btn">上传</button>

					</div>
				</div>
			</div>
		</div>
		<legend>基本信息</legend>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">代码:</label>
					<div class="controls">
						<input id="oldCode" name="oldCode" type="hidden" value="${customer.code}">
						<input id="oldCredit" name="oldCredit" type="hidden" value="${customer.finance.credit}">
						<input type="text" id="txtcode" name="txtcode" value="${customer.code}" placeholder="自动生成,如C0001" maxlength="6" disabled="true" />

					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">名称:</label>
					<div class="controls">
						<form:input path="name" htmlEscape="false" maxlength="30" class="required " />
						<span class="red">*</span>
					</div>
				</div>
			</div>
		</div>

		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">全称:</label>
					<div class="controls">
						<form:input path="fullName" htmlEscape="false" maxlength="60" class="required" style="width:360px;" />
						<span class="red">*</span>
					</div>
				</div>
			</div>
		</div>

		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">负责人:</label>
					<div class="controls">
						<form:input path="master" htmlEscape="false" class="required" maxlength="20" />
						<span class="red">*</span>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">负责人手机:</label>
					<div class="controls">
						<form:input path="phone" type="tel" htmlEscape="false" class="required mobile" maxlength="11" />
						<span class="red">*</span>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">业务员:</label>
					<c:choose>
						<c:when test="${empty customer.id}">
							<div class="controls">
								<form:select path="sales.id" cssClass="required input-medium" cssStyle="width: 220px;">
									<form:options items="${fns:getSaleList()}" itemLabel="name" itemValue="id" htmlEscape="false" />
								</form:select>
								<span class="red">*</span>
							</div>
						</c:when>
						<c:otherwise>
							<div class="controls">
								<form:input path="sales.name" htmlEscape="false" maxlength="20" readonly="true"/>
								<form:hidden path="sales.id" />
								<input id="btnUpdateSales" class="btn btn-success" type="button" value="业务员变更" />
							</div>
						</c:otherwise>
					</c:choose>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">跟单员:</label>
					<c:choose>
						<c:when test="${empty customer.id}">
							<div class="controls">
								<form:select path="merchandiser.id" cssClass="input-medium" cssStyle="width: 220px;">
									<form:options items="${fns:getMerchandiserList()}" itemLabel="name" itemValue="id" htmlEscape="false" />
								</form:select>
							</div>
						</c:when>
						<c:otherwise>
							<div class="controls">
								<form:input path="merchandiser.name" htmlEscape="false" maxlength="20" readonly="true"/>
								<form:hidden path="merchandiser.id" />
								<input id="btnUpdateMerchandiser" class="btn btn-success" type="button" value="跟单员变更" />
							</div>
						</c:otherwise>
					</c:choose>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">邮编:</label>
					<div class="controls">
						<form:input path="zipCode" htmlEscape="false" maxlength="6" class="number" />
					</div>
				</div>
			</div>

			<div class="span6">
				<div class="control-group">
					<label class="control-label">邮箱:</label>
					<div class="controls">
						<form:input path="email" type="email" htmlEscape="false" maxlength="60" />
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">地址:</label>
					<div class="controls">
						<form:input path="address" htmlEscape="false" maxlength="100" />
					</div>
				</div>
			</div>

			<div class="span6">
				<div class="control-group">
					<label class="control-label">传真:</label>
					<div class="controls">
						<form:input path="fax" htmlEscape="false" type="tel" maxlength="15" />
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">签约日期:</label>
					<div class="controls">
						<input id="contractDate" name="contractDate" type="text"
							   readonly="readonly" style="width:95px;margin-left:4px"
							   maxlength="10" class="input-small Wdate"
							   value="<fmt:formatDate value='${customer.contractDate}' pattern='yyyy-MM-dd'/>"
							   onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:false});" />
					</div>
				</div>
				<%--<div class="control-group">--%>
					<%--<label class="control-label">默认品牌:</label>--%>
					<%--<div class="controls">--%>
						<%--<form:input path="defaultBrand" htmlEscape="false" maxlength="100" />--%>
						<form:hidden path="defaultBrand"/>
						<%--<span class="help-inline">多个品牌之间用逗号分隔</span>--%>
					<%--</div>--%>
				<%--</div>--%>
			</div>

			<div class="span6">
				<div class="control-group">
					<label class="control-label">备注:</label>
					<div class="controls">
						<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="255" />
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">最小上传数:</label>
					<div class="controls">
						<form:input path="minUploadNumber" htmlEscape="false" type="number" cssclass="{required:true,min:0,max:20}" />
						<span class="red">*</span>
					</div>
				</div>
			</div>

			<div class="span6">
				<div class="control-group">
					<label class="control-label">最大上传数:</label>
					<div class="controls">
						<form:input path="maxUploadNumber" htmlEscape="false" type="number" cssclass="{required:true,min:0,max:20}"/>
						<span class="red">*</span>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span10">
				<div class="control-group">
					<label class="control-label">返件地址:</label>
					<div class="controls">
						<form:input path="returnAddress" htmlEscape="false" cssClass="required" maxlength="100" style="width:665px;"/>
						<span class="red">*</span>
					</div>
				</div>
			</div>

		</div>
		<legend>控制</legend>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">客户可下单:</label>
					<div class="controls">
						<form:radiobutton path="effectFlag" value="1"></form:radiobutton>
						可下单
						<form:radiobutton path="effectFlag" value="0"></form:radiobutton>
						不可下单
						<span class="red">*</span>
						<span class="help-inline">只控制客户帐号，本公司帐号不做限定</span>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">短信发送:</label>
					<div class="controls">
						<form:radiobutton path="shortMessageFlag" value="1"></form:radiobutton>
						发送
						<form:radiobutton path="shortMessageFlag" value="0"></form:radiobutton>
						不发送
						<span class="red">*</span>
						<span class="help-inline">1.派单短信：已经安排师傅短信
2.预约时间短信
3.完工后发给用户短信，让用户短信回复客评</span>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">时效奖励开关:</label>
					<div class="controls">
						<shiro:hasPermission name="md:customer:timeliness">
						<form:radiobutton path="timeLinessFlag" value="1"></form:radiobutton>
						开启
						<form:radiobutton path="timeLinessFlag" value="0"></form:radiobutton>
						关闭
						</shiro:hasPermission>
						<shiro:lacksPermission name="md:customer:timeliness">
							<c:choose>
								<c:when test="${customer.timeLinessFlag eq 1}">
									<form:radiobutton path="timeLinessFlag" value="1"></form:radiobutton>
									开启
								</c:when>
								<c:otherwise>
									<form:radiobutton path="timeLinessFlag" value="0"></form:radiobutton>
									关闭
								</c:otherwise>
							</c:choose>
						</shiro:lacksPermission>
						<span class="red">*</span>
						<span class="help-inline"></span>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">加急开关:</label>
					<div class="controls">
						<shiro:hasPermission name="md:customer:urgent">
						<form:radiobutton path="urgentFlag" value="1"></form:radiobutton>
						开启
						<form:radiobutton path="urgentFlag" value="0"></form:radiobutton>
						关闭
						</shiro:hasPermission>
						<shiro:lacksPermission name="md:customer:urgent">
							<c:choose>
								<c:when test="${customer.urgentFlag eq 1}">
									<form:radiobutton path="urgentFlag" value="1"></form:radiobutton>
									开启
								</c:when>
								<c:otherwise>
									<form:radiobutton path="urgentFlag" value="0"></form:radiobutton>
									关闭
								</c:otherwise>
							</c:choose>
						</shiro:lacksPermission>
						<span class="red">*</span>
						<span class="help-inline"></span>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">能否催单:</label>
					<div class="controls">
						<form:radiobutton path="reminderFlag" value="1"></form:radiobutton>
						能
						<form:radiobutton path="reminderFlag" value="0"></form:radiobutton>
						不能
						<%--
						<shiro:hasPermission name="md:customer:remiderFlag">
							<form:radiobutton path="remiderFlag" value="1"></form:radiobutton>
							能
							<form:radiobutton path="remiderFlag" value="0"></form:radiobutton>
							否
						</shiro:hasPermission>
						<shiro:lacksPermission name="md:customer:remiderFlag">
							<c:choose>
								<c:when test="${customer.remiderFlag eq 1}">
									<form:radiobutton path="remiderFlag" value="1"></form:radiobutton>
									能
								</c:when>
								<c:otherwise>
									<form:radiobutton path="remiderFlag" value="0"></form:radiobutton>
									否
								</c:otherwise>
							</c:choose>
						</shiro:lacksPermission>
						--%>
						<span class="red">*</span>
						<span class="help-inline"></span>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">VIP客户:</label>
					<div class="controls">
						<span>
							<form:radiobutton path="vipFlag" value="1" cssClass="required"></form:radiobutton>是
						</span>
						<span>
							<form:radiobutton path="vipFlag" value="0" cssClass="required"></form:radiobutton>否
						</span>
						<span class="red">*</span>
						<span class="help-inline"></span>
					</div>
				</div>
			</div>
		</div>
		<legend>职务联系</legend>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">项目负责人:</label>
					<div class="controls">
						<form:input path="projectOwner" htmlEscape="false" maxlength="10" class="required" />
						<span class="red">*</span>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">电话:</label>
					<div class="controls">
						<form:input path="projectOwnerPhone" htmlEscape="false" maxlength="16" class="required phone" />
						<span class="red">*</span>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span8">
				<div class="control-group">
					<label class="control-label">QQ:</label>
					<div class="controls">
						<form:input path="projectOwnerQq" htmlEscape="false" maxlength="11" />
					</div>
				</div>
			</div>

		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">售后负责人:</label>
					<div class="controls">
						<form:input path="serviceOwner" htmlEscape="false" maxlength="10" class="required" />
						<span class="red">*</span>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">电话:</label>
					<div class="controls">
						<form:input path="serviceOwnerPhone" htmlEscape="false" maxlength="16" class="required phone" />
						<span class="red">*</span>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span8">
				<div class="control-group">
					<label class="control-label">QQ:</label>
					<div class="controls">
						<form:input path="serviceOwnerQq" htmlEscape="false" maxlength="11" />
					</div>
				</div>
			</div>

		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">财务负责人:</label>
					<div class="controls">
						<form:input path="financeOwner" htmlEscape="false" maxlength="10" />
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">电话:</label>
					<div class="controls">
						<form:input path="financeOwnerPhone" class="phone" htmlEscape="false" maxlength="16" />
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span8">
				<div class="control-group">
					<label class="control-label">QQ:</label>
					<div class="controls">
						<form:input path="financeOwnerQq" htmlEscape="false" maxlength="11" />
					</div>
				</div>
			</div>

		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">技术负责人:</label>
					<div class="controls">
						<form:input path="technologyOwner" htmlEscape="false" maxlength="10" />
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">电话:</label>
					<div class="controls">
						<form:input path="technologyOwnerPhone" class="phone" htmlEscape="false" maxlength="16" />
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span8">
				<div class="control-group">
					<label class="control-label">QQ:</label>
					<div class="controls">
						<form:input path="technologyOwnerQq" htmlEscape="false" maxlength="11" />
					</div>
				</div>
			</div>
		</div>

		<legend>财务信息</legend>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">是否开票:</label>
						<div class="controls">
							<form:radiobutton path="finance.invoiceFlag" value="1"></form:radiobutton>
							开票
							<form:radiobutton path="finance.invoiceFlag" value="0"></form:radiobutton>
							不开票
							<span class="red">*</span>
							<span class="help-inline"></span>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">结算方式:</label>
					<div class="controls">
						<form:select path="finance.paymentType.value" cssClass="required input-medium" cssStyle="width: 220px;">
							<form:options items="${fns:getDictExceptListFromMS('PaymentType', '20')}"
										  itemLabel="label" itemValue="value" htmlEscape="false" /><%--切换为微服务--%>
						</form:select>
						<span class="red">*</span>
					</div>
				</div>
			</div>

		</div>
		<shiro:hasPermission name="sd:customerinvoice:edit">
			<div class="row-fluid">
				<div class="span6">
					<div class="control-group" style="color: red;">
						<label class="control-label"> 结账锁:</label>
						<div class="controls">
							<form:radiobutton path="finance.lockFlag" value="1"></form:radiobutton>
							加锁
							<form:radiobutton path="finance.lockFlag" value="0"></form:radiobutton>
							不加锁
						</div>
					</div>
				</div>
			</div>
		</shiro:hasPermission>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">账户金额:</label>
					<div class="controls">
						<span class="input-large uneditable-input">${customer.finance.balance}</span>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">押金:</label>
					<div class="controls">
						<shiro:hasPermission name="md:customer:deposit">
							<form:input path="finance.deposit" htmlEscape="false" type="number"
										maxlength="7" class="required number" />
							<span class="red">*</span>
						</shiro:hasPermission>
						<shiro:lacksPermission name="md:customer:deposit">
							<form:input path="finance.deposit" htmlEscape="false" readonly="true" />
							<span class="red">*</span>
						</shiro:lacksPermission>
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">信用额度:</label>
					<div class="controls">
						<shiro:hasPermission name="md:customer:deposit">
							<form:input path="finance.credit" htmlEscape="false" maxlength="10" class="required number"/>
						</shiro:hasPermission>
						<shiro:lacksPermission name="md:customer:deposit">
							<form:input path="finance.credit" htmlEscape="false" readonly="true"/>
						</shiro:lacksPermission>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">使用信用额度:</label>
					<div class="controls">
						<form:radiobutton path="finance.creditFlag" value="1"></form:radiobutton>
						使用
						<form:radiobutton path="finance.creditFlag" value="0"></form:radiobutton>
						不使用
					</div>
				</div>
			</div>

		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">使用参考价格:</label>
					<div class="controls">
						<form:select path="useDefaultPrice" class="required input-small" cssStyle="width: 220px;">
							<form:option value="0" label="不使用"/>
							<form:options items="${fns:getDictExceptListFromMS('PriceType', '20,30')}" itemLabel="label" itemValue="value" htmlEscape="false" />
						</form:select>
					</div>
				</div>
			</div>
				<div class="span6">
					<div class="control-group">
						<label class="control-label">纳税人代码:</label>
						<div class="controls">
							<form:input path="finance.taxpayerCode" htmlEscape="false" maxlength="64" />
						</div>
					</div>
				</div>
			</div>
		</div>
		<c:set var="bankTypeList" value="${fns:getDictListFromMS('banktype')}"/>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">对公开户行:</label>
					<div class="controls">
						<form:select path="finance.publicBank" cssClass="input-medium" cssStyle="width: 220px;">
							<form:option value="" label="请选择" />
							<form:options items="${bankTypeList}"
										  itemLabel="label" itemValue="value" htmlEscape="false" /><%--切换为微服务--%>
						</form:select>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">分行:</label>
					<div class="controls">
						<form:input path="finance.publicBranch" htmlEscape="false" maxlength="64" />
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">卡号:</label>
					<div class="controls">
						<form:input path="finance.publicAccount" htmlEscape="false" maxlength="64" />
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">开户人:</label>
					<div class="controls">
						<form:input path="finance.publicName" htmlEscape="false" maxlength="64" />
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">对私开户行:</label>
					<div class="controls">
						<form:select path="finance.privateBank" cssClass="input-medium" cssStyle="width: 220px;">
							<form:option value="" label="请选择" />
							<form:options items="${bankTypeList}"
										  itemLabel="label" itemValue="value" htmlEscape="false" /><%--切换为微服务--%>
						</form:select>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">分行:</label>
					<div class="controls">
						<form:input path="finance.privateBranch" htmlEscape="false" maxlength="64" />
					</div>
				</div>
			</div>
		</div>
		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">卡号:</label>
					<div class="controls">
						<form:input path="finance.privateAccount" htmlEscape="false"
									maxlength="64" />
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">开户人:</label>
					<div class="controls">
						<form:input path="finance.privateName" htmlEscape="false" maxlength="64" />
					</div>
				</div>
			</div>
		</div>

		<div class="row-fluid">
			<div class="span6">
				<div class="control-group">
					<label class="control-label">等级:</label>
					<div class="controls">
						<form:select path="finance.level" cssClass="required input-medium" cssStyle="width: 220px;">
							<form:options items="${fns:getDictListFromMS('customerlevel')}"
										  itemLabel="label" itemValue="value" htmlEscape="false" /><%-- 切换为微服务 --%>
						</form:select>
						<span class="red">*</span>
					</div>
				</div>
			</div>
			<div class="span6">
				<div class="control-group">
					<label class="control-label">返点比率(%):</label>
					<div class="controls">
						<form:select path="finance.rebateRate" cssClass="required input-medium" cssStyle="width: 220px;">
							<form:options items="${fns:getDictListFromMS('rebaterate')}"
										  itemLabel="label" itemValue="value" htmlEscape="false" /><%--切换为微服务--%>
						</form:select>
						<span class="red">*</span>
					</div>
				</div>
			</div>
		</div>

		<legend>证件上传</legend>
		<div class="control-group">
			<label class="control-label">合 同:</label>
			<div class="controls">
				<input name="attachment1.filePath" id="attachment1" type="hidden" value="${customer.attachment1.filePath}" htmlEscape="false" />
				<form:hidden path="attachment1.id" />
				<input id="fileToUpload1" type="file" size="20" name="fileToUpload1" class="input">
				<button id="buttonUpload1" type="button" class="btn">上传</button>
				<img id="attachment1_image" class="imgfile" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">个人身份证:</label>
			<div class="controls">
				<input id="attachment2" name="attachment2.filePath" type="hidden" htmlEscape="false" value="${customer.attachment2.filePath}" maxlength="50" />
				<form:hidden path="attachment2.id" />
				<input id="fileToUpload2" type="file" size="20" name="fileToUpload2" class="input">
				<button id="buttonUpload2" type="button" class="btn">上传</button>
				<img id="attachment2_image" class="imgfile" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">其他证件1:</label>
			<div class="controls">
				<input id="attachment3" name="attachment3.filePath" type="hidden" htmlEscape="false" value="${customer.attachment3.filePath}" maxlength="50" />
				<form:hidden path="attachment3.id" />
				<input id="fileToUpload3" type="file" size="20" name="fileToUpload3" class="input">
				<button id="buttonUpload3" type="button" class="btn">上传</button>
				<img id="attachment3_image" class="imgfile" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">其他证件2:</label>
			<div class="controls">
				<input id="attachment4" name="attachment4.filePath" type="hidden" htmlEscape="false" value="${customer.attachment4.filePath}" maxlength="50" />
				<form:hidden path="attachment4.id" />
				<input id="fileToUpload4" type="file" size="20" name="fileToUpload4" class="input">
				<button id="buttonUpload4" type="button" class="btn">上传</button>
				<img id="attachment4_image" class="imgfile" />
			</div>
		</div>

		<legend>产品信息</legend>
		<div class="row-fluid">
			<div id="productTree" class="ztree"
				 style="margin-top:3px;float:left;height:400px;width:350px;overflow:auto;"></div>
			<form:hidden path="productIds" />
		</div>

		<div class="row-fluid">
			<div class="span8">
				<div class="control-group">
					<label class="control-label"></label>
					<div class="controls">
						<shiro:hasPermission name="md:customer:edit">
							<input id="btnSubmit" class="btn btn-primary" type="button"
								   value="保 存" />&nbsp;</shiro:hasPermission>

						<input style="margin-left: 20px" id="btnCancel" class="btn"
							   type="button" value="返 回" onclick="history.go(-1)" />

						<c:if test="${not empty customer.id && customer.delFlag eq '2' }">
							<input style="margin-left: 20px" id="btnApprove"
								   class="btn btn-primary" type="button" value="保存并通过审核" />
						</c:if>

					</div>
				</div>
			</div>
		</div>

	</form:form>
</body>
</html>