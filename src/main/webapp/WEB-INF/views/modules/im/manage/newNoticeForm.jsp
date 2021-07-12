<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<!DOCTYPE html>
<html>
<head>
	<%@ include file="/WEB-INF/views/include/head.jsp" %>
	<title>发布通知</title>
	<meta name="decorator" content="default" />
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<script type="text/javascript">
        var clickTag = 0;
        $(document).ready(function() {
            $("#inputForm").validate({
				submitHandler : function(form)
				{
					if(isNull()){
						layerMsg('请输入通知内容');
						return false;
					}
                    if(clickTag == 1){
                        return false;
                    }
                    clickTag = 1;
                    var $btnSubmit = $("#btnSubmit");
					if ($btnSubmit.prop("disabled") == true) {
						event.preventDefault();
						return false;
					};
					$btnSubmit.prop("disabled", true);
                    loadingIndex = layer.msg('正在提交，请稍等...', {
                        icon: 16,
                        time: 0,
                        shade: 0.3
                    });
					form.submit();
				},
				errorContainer : "#messageBox",
				errorPlacement : function(error, element)
				{
					$("#messageBox").text("输入有误，请先更正。");
					if (element.is(":checkbox")
						|| element.is(":radio")
						|| element.parent().is(
							".input-append"))
					{
						error.appendTo(element.parent()
							.parent());
					} else
					{
						error.insertAfter(element);
					}
				}
            });

            $("th").css({"text-align":"left","vertical-align":"middle"});
            $("td").css({"text-align":"left","vertical-align":"middle"});
            $("td").css({"vertical-align":"middle"});
        });
	</script>
	<style type="text/css">
		.form-horizontal .control-label{width: 180px;}
		.form-horizontal .controls {margin-left:200px;*margin-left:0px;*display:block;*padding-left:20px;}
		.form-horizontal .control-group{margin-bottom: 15px;}
		.fromInput {border:1px solid #ccc;padding:4px 6px;color:#555;border-radius:4px;width: 400px;}
		.row-fluid .span4 {width: 400px;}
		.row-fluid .span6 {width: 600px;}
		.row-fluid .span7 {width: 630px;}
		.row-fluid .span8 {width: 760px;}
		.label-amout {text-align:right;width:80px;}
		legend {margin-bottom: 5px;}
	</style>
</head>

<body>
<ul class="nav nav-tabs">
	<li class="active">
		<a href="javascript:void(0);">发布通知</a>
	</li>
</ul><br>
<form:form id="inputForm" modelAttribute="notice" action="${ctx}/im/notice/manage/save" method="post" class="form-horizontal">
	<sys:message content="${message}" />
	<form:hidden path="id" />
	<c:if test="${canAction == true}">
		<div class="control-group">
			<label class="control-label">通知对象:</label>
			<div class="controls">
				<span><input type="checkbox" id="chkAllUserTypes" name="chkAllUserTypes" value="0" onclick="processCheckbox(this)"><label for="chkAllUserTypes">所有</label></span>
				<c:forEach items="${notice.userTypeList}" var="userType" varStatus="i">
					<spring:eval var="containsItem" expression="notice.userTypeValues.contains(userType.value)" />
					<span><input id="userTypeValues${i.index}" name="userTypeValues" class="required" type="checkbox" value="${userType.value}"
								 <c:if test="${containsItem}">checked="checked"</c:if>
					><label for="userTypeValues${i.index}">${userType.name}</label></span>
				</c:forEach>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label">标题:</label>
			<div class="controls">
				<form:input path="title" cssClass="fromInput required" placeholder="标题" htmlEscape="false" maxlength="100"/>
				<span class="add-on red">*</span>
			</div>
		</div>

        <div class="control-group">
            <label class="control-label">副标题:</label>
            <div class="controls">
				<form:input path="subTitle" cssClass="fromInput" placeholder="副标题" htmlEscape="false" maxlength="100"/>
            </div>
        </div>

		<div class="control-group">
			<label class="control-label">通知内容:</label>
			<div class="controls" style="width: 1000px">
				<form:textarea path="content" htmlEscape="false" rows="10" maxlength="2000" class="input-xlarge" cssStyle="min-width: 280px;max-width: 560px;min-height: 70px;max-height: 210px;width: 400px;height: 165px" placeholder=""/>
				<sys:ckeditor replace="content" uploadPath="/" />
			</div>
		</div>
	</c:if>
	<div id="formActions" class="form-actions">
		<c:if test="${canAction == true}">
			<shiro:hasPermission name="im:notice:new">
				<input id="btnSubmit" class="btn btn-primary" type="submit"
					   value="保 存" />&nbsp;</shiro:hasPermission>
		</c:if>
		<input id="btnCancel" class="btn" type="button" value="返 回"
			   onclick="history.go(-1)" />
	</div>
</form:form>
<script type="text/javascript">
    function isNull(){
        var str = contentCkeditor.document.getBody().getText();
        if ( str == "" ) return true;
        var regu = "^[ ]+$";
        var re = new RegExp(regu);
        return re.test(str);
    }

    function processCheckbox(obj) {
      if($(obj).is(":checked")){
          $.each($("[name='userTypeValues']"),function(){
			  $(this).prop("checked", true);
		  });
	  }else{
          $.each($("[name='userTypeValues']"),function(){
              $(this).prop("checked", false);
          });
	  }
    }

	$("[name='userTypeValues']").change(function (event) {
        event.preventDefault();
	    if($(this).prop("checked") == false){
	        $("#chkAllUserTypes").prop("checked", false);
		}
        return false;
    });


</script>
</body>
</html>
