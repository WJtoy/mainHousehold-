/**
 * 客户订单处理大部分操作移到订单详情中
 * 依赖layer弹窗
 */
var Order={
    init:function(){
    	this.rownum=0;
        this.orderIds = [];
        this.data={};
        this.rootUrl="";
        this.reload = false;
        this.customerId = "";
        this.hideChargeColumn = false;
        this.version = "1.2";
        this.clickTag = 0;
    },
    //下单-装载客户产品及账户余额,信用额度
    loadProductsNew:function (customerId) {
        $("#shopId").empty();
        $("#productTable").find("#addrow").remove();
        var self = this;
        if(customerId == self.customerId && self.customerId !=""){
            if(!Order.data.hasOwnProperty("rownum")){
                Order.data.rownum = Order.rownum+1;
            }else{
                Order.data.rownum = Order.rownum+1;
            }
            Order.rownum=Order.rownum+1;
            var tmpl = document.getElementById('orderForm-new-row').innerHTML;
            var doTtmpl = doT.template(tmpl);
            // var trnew = doTtmpl(Order.data);
            //按品类分组
            var categoryGroup = groupBy(Order.data.products, function (item) {
               return item.categoryName;
             },function (item){
                return item.categoryId
            });
            Order.data['groups'] = categoryGroup;
            var trnew = doTtmpl(Order.data);
            $("#productTable > tbody").append(trnew);
            var categoryId = undefined;
            if(Order.data.items && Order.data.items.length >0){
                categoryId = Order.data.items[0].product.category.id;
                Order.data['category'] = categoryId;
                //禁用/启用分类
                Order.disableProductOptions(categoryId);
            }
            var brand = document.getElementById("brand");
            var tags = Order.data.customer.brands || [];
            $(brand).select2({
                maximumSelectionLength: 1,
                tags: tags
            });
            if(tags && tags.length>0){
                var seltags = [];
                seltags.push(tags[0]);
                $(brand).select2('val', seltags);
            }

            var shops = Order.data.customer.shops || [];
            var shopId_sel=[];
            shopId_sel.push('<option value="" selected="selected">请选择</option>');
            if (shops && shops.length > 0) {
                $.each(shops, function(i, item) {
                    shopId_sel.push('<option value="'+item.value+'" data-channel="' + item.sort + '">'+item.label+'</option>');
                });
            }
            $("#shopId").append(shopId_sel.join(' '));
            $("#btnSubmit").prop("disabled",false);
            var category = undefined;
            if(categoryId){
                for(var i = 0; i < categoryGroup.length; i++) {
                    var catg = categoryGroup[i];
                    if(catg.key === "" + categoryId){
                        category = catg;
                        break;
                    }
                }
            }else{
                category = categoryGroup[0];
            }
            if(category){
                var product = category.data[0];
                var _product = $("#product");
                _product.val(product.id);
                _product.trigger("change");
            }
            return;
        }
        //get data from web
        $.ajax(
            {
                url : self.rootUrl+"/sd/order/createOrEdit/form_products",
                type : "GET",
                data : {customerId:customerId},
                contentType : "application/json",
                success : function(data) {
                    if (ajaxLogout(data)) {
                        return false;
                    }
                    if (data.success == false) {
                        layerError(data.message, "错误提示");
                        return;
                    }

                    if (data.products.length == 0) {
                        $("#btnSubmit").prop("disabled", true);
                        layerAlert("您的帐号未维护产品价格。", "系统提示");
                        return;
                    }
                    /*缓存中已添加的服务项*/
                    if (data && data.hasOwnProperty("items") && data.items && data.items.length > 0) {
                        self.rownum = 0;
                        var tmpl = document.getElementById('orderForm-item-table-row').innerHTML;
                        var doTtmpl = doT.template(tmpl);
                        var trnew = doTtmpl(data);
                        if ($("#addrow").length > 0) {
                            $(trnew).insertBefore($("#addrow"));
                        } else {
                            $(trnew).insertAfter($("#productTable tr:last"));
                        }
                        self.rownum = data.items.length - 1;
                        self.hideChargeColumns();
                        $("#customerId").prop("disabled", true);
                    }
                    var dbalance = data.customer.balance - data.customer.blockAmount;
                    $("#balance").val(dbalance.toFixed(2));
                    $("#credit").val(data.customer.credit.toFixed(2));
                    var amount = data.customer.balance + data.customer.credit - data.customer.blockAmount;
                    if (amount <= 0) {
                        $("#btnSubmit").prop("disabled", true);
                        var msg = "您的账户余额不足,请尽快充值。<br>可下单金额：" + amount.toFixed(2);
                        layerAlert(msg, "系统提示");
                        return;
                    }

                    if (data && data.customer && data.customer.shops && data.customer.shops.length > 0) {
                        var shops = data.customer.shops;
                        var shopId_sel = [];
                        shopId_sel.push('<option value="" data-channel="1"  selected="selected">请选择</option>');
                        if (shops && shops.length > 0) {
                            $.each(shops, function (i, item) {
                                shopId_sel.push('<option value="' + item.value + '" data-channel="' + item.sort + '">' + item.label + '</option>');
                            });
                        }
                        $("#shopId").append(shopId_sel.join(' '));
                    }
                    //产品处理
                    if(data && data.products){
                        //按品类分组
                        var categoryGroup = groupBy(data.products, function (item) {
                            return item.categoryName;
                        },function (item){
                            return item.categoryId
                        });
                        // console.log(categoryGroup);
                        data['groups'] = categoryGroup;
                        //产品map, key:id,value:product
                        var productMap = arrayToMap(data.products, function (item) {
                           return item.id;
                        });
                        data['productMap'] = productMap;
                    }
                    Order.data = data;
                    Order.customerId = customerId;
                    var tmpl = document.getElementById('orderForm-new-row').innerHTML;
                    var doTtmpl = doT.template(tmpl);
                    // var trnew = doTtmpl(data);
                    var categoryId = undefined;
                    if (data && data.products) {
                        var trnew = doTtmpl(data);
                        $("#productTable > tbody").append(trnew);
                        self.hideChargeColumns();
                        if(data.items && data.items.length >0){
                            categoryId = data.items[0].product.category.id;
                            Order.data['category'] = categoryId;
                            //禁用/启用分类
                            Order.disableProductOptions(categoryId);
                        }
                    }
                    if ($("select#model").length > 0) {
                        $("select#model").select2();
                    }
                    // var brand = document.getElementById("brand");
                    // var brands = Order.data.customer.brands || [];
                    // $(brand).select2({
                    //     maximumSelectionLength: 1,
                    //     tags: brandsform_products
                    // });
                    // if(brands.length>0) {
                    //     $(brand).select2('val', [brands[0]]);
                    // }
                    $("#btnSubmit").prop("disabled",false);
                    var category = undefined;
                    if(categoryId){
                        for(var i = 0; i < categoryGroup.length; i++) {
                            var catg = categoryGroup[i];
                            if(catg.key === "" + categoryId){
                                category = catg;
                                break;
                            }
                        }
                    }else{
                        category = categoryGroup[0];
                    }
                    if(category){
                        var product = category.data[0];
                        var _product = $("#product");
                        _product.val(product.id);
                        _product.trigger("change");
                    }
                },
                error : function(e)
                {
                    ajaxLogout(e.responseText,null,"装载产品错误，请重试!");
                }
            });
        return false;
    },
    //下单/修改订单时装载客户产品列表
    loadCustomerProducts:function (customerId,dataSourceId){
        var self = this;
        var responseData = {data:undefined,message:"读取产品列表错误"};
        //get data from web
        $.ajax(
            {
                url : self.rootUrl+"/sd/order/createOrEdit/form_products",
                type : "GET",
                data : {customerId:customerId,dataSourceId: dataSourceId || '1'},
                contentType : "application/json",
                async: false,
                success : function(data)
                {
                    if(data.success == false){
                        responseData.message = data.message;
                    }else if(data.products.length ==0){
                        responseData.message = "您的帐号未维护产品价格";
                    }else{
                        self.data = data;
                        self.customerId = customerId;
                        responseData.data = data;
                    }
                },
                error : function(e)
                {
                    responseData.message = "装载产品错误，请重试";
                }
            });
        return responseData;
    },
    //下单时获得产品型号列表
    loadProductModels:function (customerId,product,productIndex) {
        var self = this;
        var $td = $("#addrow td:eq(6)");
        $td.html("");
        if(!product){
            return;
        }
        if(product.hasOwnProperty("models")) {
            //has set models
            if (product.models.length > 0) {
                //select
                var selModel = "<select id='model' name='model' class='model'><option value=''></option>";
                $.each(product.models, function(i, item) {
                    selModel = selModel + "<option value='"+item+"'>" + item + "</option>";
                });
                selModel = selModel + "</select>";
                $td.html(selModel);
            } else {
                //input
                $td.html("<input type='text' class='model' id='model' name='model' value='' maxlength='100' />");
            }
        }else{
            //var cid = $("[id='customer.id']").val();
            //get data from web
            $.ajax(
                {
                    url : self.rootUrl+"/sd/order/form_product_models",
                    type : "GET",
                    data : {customerId: customerId,productId: product.id},
                    contentType : "application/json",
                    success : function(data)
                    {
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if(data.success == false){
                            layerError(data.message,"错误提示");
                            return;
                        }

                        if(!data.data || data.data.length ==0){
                            //input
                            $td.html("<input type='text' class='model' id='model' name='model' value='' maxlength='100' />");
                            if(data.data){
                                // Order.data.products[productIndex].models = [];
                                Order.data.productMap[product.id].models = [];
                            }
                        }else{
                            //select
                            // Order.data.products[productIndex].models = data.data;
                            Order.data.productMap[product.id].models = data.data;
                            var selModel = "<select id='model' name='model'><option value=''></option>";
                            $.each(data.data, function(i, item) {
                                selModel = selModel + "<option value='"+item+"'>" + item + "</option>";
                            });
                            selModel = selModel + "</select>";
                            $td.html(selModel);
                        }
                        return;
                    },
                    error : function(e)
                    {
                        ajaxLogout(e.responseText,null,"装载产品型号错误，请重试!");
                    }
                });
        }

        return false;
    },
    //下单时根据选择的产品获得产品型号、品牌列表
    loadProductProperties:function (customerId,product,defaultBrandId) {
        var self = this;
        var $tdBrand = $("#addrow td:eq(5)");
        var $tdModel = $("#addrow td:eq(6)");
        $tdBrand.html("");
        $tdModel.html("");
        if(!product){
            return;
        }
        if(!defaultBrandId){
            defaultBrandId = "0";
        }
        if(product.hasOwnProperty("brands") && product.hasOwnProperty("models")) {
            if (product.brands.length > 0) {
                var selBrand = "<select id='brand' name='brand' class='model'>";
                $.each(product.brands, function(i, item) {
                    if(i==0 && defaultBrandId == "0") {
                        defaultBrandId = item.id;
                    }
                    selBrand = selBrand + "<option value='"+item.name+"' data-id='" + item.id + "' ";
                    if(defaultBrandId == item.id){
                        selBrand = selBrand + " selected ";
                    }
                    selBrand = selBrand + ">" + item.name + "</option>";
                });
                selBrand = selBrand + "</select>";
                $tdBrand.html(selBrand);
            } else {
                $tdBrand.html("<input type='text' class='model' id='brand' name='brand' value='' maxlength='100' />");
            }
            if (product.models.length > 0) {
                var selModel = "<select id='model' name='model' class='model'>";
                selModel = selModel + "<option value=''></option>";
                $.each(product.models, function(i, item) {
                    if(item.brandId == defaultBrandId) {
                        selModel = selModel + "<option value='" + item.customerModel + "'>" + item.customerModel + "</option>";
                    }
                });
                selModel = selModel + "</select>";
                $tdModel.html(selModel);
                $("select#model").select2();
            } else {
                $tdModel.html("<input type='text' class='model' id='model' name='model' value='' maxlength='100' />");
            }
        }else{
            $.ajax(
                {
                    url : self.rootUrl+"/sd/order/createOrEdit/form_product_properties",
                    type : "GET",
                    data : {customerId: customerId,productId: product.id},
                    contentType : "application/json",
                    success : function(data)
                    {
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if(data.success == false){
                            layerError(data.message,"错误提示");
                            return;
                        }

                        var brands = data.data.brands || [];
                        var models = data.data.models || [];
                        // Order.data.products[productIndex].brands = brands;
                        // Order.data.products[productIndex].models = models;
                        Order.data.productMap[product.id].brands = brands;
                        Order.data.productMap[product.id].models = models;

                        var selBrandId = "0";
                        if (brands.length > 0) {
                            var selBrand = "<select id='brand' name='brand' class='model' >";
                            $.each(brands, function(i, item) {
                                if(i==0){
                                    selBrandId = item.id;
                                    selBrand = selBrand + "<option value='"+item.name+"' data-id='" + item.id + "' selected>" + item.name + "</option>";
                                }else{
                                    selBrand = selBrand + "<option value='"+item.name+"' data-id='" + item.id + "'>" + item.name + "</option>";
                                }
                            });
                            selBrand = selBrand + "</select>";
                            $tdBrand.html(selBrand);
                        } else {
                            $tdBrand.html("<input type='text' class='model' id='brand' name='brand' value='' maxlength='100' />");
                        }
                        if (models.length > 0) {
                            var selModel = "<select id='model' name='model' class='model' >";
                            selModel = selModel + "<option value=''></option>";
                            $.each(models, function(i, item) {
                                if(item.brandId == selBrandId) {
                                    selModel = selModel + "<option value='" + item.customerModel + "'>" + item.customerModel + "</option>";
                                }
                            });
                            selModel = selModel + "</select>";
                            $tdModel.html(selModel);
                            $("#model").select2();
                        } else {
                            $tdModel.html("<input type='text' class='model' id='model' name='model' value='' maxlength='100'/>");
                        }
                        return;
                    },
                    error : function(e)
                    {
                        ajaxLogout(e.responseText,null,"装载产品型号、品牌错误，请重试!");
                    }
                });
        }

        return false;
    },
    //下单时根据选择读取加急费，并刷新页面
    getUrgentCharge:function(areaId){
        //console.log('getUrgentCharge:start');
        var self = this;
        var customerId = $("[id='customer.id']").val();
        if(Utils.isNull(areaId)){
            areaId = $("#areaId").val();
        }
        var urgentLevelId = $("input:radio[name='urgentLevel.id']:checked").val();
        if (urgentLevelId == '0' || urgentLevelId == ''){
            $("#btnSubmit").prop("disabled", false);
        }
        //init
        //$("#btnSubmit").prop("disabled") == true ||
        if( Utils.isNull(customerId) || customerId == ''
            || Utils.isNull(areaId) || areaId == ''
            || Utils.isNull(urgentLevelId) || urgentLevelId == '' || urgentLevelId == '0'){
            $("#customerUrgentCharge").val("0.00");
            $("#engineerUrgentCharge").val("0.00");
            $("#lblUrgentCharge").text("0.0");
            $("#lblUrgent").text("0.0");
            var expectCharge = parseFloat($("#lblassignedCharge").text());
            var blockCharge = parseFloat($("#lblblockedCharge").text());
            var total = expectCharge+blockCharge;
            $("#lbltotalCharge").text(total.toFixed(2));
            //console.log('getUrgentCharge:customer isnull');
            return false;
        }
        //console.log(data);
        $.ajax({
                url : self.rootUrl+"/sd/order/getCustomerUrgentCharge?customerId=" + (customerId || '') + "&areaId=" + (areaId || '') + "&urgentLevelId=" + (urgentLevelId || '') + "&checkCustomer=false",
                type : "POST",
                success : function(data)
                {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if(data.success == false){
                        $("#btnSubmit").prop("disabled", true);
                        layerError(data.message,"加急费");
                        return;
                    }
                    $("#customerUrgentCharge").val(data.data.chargeIn);
                    $("#engineerUrgentCharge").val(data.data.chargeOut);
                    $("#lblUrgentCharge").text(data.data.chargeIn.toFixed(1));
                    $("#lblUrgent").text(data.data.chargeIn.toFixed(1));
                    var expectCharge = parseFloat($("#lblassignedCharge").text());
                    var blockCharge = parseFloat($("#lblblockedCharge").text());
                    var total = expectCharge + blockCharge + data.data.chargeIn;
                    $("#lbltotalCharge").text(total.toFixed(2));
                    $("#btnSubmit").prop("disabled", false);
                    //console.log('getUrgentCharge:success');
                    return false;
                },
                error : function(e)
                {
                    $("#btnSubmit").prop("disabled", true);
                    $("#customerUrgentCharge").val("0.00");
                    $("#engineerUrgentCharge").val("0.00");
                    $("#lblUrgentCharge").text("0.0");
                    $("#lblUrgent").text("0.0");
                    var expectCharge = parseFloat($("#lblassignedCharge").text());
                    var blockCharge = parseFloat($("#lblblockedCharge").text());
                    var total = expectCharge+blockCharge;
                    $("#lbltotalCharge").text(total.toFixed(2));
                    ajaxLogout(e.responseText,null,"读取加急费用错误，请重试!");
                    //console.log('getUrgentCharge:error');
                    e.preventDefault();
                }
            });
          //console.log('getUrgentCharge:end');
        return false;
    },
    //客户修改加急
    urgentOrder:function(id,quarter,no){
        var self = this;
        var urgentIndex = top.layer.open({
            type: 2,
            id:'layer_urgent',
            zIndex:19891015,
            title:'订单加急 ['+no+']',
            content: self.rootUrl+"/sd/order/urgent?id=" + id  + "&quarter=" + (quarter || '') + "&orderNo=" + (no || ''),
            area: ['650px', '300px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            }
        });
    },
    //修改加急等级时，根据选项取得加急费
    changeUrgentLevel:function(){
        var self = this;
        var customerId = $("[id='customerId']").val();
        var urgentLevelId = $("input:radio[name='urgentLevel.id']:checked").val();
        var areaId = $("#areaId").val();
        //init
        if( $("#btnSubmit").prop("disabled") == true
            || Utils.isNull(customerId) || customerId == ''
            || Utils.isNull(areaId) || areaId == ''
            || Utils.isNull(urgentLevelId) || urgentLevelId == '' || urgentLevelId == '0'){
            $("#lblUrgentCharge").text("0.0");
            $("#chargeIn").val("0.0");
            $("#chargeOut").val("0.0");
            return false;
        }
        $.ajax({
            url : self.rootUrl+"/sd/order/getCustomerUrgentCharge?customerId=" + (customerId || '') + "&areaId=" + (areaId || '') + "&urgentLevelId=" + (urgentLevelId || '') + "&checkCustomer=false",
            type : "POST",
            success : function(data)
            {
                if(ajaxLogout(data)){
                    return false;
                }
                if(data.success == false){
                    $("#btnSubmit").prop("disabled", true);
                    layerError(data.message,"错误提示");
                    return;
                }
                $("#lblUrgentCharge").text(data.data.chargeIn.toFixed(1));
                $("#chargeIn").val(data.data.chargeIn);
                $("#chargeOut").val(data.data.chargeOut);
                var strVal = "chargeIn:" + data.data.chargeIn +",chargeOut:" + data.data.chargeOut;
                setGlobalVar("data",strVal,"text");
                $("#btnSubmit").prop("disabled", false);
                return false;
            },
            error : function(e)
            {
                $("#btnSubmit").prop("disabled", true);
                $("#lblUrgentCharge").text("0.0");
                $("#chargeIn").val("0.0");
                $("#chargeOut").val("0.0");
                ajaxLogout(e.responseText,null,"读取加急费用错误，请重试!");
                e.preventDefault();
            }
        });
        return false;
    },
    //ajax提交添加订单项
    addItem:function(){
        var self = this;
        var $product = $("#product");
        var pid = $product.val();
        if(Utils.isEmpty(pid)){
            layerAlert("请选择产品","信息提示");
            lock = false;
            $("#product").focus();
            return true;
        }
        var stid = $("#serviceType").val();
        if(Utils.isEmpty(stid)){
            layerAlert("请选择服务类型","信息提示");
            lock = false;
            $("#serviceType").focus();
            return true;
        }
        var qty = $("#qty").val();
        if(!Utils.isPositiveInteger(qty)){
            layerAlert("数量应是整数类型，并且大于0","信息提示");
            lock = false;
            $("#qty").focus();
            return true;
        }
        var expresscompany=$("#expresscompany").val();
        var expressno=$("#expressno").val();
        if(!Utils.isEmpty(expresscompany) && Utils.isEmpty(expressno))
        {
            layerAlert("请输入快递单号","系统提示");
            lock = false;
            $("#expressno").focus();
            return true;
        }
        var balance = $("#balance").val();
        var credit = $("#credit").val();
        var $selOption = $product.find("option:selected");
        var pname = $selOption.text();
        var categoryId = $selOption.data("categoryid");
        var categoryName = $selOption.data("categoryname");
        var sname = $("#serviceType").find("option:selected").text();
        var brand = $("#brand").val();
        var pspec = $("#model").val();
        var expressCompany=$("#expressCompany").val();
        var expressCompanyName=$("#expressCompany").find("option:selected").text();
        var expressNo=$("#expressNo").val();
        var cid = $("[id='customer.id']").val();
        var urgentLevelId = "0";
        var urgentLevelName = "";
        if($("#urgentLevelId").length>0){
            var optionSelected = $("#urgentLevelId").find("option:selected");
            urgentLevelId = optionSelected.val();
            urgentLevelName = optionSelected.text();
        }
        var customerUrgentCharge = 0;
        if($("#customerUrgentCharge").length>0) {
            customerUrgentCharge = $("#customerUrgentCharge").val();
        }
        var engineerUrgentCharge = 0;
        if($("#engineerUrgentCharge").length>0) {
            engineerUrgentCharge = $("#engineerUrgentCharge").val();
        }
        var item = {
            "balance":balance,"credit":credit,
            "product.id":pid,"product.name":pname,
            "product.category.id":categoryId,"product.category.name":categoryName,
            "serviceType.id":stid,"serviceType.name":sname,
            "qty":qty,"brand":brand,"productSpec":pspec,
            "expressCompany.value":expressCompany,"expressCompany.label":expressCompanyName,"expressNo":expressNo,
            "remarks":"","customerId":cid,"urgentLevel.id":urgentLevelId,"urgentLevel.remarks":urgentLevelName,
            "customerUrgentCharge":customerUrgentCharge,"engineerUrgentCharge":engineerUrgentCharge
            };
        //post
        $.ajax({
            type: "POST",
            url: self.rootUrl+"/sd/order/saveitem?t="+ (new Date()).getTime(),
            data: item,
            complete: function () {
                lock = false;
            },
            success: function (data) {
                if(ajaxLogout(data)){
                    return false;
                }
                if(data && data.success == true){
                    self.addItemRows(data.data);
                }
                else if( data && data.message){
                    layerError(data.message,"错误提示");
                }
                else{
                    layerError("添加产品到订单详细清单时错误","错误提示");
                }
                return false;

            },
            error: function (e) {
                ajaxLogout(e.responseText,null,"添加产品错误，请重试!");
            }
        });
	},
    //开单-添加订单项
    addItemRows:function(data) {
        var self = this;
        if (!data || !data.hasOwnProperty("items")) {
            return;
        }
        $("#productTable tr.data").remove();
        self.rownum = 0;
        var tmpl = document.getElementById('orderForm-item-table-row').innerHTML;
        var doTtmpl = doT.template(tmpl);
        var trnew = doTtmpl(data);

        if ($("#addrow").length > 0) {
            $(trnew).insertBefore($("#addrow"));
        } else {
            $(trnew).insertAfter($("#productTable tr:last"));
        }
        self.hideChargeColumns();
        self.rownum = data.items.length - 1;

        $('#lblassignedCharge').text(data.expectCharge);
        $('#expectCharge').val(data.expectCharge);
        $("#lblblockedCharge").text(data.blockedCharge);
        $('#blockedCharge').val(data.blockedCharge);
        $("#lbltotalQty").text(data.totalQty);
        $("#totalQty").val(data.totalQty);

        var customerUrgentCharge = 0;
        if($("#customerUrgentCharge").length>0){
            customerUrgentCharge = parseFloat($("#customerUrgentCharge").val());
        }
        $("#lbltotalCharge").text(data.blockedCharge + data.expectCharge + customerUrgentCharge);

        var balance = parseFloat($("#balance").val());
        var credit= parseFloat($("#credit").val());
        if (balance + credit < data.blockedCharge + data.expectCharge) {
            $("#lbltotalCharge").removeClass("alert-success").addClass("alert-error");
            $("#btnSubmit").removeClass("btn-primary").addClass("btn-danger").addClass("disabled");
            $("#btnSubmit").attr("disalbed", "disabled");
        } else {
            $("#lbltotalCharge").removeClass("alert-error").addClass("alert-success");
            $("#btnSubmit").removeClass("btn-danger").removeClass("disabled").addClass("btn-primary");
            $("#btnSubmit").removeAttr('disabled');
        }
        $("#expresscompany").val("");//清空快递信息
        $("#expressno").val("");//清空快递信息
        $("#remarks").val("");//清空新增行备注

        //客服添加订单，有订单项后不允许更改客户
        if ($("#customerId").length>0) {
            if (data.items.length > 0) {
                $("#customerId").prop("disabled", true);
            } else {
                $("#customerId").removeAttr("disabled");
            }
        }

        $("#selectAll").attr("checked", false);
        if(!data.category){
            //取消禁止
            Order.disableProductOptions(undefined);
        }else if(data.category && data.category.id){
            //禁止其他类目
            Order.disableProductOptions(data.category.id);
        }
    },
    //下单时，禁止/取消禁止 选择其他品类产品
    //categoryId:当前有效的类目id
    disableProductOptions:function (categoryId){
        var $option;
        var _optGroup;
        if(!categoryId || categoryId === 0){
            $("#product optgroup").each(function(){
                _optGroup = $(this);
                _optGroup.show();
            });
            // $("#product option").each(function(){ //遍历全部option
            //     $option = $(this);
            //     $option.removeAttr("disabled");
            // });
            // $("#product").trigger("chosen:updated");
            return;
        }
        $("#product optgroup").each(function(){
            _optGroup = $(this);
            if(_optGroup.attr("id") == categoryId){
                _optGroup.show();
            }else{
                _optGroup.hide();
            }
        });

        // $("#product option").each(function(){ //遍历全部option
        //     $option = $(this);
        //     if($option.data("categoryid") === categoryId){
        //         $option.removeAttr("disabled");
        //     }else{
        //         $option.attr("disabled","disabled");//禁止
        //     }
        // });
        // $("#product").trigger("chosen:updated");
    },
    //装载客户产品及账户余额,信用额度
    editForm_loadProducts:function (customerId,showNew) {
        var self = this;
        $.ajax(
            {
                url : self.rootUrl+"/sd/order/form_products",
                type : "GET",
                data : {id:customerId,action:"edit"},
                contentType : "application/json",
                success : function(data) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.products.length == 0) {
                        $("#btnSubmit").prop("disabled", true);
                        layerAlert("你的帐号未设定服务产品。", "系统提示");
                        return;
                    }
                    /*
                    $("#balance").val(data.customer.balance-data.customer.blockAmount);
                    $("#credit").val(data.customer.credit);
                    var amount = data.customer.balance + data.customer.credit - data.customer.blockAmount;
                    if (amount <= 0) {
                        $("#btnSubmit").prop("disabled", true);
                        var msg = "您的账户余额不足,请尽快充值。<br>可下单金额：" + amount.toFixed(2);
                        layerAlert( msg, "系统提示");
                        return;
                    }*/
                    Order.data = data;
                    Order.customerId = customerId;
                    //添加新产品
                    if (showNew && showNew == true) {
                        self.rownum = self.rownum + 1;
                        Order.data.rownum = self.rownum;
                        var tmpl = document.getElementById('editForm-new-row').innerHTML;
                        var doTtmpl = doT.template(tmpl);
                        var trnew = doTtmpl(data);
                        $("#productTable > tbody").append(trnew);
                        var brand = document.getElementById("items." + self.rownum + ".brand");
                        var brands = Order.data.customer.brands || [];
                        $(brand).select2({
                            maximumSelectionLength: 1,
                            tags: brands
                        });
                        if(brands.length>0) {
                            $(brand).select2('val', [brands[0]]);
                        }
                    }
                    $("#btnSubmit").prop("disabled",false);
                },
                error : function()
                {
                    ajaxLogout(e.responseText,null,"装载产品错误，请重试!");
                }
            });
    },
    //修改订单，装载客户产品及账户余额,信用额度
    editForm_loadProductsForKKL:function (customerId,dataSourceId) {
        var self = this;
        // //编辑界面，使用界面控件中的品类，不能更换
        // var categoryId = $("[id='category.id']").val();
        $.ajax(
            {
                url : self.rootUrl+"/sd/order/createOrEdit/form_products",
                type : "GET",
                data : {customerId:customerId,action:"edit", dataSourceId:dataSourceId},
                contentType : "application/json",
                success : function(data) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.products.length == 0) {
                        $("#btnSubmit").prop("disabled", true);
                        layerAlert("你的帐号未设定服务产品。", "系统提示");
                        return;
                    }
                    //产品处理
                    if(data && data.products){
                        //按品类分组
                        var categoryGroup = groupBy(data.products, function (item) {
                            return item.categoryName;
                        },function (item){
                            return item.categoryId
                        },function (item){
                            return item.services && item.services.length>0;
                        });
                        // console.log(categoryGroup);
                        data['groups'] = categoryGroup;
                        //产品map, key:id,value:product
                        var productMap = arrayToMap(data.products, function (item) {
                            return item.id;
                        });
                        data['productMap'] = productMap;
                    }
                    Order.data = data;
                    Order.customerId = customerId;
                    //添加新产品
                    self.rownum = self.rownum + 1;
                    Order.data.rownum = self.rownum;
                    var tmpl = document.getElementById('editForm-new-row').innerHTML;
                    var doTtmpl = doT.template(tmpl);
                    // var trnew = doTtmpl(data);
                    // var product = undefined;
                    if (data && data.products) {
                        var trnew = doTtmpl(Order.data);
                        //添加服务产品选择行
                        $("#productTable > tbody").append(trnew);
                        self.hideChargeColumnsForEditForm();
                        //品类可选控制
                        if(data.items && data.items.length >0){
                            categoryId = data.items[0].product.category.id;
                            Order.data['category'] = categoryId;
                            //禁用/启用分类
                            Order.disableProductOptions(categoryId);
                        }
                    }
                    var category = undefined;
                    if(categoryId){
                        for(var i = 0; i < categoryGroup.length; i++) {
                            var catg = categoryGroup[i];
                            if(catg.key === "" + categoryId){
                                category = catg;
                                break;
                            }
                        }
                    }else{
                        category = categoryGroup[0];
                    }
                    if(category){
                        var product = category.data[0];
                        var _product = $("#order_tr_" + self.rownum).find("#product");
                        _product.val(product.id);
                        _product.trigger("change");//此处触发产品选择控件事件，转载品牌，型号等
                    }
                    $("#btnSubmit").prop("disabled",false);
                },
                error : function()
                {
                    ajaxLogout(e.responseText,null,"装载产品错误，请重试!");
                }
            });
    },
    //B2B转单：装载客户产品及账户余额,信用额度
    editForm_loadProductsForB2B:function (customerId,dataSourceId,showNew) {
        var self = this;
        $.ajax(
            {
                url : self.rootUrl+"/sd/order/createOrEdit/form_products",
                type : "GET",
                data : {customerId:customerId,action:"edit", dataSourceId:dataSourceId},
                contentType : "application/json",
                success : function(data) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.products.length == 0) {
                        $("#btnSubmit").prop("disabled", true);
                        layerAlert("你的帐号未设定服务产品。", "系统提示");
                        return;
                    }
                    //产品处理
                    if(data && data.products){
                        //按品类分组
                        var categoryGroup = groupBy(data.products, function (item) {
                            return item.categoryName;
                        },function (item){
                            return item.categoryId
                        });
                        // console.log(categoryGroup);
                        data['groups'] = categoryGroup;
                        //产品map, key:id,value:product
                        var productMap = arrayToMap(data.products, function (item) {
                            return item.id;
                        });
                        data['productMap'] = productMap;
                    }
                    Order.data = data;
                    Order.customerId = customerId;
                    //添加新产品
                    if (showNew && showNew == true) {
                        self.rownum = self.rownum + 1;
                        Order.data.rownum = self.rownum;
                        var tmpl = document.getElementById('editForm-new-row').innerHTML;
                        var doTtmpl = doT.template(tmpl);
                        var categoryId = undefined;
                        if (data && data.products) {
                            var trnew = doTtmpl(Order.data);
                            //添加服务产品选择行
                            $("#productTable > tbody").append(trnew);
                            //品类可选控制
                            categoryId = $("[id='category.id']").val() | '0';
                            Order.data['category'] = categoryId;
                            //禁用/启用分类
                            Order.disableProductOptions(categoryId);
                        }
                        var category = undefined;
                        if(categoryId){
                            for(var i = 0; i < categoryGroup.length; i++) {
                                var catg = categoryGroup[i];
                                if(catg.key === "" + categoryId){
                                    category = catg;
                                    break;
                                }
                            }
                        }else{
                            category = categoryGroup[0];
                        }
                        if(category){
                            var product = category.data[0];
                            var _product = $("#order_tr_" + self.rownum).find("#product");
                            _product.val(product.id);
                            _product.trigger("change");//此处触发产品选择控件事件，转载品牌，型号等
                        }
                        // var trnew = doTtmpl(data);
                        // $("#productTable > tbody").append(trnew);
                        // var $tdBrand = $("#order_tr_" + self.rownum + " td:eq(4)");
                        // var $tdModel = $("#order_tr_" + self.rownum + " td:eq(5)");
                        // var $tdB2BProductCode = $("#order_tr_" + self.rownum + " td:eq(6)");
                        // var brands = Order.data.products[0].brands || [];
                        // var productSpecs = Order.data.products[0].models || [];
                        // var b2bProductCodes = Order.data.products[0].b2bProductCodes || [];
                        // var defaultBrandId = "0";
                        // if (brands.length > 0) {
                        //     var selBrand = "<select id='items."+ self.rownum + ".brand' name='items["+ self.rownum + "].brand' data-index='"+ self.rownum + "' class='brand' style='width:80px'>";
                        //     // selBrand = selBrand + "<option value=''></option>";
                        //     $.each(brands, function(i, item) {
                        //         if(i==0 && defaultBrandId == "0") {
                        //             defaultBrandId = item.id;
                        //         }
                        //         selBrand = selBrand + "<option value='"+item.name+"' data-id='" + item.id + "' ";
                        //         if(defaultBrandId == item.id){
                        //             selBrand = selBrand + " selected ";
                        //         }
                        //         selBrand = selBrand + ">" + item.name + "</option>";
                        //         // selBrand = selBrand + "<option value='"+item+"'>" + item + "</option>";
                        //     });
                        //     selBrand = selBrand + "</select>";
                        //     $tdBrand.html(selBrand);
                        // } else {
                        //     $tdBrand.html("<input type='text' class='brand' id='items."+ self.rownum + ".brand' name='items["+ self.rownum + "].brand' value='' maxlength='100' style='width:80px'/>");
                        // }
                        // if (productSpecs.length > 0) {
                        //     var selModel = "<select id='items."+ self.rownum + ".productSpec' name='items["+ self.rownum + "].productSpec' class='spec' style='width:120px'>";
                        //     selModel = selModel + "<option value=''></option>";
                        //     $.each(productSpecs, function(i, item) {
                        //         if(item.brandId == defaultBrandId) {
                        //             selModel = selModel + "<option value='" + item.customerModel + "'>" + item.customerModel + "</option>";
                        //         }
                        //         // selModel = selModel + "<option value='"+item+"'>" + item + "</option>";
                        //     });
                        //     selModel = selModel + "</select>";
                        //     $tdModel.html(selModel);
                        //     $("[id='items."+ self.rownum + ".productSpec'").select2();
                        // } else {
                        //     $tdModel.html("<input type='text' class='spec' id='items."+ self.rownum + ".productSpec' name='items["+ self.rownum + "].productSpec' value='' maxlength='100' style='width:120px'/>");
                        // }
                        // if (b2bProductCodes.length > 0) {
                        //     var selB2BProductCode = "<select id='items."+ self.rownum + ".b2bProductCode' name='items["+ self.rownum + "].b2bProductCode' class='b2bspec' style='width:120px'>";
                        //     selB2BProductCode = selB2BProductCode + "<option value=''></option>";
                        //     $.each(b2bProductCodes, function(i, item) {
                        //         selB2BProductCode = selB2BProductCode + "<option value='"+item+"'>" + item + "</option>";
                        //     });
                        //     selB2BProductCode = selB2BProductCode + "</select>";
                        //     $tdB2BProductCode.html(selB2BProductCode);
                        //     $("[id='items."+ self.rownum + ".b2bProductCode'").select2();
                        // } else {
                        //     $tdB2BProductCode.html("<input type='text' class='b2bspec' id='items."+ self.rownum + ".b2bProductCode' name='items["+ self.rownum + "].b2bProductCode' value='' maxlength='100' style='width:120px'/>");
                        // }
                    }
                    $("#btnSubmit").prop("disabled",false);
                },
                error : function()
                {
                    ajaxLogout(e.responseText,null,"装载产品错误，请重试!");
                }
            });
    },
    //下单时获得产品型号列表
    editForm_loadProductModels:function (customerId,product,productIndex,rowIndex) {
        var self = this;
        var $td = $("#order_tr_" + rowIndex + " td:eq(5)");
        $td.html("");
        if(!product){
            return;
        }
        if(product.hasOwnProperty("models")) {
            //has set models
            if (product.models.length > 0) {
                //select
                var selModel = "<select id='items."+ rowIndex + ".productSpec' name='items["+ rowIndex + "].productSpec' class='spec'><option value=''></option>";
                $.each(product.models, function(i, item) {
                    selModel = selModel + "<option value='"+item+"'>" + item + "</option>";
                });
                selModel = selModel + "</select>";
                $td.html(selModel);
            } else {
                //input
                $td.html("<input type='text' class='model' id='items."+ rowIndex + ".productSpec' name='items["+ rowIndex + "].productSpec' value='' maxlength='100' />");
            }
        }else{
            $.ajax(
                {
                    url : self.rootUrl+"/sd/order/form_product_models",
                    type : "GET",
                    data : {customerId: customerId,productId: product.id},
                    contentType : "application/json",
                    success : function(data)
                    {
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if(data.success == false){
                            layerError(data.message,"错误提示");
                            return;
                        }

                        if(!data.data || data.data.length ==0){
                            //input
                            $td.html("<input type='text' class='model' id='items["+ rowIndex + "].productSpec' name='items["+ rowIndex + "].productSpec' value='' maxlength='100' />");
                            if(data.data){
                                Order.data.products[productIndex].models = [];
                            }
                        }else{
                            //select
                            Order.data.products[productIndex].models = data.data;
                            var selModel = "<select id='items."+ rowIndex + ".productSpec' name='items["+ rowIndex + "].productSpec'><option value=''></option>";
                            $.each(data.data, function(i, item) {
                                selModel = selModel + "<option value='"+item+"'>" + item + "</option>";
                            });
                            selModel = selModel + "</select>";
                            $td.html(selModel);
                        }
                        return;
                    },
                    error : function(e)
                    {
                        ajaxLogout(e.responseText,null,"装载产品型号错误，请重试!");
                    }
                });
        }

        return false;
    },
    //编辑工单时获取产品属性（型号、品牌、b2b产品编码）
    editForm_loadProductPropertiesForKKL:function (dataSourceId, customerId,product,rowIndex,defaultBrandId) {
        var self = this;
        var $tdBrand = $("#order_tr_" + rowIndex + " td:eq(4)");
        var $tdModel = $("#order_tr_" + rowIndex + " td:eq(5)");
        var $tdB2BProductCode = $("#order_tr_" + rowIndex + " td:eq(6)");
        $tdBrand.html("");
        $tdModel.html("");
        $tdB2BProductCode.html("");
        if(!product){
            return;
        }
        if(!defaultBrandId || defaultBrandId == null || defaultBrandId == undefined){
            defaultBrandId = "0";
        }
        if(product.hasOwnProperty("brands") && product.hasOwnProperty("models") && product.hasOwnProperty("b2bProductCodes")) {
            if (product.brands.length > 0) {
                var selBrand = "<select id='items."+ rowIndex + ".brand' name='items["+ rowIndex + "].brand' data-productid='" + product.id + "' data-index='" + rowIndex + "' class='brand'>";
                $.each(product.brands, function(i, item) {
                    if(i==0 && defaultBrandId == "0") {
                        defaultBrandId = item.id;
                    }
                    selBrand = selBrand + "<option value='"+item.name+"' data-id='" + item.id + "' ";
                    if(defaultBrandId == item.id){
                        selBrand = selBrand + " selected ";
                    }
                    selBrand = selBrand + ">" + item.name + "</option>";
                });
                selBrand = selBrand + "</select>";
                $tdBrand.html(selBrand);
            } else {
                $tdBrand.html("<input type='text' class='model' id='items."+ rowIndex + ".brand' name='items["+ rowIndex + "].brand' value='' maxlength='100' />");
            }
            if (product.models.length > 0) {
                var selModel = "<select id='items."+ rowIndex + ".productSpec' name='items["+ rowIndex + "].productSpec' class='spec'>";
                selModel = selModel + "<option value=''></option>";
                $.each(product.models, function(i, item) {
                    if(item.brandId == defaultBrandId) {
                        selModel = selModel + "<option value='" + item.customerModel + "'>" + item.customerModel + "</option>";
                    }
                });
                selModel = selModel + "</select>";
                $tdModel.html(selModel);
                $("[id='items." + rowIndex + ".productSpec']").select2();
            } else {
                $tdModel.html("<input type='text' class='model' id='items."+ rowIndex + ".productSpec' name='items["+ rowIndex + "].productSpec' value='' maxlength='100' />");
            }
            if (product.b2bProductCodes.length > 0) {
                var selB2BProductCode = "<select id='items."+ rowIndex + ".b2bProductCode' name='items["+ rowIndex + "].b2bProductCode' class='b2bspec'>";
                selB2BProductCode = selB2BProductCode + "<option value=''></option>";
                $.each(product.b2bProductCodes, function(i, item) {
                    selB2BProductCode = selB2BProductCode + "<option value='"+item+"'>" + item + "</option>";
                });
                selB2BProductCode = selB2BProductCode + "</select>";
                $tdB2BProductCode.html(selB2BProductCode);
                $("[id='items." + rowIndex + ".b2bProductCode']").select2();
            } else {
                $tdB2BProductCode.html("<input type='text' class='b2bspec' id='items."+ rowIndex + ".b2bProductCode' name='items["+ rowIndex + "].b2bProductCode' value='' maxlength='100' />");
            }

        } else {
            $.ajax(
                {
                    url : self.rootUrl+"/sd/order/createOrEdit/form_product_properties",
                    type : "GET",
                    data : {customerId: customerId,dataSourceId: dataSourceId,productId: product.id},
                    contentType : "application/json",
                    success : function(data)
                    {
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if(data.success == false){
                            layerError(data.message,"错误提示");
                            return;
                        }
                        var brands = data.data.brands || [];
                        var models = data.data.models || [];
                        var b2bProductCodes = data.data.b2bProductCodes || [];
                        Order.data.productMap[product.id].brands = brands;
                        Order.data.productMap[product.id].models = models;
                        Order.data.productMap[product.id].b2bProductCodes = b2bProductCodes;

                        if (brands.length > 0) {
                            var selBrand = "<select id='items."+ rowIndex + ".brand' name='items["+ rowIndex + "].brand' data-productid='" + product.id + "' data-index='" + rowIndex + "' class='brand' >";
                            $.each(brands, function(i, item) {
                                if(i==0 && defaultBrandId == "0") {
                                    defaultBrandId = item.id;
                                }
                                if(defaultBrandId == item.id){
                                    selBrand = selBrand + "<option value='"+item.name+"' data-id='" + item.id + "' selected>" + item.name + "</option>";
                                }else{
                                    selBrand = selBrand + "<option value='"+item.name+"' data-id='" + item.id + "'>" + item.name + "</option>";
                                }
                            });
                            selBrand = selBrand + "</select>";
                            $tdBrand.html(selBrand);
                        } else {
                            $tdBrand.html("<input type='text' class='model' id='items."+ rowIndex + ".brand' name='items["+ rowIndex + "].brand' value='' maxlength='100' />");
                        }
                        if (models.length > 0) {
                            var selModel = "<select id='items."+ rowIndex + ".productSpec' name='items["+ rowIndex + "].productSpec' class='spec' >";
                            selModel = selModel + "<option value=''></option>";
                            $.each(models, function(i, item) {
                                if(item.brandId == defaultBrandId) {
                                    selModel = selModel + "<option value='" + item.customerModel + "'>" + item.customerModel + "</option>";
                                }
                            });
                            selModel = selModel + "</select>";
                            $tdModel.html(selModel);
                            $("[id='items." + rowIndex + ".productSpec']").select2();
                        } else {
                            $tdModel.html("<input type='text' class='model' id='items."+ rowIndex + ".productSpec' name='items["+ rowIndex + "].productSpec' value='' maxlength='100'/>");
                        }
                        if (b2bProductCodes.length > 0) {
                            var selB2BProductCode = "<select id='items."+ rowIndex + ".b2bProductCode' name='items["+ rowIndex + "].b2bProductCode' class='b2bspec' >";
                            selB2BProductCode = selB2BProductCode + "<option value=''></option>";
                            $.each(b2bProductCodes, function(i, item) {
                                selB2BProductCode = selB2BProductCode + "<option value='"+item+"'>" + item + "</option>";
                            });
                            selB2BProductCode = selB2BProductCode + "</select>";
                            $tdB2BProductCode.html(selB2BProductCode);
                            $("[id='items." + rowIndex + ".b2bProductCode']").select2();
                        } else {
                            $tdB2BProductCode.html("<input type='text' class='b2bspec' id='items."+ rowIndex + ".b2bProductCode' name='items["+ rowIndex + "].b2bProductCode' value='' maxlength='100' />");
                        }
                        return;
                    },
                    error : function(e)
                    {
                        ajaxLogout(e.responseText,null,"装载产品品牌错误，请重试!");
                    }
                });
        }

        return false;
    },
    //导入订单人工转单，编辑工单时获取产品属性（型号、品牌）
    //无B2B产品编码
    editForm_loadProductPropertiesForIMP:function (dataSourceId, customerId,product,rowIndex,defaultBrandId) {
        var self = this;
        var $tdBrand = $("#order_tr_" + rowIndex + " td:eq(4)");
        var $tdModel = $("#order_tr_" + rowIndex + " td:eq(5)");
        $tdBrand.html("");
        $tdModel.html("");
        if(!product){
            return;
        }
        if(!defaultBrandId || defaultBrandId == null || defaultBrandId == undefined){
            defaultBrandId = "0";
        }
        if(product.hasOwnProperty("brands") && product.hasOwnProperty("models") && product.hasOwnProperty("b2bProductCodes")) {
            if (product.brands.length > 0) {
                var selBrand = "<select id='items."+ rowIndex + ".brand' name='items["+ rowIndex + "].brand' data-productid='" + product.id + "' data-index='" + rowIndex + "' class='brand'>";
                $.each(product.brands, function(i, item) {
                    if(i==0 && defaultBrandId == "0") {
                        defaultBrandId = item.id;
                    }
                    selBrand = selBrand + "<option value='"+item.name+"' data-id='" + item.id + "' ";
                    if(defaultBrandId == item.id){
                        selBrand = selBrand + " selected ";
                    }
                    selBrand = selBrand + ">" + item.name + "</option>";
                });
                selBrand = selBrand + "</select>";
                $tdBrand.html(selBrand);
            } else {
                $tdBrand.html("<input type='text' class='brand' id='items."+ rowIndex + ".brand' name='items["+ rowIndex + "].brand' value='' maxlength='100' />");
            }
            if (product.models.length > 0) {
                var selModel = "<select id='items."+ rowIndex + ".productSpec' name='items["+ rowIndex + "].productSpec' class='spec'>";
                selModel = selModel + "<option value=''></option>";
                $.each(product.models, function(i, item) {
                    if(item.brandId == defaultBrandId) {
                        selModel = selModel + "<option value='" + item.customerModel + "'>" + item.customerModel + "</option>";
                    }
                });
                selModel = selModel + "</select>";
                $tdModel.html(selModel);
                $("[id='items." + rowIndex + ".productSpec']").select2();
            } else {
                $tdModel.html("<input type='text' class='spec' id='items."+ rowIndex + ".productSpec' name='items["+ rowIndex + "].productSpec' value='' maxlength='100' />");
            }

        } else {
            $.ajax(
                {
                    url : self.rootUrl+"/sd/order/createOrEdit/form_product_properties",
                    type : "GET",
                    data : {customerId: customerId,dataSourceId: dataSourceId,productId: product.id},
                    contentType : "application/json",
                    success : function(data)
                    {
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if(data.success == false){
                            layerError(data.message,"错误提示");
                            return;
                        }
                        var brands = data.data.brands || [];
                        var models = data.data.models || [];

                        Order.data.productMap[product.id].brands = brands;
                        Order.data.productMap[product.id].models = models;

                        if (brands.length > 0) {
                            var selBrand = "<select id='items."+ rowIndex + ".brand' name='items["+ rowIndex + "].brand' data-productid='" + product.id + "' data-index='" + rowIndex + "' class='brand' >";
                            $.each(brands, function(i, item) {
                                if(i==0 && defaultBrandId == "0") {
                                    defaultBrandId = item.id;
                                }
                                if(defaultBrandId == item.id){
                                    selBrand = selBrand + "<option value='"+item.name+"' data-id='" + item.id + "' selected>" + item.name + "</option>";
                                }else{
                                    selBrand = selBrand + "<option value='"+item.name+"' data-id='" + item.id + "'>" + item.name + "</option>";
                                }
                            });
                            selBrand = selBrand + "</select>";
                            $tdBrand.html(selBrand);
                        } else {
                            $tdBrand.html("<input type='text' class='brand' id='items."+ rowIndex + ".brand' name='items["+ rowIndex + "].brand' value='' maxlength='100' />");
                        }
                        if (models.length > 0) {
                            var selModel = "<select id='items."+ rowIndex + ".productSpec' name='items["+ rowIndex + "].productSpec' class='spec' >";
                            selModel = selModel + "<option value=''></option>";
                            $.each(models, function(i, item) {
                                if(item.brandId == defaultBrandId) {
                                    selModel = selModel + "<option value='" + item.customerModel + "'>" + item.customerModel + "</option>";
                                }
                            });
                            selModel = selModel + "</select>";
                            $tdModel.html(selModel);
                            $("[id='items." + rowIndex + ".productSpec']").select2();
                        } else {
                            $tdModel.html("<input type='text' class='spec' id='items."+ rowIndex + ".productSpec' name='items["+ rowIndex + "].productSpec' value='' maxlength='100'/>");
                        }
                        return;
                    },
                    error : function(e)
                    {
                        ajaxLogout(e.responseText,null,"装载产品品牌错误，请重试!");
                    }
                });
        }

        return false;
    },
    //导入订单人工转单，装载新订单项产品及账户余额,信用额度
    editForm_loadProductsForIMP:function (customerId,dataSourceId,showNew) {
        var self = this;
        $.ajax(
            {
                url : self.rootUrl+"/sd/order/createOrEdit/form_products",
                type : "GET",
                data : {customerId:customerId,action:"edit", dataSourceId:dataSourceId},
                contentType : "application/json",
                success : function(data) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.products.length == 0) {
                        $("#btnSubmit").prop("disabled", true);
                        layerAlert("你的帐号未设定服务产品。", "系统提示");
                        return;
                    }
                    //产品处理
                    if(data && data.products){
                        //按品类分组
                        var categoryGroup = groupBy(data.products, function (item) {
                            return item.categoryName;
                        },function (item){
                            return item.categoryId
                        });
                        // console.log(categoryGroup);
                        data['groups'] = categoryGroup;
                        //产品map, key:id,value:product
                        var productMap = arrayToMap(data.products, function (item) {
                            return item.id;
                        });
                        data['productMap'] = productMap;
                    }
                    Order.data = data;
                    Order.customerId = customerId;
                    //添加新产品
                    if (showNew && showNew == true) {
                        self.rownum = self.rownum + 1;
                        Order.data.rownum = self.rownum;
                        var tmpl = document.getElementById('editForm-new-row').innerHTML;
                        var doTtmpl = doT.template(tmpl);
                        var categoryId = undefined;
                        if (data && data.products) {
                            var trnew = doTtmpl(Order.data);
                            //添加服务产品选择行
                            $("#productTable > tbody").append(trnew);
                            //品类可选控制
                            categoryId = $("[id='category.id']").val() | '0';
                            Order.data['category'] = categoryId;
                            //禁用/启用分类
                            Order.disableProductOptions(categoryId);
                        }
                        var category = undefined;
                        if(categoryId){
                            for(var i = 0; i < categoryGroup.length; i++) {
                                var catg = categoryGroup[i];
                                if(catg.key === "" + categoryId){
                                    category = catg;
                                    break;
                                }
                            }
                        }else{
                            category = categoryGroup[0];
                        }
                        if(category){
                            var product = category.data[0];
                            var _product = $("#order_tr_" + self.rownum).find("#product");
                            _product.val(product.id);
                            _product.trigger("change");//此处触发产品选择控件事件，转载品牌，型号等
                        }
                        /*
                        var $tdBrand = $("#order_tr_" + self.rownum + " td:eq(4)");
                        var $tdModel = $("#order_tr_" + self.rownum + " td:eq(5)");
                        var $tdxpress = $("#order_tr_" + self.rownum + " td:eq(6)");
                        var brands = Order.data.products[0].brands || [];
                        var productSpecs = Order.data.products[0].models || [];
                        var expresses = Order.data.expresses || [];
                        var defaultBrandId = "0";
                        if (brands.length > 0) {
                            var selBrand = "<select id='items."+ self.rownum + ".brand' name='items["+ self.rownum + "].brand' data-index='"+ self.rownum + "' class='brand'>";
                            // selBrand = selBrand + "<option value=''></option>";
                            $.each(brands, function(i, item) {
                                if(i==0 && defaultBrandId == "0") {
                                    defaultBrandId = item.id;
                                }
                                selBrand = selBrand + "<option value='"+item.name+"' data-id='" + item.id + "' ";
                                if(defaultBrandId == item.id){
                                    selBrand = selBrand + " selected ";
                                }
                                selBrand = selBrand + ">" + item.name + "</option>";
                                // selBrand = selBrand + "<option value='"+item+"'>" + item + "</option>";
                            });
                            selBrand = selBrand + "</select>";
                            $tdBrand.html(selBrand);
                        } else {
                            $tdBrand.html("<input type='text' class='brand' id='items."+ self.rownum + ".brand' name='items["+ self.rownum + "].brand' value='' maxlength='100'/>");
                        }
                        if (productSpecs.length > 0) {
                            var selModel = "<select id='items."+ self.rownum + ".productSpec' name='items["+ self.rownum + "].productSpec' class='spec' >";
                            selModel = selModel + "<option value=''></option>";
                            $.each(productSpecs, function(i, item) {
                                if(item.brandId == defaultBrandId) {
                                    selModel = selModel + "<option value='" + item.customerModel + "'>" + item.customerModel + "</option>";
                                }
                                // selModel = selModel + "<option value='"+item+"'>" + item + "</option>";
                            });
                            selModel = selModel + "</select>";
                            $tdModel.html(selModel);
                            $("[id='items." + self.rownum + ".productSpec']").select2();
                        } else {
                            $tdModel.html("<input type='text' class='spec' id='items."+ self.rownum + ".productSpec' name='items["+ self.rownum + "].productSpec' value='' maxlength='100' />");
                        }
                        if (expresses.length > 0) {
                            var selExpress = "<select id='items."+ self.rownum + ".expressCompany' name='items["+ self.rownum + "].expressCompany' class='input-large noselect2' style='width:120px'>";
                            selExpress = selExpress + "<option value=''></option>";
                            $.each(expresses, function(i, item) {
                                selExpress = selExpress + "<option value='"+item.value+"'>" + item.label + "</option>";
                            });
                            selExpress = selExpress + "</select>";
                            $tdxpress.html(selExpress);
                        } else {
                            $tdxpress.html("<input type='text' class='b2bspec' id='items."+ self.rownum + ".expressCompany' name='items["+ self.rownum + "].expressCompany' value='' maxlength='30' />");
                        }
                        */
                    }
                    $("#btnSubmit").prop("disabled",false);
                },
                error : function()
                {
                    ajaxLogout(e.responseText,null,"装载产品错误，请重试!");
                }
            });
    },
    //B2B转单：下单或编辑工单时获取产品属性（型号、品牌、b2b产品编码）
    editForm_loadProductPropertiesForB2B:function (dataSourceId, customerId,product,rowIndex,defaultBrandId) {
        var self = this;
        var $tdBrand = $("#order_tr_" + rowIndex + " td:eq(4)");
        var $tdModel = $("#order_tr_" + rowIndex + " td:eq(5)");
        var $tdB2BProductCode = $("#order_tr_" + rowIndex + " td:eq(6)");
        $tdBrand.html("");
        $tdModel.html("");
        $tdB2BProductCode.html("");
        if(!product){
            return;
        }
        if(!defaultBrandId || defaultBrandId == null || defaultBrandId == undefined){
            defaultBrandId = "0";
        }
        if(product.hasOwnProperty("brands") && product.hasOwnProperty("models") && product.hasOwnProperty("b2bProductCodes")) {
            if (product.brands.length > 0) {
                var selBrand = "<select id='items."+ rowIndex + ".brand' name='items["+ rowIndex + "].brand' data-productid='" + product.id + "' data-index='" + rowIndex + "' class='brand' style='width:80px'>";
                $.each(product.brands, function(i, item) {
                    if(i==0 && defaultBrandId == "0") {
                        defaultBrandId = item.id;
                    }
                    selBrand = selBrand + "<option value='"+item.name+"' data-id='" + item.id + "' ";
                    if(defaultBrandId == item.id){
                        selBrand = selBrand + " selected ";
                    }
                    selBrand = selBrand + ">" + item.name + "</option>";
                });
                selBrand = selBrand + "</select>";
                $tdBrand.html(selBrand);
            } else {
                $tdBrand.html("<input type='text' class='model' id='items."+ rowIndex + ".brand' name='items["+ rowIndex + "].brand' value='' maxlength='100' style='width:120px'/>");
            }
            if (product.models.length > 0) {
                var selModel = "<select id='items."+ rowIndex + ".productSpec' name='items["+ rowIndex + "].productSpec' class='spec' style='width:120px'>";
                selModel = selModel + "<option value=''></option>";
                $.each(product.models, function(i, item) {
                    if(item.brandId == defaultBrandId) {
                        selModel = selModel + "<option value='" + item.customerModel + "'>" + item.customerModel + "</option>";
                    }
                });
                selModel = selModel + "</select>";
                $tdModel.html(selModel);
                $("[id='items."+ rowIndex + ".productSpec'").select2();
            } else {
                $tdModel.html("<input type='text' class='model' id='items."+ rowIndex + ".productSpec' name='items["+ rowIndex + "].productSpec' value='' maxlength='100' style='width:120px'/>");
            }
            if (product.b2bProductCodes.length > 0) {
                var selB2BProductCode = "<select id='items."+ rowIndex + ".b2bProductCode' name='items["+ rowIndex + "].b2bProductCode' class='b2bspec' style='width:120px'>";
                selB2BProductCode = selB2BProductCode + "<option value=''></option>";
                $.each(product.b2bProductCodes, function(i, item) {
                    selB2BProductCode = selB2BProductCode + "<option value='"+item+"'>" + item + "</option>";
                });
                selB2BProductCode = selB2BProductCode + "</select>";
                $tdB2BProductCode.html(selB2BProductCode);
                $("[id='items."+ rowIndex + ".b2bProductCode'").select2();
            } else {
                $tdB2BProductCode.html("<input type='text' class='model' id='items."+ rowIndex + ".b2bProductCode' name='items["+ rowIndex + "].b2bProductCode' value='' maxlength='100' style='width:120px'/>");
            }

        }else{
            $.ajax(
                {
                    url : self.rootUrl+"/sd/order/createOrEdit/form_product_properties",
                    type : "GET",
                    data : {customerId: customerId,dataSourceId: dataSourceId,productId: product.id},
                    contentType : "application/json",
                    success : function(data)
                    {
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if(data.success == false){
                            layerError(data.message,"错误提示");
                            return;
                        }
                        var brands = data.data.brands || [];
                        var models = data.data.models || [];
                        var b2bProductCodes = data.data.b2bProductCodes || [];

                        Order.data.productMap[product.id].brands = brands;
                        Order.data.productMap[product.id].models = models;
                        Order.data.productMap[product.id].b2bProductCodes = b2bProductCodes;

                        if (brands.length > 0) {
                            var selBrand = "<select id='items."+ rowIndex + ".brand' name='items["+ rowIndex + "].brand' data-productid='" + product.id + "' data-index='" + rowIndex + "' class='brand' >";
                            $.each(brands, function(i, item) {
                                if(i==0 && defaultBrandId == "0") {
                                    defaultBrandId = item.id;
                                }
                                if(defaultBrandId == item.id){
                                    selBrand = selBrand + "<option value='"+item.name+"' data-id='" + item.id + "' selected>" + item.name + "</option>";
                                }else{
                                    selBrand = selBrand + "<option value='"+item.name+"' data-id='" + item.id + "'>" + item.name + "</option>";
                                }
                            });
                            selBrand = selBrand + "</select>";
                            $tdBrand.html(selBrand);
                        } else {
                            $tdBrand.html("<input type='text' class='model' id='items."+ rowIndex + ".brand' name='items["+ rowIndex + "].brand' value='' maxlength='100' style='width:80px'/>");
                        }
                        if (models.length > 0) {
                            var selModel = "<select id='items."+ rowIndex + ".productSpec' name='items["+ rowIndex + "].productSpec' class='spec'>";
                            selModel = selModel + "<option value=''></option>";
                            $.each(models, function(i, item) {
                                if(item.brandId == defaultBrandId) {
                                    selModel = selModel + "<option value='" + item.customerModel + "'>" + item.customerModel + "</option>";
                                }
                            });
                            selModel = selModel + "</select>";
                            $tdModel.html(selModel);
                            $("[id='items."+ rowIndex + ".productSpec'").select2();
                        } else {
                            $tdModel.html("<input type='text' class='model' id='items."+ rowIndex + ".productSpec' name='items["+ rowIndex + "].productSpec' value='' maxlength='100' style='width:120px'/>");
                        }
                        if (b2bProductCodes.length > 0) {
                            var selB2BProductCode = "<select id='items."+ rowIndex + ".b2bProductCode' name='items["+ rowIndex + "].b2bProductCode' class='b2bspec' style='width:120px'>";
                            selB2BProductCode = selB2BProductCode + "<option value=''></option>";
                            $.each(b2bProductCodes, function(i, item) {
                                selB2BProductCode = selB2BProductCode + "<option value='"+item+"'>" + item + "</option>";
                            });
                            selB2BProductCode = selB2BProductCode + "</select>";
                            $tdB2BProductCode.html(selB2BProductCode);
                            $("[id='items."+ rowIndex + ".b2bProductCode'").select2();
                        } else {
                            $tdB2BProductCode.html("<input type='text' class='b2bspec' id='items."+ rowIndex + ".b2bProductCode' name='items["+ rowIndex + "].b2bProductCode' value='' maxlength='100' style='width:120px'/>");
                        }
                        return;
                    },
                    error : function(e)
                    {
                        ajaxLogout(e.responseText,null,"装载产品品牌错误，请重试!");
                    }
                });
        }

        return false;
    },
    //修改-添加订单项(进入订单时调用)
    editForm_addItemRows:function(data) {
        var self = this;
        if ($(".dark-tooltip").length > 0) {
            $(".dark-tooltip").remove();
        }
        if (!data || !data.hasOwnProperty("items")) {
            return;
        }
        $("#productTable tr.data").remove();
        self.rownum = 0;
        var tmpl = document.getElementById('editForm-item-table-row').innerHTML;
        var doTtmpl = doT.template(tmpl);
        var trnew = doTtmpl(data);

        if ($("#addrow").length > 0) {
            $(trnew).insertBefore($("#addrow"));
        } else {
            $(trnew).insertAfter($("#productTable tr:last"));
        }
        self.hideChargeColumnsForEditForm();
        $("select.spec").select2();
        $("select.b2bspec").select2();
        self.rownum = data.items.length-1;

        $('#lblassignedCharge').text(data.expectCharge);
        $('#expectCharge').val(data.expectCharge);
        $("#lblblockedCharge").text(data.blockedCharge);
        $('#blockedCharge').val(data.blockedCharge);
        $("#lbltotalQty").text(data.totalQty);
        $("#totalQty").val(data.totalQty);
        var customerUrgentCharge = 0;
        if($("#customerUrgentCharge").length>0){
            customerUrgentCharge = parseFloat($("#customerUrgentCharge").val());
        }
        var totalCharge = data.blockedCharge + data.expectCharge + customerUrgentCharge;
        $("#lbltotalCharge").text(totalCharge.toFixed(2));

        var balance = parseFloat($("#balance").val());
        var credit= parseFloat($("#credit").val());
        if (balance + credit < data.blockedCharge + data.expectCharge) {
            $("#lbltotalCharge").removeClass("alert-success").addClass("alert-error");
            $("#btnSubmit").removeClass("btn-primary").addClass("btn-danger").addClass("disabled");
            //$("#btnSubmit").attr("disalbed", "disabled");
            $("#btnSubmit").prop("disabled",true);
        } else {
            $("#lbltotalCharge").removeClass("alert-error").addClass("alert-success");
            $("#btnSubmit").removeClass("btn-danger").removeClass("disabled").addClass("btn-primary");
            $("#btnSubmit").removeAttr('disabled');
        }
        $("#remarks").val("");//清空新增行备注
    },
    //修改-删除订单项
    editForm_DelRow:function(index){
        $("[id='order_tr_"+index+"']").remove();
    },
    //新增问题反馈窗口
    feedback:function(orderId,quarter,parentIndex){
        if (!orderId) {
            return false;
        }
        var self = this;
        var replyIdex = top.layer.open({
            type: 2,
            id:'layer_feedback',
            zIndex: 19891015,
            title:'问题反馈',
            content: self.rootUrl+"/sd/feedback/form?type=order&order.id=" + orderId + "&quarter=" + quarter+ "&parentIndex=" + (parentIndex || ''),
            shade: 0.3,
            shadeClose: true,
            area: ['800px', '480px'],
            maxmin: false,
            success: function(layero){},
            end: function(){}
        });
    },
    //打开问题反馈列表，并可回复
    replylist:function(feedbackId,quarter,orderNo,orderId) {
        if (!feedbackId || !quarter) {
            return false;
        }
        var self = this;
        var title = (orderNo || '') + '问题反馈';
        if(!Utils.isNull(orderId)){
            var title = '<a style="cursor:pointer;" onclick="Order.viewOrderDetail(' + "'" + orderId  + "','" + (quarter || '') +"'" +');">' + (orderNo || '') + '问题反馈</a>';
        }
        var replyIdex = top.layer.open({
            type: 2,
            id:'layer_replylist',
            zIndex: 19891015,
            title: title,
            move: false,
            // title: (orderNo || '') + '问题反馈',
            content: self.rootUrl+"/sd/feedback/replylist?id="+ feedbackId + "&quarter=" + quarter,
            shade: 0.3,
            area: ['810px', '565px'],
            shadeClose: true,
            maxmin: false,
            success: function(layero){
            }
        });

        //top.layer.full(replyIdex);
    },
    //添加问题反馈(提交)
    addFeedbackReply:function (id,quarter){
        if ($("#btnSend").hasClass("disabled") == true)
        {
            return true;
        }
        var remarksVal = $("#remarks").val();
        if (remarksVal == '')
        {
            layerAlert("请输入回复内容");
            return true;
        }
        var forbiddenArray = filterForbiddenStr(remarksVal);
        if(forbiddenArray != null){
            layerAlert("内容含<font color='#4EB4E4'>【" + forbiddenArray.toLocaleString() + "】</font>等不文明用语,请注意用词文明！","提示");
            return false;
        }
        /*
        var checkResult = hasForbiddenStr(remarksVal);
        if(checkResult === true){
            layerAlert("请注意检查，使用文明用语沟通！","提示");
            return false;
        }*/
        var self = this;
        var reply = {feedbackId:id,quarter:quarter};
        reply.remarks = remarksVal;
        $.ajax({
            cache : false,
            type : "POST",
            url : self.rootUrl + "/sd/feedback/reply",
            data : reply,
            success : function(data)
            {
                if(ajaxLogout(data)){
                    return false;
                }
                if (data.success)
                {
                    self.addReplyItem(data.data);
                    $("#remarks").val("").focus();
                }else
                {
                    layerError("保存回复内容错误:" + data.message, "错误提示");
                }
            },
            error : function(e)
            {
                ajaxLogout(e.responseText,null,"保存回复内容错误，请重试!");
            }
        });
    },
    //添加图片窗口
    addReplyAttach:function(feedbackId,quarter,feedbackLayerIndex){
        if (!feedbackId) {
            return false;
        }
        var self = this;
        var replyIdex = top.layer.open({
            type: 2,
            id: 'layer_replylAttach',
            zIndex: 19891016,
            title: '附件图片',
            content: self.rootUrl+"/sd/feedback/replyAttach?id="+ feedbackId + "&quarter=" + quarter +"&parentIndex=" + (feedbackLayerIndex || ''),
            shade: 0.3,
            area: ['1200px', '710px'],
            maxmin: false,
            success: function(layero){

            }
        });
    },
    //动态添加问题反馈内容（ui）
    addReplyItem:function(data){
        var tmpl = document.getElementById('tpl-reply-content').innerHTML;
        var doTtmpl = doT.template(tmpl);
        var reply = doTtmpl(data);
        $("#chat_chatmsglist").append(reply);
    },
    //处理反馈异常
    handled:function(orderId,quarter){
        var self = this;
        var $btn = $("#btnHandled");
        if($btn.attr("disabled") == "disabled"){
            return false;
        }
        var data = {orderId:orderId,quarter:quarter};
        $.ajax({
            cache : false,
            type : "POST",
            url : self.rootUrl+"/sd/feedback/handled",
            data : data,
            success : function(data)
            {
                if(ajaxLogout(data)){
                    return false;
                }
                if (data.success)
                {
                    layerInfo("订单反馈异常处理成功.","系统提示");
                    setTimeout(function() {
                        loadMyMessages( self.rootUrl);//刷新数量
                        try {
                            var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                            pframe.repage();
                            $btn.hide();
                        }catch(err) {}
                    }, 1000);
                }else
                {
                    $btn.removeAttr("disabled");
                    layerError("订单异常处理错误:" + data.message, "错误提示");
                }
                return false;
            },
            error : function(e)
            {
                $btn.removeAttr("disabled");
                ajaxLogout(e.responseText,null,"订单异常处理错误，请重试!");
            }
        });
        return false;
    },
    //标记已读
    readReply:function(id,quarter){
        if(!id){
            return false;
        }
        var self = this;
        $.ajax({
            type : "POST",
            url : self.rootUrl+"/sd/feedback/read?id="+id + "&quarter=" + quarter,
            data : null,
            success : function(data)
            {
                if(ajaxLogout(data)){
                    return false;
                }
                var pframe = getActiveTabIframe();//定义在jeesite.min.js中
                if(pframe != undefined){
                    $("img[id='complain_"+id+"']", pframe.document).hide();
//                        pframe.repage();
                }
                loadMyMessages(self.rootUrl);//刷新数量
            },
            error : function(e)
            {
                // layerError("更新问题反馈状态错误:" + e, "错误提示");
                ajaxLogout(e.responseText,null,"更新问题反馈状态错误，请关闭并重新进入此页面!");
            }
        });
    },
    //异常处理
    addPending:function(orderId,quarter){
        if (!orderId) {
            return false;
        }
        var self = this;
        var pendig_Index = top.layer.open({
            type: 2,
            id: 'layer_pending_service',
            zIndex: 19891015,
            title: '上门服务',
            content: self.rootUrl + "/sd/pending/form?orderId="+orderId + "&quarter=" + (quarter || ''),
            shade: 0.3,
            area: ['810px', '610px'],
            maxmin: false,
            success: function(layero){
            }
        });
        top.layer.full(pendig_Index);
        /*
        var mywin = window.open(self.rootUrl+"/sd/pending/form?orderId="+orderId,'orderpenddingwindow',"dependent=yes,fullscreen=yes,resizable=yes,scrollbars=yes,status=yes,modal=yes,alwaysRaised=yes,minimizable=no,top=150,left=200,toolbar=no,menubar=yes,location=no");
        mywin.moveTo(0, 0);
        mywin.resizeTo(screen.availWidth,screen.availHeight);
        */
    },
    //编辑订单时，删除选中项后，重新获得订单项，并动态添加在ui中
    delProductRows:function(){
        var self = this;
        if($("#productTable input[type='checkbox'][name='checkedRecords']:checkbox:checked").length == 0){
            clickTag = 0;
            layerInfo("请选择要删除的明细.","系统提示");
            return;
        }
        var itemids = [];

        $("#productTable input[type='checkbox'][name='checkedRecords']:checkbox:checked").each(function(){
            var id = $(this).val();
            var itemid = $("[id='items["+id+"].id']").val();
            if(!Utils.isEmpty(itemid) && itemid !='0' && itemid != 'undefined' && itemid != undefined){
                itemids.push(itemid);
            }
            else{
                itemid = $("[id='items["+id+"].tmpId']").val();
                if(!Utils.isEmpty(itemid)){
                    itemids.push(itemid);
                }
            }
        });

        if(itemids.length>0){
            var data = {ids:itemids.join(","),balance:$("#balance").val(),credit:$("#credit").val()};
            $.ajax({
                url: self.rootUrl+"/sd/order/delnewitem?"+ (new Date()).getTime(),
                type: "POST",
                data: data,
                cache:false,
                complete: function () {
                    setTimeout(function(){
                        clickTag = 0;
                    },1000);
                },
                success: function(data, status) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if(data && data.success == true){
                        self.addItemRows(data.data);
                        $("#selectAll").attr("checked", false);
                    }
                    else if( data && data.message){
                        layerError(data.message,"错误提示");
                    }
                    else{
                        layerError("删除订单详细清单时错误","错误提示");
                    }
                },
                error: function(request, status, error) {
                    ajaxLogout(e.responseText,null,"删除订单详细清单时错误，请重试!");
                }
            });
        }else{
            clickTag = 0;
        }
        return false;
    },
    //接单
    accept:function(id,quarter,type,parentIndex){
        var self = this;
        if(self.clickTag === 1){
            return;
        }
        self.clickTag = 1;
        var $btn = $("#btnAccept");
        $btn.attr('disabled', 'disabled');
        //type 0-调用来自订单列表 1-调用来自订单详情页
        var loadingIndex;
        var ajaxSuccess = 0;
        var data = {id: id,quarter:quarter,remarks:""};
        $.ajax({
                async: false,
                cache: false,
                type: "POST",
                url: self.rootUrl+"/sd/order/accept",
                data: data,
                beforeSend: function () {
                    loadingIndex = layer.msg('处理中，请稍等...', {
                        icon: 16,
                        time: 0,
                        shade: 0.3
                    });
                },
                complete: function () {
                    if(loadingIndex) {
                        layer.close(loadingIndex);
                    }
                    if(ajaxSuccess == 0) {
                        setTimeout(function () {
                            self.clickTag = 0;
                            $btn.removeAttr('disabled');
                        }, 2000);
                    }
                },
                success: function (data) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.success) {
                        if(type && type ==1) {
                            //from orderdeatil
                            if(parentIndex && parentIndex != undefined && parentIndex != ''){
                                var layero = $("#layui-layer" + parentIndex, top.document);
                                var iframeWin = top[layero.find('iframe')[0]['name']];
                                iframeWin.reload();
                            }
                        }else {
                            //列表
                            $("#searchForm").submit();
                        }
                    }else{
                        layerError("接单错误:" + data.message, "错误提示");
                    }
                },
                error: function (e) {
                    ajaxLogout(e.responseText,null,"接单错误，请重试!");
                }
            });
    },
    //订单日志-跟踪进度
    showTrackingLogs:function(id,quarter,isCustomer){
        var self = this;
        if($("#tb_tracking").length == 0){
            $.ajax({
                cache: false,
                type: "GET",
                url: self.rootUrl+"/sd/order/trackingLog?orderId=" + id + "&isCustomer=" + (isCustomer || '') + "&quarter=" + (quarter || ''),
                dataType: 'json',
                success: function (data) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.success) {
                        if(data.data && data.data.length>0) {
                            var tplId = "tpl-tracking";
                            if(isCustomer && isCustomer == 'true'){
                                tplId = "tpl-customer-tracking";
                            }
                            var tmpl = document.getElementById(tplId).innerHTML;
                            var doTtmpl = doT.template(tmpl);
                            var html = doTtmpl(data.data);
                            $("#tabTracking").append(html);
                        }else{
                            $("#tabTracking").html("无记录");
                        }
                    }else{
                        layerError(data.message,"错误提示");
                    }
                },
                error: function (e) {
                    ajaxLogout(e.responseText,null,"装载跟踪进度错误，请重试!");
                }
            });
        }
    },
    //订单详情-跟踪进度
    orderDetail_Tracking:function(id,quarter,status){
        var self = this;
        if($("#tb_tracking").length == 0){
            $.ajax({
                cache: false,
                type: "GET",
                url: self.rootUrl+"/sd/order/orderDetailTracking?orderId=" + id+ "&quarter=" + (quarter || '') + "&status=" + (status || ''),
                dataType: 'json',
                success: function (data) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.success && data.success == true) {
                        if(data.data) {
                            var tplId = "tpl-tracking";
                            var tmpl = document.getElementById(tplId).innerHTML;
                            var doTtmpl = doT.template(tmpl);
                            data.ctx = self.rootUrl;
                            var html = doTtmpl(data.data);
                            $("#tabTracking").append(html);
                        }else{
                            $("#tabTracking").html("无记录");
                        }
                    }else{
                        layerError(data.message,"错误提示");
                    }
                },
                error: function (e) {
                    layerError("装载跟踪进度:" + e, "错误提示");
                }
            });
        }
    },

    //订单详情-跟踪进度(分页获取)
    orderDetail_TrackingNew:function(id,quarter,status){
        var self = this;
        if($("#tb_tracking").length == 0){
            $.ajax({
                cache: false,
                type: "GET",
                url: self.rootUrl+"/sd/order/orderDetailTrackingNew?orderId=" + id+ "&quarter=" + (quarter || '') + "&status=" + (status || '')+"&pageNo=0",
                dataType: 'json',
                success: function (data) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.success && data.success == true) {
                        if(data.data) {
                            var tplId = "tpl-tracking";
                            var tmpl = document.getElementById(tplId).innerHTML;
                            var doTtmpl = doT.template(tmpl);
                            data.ctx = self.rootUrl;
                            var html = doTtmpl(data.data);
                            $("#tabTracking").append(html);
                        }else{
                            $("#tabTracking").html("无记录");
                        }
                    }else{
                        layerError(data.message,"错误提示");
                    }
                },
                error: function (e) {
                    layerError("装载跟踪进度:" + e, "错误提示");
                }
            });
        }
    },

    //订单日志-异常处理
    showExceptLogs:function(id,quarter){
        var self = this;
        if($("#tb_except").length == 0){
            $.ajax({
                cache: false,
                type: "GET",
                url: self.rootUrl+"/sd/order/exceptLog?orderId=" + id + "&quarter=" + (quarter || ''),
                dataType: 'json',
                success: function (data) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.success ) {
                        if(data.data && data.data.length>0) {
                            var tmpl = document.getElementById('tpl-except').innerHTML;
                            var doTtmpl = doT.template(tmpl);
                            var html = doTtmpl(data.data);
                            $("#tabException").append(html);
                        }else {
                            $("#tabException").append("<table id='tb_except' style='display:none;'></table>无记录");
                        }
                    }else{
                        layerError(data.message);
                    }
                },
                error: function (e) {
                    ajaxLogout(e.responseText,null,"装载异常处理错误，请重试!");
                }
            });
        }
    },
    //订单日志-问题反馈
    showFeedbackLogs:function(feedbackId,quarter){
        var self = this;
        if($("#tb_feedback").length == 0){
            $.ajax({
                cache: false,
                type: "GET",
                url: self.rootUrl+"/sd/order/feedbackLog?id=" + feedbackId+ "&quarter=" + (quarter || ''),
                dataType: 'json',
                success: function (data) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.success) {
                        if(data.data && data.data.length>0) {
                            var tmpl = document.getElementById('tpl-feedback').innerHTML;
                            var doTtmpl = doT.template(tmpl);
                            var html = doTtmpl(data.data);
                            $("#tabFeedback").append(html);
                        }else{
                            $("#tabFeedback").html("无记录");
                        }
                    }else{
                        layerError(data.message, "错误提示");
                    }
                },
                error: function (e) {
                    ajaxLogout(e.responseText,null,"装载反馈错误，请重试!");
                }
            });
        }
    },
    //订单详情-退补单
    showCustomerReturnAndAdditionalList:function(orderId,orderNo,quarter){
        var self = this;
        if($("#tb_cutomerReturn").length == 0){
            $.ajax({
                cache: false,
                type: "GET",
                url: self.rootUrl+"/sd/order/customerReturnAndAdditionalList?orderId=" + orderId+ "&quarter=" + (quarter || '') + "&orderNo=" + (orderNo || ''),
                dataType: 'json',
                success: function (data) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.success) {
                        if(data.data && data.data.length>0) {
                            var tmpl = document.getElementById('tpl-cutomerReturn').innerHTML;
                            var doTtmpl = doT.template(tmpl);
                            var html = doTtmpl(data.data);
                            $("#tabCustomerReturn").append(html);
                        }else{
                            $("#tabCustomerReturn").append("<table id='tb_cutomerReturn' style='display:none;'></table>无记录");
                        }
                    }else{
                        layerError(data.message, "出错了!");
                    }
                },
                error: function (e) {
                    ajaxLogout(e.responseText,null,"装载退补错误，请重试!");
                }
            });
        }
    },
    //浏览订单明细(查看 for customer,engineer)
    viewOrderDetail:function(id,quarter,layerId){
        var self = this;
        var zindex = 19891015;
        if(layerId){
            zindex = 19891019;
        }
        var orderDetail_index = top.layer.open({
            type: 2,
            id: layerId || 'layer_orderdetail',
            zIndex: zindex,
            title:'订单详情',
            content: self.rootUrl+"/sd/order/orderDetailInfo?id="+ id+ "&quarter=" + (quarter || ''),
            shade: 0.3,
            shadeClose: true,
            area:['1200px','800px'],
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
        // top.layer.full(orderDetail_index);
        setCookie('layer.parent.id',orderDetail_index);
    },
    //弹窗显示订单明细(for 客服)
    showOrderDetail:function(id,quarter,refreshParent){
        var self = this;
        var h = $(top.window).height();
        var w = $(top.window).width();
        if(!refreshParent){
            refreshParent = 'true';
        }
        var orderDetail_index = top.layer.open({
            type: 2,
            id: 'layer_orderdetail',
            zIndex: 19891015,
            title: '订单详情',
            content: self.rootUrl+"/sd/order/kefu/orderDetailInfo?id="+ id + "&quarter=" + (quarter || '') +"&refreshParent=" + (refreshParent || ''),
            shade: 0.3,
            area:[(w-40)+'px',(h-40)+'px'],
            maxmin: false,
            success: function(layero,index){
                // top.layer.style(index, {
                //     /*'margin-top': '5px',*/
                //     'width': (w-40) + 'px',
                //     'height':(h-40) + 'px',
                //     'left':'20px',
                //     'top':'20px'
                // });
            },
            end:function(){
                var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                if(iframe != undefined){
                    var repageFlag = $("#repageFlag",iframe.document).val();
                    if(repageFlag == "true"){
                        iframe.repage();
                    }
                }
            }
        });
        setCookie('layer.parent.id',orderDetail_index);
    },
    showKefuOrderDetail:function(id,quarter,orderType,refreshParent){
        var self = this;
        var h = $(top.window).height();
        var w = $(top.window).width();
        if(!refreshParent){
            refreshParent = 'true';
        }
        var realUrl = self.rootUrl+"/sd/order/kefuOrderList/service/orderDetailInfo?id="+ id + "&quarter=" + (quarter || '') +"&refreshParent=" + (refreshParent || '');
        if(orderType == 3 || orderType === 4){
            realUrl = self.rootUrl+"/sd/order/kefuOrderList/service/orderDetailInfoForReturn?id="+ id + "&quarter=" + (quarter || '') +"&refreshParent=" + (refreshParent || '');
        }
        var orderDetail_index = top.layer.open({
            type: 2,
            id:'layer_orderdetail',
            zIndex: 19891015,
            title:'订单详情',
            content: realUrl,
            shade: 0.3,
            area:[(w-40)+'px',(h-40)+'px'],
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
                var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                if(iframe != undefined){
                    var repageFlag = $("#repageFlag",iframe.document).val();
                    if(repageFlag == "true"){
                        iframe.repage();
                    }
                }
            }
        });
        setCookie('layer.parent.id',orderDetail_index);
    },
    //客服历史派单订单详情页
    showKefuHistoryOrderDetail:function(id,quarter){
        var self = this;
        var h = $(top.window).height();
        var w = $(top.window).width();
        var orderDetail_index = top.layer.open({
            type: 2,
            id:'layer_history_orderdetail',
            zIndex: 19891019,
            title: '订单详情',
            content: self.rootUrl+"/sd/order/kefuOrderList/service/historyOrderDetailInfo?id="+ id + "&quarter=" + (quarter || ''),
            shade: 0.3,
            area:[(w-40)+'px',(h-40)+'px'],
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
        setCookie('layer.parent.id',orderDetail_index);
    },
    //回访订单详情页
    showFollowUpFailOrderDetail:function(id,quarter,orderType,refreshParent){
        var self = this;
        var h = $(top.window).height();
        var w = $(top.window).width();
        if(!refreshParent){
            refreshParent = 'true';
        }
        var realUrl = self.rootUrl+"/sd/order/kefuOrderList/service/orderDetailInfoForFollowUp?id="+ id + "&quarter=" + (quarter || '') +"&refreshParent=" + (refreshParent || '');
        if(orderType == 3 || orderType === 4){
            realUrl = self.rootUrl+"/sd/order/kefuOrderList/service/orderDetailInfoForReturn?id="+ id + "&quarter=" + (quarter || '') +"&refreshParent=" + (refreshParent || '');
        }
        var orderDetail_index = top.layer.open({
            type: 2,
            id:'layer_orderdetail',
            zIndex: 19891015,
            title:'订单详情',
            content: realUrl,
            shade: 0.3,
            area:[(w-40)+'px',(h-40)+'px'],
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
                var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                if(iframe != undefined){
                    var repageFlag = $("#repageFlag",iframe.document).val();
                    if(repageFlag == "true"){
                        iframe.repage();
                    }
                }
            }
        });
        setCookie('layer.parent.id',orderDetail_index);
    },
    //弹窗显示订单明细(for 客服派单)
    showOrderDetailForPlan:function(id,quarter,refreshParent){
        var self = this;
        var h = $(top.window).height();
        var w = $(top.window).width();
        if(!refreshParent){
            refreshParent = 'true';
        }
        var orderDetail_index = top.layer.open({
            type: 2,
            id:'layer_orderdetail',
            zIndex: 19891015,
            title:'订单详情',
            content: self.rootUrl+"/sd/order/plan/orderDetailInfo?id="+ id + "&quarter=" + (quarter || '') +"&refreshParent=" + (refreshParent || ''),
            shade: 0.3,
            area:[(w-40)+'px',(h-40)+'px'],
            maxmin: false,
            success: function(layero,index){
                setCookie('layer.parent.id',index);
            },
            end:function(){
                var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                if(iframe != undefined){
                    var repageFlag = $("#repageFlag",iframe.document).val();
                    if(repageFlag == "true"){
                        iframe.repage();
                    }
                }
            }
        });
    },
    //弹窗显示订单明细(for 客服派单)
    showKefuOrderDetailForPlan:function(id,quarter,orderType,refreshParent){
        var self = this;
        var h = $(top.window).height();
        var w = $(top.window).width();
        if(!refreshParent){
            refreshParent = 'true';
        }
        var realUrl = self.rootUrl+"/sd/order/kefuOrderList/service/orderDetailInfoForPlan?id="+ id + "&quarter=" + (quarter || '') +"&refreshParent=" + (refreshParent || '');
        if(orderType == 3 || orderType === 4){
            realUrl = self.rootUrl+"/sd/order/kefuOrderList/service/orderDetailInfoForReturn?id="+ id + "&quarter=" + (quarter || '') +"&refreshParent=" + (refreshParent || '');
        }
        var orderDetail_index = top.layer.open({
            type: 2,
            id:'layer_orderdetail',
            zIndex: 19891015,
            title:'订单详情',
            content: realUrl,
            shade: 0.3,
            area:[(w-40)+'px',(h-40)+'px'],
            maxmin: false,
            success: function(layero,index){
                setCookie('layer.parent.id',index);
            },
            end:function(){
                var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                if(iframe != undefined){
                    var repageFlag = $("#repageFlag",iframe.document).val();
                    if(repageFlag == "true"){
                        iframe.repage();
                    }
                }
            }
        });
    },
    //显示详情页订单操作工具栏(showOrderDetailForPlan页面)
    detailPlan_toolbar:function(order){
        var tmpl = document.getElementById('tpl-order-toolbar').innerHTML;
        var doTtmpl = doT.template(tmpl);
        var html = doTtmpl(order);
        $("#toolbar").html("").append(html);
        return;
    },
    //派单
    plan:function(id,no,quarter,parentIndex){
        var self = this;
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_plan',
            zIndex: 19891015,
            title:'派单 ['+no+']',
            content: self.rootUrl+"/sd/order/plan?orderId="+ id +'&quarter=' + (quarter || '') + "&parentIndex=" + (parentIndex || '') ,
            area: ['980px', '640px'],
            shade: 0.3,
            maxmin: true,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    crushPlan:function(id,no,quarter,parentIndex,crushPlanFlag) {
        var self = this;
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_plan',
            zIndex: 19891015,
            title:'派单 ['+no+']',
            content: self.rootUrl+"/sd/order/plan?orderId="+ id +'&quarter=' + (quarter || '') + "&parentIndex=" + (parentIndex || '') + "&crushPlanFlag="+(crushPlanFlag || 0),
            area: ['980px', '640px'],
            shade: 0.3,
            maxmin: true,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //网点派单给具体的安维人员
    servicePointPlan:function(id,no,quarter){
        var self = this;
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_return',
            zIndex: 19891015,
            title:'派单 ['+no+']',
            content: self.rootUrl+"/sd/order/servicepointplan?orderId="+ id + "&quarter=" + (quarter || ''),
            area: ['1000px', '620px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            }
        });
    },
    //申请退单(客服)
    returnOrder:function(id,no,quarter,parentIndex){
        var self = this;
        var returnIndex = top.layer.open({
            type: 2,
            id:'layer_return',
            zIndex: 19891015,
            title:'退单 ['+no+']',
            content: self.rootUrl+"/sd/order/return?orderId="+ id + "&quarter=" + (quarter || '') + "&parentIndex=" + (parentIndex || ''),
            area: ['650px', '320px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            }
        });
    },
    //申请取消(客户)
    cancelOrder:function(id,no,quarter){
        var self = this;
        var candelIndex = top.layer.open({
            type: 2,
            id:'layer_cancel',
            zIndex: 19891015,
            title:'取消订单 ['+no+']',
            content: self.rootUrl+"/sd/order/cancel?id="+ id + "&orderNo=" + no + "&quarter=" + ( quarter || ''),
            area: ['650px', '300px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            }
        });
    },
    //取消导入的订单(客户)
    cancelImportOrder:function(id){
        var self = this;
        var candelIndex = top.layer.open({
            type: 2,
            id:'layer_cancel',
            zIndex: 19891015,
            title:'取消订单',
            content: self.rootUrl+"/sd/order/import/new/cancel?id="+ id ,
            area: ['650px', '300px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
                setCookie('layer.parent.id',index);
            },
            end:function(){
                setCookie('layer.parent.id',0,undefined);
            }
        });
    },
    //退回到派单区
    orderBackToAccept:function(id,quarter,parentIndex){
        var self = this;
        top.layer.confirm('确定要退回订单到[派单区]吗?', {icon: 3, title:'系统确认',zIndex: 19891015}, function(index){
            top.layer.close(index);//关闭本身
            // do something
            var loadingIndex = top.layer.msg('正在提交，请稍等...', {
                icon: 16,
                time: 0,//不定时关闭
                shade: 0.3,
                zIndex: 19891015
            });
            var data = { orderId : id || '' , quarter:quarter || ''};
            $.ajax({
                cache : false,
                type : "POST",
                url : self.rootUrl + "/sd/order/orderBackToAccept",
                data : data,
                success : function(data)
                {
                    top.layer.close(loadingIndex);
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.success)
                    {
                        layerMsg("订单退回成功!");
                        if (parentIndex && parentIndex != undefined && parentIndex != '' && parentIndex != null) {
                            var layero = $("#layui-layer" + parentIndex, top.document);
                            var iframeWin = top[layero.find('iframe')[0]['name']];
                            iframeWin.reload();
                            return false;
                        }
                    }else
                    {
                        layerError(data.message, "错误提示");
                    }
                },
                error : function(e)
                {
                    top.layer.close(loadingIndex);
                    ajaxLogout(e.responseText,null,"订单退回错误，请重试!");
                }
            });
            return false;
        });
        return false;
    },
    //停滞表单
    kefuPending:function(id,quarter,no,type, reservationTimes){
        var self = this;
        top.layer.open({
            type: 2,
            id:'layer_pending',
            zIndex: 19891015,
            title:'工单['+no+']已预约'+reservationTimes+'次',
            content: self.rootUrl+"/sd/order/kefuOrderList/service/pending?orderId="+ id +"&quarter=" + quarter || '',
            area: ['550px', '400px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //停滞表单
    newPending:function(id,quarter,no,type, reservationTimes){
        var self = this;
        top.layer.open({
            type: 2,
            id:'layer_pending',
            zIndex: 19891015,
            title:'工单['+no+']已预约'+reservationTimes+'次',
            content: self.rootUrl+"/sd/order/pending?orderId="+ id +"&quarter=" + quarter || '',
            area: ['550px', '400px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //停滞表单
    pending:function(id,quarter,no,type){
        //type 0-列表中调用 1-订单明细调用
        var self = this;
        var orderDetail = getGlobalVar('layerFrameConfig.orderDetail',"json");
        var pendingIndex = top.layer.open({
            type: 2,
            id:'layer_pending',
            zIndex: 19891015,
            title:'停滞原因设定 ['+no+']',
            content: self.rootUrl+"/sd/order/pending?orderId="+ id +"&quarter=" + quarter || '',
            area: ['550px', '400px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //预约上门时间
    appoint:function(id,quarter,no,type){
        //type 0-列表中调用 1-订单明细调用
        var self = this;
        var appointIndex = top.layer.open({
            type: 2,
            id:'layer_appoint',
            zIndex: 19891015,
            title:'预约订单['+no+']上门时间',
            content: self.rootUrl+"/sd/order/appoint?orderId="+ id +"&quarter=" + quarter || '',
            area: ['550px', '320px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //客服待跟进
    newNextFollowUpTime:function(id,quarter,no,type, isTodayForAppointment){
        var self = this;
        if (isTodayForAppointment === '1') {
            var submit = function(v, h, f) {
                if (v === 'ok') {
                    top.layer.open({
                        type: 2,
                        id:'layer_appoint',
                        zIndex: 19891015,
                        title:'设置订单['+no+']下次跟进时间',
                        content: self.rootUrl+"/sd/order/nextFollowUpTime?orderId="+ id +"&quarter=" + quarter || '',
                        area: ['550px', '400px'],
                        shade: 0.3,
                        maxmin: false,
                        success: function(layero,index){
                        },
                        end:function(){
                        }
                    });
                }
                return true;
            };
            $.jBox.confirm("工单预约已到期，请确认是否使用待跟进处理", "提示", submit);
        }
        else {
            top.layer.open({
                type: 2,
                id:'layer_appoint',
                zIndex: 19891015,
                title:'设置订单['+no+']下次跟进时间',
                content: self.rootUrl+"/sd/order/nextFollowUpTime?orderId="+ id +"&quarter=" + quarter || '',
                area: ['550px', '400px'],
                shade: 0.3,
                maxmin: false,
                success: function(layero,index){
                },
                end:function(){
                }
            });
        }
    },
    //客服待跟进
    nextFollowUpTime:function(id,quarter,no,type){
        var self = this;
        var nextFollowUpIndex = top.layer.open({
            type: 2,
            id:'layer_appoint',
            zIndex: 19891015,
            title:'设置订单['+no+']下次跟进时间',
            content: self.rootUrl+"/sd/order/nextFollowUpTime?orderId="+ id +"&quarter=" + quarter || '',
            area: ['550px', '400px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //上门服务(主窗口)
    service:function(id){
        var self = this;
        var mywin = window.open(self.rootUrl+"/sd/order/service?orderId="+id,'orderprocesswindow',"dependent=yes,fullscreen=yes,resizable=yes,scrollbars=yes,status=yes,modal=yes,alwaysRaised=yes,minimizable=no,top=150,left=200,toolbar=no,menubar=yes,location=no");
        mywin.moveTo(0, 0);
        mywin.resizeTo(screen.availWidth,screen.availHeight);
    },
    //添加上门服务项目
    addService:function(id,parentIndex){
        var self = this;
        var service_index = top.layer.open({
            type: 2,
            id:'layer_addService',
            zIndex:19891016,
            title:'添加服务明细',
            content: self.rootUrl+"/sd/order/addservice?orderId="+ id + "&parentIndex=" + (parentIndex || ''),
            area: ['1000px', '750px'],
            shade: 0.3,
            maxmin: true,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //确认上门
    orderConfirmDoor:function(id,quarter,parentIndex,confirmType)
    {
        if(!confirmType){
            confirmType = 0;//客服
        }
        var self = this;
        var clicktag = 0;
        top.layer.confirm('确定要执行[确认上门]操作吗?', {icon: 3, title:'系统确认'}, function(index){
            if(clicktag == 1){
                return false;
            }
            clicktag = 1;
            top.layer.close(index);//关闭本身
            // do something
            var loadingIndex;
            var ajaxSuccess = 0;
            var data = { orderId : id,quarter: quarter,confirmType: confirmType};
            $.ajax({
                async: false,
                cache: false,
                type : "POST",
                url : self.rootUrl + "/sd/order/confirmDoorAuto",
                data : data,
                beforeSend: function () {
                    loadingIndex = top.layer.msg('正在提交，请稍等...', {
                        icon: 16,
                        time: 0,
                        shade: 0.3
                    });
                },
                complete: function () {
                    if(loadingIndex) {
                        top.layer.close(loadingIndex);
                    }
                    //失败
                    if(ajaxSuccess == 0) {
                        setTimeout(function () {
                            clicktag = 0;
                        }, 2000);
                    }
                },
                success : function(data)
                {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.success)
                    {
                        layerMsg("确认上门成功!");
                        if(confirmType === 0) {//客服
                            var pid = 0;
                            if (parentIndex) {
                                pid = parentIndex;
                            } else {
                                var orderDetail = getGlobalVar('layerFrameConfig.orderDetail', "json");
                                if (orderDetail && orderDetail != null && orderDetail != undefined) {
                                    pid = orderDetail.index;
                                }
                            }
                            if (pid > 0) {
                                var layero = $("#layui-layer" + pid, top.document);
                                var iframeWin = top[layero.find('iframe')[0]['name']];
                                iframeWin.reload('tabService');
                                ajaxSuccess = 1;
                                return false;
                            }
                        }else{
                            //网点，刷新列表
                            var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                            if(iframe != undefined) {
                                iframe.repage();
                            }else{
                                layerMsg('请手动刷新列表',false);
                            }
                        }
                    }else
                    {
                        layerError(data.message, "错误提示");
                    }
                },
                error : function(e)
                {
                    ajaxLogout(e.responseText,null,"确认上门错误，请重试!");
                }
            });
            return false;
        });
        return false;

    },
    //配件申请
    materialApply:function(orderId,quarter,detailId,productId,parentLayerIndex){
        var self = this;
        var pendingIndex = top.layer.open({
            type: 2,
            id:'layer_materialApply',
            zIndex:19891016,
            title:'配件申请',
            content: self.rootUrl+"/sd/material/addMaterialApply?orderId="+ orderId +"&quarter=" + quarter +"&orderDetailId=" + detailId+"&productId=" + productId+"&parentIndex=" + (parentLayerIndex || ''),
            area: ['1000px', '600px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){

            }
        });
    },
    //配件审核
    materialList:function(orderId){
        var self = this;
        var materialWin = window.open(self.rootUrl+"/sd/material/materialapprove?orderId="+orderId,'orderprocessmaterialwindow',"dependent=yes,fullscreen=yes,resizable=yes,scrollbars=yes,status=yes,modal=yes,alwaysRaised=yes,minimizable=no,top=150,left=200,toolbar=no,menubar=yes,location=no");
        materialWin.moveTo(0, 0);
        materialWin.resizeTo(screen.availWidth,screen.availHeight);
    },
    //删除上门服务页面附件
    deleteServiceAttachment:function(rowIndex){
        if(!rowIndex){return false;}
        var self = this;
        var clicktag = 0;
        top.layer.confirm('确定要删除该附件吗?', {icon: 3, title:'系统确认'}, function(index) {
            if(clicktag == 1){
                return false;
            }
            clicktag = 1;
            top.layer.close(index);//关闭本身
            // do something
            var id = $("#id" + rowIndex).val();
            if (id.length > 0) {
                var loadingIndex;
                var ajaxSuccess = 0;
                var data1 = {attachmentid: id, orderId: $("#id").val()};
                $.ajax({
                    cache: false,
                    type: "POST",
                    async: false,
                    url: self.rootUrl + "/sd/order/deleteAttach",
                    data: data1,
                    beforeSend: function () {
                        loadingIndex = layer.msg('正在删除，请稍等...', {
                            icon: 16,
                            time: 0,
                            shade: 0.3
                        });
                    },
                    complete: function () {
                        if(loadingIndex) {
                            layer.close(loadingIndex);
                        }
                        //失败
                        if(ajaxSuccess == 0) {
                            setTimeout(function () {
                                clicktag = 0;
                            }, 2000);
                        }
                    },
                    success: function (data) {
                        if(ajaxLogout(data)){
                            return false;
                        }
                        if (data) {
                            if (data && data.success == true) {
                                ajaxSuccess = 1;
                                $("#accesstr" + rowIndex).remove();
                            }
                            else {
                                layerError(" 删除失败!",true);
                            }
                        }
                        return false;
                    },
                    error: function (e) {
                        ajaxLogout(e.responseText,null,"删除附件错误，请重试!");
                    }
                });
            }
        });
    },
    getCustomerPrice:function(customerId,  productId,serviceTypeId) {
        var self = this;
        var data = {customerId: customerId, productId: productId, serviceTypeId: serviceTypeId};
        $.ajax({
            cache: false,
            type: "POST",
            url: self.rootUrl + "/sd/order/getCustomerPrice",
            data: data,
            success: function (data) {
                if(ajaxLogout(data)){
                    return false;
                }
                if (data.success) {
                    layerInfo(data.message, '产品说明');
                } else {
                    layerError("<p class='text-error'>" + data.message + "</p>", "错误提示");
                }
            },
            error: function (e) {
                ajaxLogout(e.responseText,null,"<p class='text-error'>" + "获取产品说明失败，请重试!"+"</p>");
            }
        });
    },
    //手机异常处理完成
    orderDealAPPException:function(id,quarter,parentIndex) {
        var self = this;
        top.layer.confirm('确定该手机异常已处理完成了吗?', {icon: 3, title:'系统确认'}, function(index){
            top.layer.close(index);//关闭本身
            // do something
            var data = {orderId: id,quarter: quarter};
            $.ajax(
                {
                cache: false,
                type: "POST",
                url: self.rootUrl + "/sd/order/orderDealAPPException",
                data: data,
                success: function (data) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.success) {
                        layerMsg("订单异常处理成功!");
                        if (parentIndex && parentIndex != undefined && parentIndex != '') {
                            var layero = $("#layui-layer" + parentIndex, top.document);
                            var iframeWin = top[layero.find('iframe')[0]['name']];
                            iframeWin.removeButton("btnAppAbnormaly");
                            iframeWin.showButton("btnGrade");
                            //刷新详情页
                            //iframeWin.reload();
                            //loadMyMessages(self.rootUrl,1000);
                            return false;
                        }
                    } else {
                        layerError(data.message, "错误提示");
                    }
                },
                error: function (e) {
                    ajaxLogout(e.responseText,null,"订单异常处理失败，请重试!");
                }
            });
        });
        return false;
    },
    //客服完工,for 云米安装及维修单
    completeForViomi:function(id,quarter,parentIndex){
        //parentIndex:父layer窗口的index
        if(!id){return false;}
        var self = this;
        var gradeIndex = top.layer.open({
            type: 2,
            id:'layer_kefu_complete',
            zIndex:19891015,
            title:'完工',
            content: self.rootUrl+"/sd/order/completeForViomi?orderId="+ id + "&quarter=" + (quarter || '') + "&parentIndex=" + (parentIndex || ''),
            area: ['780px', '450px'],
            shade: 0.3,
            maxmin: true,
            success: function(layero,index){
            },
            end:function(){

            }
        });
    },
    //客服/网点完工
    completeForKefu:function(id,quarter,parentIndex){
        //parentIndex:父layer窗口的index
        if(!id){return false;}
        var self = this;
        var gradeIndex = top.layer.open({
            type: 2,
            id:'layer_kefu_complete',
            zIndex:19891015,
            title:'完成服务',
            content: self.rootUrl+"/sd/order/completeForKefu?orderId="+ id + "&quarter=" + (quarter || '') + "&parentIndex=" + (parentIndex || ''),
            area: ['500px', '350px'],
            shade: 0.3,
            maxmin: true,
            success: function(layero,index){
            },
            end:function(){

            }
        });
    },
    //客评
    grade:function (id,quarter,parentIndex) {
        //parentIndex:父layer窗口的index
        if(!id){return false;}
        var self = this;
        var gradeIndex = top.layer.open({
            type: 2,
            id:'layer_tracking',
            zIndex:19891015,
            title:'客评',
            content: self.rootUrl+"/sd/order/grade?orderId="+ id + "&quarter=" + (quarter || '') + "&parentIndex=" + (parentIndex || ''),
            area: ['780px', '450px'],
            shade: 0.3,
            maxmin: true,
            success: function(layero,index){
            },
            end:function(){

            }
        });
    },
    //智能回访记录
    voiceService:function (quarter,id) {
        if(!id){return false;}
        var self = this;
        // var h = $(top.window).height();
        // var w = $(top.window).width();
        // console.log('w:',w);
        // console.log('h:',h);
        var voiceGradeIndex = top.layer.open({
            type: 2,
            id:'layer_voice_service',
            zIndex:19891016,
            title:'智能回访',
            content: self.rootUrl+"/voice/taskInfo/" + (quarter || '') + "/" + (id || ''),
            area: ['1320px', '700px'],
            // area:[(w-120)+'px',(h-120)+'px'],
            // area:[w+'px',h+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){}
        });
    },
    //维护进度跟踪(客服)
    tracking:function(id,quarter) {
        if (!id) {
            return false;
        }
        var self = this;
        var trackingIndex = top.layer.open({
            type: 2,
            id:'layer_tracking',
            zIndex:19891015,
            title:'跟踪进度',
            content: self.rootUrl+"/sd/order/tracking?orderId="+ id + "&quarter=" + (quarter || ''),
            area: ['1000px', '600px'],
            shade: 0.3,
            maxmin: true,
            success: function(layero,index){
            },
            end:function(){

            }
        });
        top.layer.full(trackingIndex);

    },//维护进度跟踪(网点)
    trackingEnginner:function(id,quarter) {
        if (!id) {
            return false;
        }
        var self = this;
        var trackingIndex = top.layer.open({
            type: 2,
            id:'layer_tracking',
            zIndex:19891015,
            title:'跟踪进度',
            content: self.rootUrl+"/sd/order/trackingEnginner?orderId="+ id + "&quarter=" + (quarter || ''),
            area: ['1000px', '640px'],
            shade: 0.3,
            maxmin: true,
            success: function(layero,index){
            },
            end:function(){

            }
        });
    },
    //异常单-维护进度跟踪
    pendintTracking:function(id,quarter) {
        if (!id) {
            return false;
        }
        var self = this;
        var trackingIndex = top.layer.open({
            type: 2,
            id:'layer_tracking',
            zIndex:19891015,
            title:'跟踪进度',
            content: self.rootUrl+"/sd/pending/tracking?orderId="+ id + "&quarter=" + (quarter || ''),
            area: ['1000px', '600px'],
            shade: 0.3,
            maxmin: true,
            success: function(layero,index){
            },
            end:function(){

            }
        });
        top.layer.full(trackingIndex);
    },
    // 保存跟踪进度
    // flag:close-保存后关闭窗口 closeAndReloadParent-关闭并刷新父窗口
    saveTacking:function(flag,thisIndex,parentIndex){
        var self = this;
        var remarks =  $("#remarks","#trackingForm").val();
        if(Utils.isEmpty(remarks)){
            layerError("请输入跟踪内容.","错误提示");
            clickTag = 0;
            return false;
        }
        var $btnSubmit = $("#btnSaveTracking");
        $btnSubmit.attr('disabled', 'disabled');
        var ajaxSuccess = 0;
        var loadingIndex;
        $.ajax({
            type: "POST",
            url: self.rootUrl + "/sd/order/tracking?"+ (new Date()).getTime(),
            data:$("#trackingForm").serialize(),
            beforeSend: function () {
                loadingIndex = layer.msg('正在提交，请稍等...', {
                    icon: 16,
                    time: 0,
                    shade: 0.3
                });
            },
            complete: function () {
                //console.log("" + new Date().getTime() + " [complete] clickTag:" + clickTag + " ,ajaxSuccess:" + ajaxSuccess);
                if(loadingIndex) {
                    layer.close(loadingIndex);
                }
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
                $('#btnSaveTracking').removeAttr('disabled');
                if(data && data.success == true){
                    if(flag && flag=="close"){
                        top.layer.close(thisIndex);
                    }else if(flag && flag == "closeAndReloadParent") {
                        top.layer.close(thisIndex);
                        if (parentIndex) {
                            var layero = $("#layui-layer" + parentIndex, top.document);
                            var iframeWin = top[layero.find('iframe')[0]['name']];
                            iframeWin.reload();
                        }else{
                            var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                            if(iframe != undefined) {
                                iframe.repage();
                            }
                        }
                    }
                    else {
                        reload();
                    }
                    ajaxSuccess = 1;
                }
                else if( data && data.message){
                    layerError(data.message,"错误提示");
                }
                else{
                    layerError("跟踪进度错误","错误提示");
                }
                return false;
            },
            error: function (e) {
                ajaxLogout(e.responseText,null,"保存跟踪进度错误，请重试!");
            }
        });
        return false;
    },
    // 以下为详情页操作
    //显示订单详情页进度
    detail_order_process:function(order){
        var tmpl = document.getElementById('tpl-order-process').innerHTML;
        var doTtmpl = doT.template(tmpl);
        var html = doTtmpl(order);
        $("#bwizard").html("").append(html);
        return;
    },
    //显示详情页订单操作工具栏
    detail_order_toolbar:function(order){
        var tmpl = document.getElementById('tpl-order-toolbar').innerHTML;
        var doTtmpl = doT.template(tmpl);
        var html = doTtmpl(order);
        $("#toolbar").html("").append(html);
        return;
    },
    //配件管理(审核，驳回，图片管理)
    attachlist:function(id,no,quarter,openerIndex){
        var self = this;
        var screen = getScreenWidthAndHeight();
        if(screen.width>1350){
            screen.width = 1350;
            screen.height = 690;
        }
        var attach_index = top.layer.open({
            type: 2,
            id:'layer_attachlist',
            zIndex: 19891015,
            title:'配件单',
            content: self.rootUrl+"/sd/material/list?orderId="+ id+"&orderNo=" + (no || '') + "&quarter=" + (quarter || ''),
            shade: 0.3,
            // area: ['1435px', '800px'],
            area: [screen.width+'px', screen.height+'px'],
            maxmin: true,
            success: function(layero){// top.layer.setTop(layero);
            },
            end:function(){
                if(openerIndex){
                    //关闭窗口后，刷新引用调用窗口
                    var layero = $("#layui-layer" + openerIndex, top.document);
                    var iframeCtl = layero.find('iframe');
                    if (iframeCtl && iframeCtl.length > 0) {
                        var iframeWin = top[iframeCtl[0]['name']];
                        if(iframeWin){
                            iframeWin.location.reload();
                        }
                    }
                }
            }
        });
    },
    //查看完成图片(旧方式上传的完成图片）
    photolist:function(id,quarter){
        var self = this;
        var h = $(top.window).height();
        var w = $(top.window).width();
        var photoIndex = top.layer.open({
            type: 2,
            id:'layer_photolist',
            zIndex:19891015,
            title:'完成照片查看',
            content: self.rootUrl+"/sd/order/viewDetailAttachment?orderId="+ id + "&quarter=" + quarter,
            area:[(w-40)+'px',(h-40)+'px'],
            shade: 0.3,
            shadeClose: true,
            maxmin: true
        });
    },
    //查看新方式上传的完成图片
    photolistNew:function(id,quarter,isNewOrder){
        var self = this;
        var h = $(top.window).height();
        var w = $(top.window).width();
        var photoIndex = top.layer.open({
            type: 2,
            id: 'layer_photolistNew',
            zIndex: 19891015,
            title: '完成照片查看',
            content: self.rootUrl+"/sd/orderItemComplete/orderAttachmentFrom?orderId="+ id + "&quarter=" + quarter,
            area: ['936px','762px'],
            shade: 0.3,
            shadeClose: true,
            maxmin: true
        });
    },
    //浏览完成图片，不可新增和编辑
    browsePhotolist:function(id,quarter){
        var self = this;
        var h = $(top.window).height();
        var w = $(top.window).width();
        var photoIndex = top.layer.open({
            type: 2,
            id:'layer_photolistNew',
            zIndex: 19891015,
            title:'完成照片查看',
            content: self.rootUrl+"/sd/orderItemComplete/browseOrderAttachment?orderId=" + id + "&quarter=" + quarter,
            area:[(w-40)+'px',(h-40)+'px'],
            shade: 0.3,
            shadeClose: true,
            maxmin: true,
            success: function(layero){
            }
        });
    },
    //修改用户上门信息
    editUserInfo:function (id,no,quarter,parentIndex) {
        var self = this;
        var pendingIndex = top.layer.open({
            type: 2,
            id:'layer_editUserInfo',
            zIndex: 19891015,
            title:'修改实际上门联系信息['+no+']',
            content: self.rootUrl+"/sd/order/updateUserServiceInfo?orderId="+ id + "&quarter=" + (quarter || '') + "&parentIndex=" + (parentIndex || ''),
            area: ['830px', '380px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
                // layer.setTop(layero);
            },
            end:function(){
            }
        });
    },
    //投诉单管理
    //新增或编辑投诉单
    complain_form:function(id,orderId,quarter,parentIndex){
        var self = this;
        var screen = getOpenDialogWidthAndHeight();
        //console.log(screen.width + 'x' + screen.height);
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_complain_form',
            zIndex:19891016,
            title:'投诉单',
            content: self.rootUrl+"/sd/complain/form?id=" + (id || '') + "&quarter=" + quarter + "&orderId="+ orderId + "&parentIndex=" + (parentIndex || '0'),
            //area: ['980px', '640px'],
            area: ['1255px', screen.height-100+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //具体订单下的投诉单列表
    complain_list:function(orderId,orderNo,quarter){
        var self = this;
        var screen = getOpenDialogWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_complain_list',
            zIndex:19891015,
            title:'投诉单列表',
            content: self.rootUrl+"/sd/complain/orderlist?quarter=" + quarter + "&orderNo=" + orderNo + "&orderId="+ orderId + "&parentIndex=0",
            // area: ['980px', '640px'],
            area: ['1255px', screen.height+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
                setCookie('layer.parent.id',index);
            },
            end:function(){
                setCookie('layer.parent.id',0,undefined);
            }
        });
    },
    //查看投诉单
    complain_view:function(id,quarter){
        var self = this;
        var screen = getOpenDialogWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_complain_view',
            zIndex:19891016,
            title:'投诉单',
            content: self.rootUrl+"/sd/complain/view?id=" + (id || '') + "&quarter=" + quarter,
            // area: ['980px', '640px'],
            area: ['1255px', screen.height+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //判定投诉单列表
    complain_judge:function(id,quarter){
        var self = this;
        var screen = getOpenDialogWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_complain_judge',
            zIndex:19891016,
            title:'判定',
            content: self.rootUrl+"/sd/complain/judge?id=" + (id || '') + "&quarter=" + quarter + "&parentIndex=0",
            // area: ['980px', '640px'],
            area: ['1255px', screen.height+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    complain_judgeNew:function(id,quarter){
        var self = this;
        var screen = getOpenDialogWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_complain_judge',
            zIndex:19891016,
            title:'判定',
            content: self.rootUrl+"/sd/complain/judgeNew?id=" + (id || '') + "&quarter=" + quarter + "&parentIndex=0",
            // area: ['980px', '640px'],
            area: ['1255px', screen.height+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
                // var iframe = getActiveTabIframe();//定义在jeesite.min.js中
                // if (iframe != undefined) {
                //     iframe.repage();
                // }
            }
        });
    },
    //完成投诉单
    complain_complete:function(id,quarter){
        var self = this;
        var screen = getOpenDialogWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_complain_close',
            zIndex:19891016,
            title:'完成',
            content: self.rootUrl+"/sd/complain/complete?id=" + (id || '') + "&quarter=" + quarter + "&parentIndex=0",
            area: ['1255px', screen.height+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //处理申诉单界面
    appeal_deal:function(id,quarter){
        var self = this;
        var screen = getOpenDialogWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_complain_close',
            zIndex:19891016,
            title:'申诉处理',
            content: self.rootUrl+"/sd/complain/appealDealForm?id=" + (id || '') + "&quarter=" + quarter + "&parentIndex=0",
            area: ['1255px', screen.height+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //订单日志-投诉单
    showComplainList:function(orderId,quarter,forceRefresh){
        var self = this;
        if((forceRefresh && forceRefresh == true) || $("#tb_complain").length == 0){
            var parentLayerIndex = top.layer.getFrameIndex('layer_orderdetail');
            var loadingIndex = top.layer.msg('正在加载投诉单列表...', {
                icon: 16,
                time: 0,//不定时关闭
                shade: 0.3
            });

            $.ajax({
                cache: false,
                type: "GET",
                url: self.rootUrl+"/sd/complain/ajax/list?orderId=" + orderId + "&quarter=" + (quarter || ''),
                dataType: 'json',
                success: function (data) {
                    top.layer.close(loadingIndex);
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.success ) {
                        if(data.data && data.data.length>0) {
                            var tmpl = document.getElementById('tpl-complain').innerHTML;
                            var doTtmpl = doT.template(tmpl);
                            // var html = doTtmpl(data.data);
                            var item = {parentLayerIndex:parentLayerIndex,data:data.data};
                            var html = doTtmpl(item);
                            $("#tabComplain").html(html);
                            //让模板的自定义提示生效
                            $('a[data-toggle=tooltip]').darkTooltip();
                        }else {
                            $("#tabComplain").empty().append("<table id='tb_complain' style='display:none;'></table>无记录");
                        }
                    }else{
                        layerError(data.message);
                    }
                },
                error: function (e) {
                    ajaxLogout(e.responseText,null,"装载投诉单错误，请重试!");
                    top.layer.close(loadingIndex);
                }
            });
        }
    },
    //判定-显示投诉日志列表
    showComplainLogList:function(complainId,quarter){
        var self = this;
        var loadingIndex = top.layer.msg('正在加载投诉日志列表...', {
            icon: 16,
            time: 0,//不定时关闭
            shade: 0.3
        });

        $.ajax({
            cache: false,
            type: "GET",
            url: self.rootUrl+"/sd/complain/ajax/complainLogList?complainId=" + complainId + "&quarter=" + (quarter || ''),
            dataType: 'json',
            success: function (data) {
                top.layer.close(loadingIndex);
                if(ajaxLogout(data)){
                    return false;
                }
                if (data.success ) {
                    if(data.data && data.data.length>0) {
                        var tmpl = document.getElementById('tpl-complainlogList').innerHTML;
                        var doTtmpl = doT.template(tmpl);
                        var html = doTtmpl(data.data);
                        $("#tabComplainLogList").html(html);
                    }else {
                        $("#tabComplainLogList").append("<table id='tb_complainLogList' style='display:none;'></table>无记录");
                    }
                }else{
                    layerError(data.message);
                }
            },
            error: function (e) {
                ajaxLogout(e.responseText,null,"装载投诉日志列表错误，请重试!");
                top.layer.close(loadingIndex);
            }
        });
    },
    //撤销投诉单操作
    doCancelComplain:function (orderId,complainId,quarter) {
        var self = this;
        $.ajax({
            cache: false,
            type: "GET",
            url: self.rootUrl+"/sd/complain/ajax/cancleComplain?complainId=" + complainId + "&quarter=" + (quarter || ''),
            dataType: 'json',
            success: function (data) {
                if(ajaxLogout(data)){
                    return false;
                }
                if (data.success ) {
                    Order.showComplainList(orderId,quarter,true);
                }else{
                    layerError(data.message);
                }
            },
            error: function (e) {
                ajaxLogout(e.responseText,null,"装载投诉单错误，请重试!");
            }
        });
    },
    //申诉
    appeal_form:function(id,complainNo,quarter,parentIndex){
        var self = this;
        var screen = getOpenDialogWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_appeal_form',
            zIndex:19891016,
            title:'申诉',
            content: self.rootUrl+"/sd/complain/appealForm?id=" + (id || '') + "&quarter=" + quarter +"&complainNo="+complainNo+ "&parentIndex=" + (parentIndex || '0'),
            area: ['800px', screen.height*2/3+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
                setCookie('layer.parent.id',index);
            },
            end:function(){
            }
        });
    },
    //弹窗显示订单明细(for 投诉单)
    showComplainOrderDetail:function(id,quarter,refreshParent){
        var self = this;
        var h = $(top.window).height();
        var w = $(top.window).width();
        if(!refreshParent){
            refreshParent = 'false';
        }
        var orderDetail_index = top.layer.open({
            type: 2,
            id:'layer_orderdetail',
            zIndex:19891015,
            title:'订单详情',
            content: self.rootUrl+"/sd/complain/orderDetailInfo?id="+ id + "&quarter=" + (quarter || '') +"&refreshParent=" + (refreshParent || ''),
            shade: 0.3,
            area:[(w-200)+'px',(h-100)+'px'],
            // area:[screen.width+'px',screen.height+'px'],
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //突击单管理
    //新增或编辑突击单
    crush_form:function(id,orderId,quarter,parentIndex){
        var self = this;
        var screen = getOpenDialogWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_assault_form',
            zIndex:19891016,
            title:'突击单',
            content: self.rootUrl+"/sd/order/crush/form?id=" + (id || '') + "&quarter=" + quarter + "&orderId="+ orderId + "&parentIndex=" + (parentIndex || '0'),
            //area: ['980px', '640px'],
            area: ['1255px', screen.height-50+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //订单详情-加载投突击列表
    crush_showList:function(orderId,quarter,forceRefresh){
        var self = this;
        var parentLayerIndex = top.layer.getFrameIndex('layer_orderdetail');
        var loadingIndex = top.layer.msg('正在加载突击单列表...', {
            icon: 16,
            time: 0,//不定时关闭
            shade: 0.3
        });

        $.ajax({
            cache: false,
            type: "GET",
            url: self.rootUrl+"/sd/order/crush/ajax/list?orderId=" + orderId + "&quarter=" + (quarter || ''),
            dataType: 'json',
            success: function (data) {
                top.layer.close(loadingIndex);
                if(ajaxLogout(data)){
                    return false;
                }
                if (data.success ) {
                    if(data.data && data.data.length>0) {
                        var tmpl = document.getElementById('tpl-orderCrushList').innerHTML;
                        var doTtmpl = doT.template(tmpl);
                        var html = doTtmpl(data.data);
                        $("#tabOrderCrush").html(html);
                        //让模板的自定义提示生效
                        $('a[data-toggle=tooltip]').darkTooltip();
                    }else {
                        $("#tabOrderCrush").empty().append("<table id='tabOrderCrush' style='display:none;'></table>无记录");
                    }
                }else{
                    layerError(data.message);
                }
            },
            error: function (e) {
                ajaxLogout(e.responseText,null,"装载突击单错误，请重试!");
                top.layer.close(loadingIndex);
            }
        });
    },
    //查看突击单
    crush_view:function(id,quarter){
        var self = this;
        var screen = getOpenDialogWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_crush_view',
            zIndex:19891016,
            title:'突击单',
            content: self.rootUrl+"/sd/order/crush/view?id=" + (id || '') + "&quarter=" + quarter,
            // area: ['980px', '640px'],
            area: ['1255px', screen.height+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //突击单 完成界面
    crush_close:function(id,quarter){
        var self = this;
        var screen = getOpenDialogWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_crush_view',
            zIndex:19891016,
            title:'突击单',
            content: self.rootUrl+"/sd/order/crush/close?id=" + (id || '') + "&quarter=" + quarter,
            // area: ['980px', '640px'],
            area: ['1404px', screen.height+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //修改到货日期
    arrivalDate:function(id,no,quarter,parentIndex){
        var self = this;
        top.layer.open({
            type: 2,
            id:'layer_arrivalDate',
            zIndex:19891015,
            title:'到货日期 ['+no+']',
            content: self.rootUrl+"/sd/order/arrivaldate?orderId="+ id + "&orderNo=" + (no || '') + "&quarter=" + (quarter || '') + "&parentIndex=" + (parentIndex || ''),
            area: ['830px', '380px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //订单详情-加载辅材收费列表
    auxiliaryMaterial_showDetailInfo:function(orderId,quarter,forceRefresh){
        var self = this;
        var parentLayerIndex = top.layer.getFrameIndex('layer_orderdetail');
        var loadingIndex = top.layer.msg('正在加载辅材收费列表...', {
            icon: 16,
            time: 0,//不定时关闭
            shade: 0.3
        });

        $.ajax({
            cache: false,
            type: "GET",
            url: self.rootUrl+"/sd/orderAuxiliaryMaterial/detailInfo?orderId=" + orderId + "&quarter=" + (quarter || ''),
            dataType: 'json',
            success: function (data) {
                top.layer.close(loadingIndex);
                if(ajaxLogout(data)){
                    return false;
                }
                if (data.success ) {
                    if(data.data && data.data.items && data.data.items.length>0 && data.data.formType==0) {
                        var tmpl = document.getElementById('tpl-orderAuxiliaryMaterialList').innerHTML;
                        var doTtmpl = doT.template(tmpl);
                        var html = doTtmpl(data.data);
                        $("#tabAuxiliaryMaterials").html(html);
                        //让模板的自定义提示生效
                        $('a[data-toggle=tooltip]').darkTooltip();
                        $('#tb_orderAuxiliaryMaterialList').viewer();
                    }else if(data.data && data.data.formType==1){
                        var tmpl = document.getElementById('tpl-orderAuxiliaryMaterialNoItem').innerHTML;
                        var doTtmpl = doT.template(tmpl);
                        var html = doTtmpl(data.data);
                        $("#tabAuxiliaryMaterials").html(html);
                        //让模板的自定义提示生效
                        $('a[data-toggle=tooltip]').darkTooltip();
                        $('#tb_orderAuxiliaryMaterialNoItem').viewer();
                    }else {
                        $("#tabAuxiliaryMaterials").empty().append("<table id='tabAuxiliaryMaterials' style='display:none;'></table>无记录");
                    }
                }else{
                    layerError(data.message);
                }
            },
            error: function (e) {
                ajaxLogout(e.responseText,null,"装载辅材收费信息错误，请重试!");
                top.layer.close(loadingIndex);
            }
        });
    },
    //B2B订单人工处理
    b2bOrderEdit:function(dataSource,b2bOrderNo,quarter,b2bDataSourceName){
        var self = this;
        var screen = getOpenDialogWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_b2b_workcard',
            zIndex:19891016,
            title:'B2B订单人工处理',
            content: self.rootUrl+"/b2b/" + b2bDataSourceName + "/order/manual?dataSource=" + dataSource + "&b2bOrderNo=" + (b2bOrderNo || '') + "&quarter=" + (quarter || ''),
            // area: ['980px', '640px'],
            area: ['1255px', screen.height+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //康宝B2B订单人工处理
    canboOrderEdit:function(dataSource,b2bOrderNo,quarter){
        var self = this;
        var screen = getOpenDialogWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_b2b_workcard',
            zIndex:19891016,
            title:'B2B订单人工处理',
            content: self.rootUrl+"/b2b/canbo/order/manual?dataSource=" + dataSource + "&b2bOrderNo=" + (b2bOrderNo || '') + "&quarter=" + (quarter || ''),
            area: ['1255px', screen.height+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //樱雪B2B订单人工处理
    inseOrderEdit:function(dataSource,b2bOrderNo,quarter){
        var self = this;
        var screen = getOpenDialogWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_b2b_workcard',
            zIndex:19891016,
            title:'B2B订单人工处理',
            content: self.rootUrl+"/b2b/inse/order/manual?dataSource=" + dataSource + "&b2bOrderNo=" + (b2bOrderNo || '') + "&quarter=" + (quarter || ''),
            area: ['1255px', screen.height+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //京东B2B订单人工处理
    jdOrderEdit:function(dataSource,b2bOrderNo,quarter){
        var self = this;
        var screen = getOpenDialogWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_b2b_workcard',
            zIndex:19891016,
            title:'B2B订单人工处理',
            content: self.rootUrl+"/b2b/jd/order/manual?dataSource=" + dataSource + "&b2bOrderNo=" + (b2bOrderNo || '') + "&quarter=" + (quarter || ''),
            area: ['1255px', screen.height+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //天猫一键反馈
    anomalyReply:function (id,anomalyId,quarter,refreshType){
        var self = this;
        top.layer.open({
            type: 2,
            id:'layer_tmallReply',
            zIndex:19891015,
            title:'天猫求助反馈',
            content: self.rootUrl+"/sd/order/anomaly/reply?id="+ (id || '') + "&anomalyId=" + (anomalyId || '' ) + "&quarter=" + (quarter || '') + "&refreshType=" + (refreshType || 'refreshList'),
            shade: 0.3,
            area: ['650px', '350px'],
            maxmin: false,
            success: function(layero,index){
                //setCookie('layer.parent.id',index);
            },
            end:function(){
            }
        });
    },
    //康宝转单取消
    canboOrderCancel:function(dataSource,b2bOrderNo,quarter,comment){
        var self = this;
        var candelIndex = top.layer.open({
            type: 2,
            id:'layer_b2bCancel',
            zIndex:19891015,
            title:'取消订单',
            content: self.rootUrl+"/b2b/canbo/order/cancelOrderTransition?dataSource=" + dataSource + "&b2bOrderNo=" + (b2bOrderNo || '') + "&quarter=" + (quarter || '')+ "&comment=" + (comment || ''),
            area: ['650px', '300px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            }
        });
    },
    //京东转单取消
    jdOrderCancel:function(dataSource,b2bOrderNo,quarter,comment){
        var self = this;
        var candelIndex = top.layer.open({
            type: 2,
            id:'layer_jdCancel',
            zIndex:19891015,
            title:'取消订单',
            content: self.rootUrl+"/b2b/jd/order/cancelOrderTransition?dataSource=" + dataSource + "&b2bOrderNo=" + (b2bOrderNo || '') + "&quarter=" + (quarter || '')+ "&comment=" + (comment || ''),
            area: ['650px', '300px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            }
        });
    },
    //天猫转单取消
    tmallOrderCancel:function(dataSource,b2bOrderNo,quarter,comment){
        var self = this;
        var candelIndex = top.layer.open({
            type: 2,
            id:'layer_tmallCancel',
            zIndex:19891015,
            title:'取消订单',
            content: self.rootUrl+"/b2b/tmall/order/cancelOrderTransition?dataSource=" + dataSource + "&b2bOrderNo=" + (b2bOrderNo || '') + "&quarter=" + (quarter || '')+ "&comment=" + (comment || ''),
            area: ['650px', '300px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            }
        });
    },
    //樱雪转单取消
    inseOrderCancel:function(dataSource,b2bOrderNo,quarter,comment){
        var self = this;
        var candelIndex = top.layer.open({
            type: 2,
            id:'layer_b2bCancel',
            zIndex:19891015,
            title:'取消订单',
            content: self.rootUrl+"/b2b/inse/order/cancelOrderTransition?dataSource=" + dataSource + "&b2bOrderNo=" + (b2bOrderNo || '') + "&quarter=" + (quarter || '')+ "&comment=" + (comment || ''),
            area: ['650px', '300px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            }
        });
    },
    //天猫一键反馈(订单详情页)
    anomalyList:function (orderId,quarter){
        var self = this;
        var parentLayerIndex = top.layer.getFrameIndex('layer_orderdetail');
        var loadingIndex = top.layer.msg('正在加载天猫一键求助列表...', {
            icon: 16,
            time: 0,//不定时关闭
            shade: 0.3
        });

        $.ajax({
            cache: false,
            type: "GET",
            url: self.rootUrl+"/sd/order/anomaly/ajax/list?orderId=" + orderId + "&quarter=" + (quarter || ''),
            dataType: 'json',
            success: function (data) {
                top.layer.close(loadingIndex);
                if(ajaxLogout(data)){
                    return false;
                }
                if (data.success ) {
                    if(data.data && data.data.length>0) {
                        var tmpl = document.getElementById('tpl-anomaly').innerHTML;
                        var doTtmpl = doT.template(tmpl);
                        var hasAnomalyPermission = $("#hasAnomalyPermission").val();
                        var tmpData = data.data;
                        if(hasAnomalyPermission=="1"){
                            tmpData.hasPermission = 1;
                        }else{
                            tmpData.hasPermission = 0;
                        }
                        var html = doTtmpl(tmpData);
                        $("#tabAnomaly").html(html);
                        //让模板的自定义提示生效
                        $("#tb_anomaly").find('a[data-toggle=tooltip]').darkTooltip();
                        $("#tb_anomaly").find('a[data-toggle=tooltipeast]').darkTooltip({gravity : 'east'});
                        //$('a[data-toggle=tooltipeast]').darkTooltip({gravity : 'east'});
                        $("#divAnomaly").find("div.upload_warp_img_div").each(function(){
                            $(this).viewer();//{url: "data-original"}
                        });

                    }else {
                        $("#tabAnomaly").empty().append("<table id='tabAnomaly' style='display:none;'></table>无求助记录");
                    }
                }else{
                    layerError(data.message);
                }
            },
            error: function (e) {
                ajaxLogout(e.responseText,null,"装载求助单错误，请重试!");
                top.layer.close(loadingIndex);
            }
        });
    },
    //网点预约时间
    servicepointPending:function(id ,no ,quarter ,reservationTimes){
        var self = this;
        top.layer.open({
            type: 2,
            id:'layer_pending',
            zIndex:19891015,
            title:'工单['+no+']已预约'+reservationTimes+'次',
            content: self.rootUrl+"/sd/order/servicePointOrderList/service/pending?orderId="+ id +"&quarter=" + quarter || '',
            area: ['550px', '400px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //网点完成服务
    servicepointComplete:function(id ,no ,quarter){
        var self = this;
        top.layer.open({
            type: 2,
            id:'layer_servicepoint_complete',
            zIndex:19891015,
            title:'工单['+no+'] 完成服务',
            content: self.rootUrl+"/sd/order/servicePointOrderList/service/complete?orderId="+ id +"&quarter=" + (quarter || ''),
            area: ['550px', '400px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //催单
    //订单日志-催单
    showReminderListForKefu:function(orderId,quarter,forceRefresh,userType){
        var self = this;
        if((forceRefresh && forceRefresh == true) || $("#tb_reminder").length == 0){
            var parentLayerIndex = top.layer.getFrameIndex('layer_orderdetail');
            var loadingIndex = top.layer.msg('正在加载催单列表...', {
                icon: 16,
                time: 0,//不定时关闭
                shade: 0.3
            });

            $.ajax({
                cache: false,
                type: "GET",
                url: self.rootUrl+"/sd/reminder/ajax/list?orderId=" + orderId + "&quarter=" + (quarter || '') +"&detailType=1" ,
                dataType: 'json',
                success: function (data) {
                    top.layer.close(loadingIndex);
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.success ) {
                        if(data.data){
                            //{isWait:1|0,value:model}
                            var ajaxData = data.data;
                            var tmpl = document.getElementById('tpl-reminderItem').innerHTML;
                            var doTtmpl = doT.template(tmpl);
                            var item = {parentLayerIndex:parentLayerIndex,userType:userType,isWait:ajaxData.isWait,data:ajaxData.value};
                            var html = doTtmpl(item);
                            $("#tabReminder").html(html);
                            //让模板的自定义提示生效
                            $('a[data-toggle=tooltip]').darkTooltip();
                        }else{
                            $("#tabReminder").empty().append("<table id='tb_reminder' style='display:none;'></table>无催单");
                        }
                    }else{
                        layerError(data.message);
                    }
                },
                error: function (e) {
                    ajaxLogout(e.responseText,null,"装载催单错误，请重试!");
                    top.layer.close(loadingIndex);
                }
            });
        }
    },
    //订单日志-催单
    showReminderListForCustomer:function(orderId,quarter,forceRefresh){
        var self = this;
        if((forceRefresh && forceRefresh == true) || $("#tb_reminder").length == 0){
            var parentLayerIndex = top.layer.getFrameIndex('layer_orderdetail');
            var loadingIndex = top.layer.msg('正在加载催单列表...', {
                icon: 16,
                time: 0,//不定时关闭
                shade: 0.3
            });
            $.ajax({
                cache: false,
                type: "GET",
                url: self.rootUrl+"/sd/reminder/ajax/list?orderId=" + orderId + "&quarter=" + (quarter || '') + "&detailType=2",
                dataType: 'json',
                success: function (data) {
                    top.layer.close(loadingIndex);
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.success ) {
                        if(data.data){
                            //{isWait:1|0,value:model}
                            var ajaxData = data.data;
                            var tmpl = document.getElementById('tpl-reminderItem').innerHTML;
                            var doTtmpl = doT.template(tmpl);
                            var item = {
                                parentLayerIndex:parentLayerIndex,
                                userType:userType,
                                isWait:ajaxData.isWait,
                                reminderCheckFlag:ajaxData.reminderCheckFlag,
                                reminderCheckMsg:ajaxData.reminderCheckMsg,
                                needConfirm:ajaxData.needConfirm,
                                data:ajaxData.value,
                                reminderType:ajaxData.reminderReasons};
                            var html = doTtmpl(item);
                            $("#tabReminder").html(html);
                            //让模板的自定义提示生效
                            $('a[data-toggle=tooltip]').darkTooltip();
                        }else{
                            $("#tabReminder").empty().append("<table id='tb_reminder' style='display:none;'></table>无催单");
                        }
                    }else{
                        layerError(data.message);
                    }
                },
                error: function (e) {
                    ajaxLogout(e.responseText,null,"装载催单错误，请重试!");
                    top.layer.close(loadingIndex);
                }
            });
        }
    },
    //新建
    reminder:function(orderId,quarter,orderNo,parentIndex){
        var self = this;
        var screen = getScreenWidthAndHeight();
        // console.log('height:' + screen.height);
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_reminder_form',
            zIndex:19891016,
            title:'首次催单',
            content: self.rootUrl+"/sd/reminder/form?orderId=" + (orderId || '') + "&quarter=" + quarter + "&orderNo=" + (orderNo || ''),
            area: ['1020px', '600px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
                if(parentIndex){
                    setCookie('layer.parent.id',parentIndex);
                }
            },
            end:function(){
            }
        });
    },
    //查看
    reminderView:function(id,orderId,quarter){
        var self = this;
        var screen = getScreenWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_reminder_form',
            zIndex:19891016,
            title:'催单详情',
            content: self.rootUrl+"/sd/reminder/view?id=" + (id || '') + "&orderId=" + (orderId || '') + "&quarter=" + quarter,
            area: ['1385px', '800px'],
            // area: [screen.width-60 + 'px', screen.height-100+'px'],
            shade: 0.3,
            shadeClose: true,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //装载催单项目列表
    loadReminderItems:function(reminderId,itemId,quarter){
        var self = this;
        var loadingIndex = top.layer.msg('正在加载列表...', {
            icon: 16,
            time: 0,//不定时关闭
            shade: 0.3
        });

        $.ajax({
            cache: false,
            type: "GET",
            url: self.rootUrl+"/sd/reminder/items?reminderId=" + reminderId + "&itemId=" + itemId + "&quarter=" + (quarter || ''),
            dataType: 'json',
            success: function (data) {
                top.layer.close(loadingIndex);
                if(ajaxLogout(data)){
                    return false;
                }
                if (data.success ) {
                    if(data.data && data.data.length>0) {
                        var tmpl = document.getElementById('tpl-items').innerHTML;
                        var doTtmpl = doT.template(tmpl);
                        var html = doTtmpl(data.data);
                        $("#tabItems").html(html);
                        $('a[data-toggle=tooltip]',"#tabItems").darkTooltip();
                    }else {
                        $("#tabItems").append("<table id='tb_items' style='display:none;'></table>无记录");
                    }
                }else{
                    layerError(data.message);
                }
            },
            error: function (e) {
                ajaxLogout(e.responseText,null,"装载日志列表错误，请重试!");
                top.layer.close(loadingIndex);
            }
        });
    },
    //装载催单项目列表
    showReminderItems:function(items){
        if(items && items.length>0) {
            var tmpl = document.getElementById('tpl-items').innerHTML;
            var doTtmpl = doT.template(tmpl);
            var html = doTtmpl(items);
            $("#tabItems").html(html);
            $('a[data-toggle=tooltip]',"#tabItems").darkTooltip();
        }else {
            $("#tabItems").append("<table id='tb_items' style='display:none;'></table>无记录");
        }
    },
    //装载待确认催单项目列表
    showReminderConfirmItems:function(model){
        if(model && model.items && model.items.length>0) {
            var tmpl = document.getElementById('tpl-items').innerHTML;
            var doTtmpl = doT.template(tmpl);
            var html = doTtmpl(model);
            $("#tabItems").html(html);
            $('a[data-toggle=tooltip]',"#tabItems").darkTooltip();
        }else {
            $("#tabItems").append("<table id='tb_items' style='display:none;'></table>无记录");
        }
    },
    //装载催单日志列表
    loadReminderLogList:function(reminderId,quarter){
        var self = this;
        var loadingIndex = top.layer.msg('正在加载列表...', {
            icon: 16,
            time: 0,//不定时关闭
            shade: 0.3
        });

        $.ajax({
            cache: false,
            type: "GET",
            url: self.rootUrl+"/sd/reminder/logs/list?reminderId=" + reminderId + "&quarter=" + (quarter || ''),
            dataType: 'json',
            success: function (data) {
                top.layer.close(loadingIndex);
                if(ajaxLogout(data)){
                    return false;
                }
                if (data.success ) {
                    if(data.data && data.data.length>0) {
                        var tmpl = document.getElementById('tpl-logList').innerHTML;
                        var doTtmpl = doT.template(tmpl);
                        var html = doTtmpl(data.data);
                        $("#tabLogList").html(html);
                    }else {
                        $("#tabLogList").append("<table id='tb_logList' style='display:none;'></table>无记录");
                    }
                }else{
                    layerError(data.message);
                }
            },
            error: function (e) {
                ajaxLogout(e.responseText,null,"装载日志列表错误，请重试!");
                top.layer.close(loadingIndex);
            }
        });
    },
    //处理
    reminderToReply:function(id,quarter,itemId){
        var self = this;
        var screen = getScreenWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_reminder_form',
            zIndex:19891016,
            title:'催单-回复',
            content: self.rootUrl+"/sd/reminder/processForm?id=" + (id || '') + "&quarter=" + quarter +"&itemId="+itemId,
            area: ['1385px', '800px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //跟单确认
    reminderToConfirm:function(id,quarter,itemId){
        var self = this;
        var screen = getScreenWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_reminder_form',
            zIndex:19891016,
            title:'催单-确认',
            content: self.rootUrl+"/sd/reminder/confirmForm?id=" + (id || '') +  "&quarter=" + quarter +"&itemId="+itemId,
            area: ['1385px', '800px'],
            // area: [screen.width-60 + 'px', screen.height-100+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //再次催单
    reReminder:function(id,orderId,quarter,orderNo,timeAt){
        var self = this;
        var reminderTimes = parseInt(timeAt)+1;
        var screen = getScreenWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_reminder_form',
            zIndex:19891016,
            title:'第'+reminderTimes+'次催单',
            content: self.rootUrl+"/sd/reminder/reReminder?id=" + (id || '') +  "&quarter=" + quarter + "&orderId=" + (orderId || ''),
            area: ['1385px', '800px'],
            // area: [screen.width-60 + 'px', screen.height-100+'px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //客服催单列表处理催单
    reminderToProcess:function(id,quarter,itemId){
        var self = this;
        var screen = getScreenWidthAndHeight();
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_reminder_form',
            zIndex:19891016,
            title:'催单-回复',
            content: self.rootUrl+"/sd/kefu/reminder/processForm?id=" + (id || '') + "&quarter=" + quarter +"&itemId="+itemId,
            area: ['1385px', '800px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //客户催单详情
    customerReminderView:function(id,quarter){
        var self = this;
        var planIndex = top.layer.open({
            type: 2,
            id:'layer_reminder_form',
            zIndex:19891016,
            title:'催单-回复',
            content: self.rootUrl+"/sd/customer/reminder/viewForm?id=" + (id || '') + "&quarter=" + quarter,
            area: ['1385px', '800px'],
            shade: 0.3,
            maxmin: false,
            success: function(layero,index){
            },
            end:function(){
            }
        });
    },
    //同望b2b完成工单重发
    canboCompleteOrderResend:function(id,b2bInterfaceId){
        var self = this;
        var canboResend = top.layer.open({
            type: 2,
            id:'layer_canbo_order_resend',
            zIndex:19891015,
            title:'同望重发',
            content: self.rootUrl+"/b2b/rpt/processlog/canboResend?id=" + id + "&b2bInterfaceId=" + (b2bInterfaceId || ''),
            area: ['1255px', screen.height-200+'px'],
            shade: 0.3,
            shadeClose:true,
            maxmin: false,
            success: function(layero,index){
            }
        });
    },
    //同望取消重试
    canboCancelOrderResend:function(id,b2bInterfaceId){
        var self = this;
        var canboResend = top.layer.open({
            type: 2,
            id:'layer_canbo_cancel_resend',
            zIndex:19891015,
            title:'同望取消重发',
            content: self.rootUrl+"/b2b/rpt/processlog/canboCancel?id=" + id + "&b2bInterfaceId=" + (b2bInterfaceId || ''),
            area: ['1255px', screen.height-200+'px'],
            shade: 0.3,
            shadeClose:true,
            maxmin: false,
            success: function(layero,index){
            }
        });
    },
    //同望预约重试
    canboAppointmentOrderResend:function(id,b2bInterfaceId){
        var self = this;
        var canboResend = top.layer.open({
            type: 2,
            id:'layer_canbo_appointment_resend',
            zIndex:19891015,
            title:'同望预约重发',
            content: self.rootUrl+"/b2b/rpt/processlog/canboAppointment?id=" + id + "&b2bInterfaceId=" + (b2bInterfaceId || ''),
            area: ['1255px', screen.height-200+'px'],
            shade: 0.3,
            shadeClose:true,
            maxmin: false,
            success: function(layero,index){
            }
        });
    },
    //定位上门地址
    locateAddr:function(serviceAddress) {
        var self = this;
        var address = encodeURI(serviceAddress);
        var locateAddress = top.layer.open({
            type: 2,
            id:'layer_location_address',
            zIndex:19891015,
            title:'地址定位',
            content: self.rootUrl+"/sd/order/kefuOrderList/locateAddress?address=" + (address || ''),
            area: ['1255px', screen.height-200+'px'],
            shade: 0.3,
            shadeClose:true,
            maxmin: false,
            success: function(layero,index){
            }
        });
    },
    getDataSourceLogo:function(ctx,dataSource){
        var html = "";
        switch (dataSource) {
            case "2":
                html = '<img style="width: 32px;" src="' + ctx + '/images/tmall_logo.png"/>';
                break;
            case "7":
                html = '<img style="width: 32px;" src="' + ctx + '/images/jd_logo.png"/>';
                break;
            case "19":
                html = '<img style="width: 32px;" src="' + ctx + '/images/viomi_logo.png"/>';
                break;
            case "22":
                html = '<img style="width: 32px;" src="' + ctx + '/images/jd_ue_plus_logo.png"/>';
                break;
            default:
                break;
        }
        return html;
    },
    //获取产品安装规范
    getProductFixSpec:function (customerId,productId,productName) {
        var self = this;
        var productFixSpec = top.layer.open({
            type: 2,
            id:'layer_product_fix',
            zIndex:19891015,
            title:productName + '-安装规范',
            content: self.rootUrl+"/sd/order/kefuOrderList/getProductFixSpec?customerId="+customerId+"&productId="+productId,
            area: ['860px','90%'],
            shade: 0.3,
            shadeClose:true,
            maxmin: false,
            success: function(layero,index){
            }
        });
    },
    hideChargeColumns:function (){
        var self = this;
        if(self.hideChargeColumn){
            //head
            $("#productTable thead tr th:eq(10)").hide();
            $("#productTable thead tr th:eq(11)").hide();
            //row
            $("#productTable tr td:nth-child(11)").hide();
            $("#productTable tr td:nth-child(12)").hide();
        }
    },
    hideChargeColumnsForEditForm:function (){
        var self = this;
        if(self.hideChargeColumn){
            //head
            $("#productTable thead tr th:eq(7)").hide();
            $("#productTable thead tr th:eq(8)").hide();
            //row
            $("#productTable tr td:nth-child(8)").hide();
            $("#productTable tr td:nth-child(9)").hide();
        }
    },
    //换货-确认收到新货品
    confirmReceived:function(id,quarter,parentIndex){
        var self = this;
        var $btn = $("#btnReceivingGoods");
        $btn.attr('disabled', 'disabled');
        var loadingIndex;
        var ajaxSuccess = 0;
        top.layer.confirm('请确认，网点或用户是否已收到新货品？', {btn: ['收到', '未收到'],icon: 3, title:'系统确认',zIndex: 19891015}, function(index){
            top.layer.close(index);//关闭本身
            // do something
            var data = { orderId : id || '' , quarter:quarter || ''};
            $.ajax({
                async: false,
                cache: false,
                type: "POST",
                url: self.rootUrl+"/sd/order/return/confirmReceived",
                data: data,
                beforeSend: function () {
                    loadingIndex = layer.msg('处理中，请稍等...', {
                        icon: 16,
                        time: 0,
                        shade: 0.3,
                        zIndex: 19891015
                    });
                },
                complete: function () {
                    if(loadingIndex) {
                        layer.close(loadingIndex);
                    }
                    if(ajaxSuccess == 0) {
                        setTimeout(function () {
                            $btn.removeAttr('disabled');
                        }, 2000);
                    }
                },
                success: function (data) {
                    if(ajaxLogout(data)){
                        return false;
                    }
                    if (data.success) {
                        if(parentIndex && parentIndex != undefined && parentIndex != ''){
                            var layero = $("#layui-layer" + parentIndex, top.document);
                            var iframeWin = top[layero.find('iframe')[0]['name']];
                            iframeWin.confirmReceivedSuccess(id,data.data);
                        }
                    }else{
                        layerError("确认收货错误:" + data.message, "错误提示");
                    }
                },
                error: function (e) {
                    ajaxLogout(e.responseText,null,"确认收货错误，请重试!");
                }
            });
            return false;
        });
        return false;

    },
    //退货/换货-完工窗体
    kefuCompleteForReturn:function(id,quarter,orderType,parentIndex){
        var self = this;
        if(orderType != '3' && orderType != '4'){
            layerError("非退换货工单不允许此操作", "错误提示");
            return;
        }
        var h = $(top.window).height();
        var w = $(top.window).width();
        var url = self.rootUrl+"/sd/order/return/completeFrom?orderId="+ id + "&quarter=" + quarter + "&parentIndex=" + (parentIndex || '');
        if(orderType === '4'){
            url = self.rootUrl+"/sd/order/return/exchange/completeFrom?orderId="+ id + "&quarter=" + quarter + "&parentIndex=" + (parentIndex || '');
        }
        top.layer.open({
            type: 2,
            id: 'layer_returnCompleteForm',
            zIndex: 19891015,
            title: '完工',
            content: url,
            area: ['936px','762px'],
            shade: 0.3,
            shadeClose: true,
            maxmin: true
        });
    },
    //验证B2B条码SN
    validB2BSN:function(dataSourceId,b2bOrderNo,sn){
        var self = this;
        var data = { dataSourceId : dataSourceId || 0 , b2bOrderNo:b2bOrderNo || '', sn:sn || ''};
        $.ajax({
            async: false,
            cache: false,
            type: "POST",
            url: self.rootUrl+"/sd/order/return/validSN",
            data: data,
            beforeSend: function () {
                loadingIndex = layer.msg('条码SN验证中，请稍等...', {
                    icon: 16,
                    time: 0,
                    shade: 0.3,
                    zIndex: 19891015
                });
            },
            complete: function () {
                if(loadingIndex) {
                    layer.close(loadingIndex);
                }
            },
            success: function (data) {
                if(ajaxLogout(data)){
                    return false;
                }
                if (data.success) {
                    return true;
                }else{
                    layerError("条码SN验证失败:" + data.message, "错误提示");
                    return false;
                }
            },
            error: function (e) {
                ajaxLogout(e.responseText,null,"条码SN验证失败，请重试!");
            }
        });
        return true;
    },
    //订单详情-加载互助基金信息
    showInsurance:function(orderId,quarter){
        var self = this;
        var parentLayerIndex = top.layer.getFrameIndex('layer_orderdetail');
        var loadingIndex = top.layer.msg('正在加载互助基金信息...', {
            icon: 16,
            time: 0,//不定时关闭
            shade: 0.3
        });

        $.ajax({
            cache: false,
            type: "GET",
            url: self.rootUrl+"/sd/order/kefuOrderList/getOrderInsurance?strOrderId=" + orderId + "&quarter=" + (quarter || ''),
            dataType: 'json',
            success: function (data) {
                top.layer.close(loadingIndex);
                if(ajaxLogout(data)){
                    return false;
                }
                if (data.success ) {
                    if(data.data && data.data.length>0) {
                        var tmpl = document.getElementById('tpl-orderInsuranceList').innerHTML;
                        var doTtmpl = doT.template(tmpl);
                        var html = doTtmpl(data.data);
                        $("#tabInsurance").html(html);
                        //让模板的自定义提示生效
                        $('a[data-toggle=tooltip]').darkTooltip();
                    }else {
                        $("#tabInsurance").empty().append("<table id='tabInsurance' style='display:none;'></table>无记录");
                    }
                }else{
                    layerError(data.message);
                }
            },
            error: function (e) {
                ajaxLogout(e.responseText,null,"装载互助信息错误，请重试!");
                top.layer.close(loadingIndex);
            }
        });
    }
};

$(function(){
    Order.init();
});

