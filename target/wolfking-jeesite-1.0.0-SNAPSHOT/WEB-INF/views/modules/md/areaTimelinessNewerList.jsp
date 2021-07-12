<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>区域时效奖励开关</title>
    <meta name="decorator" content="default"/>
    <%@include file="/WEB-INF/views/include/treetable.jsp" %>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <style type="text/css">
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
        }
    </style>

    <script type="text/javascript">
        function validate() {
            var areaId = $("#areaId").val();
            if (areaId == null || areaId=='') {
                layerError("请选择区域", "错误提示");
                return false;
            }
            layerLoading("查询中...", true);
            $("#searchForm").submit();
        }
        var $btnSubmit = $("#btnSubmit");
        $(document).ready(function () {
            $("[name^='selectAll']:checkbox").on("click", function () {
                var productCategoryId = $(this).data("categoryid");
                var checkedValue = $(this).attr("checked") || "";
                if (checkedValue == "") {
                    $(":checked[name='checkedRecords-"+productCategoryId+"']").removeAttr("checked");
                } else {
                    $(":checkbox[name='checkedRecords-"+productCategoryId+"']").attr("checked", "checked");
                }
            });

            $(":checkbox[name^='checkedRecords']").on("click", function () {
                var productCategoryId = $(this).data("categoryid");

                if ($(":checkbox[name='checkedRecords-"+productCategoryId+"']").length != $(":checked[name='checkedRecords-"+productCategoryId+"']").length) {
                    $("[name='selectAll-"+productCategoryId+"']").removeAttr("checked");
                } else {
                    $("[name='selectAll-"+productCategoryId+"']").attr("checked", "checked");
                }
            });

            var clickTag = 0;
            $("#btnSubmit").click(function () {

                if (clickTag == 1) {
                    return false;
                }

                /* if(!$("#inputForm").valid()){
                     return false;
                 }*/

                var areaId = $("#areaId").val();

                if (areaId == null || areaId=='') {
                    layerError("请选中区域", "错误提示");
                    clickTag = 0;
                    $btnSubmit.removeAttr('disabled');
                    return false;
                }
                var entity = [];
                $("input[name^='checkedRecords']:checkbox").each(function () {
                    var id = $(this).data("id");
                    var area = {};
                    area.id = $(this).data("area-id");
                    var productCategoryId = $(this).data("categoryid");
                    var openChecked = $(this).is(":checked");
                    var isOpen = 1;
                    isOpen = openChecked?1:0;
                    if (id != null && id > 0) {
                        entity.push({id: id, area: area, isOpen: isOpen, productCategoryId: productCategoryId})
                    } else {
                        entity.push({area: area, isOpen: isOpen, productCategoryId: productCategoryId})
                    }
                });


                top.$.jBox.confirm("确认保存数据吗？", "系统提示", function (v, h, f) {
                    if (v == "ok") {
                        top.$.jBox.tip('请稍候...', 'loading');
                        clickTag = 1;
                        $btnSubmit.attr('disabled', 'disabled');
                        var loadingIndex;
                        $.ajax({
                            cache: false,
                            type: "POST",
                            url: "${ctx}/md/areaTimelinessNew/save",
                            data: JSON.stringify(entity),
                            dataType: 'json',
                            contentType: "application/json;charset=utf-8",
                            success: function (data) {
                                layer.close(loadingIndex);
                                if (ajaxLogout(data)) {
                                    $obj.removeAttr('disabled');
                                    return false;
                                }
                                if (data.success) {
                                    layerMsg("保存成功");
                                    repage();
                                } else {
                                    setTimeout(function () {
                                        $btnSubmit.removeAttr('disabled');
                                    }, 2000);
                                    layerError("数据保存错误:" + data.message, "错误提示");
                                    top.$.jBox.closeTip();
                                }
                                clickTag = 0;
                            },
                            error: function (data) {
                                setTimeout(function () {
                                    $btnSubmit.removeAttr('disabled');
                                }, 2000);
                                ajaxLogout(data, null, "数据保存错误，请重试!");
                                top.$.jBox.closeTip();
                                clickTag = 0;
                            }
                        });
                    }
                }, {buttonsFocus: 1});
                top.$('.jbox-body .jbox-icon').css('top', '55px');
            });
        });
    </script>
    <style type="text/css">
        .col_product {width: 250px;}
    </style>
