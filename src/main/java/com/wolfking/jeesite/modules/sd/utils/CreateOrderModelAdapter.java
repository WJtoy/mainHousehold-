package com.wolfking.jeesite.modules.sd.utils;

import com.google.common.collect.Lists;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.wolfking.jeesite.common.utils.DateUtils;
import com.kkl.kklplus.utils.StringUtils;
import com.wolfking.jeesite.modules.md.entity.Customer;
import com.wolfking.jeesite.modules.md.entity.Product;
import com.wolfking.jeesite.modules.md.entity.ProductCategory;
import com.wolfking.jeesite.modules.md.entity.ServiceType;
import com.wolfking.jeesite.modules.md.utils.ProductCategoryAdapter;
import com.wolfking.jeesite.modules.sd.entity.OrderItem;
import com.wolfking.jeesite.modules.sd.entity.viewModel.CreateOrderModel;
import com.wolfking.jeesite.modules.sd.entity.viewModel.OrderItemModel;
import com.wolfking.jeesite.modules.sys.entity.Area;
import com.wolfking.jeesite.modules.sys.entity.Dict;
import com.wolfking.jeesite.modules.sys.entity.User;
import com.wolfking.jeesite.modules.sys.entity.adapter.DictSimpleAdapter;
import com.wolfking.jeesite.modules.sys.utils.LogUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * 订单自定义Gson序列化/序列化
 */
public class CreateOrderModelAdapter extends TypeAdapter<CreateOrderModel> {

    private final String  dateFormat = new String("yyyy-MM-dd HH:mm:ss");

