package order_service.application.service;

import order_service.domain.model.Order;
import order_service.domain.model.OrderProduct;
import order_service.domain.model.OrderStatus;
import order_service.domain.repository.OrderRepository;
import order_service.domain.repository.OrderProductRepository;
import order_service.infrastructure.client.DeliveryPaymentClient;
import order_service.infrastructure.client.ProductClient;
import order_service.infrastructure.client.UserClient;
import order_service.infrastructure.client.EmailClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private UserClient userClient;

    @Autowired
    private EmailClient emailClient;

    @Autowired
    private DeliveryPaymentClient deliveryPaymentClient;


    public Order createOrder(Long clientId, List<OrderProduct> orderProducts, double finalAmount, String jwtToken) {
        if (!userClient.checkClientExists(clientId, jwtToken)) {
            throw new RuntimeException("Client not found");
        }

        Order order = new Order();
        order.setClientId(clientId);
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.CREATED);
        order.setTotalPrice(finalAmount);

        for (OrderProduct op : orderProducts) {
            op.setOrder(order);
        }

        order.setOrderProducts(orderProducts);

        return orderRepository.save(order);
    }

    public Order markOrderAsCompleted(Long orderId, String jwtToken) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            return order;
        }

        order.setStatus(OrderStatus.COMPLETED);
        Order completedOrder = orderRepository.save(order);

        sendOrderConfirmation(completedOrder, jwtToken);
        return completedOrder;
    }

    private void sendOrderConfirmation(Order order, String jwtToken) {
        String email = userClient.getClientEmailById(order.getClientId(), jwtToken);
        StringBuilder sb = new StringBuilder();

        sb.append("Hello from Maison Belle,\n\n");
        sb.append("We’re delighted to let you know that your order has been placed successfully! ")
                .append("Thank you for choosing us for your beauty and fashion needs. 💐\n\n");

        sb.append("Here are the details of your order:\n");
        sb.append("Order ID: ").append(order.getId()).append("\n");
        sb.append("Date: ").append(order.getOrderDate()).append("\n");
        sb.append("Total: ").append(String.format("%.2f", order.getTotalPrice())).append(" $\n\n");

        sb.append("Products:\n");
        for (var op : order.getOrderProducts()) {
            sb.append("- ").append(op.getProductName())
                    .append(" (").append(op.getBrandName()).append("), ")
                    .append("Quantity: ").append(op.getQuantity())
                    .append(", Price: ").append(String.format("%.2f", op.getSalePrice())).append(" $\n");

            if (op.getShade() != null) {
                sb.append("   Shade: ").append(op.getShade()).append("\n");
            }
            if (op.getSize() != null) {
                sb.append("   Size: ").append(op.getSize()).append("\n");
            }
        }

        if (order.getGiftWrap() != null && order.getGiftWrap()) {
            sb.append("\nGift wrap: YES (+$3)\n");
        }

        if (order.getPersonalizedMessage() != null && !order.getPersonalizedMessage().isBlank()) {
            sb.append("\nPersonalized Message:\n");
            sb.append("\"").append(order.getPersonalizedMessage()).append("\"\n");
        }

        try {
            var delivery = deliveryPaymentClient.getDeliveryByOrderId(order.getId(), jwtToken);
            if (delivery != null) {
                sb.append("\nDelivery Details:\n");
                sb.append("   Method: ").append(delivery.getMethod()).append("\n");
                if (delivery.getCourierCompany() != null) {
                    sb.append("   Courier: ").append(delivery.getCourierCompany()).append("\n");
                }
                if (delivery.getEasyboxCompany() != null) {
                    sb.append("   Easybox Company: ").append(delivery.getEasyboxCompany()).append("\n");
                    sb.append("   Easybox Location: ").append(delivery.getEasyboxLocation()).append("\n");
                }
                if (delivery.getPickupLocation() != null) {
                    sb.append("   Pickup Store: ").append(delivery.getPickupLocation()).append("\n");
                }
                if (delivery.getAwb() != null) {
                    sb.append("   AWB: ").append(delivery.getAwb()).append("\n");
                }
                if (delivery.getAddressId() != null) {
                    var address = deliveryPaymentClient.getAddressById(delivery.getAddressId(), jwtToken);
                    if (address != null) {
                        sb.append("   Address: ")
                                .append(address.getStreet()).append(", ")
                                .append(address.getCity()).append(", ")
                                .append(address.getCounty()).append(", ")
                                .append(address.getCountry())
                                .append(" (").append(address.getPostalCode()).append(")\n");
                    }
                }
            }
        } catch (Exception e) {
            sb.append("\nCould not fetch delivery details.\n");
        }

        sb.append("\nYour order is now being processed and will soon be on its way. ")
                .append("We’ll notify you once it has been shipped.\n\n");

        sb.append("We truly appreciate your trust in Maison Belle and can’t wait to delight you again ")
                .append("with our latest collections. See you soon for your next shopping experience!\n\n");

        sb.append("With love,\n")
                .append("The Maison Belle Team 🌸\n");

        emailClient.sendEmailToClient(order.getClientId(), jwtToken,
                "Maison Belle - Order Confirmation #" + order.getId(), sb.toString());

        try {
            byte[] pdfBytes = generateInvoicePdf(order, jwtToken);
            String fileName = "bill_" + order.getId() + ".pdf";

            emailClient.sendEmailWithPdfToClient(order.getClientId(), jwtToken,
                    "Invoice for Order #" + order.getId(),
                    "Attached is your invoice for order #" + order.getId() + ".\n\n"
                            + "We’re excited to have you as part of the Maison Belle family!",
                    fileName, pdfBytes);
        } catch (Exception e) {
            System.err.println("Failed to send invoice PDF: " + e.getMessage());
        }
    }


    public List<Order> getOrdersByClient(Long clientId) {
        return orderRepository.findByClientIdAndStatusIn(
                clientId,
                List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED, OrderStatus.RETURN_REQUESTED)
        );
    }

    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("Order already completed, cannot be cancelled.");
        }
        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    public void deleteIncompleteOrdersForClient(Long clientId) {
        List<Order> incompleteOrders = orderRepository.findByClientIdAndStatus(clientId, OrderStatus.CREATED);
        orderRepository.deleteAll(incompleteOrders);
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findOrderWithProductsById(id);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public boolean deleteOrder(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            return false;
        }
        orderRepository.deleteById(orderId);
        return true;
    }

    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    public Order cancelOrderWithEmail(Long orderId, String jwtToken) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order is already cancelled.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);

        sendCancellationEmail(cancelledOrder, jwtToken);

        return cancelledOrder;
    }

    private void sendCancellationEmail(Order order, String jwtToken) {
        try {
            String email = userClient.getClientEmailById(order.getClientId(), jwtToken);
            StringBuilder sb = new StringBuilder();

            sb.append("Hello from Maison Belle,\n\n");
            sb.append("We would like to confirm that your order has been successfully cancelled. ")
                    .append("We’re sorry to see it go, but we completely understand — sometimes plans change.\n\n");

            sb.append("Here are the details of your cancelled order:\n");
            sb.append("Order ID: ").append(order.getId()).append("\n");
            sb.append("Date: ").append(order.getOrderDate()).append("\n");
            sb.append("Total Value: ").append(order.getTotalPrice()).append(" $\n\n");

            sb.append("Cancelled Products:\n");
            for (var op : order.getOrderProducts()) {
                sb.append("- ").append(op.getProductName())
                        .append(" (").append(op.getBrandName()).append("), ")
                        .append("Quantity: ").append(op.getQuantity())
                        .append(", Price: ").append(op.getSalePrice()).append(" $\n");

                if (op.getShade() != null) {
                    sb.append("   Shade: ").append(op.getShade()).append("\n");
                }
                if (op.getSize() != null) {
                    sb.append("   Size: ").append(op.getSize()).append("\n");
                }
            }

            sb.append("\nDon’t worry — if you change your mind, you’re always welcome back on Maison Belle. ")
                    .append("Our latest collections of beauty, fashion, and skincare are waiting for you!\n\n");

            sb.append("With love,\n")
                    .append("The Maison Belle Team 💐\n");

            emailClient.sendEmailToClient(order.getClientId(), jwtToken,
                    "Maison Belle - Order Cancelled #" + order.getId(), sb.toString());

        } catch (Exception e) {
            System.err.println("Error while sending cancellation email: " + e.getMessage());
        }
    }

    private byte[] generateInvoicePdf(Order order, String jwtToken) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.add(new Paragraph("Invoice - Order #" + order.getId())
                    .setBold().setFontSize(16));

            document.add(new Paragraph("Date: " + order.getOrderDate()));
            document.add(new Paragraph("Total: " + order.getTotalPrice() + " $"));
            document.add(new Paragraph(" "));

            Table table = new Table(new float[]{4, 2, 2});
            table.addHeaderCell("Product");
            table.addHeaderCell("Quantity");
            table.addHeaderCell("Price");

            for (var op : order.getOrderProducts()) {
                table.addCell(op.getProductName() + " (" + op.getBrandName() + ")");
                table.addCell(String.valueOf(op.getQuantity()));
                table.addCell(op.getSalePrice() + " $");
            }

            document.add(table);
            var delivery = deliveryPaymentClient.getDeliveryByOrderId(order.getId(), jwtToken);
            if (delivery != null) {
                document.add(new Paragraph(" "));
                document.add(new Paragraph("Delivery Method: " + delivery.getMethod()));

                if (delivery.getCourierCompany() != null) {
                    document.add(new Paragraph("Courier: " + delivery.getCourierCompany()));
                }
                if (delivery.getEasyboxCompany() != null) {
                    document.add(new Paragraph("Easybox: " + delivery.getEasyboxCompany() +
                            " (" + delivery.getEasyboxLocation() + ")"));
                }
                if (delivery.getPickupLocation() != null) {
                    document.add(new Paragraph("Pickup Store: " + delivery.getPickupLocation()));
                }
                if (delivery.getAwb() != null) {
                    document.add(new Paragraph("AWB: " + delivery.getAwb()));
                }

                if (delivery.getAddressId() != null) {
                    var address = deliveryPaymentClient.getAddressById(delivery.getAddressId(), jwtToken);
                    if (address != null) {
                        document.add(new Paragraph("Address: " +
                                address.getStreet() + ", " +
                                address.getCity() + ", " +
                                address.getCounty() + ", " +
                                address.getCountry() + " (" +
                                address.getPostalCode() + ")"));
                    }
                }
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF invoice: " + e.getMessage(), e);
        }
    }

    public Order requestReturn(Long orderId, String jwtToken) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.RETURN_REQUESTED) {
            throw new RuntimeException("Return request already submitted.");
        }

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new RuntimeException("Only completed orders can be returned.");
        }

        order.setStatus(OrderStatus.RETURN_REQUESTED);
        Order returnedOrder = orderRepository.save(order);

        sendReturnEmail(returnedOrder, jwtToken);

        return returnedOrder;
    }

    private void sendReturnEmail(Order order, String jwtToken) {
        try {
            String email = userClient.getClientEmailById(order.getClientId(), jwtToken);
            StringBuilder sb = new StringBuilder();

            sb.append("Hello from Maison Belle,\n\n");
            sb.append("We confirm that your return request has been successfully registered. ")
                    .append("The courier company will contact you shortly to pick up your package.\n\n");

            sb.append("Return Request Details:\n");
            sb.append("Order ID: ").append(order.getId()).append("\n");
            sb.append("Date: ").append(order.getOrderDate()).append("\n");
            sb.append("Total Value: ").append(String.format("%.2f", order.getTotalPrice())).append(" $\n\n");

            sb.append("Products to be returned:\n");
            for (var op : order.getOrderProducts()) {
                sb.append("- ").append(op.getProductName())
                        .append(" (").append(op.getBrandName()).append("), ")
                        .append("Quantity: ").append(op.getQuantity())
                        .append(", Price: ").append(String.format("%.2f", op.getSalePrice())).append(" $\n");

                if (op.getShade() != null) {
                    sb.append("   Shade: ").append(op.getShade()).append("\n");
                }
                if (op.getSize() != null) {
                    sb.append("   Size: ").append(op.getSize()).append("\n");
                }
            }

            sb.append("\nThank you for shopping with Maison Belle. We hope to see you again soon.\n\n");
            sb.append("With love,\nThe Maison Belle Team 🌸");

            emailClient.sendEmailToClient(order.getClientId(), jwtToken,
                    "Maison Belle - Return Request Confirmation #" + order.getId(), sb.toString());

        } catch (Exception e) {
            System.err.println("Error while sending return email: " + e.getMessage());
        }
    }


}
