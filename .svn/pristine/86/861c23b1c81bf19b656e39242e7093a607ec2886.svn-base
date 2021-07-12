<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <%@ include file="/WEB-INF/views/include/head.jsp" %>
    <title>客户产品</title>
    <meta name="decorator" content="default"/>
    <%@ include file="/WEB-INF/views/include/pageSearch.jsp" %>
    <script src="${ctxStatic}/jquery-honeySwitch/honeySwitch.js" type="text/javascript"></script>
    <link href="${ctxStatic}/jquery-honeySwitch/honeySwitch.css" rel="stylesheet"/>
    <script type="text/javascript">
        $(document).on('change',"#customer\\.id",function (e) {
            var customerId = $(this).val();
            if (customerId !=null || customerId !='' || customerId != '0') {
                $.ajax({
                    url: "${ctx}/provider/md/customerProduct/ajax/customerProductList",
                    data: {customerId: customerId},
                    success:function (e) {
                        if(e.success){
                            $("#product\\.id").empty();
                            var programme_sel=[];
                            programme_sel.push('<option value="" selected="selected">请选择</option>')
                            for(var i=0,len = e.data.length;i<len;i++){
                                var programme = e.data[i];
                                programme_sel.push('<option value="'+programme.id+'">'+programme.name+'</option>')
                            }
                            $("#product\\.id").append(programme_sel.join(' '));
                            $("#product\\.id").val("");
                            $("#product\\.id").change();
                        }else {
                            $("#product\\.id").html('<option value="" selected>请选择</option>');
                            layerMsg('该客户还没有配置产品！');
                        }
                    },
                    error:function (e) {
                        layerError("请求产品失败","错误提示");
                    }
                });
            }
        });

        //覆盖分页前方法
        function beforePage() {
            var val = $("#customer\\.id").val();
            if (val == undefined || val.length == 0) {
                layerInfo("请选择客户!", "信息提示");
                return false;
            }
            var $btnSubmit = $("#btnSubmit");
            $btnSubmit.attr('disabled', 'disabled');
            $("#btnClearSearch").attr('disabled', 'disabled');
            layerLoading("查询中...", true);
        }

        var clicktag = 0;
        $(document).on("click", "#btnSubmit", function () {
            if (clicktag == 0) {
                clicktag = 1;
                beforePage();
                setPage();
                this.form.submit();
            }
        });

        function editCustomerProduct(type,id) {
            var text = "添加安装规范";
            var url = "${ctx}/provider/md/customerProduct/form";
            if(type == 2){
                text = "修改"
                url = "${ctx}/provider/md/customerProduct/form?id=" + id;
            }
            top.layer.open({
                type: 2,
                id:"custoemrProduct",
                zIndex:19891015,
                title:text,
                content: url,
                area: ['1000px', '800px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }

        //删除
        function delCustomerProduct(id,customerId,productId,productName) {
            layer.confirm('确定要删除<font color=\'blue\'>'+productName+'</font>安装规范吗？',{icon: 3, title:'提示'}, function(){
                var loadingIndex = layerLoading('正在提交，请稍候...');
                var $btnDelete = $("#btnDelete");
                if ($btnDelete.prop("disabled") == true) {
                    event.preventDefault();
                    return false;
                }
                $btnDelete.prop("disabled", true);
                $.ajax({
                    url:"${ctx}/provider/md/customerProduct/ajax/removeFixSpec?id=" + id +"&customer.id="+customerId+"&product.id="+productId,
                    type:"POST",
                    dataType:"json",
                    success: function(data){
                        //提交后的回调函数
                        if(loadingIndex) {
                            top.layer.close(loadingIndex);
                        }
                        if(ajaxLogout(data)){
                            setTimeout(function () {
                                $btnDelete.removeAttr('disabled');
                            }, 2000);
                            return false;
                        }
                        if (data.success) {
                            layerMsg("删除成功");
                            var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                            if(pframe){
                                pframe.repage();
                            }
                            top.layer.close(this_index);//关闭本身
                        }else{
                            setTimeout(function () {
                                $btnDelete.removeAttr('disabled');
                            }, 2000);
                            layerError(data.message, "错误提示");
                        }
                        return false;
                    },
                    error: function (data)
                    {
                        if(loadingIndex) {
                            layer.close(loadingIndex);
                        }
                        setTimeout(function () {
                            $btnDelete.removeAttr('disabled');
                        }, 2000);
                        ajaxLogout(data,null,"数据删除错误，请重试!");
                        //var msg = eval(data);
                    },
                    timeout: 30000               //限制请求的时间，当请求大于3秒后，跳出请求
                });
            }, function(){
            });
        }
    </script>
    <c:set var="cuser" value="${fns:getUser()}" />
</head>

<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="javascript:void(0);">安装规范</a></li>
</ul>
<c:set var="canDdit" value="0"/>
<shiro:hasPermission name="md:customerproduct:edit">
    <c:set var="canDdit" value="1"/>
</shiro:hasPermission>
<form:form id="searchForm" modelAttribute="customerProduct" action="${ctx}/provider/md/customerProduct/findList" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <label><span class=" red">*</span>客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;户：</label>
    <form:select path="customer.id" cssStyle="width: 250px;">
        <form:option value="" label="请选择"></form:option>
        <form:options items="${fns:getMyCustomerListFromMS()}" itemLabel="name" itemValue="id"></form:options>
    </form:select>
    &nbsp;
    <label>产&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;品：</label>
    <form:select path="product.id" cssClass="input-small" cssStyle="width:250px;">
        <form:option value="" label="所有"/>
        <form:options items="${productList}" itemLabel="name" itemValue="id" htmlEscape="false"/>
    </form:select>
    &nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" onclick="return setPage();" value="查询" />
</form:form>
<shiro:hasPermission name="md:customerproduct:edit">
    <button style="margin-top: 15px;margin-bottom: 15px;border-radius: 4px;border:1px solid;border-color:#C0C0C0;background-color: rgb(238,238,238);width: 120px;height: 30px" onclick="editCustomerProduct(1,null)">
        <i class="icon-plus-sign"></i>&nbsp;添加安装规范
    </button>
</shiro:hasPermission>
<sys:message content="${message}" type="loading"/>
<table id="contentTable" class="table table-striped table-bordered table-condensed table-hover">
    <thead>
    <tr>
        <th width="50">序号</th>
         <th width="250">产品</th>
         <th width="500">安装规范</th>
         <th width="100">操作</th>
     </tr>
     </thead>
     <tbody>
     <c:set var="index" value="0"></c:set>
     <c:forEach items="${page.list}" var="entity">
         <c:set var="index" value="${index+1}"></c:set>
         <%--<td>${index+(page.pageNo-1)*page.pageSize}</td>--%>
         <td>${index}</td>
         <td>${entity.productDto.name}</td>
<%--         <td><a href="javascript:void(0);" data-toggle="tooltip" data-tooltip="${entity.fixSpec}">${fns:abbr(entity.fixSpec,250)}</a></td>--%>
         <td>${fns:abbr(entity.fixSpec,250)}</td>
         <td>
            <shiro:hasPermission name="md:customerproduct:edit">
             <a href="javascript:editCustomerProduct(2,'${entity.id}')">修改</a>
             <a href="#" onclick="delCustomerProduct('${entity.id}','${entity.customerDto.id}','${entity.productDto.id}','${entity.productDto.name}')">删除</a>
            </shiro:hasPermission>
         </td>
        </tr>
        </c:forEach>
    </tbody>
</table>
<div class="pagination">${page}</div>
<script type="text/javascript" language="javascript">
    $(document).ready(function () {
        $("th").css({"text-align":"center","vertical-align":"middle"});
        $("td").css({"text-align":"center","vertical-align":"middle"});

        $('a[data-toggle=tooltip]').darkTooltip();
        $('a[data-toggle=tooltipnorth]').darkTooltip({gravity : 'north'});
        $('a[data-toggle=tooltipeast]').darkTooltip({gravity : 'east'});
    });
</script>
</body>
</html>
