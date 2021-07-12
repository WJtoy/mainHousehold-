<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>故障列表</title>
    <meta name="decorator" content="default" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@include file="/WEB-INF/views/include/treetable.jsp"%>
    <%@include file="/WEB-INF/views/include/treeview.jsp" %>
    <c:set var="currentuser" value="${fns:getUser()}"/>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center !important;
            vertical-align: middle;
            BackColor: Transparent;
            height: 30px;
        }
        #editBtn {
            position: fixed;
            left: 0px;
            bottom: 0;
            width: 100%;
            height: 60px;
            background: #fff;
            z-index: 10;
            padding-left: 190px;
            border-top: 1px solid #e5e5e5;
        }

    </style>
    <script type="text/javascript">
        var this_index = top.layer.index;
        function cancel() {
            top.layer.close(this_index);// 关闭本身
        }
    </script>


</head>

<body>

<form:form id="inputForm" action="${ctx}/md/customerProductType/customerActionList" method="post" class="form-horizontal" cssStyle="margin-left: 0px;width: 100%">
    <sys:message content="${message}" />

    <ipunt type="hidden" value="${customerId}" id="customerId"></ipunt>

    <div style="margin-top:24px; height: 50px;">
        <div class="row-fluid" style="margin-left: 24px;width: 80%">
            <div class="span6" style="width: 355px;">
                <label class="control-label" style="width: 100px;">客户产品分类：</label>
                <div class="controls" style="margin-left: 80px;">
                    <input type="hidden" value="${customerProductTypeId}" id="customerProductTypeId">
                    <input id="customerProductTypeName" name="customerProductTypeName" style="width:237px;" readonly="readonly" type="text" value="${customerProductTypeName}" class="valid" aria-invalid="false">
                </div>
            </div>
        </div>
    </div>


</form:form>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="100px">故障分类</th>
        <th width="100px">故障现象</th>
        <th width="100px">故障分析</th>
        <th width="100px">故障处理</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${mdCustomerActionList}" var="entity">
      <tr>
          <td>${entity.errorTypeName}</td>
          <td>${entity.errorAppearanceName}</td>
          <td>${entity.errorAnalysisName}</td>
          <td>${entity.errorProcess}</td>
      </tr>
    </c:forEach>
    </tbody>
</table>
<div style="height: 40px;float: left"></div>
<div id="editBtn"  class="line-row" style="width: 90%;">
        <input id="btnSubmit1" class="btn " type="button" onclick="javascript:cancel();" value="关闭" style="margin-left: 690px;width: 96px;height: 40px;margin-top: 10px;"/>
</div>

</body>
</html>