</head>
<body>
<ul class="nav nav-tabs">
    <li>
        <a href="${ctx}/md/timelinessPriceNew/list">补贴金额</a>
    </li>
    <li class="active"><a href="javascript:void(0);">区域设置</a></li>
    <li><a href="${ctx}/md/servicepoint/findServicePointTimelinessList">网点设置</a></li>
    <li><a href="${ctx}/md/servicepoint/servicePointAreaTimelinessList">网点批量设置</a></li>
</ul>
<form:form id="searchForm" modelAttribute="areaTimeLiness" action="${ctx}/md/areaTimelinessNew/list" method="post"
           class="breadcrumb form-search">
    <div>
        <input type="hidden" name="isSearching" value="${areaTimeLiness.isSearchingYes}"/>
        <span class=" red">*</span>
        <label style="margin-left: 0px">区&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;域：</label>
            <%--<select id="area.id" name="area.id" class="input-small" style="width:200px;">
                <option value="0" <c:out value="${(empty areaTimeLiness.area.id)?'selected=selected':''}"/>>所有
                </option>
                &lt;%&ndash;<c:forEach items="${provinceList}" var="dict">
                    <option value="${dict.id}" <c:out
                            value="${(areaTimeLiness.area.id eq dict.id)?'selected=selected':''}"/>>${dict.name}</option>
                </c:forEach>&ndash;%&gt;
            </select>--%>
        <sys:newprovinceeareaselect id="area" name="area.id" value="${areaTimeLiness.area.id}"
                                    labelValue="${areaTimeLiness.area.name}" labelName="area.name"
                                    title="区域" mustSelectCounty="false" cssClass="required"> </sys:newprovinceeareaselect>
        &nbsp;&nbsp;
        <input id="btnSeach" class="btn btn-primary" type="button" onclick="validate()" value="查询"/>
        <shiro:hasPermission name="md:timelinessprice:edit">
            &nbsp;<a class="btn btn-primary" id="btnSubmit"><i class="icon-save icon-white"></i>&nbsp;保存列表</a>
        </shiro:hasPermission>
    </div>
</form:form>
<sys:message content="${message}"/>
<table id="treeTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th rowspan="2">序号</th>
        <th rowspan="2"><label class="col_product">市</label></th>
        <th colspan="${fn:length(productCategoryList)}" >时效奖励</th>
    </tr>
    <tr>
        <c:forEach items="${productCategoryList}" var="productCategory">
            <th><label><input name="selectAll-${productCategory.id}" type="checkbox"  data-categoryid="${productCategory.id}" />${productCategory.name}</label></th>
        </c:forEach>
    </tr>
    </thead>
    <%--
    <thead>
    <tr>
        <th width="10">序号</th>
        <th width="200">市</th>
        <th width="200"><input id="selectAll" type="checkbox" value="0"/>时效奖励</th>
    </tr>
    </thead>
    --%>
    <tbody>
    <c:forEach items="${list}" var="entity">
        <c:set var="index" value="${index+1}"/>
        <tr>
            <td>${index}</td>
            <td>${entity.areaName}</td>
            <c:forEach items="${entity.itemList}" var="categoryItem">
                <td>
                    <input type="checkbox" name="checkedRecords-${categoryItem.productCategoryId}" data-id="${categoryItem.id}"
                           data-area-id="${entity.areaId}" data-categoryid="${categoryItem.productCategoryId}"
                           ${categoryItem.isOpen==1?'checked="checked"':''}>
                </td>
            </c:forEach>
        </tr>
    </c:forEach>
    </tbody>
</table>
<script class="removedscript" type="text/javascript">
    function initCheckBoxNew() {
        var trLength = $("#treeTable tbody tr").length;// 获取当前表格中tr的个数
        if (trLength == 0) {
            return false;
        }

        $("[name^='selectAll']").each(function() {
            var productCategoryId = $(this).data("categoryid");

            if ($(":checkbox[name='checkedRecords-"+productCategoryId+"']").length == $(":checked[name='checkedRecords-"+productCategoryId+"']").length) {
                $(this).attr("checked", "checked");
            }
        });
    }
    initCheckBoxNew();
</script>
</body>
</html>
