<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>部门区域</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>

    <style>
        .table thead th, .table tbody td {
            text-align: center;
            vertical-align: middle;
            BackColor: Transparent;
            height: 30px;
        }
    </style>

    <script type="text/javascript">
        $(document).ready(function() {

            $(document).on('change',"#officeId",function (e) {
                editOffice();
            });

            $("#btnSubmit").click(function () {
                top.$.jBox.tip('请稍候...', 'loading');
                $("#searchForm").attr("action", "${ctx}/sys/officeRegion/list");
                $("#searchForm").submit();
            });
        });

        function editOffice() {
            var officeId = $("#officeId").val();
            if (officeId !='') {
                var name = $("#officeId").find("option:selected").text().replace(/&nbsp;|\s/g,"");
                $('.select2-chosen').text(name);
            }
        }
        function editOfficeRegion(officeId,productCategoryId,provinceId,cityId,beGrantedAreaIds,unauthorizedAreaIds) {
            var text = "添加客服";
            var url = "${ctx}/sys/officeRegion/form?officeId="+ officeId + "&productCategoryId=" + productCategoryId + "&provinceId=" + provinceId + "&cityId=" + cityId + "&beGrantedAreaIds=" + beGrantedAreaIds + "&unauthorizedAreaIds=" + unauthorizedAreaIds;
            var area = ['1000px', '596px'];
            top.layer.open({
                type: 2,
                id:"officeRegion",
                zIndex:150,
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

    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">部门区域</a></li>
</ul>
<form:form id="searchForm" modelAttribute="sysOfficeRegion" action="${ctx}/sys/officeRegion/list" method="post"
           class="form-inline">
    <div class="control-group" style="height: 45px;margin-top: 20px;border-bottom: 1px solid #EEEEEE;">

        <label class="margin_line" style="margin-left: 5px">部&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;门：</label>
        <form:select path="officeId" class="input-xlarge required" cssStyle="width: 250px">
            <form:option value="" label="请选择"/>
            <form:options items="${officeList}" itemLabel="name" itemValue="id" htmlEscape="false"/>
        </form:select>
        &nbsp;&nbsp;
        <input id="btnSubmit" class="btn btn-primary" type="button"  value="查询"/>
    </div>
</form:form>

<sys:message content="${message}"/>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="200px">省</th>
        <th width="200px">市</th>
        <c:choose>
            <c:when test="${not empty productCategoryList}">
                <c:forEach items="${productCategoryList}" var="productCategory">

                    <th width="250px">
                            ${productCategory.name}
                    </th>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <th>产品品类</th>
            </c:otherwise>
        </c:choose>

    </tr>
    </thead>
    <tbody>
    <c:if test="${not empty provinceRegionList}">
        <c:forEach items="${provinceRegionList}" var="item">
            <tr>
                <td rowspan="${item.regionList.size() + 1}">${item.provinceName}</td>
                <c:forEach items="${item.regionList}" var="pItem">
                    <tr>
                        <td>${pItem.cityName}</td>
                    <c:forEach items="${pItem.officeProductCategories}" var="officeProductCategory">
                        <c:choose>
                            <c:when test="${officeProductCategory.isCoverage == 0}">
                                <td>
                                    <a style="color: #F54142" href="javascript:void(0);" onclick="editOfficeRegion('${sysOfficeRegion.officeId}','${officeProductCategory.id}','${item.provinceId}','${pItem.cityId}','${officeProductCategory.beGrantedAreaIds}','${officeProductCategory.unauthorizedAreaIds}')">未添加客服</a>
                                </td>
                            </c:when>
                            <c:otherwise>
                                <td style="color: #0096DA">✔</td>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                    </tr>

                </c:forEach>
            </tr>

        </c:forEach>
    </c:if>
    </tbody>
</table>
<script type="text/javascript">
    setTimeout('editOffice()',100);
</script>
</body>
</html>