    @Override
    public CreateOrderModel read(final JsonReader in) throws IOException {
        final CreateOrderModel order = new CreateOrderModel();
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "id":
                    order.setId(Long.valueOf(in.nextString()));
                    break;
                case "quarter":
                    if(in.peek() == JsonToken.NULL){
                        in.nextNull();
                        order.setQuarter("");
                    }else{
                        order.setQuarter(in.nextString());
                    }
                    break;
                case "orderNo":
                    order.setOrderNo(in.nextString());
                    break;
                case "customer":
                    in.beginObject();
                    Customer customer = new Customer();
                    while (in.hasNext()) {
                        switch (in.nextName()) {
                            case "id":
                                customer.setId(in.nextLong());
                                break;
                            case "name":
                                customer.setName(in.nextString());
                                break;
                            case "urgentFlag":
                                customer.setUrgentFlag(in.nextInt());
                                break;
                        }
                    }
                    order.setCustomer(customer);
                    in.endObject();
                    break;
                case "area":
                    in.beginObject();
                    Area area = new Area();
                    while (in.hasNext()) {
                        switch (in.nextName()) {
                            case "id":
                                area.setId(in.nextLong());
                                break;
                            case "name":
                                area.setName(in.nextString());
                                break;
                        }
                    }
                    order.setArea(area);
                    in.endObject();
                    break;
                case "userName":
                    order.setUserName(in.nextString());
                    break;
                case "phone1":
                    order.setPhone1(in.nextString());
                    break;
                //case "servicePhone":
                //    order.setServicePhone(in.nextString());
                //    break;
                case "address":
                    order.setAddress(in.nextString());
                    break;
                case "description":
                    order.setDescription(in.nextString());
                    break;
                //case "serviceAddress":
                //    order.setServiceAddress(in.nextString());
                //    break;
                case "expectCharge":
                    order.setExpectCharge(in.nextDouble());
                    break;
                case "blockedCharge":
                    order.setBlockedCharge(in.nextDouble());
                    break;
                case "balanceCharge":
                    order.setBalanceCharge(in.nextDouble());
                    break;
                case "customerUrgentCharge":
                    order.setCustomerUrgentCharge(in.nextDouble());
                    break;
                case "engineerUrgentCharge":
                    order.setEngineerUrgentCharge(in.nextDouble());
                    break;
                case "totalQty":
                    order.setTotalQty(in.nextInt());
                    break;
                case "orderServiceType":
                    order.setOrderPaymentType(DictSimpleAdapter.getInstance().read(in));
                    break;
                case "createBy":
                    if(in.peek() == JsonToken.NULL){
                        in.nextNull();
                        order.setCreateBy(null);
                    }else{
                        User createBy = new User();
                        in.beginObject();
                        while (in.hasNext()) {
                            switch (in.nextName()) {
                                case "id":
                                    if(in.peek() == JsonToken.NULL) {
                                        in.nextNull();
                                        createBy.setId(null);
                                    }else {
                                        createBy.setId(in.nextLong());
                                    }
                                    break;
                                case "name":
                                    createBy.setName(in.nextString());
                                    break;
                            }
                        }
                        order.setCreateBy(createBy);
                        in.endObject();
                    }
                    break;
                case "createDate":
                    StringBuilder strd = new StringBuilder();
                    strd.append(in.nextString());
                    if(StringUtils.isBlank(strd)){
                        order.setCreateDate(null);
                    }else{
                        try{
                            Date date = DateUtils.parse(strd.toString(),dateFormat);
                            order.setCreateDate(date);
                        } catch (ParseException e) {
                            order.setCreateDate(null);
                            try {
                                LogUtils.saveLog("日期格式错误:", "CreateOrderModelAdapter.read", String.format("id:%s,createDate:%s", order.getItems(), strd.toString()), e, null);
                            }catch (Exception e1){}
                        }
                    }
                    strd.setLength(0);
                    break;
                //Items
                case "items":
                    in.beginArray();
                    final List items = Lists.newArrayList();
                    while (in.hasNext()) {
                        items.add(OrderItemAdapter.getInstance().read(in));//调用OrderItem的序列化类
                    }
                    order.setItems(items);
                    in.endArray();
                    break;
                case "category":
                    if(in.peek() == JsonToken.NULL){
                        in.nextNull();
                        order.setCategory(null);
                    }else{
                        order.setCategory(ProductCategoryAdapter.getInstance().read(in));
                    }
                    break;
            }
        }
        in.endObject();
        return order;
    }

    @Override
    public void write(final JsonWriter out, final CreateOrderModel order) throws IOException {
        out.beginObject();
        out.name("id").value(order.getId().toString())
            .name("quarter").value(order.getQuarter())
            .name("orderNo").value(order.getOrderNo());
        if (order.getCustomer() != null){
            out.name("customer")
                    .beginObject()
                    .name("id").value(order.getCustomer().getId().toString())
                    .name("name").value(order.getCustomer().getName())
                    .name("urgentFlag").value(order.getCustomer().getUrgentFlag())
                    .endObject();
        }
        //用户
        if(order.getArea()!=null) {
            out.name("area")
                    .beginObject()
                    .name("id").value(order.getArea().getId())
                    .name("name").value(order.getArea().getName())
                    .endObject();
        }
        out.name("userName").value(order.getUserName())
                .name("phone1").value(order.getPhone1())
                .name("address").value(order.getAddress())
                .name("description").value(StringUtils.isBlank(order.getDescription())?"":order.getDescription())
                .name("expectCharge").value(order.getExpectCharge())
                .name("blockedCharge").value(order.getBlockedCharge())
                .name("balanceCharge").value(order.getBlockedCharge())
                .name("customerUrgentCharge").value(order.getCustomerUrgentCharge())
                .name("engineerUrgentCharge").value(order.getEngineerUrgentCharge())
                .name("totalQty").value(order.getTotalQty());
        //orderServiceType
        out.name("orderServiceType");
        DictSimpleAdapter.getInstance().write(out,order.getOrderPaymentType());

        if(order.getCreateBy() != null){
            out.name("createBy")
                    .beginObject()
                    .name("id").value(order.getCreateBy().getId()==null?0l:order.getCreateBy().getId())
                    .name("name").value(order.getCreateBy().getName())
                    .endObject();
        }
        if(order.getCreateDate() != null){
            out.name("createDate").value(DateUtils.formatDate(order.getCreateDate(),dateFormat));
        }
        if(order.getItems() != null && order.getItems().size()>0) {
            out.name("items").beginArray();
            for (final OrderItem item : order.getItems()) {
                OrderItemAdapter.getInstance().write(out, item);
            }
            out.endArray();
        }
        if(order.getCategory() != null && order.getCategory().getId() != null){
            out.name("category");
            ProductCategoryAdapter.getInstance().write(out,order.getCategory());
        }
        out.endObject();
    }

    private static CreateOrderModelAdapter adapter;
    public CreateOrderModelAdapter() {}
    public static CreateOrderModelAdapter getInstance() {
        if (adapter == null){
            adapter = new CreateOrderModelAdapter();
        }
        return adapter;
    }

    /*
    public static void main(String[] args) throws IOException {
        long orderId = 1235645123131l;
        CreateOrderModel model = new CreateOrderModel();
        model.setId(orderId);
        model.setQuarter("20201");
        model.setOrderNo("K20200226000001");
        Customer c = new Customer();
        c.setId(1l);
        c.setName("厂商");
        c.setUrgentFlag(1);
        model.setCustomer(c);
        Area a = new Area();
        a.setId(3403l);
        a.setName("深圳市");
        model.setArea(a);
        model.setUserName("用户");
        model.setPhone1("13800000000");
        model.setAddress("用户住址");
        model.setDescription("描述");
        model.setExpectCharge(10.0);
        model.setBlockedCharge(1.0);
        model.setBalanceCharge(20.0);
        model.setCustomerUrgentCharge(15.0);
        model.setEngineerUrgentCharge(10.0);
        model.setOrderPaymentType(new Dict("1","月结"));
        User user = new User(1l);
        user.setName("管理员");
        model.setCreateBy(user);
        model.setCreateDate(new Date());
        //items
        //product
        Product p = new Product();
        p.setId(10l);
        p.setName("产品");
        p.setSetFlag(0);
        ProductCategory pc = new ProductCategory();
        pc.setId(100l);
        pc.setName("品类");
        p.setCategory(pc);
        //serviceType
        ServiceType st = new ServiceType();
        st.setId(1l);
        st.setName("安装");

        OrderItemModel item = new OrderItemModel();
        item.setId(1l);
        item.setOrderId(orderId);
        item.setDelFlag(0);
        item.setItemNo(1);
        item.setProduct(p);
        item.setBrand("品牌");
        item.setProductSpec("规格");
        item.setServiceType(st);
        item.setStandPrice(10.0);
        item.setDiscountPrice(8.0);
        item.setQty(1);
        item.setCharge(10.0);
        item.setBlockedCharge(0.0);
        item.setExpressCompany(new Dict("yt","圆通"));
        item.setExpressNo("快递单号");
        item.setRemarks("item备注");
        item.setB2bProductCode("b2b product code");
        item.setCurrentUser(user);

        model.setItems(Lists.newArrayList(item));
        model.setCategory(pc);

        StringBuilder json = new StringBuilder();
        json.append(CreateOrderModelAdapter.getInstance().toJson(model));
        System.out.println(json.toString());

        CreateOrderModel m = CreateOrderModelAdapter.getInstance().fromJson(json.toString());
        json.setLength(0);
        json.append(CreateOrderModelAdapter.getInstance().toJson(m));
        System.out.println(json.toString());
    }
    */
}
