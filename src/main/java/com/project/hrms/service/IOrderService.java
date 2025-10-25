package com.project.hrms.service;

import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.models.Order;

import java.util.List;

public interface IOrderService {

    Order createOrder(OrderDTO orderDTO);

    Order getOrderById(Long id);

    Order updateOrder(Long id, OrderDTO orderDTO);

    void deleteOrder(Long id);

    List<Order> getAllOrderByUser_Id(Long userId);
}
